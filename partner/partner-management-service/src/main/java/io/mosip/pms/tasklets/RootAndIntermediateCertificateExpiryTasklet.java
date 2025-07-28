package io.mosip.pms.tasklets;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
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
import io.mosip.pms.common.dto.CertificateDetailsDto;
import io.mosip.pms.common.dto.TrustCertTypeListResponseDto;
import io.mosip.pms.common.dto.TrustCertificateSummaryDto;
import io.mosip.pms.common.entity.NotificationEntity;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.exception.BatchJobServiceException;
import io.mosip.pms.partner.manager.constant.ErrorCode;
import io.mosip.pms.tasklets.service.EmailNotificationService;
import io.mosip.pms.tasklets.util.BatchJobHelper;
import io.mosip.pms.tasklets.util.KeyManagerHelper;
import io.mosip.pms.tasklets.util.KeycloakHelper;

/**
 * This Batch Job will create notifications for the Root and Intermediate
 * certificates expiring as per the configured period.
 *
 * @author Mayura Deshmukh
 * @since 1.3.x
 */
@Component
public class RootAndIntermediateCertificateExpiryTasklet implements Tasklet {

	private Logger log = PMSLogger.getLogger(RootAndIntermediateCertificateExpiryTasklet.class);

	@Value("#{'${mosip.pms.batch.job.root.intermediate.cert.expiry.periods}'.split(',')}")
	private List<Integer> rootIntermediateExpiryPeriods;

	@Autowired
	KeyManagerHelper keyManagerHelper;

	@Autowired
	BatchJobHelper batchJobHelper;

	@Autowired
	EmailNotificationService emailNotificationService;

	@Autowired
	KeycloakHelper keycloakHelper;

	@Value("${mosip.pms.root.and.intermediate.certificates.available}")
	private Boolean isRootIntermediateCertEndpointAvailable;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		log.info("RootAndIntermediateCertificateExpiryTasklet: START");

		// Check if root/intermediate cert endpoint is available
		if (Boolean.FALSE.equals(isRootIntermediateCertEndpointAvailable)) {
			log.info(
					"RootAndIntermediateCertificateExpiryTasklet: Notifications cannot be generated since the \"getCaCertificates()\" endpoint is not available in the current version of KeyManager.");
			return RepeatStatus.FINISHED;
		}

		List<String> totalNotificationsCreated = new ArrayList<String>();
		int pmsPartnerAdminsCount = 0;
		try {
			List<String> certificateTypes = new ArrayList<String>();
			certificateTypes.add(PartnerConstants.ROOT);
			certificateTypes.add(PartnerConstants.INTERMEDIATE);
			// Step 1: Fetch Partner Admin User IDs from Keycloak, which are Valid Partners
			// in PMS
			List<Partner> pmsPartnerAdmins = keycloakHelper.getPartnerIdsWithPartnerAdminRole();
			pmsPartnerAdminsCount = pmsPartnerAdmins.size();
			pmsPartnerAdmins.forEach(admin -> {
				log.info("PMS Active Partner Admin Id: {}", admin.getId());
			});
			if (pmsPartnerAdminsCount > 0) {
				// Step 2: get all Root certificates expiring after 30 days, 15 days, 10 days, 9
				// days and so on
				rootIntermediateExpiryPeriods.forEach(expiryPeriod -> {
					LocalDate validTillDate = LocalDate.now(ZoneId.of("UTC")).plusDays(expiryPeriod);
					LocalTime validTillTime = LocalTime.MAX;
					LocalDateTime validTillDateTime = LocalDateTime.of(validTillDate, validTillTime);
					certificateTypes.forEach(certificateType -> {
						log.info("Starting execution for " + certificateType + " certificate, for expiry period, {}",
								expiryPeriod + " days");
						TrustCertTypeListResponseDto response = keyManagerHelper.getTrustCertificates(certificateType,
								validTillDateTime);
						log.debug("Response received, {}", response);
						List<String> countPerCertTypeExpiryPeriod = new ArrayList<String>();
						if (response.getAllPartnerCertificates().size() > 0) {
							log.info("Count of " + certificateType + " certificates expiring after " + expiryPeriod
									+ " days, {}", response.getAllPartnerCertificates().size());
							pmsPartnerAdmins.forEach(partnerAdminDetails -> {
								// Step 3: add the notification
								response.getAllPartnerCertificates().forEach(expiringCertificate -> {
									// certificatePartnerId is null, since Root/Intermediate Certificate is not
									// associated with any partner
									List<CertificateDetailsDto> certificateDetailsList = populateCertificateDetails(
											certificateType, expiryPeriod, null, expiringCertificate);
									// Decrypt the email ID if it's already encrypted to avoid encrypting it again
									String decryptedEmailId = keyManagerHelper
											.decryptData(partnerAdminDetails.getEmailId());
									NotificationEntity savedNotification = batchJobHelper.saveNotification(
											getNotificationType(certificateType), partnerAdminDetails,
											certificateDetailsList, null, null, null, decryptedEmailId);
									// Step 4: send email notification
									emailNotificationService.sendEmailNotification(savedNotification, decryptedEmailId);
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

	private String getNotificationType(String certificateType) throws BatchJobServiceException {
		switch (certificateType) {
		case PartnerConstants.ROOT:
			return PartnerConstants.ROOT_CERT_EXPIRY;
		case PartnerConstants.INTERMEDIATE:
			return PartnerConstants.INTERMEDIATE_CERT_EXPIRY_NOTIFICATION_TYPE;
		default:
			throw new BatchJobServiceException(ErrorCode.INVALID_CERTIFICATE_TYPE.getErrorCode(),
					ErrorCode.INVALID_CERTIFICATE_TYPE.getErrorMessage());
		}
	}

}