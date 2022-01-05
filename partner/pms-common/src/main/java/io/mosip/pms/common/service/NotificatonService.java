package io.mosip.pms.common.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.ApiAccessibleExceptionConstant;
import io.mosip.pms.common.constant.EventType;
import io.mosip.pms.common.exception.ApiAccessibleException;
import io.mosip.pms.common.response.dto.NotificationDto;
import io.mosip.pms.common.util.NotificationUtil;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.common.util.TemplateUtil;

@Component
public class NotificatonService {

	private Logger log = PMSLogger.getLogger(NotificatonService.class);

	@Autowired
	NotificationUtil notificationUtil;

	@Value("${partner.registration.sub-template}")
	private String partnerRegistrationSubTemplate;

	@Value("${partner.reg.content-template}")
	private String partnerRegContentTemplate;

	@Value("${partner.status.sub-template}")
	private String partnerStatusSubTemplate;

	@Value("${partner.status.content-template}")
	private String partnerStatusContentTemplate;

	@Value("${partner.apikey.status.sub-template}")
	private String partnerApikeyStatusSubTemplate;

	@Value("${apikey.status.content-template}")
	private String apikeyStatusContentTemplate;
	
	@Value("${partner.apikey.status.sub-template}")
	private String apikeyStatusSubjectTemplate;

	@Autowired
	private TemplateUtil templateUtil;

	@Autowired
	RestTemplate restTemplate;

	public void sendNotications(EventType eventType, List<NotificationDto> notificationDtos) {
		switch (eventType) {
		case PARTNER_REGISTRED:
			sendNotications(notificationDtos, partnerRegistrationSubTemplate, partnerRegContentTemplate);
			break;
		case PARTNER_UPDATED:
			sendNotications(notificationDtos, partnerStatusSubTemplate, partnerStatusContentTemplate);
			break;
		case APIKEY_STATUS_UPDATED:
			sendNotications(notificationDtos, partnerApikeyStatusSubTemplate, apikeyStatusContentTemplate);
			break;
		case APIKEY_EXPIRED:
			sendNotications(notificationDtos, apikeyStatusSubjectTemplate, apikeyStatusContentTemplate);
		default:
			break;
		}

	}

	/**
	 * 
	 * @param notificationDtos
	 * @param emialSubTemplate
	 * @param emailBodyTemplate
	 */
	private void sendNotications(List<NotificationDto> notificationDtos, String emialSubTemplate,
			String emailBodyTemplate) {
		String emailSubject = getEmailSubject(emialSubTemplate, notificationDtos.get(0).getLangCode());
		Map<NotificationDto, String> mailBodyWithData = getEmailBody(emailBodyTemplate, notificationDtos);
		for (Entry<NotificationDto, String> notificationDto : mailBodyWithData.entrySet()) {
			try {
				notificationUtil.emailNotification(notificationDto.getKey(), null, emailSubject,
						notificationDto.getValue());
			} catch (IOException e) {
				log.error("error occured while send notifications.", e.getLocalizedMessage());
				throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
						ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage());
			}
		}
	}

	/**
	 * 
	 * @param templateFor
	 * @param notificationDtos
	 * @return
	 */
	private Map<NotificationDto, String> getEmailBody(String templateFor, List<NotificationDto> notificationDtos) {
		String fileText = templateUtil.getTemplate(notificationDtos.get(0).getLangCode(), templateFor);
		return mergeTemplates(fileText, notificationDtos);
	}

	/**
	 * 
	 * @param template
	 * @param data
	 * @return
	 */
	private Map<NotificationDto, String> mergeTemplates(String template, List<NotificationDto> data) {
		List<String> mergedTemplates = new ArrayList<String>();
		Map<NotificationDto, String> mailBodyDataMap = new HashMap<NotificationDto, String>();
		for (NotificationDto notificationDto : data) {
			try {
				mergedTemplates.add(templateUtil.templateMerge(template, notificationDto));
				mailBodyDataMap.put(notificationDto, templateUtil.templateMerge(template, notificationDto));
			} catch (IOException e) {
				log.error("error occured while mergeTemplates", e.getLocalizedMessage());
				throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
						ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage());
			}
		}
		return mailBodyDataMap;
	}

	/**
	 * 
	 * @param templateFor
	 * @return
	 */
	private String getEmailSubject(String templateFor, String langCode) {
		return templateUtil.getTemplate(langCode, templateFor);
	}

}