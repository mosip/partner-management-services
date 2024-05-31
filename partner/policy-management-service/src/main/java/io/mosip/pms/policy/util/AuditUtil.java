package io.mosip.pms.policy.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Objects;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
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

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.authmanager.exception.AuthNException;
import io.mosip.kernel.core.authmanager.exception.AuthZException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.pms.common.constant.AuditErrorCode;
import io.mosip.pms.common.exception.ValidationException;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.policy.dto.AuditRequestDto;
import io.mosip.pms.policy.dto.AuditResponseDto;
import io.mosip.pms.policy.dto.PolicyManageEnum;
import io.mosip.pms.policy.errorMessages.PolicyManagementServiceException;


@Component
public class AuditUtil {	

	private Logger log = PMSLogger.getLogger(AuditUtil.class);
	
	@Autowired
	RestTemplate restTemplate;
	
	@Value("${mosip.kernel.masterdata.audit-url}")
	private String auditUrl;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	RestUtil restUtil;

	/** The Constant UNKNOWN_HOST. */
	private static final String UNKNOWN_HOST = "Unknown Host";

	private String hostIpAddress = null;

	private String hostName = null;
	
	public String getServerIp() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			return UNKNOWN_HOST;
		}
	}
	
	public String getServerName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return UNKNOWN_HOST;
		}
	}
	
	@PostConstruct
	public void getHostDetails() {
		hostIpAddress = getServerIp();
		hostName = getServerName();
	}
	
	public  void setAuditRequestDto(PolicyManageEnum PartnerManageEnum) {
		AuditRequestDto auditRequestDto = new AuditRequestDto();
		auditRequestDto.setHostIp(hostIpAddress);
		auditRequestDto.setHostName(hostName);
		auditRequestDto.setApplicationId(PartnerManageEnum.getApplicationId());
		auditRequestDto.setApplicationName(PartnerManageEnum.getApplicationName());
		auditRequestDto.setSessionUserId(getUserId());
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

	public  void setAuditRequestDto(PolicyManageEnum PartnerManageEnum, String refId, String refIdType) {
		AuditRequestDto auditRequestDto = new AuditRequestDto();
		auditRequestDto.setHostIp(hostIpAddress);
		auditRequestDto.setHostName(hostName);
		auditRequestDto.setApplicationId(PartnerManageEnum.getApplicationId());
		auditRequestDto.setApplicationName(PartnerManageEnum.getApplicationName());
		auditRequestDto.setSessionUserId(getUserId());
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

	private void callAuditManager(AuditRequestDto auditRequestDto)  {
		RequestWrapper<AuditRequestDto> auditReuestWrapper = new RequestWrapper<>();
		auditReuestWrapper.setRequest(auditRequestDto);
		HttpEntity<RequestWrapper<AuditRequestDto>> httpEntity = new HttpEntity<>(auditReuestWrapper);
		ResponseEntity<String> response =null;
		try {
			response =  restTemplate.exchange(auditUrl, HttpMethod.POST, httpEntity, String.class);
			getAuditDetailsFromResponse(response.getBody());
		} catch (HttpClientErrorException | HttpServerErrorException ex) {
			handlException( ex);
		} catch (Exception e) {
			log.error("Failed  to get response from audit manager", e);
		}
	}
	
	private AuditResponseDto getAuditDetailsFromResponse(String responseBody) throws Exception {
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
			throw new PolicyManagementServiceException(AuditErrorCode.AUDIT_PARSE_EXCEPTION.getErrorCode(),
					AuditErrorCode.AUDIT_PARSE_EXCEPTION.getErrorMessage());
		}

		return auditResponseDto;
	}

	private String getUserId() {
		if (Objects.nonNull(SecurityContextHolder.getContext())
				&& Objects.nonNull(SecurityContextHolder.getContext().getAuthentication())
				&& Objects.nonNull(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				&& SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof AuthUserDetails) {
			return ((AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
					.getUserId();
		} else {
			return SecurityContextHolder.getContext().getAuthentication().getName();
		}
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
		throw new PolicyManagementServiceException(AuditErrorCode.AUDIT_EXCEPTION.getErrorCode(),
				AuditErrorCode.AUDIT_EXCEPTION.getErrorMessage() + ex);

	}

}
