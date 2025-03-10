package io.mosip.pms.batchjob.tasklets;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import io.mosip.pms.batchjob.constants.PartnerConstants;
import io.mosip.pms.batchjob.dto.CertificateDetailsDto;
import io.mosip.pms.batchjob.dto.NotificationDetailsDto;
import io.mosip.pms.batchjob.dto.TrustCertTypeListResponseDto;
import io.mosip.pms.batchjob.dto.TrustCertificateSummaryDto;
import io.mosip.pms.batchjob.entity.Notification;
import io.mosip.pms.batchjob.entity.Partner;
import io.mosip.pms.batchjob.exceptions.BatchJobServiceException;
import io.mosip.pms.batchjob.impl.CertificateExpiryService;
import io.mosip.pms.batchjob.impl.EmailNotificationService;
import io.mosip.pms.batchjob.repository.NotificationServiceRepository;
import io.mosip.pms.batchjob.util.BatchJobHelper;

@Component
public class RootCertificateExpiryTasklet implements Tasklet {

	private Logger log = LoggerConfiguration.logConfig(RootCertificateExpiryTasklet.class);

	@Value("#{'${mosip.pms.batch.job.root.cert.expiry.periods}'.split(',')}")
	private List<Integer> rootExpiryPeriods;

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

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
			throws JsonProcessingException {
		log.info("RootCertificateExpiryTasklet: START");
		try {
			List<String> notificationIdsCreated = new ArrayList<String>();

			// Step 1: TODO get the partner admin users
			List<String> partnerAdmins = new ArrayList<String>();
			partnerAdmins.add("mayurad");
			partnerAdmins.add("mayurad1111");
			// Step 2: get all Root certificates expiring after 30 days, 15 days, 10 days, 9
			// days and so on
			rootExpiryPeriods.forEach(expiryPeriod -> {
				log.info("Starting notifications creation for expiry period, {}", expiryPeriod + " days");
				notificationIdsCreated.clear();
				LocalDate validTillDate = LocalDate.now().plusDays(expiryPeriod);
				LocalTime validTillTime = LocalTime.MAX;
				LocalDateTime validTillDateTime = LocalDateTime.of(validTillDate, validTillTime);
				TrustCertTypeListResponseDto response = certificateExpiryService
						.getTrustCertificates(PartnerConstants.ROOT, validTillDateTime);
				log.debug("getCaCertificates response received, {}", response);
				if (response.getAllPartnerCertificates().size() > 0) {
					log.info("number of root certificates expiring after " + expiryPeriod + " days, {}",
							response.getAllPartnerCertificates().size());
					// Step 3: validate the partner ids are active or not

					partnerAdmins.forEach(partnerAdminId -> {
						Optional<Partner> partnerAdminDetails = batchJobHelper.getPartnerById(partnerAdminId);
						if (batchJobHelper.validateActivePartnerId(partnerAdminDetails)) {
							// Step 4: add the notification
							response.getAllPartnerCertificates().forEach(expiringCertificate -> {
								Notification savedNotification = saveCertificateExpiryNotification(expiryPeriod,
										partnerAdminDetails, expiringCertificate);
								// Step 5: send email notification
								emailNotificationService.sendEmailNotification(savedNotification.getId());
								notificationIdsCreated.add(savedNotification.getId());
							});
						} else {
							log.info("this partner admin is not active or valid in PMS, {}", partnerAdminId);
						}

					});
				} else {
					log.info("there are no root certificates expiring after " + expiryPeriod + " days");
				}
				log.info("Completed notifications creation for expiry period, {}",
						expiryPeriod + " days. Added " + notificationIdsCreated.size() + " notifications.");
				log.info("Created notifications, {}", notificationIdsCreated);
			});
		} catch (Exception e) {
			log.error("Error occurred while running RootCertificateExpiryTasklet: {}", e.getMessage(), e);
		}
		log.info("RootCertificateExpiryTasklet: DONE");
		return RepeatStatus.FINISHED;
	}

	private Notification saveCertificateExpiryNotification(int expiryPeriod, Optional<Partner> partnerAdminDetails,
			TrustCertificateSummaryDto expiringCertificate) throws BatchJobServiceException {
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
			certificateDetails.setCertificateType(PartnerConstants.ROOT);
			certificateDetails.setExpiryDateTime(expiringCertificate.getValidTillDate().toString());
			certificateDetails.setExpiryPeriod("" + expiryPeriod);
			certificateDetails.setCertificateId(expiringCertificate.getCertId());
			expiringCertificates.add(certificateDetails);
			NotificationDetailsDto notificationDetailsDto = new NotificationDetailsDto();
			notificationDetailsDto.setCertificateDetails(expiringCertificates);
			String id = UUID.randomUUID().toString();
			Notification notification = new Notification();
			notification.setId(id);
			notification.setPartnerId(partnerAdminDetails.get().getId());
			notification.setNotificationType(PartnerConstants.ROOT_CERT_EXPIRY);
			notification.setNotificationStatus(PartnerConstants.STATUS_ACTIVE);
			notification.setEmailId(partnerAdminDetails.get().getEmailId());
			notification.setEmailLangCode(partnerAdminDetails.get().getLangCode());
			notification.setEmailSent(false);
			notification.setCreatedBy(PartnerConstants.SYSTEM_USER);
			notification.setCreatedDatetime(LocalDateTime.now());
			notification.setNotificationDetailsJson(objectMapper.writeValueAsString(notificationDetailsDto));
			log.info("saving notifications, {}", notification);
			Notification savedNotification = notificationServiceRepository.save(notification);
			return savedNotification;
		} catch (JsonProcessingException jpe) {
			log.error("Error creating the notification: {}", jpe.getMessage());
			throw new BatchJobServiceException(ErrorCodes.NOTIFICATION_CREATE_ERROR.getCode(),
					ErrorCodes.NOTIFICATION_CREATE_ERROR.getMessage());
		}
	}

}
