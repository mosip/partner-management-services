package io.mosip.pms.batchjob.util;

import java.util.HashMap;
import java.util.Map;

import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.response.dto.TemplatesResponseDto;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.type.TypeReference;

import io.mosip.pms.batchjob.config.LoggerConfiguration;
import io.mosip.pms.batchjob.constants.ErrorCodes;
import io.mosip.pms.batchjob.exceptions.BatchJobServiceException;
import io.mosip.pms.batchjob.impl.EmailNotificationService;

@Component
@EnableCaching
public class TemplateHelper {
	private static final Logger LOGGER = LoggerConfiguration.logConfig(EmailNotificationService.class);

	private static final String LANG_CODE = "langcode";
	private static final String TEMPLATE_TYPE_CODE = "templatetypecode";

	@Value("${email.notification.partner.cert.expiry.template}")
	private String partnerCertExpiryTemplate;

	@Value("${email.notification.root.cert.expiry.template}")
	private String rootCertExpiryTemplate;

	@Value("${email.notification.intermediate.cert.expiry.template}")
	private String intermediateCertExpiryTemplate;

	@Value("${resource.template.url}")
	private String getTemplateUrl;

	@Autowired
    RestHelper restHelper;

	@Cacheable(value = "emailTemplates", key = "#langCode + '_' + #notificationType")
	public String fetchEmailTemplate(String langCode, String notificationType) {
		Map<String, String> pathSegments = new HashMap<>();
		pathSegments.put(LANG_CODE, langCode);
		pathSegments.put(TEMPLATE_TYPE_CODE, getTemplateTypeCode(notificationType));

		LOGGER.info("Fetching email template: notificationType={}, language={}", notificationType, langCode);
		// Build the URL
		String urlWithPath = UriComponentsBuilder.fromUriString(getTemplateUrl).buildAndExpand(pathSegments)
				.toUriString();

		TemplatesResponseDto response = restHelper.sendRequest(urlWithPath, HttpMethod.GET, null,
				new TypeReference<TemplatesResponseDto>() {
				}, MediaType.APPLICATION_JSON);
		return response.getTemplates().getFirst().getFileText();
	}

	private String getTemplateTypeCode(String notificationType) {
		switch (notificationType) {
		case PartnerConstants.ROOT_CERT_EXPIRY:
			return rootCertExpiryTemplate;
		case PartnerConstants.INTERMEDIATE_CERT_EXPIRY:
			return intermediateCertExpiryTemplate;	
		case PartnerConstants.PARTNER_CERT_EXPIRY:
			return partnerCertExpiryTemplate;		
		default:
			throw new BatchJobServiceException(ErrorCodes.INVALID_TEMPLATE_TYPE.getCode(),
					ErrorCodes.INVALID_TEMPLATE_TYPE.getMessage());
		}
	}
}
