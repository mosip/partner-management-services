package io.mosip.pms.batchjob.util;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.response.dto.TemplatesResponseDto;
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

	@Value("${resource.template.url}")
	private String getTemplateUrl;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	RestUtil restUtil;

	@Autowired
	BatchJobHelper batchJobHelper;

	@Cacheable(value = "emailTemplates", key = "#langCode + '_' + #notificationType")
	public String fetchEmailTemplate(String langCode, String notificationType) {
		Map<String, String> pathSegments = Map.of(
				LANG_CODE, langCode,
				TEMPLATE_TYPE_CODE, getTemplateTypeCode(notificationType)
		);

		LOGGER.info("Fetching email template: notificationType={}, language={}", notificationType, langCode);

		try {
			Map<String, Object> response = restUtil.getApi(getTemplateUrl, pathSegments, Map.class);
			batchJobHelper.validateApiResponse(response, getTemplateUrl);
			TemplatesResponseDto templatesResponseDto = objectMapper.convertValue(response.get(PartnerConstants.RESPONSE), TemplatesResponseDto.class);

			if (templatesResponseDto == null || templatesResponseDto.getTemplates().isEmpty()) {
				throw new BatchJobServiceException(
						ErrorCodes.TEMPLATE_FETCH_ERROR.getCode(),
						ErrorCodes.TEMPLATE_FETCH_ERROR.getMessage()
				);
			}

			return templatesResponseDto.getTemplates().getFirst().getFileText();
		}
		catch (Exception e) {
			LOGGER.error("Error fetching email template: {}", e.getMessage(), e);
			throw new BatchJobServiceException(
					ErrorCodes.TEMPLATE_FETCH_ERROR.getCode(),
					ErrorCodes.TEMPLATE_FETCH_ERROR.getMessage()
			);
		}
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
