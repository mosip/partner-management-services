package io.mosip.pms.batchjob.tasklets;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.mosip.pms.batchjob.util.KeycloakHelper;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.constant.PartnerConstants;
import org.slf4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pms.batchjob.config.LoggerConfiguration;
import io.mosip.pms.batchjob.constants.ErrorCodes;
import io.mosip.pms.common.dto.CertificateDetailsDto;
import io.mosip.pms.common.dto.NotificationDetailsDto;
import io.mosip.pms.common.dto.TrustCertTypeListResponseDto;
import io.mosip.pms.common.dto.TrustCertificateSummaryDto;
import io.mosip.pms.common.entity.NotificationEntity;
import io.mosip.pms.batchjob.exceptions.BatchJobServiceException;
import io.mosip.pms.batchjob.impl.CertificateExpiryService;
import io.mosip.pms.batchjob.impl.EmailNotificationService;
import io.mosip.pms.common.repository.NotificationServiceRepository;
import io.mosip.pms.batchjob.util.BatchJobHelper;

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
	NotificationServiceRepository notificationServiceRepository;

	@Autowired
	BatchJobHelper batchJobHelper;

	@Autowired
	EmailNotificationService emailNotificationService;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	KeycloakHelper keycloakHelper;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
			throws JsonProcessingException {
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
			List<Partner> pmsPartnerAdmins = getValidPartnerAdmins(keycloakPartnerAdmins);
			pmsPartnerAdminsCount = pmsPartnerAdmins.size();
			log.info("PMS has {} Active Partner Admin users.", pmsPartnerAdminsCount);

			// Step 3: get all Root certificates expiring after 30 days, 15 days, 10 days, 9
			// days and so on
			rootIntermediateExpiryPeriods.forEach(expiryPeriod -> {
				LocalDate validTillDate = LocalDate.now().plusDays(expiryPeriod);
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
								NotificationEntity savedNotification = saveCertificateExpiryNotification(certificateType,
										expiryPeriod, partnerAdminDetails, expiringCertificate);
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

	private List<Partner> getValidPartnerAdmins(List<String> keycloakPartnerAdmins) {
		List<Partner> pmsPartnerAdmins = new ArrayList<Partner>();
		keycloakPartnerAdmins.forEach(keycloakPartnerAdminId -> {
			Optional<Partner> partnerAdminDetails = batchJobHelper.getPartnerById(keycloakPartnerAdminId);
			if (batchJobHelper.validateActivePartnerId(partnerAdminDetails)) {
				pmsPartnerAdmins.add(partnerAdminDetails.get());
			} else {
				log.debug("this partner admin is not active or valid in PMS, {}", keycloakPartnerAdminId);
			}
		});
		return pmsPartnerAdmins;
	}

	private NotificationEntity saveCertificateExpiryNotification(String certificateType, int expiryPeriod,
														   Partner partnerAdminDetails, TrustCertificateSummaryDto expiringCertificate)
			throws BatchJobServiceException {
		try {
			List<CertificateDetailsDto> expiringCertificates = new ArrayList<CertificateDetailsDto>();
			CertificateDetailsDto certificateDetails = new CertificateDetailsDto();
			certificateDetails.setCertificateId(expiringCertificate.getCertId());
			certificateDetails.setIssuedBy(expiringCertificate.getIssuedBy());
			certificateDetails.setIssuedTo(expiringCertificate.getIssuedTo());
			certificateDetails.setPartnerDomain(expiringCertificate.getPartnerDomain());
			// This is null, since Root/Intermediate Certificate is not associated with any
			// partner
			certificateDetails.setPartnerId(null);
			certificateDetails.setCertificateType(certificateType);
			certificateDetails.setExpiryDateTime(expiringCertificate.getValidTillDate().toString());
			certificateDetails.setExpiryPeriod("" + expiryPeriod);
			certificateDetails.setCertificateId(expiringCertificate.getCertId());
			expiringCertificates.add(certificateDetails);
			NotificationDetailsDto notificationDetailsDto = new NotificationDetailsDto();
			notificationDetailsDto.setCertificateDetails(expiringCertificates);
			String id = UUID.randomUUID().toString();
			NotificationEntity notification = new NotificationEntity();
			notification.setId(id);
			notification.setPartnerId(partnerAdminDetails.getId());
			notification
					.setNotificationType(certificateType == PartnerConstants.ROOT ? PartnerConstants.ROOT_CERT_EXPIRY
							: PartnerConstants.INTERMEDIATE_CERT_EXPIRY);
			notification.setNotificationStatus(PartnerConstants.STATUS_ACTIVE);
			notification.setEmailId(partnerAdminDetails.getEmailId());
			notification.setEmailLangCode(partnerAdminDetails.getLangCode());
			notification.setEmailSent(false);
			notification.setCreatedBy(PartnerConstants.SYSTEM_USER);
			notification.setCreatedDatetime(LocalDateTime.now());
			notification.setNotificationDetailsJson(objectMapper.writeValueAsString(notificationDetailsDto));
			log.info("saving notifications, {}", notification);
			NotificationEntity savedNotification = notificationServiceRepository.save(notification);
			return savedNotification;
		} catch (JsonProcessingException jpe) {
			log.error("Error creating the notification: {}", jpe.getMessage());
			throw new BatchJobServiceException(ErrorCodes.NOTIFICATION_CREATE_ERROR.getCode(),
					ErrorCodes.NOTIFICATION_CREATE_ERROR.getMessage());
		}
	}

}