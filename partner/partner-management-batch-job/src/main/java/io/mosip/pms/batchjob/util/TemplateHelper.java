package io.mosip.pms.batchjob.util;

import io.mosip.pms.batchjob.config.LoggerConfiguration;
import io.mosip.pms.batchjob.constants.ErrorCodes;
import io.mosip.pms.batchjob.dto.TemplateResponseDto;
import io.mosip.pms.batchjob.exceptions.BatchJobServiceException;
import io.mosip.pms.batchjob.impl.EmailNotificationService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Component
@EnableCaching
public class TemplateHelper {
    private static final Logger LOGGER = LoggerConfiguration.logConfig(EmailNotificationService.class);

    private static final String LANG_CODE = "langcode";
    private static final String TEMPLATE_TYPE_CODE = "templatetypecode";
    private static final String ROOT_CERT_EXPIRY = "ROOT_CERT_EXPIRY";

    @Value("${email.notification.partner.cert.expiry.template}")
    private String partnerCertExpiryTemplate;

    @Value("${email.notification.root.cert.expiry.template}")
    private String rootCertExpiryTemplate;

    @Value("${email.notification.intermediate.cert.expiry.template}")
    private String intermediateCertExpiryTemplate;

    @Value("${pmp.partner.email.template.get.rest.uri}")
    private String getTemplateUrl;

    @Autowired
    RestUtil restUtil;

    @Cacheable(value = "emailTemplates", key = "#langCode + '_' + #notificationType")
    public String fetchEmailTemplate(String langCode, String notificationType) {
        Map<String, String> pathSegments = new HashMap<>();
        pathSegments.put(LANG_CODE, langCode);
        pathSegments.put(TEMPLATE_TYPE_CODE, getTemplateTypeCode(notificationType));

        LOGGER.info("Fetching email template: notificationType={}, language={}", notificationType, langCode);
        // Build the URL
        String urlWithPath = UriComponentsBuilder.fromUriString(getTemplateUrl)
                .buildAndExpand(pathSegments)
                .toUriString();

        TemplateResponseDto response = restUtil.sendRequest(urlWithPath, HttpMethod.GET, null, TemplateResponseDto.class, MediaType.APPLICATION_JSON);
        return response.getTemplates().getFirst().getFileText();
    }

    private String getTemplateTypeCode(String notificationType) {
        switch (notificationType) {
            case ROOT_CERT_EXPIRY:
                return rootCertExpiryTemplate;
            default:
                throw new BatchJobServiceException(
                        ErrorCodes.INVALID_TEMPLATE_TYPE.getCode(),
                        ErrorCodes.INVALID_TEMPLATE_TYPE.getMessage()
                );
        }
    }
}
