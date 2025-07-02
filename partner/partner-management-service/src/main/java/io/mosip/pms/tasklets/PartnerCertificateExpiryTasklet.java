package io.mosip.pms.tasklets;

import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.mosip.pms.tasklets.util.KeyManagerHelper;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.dto.CertificateDetailsDto;
import io.mosip.pms.common.entity.NotificationEntity;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.tasklets.service.EmailNotificationService;
import io.mosip.pms.tasklets.util.BatchJobHelper;
import io.mosip.pms.tasklets.util.KeycloakHelper;
import io.mosip.pms.tasklets.util.PartnerCertificateExpiryHelper;

/**
 * This Batch Job will create notifications for the Partner certificates
 * expiring as per the configured period.
 * 
 * @author Mayura Deshmukh
 * @since 1.3.x
 */
@Component
public class PartnerCertificateExpiryTasklet implements Tasklet {
	private Logger log = PMSLogger.getLogger(PartnerCertificateExpiryTasklet.class);

	@Value("#{'${mosip.pms.batch.job.partner.cert.expiry.periods}'.split(',')}")
	private List<Integer> partnerCertExpiryPeriods;

	@Autowired
	KeycloakHelper keycloakHelper;

	@Autowired
	BatchJobHelper batchJobHelper;

	@Autowired
	EmailNotificationService emailNotificationService;

	@Autowired
	PartnerCertificateExpiryHelper partnerCertificateExpiryHelper;

	@Autowired
	KeyManagerHelper keyManagerHelper;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		log.info("PartnerCertificateExpiryTasklet: START");
		List<String> totalNotificationsCreated = new ArrayList<String>();
		int activePartnersCount = 0;
		int countOfCertsExpiringWithin30Days = 0;
		int countOfPartnersWithInvalidCerts = 0;
		try {
			// Step 1: Fetch Partner Admin User IDs from Keycloak, which are Valid Partners
			// in PMS
			List<Partner> pmsPartnerAdmins = keycloakHelper.getPartnerIdsWithPartnerAdminRole();
			pmsPartnerAdmins.forEach(admin -> {
				log.info("PMS Partner Admin Id: {}", admin.getId());
			});
			// Step 2: Get all PMS partners which are ACTIVE and NOT partner admins
			List<Partner> activePartnersList = batchJobHelper.getAllActiveNonAdminPartners(pmsPartnerAdmins);
			activePartnersCount = activePartnersList.size();
			log.info("PMS has {} Active Partner (Non Admin) users.", activePartnersCount);
			// Step 3: For each partner get the certificate and check if it is expiring
			Iterator<Partner> activePartnersListIterator = activePartnersList.iterator();
			while (activePartnersListIterator.hasNext()) {
				Partner pmsPartner = activePartnersListIterator.next();
				log.info("Fetching certificate for partner id {}", pmsPartner.getId());
				X509Certificate decodedPartnerCertificate = partnerCertificateExpiryHelper
						.getDecodedCertificate(pmsPartner);
				if (decodedPartnerCertificate != null) {
					log.info("Checking if certificate is expiring for partner id {}",
							pmsPartner.getId() + " within next 30 days.");
					LocalDateTime partnerCertificateExpiryDate = partnerCertificateExpiryHelper
							.getCertificateExpiryDateTime(decodedPartnerCertificate);
					log.info("The certificate expiry date is {}", partnerCertificateExpiryDate);
					boolean isExpiringWithin30Days = partnerCertificateExpiryHelper.checkIfExpiring(pmsPartner,
							partnerCertificateExpiryDate, 30, true);
					if (isExpiringWithin30Days) {
						countOfCertsExpiringWithin30Days++;
						log.info("Certificate is expiring for partner id {}",
								pmsPartner.getId() + " during the next 30 days.");
						// Step 4: Check if the certificate is expiring after 30 days, 15 days, 10 days,
						// 9 days and so on
						Iterator<Integer> expiryPeriodsIterator = partnerCertExpiryPeriods.iterator();
						while (expiryPeriodsIterator.hasNext()) {
							Integer expiryPeriod = expiryPeriodsIterator.next();
							log.info("Checking for certificate expiry after " + expiryPeriod + " days.");
							boolean isExpiringAfterExpiryPeriod = partnerCertificateExpiryHelper
									.checkIfExpiring(pmsPartner, partnerCertificateExpiryDate, expiryPeriod, false);
							// Step 5: If yes, add the notification
							if (isExpiringAfterExpiryPeriod) {
								List<CertificateDetailsDto> expiringCertificates = new ArrayList<CertificateDetailsDto>();
								CertificateDetailsDto certificateDetails = partnerCertificateExpiryHelper
										.populateCertificateDetails(expiryPeriod, pmsPartner,
												decodedPartnerCertificate);
								expiringCertificates.add(certificateDetails);
								// Decrypt the email ID if it's already encrypted to avoid encrypting it again
								String decryptedEmailId = keyManagerHelper.decryptData(pmsPartner.getEmailId());
								NotificationEntity savedNotification = batchJobHelper.saveNotification(
										PartnerConstants.PARTNER_CERT_EXPIRY_NOTIFICATION_TYPE, pmsPartner, expiringCertificates, null,
										null, null, decryptedEmailId);
								// Step 6: send email notification
								emailNotificationService.sendEmailNotification(savedNotification, decryptedEmailId);
								log.info("Created partner certificate expiry notification with notification id "
										+ savedNotification.getId());
								totalNotificationsCreated.add(savedNotification.getId());
								break;
							} else {
								log.info("Certificate is NOT expiring  after " + expiryPeriod + " days.");
								// check for next time interval
							}
						}
					} else {
						log.info("Certificate is NOT expiring for partner id {}",
								pmsPartner.getId() + " during the next 30 days.");
					}
				} else {
					countOfPartnersWithInvalidCerts++;
					log.info("Valid certificate not found for partner id {}", pmsPartner.getId());
				}
			}
		} catch (Exception e) {
			log.error("Error occurred while running PartnerCertificateExpiryTasklet: {}", e.getMessage(), e);
		}
		log.info("Found " + countOfCertsExpiringWithin30Days
				+ " certificates which are expiring during the next 30 days. But notifications will only be created as per the configured expiry days.");
		log.info("PartnerCertificateExpiryTasklet - Partner Certificates: DONE, created {}",
				totalNotificationsCreated.size() + " notifications." + " Checked certificate expiry for "
						+ activePartnersCount + " partners.");
		totalNotificationsCreated.forEach(notificationId -> {
			log.info(notificationId);
		});
		if (countOfPartnersWithInvalidCerts > 0) {
			log.info("Note: Valid partner certificate is not available for " + countOfPartnersWithInvalidCerts
					+ " partners. ");
		}
		return RepeatStatus.FINISHED;
	}

}