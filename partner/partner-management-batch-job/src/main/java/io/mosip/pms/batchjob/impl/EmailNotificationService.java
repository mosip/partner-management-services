package io.mosip.pms.batchjob.impl;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pms.batchjob.config.LoggerConfiguration;
import io.mosip.pms.batchjob.constants.ErrorCodes;
import io.mosip.pms.batchjob.dto.CertificateDetailsDto;
import io.mosip.pms.batchjob.dto.NotificationDetailsDto;
import io.mosip.pms.batchjob.entity.Notification;
import io.mosip.pms.batchjob.exceptions.BatchJobServiceException;
import io.mosip.pms.batchjob.repository.NotificationServiceRepository;
import io.mosip.pms.batchjob.util.RestUtil;
import io.mosip.pms.batchjob.util.TemplateHelper;

@Service
public class EmailNotificationService {

    private static final Logger LOGGER = LoggerConfiguration.logConfig(EmailNotificationService.class);

    @Value("${pmp.partner.email.send.post.uri}")
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
            Optional<Notification> optionalNotification = notificationServiceRepository.findById(notificationId);

            if (optionalNotification.isEmpty()) {
                LOGGER.error("No notification found for ID: {}", notificationId);
                return;
            }

            Notification notification = optionalNotification.get();

            if (notification.getEmailSent()) {
                LOGGER.warn("Email notification already sent for ID: {}", notificationId);
                return;
            }

            String template = templateHelper.fetchEmailTemplate(notification.getEmailLangCode(), notification.getNotificationType());
            String populatedTemplate = populateTemplate(template, notification);
            sendEmail(notification, populatedTemplate);

            // update notification status
            notification.setEmailSent(true);
            notification.setEmailSentDatetime(LocalDateTime.now());
            notificationServiceRepository.save(notification);
            LOGGER.info("Notification status successfully updated for ID: {}", notificationId);
        } catch (BatchJobServiceException e) {
            LOGGER.error("Failed to send email for Notification ID: {} - {}", notificationId, e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error("Unexpected error while sending email for Notification ID: {} - {}", notificationId, e.getMessage(), e);
        }
    }


    private String populateTemplate(String templateContent, Notification notification) throws JsonProcessingException {
        NotificationDetailsDto notificationDetails = objectMapper.readValue(notification.getNotificationDetailsJson(), NotificationDetailsDto.class);
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

    private void sendEmail(Notification notification, String emailTemplate) {
        try {
            MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();

            // Add email details
            requestBody.add("mailTo", notification.getEmailId());
            requestBody.add("mailSubject", "Root Certificate Expiry Notification");
            requestBody.add("mailContent", emailTemplate);

            // Send email request
            restUtil.sendRequest(
                    sendEmailUrl,
                    HttpMethod.POST,
                    requestBody,
                    new TypeReference<Map>(){},
                    MediaType.MULTIPART_FORM_DATA
            );
            LOGGER.info("Email sent successfully for notification ID: {}", notification.getId());
        } catch (BatchJobServiceException e) {
            LOGGER.error("Error while sending email for notification ID: {} - {}", notification.getId(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unexpected error while sending email for notification ID: {}", notification.getId(), e);
            throw new BatchJobServiceException(
                    ErrorCodes.EMAIL_SEND_FAILED.getCode(),
                    ErrorCodes.EMAIL_SEND_FAILED.getMessage(),
                    e
            );
        }
    }



}
