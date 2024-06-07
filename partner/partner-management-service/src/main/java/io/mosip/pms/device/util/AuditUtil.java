package io.mosip.pms.device.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import jakarta.annotation.PostConstruct;

import io.mosip.pms.oidc.client.contant.ClientServiceAuditEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.authmanager.exception.AuthNException;
import io.mosip.kernel.core.authmanager.exception.AuthZException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.pms.common.constant.AuditErrorCode;
import io.mosip.pms.common.exception.ValidationException;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.device.exception.DeviceServiceException;
import io.mosip.pms.device.util.dto.AuditRequestDto;
import io.mosip.pms.device.util.dto.AuditResponseDto;
import io.mosip.pms.partner.constant.PartnerServiceAuditEnum;

@Component
public class AuditUtil {

	/** The Constant APPLICATION_ID. */
	private static final String APPLICATION_ID = "MOSIP_7";

	/** The Constant APPLICATION_NAME. */
	private static final String APPLICATION_NAME = "PARTNER_MANAGEMENT";

	/** The Constant UNKNOWN_HOST. */
	private static final String UNKNOWN_HOST = "Unknown Host";
	
	private static final String NOID = "NO_ID";
	
	private static final String NOIDTYPE= "NO_ID_TYPE";

	private String hostIpAddress = null;

	private String hostName = null;

	private AtomicInteger eventCounter;

	@Value("${mosip.kernel.masterdata.audit-url}")
	private String auditUrl;
	
	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	RestUtil restUtil;	
	
	@Autowired
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate restTemplate;
	
	/**
	 * Audit request.
	 *
	 * @param auditRequestDto the audit request dto
	 */
	@PostConstruct
	private void init() {
		if (System.getProperty("seqGen") == null) {
			eventCounter = new AtomicInteger(500);
		} else {
			Integer eventCount = Integer.getInteger(System.getProperty("seqGen"));
			eventCounter = new AtomicInteger(eventCount);
		}

	}

	public void auditRequest(String eventName, String eventType, String description) {
		String eventId = "ADM-" + eventCounter.incrementAndGet();
		setAuditRequestDto(eventName, eventType, description, eventId, NOID, NOIDTYPE);
	}

	public void auditRequest(String eventName, String eventType, String description, String eventId) {
		setAuditRequestDto(eventName, eventType, description, eventId, NOID, NOIDTYPE);
	}
	
	public void auditRequest(String eventName, String eventType, String description, String eventId, String refId, String refIdType) {
		setAuditRequestDto(eventName, eventType, description, eventId, refId, refIdType);
	}

	/**
	 * Sets the audit request dto.
	 *
	 * @param auditRequestDto the new audit request dto
	 */
	private void setAuditRequestDto(String eventName, String eventType, String description, String eventId, String refId, String refIdType) {
		AuditRequestDto auditRequestDto = new AuditRequestDto();
		if (!validateSecurityContextHolder()) {

		}

		auditRequestDto.setEventId(eventId);
		auditRequestDto.setId(refId);
		auditRequestDto.setIdType(refIdType);
		auditRequestDto.setEventName(eventName);
		auditRequestDto.setEventType(eventType);
		auditRequestDto.setModuleId("PMP-AUT");
		auditRequestDto.setModuleName("partner service");
		auditRequestDto.setDescription(description);
		auditRequestDto.setActionTimeStamp(DateUtils.getUTCCurrentDateTime());
		auditRequestDto.setHostIp(hostIpAddress);
		auditRequestDto.setHostName(hostName);
		auditRequestDto.setApplicationId(APPLICATION_ID);
		auditRequestDto.setApplicationName(APPLICATION_NAME);
		auditRequestDto.setSessionUserId(SecurityContextHolder.getContext().getAuthentication().getName());
		auditRequestDto.setSessionUserName(SecurityContextHolder.getContext().getAuthentication().getName());
		auditRequestDto.setCreatedBy(SecurityContextHolder.getContext().getAuthentication().getName());		
		callAuditManager(auditRequestDto);
	}
	
	public void setAuditRequestDto(io.mosip.pms.partner.manager.constant.PartnerManageEnum PartnerManageEnum) {
		AuditRequestDto auditRequestDto = new AuditRequestDto();
		auditRequestDto.setHostIp(hostIpAddress);
		auditRequestDto.setHostName(hostName);
		auditRequestDto.setApplicationId(PartnerManageEnum.getApplicationId());
		auditRequestDto.setApplicationName(PartnerManageEnum.getApplicationName());
		auditRequestDto.setSessionUserId(SecurityContextHolder.getContext().getAuthentication().getName());
		auditRequestDto.setSessionUserName(SecurityContextHolder.getContext().getAuthentication().getName());
		auditRequestDto.setCreatedBy(SecurityContextHolder.getContext().getAuthentication().getName());
		auditRequestDto.setActionTimeStamp(DateUtils.getUTCCurrentDateTime());
		auditRequestDto.setDescription(PartnerManageEnum.getDescription());
		auditRequestDto.setEventType(PartnerManageEnum.getType());
		auditRequestDto.setEventName(PartnerManageEnum.getName());
		auditRequestDto.setModuleId(PartnerManageEnum.getModuleId());
		auditRequestDto.setModuleName(PartnerManageEnum.getModuleName());
		auditRequestDto.setEventId(PartnerManageEnum.getEventId());
		auditRequestDto.setId(PartnerManageEnum.getId());
		auditRequestDto.setIdType(PartnerManageEnum.getIdType());
		callAuditManager(auditRequestDto);
	}
	
	public void setAuditRequestDto(io.mosip.pms.partner.manager.constant.PartnerManageEnum PartnerManageEnum, String refId, String refIdType) {
		AuditRequestDto auditRequestDto = new AuditRequestDto();
		auditRequestDto.setHostIp(hostIpAddress);
		auditRequestDto.setHostName(hostName);
		auditRequestDto.setApplicationId(PartnerManageEnum.getApplicationId());
		auditRequestDto.setApplicationName(PartnerManageEnum.getApplicationName());
		auditRequestDto.setSessionUserId(SecurityContextHolder.getContext().getAuthentication().getName());
		auditRequestDto.setSessionUserName(SecurityContextHolder.getContext().getAuthentication().getName());
		auditRequestDto.setCreatedBy(SecurityContextHolder.getContext().getAuthentication().getName());
		auditRequestDto.setActionTimeStamp(DateUtils.getUTCCurrentDateTime());
		auditRequestDto.setDescription(PartnerManageEnum.getDescription());
		auditRequestDto.setEventType(PartnerManageEnum.getType());
		auditRequestDto.setEventName(PartnerManageEnum.getName());
		auditRequestDto.setModuleId(PartnerManageEnum.getModuleId());
		auditRequestDto.setModuleName(PartnerManageEnum.getModuleName());
		auditRequestDto.setEventId(PartnerManageEnum.getEventId());
		auditRequestDto.setId(refId);
		auditRequestDto.setIdType(refIdType);
		callAuditManager(auditRequestDto);
	}
	
	public void setAuditRequestDto(PartnerServiceAuditEnum PartnerManageEnum) {
		AuditRequestDto auditRequestDto = new AuditRequestDto();
		auditRequestDto.setHostIp(hostIpAddress);
		auditRequestDto.setHostName(hostName);
		auditRequestDto.setApplicationId(PartnerManageEnum.getApplicationId());
		auditRequestDto.setApplicationName(PartnerManageEnum.getApplicationName());
		auditRequestDto.setSessionUserId(SecurityContextHolder.getContext().getAuthentication().getName());
		auditRequestDto.setSessionUserName(SecurityContextHolder.getContext().getAuthentication().getName());
		auditRequestDto.setCreatedBy(SecurityContextHolder.getContext().getAuthentication().getName());
		auditRequestDto.setActionTimeStamp(DateUtils.getUTCCurrentDateTime());
		auditRequestDto.setDescription(PartnerManageEnum.getDescription());
		auditRequestDto.setEventType(PartnerManageEnum.getType());
		auditRequestDto.setEventName(PartnerManageEnum.getName());
		auditRequestDto.setModuleId(PartnerManageEnum.getModuleId());
		auditRequestDto.setModuleName(PartnerManageEnum.getModuleName());
		auditRequestDto.setEventId(PartnerManageEnum.getEventId());
		auditRequestDto.setId(PartnerManageEnum.getId());
		auditRequestDto.setIdType(PartnerManageEnum.getIdType());
		callAuditManager(auditRequestDto);
	}
	
	public void setAuditRequestDto(PartnerServiceAuditEnum PartnerManageEnum, String refId, String refIdType) {
		AuditRequestDto auditRequestDto = new AuditRequestDto();
		auditRequestDto.setHostIp(hostIpAddress);
		auditRequestDto.setHostName(hostName);
		auditRequestDto.setApplicationId(PartnerManageEnum.getApplicationId());
		auditRequestDto.setApplicationName(PartnerManageEnum.getApplicationName());
		auditRequestDto.setSessionUserId(SecurityContextHolder.getContext().getAuthentication().getName());
		auditRequestDto.setSessionUserName(SecurityContextHolder.getContext().getAuthentication().getName());
		auditRequestDto.setCreatedBy(SecurityContextHolder.getContext().getAuthentication().getName());
		auditRequestDto.setActionTimeStamp(DateUtils.getUTCCurrentDateTime());
		auditRequestDto.setDescription(PartnerManageEnum.getDescription());
		auditRequestDto.setEventType(PartnerManageEnum.getType());
		auditRequestDto.setEventName(PartnerManageEnum.getName());
		auditRequestDto.setModuleId(PartnerManageEnum.getModuleId());
		auditRequestDto.setModuleName(PartnerManageEnum.getModuleName());
		auditRequestDto.setEventId(PartnerManageEnum.getEventId());
		auditRequestDto.setId(refId);
		auditRequestDto.setIdType(refIdType);
		callAuditManager(auditRequestDto);
	}

	/**
	 * Validate security context holder.
	 *
	 * @return true, if successful
	 */
	private boolean validateSecurityContextHolder() {
		Predicate<SecurityContextHolder> contextPredicate = i -> SecurityContextHolder.getContext() != null;
		Predicate<SecurityContextHolder> authPredicate = i -> SecurityContextHolder.getContext()
				.getAuthentication() != null;
		Predicate<SecurityContextHolder> principlePredicate = i -> SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal() != null;
		return contextPredicate.and(authPredicate).and(principlePredicate) != null;

	}

	/**
	 * Gets the server ip.
	 *
	 * @return the server ip
	 */
	public String getServerIp() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			return UNKNOWN_HOST;
		}
	}

	/**
	 * Gets the server name.
	 *
	 * @return the server name
	 */
	public String getServerName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return UNKNOWN_HOST;
		}
	}

	/**
	 * To Set the Host Ip & Host Name
	 */
	@PostConstruct
	public void getHostDetails() {
		hostIpAddress = getServerIp();
		hostName = getServerName();
	}

	/**
	 * For Auditing Login Services
	 * 
	 * @param auditRequestDto
	 * @param token
	 * @return
	 */
	public void callAuditManager(AuditRequestDto auditRequestDto) {

		RequestWrapper<AuditRequestDto> auditReuestWrapper = new RequestWrapper<>();
		auditReuestWrapper.setRequest(auditRequestDto);
		HttpEntity<RequestWrapper<AuditRequestDto>> httpEntity = new HttpEntity<>(auditReuestWrapper);
		ResponseEntity<String> response =null;
		try {
			response =  restTemplate.exchange(auditUrl, HttpMethod.POST, httpEntity, String.class);

		} catch (HttpClientErrorException | HttpServerErrorException ex) {
			handlException(ex);
		}
		String responseBody = response.getBody();
		getAuditDetailsFromResponse(responseBody);

	}

	private AuditResponseDto getAuditDetailsFromResponse(String responseBody) {

		List<ServiceError> validationErrorsList = null;
		validationErrorsList = ExceptionUtils.getServiceErrorList(responseBody);
		AuditResponseDto auditResponseDto = null;
		if (!validationErrorsList.isEmpty()) {
			throw new ValidationException(validationErrorsList);
		}
		ResponseWrapper<AuditResponseDto> responseObject = null;
		try {

			responseObject = objectMapper.readValue(responseBody,
					new TypeReference<ResponseWrapper<AuditResponseDto>>() {
					});
			auditResponseDto = responseObject.getResponse();
		} catch (IOException | NullPointerException exception) {
			throw new DeviceServiceException(AuditErrorCode.AUDIT_PARSE_EXCEPTION.getErrorCode(),
					AuditErrorCode.AUDIT_PARSE_EXCEPTION.getErrorMessage());
		}

		return auditResponseDto;
	}

	private void handlException(HttpStatusCodeException ex) {
		List<ServiceError> validationErrorsList = ExceptionUtils.getServiceErrorList(ex.getResponseBodyAsString());

		if (ex.getRawStatusCode() == 401) {
			if (!validationErrorsList.isEmpty()) {
				throw new AuthNException(validationErrorsList);
			} else {
				throw new BadCredentialsException("Authentication failed from AuthManager");
			}
		}
		if (ex.getRawStatusCode() == 403) {
			if (!validationErrorsList.isEmpty()) {
				throw new AuthZException(validationErrorsList);
			} else {
				throw new AccessDeniedException("Access denied from AuthManager");
			}
		}
		throw new DeviceServiceException(AuditErrorCode.AUDIT_EXCEPTION.getErrorCode(),
				AuditErrorCode.AUDIT_EXCEPTION.getErrorMessage() + ex);

	}

	public void setAuditRequestDto(ClientServiceAuditEnum clientServiceAuditEnum, String refId, String refIdType) {
		AuditRequestDto auditRequestDto = new AuditRequestDto();
		auditRequestDto.setHostIp(hostIpAddress);
		auditRequestDto.setHostName(hostName);;
		auditRequestDto.setApplicationId(clientServiceAuditEnum.getApplicationId());
		auditRequestDto.setApplicationName(clientServiceAuditEnum.getApplicationName());
		auditRequestDto.setSessionUserId(SecurityContextHolder.getContext().getAuthentication().getName());
		auditRequestDto.setSessionUserName(SecurityContextHolder.getContext().getAuthentication().getName());
		auditRequestDto.setCreatedBy(SecurityContextHolder.getContext().getAuthentication().getName());
		auditRequestDto.setActionTimeStamp(DateUtils.getUTCCurrentDateTime());
		auditRequestDto.setDescription(clientServiceAuditEnum.getDescription());
		auditRequestDto.setEventType(clientServiceAuditEnum.getType());
		auditRequestDto.setEventName(clientServiceAuditEnum.getName());
		auditRequestDto.setModuleId(clientServiceAuditEnum.getModuleId());
		auditRequestDto.setModuleName(clientServiceAuditEnum.getModuleName());
		auditRequestDto.setEventId(clientServiceAuditEnum.getEventId());
		auditRequestDto.setId(refId);
		auditRequestDto.setIdType(refIdType);
		callAuditManager(auditRequestDto);
	}

	public void setAuditRequestDto(ClientServiceAuditEnum clientServiceAuditEnum) {
		AuditRequestDto auditRequestDto = new AuditRequestDto();
		auditRequestDto.setHostIp(hostIpAddress);
		auditRequestDto.setHostName(hostName);;
		auditRequestDto.setApplicationId(clientServiceAuditEnum.getApplicationId());
		auditRequestDto.setApplicationName(clientServiceAuditEnum.getApplicationName());
		auditRequestDto.setSessionUserId(SecurityContextHolder.getContext().getAuthentication().getName());
		auditRequestDto.setSessionUserName(SecurityContextHolder.getContext().getAuthentication().getName());
		auditRequestDto.setCreatedBy(SecurityContextHolder.getContext().getAuthentication().getName());
		auditRequestDto.setActionTimeStamp(DateUtils.getUTCCurrentDateTime());
		auditRequestDto.setDescription(clientServiceAuditEnum.getDescription());
		auditRequestDto.setEventType(clientServiceAuditEnum.getType());
		auditRequestDto.setEventName(clientServiceAuditEnum.getName());
		auditRequestDto.setModuleId(clientServiceAuditEnum.getModuleId());
		auditRequestDto.setModuleName(clientServiceAuditEnum.getModuleName());
		auditRequestDto.setEventId(clientServiceAuditEnum.getEventId());
		callAuditManager(auditRequestDto);
	}
}
