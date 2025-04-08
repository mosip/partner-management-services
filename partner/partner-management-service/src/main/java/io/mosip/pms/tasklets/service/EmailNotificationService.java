package io.mosip.pms.tasklets.service;

import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

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
import io.mosip.pms.exception.BatchJobServiceException;
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
	VelocityEngine velocityEngine;

	@Autowired
	NotificationServiceRepository notificationServiceRepository;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	TemplateHelper templateHelper;

	@Transactional
	public void sendEmailNotification(String notificationId) {
		try {
			Optional<NotificationEntity> optionalNotification = notificationServiceRepository.findById(notificationId);

			if (optionalNotification.isEmpty()) {
				log.error("No notification found for ID: {}", notificationId);
				return;
			}

			NotificationEntity notificationEntity = optionalNotification.get();

			if (notificationEntity.getEmailSent() && notificationEntity.getEmailSentDatetime() != null) {
				log.warn("Email notification already sent for ID: {}", notificationId);
				return;
			}

			EmailTemplateDto templateDto = templateHelper.fetchEmailTemplate(notificationEntity.getEmailLangCode(),
					notificationEntity.getNotificationType());
			String populatedTemplate = populateTemplate(templateDto.getBody(), notificationEntity);
			sendEmail(notificationEntity, populatedTemplate, templateDto.getSubject());

			// update notificationEntity status
			notificationEntity.setEmailSent(true);
			notificationEntity.setEmailSentDatetime(LocalDateTime.now(ZoneId.of("UTC")));
			notificationServiceRepository.save(notificationEntity);
			log.debug("notification status successfully updated for ID: {}", notificationId);
		} catch (BatchJobServiceException e) {
			log.error("Failed to send email for notification ID: {} - {}", notificationId, e.getMessage());
		} catch (Exception e) {
			log.error("Unexpected error while sending email for notification ID: {} - {}", notificationId,
					e.getMessage());
		}
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
				PartnerConstants.INTERMEDIATE_CERT_EXPIRY:
			CertificateDetailsDto cert = notificationDetails.getCertificateDetails().stream().findFirst().orElse(null);
			if (cert != null) {
				context.put("partnerId", notificationEntity.getPartnerId());
				context.put("certificateId", cert.getCertificateId());
				context.put("expiryDateTime", cert.getExpiryDateTime());
				context.put("partnerDomain", cert.getPartnerDomain());
				context.put("issuedTo", cert.getIssuedTo());
				context.put("issuedBy", cert.getIssuedBy());
			}
			break;

		case PartnerConstants.WEEKLY_SUMMARY:
			LocalDate createdDate = notificationEntity.getCreatedDatetime().toLocalDate();
			context.put("partnerId", notificationEntity.getPartnerId());
			context.put("fromDate", createdDate);
			context.put("toDate", createdDate.plusDays(7));
			context.put("partnerCertificateCount",
					notificationDetails.getCertificateDetails() != null
							? notificationDetails.getCertificateDetails().size()
							: 0);
			break;

		default:
			log.error("Invalid Notification Type: {}", notificationEntity.getNotificationType());
			throw new BatchJobServiceException(ErrorCode.INVALID_NOTIFICATION_TYPE.getErrorCode(),
					ErrorCode.INVALID_NOTIFICATION_TYPE.getErrorMessage());
		}

		return context;
	}

	private void sendEmail(NotificationEntity notificationEntity, String emailTemplate, String emailSubject) {
		try {
			MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();

			// Add email details
			requestBody.add("mailTo", notificationEntity.getEmailId());
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
