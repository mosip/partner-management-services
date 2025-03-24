package io.mosip.pms.batchjob.tasklets;

import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.pms.batchjob.config.LoggerConfiguration;
import io.mosip.pms.batchjob.impl.CertificateExpiryService;
import io.mosip.pms.batchjob.impl.EmailNotificationService;
import io.mosip.pms.batchjob.util.BatchJobHelper;
import io.mosip.pms.batchjob.util.KeycloakHelper;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.dto.CertificateDetailsDto;
import io.mosip.pms.common.dto.PartnerCertDownloadResponeDto;
import io.mosip.pms.common.entity.NotificationEntity;
import io.mosip.pms.common.entity.Partner;

/**
 * This Batch Job will create notifications for the Partner certificates
 * expiring as per the configured period.
 * 
 * @author Mayura Deshmukh
 * @since 1.3.x
 */
@Component
public class PartnerCertificateExpiryTasklet implements Tasklet {
	private Logger log = LoggerConfiguration.logConfig(PartnerCertificateExpiryTasklet.class);

	@Value("#{'${mosip.pms.batch.job.partner.cert.expiry.periods}'.split(',')}")
	private List<Integer> partnerCertExpiryPeriods;

	@Autowired
	KeycloakHelper keycloakHelper;

	@Autowired
	BatchJobHelper batchJobHelper;

	@Autowired
	CertificateExpiryService certificateExpiryService;

	@Autowired
	EmailNotificationService emailNotificationService;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		log.info("PartnerCertificateExpiryTasklet: START");
		List<String> totalNotificationsCreated = new ArrayList<String>();
		int activePartnersCount = 0;
		int countOfCertsExpiringInNext30Days = 0;
		int countOfPartnersWithInvalidCerts = 0;
		try {
			// Step 1: Fetch Partner Admin user IDs from Keycloak
			List<String> keycloakPartnerAdmins = keycloakHelper.getPartnerIdsWithPartnerAdminRole();
			log.info("KeyCloak returned {} Partner Admin users.", keycloakPartnerAdmins.size());
			// Step 2: Get all PMS partners which are ACTIVE and NOT partner admins
			List<Partner> activePartnersList = batchJobHelper.getAllActiveNonAdminPartners(keycloakPartnerAdmins);
			activePartnersCount = activePartnersList.size();
			log.info("PMS has {} Active Partner (Non Admin) users.", activePartnersCount);
			// Step 3: For each partner get the certificate and check if it is expiring
			Iterator<Partner> activePartnersListIterator = activePartnersList.iterator();
			while (activePartnersListIterator.hasNext()) {
				Partner pmsPartner = activePartnersListIterator.next();
				log.info("Fetching certificate for partner id {}", pmsPartner.getId());
				X509Certificate decodedPartnerCertificate = getDecodedCertificate(pmsPartner);
				if (decodedPartnerCertificate != null) {
					log.info("Checking if certificate is expiring for partner id {}",
							pmsPartner.getId() + " within next 30 days.");
					LocalDateTime partnerCertificateExpiryDate = decodedPartnerCertificate.getNotAfter().toInstant()
							.atZone(ZoneId.of("UTC")).toLocalDateTime();
					log.info("The certificate expiry date is {}", partnerCertificateExpiryDate);
					boolean isCertificateExpiring = checkIfCertificateIsExpiring(pmsPartner,
							partnerCertificateExpiryDate, 30, true);
					if (isCertificateExpiring) {
						countOfCertsExpiringInNext30Days++;
						log.info("Certificate is expiring for partner id {}",
								pmsPartner.getId() + " during the next 30 days.");
						// Step 5: Check if the certificate is expiring after 30 days, 15 days, 10 days,
						// 9 days and so on
						Iterator<Integer> expiryPeriodsIterator = partnerCertExpiryPeriods.iterator();
						while (expiryPeriodsIterator.hasNext()) {
							Integer expiryPeriod = expiryPeriodsIterator.next();
							log.info("Checking for certificate expiry after " + expiryPeriod + " days.");
							boolean isCertExpiring = checkIfCertificateIsExpiring(pmsPartner, partnerCertificateExpiryDate,
									expiryPeriod, false);
							// Step 6: If yes, add the notification
							if (isCertExpiring) {
								List<CertificateDetailsDto> certificateDetailsList = populateCertificateDetails(
										expiryPeriod, pmsPartner, decodedPartnerCertificate);
								NotificationEntity savedNotification = batchJobHelper.saveCertificateExpiryNotification(
										PartnerConstants.PARTNER, expiryPeriod, pmsPartner, certificateDetailsList);
								// Step 7: send email notification
								emailNotificationService.sendEmailNotification(savedNotification.getId());
								log.info("Created notification with notification id " + savedNotification.getId());
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
		log.info("Found " + countOfCertsExpiringInNext30Days
				+ " certificates which are expiring during the next 30 days. But notifications will only be created as per the configured expiry days.");
		log.info("PartnerCertificateExpiryTasklet: DONE, created {}", totalNotificationsCreated.size()
				+ " notifications." + " Checked certificate expiry for " + activePartnersCount + " partners.");
		totalNotificationsCreated.forEach(notificationId -> {
			log.info(notificationId);
		});
		if (countOfPartnersWithInvalidCerts > 0) {
			log.info("Note: Valid partner certificate is not available for " + countOfPartnersWithInvalidCerts
					+ " partners. ");
		}
		return RepeatStatus.FINISHED;
	}

	private X509Certificate getDecodedCertificate(Partner pmsPartner) {
		X509Certificate decodedPartnerCertificate = null;
		if (pmsPartner.getCertificateAlias() != null) {
			try {
				PartnerCertDownloadResponeDto certResp = certificateExpiryService
						.getPartnerCertificate(pmsPartner.getCertificateAlias());

				decodedPartnerCertificate = batchJobHelper.decodeCertificateData(certResp.getCertificateData());
			} catch (Exception e) {
				log.debug("Error occurred while fetching certificate for : {}", pmsPartner.getId());
			}
		}
		return decodedPartnerCertificate;
	}

	private boolean checkIfCertificateIsExpiring(Partner pmsPartner, LocalDateTime partnerCertificateExpiryDate,
			Integer expiryPeriod, boolean withinExpiryPeriod) {
		boolean isCertificateExpiring = false;
		LocalDate todayDate = LocalDate.now(ZoneId.of("UTC"));
		LocalDate validTillDate = todayDate.plusDays(expiryPeriod);
		LocalTime validTillMinTime = LocalTime.MIN;
		LocalDateTime validTillMinDateTime = LocalDateTime.of(validTillDate, validTillMinTime);
		if (withinExpiryPeriod) {
			validTillMinDateTime = LocalDateTime.of(todayDate, validTillMinTime);
		}
		LocalTime validTillMaxTime = LocalTime.MAX;
		LocalDateTime validTillMaxDateTime = LocalDateTime.of(validTillDate, validTillMaxTime);
		log.debug("validTillMinDateTime {}", validTillMinDateTime);
		log.debug("validTillMaxDateTime {}", validTillMaxDateTime);

		// Check if the certificate has expired
		if (partnerCertificateExpiryDate.isAfter(validTillMinDateTime)
				&& partnerCertificateExpiryDate.isBefore(validTillMaxDateTime)) {
			log.debug("The certificate for partner id {}",
					pmsPartner.getId() + "" + " is expiring after " + expiryPeriod + " days.");
			isCertificateExpiring = true;
		}
		return isCertificateExpiring;
	}

	private List<CertificateDetailsDto> populateCertificateDetails(int expiryPeriod, Partner partner,
			X509Certificate expiringCertificate) {
		List<CertificateDetailsDto> expiringCertificates = new ArrayList<CertificateDetailsDto>();
		CertificateDetailsDto certificateDetails = new CertificateDetailsDto();
		certificateDetails.setCertificateId(partner.getCertificateAlias());
		certificateDetails.setIssuedBy(expiringCertificate.getIssuerX500Principal().getName());
		certificateDetails.setIssuedTo(expiringCertificate.getSubjectX500Principal().getName());
		certificateDetails.setPartnerDomain(batchJobHelper.getPartnerDomain(partner.getPartnerTypeCode()));
		certificateDetails.setPartnerId(partner.getId());
		certificateDetails.setCertificateType(PartnerConstants.PARTNER);
		certificateDetails.setExpiryDateTime(
				expiringCertificate.getNotAfter().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime().toString());
		certificateDetails.setExpiryPeriod("" + expiryPeriod);
		expiringCertificates.add(certificateDetails);
		return expiringCertificates;
	}

}