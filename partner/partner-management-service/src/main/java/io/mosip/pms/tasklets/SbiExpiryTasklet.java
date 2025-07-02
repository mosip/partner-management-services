package io.mosip.pms.tasklets;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.dto.SbiDetailsDto;
import io.mosip.pms.common.entity.NotificationEntity;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.device.authdevice.entity.SecureBiometricInterface;
import io.mosip.pms.device.authdevice.repository.SecureBiometricInterfaceRepository;
import io.mosip.pms.partner.util.PartnerHelper;
import io.mosip.pms.tasklets.service.EmailNotificationService;
import io.mosip.pms.tasklets.util.BatchJobHelper;
import io.mosip.pms.tasklets.util.KeyManagerHelper;
import io.mosip.pms.tasklets.util.KeycloakHelper;
import io.mosip.pms.tasklets.util.PartnerCertificateExpiryHelper;

/**
 * This Batch Job will create notifications for the all FTM chip certificates
 * expiring as per the configured period.
 * 
 * @author Mayura Deshmukh
 * @since 1.3.x
 */
@Component
public class SbiExpiryTasklet implements Tasklet {

	private Logger log = PMSLogger.getLogger(SbiExpiryTasklet.class);

	@Value("#{'${mosip.pms.batch.job.sbi.expiry.periods}'.split(',')}")
	private List<Integer> sbiExpiryPeriods;

	@Autowired
	KeycloakHelper keycloakHelper;

	@Autowired
	PartnerHelper partnerHelper;

	@Autowired
	BatchJobHelper batchJobHelper;

	@Autowired
	EmailNotificationService emailNotificationService;

	@Autowired
	PartnerCertificateExpiryHelper partnerCertificateExpiryHelper;

	@Autowired
	KeyManagerHelper keyManagerHelper;

	@Autowired
	SecureBiometricInterfaceRepository sbiRepository;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		log.info("SbiExpiryTasklet: START");
		List<String> totalNotificationsCreated = new ArrayList<String>();
		int deviceProvidersCount = 0;
		int countOfSbiExpiringWithin30Days = 0;
		try {
			// Step 1: Get all Device Providers which are Active and Approved
			List<Partner> deviceProvidersList = batchJobHelper
					.getAllActiveAndApprovedPartners(PartnerConstants.DEVICE_PROVIDER_PARTNER_TYPE);
			deviceProvidersCount = deviceProvidersList.size();
			log.info("PMS has {} Device Providers which are Active and Approved.", deviceProvidersCount);
			// Step 2: For each Device Provider get all the SBI's which are Active and
			// Approved
			Iterator<Partner> deviceProvidersListIterator = deviceProvidersList.iterator();
			while (deviceProvidersListIterator.hasNext()) {
				Partner deviceProvider = deviceProvidersListIterator.next();
				String deviceProviderId = deviceProvider.getId();
				log.info("Fetching all the SBI's for the device provider id {}", deviceProviderId);
				List<SecureBiometricInterface> sbiList = sbiRepository
						.findAllActiveAndApprovedByProviderId(deviceProviderId);
				int sbiCount = sbiList.size();
				log.info("Found {} SBIs which are Active and Approved.", sbiCount);
				for (SecureBiometricInterface sbiDetail : sbiList) {
					// Step 3: For each SBI check if it is expiring or not
					if (sbiDetail.getSwExpiryDateTime() != null) {
						log.info("Checking if SBI is expiring for SBI provider id {}",
								deviceProviderId + " within next 30 days.");
						LocalDateTime sbiExpiryDateTime = sbiDetail.getSwExpiryDateTime();
						log.info("The SBI expiry date is {}", sbiExpiryDateTime);
						boolean isExpiringWithin30Days = partnerCertificateExpiryHelper.checkIfExpiring(deviceProvider,
								sbiExpiryDateTime, 30, true);
						if (isExpiringWithin30Days) {
							countOfSbiExpiringWithin30Days++;
							log.info("SBI is expiring for device provider id {}",
									deviceProviderId + " during the next 30 days.");
							// Step 4: Check if the SBI is expiring after 30 days, 15 days, 10 days,
							// 9 days and so on, if yes create notifications
							Iterator<Integer> expiryPeriodsIterator = sbiExpiryPeriods.iterator();
							while (expiryPeriodsIterator.hasNext()) {
								Integer expiryPeriod = expiryPeriodsIterator.next();
								log.info("Checking for SBI expiry after " + expiryPeriod + " days.");
								boolean isExpiringAfterExpiryPeriod = partnerCertificateExpiryHelper
										.checkIfExpiring(deviceProvider, sbiExpiryDateTime, expiryPeriod, false);
								// Step 5: If yes, add the notification
								if (isExpiringAfterExpiryPeriod) {
									List<SbiDetailsDto> expiringSbiList = new ArrayList<SbiDetailsDto>();
									SbiDetailsDto sbiDetailsDto = partnerCertificateExpiryHelper
											.populateSbiDetails(expiryPeriod, sbiDetail);
									expiringSbiList.add(sbiDetailsDto);
									// Decrypt the email ID if it's already encrypted to avoid encrypting it again
									String decryptedEmailId = keyManagerHelper.decryptData(deviceProvider.getEmailId());
									NotificationEntity savedNotification = batchJobHelper.saveNotification(
											PartnerConstants.SBI_EXPIRY_NOTIFICATION_TYPE, deviceProvider, null, null,
											expiringSbiList, null, decryptedEmailId);
									// Step 6: send email notification
									emailNotificationService.sendEmailNotification(savedNotification, decryptedEmailId);
									log.info("Created SBI expiry notification with notification id "
											+ savedNotification.getId());
									totalNotificationsCreated.add(savedNotification.getId());
									break;
								} else {
									log.info("SBI is NOT expiring  after " + expiryPeriod + " days.");
									// check for next time interval
								}
							}
						} else {
							log.info("SBI is NOT expiring for device provider id {}",
									deviceProviderId + " during the next 30 days.");
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("Error occurred while running SbiExpiryTasklet: {}", e.getMessage(), e);
		}
		log.info("Overall found " + countOfSbiExpiringWithin30Days
				+ " SBIs which are expiring during the next 30 days. But notifications will only be created as per the configured expiry days.");
		log.info("SbiExpiryTasklet: DONE, created {}", totalNotificationsCreated.size() + " notifications."
				+ " Checked SBI expiry for " + deviceProvidersCount + " device providers.");
		totalNotificationsCreated.forEach(notificationId -> {
			log.info(notificationId);
		});
		return RepeatStatus.FINISHED;
	}

}