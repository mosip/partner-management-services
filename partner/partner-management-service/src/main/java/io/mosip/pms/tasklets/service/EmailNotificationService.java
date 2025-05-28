package io.mosip.pms.tasklets.service;

import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

import io.mosip.pms.tasklets.util.BatchJobHelper;
import io.mosip.pms.tasklets.util.KeyManagerHelper;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.dto.CertificateDetailsDto;
import io.mosip.pms.common.dto.EmailTemplateDto;
import io.mosip.pms.common.dto.NotificationDetailsDto;
import io.mosip.pms.common.entity.NotificationEntity;
import io.mosip.pms.common.repository.NotificationServiceRepository;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.exception.BatchJobServiceException;
import io.mosip.pms.partner.manager.constant.AuditConstant;
import io.mosip.pms.partner.manager.constant.ErrorCode;
import io.mosip.pms.tasklets.util.TemplateHelper;

@Service
public class EmailNotificationService {

	private Logger log = PMSLogger.getLogger(EmailNotificationService.class);

	@Value("${emailResourse.url}")
	private String sendEmailUrl;

	@Autowired
	RestUtil restUtil;

	@Autowired
	AuditUtil auditUtil;

	@Autowired
	BatchJobHelper batchJobHelper;

	@Autowired
	VelocityEngine velocityEngine;

	@Autowired
	NotificationServiceRepository notificationServiceRepository;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	TemplateHelper templateHelper;

	@Autowired
	KeyManagerHelper keyManagerHelper;

	@Transactional
	public void sendEmailNotification(NotificationEntity notificationEntity, String emailId) {
		try {
			log.info("notificationEntity: {}", notificationEntity);
			if (notificationEntity.getId().isEmpty()) {
				log.error("No notification found for {}", notificationEntity);
				return;
			}

			if (notificationEntity.getEmailSent() && notificationEntity.getEmailSentDatetime() != null) {
				log.warn("Email notification already sent for ID: {}", notificationEntity.getEmailId());
				return;
			}

			EmailTemplateDto templateDto = templateHelper.fetchEmailTemplate(notificationEntity.getEmailLangCode(),
					notificationEntity.getNotificationType());
			String populatedTemplate = populateTemplate(templateDto.getBody(), notificationEntity);
			sendEmail(notificationEntity, populatedTemplate, templateDto.getSubject(), emailId);

			// update notificationEntity status
			notificationEntity.setEmailSent(true);
			notificationEntity.setEmailSentDatetime(LocalDateTime.now(ZoneId.of("UTC")));
			notificationServiceRepository.save(notificationEntity);

			saveAuditLogForEmailSuccess(notificationEntity);

			log.debug("notification status successfully updated for ID: {}", notificationEntity.getId());
		} catch (BatchJobServiceException e) {
			saveAuditLogForEmailFailure(notificationEntity);
			log.error("Failed to send email for notification ID: {} - {}", notificationEntity.getId(), e.getMessage());
		} catch (Exception e) {
			saveAuditLogForEmailFailure(notificationEntity);
			log.error("Unexpected error while sending email for notification ID: {} - {}", notificationEntity.getId(),
					e.getMessage());
		}
	}

	private void saveAuditLogForEmailSuccess(NotificationEntity notificationEntity) {
		String notificationType = notificationEntity.getNotificationType();
		auditUtil.setAuditRequestDto(batchJobHelper.getAuditLogEventTypeForEmail(notificationType, true),
				notificationEntity.getId(), notificationType, AuditConstant.AUDIT_SYSTEM);
	}

	private void saveAuditLogForEmailFailure(NotificationEntity notificationEntity) {
		String notificationType = notificationEntity.getNotificationType();
		auditUtil.setAuditRequestDto(batchJobHelper.getAuditLogEventTypeForEmail(notificationType, false), "failure",
				notificationType, AuditConstant.AUDIT_SYSTEM);
	}

	private String populateTemplate(String templateContent, NotificationEntity notificationEntity)
			throws JsonProcessingException {
		VelocityContext context = createVelocityContext(notificationEntity);
		StringWriter writer = new StringWriter();
		velocityEngine.evaluate(context, writer, "logTag", templateContent);
		return writer.toString();
	}

	private VelocityContext createVelocityContext(NotificationEntity notificationEntity)
			throws JsonProcessingException {
		VelocityContext context = new VelocityContext();
		String notificationType = notificationEntity.getNotificationType();
		NotificationDetailsDto notificationDetails = objectMapper
				.readValue(notificationEntity.getNotificationDetailsJson(), NotificationDetailsDto.class);

		switch (notificationType) {
			case PartnerConstants.PARTNER_CERT_EXPIRY, PartnerConstants.ROOT_CERT_EXPIRY,
				 PartnerConstants.INTERMEDIATE_CERT_EXPIRY, PartnerConstants.FTM_CHIP_CERT_EXPIRY:
				CertificateDetailsDto cert = notificationDetails.getCertificateDetails().stream().findFirst().orElse(null);
				if (cert != null) {
					context.put("partnerId", notificationEntity.getPartnerId());
					context.put("certificateId", cert.getCertificateId());
					context.put("expiryDateTime", cert.getExpiryDateTime());
					context.put("partnerDomain", cert.getPartnerDomain());
					context.put("issuedTo", cert.getIssuedTo());
					context.put("issuedBy", cert.getIssuedBy());
					//For FTM certificate expiry
					if (notificationType.equals(PartnerConstants.FTM_CHIP_CERT_EXPIRY)) {
						context.put("ftmId", cert.getFtmId());
						context.put("make", cert.getMake());
						context.put("model", cert.getModel());
					}
				}
				break;

			case PartnerConstants.WEEKLY_SUMMARY:
				LocalDate createdDate = notificationEntity.getCreatedDatetime().toLocalDate();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

				context.put("partnerId", notificationEntity.getPartnerId());
				context.put("fromDate", createdDate.format(formatter));
				context.put("toDate", createdDate.plusDays(7).format(formatter));

				List<CertificateDetailsDto> certificateDetails = Optional.ofNullable(notificationDetails.getCertificateDetails())
						.orElse(Collections.emptyList());

				List<CertificateDetailsDto> partnerCertificates = new ArrayList<>();
				List<CertificateDetailsDto> ftmCertificates = new ArrayList<>();
				List<String> partnerIds = new ArrayList<>();
				List<String> ftmIds = new ArrayList<>();

				for (CertificateDetailsDto certDetail : certificateDetails) {
					String certType = certDetail.getCertificateType();
					if ("partner".equalsIgnoreCase(certType)) {
						partnerCertificates.add(certDetail);
						partnerIds.add(certDetail.getPartnerId());
					} else if ("ftm".equalsIgnoreCase(certType)) {
						ftmCertificates.add(certDetail);
						ftmIds.add(certDetail.getFtmId());
					}
				}

				context.put("partnerCertificateCount", partnerCertificates.size());
				context.put("ftmChipCertificateCount", ftmCertificates.size());
				context.put("partnerIdList", partnerIds);
				context.put("ftmChipDetailList", ftmIds);
				break;

			default:
				log.error("Invalid Notification Type: {}", notificationEntity.getNotificationType());
				throw new BatchJobServiceException(ErrorCode.INVALID_NOTIFICATION_TYPE.getErrorCode(),
						ErrorCode.INVALID_NOTIFICATION_TYPE.getErrorMessage());
		}

		return context;
	}

	private void sendEmail(NotificationEntity notificationEntity, String emailTemplate, String emailSubject,
			String emailId) {
		try {
			MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();

			log.debug("emailId {}", emailId);
			requestBody.add("mailTo", emailId);
			requestBody.add("mailSubject", emailSubject);
			requestBody.add("mailContent", emailTemplate);

			// Send email request
			restUtil.postApi(sendEmailUrl, null, "", "", MediaType.MULTIPART_FORM_DATA, requestBody, Map.class);
			log.info("Email sent successfully for notification ID: {}", notificationEntity.getId());
		} catch (BatchJobServiceException e) {
			log.error("Error while sending email for notification ID: {} - {}", notificationEntity.getId(),
					e.getMessage());
			throw e;
		} catch (Exception e) {
			log.error("Unexpected error while sending email for notification ID: {}", notificationEntity.getId());
			throw new BatchJobServiceException(ErrorCode.EMAIL_SEND_FAILED.getErrorCode(),
					ErrorCode.EMAIL_SEND_FAILED.getErrorMessage());
		}
	}

}
