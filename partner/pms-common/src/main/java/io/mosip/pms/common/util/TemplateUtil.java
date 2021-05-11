package io.mosip.pms.common.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.templatemanager.spi.TemplateManager;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.logger.logback.factory.Logfactory;
import io.mosip.pms.common.constant.ApiAccessibleExceptionConstant;
import io.mosip.pms.common.exception.ApiAccessibleException;
import io.mosip.pms.common.response.dto.NotificationDto;
import io.mosip.pms.common.response.dto.TemplatesResponseDto;

@Component
public class TemplateUtil {

	private Logger log = Logfactory.getSlf4jLogger(TemplateUtil.class);

	@Value("${resource.template.url}")
	private String resourceUrl;

	@Value("${mosip.mandatory-languages}")
	private String mandatoryLanguage;

	@Autowired
	private TemplateManager templateManager;

	@Autowired
	RestUtil restUtil;

	@Autowired
	private ObjectMapper mapper;

	/**
	 * 
	 * @param langCode
	 * @param templatetypecode
	 * @return
	 */
	public String getTemplate(String langCode, String templatetypecode) {
		Map<String, String> pathsegments = new HashMap<>();
		pathsegments.put("langcode", mandatoryLanguage);
		pathsegments.put("templatetypecode", templatetypecode);
		log.info("In getTemplate method of TemplateUtil service url:", resourceUrl);

		TemplatesResponseDto templatesResponseDto = null;
		try {
			Map<String, Object> getApiResponse = restUtil.getApi(resourceUrl, pathsegments, Map.class);
			templatesResponseDto = mapper.readValue(mapper.writeValueAsString(getApiResponse.get("response")),
					TemplatesResponseDto.class);
		} catch (Exception e) {
			log.error("Error occured while parsing the response from template api", e.getLocalizedMessage());
			e.printStackTrace();
		}
		if(templatesResponseDto == null) {
			log.error("Template not found" , langCode,templatetypecode);
			throw new ApiAccessibleException(ApiAccessibleExceptionConstant.TEMPLATE_NOT_FOUND.getErrorCode(),
					ApiAccessibleExceptionConstant.TEMPLATE_NOT_FOUND.getErrorMessage());
		}

		return templatesResponseDto.getTemplates().get(0).getFileText().replaceAll("^\"|\"$", "");
	}

	public String templateMerge(String fileText, NotificationDto acknowledgementDTO) throws IOException {
		log.info("In templateMerge method ofTemplateUtil");
		String mergeTemplate = null;
		Map<String, Object> map = mapSetting(acknowledgementDTO);
		InputStream templateInputStream = new ByteArrayInputStream(fileText.getBytes(Charset.forName("UTF-8")));
		InputStream resultedTemplate = templateManager.merge(templateInputStream, map);
		mergeTemplate = IOUtils.toString(resultedTemplate, StandardCharsets.UTF_8.name());
		return mergeTemplate;
	}

	private Map<String, Object> mapSetting(NotificationDto acknowledgementDto) {
		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("partnerName", acknowledgementDto.getPartnerName());
		responseMap.put("partnerId", acknowledgementDto.getPartnerId());
		responseMap.put("partnerStatus", acknowledgementDto.getPartnerStatus());
		responseMap.put("status", acknowledgementDto.getPartnerStatus());
		responseMap.put("Date", DateUtils.getCurrentDateTimeString());
		responseMap.put("policyId", acknowledgementDto.getPolicyId());
		responseMap.put("policyName", acknowledgementDto.getPolicyName());
		responseMap.put("PolicyName", acknowledgementDto.getPolicyName());
		responseMap.put("policyStatus", acknowledgementDto.getPolicyStatus());
		responseMap.put("apiKey", acknowledgementDto.getApiKey());
		responseMap.put("apiKeyStatus", acknowledgementDto.getApiKeyStatus());
		responseMap.put("apiKeyExpiresOn", acknowledgementDto.getApiKeyExpiryDate());
		responseMap.put("policyExpiresOn", acknowledgementDto.getPolicyExpiryDateTime());
		return responseMap;
	}
}