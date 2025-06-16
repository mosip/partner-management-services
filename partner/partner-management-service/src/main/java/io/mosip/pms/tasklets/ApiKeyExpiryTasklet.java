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
import io.mosip.pms.common.dto.ApiKeyDetailsDto;
import io.mosip.pms.common.entity.NotificationEntity;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.entity.PartnerPolicy;
import io.mosip.pms.common.repository.PartnerPolicyRepository;
import io.mosip.pms.common.util.PMSLogger;
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
public class ApiKeyExpiryTasklet implements Tasklet {

	private Logger log = PMSLogger.getLogger(ApiKeyExpiryTasklet.class);

	@Value("#{'${mosip.pms.batch.job.api.key.expiry.periods}'.split(',')}")
	private List<Integer> apiKeyExpiryPeriods;

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
	PartnerPolicyRepository partnerPolicyRepository;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		log.info("ApiKeyExpiryTasklet: START");
		List<String> totalNotificationsCreated = new ArrayList<String>();
		int authPartnersCount = 0;
		int countOfApiKeysExpiringWithin30Days = 0;
		try {
			// Step 1: Get all Auth Partners which are Active and Approved
			List<Partner> authPartnersList = batchJobHelper
					.getAllActiveAndApprovedPartners(PartnerConstants.AUTH_PARTNER_TYPE);
			authPartnersCount = authPartnersList.size();
			log.info("PMS has {} Auth Partners which are Active and Approved.", authPartnersCount);
			// Step 2: For each Auth Partner get all the API Keys which are Active and
			// Approved
			Iterator<Partner> authPartnersListIterator = authPartnersList.iterator();
			while (authPartnersListIterator.hasNext()) {
				Partner authPartner = authPartnersListIterator.next();
				String authPartnerId = authPartner.getId();
				log.info("Fetching all the API Keys for the auth partner id {}", authPartnerId);
				List<PartnerPolicy> apiKeyList = partnerPolicyRepository.findByPartnerIdAndIsActiveTrue(authPartnerId);
				int apiKeysCount = apiKeyList.size();
				log.info("Found {} API Keys which are Active.", apiKeysCount);
				for (PartnerPolicy apiKeyDetails : apiKeyList) {
					// Step 3: For each API Key check if it is expiring or not
					if (apiKeyDetails.getValidToDatetime() != null) {
						log.info("Checking if API key is expiring for auth partner id {}",
								authPartnerId + " within next 30 days.");
						LocalDateTime apiKeyExpiryDateTime = apiKeyDetails.getValidToDatetime().toLocalDateTime();
						log.info("The API key expiry date is {}", apiKeyExpiryDateTime);
						boolean isExpiringWithin30Days = partnerCertificateExpiryHelper.checkIfExpiring(authPartner,
								apiKeyExpiryDateTime, 30, true);
						if (isExpiringWithin30Days) {
							countOfApiKeysExpiringWithin30Days++;
							log.info("API Key is expiring for auth partner id {}",
									authPartnerId + " during the next 30 days.");
							// Step 4: Check if the API Key is expiring after 30 days, 15 days, 10 days,
							// 9 days and so on, if yes create notifications
							Iterator<Integer> expiryPeriodsIterator = apiKeyExpiryPeriods.iterator();
							while (expiryPeriodsIterator.hasNext()) {
								Integer expiryPeriod = expiryPeriodsIterator.next();
								log.info("Checking for API Key expiry after " + expiryPeriod + " days.");
								boolean isExpiringAfterExpiryPeriod = partnerCertificateExpiryHelper
										.checkIfExpiring(authPartner, apiKeyExpiryDateTime, expiryPeriod, false);
								// Step 5: If yes, add the notification
								if (isExpiringAfterExpiryPeriod) {
									List<ApiKeyDetailsDto> expiringApiKeysList = new ArrayList<ApiKeyDetailsDto>();
									ApiKeyDetailsDto apiKeyDetailsDto = partnerCertificateExpiryHelper
											.populateApiKeyDetails(expiryPeriod, apiKeyDetails);
									expiringApiKeysList.add(apiKeyDetailsDto);
									// Decrypt the email ID if it's already encrypted to avoid encrypting it again
									String decryptedEmailId = keyManagerHelper.decryptData(authPartner.getEmailId());
									NotificationEntity savedNotification = batchJobHelper.saveNotification(
											PartnerConstants.API_KEY_EXPIRY_NOTIFICATION_TYPE, authPartner, null, null,
											null, expiringApiKeysList, decryptedEmailId);
									// Step 6: send email notification
									emailNotificationService.sendEmailNotification(savedNotification, decryptedEmailId);
									log.info("Created SBI expiry notification with notification id "
											+ savedNotification.getId());
									totalNotificationsCreated.add(savedNotification.getId());
									break;
								} else {
									log.info("API Key is NOT expiring  after " + expiryPeriod + " days.");
									// check for next time interval
								}
							}
						} else {
							log.info("API Key is NOT expiring for auth partner id {}",
									authPartnerId + " during the next 30 days.");
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("Error occurred while running ApiKeyExpiryTasklet: {}", e.getMessage(), e);
		}
		log.info("Overall found " + countOfApiKeysExpiringWithin30Days
				+ " API Keys which are expiring during the next 30 days. But notifications will only be created as per the configured expiry days.");
		log.info("ApiKeyExpiryTasklet: DONE, created {}", totalNotificationsCreated.size() + " notifications."
				+ " Checked API Key expiry for " + authPartnersCount + " auth partners.");
		totalNotificationsCreated.forEach(notificationId -> {
			log.info(notificationId);
		});

		return RepeatStatus.FINISHED;
	}

}