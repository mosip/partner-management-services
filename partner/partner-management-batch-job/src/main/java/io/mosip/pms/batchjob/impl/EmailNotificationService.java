package io.mosip.pms.batchjob.impl;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

import io.mosip.pms.common.dto.EmailTemplateDto;
import io.mosip.pms.common.entity.NotificationEntity;
import io.mosip.pms.common.repository.NotificationServiceRepository;
import io.mosip.pms.common.util.RestUtil;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pms.batchjob.config.LoggerConfiguration;
import io.mosip.pms.batchjob.constants.ErrorCodes;
import io.mosip.pms.common.dto.CertificateDetailsDto;
import io.mosip.pms.common.dto.NotificationDetailsDto;
import io.mosip.pms.batchjob.exceptions.BatchJobServiceException;
import io.mosip.pms.batchjob.util.TemplateHelper;

@Service
public class EmailNotificationService {

    private static final Logger LOGGER = LoggerConfiguration.logConfig(EmailNotificationService.class);

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
                LOGGER.error("No notification found for ID: {}", notificationId);
                return;
            }

            NotificationEntity notificationEntity = optionalNotification.get();

            if (notificationEntity.getEmailSent() && notificationEntity.getEmailSentDatetime() != null) {
                LOGGER.warn("Email notification already sent for ID: {}", notificationId);
                return;
            }

            EmailTemplateDto templateDto = templateHelper.fetchEmailTemplate(notificationEntity.getEmailLangCode(), notificationEntity.getNotificationType());
            String populatedTemplate = populateTemplate(templateDto.getBody(), notificationEntity);
            sendEmail(notificationEntity, populatedTemplate, templateDto.getSubject());

            // update notificationEntity status
            notificationEntity.setEmailSent(true);
            notificationEntity.setEmailSentDatetime(LocalDateTime.now());
            notificationServiceRepository.save(notificationEntity);
            LOGGER.info("notification status successfully updated for ID: {}", notificationId);
        } catch (BatchJobServiceException e) {
            LOGGER.error("Failed to send email for notification ID: {} - {}", notificationId, e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error("Unexpected error while sending email for notification ID: {} - {}", notificationId, e.getMessage(), e);
        }
    }


    private String populateTemplate(String templateContent, NotificationEntity notificationEntity) throws JsonProcessingException {
        NotificationDetailsDto notificationDetails = objectMapper.readValue(notificationEntity.getNotificationDetailsJson(), NotificationDetailsDto.class);
        VelocityContext context = getVelocityContext(notificationDetails);
        StringWriter writer = new StringWriter();
        velocityEngine.evaluate(context, writer, "logTag", templateContent);
        return writer.toString();
    }

    private VelocityContext getVelocityContext(NotificationDetailsDto notificationDetails) {
        CertificateDetailsDto certificateDetails = notificationDetails.getCertificateDetails().getFirst();

        VelocityContext context = new VelocityContext();
        context.put("userName", certificateDetails.getPartnerId());
        context.put("certificateId", certificateDetails.getCertificateId());
        context.put("expiryDateTime", certificateDetails.getExpiryDateTime());
        context.put("partnerDomain", certificateDetails.getPartnerDomain());
        context.put("issuedTo", certificateDetails.getIssuedTo());
        context.put("issuedBy", certificateDetails.getIssuedBy());
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
            LOGGER.info("Email sent successfully for notification ID: {}", notificationEntity.getId());
        } catch (BatchJobServiceException e) {
            LOGGER.error("Error while sending email for notification ID: {} - {}", notificationEntity.getId(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unexpected error while sending email for notification ID: {}", notificationEntity.getId(), e);
            throw new BatchJobServiceException(
                    ErrorCodes.EMAIL_SEND_FAILED.getCode(),
                    ErrorCodes.EMAIL_SEND_FAILED.getMessage(),
                    e
            );
        }
    }

}
