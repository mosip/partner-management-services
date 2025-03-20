package io.mosip.pms.batchjob.util;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.dto.EmailTemplateDto;
import io.mosip.pms.common.dto.TemplatesResponseDto;
import io.mosip.pms.common.util.RestUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Component;

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

	@Value("${email.notification.partner.cert.expiry.subject.template}")
	private String partnerCertExpirySubjectTemplate;

	@Value("${email.notification.root.cert.expiry.subject.template}")
	private String rootCertExpirySubjectTemplate;

	@Value("${email.notification.intermediate.cert.expiry.subject.template}")
	private String intermediateCertExpirySubjectTemplate;

	@Value("${resource.template.url}")
	private String getTemplateUrl;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	RestUtil restUtil;

	@Autowired
	BatchJobHelper batchJobHelper;

	@Cacheable(value = "emailTemplates", key = "#langCode + '_' + #notificationType")
	public EmailTemplateDto fetchEmailTemplate(String langCode, String notificationType) {
		LOGGER.info("Fetching email template and subject: notificationType={}, language={}", notificationType, langCode);

		try {
			// Fetch Subject
			String subject = fetchTemplateContent(langCode, getSubjectTemplateTypeCode(notificationType));

			// Fetch Template Body
			String body = fetchTemplateContent(langCode, getBodyTemplateTypeCode(notificationType));

			EmailTemplateDto emailTemplateDto = new EmailTemplateDto();
			emailTemplateDto.setSubject(subject);
			emailTemplateDto.setBody(body);
			return emailTemplateDto;
		} catch (Exception e) {
			LOGGER.error("Error fetching template: {}", e.getMessage(), e);
			throw new BatchJobServiceException(
					ErrorCodes.TEMPLATE_FETCH_ERROR.getCode(),
					ErrorCodes.TEMPLATE_FETCH_ERROR.getMessage()
			);
		}
	}

	private String fetchTemplateContent(String langCode, String templateTypeCode) {
		Map<String, String> pathSegments = Map.of(
				LANG_CODE, langCode,
				TEMPLATE_TYPE_CODE, templateTypeCode
		);

		Map<String, Object> response = restUtil.getApi(getTemplateUrl, pathSegments, Map.class);
		batchJobHelper.validateApiResponse(response, getTemplateUrl);

		TemplatesResponseDto templatesResponseDto = objectMapper.convertValue(
				response.get(PartnerConstants.RESPONSE), TemplatesResponseDto.class
		);

		if (templatesResponseDto == null || templatesResponseDto.getTemplates().isEmpty()) {
			throw new BatchJobServiceException(
					ErrorCodes.TEMPLATE_FETCH_ERROR.getCode(),
					ErrorCodes.TEMPLATE_FETCH_ERROR.getMessage()
			);
		}

		return templatesResponseDto.getTemplates().getFirst().getFileText();
	}

	private String getSubjectTemplateTypeCode(String notificationType) {
		switch (notificationType) {
			case PartnerConstants.ROOT_CERT_EXPIRY:
				return rootCertExpirySubjectTemplate;
			case PartnerConstants.INTERMEDIATE_CERT_EXPIRY:
				return intermediateCertExpirySubjectTemplate;
			case PartnerConstants.PARTNER_CERT_EXPIRY:
				return partnerCertExpirySubjectTemplate;
			default:
				throw new BatchJobServiceException(
						ErrorCodes.INVALID_TEMPLATE_TYPE.getCode(),
						ErrorCodes.INVALID_TEMPLATE_TYPE.getMessage()
				);
		}
	}

	private String getBodyTemplateTypeCode(String notificationType) {
		switch (notificationType) {
			case PartnerConstants.ROOT_CERT_EXPIRY:
				return rootCertExpiryTemplate;
			case PartnerConstants.INTERMEDIATE_CERT_EXPIRY:
				return intermediateCertExpiryTemplate;
			case PartnerConstants.PARTNER_CERT_EXPIRY:
				return partnerCertExpiryTemplate;
			default:
				throw new BatchJobServiceException(
						ErrorCodes.INVALID_TEMPLATE_TYPE.getCode(),
						ErrorCodes.INVALID_TEMPLATE_TYPE.getMessage()
				);
		}
	}

}
