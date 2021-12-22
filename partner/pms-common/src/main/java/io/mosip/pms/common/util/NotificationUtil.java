package io.mosip.pms.common.util;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.pms.common.constant.ApiAccessibleExceptionConstant;
import io.mosip.pms.common.exception.ApiAccessibleException;
import io.mosip.pms.common.response.dto.NotificationDto;
import io.mosip.pms.common.response.dto.NotificationResponseDto;
import io.mosip.pms.common.response.dto.ResponseWrapper;

/**
 * 
 * @author Nagarjuna
 *
 */
@Component
public class NotificationUtil {

	private Logger log = PMSLogger.getLogger(NotificationUtil.class);

	@Value("${emailResourse.url}")
	private String emailResourseUrl;

	@Autowired
	RestUtil restUtil;

	@Autowired
	private ObjectMapper mapper;

	/**
	 * Calls the email notifier api to send email notifications
	 * 
	 * @param notificationDto
	 * @param file
	 * @param subject
	 * @param body
	 * @return
	 * @throws IOException
	 */
	public ResponseWrapper<NotificationResponseDto> emailNotification(NotificationDto notificationDto,
			MultipartFile file, String subject, String body) throws IOException {
		log.info("In emailNotification method of NotificationUtil service");
		HttpEntity<byte[]> doc = null;
		MultiValueMap<Object, Object> emailMap = new LinkedMultiValueMap<>();
		if (file != null) {
			LinkedMultiValueMap<String, String> pdfHeaderMap = new LinkedMultiValueMap<>();
			pdfHeaderMap.add("Content-disposition",
					"form-data; name=attachments; filename=" + file.getOriginalFilename());
			pdfHeaderMap.add("Content-type", "text/plain");
			doc = new HttpEntity<>(file.getBytes(), pdfHeaderMap);
			emailMap.add("attachments", doc);
		}

		ResponseWrapper<NotificationResponseDto> response = new ResponseWrapper<>();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		emailMap.add("mailContent", body);
		emailMap.add("mailSubject", subject);
		emailMap.add("mailTo", notificationDto.getEmailId());
		HttpEntity<MultiValueMap<Object, Object>> httpEntity = new HttpEntity<>(emailMap, headers);
		log.info("In emailNotification method of NotificationUtil service emailResourseUrl:", emailResourseUrl);
		NotificationResponseDto notifierResponse = new NotificationResponseDto();
		try {
			Map<String, Object> responseFromEmailAPI = restUtil.postApi(emailResourseUrl, null, "", "",
					MediaType.MULTIPART_FORM_DATA, httpEntity, Map.class);
			notifierResponse = mapper.readValue(mapper.writeValueAsString(responseFromEmailAPI.get("response")),
					NotificationResponseDto.class);
		} catch (RestClientException e) {
			log.error("Error occured while parsing the response of email notifier api.", e.getLocalizedMessage());
			throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
					ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage());
		}
		response.setResponse(notifierResponse);
		response.setResponsetime(getCurrentResponseTime());
		return response;
	}

	private LocalDateTime getCurrentResponseTime() {
		return DateUtils.getUTCCurrentDateTime();
	}
}