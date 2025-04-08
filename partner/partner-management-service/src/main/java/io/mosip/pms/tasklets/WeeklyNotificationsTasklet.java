package io.mosip.pms.tasklets;

import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
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
 * This Batch Job will create weekly notifications for the Partner certificates
 * expiring as per the configured period.
 * 
 * @author Mayura Deshmukh
 * @since 1.3.x
 */
@Component
public class WeeklyNotificationsTasklet implements Tasklet {
	private Logger log = PMSLogger.getLogger(WeeklyNotificationsTasklet.class);

	@Autowired
	KeycloakHelper keycloakHelper;

	@Autowired
	BatchJobHelper batchJobHelper;

	@Autowired
	EmailNotificationService emailNotificationService;

	@Autowired
	PartnerCertificateExpiryHelper partnerCertificateExpiryHelper;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		log.info("WeeklyNotificationsTasklet - Partner Certificates: START");
		List<String> totalNotificationsCreated = new ArrayList<String>();
		Map<Partner, X509Certificate> weeklySummaryPartnerDetails = new HashMap<Partner, X509Certificate>();
		int activePartnersCount = 0;
		int countOfPartnersWithInvalidCerts = 0;
		try {
			// Step 1: Fetch Partner Admin User IDs from Keycloak, which are Active Partners
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
					// Step 4: Check if the certificate is expiring within 7 days
					log.info("Checking if certificate is expiring for partner id {}",
							pmsPartner.getId() + " within next 7 days.");
					LocalDateTime partnerCertificateExpiryDate = decodedPartnerCertificate.getNotAfter().toInstant()
							.atZone(ZoneId.of("UTC")).toLocalDateTime();
					log.info("The certificate expiry date is {}", partnerCertificateExpiryDate);
					boolean isExpiringWithin7Days = partnerCertificateExpiryHelper
							.checkIfCertificateIsExpiring(pmsPartner, partnerCertificateExpiryDate, 7, true);
					if (isExpiringWithin7Days) {
						log.info("Certificate is expiring for partner id {}",
								pmsPartner.getId() + " during the next 7 days.");
						weeklySummaryPartnerDetails.put(pmsPartner, decodedPartnerCertificate);
					} else {
						log.info("Certificate is NOT expiring for partner id {}",
								pmsPartner.getId() + " during the next 7 days.");
					}
				} else {
					countOfPartnersWithInvalidCerts++;
					log.info("Valid certificate not found for partner id {}", pmsPartner.getId());
				}
			}
			// Step 5: Create a weekly notification for all the partner admin users
			log.info("Creating weekly summary notifications, for  {}", weeklySummaryPartnerDetails.size());
			createWeeklySummaryNotifications(pmsPartnerAdmins, weeklySummaryPartnerDetails, totalNotificationsCreated);

		} catch (Exception e) {
			log.error("Error occurred while running WeeklyNotificationsTasklet: {}", e.getMessage(), e);
		}

		log.info("WeeklyNotificationsTasklet : DONE, created {}", totalNotificationsCreated.size() + " notifications."
				+ " Checked certificate expiry for " + activePartnersCount + " partners.");
		totalNotificationsCreated.forEach(notificationId -> {
			log.info(notificationId);
		});
		if (countOfPartnersWithInvalidCerts > 0) {
			log.info("Note: Valid partner certificate is not available for " + countOfPartnersWithInvalidCerts
					+ " partners. ");
		}
		return RepeatStatus.FINISHED;
	}

	private void createWeeklySummaryNotifications(List<Partner> pmsPartnerAdmins,
			Map<Partner, X509Certificate> weeklySummaryPartnerDetails, List<String> totalNotificationsCreated) {
		if (weeklySummaryPartnerDetails.size() > 0) {
			List<CertificateDetailsDto> expiringCertificates = new ArrayList<CertificateDetailsDto>();
			weeklySummaryPartnerDetails.forEach((partnerWithExpiringCert, decodedPartnerCertificate) -> {
				log.info("Weekly Summary - adding certificate expiry details for partner id {}",
						partnerWithExpiringCert.getId());
				CertificateDetailsDto certificateDetailsList = partnerCertificateExpiryHelper
						.populateCertificateDetails(7, partnerWithExpiringCert, decodedPartnerCertificate);
				expiringCertificates.add(certificateDetailsList);
			});
			Iterator<Partner> pmsPartnerAdminsIterator = pmsPartnerAdmins.iterator();
			while (pmsPartnerAdminsIterator.hasNext()) {
				Partner pmsPartnerAdmin = pmsPartnerAdminsIterator.next();
				NotificationEntity savedNotification = batchJobHelper.saveCertificateExpiryNotification(
						PartnerConstants.WEEKLY, pmsPartnerAdmin, expiringCertificates);
				// Step 6: send email notification
				emailNotificationService.sendEmailNotification(savedNotification.getId());
				log.info("Created weekly summary notification with notification id " + savedNotification.getId());
				totalNotificationsCreated.add(savedNotification.getId());
			}
		}

	}

}