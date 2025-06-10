package io.mosip.pms.tasklets;

import java.security.cert.X509Certificate;
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
import io.mosip.pms.common.dto.FtmDetailsDto;
import io.mosip.pms.common.entity.NotificationEntity;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.device.authdevice.entity.FTPChipDetail;
import io.mosip.pms.device.authdevice.repository.FTPChipDetailRepository;
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
public class FTMChipCertificateExpiryTasklet implements Tasklet {

	private Logger log = PMSLogger.getLogger(FTMChipCertificateExpiryTasklet.class);

	@Value("#{'${mosip.pms.batch.job.ftm.chip.cert.expiry.periods}'.split(',')}")
	private List<Integer> ftmChipCertExpiryPeriods;

	@Value("${mosip.pms.ca.signed.partner.certificate.available}")
	private Boolean isCaSignedFtmChipCertificateAvailable;

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
	FTPChipDetailRepository ftpChipDetailRepository;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		log.info("FTMChipCertificateExpiryTasklet: START");
		// Check if getPartnerSignedCertificate() endpoint is available
		if (Boolean.FALSE.equals(isCaSignedFtmChipCertificateAvailable)) {
			log.info(
					"FTMChipCertificateExpiryTasklet: Notifications cannot be generated since the \"getPartnerSignedCertificate()\" endpoint is not available in the current version of KeyManager.");
			return RepeatStatus.FINISHED;
		}
		List<String> totalNotificationsCreated = new ArrayList<String>();
		int ftmProvidersCount = 0;
		int countOfCertsExpiringWithin30Days = 0;
		int countOfPartnersWithInvalidCerts = 0;
		try {
			// Step 1: Get all FTM Providers which are Active and Approved
			List<Partner> ftmProvidersList = batchJobHelper.getAllActiveAndApprovedFtmProviders();
			ftmProvidersCount = ftmProvidersList.size();
			log.info("PMS has {} FTM Providers which are Active and Approved.", ftmProvidersCount);
			// Step 2: For each FTM Provider get all the FTM chips which are Active and
			// Approved
			Iterator<Partner> ftmProvidersListIterator = ftmProvidersList.iterator();
			while (ftmProvidersListIterator.hasNext()) {
				Partner ftmProvider = ftmProvidersListIterator.next();
				String ftmProviderId = ftmProvider.getId();
				log.info("Fetching all the FTM chips for the FTM provider id {}", ftmProviderId);
				List<FTPChipDetail> ftpChipDetailList = ftpChipDetailRepository
						.findAllActiveAndApprovedFtmChipDetailsByProviderId(ftmProviderId);
				int ftmChipDetailsCount = ftpChipDetailList.size();
				log.info("Found {} FTM chip details which are Active and Approved.", ftmChipDetailsCount);
				for (FTPChipDetail ftpChipDetail : ftpChipDetailList) {
					// Step 3: For each FTM chip get the cerificate and check if it is expiring or
					// not
					if (ftpChipDetail.getCertificateAlias() != null) {
						X509Certificate decodedFtmCert = partnerCertificateExpiryHelper
								.getDecodedFtmCertificate(ftpChipDetail);
						if (decodedFtmCert != null) {
							log.info("Checking if FTM chip certificate is expiring for FTM provider id {}",
									ftmProviderId + " within next 30 days.");
							LocalDateTime ftmChipCertificateExpiryDate = partnerCertificateExpiryHelper
									.getCertificateExpiryDateTime(decodedFtmCert);
							log.info("The FTM chip certificate expiry date is {}", ftmChipCertificateExpiryDate);
							boolean isExpiringWithin30Days = partnerCertificateExpiryHelper
									.checkIfCertificateIsExpiring(ftmProvider, ftmChipCertificateExpiryDate, 30, true);
							if (isExpiringWithin30Days) {
								countOfCertsExpiringWithin30Days++;
								log.info("FTM chip certificate is expiring for FTM provider id {}",
										ftmProviderId + " during the next 30 days.");
								// Step 4: Check if the certificate is expiring after 30 days, 15 days, 10 days,
								// 9 days and so on, if yes create notifications
								Iterator<Integer> expiryPeriodsIterator = ftmChipCertExpiryPeriods.iterator();
								while (expiryPeriodsIterator.hasNext()) {
									Integer expiryPeriod = expiryPeriodsIterator.next();
									log.info("Checking for FTM chip certificate expiry after " + expiryPeriod
											+ " days.");
									boolean isExpiringAfterExpiryPeriod = partnerCertificateExpiryHelper
											.checkIfCertificateIsExpiring(ftmProvider, ftmChipCertificateExpiryDate,
													expiryPeriod, false);
									// Step 5: If yes, add the notification
									if (isExpiringAfterExpiryPeriod) {
										List<FtmDetailsDto> expiringFtmList = new ArrayList<FtmDetailsDto>();
										FtmDetailsDto ftmDetailsDto = partnerCertificateExpiryHelper.populateFtmDetails(expiryPeriod, ftpChipDetail,
												decodedFtmCert);
										expiringFtmList.add(ftmDetailsDto);
										// Decrypt the email ID if it's already encrypted to avoid encrypting it again
										String decryptedEmailId = keyManagerHelper
												.decryptData(ftmProvider.getEmailId());
										NotificationEntity savedNotification = batchJobHelper.saveNotification(
												PartnerConstants.FTM_CHIP_CERT_EXPIRY, ftmProvider, null,
												expiringFtmList, decryptedEmailId);
										// Step 6: send email notification
										emailNotificationService.sendEmailNotification(savedNotification,
												decryptedEmailId);
										log.info(
												"Created FTM chip certificate expiry notification with notification id "
														+ savedNotification.getId());
										totalNotificationsCreated.add(savedNotification.getId());
										break;
									} else {
										log.info(" FTM chip certificate is NOT expiring  after " + expiryPeriod
												+ " days.");
										// check for next time interval
									}
								}
							} else {
								log.info("FTM chip certificate is NOT expiring for FTM provider id {}",
										ftmProviderId + " during the next 30 days.");
							}
						} else {
							countOfPartnersWithInvalidCerts++;
							log.debug("Valid FTM chip certificate not found for FTM provider id {}", ftmProviderId);
						}
					} else {
						countOfPartnersWithInvalidCerts++;
						log.info("Skipping this FTM chip since certificate alias are missing {}",
								ftpChipDetail.getFtpChipDetailId());
					}
				}
			}
		} catch (Exception e) {
			log.error("Error occurred while running FTMChipCertificateExpiryTasklet: {}", e.getMessage(), e);
		}
		log.info("Overall found " + countOfCertsExpiringWithin30Days
				+ " FTM chip certificates which are expiring during the next 30 days. But notifications will only be created as per the configured expiry days.");
		log.info("FTMChipCertificateExpiryTasklet: DONE, created {}",
				totalNotificationsCreated.size() + " notifications." + " Checked FTM chip certificate expiry for "
						+ ftmProvidersCount + " FTM providers.");
		totalNotificationsCreated.forEach(notificationId -> {
			log.info(notificationId);
		});
		if (countOfPartnersWithInvalidCerts > 0) {
			log.info("Note: Valid FTM chip certificate is not available for " + countOfPartnersWithInvalidCerts
					+ " FTM providers. ");
		}
		return RepeatStatus.FINISHED;
	}

	
}