package io.mosip.pms.batchjob.tasklets;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
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
import io.mosip.pms.common.dto.TrustCertTypeListResponseDto;
import io.mosip.pms.common.dto.TrustCertificateSummaryDto;
import io.mosip.pms.common.entity.NotificationEntity;
import io.mosip.pms.common.entity.Partner;

/**
 * This Batch Job will create notifications for the Root and Intermediate
 * certificates expiring as per the configured period.
 *
 * @author Mayura Deshmukh
 * @since 1.3.x
 */
@Component
public class RootAndIntermediateCertificateExpiryTasklet implements Tasklet {

	private Logger log = LoggerConfiguration.logConfig(RootAndIntermediateCertificateExpiryTasklet.class);

	@Value("#{'${mosip.pms.batch.job.root.intermediate.cert.expiry.periods}'.split(',')}")
	private List<Integer> rootIntermediateExpiryPeriods;

	@Autowired
	CertificateExpiryService certificateExpiryService;

	@Autowired
	BatchJobHelper batchJobHelper;

	@Autowired
	EmailNotificationService emailNotificationService;

	@Autowired
	KeycloakHelper keycloakHelper;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		log.info("RootAndIntermediateCertificateExpiryTasklet: START");
		List<String> totalNotificationsCreated = new ArrayList<String>();
		int pmsPartnerAdminsCount = 0;
		try {
			List<String> certificateTypes = new ArrayList<String>();
			certificateTypes.add(PartnerConstants.ROOT);
			certificateTypes.add(PartnerConstants.INTERMEDIATE);

			// Step 1: Fetch Partner Admin user IDs from Keycloak
			List<String> keycloakPartnerAdmins = keycloakHelper.getPartnerIdsWithPartnerAdminRole();
			log.info("KeyCloak returned {} Partner Admin users.", keycloakPartnerAdmins.size());

			// Step 2: Validate if each of these are valid Partner Admins in PMS
			List<Partner> pmsPartnerAdmins = batchJobHelper.getValidPartnerAdminsInPms(keycloakPartnerAdmins);
			pmsPartnerAdminsCount = pmsPartnerAdmins.size();
			log.info("PMS has {} Active Partner Admin users.", pmsPartnerAdminsCount);
			pmsPartnerAdmins.forEach(admin -> {
				log.info("PMS Active Partner Admin Id: {}", admin.getId());
			});

			if (pmsPartnerAdminsCount > 0) {
				// Step 3: get all Root certificates expiring after 30 days, 15 days, 10 days, 9
				// days and so on
				rootIntermediateExpiryPeriods.forEach(expiryPeriod -> {
					LocalDate validTillDate = LocalDate.now(ZoneId.of("UTC")).plusDays(expiryPeriod);
					LocalTime validTillTime = LocalTime.MAX;
					LocalDateTime validTillDateTime = LocalDateTime.of(validTillDate, validTillTime);
					certificateTypes.forEach(certificateType -> {
						log.info("Starting execution for " + certificateType + " certificate, for expiry period, {}",
								expiryPeriod + " days");
						TrustCertTypeListResponseDto response = certificateExpiryService
								.getTrustCertificates(certificateType, validTillDateTime);
						log.debug("Response received, {}", response);
						List<String> countPerCertTypeExpiryPeriod = new ArrayList<String>();
						if (response.getAllPartnerCertificates().size() > 0) {
							log.info("Count of " + certificateType + " certificates expiring after " + expiryPeriod
									+ " days, {}", response.getAllPartnerCertificates().size());
							pmsPartnerAdmins.forEach(partnerAdminDetails -> {
								// Step 4: add the notification
								response.getAllPartnerCertificates().forEach(expiringCertificate -> {
									// certificatePartnerId is null, since Root/Intermediate Certificate is not
									// associated with any partner
									List<CertificateDetailsDto> certificateDetailsList = populateCertificateDetails(
											certificateType, expiryPeriod, null, expiringCertificate);
									NotificationEntity savedNotification = batchJobHelper
											.saveCertificateExpiryNotification(certificateType, expiryPeriod,
													partnerAdminDetails, certificateDetailsList);
									// Step 5: send email notification
									emailNotificationService.sendEmailNotification(savedNotification.getId());
									countPerCertTypeExpiryPeriod.add(savedNotification.getId());
									totalNotificationsCreated.add(savedNotification.getId());
								});

							});
						} else {
							log.info("There are no " + certificateType + " certificates expiring after " + expiryPeriod
									+ " days");
						}
						log.info("Completed execution for " + certificateType + " certificate, for expiry period, {}",
								expiryPeriod + " days");

						log.info("Created {}", countPerCertTypeExpiryPeriod.size() + " notifications");

					});

				});
			} else {
				log.info("There are no " + pmsPartnerAdminsCount
						+ " partner admin users in PMS. Hence skipping creation of notifications.");
			}
		} catch (Exception e) {
			log.error("Error occurred while running RootAndIntermediateCertificateExpiryTasklet: {}", e.getMessage(),
					e);
		}
		log.info("RootAndIntermediateCertificateExpiryTasklet: DONE, created {}",
				totalNotificationsCreated.size() + " notifications, for " + pmsPartnerAdminsCount + " partner admins.");
		totalNotificationsCreated.forEach(notificationId -> {
			log.info(notificationId);
		});
		return RepeatStatus.FINISHED;
	}

	private List<CertificateDetailsDto> populateCertificateDetails(String certificateType, int expiryPeriod,
			String certificatePartnerId, TrustCertificateSummaryDto expiringCertificate) {
		List<CertificateDetailsDto> expiringCertificates = new ArrayList<CertificateDetailsDto>();
		CertificateDetailsDto certificateDetails = new CertificateDetailsDto();
		certificateDetails.setCertificateId(expiringCertificate.getCertId());
		certificateDetails.setIssuedBy(expiringCertificate.getIssuedBy());
		certificateDetails.setIssuedTo(expiringCertificate.getIssuedTo());
		certificateDetails.setPartnerDomain(expiringCertificate.getPartnerDomain());
		certificateDetails.setPartnerId(certificatePartnerId);
		certificateDetails.setCertificateType(certificateType);
		certificateDetails.setExpiryDateTime(expiringCertificate.getValidTillDate().toString());
		certificateDetails.setExpiryPeriod("" + expiryPeriod);
		expiringCertificates.add(certificateDetails);
		return expiringCertificates;
	}

}