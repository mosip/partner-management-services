package io.mosip.pms.tasklets.util;

import java.util.Map;

import io.mosip.pms.partner.util.PartnerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.dto.EmailTemplateDto;
import io.mosip.pms.common.dto.TemplatesResponseDto;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.exception.BatchJobServiceException;
import io.mosip.pms.partner.manager.constant.ErrorCode;

@Component
@EnableCaching
public class TemplateHelper {
	private Logger log = PMSLogger.getLogger(TemplateHelper.class);

	private static final String LANG_CODE = "langcode";
	private static final String TEMPLATE_TYPE_CODE = "templatetypecode";

	@Value("${email.notification.partner.cert.expiry.template}")
	private String partnerCertExpiryTemplate;

	@Value("${email.notification.root.cert.expiry.template}")
	private String rootCertExpiryTemplate;

	@Value("${email.notification.intermediate.cert.expiry.template}")
	private String intermediateCertExpiryTemplate;

	@Value("${email.notification.weekly.summary.template}")
	private String weeklySummaryTemplate;

	@Value("${email.notification.ftm.chip.cert.expiry.template}")
	private String ftmChipCertExpiryTemplate;

	@Value("${email.notification.partner.cert.expiry.subject.template}")
	private String partnerCertExpirySubjectTemplate;

	@Value("${email.notification.root.cert.expiry.subject.template}")
	private String rootCertExpirySubjectTemplate;

	@Value("${email.notification.intermediate.cert.expiry.subject.template}")
	private String intermediateCertExpirySubjectTemplate;

	@Value("${email.notification.weekly.summary.subject.template}")
	private String weeklySummarySubjectTemplate;

	@Value("${email.notification.ftm.chip.cert.expiry.subject.template}")
	private String ftmChipCertExpirySubjectTemplate;

	@Value("${resource.template.url}")
	private String getTemplateUrl;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	RestUtil restUtil;
	
	@Cacheable(value = "emailTemplates", key = "#langCode + '_' + #notificationType")
	public EmailTemplateDto fetchEmailTemplate(String langCode, String notificationType) {
		log.info("Fetching email template and subject: notificationType={}, language={}", notificationType, langCode);

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
			log.error("Error fetching template: {}", e.getMessage());
			throw new BatchJobServiceException(ErrorCode.TEMPLATE_FETCH_ERROR.getErrorCode(),
					ErrorCode.TEMPLATE_FETCH_ERROR.getErrorMessage());
		}
	}

	private String fetchTemplateContent(String langCode, String templateTypeCode) {
		Map<String, String> pathSegments = Map.of(LANG_CODE, langCode, TEMPLATE_TYPE_CODE, templateTypeCode);

		Map<String, Object> response = restUtil.getApi(getTemplateUrl, pathSegments, Map.class);
		PartnerUtil.validateApiResponse(response, getTemplateUrl);

		TemplatesResponseDto templatesResponseDto = objectMapper.convertValue(response.get(PartnerConstants.RESPONSE),
				TemplatesResponseDto.class);

		if (templatesResponseDto == null || templatesResponseDto.getTemplates().isEmpty()) {
			throw new BatchJobServiceException(ErrorCode.TEMPLATE_FETCH_ERROR.getErrorCode(),
					ErrorCode.TEMPLATE_FETCH_ERROR.getErrorMessage());
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
			case PartnerConstants.WEEKLY_SUMMARY:
				return weeklySummarySubjectTemplate;
			case PartnerConstants.FTM_CHIP_CERT_EXPIRY:
				return ftmChipCertExpirySubjectTemplate;
			default:
				throw new BatchJobServiceException(ErrorCode.INVALID_TEMPLATE_TYPE.getErrorCode(),
						ErrorCode.INVALID_TEMPLATE_TYPE.getErrorMessage());
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
			case PartnerConstants.WEEKLY_SUMMARY:
				return weeklySummaryTemplate;
			case PartnerConstants.FTM_CHIP_CERT_EXPIRY:
				return ftmChipCertExpiryTemplate;
			default:
				throw new BatchJobServiceException(ErrorCode.INVALID_TEMPLATE_TYPE.getErrorCode(),
						ErrorCode.INVALID_TEMPLATE_TYPE.getErrorMessage());
		}
	}

}
