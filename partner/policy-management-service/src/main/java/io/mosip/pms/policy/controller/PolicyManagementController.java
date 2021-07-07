package io.mosip.pms.policy.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.validation.Valid;

import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.pms.common.dto.FilterValueDto;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.common.dto.PolicyFilterValueDto;
import io.mosip.pms.common.dto.PolicySearchDto;
import io.mosip.pms.common.dto.SearchAuthPolicy;
import io.mosip.pms.common.dto.SearchDto;
import io.mosip.pms.common.entity.PolicyGroup;
import io.mosip.pms.policy.dto.FilterResponseCodeDto;
import io.mosip.pms.policy.dto.KeyValuePair;
import io.mosip.pms.policy.dto.PolicyCreateRequestDto;
import io.mosip.pms.policy.dto.PolicyCreateResponseDto;
import io.mosip.pms.policy.dto.PolicyGroupCreateRequestDto;
import io.mosip.pms.policy.dto.PolicyGroupCreateResponseDto;
import io.mosip.pms.policy.dto.PolicyGroupUpdateRequestDto;
import io.mosip.pms.policy.dto.PolicyManageEnum;
import io.mosip.pms.policy.dto.PolicyResponseDto;
import io.mosip.pms.policy.dto.PolicyStatusUpdateRequestDto;
import io.mosip.pms.policy.dto.PolicyStatusUpdateResponseDto;
import io.mosip.pms.policy.dto.PolicyUpdateRequestDto;
import io.mosip.pms.policy.dto.PolicyWithAuthPolicyDto;
import io.mosip.pms.policy.dto.RequestWrapper;
import io.mosip.pms.policy.dto.ResponseWrapper;
import io.mosip.pms.policy.service.PolicyManagementService;
import io.mosip.pms.policy.util.AuditUtil;
import io.swagger.annotations.Api;


@RestController
@RequestMapping(value = "/policies")
@Api(tags = { "policy management controller " })
public class PolicyManagementController {

	private static final Logger logger = LoggerFactory.getLogger(PolicyManagementController.class);

	@Autowired
	private PolicyManagementService policyManagementService;
	
	@Autowired
	AuditUtil auditUtil;

	//@PreAuthorize("hasAnyRole('POLICYMANAGER','PARTNER_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostpoliciesgroupnew())")
	@PostMapping(value = "/group/new")
	public ResponseWrapper<PolicyGroupCreateResponseDto> definePolicyGroup(
			@RequestBody @Valid RequestWrapper<PolicyGroupCreateRequestDto> createRequest) {
		logger.info("Calling PolicyManagementService from PolicyManagementController.");
		auditUtil.setAuditRequestDto(PolicyManageEnum.CREATE_POLICY_GROUP);
		PolicyGroupCreateResponseDto responseDto = policyManagementService.createPolicyGroup(createRequest.getRequest());
		ResponseWrapper<PolicyGroupCreateResponseDto> response = new ResponseWrapper<>();
		response.setResponse(responseDto);
		response.setId(createRequest.getId());
		response.setVersion(createRequest.getVersion());
		return response;		
	}

	//@PreAuthorize("hasAnyRole('POLICYMANAGER','PARTNER_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPutpoliciesgrouppolicygroupid())")
	@PutMapping(value = "/group/{policygroupId}")
	public ResponseWrapper<PolicyGroupCreateResponseDto> updatePolicyGroup(@PathVariable String policygroupId,
			@RequestBody @Valid RequestWrapper<PolicyGroupUpdateRequestDto> createRequest) {
		logger.info("Calling PolicyManagementService from PolicyManagementController.");
		auditUtil.setAuditRequestDto(PolicyManageEnum.UPDATE_POLICY_GROUP);
		PolicyGroupCreateResponseDto responseDto = policyManagementService.updatePolicyGroup(createRequest.getRequest(), policygroupId);
		ResponseWrapper<PolicyGroupCreateResponseDto> response = new ResponseWrapper<>();
		response.setResponse(responseDto);
		response.setId(createRequest.getId());
		response.setVersion(createRequest.getVersion());		
		return response;		

	}

	//@PreAuthorize("hasAnyRole('POLICYMANAGER','PARTNER_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostpolicies())")
	@PostMapping
	public ResponseWrapper<PolicyCreateResponseDto> definePolicy(
			@RequestBody @Valid RequestWrapper<PolicyCreateRequestDto> createRequest) throws Exception {
		logger.info("Calling PolicyManagementService from PolicyManagementController.");
		ResponseWrapper<PolicyCreateResponseDto> response = new ResponseWrapper<PolicyCreateResponseDto>();
		auditUtil.setAuditRequestDto(PolicyManageEnum.CREATE_POLICY);
		PolicyCreateResponseDto responseDto = policyManagementService.
				createPolicies(createRequest.getRequest());		
		response.setId(createRequest.getId());
		response.setVersion(createRequest.getVersion());
		response.setResponse(responseDto);		
		logger.info("Returning response from PolicyManagementController.");
		return response;
	}

	//@PreAuthorize("hasAnyRole('POLICYMANAGER','PARTNER_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostpoliciespolicyidgrouppublish())")
	@PostMapping(value = "/{policyId}/group/{policygroupId}/publish")
	public ResponseWrapper<PolicyResponseDto> publishPolicy(@PathVariable @Valid String policygroupId,
			@PathVariable @Valid String policyId) throws JsonParseException, JsonMappingException, IOException {
		ResponseWrapper<PolicyResponseDto> response = new ResponseWrapper<PolicyResponseDto>();
		auditUtil.setAuditRequestDto(PolicyManageEnum.PUBLISH_POLICY);
		PolicyResponseDto responseDto = policyManagementService.publishPolicy(policygroupId, policyId);
		response.setResponse(responseDto);
		return response;
	}

	//@PreAuthorize("hasAnyRole('POLICYMANAGER','PARTNER_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPutpoliciespolicyid())")
    @PutMapping(value ="/{policyId}")
	public ResponseWrapper<PolicyCreateResponseDto> updatePolicyDetails(
			@RequestBody @Valid RequestWrapper<PolicyUpdateRequestDto> updateRequestDto, @PathVariable String policyId)
			throws Exception {
		logger.info("Calling PolicyManagementService from PolicyManagementController.");
		auditUtil.setAuditRequestDto(PolicyManageEnum.UPDATE_POLICY);
		ResponseWrapper<PolicyCreateResponseDto> response = new ResponseWrapper<PolicyCreateResponseDto>();
		PolicyCreateResponseDto responseDto = policyManagementService.updatePolicies(updateRequestDto.getRequest(),
				policyId);
		response.setResponse(responseDto);
		response.setId(updateRequestDto.getId());
		response.setVersion(updateRequestDto.getVersion());
		logger.info("Returning response from PolicyManagementController.");
		return response;
	}

	//@PreAuthorize("hasAnyRole('POLICYMANAGER','PARTNER_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPatchpoliciespolicyidgrouppolicygroupid())")
	@PatchMapping(value = "/{policyId}/group/{policygroupId}")
	public ResponseWrapper<PolicyStatusUpdateResponseDto> updatePolicyStatus(
			@RequestBody RequestWrapper<PolicyStatusUpdateRequestDto> requestDto, @PathVariable String policygroupId,
			@PathVariable String policyId) throws Exception {
		PolicyStatusUpdateRequestDto statusUpdateRequest = requestDto.getRequest();
		logger.info("Calling PolicyManagementService from PolicyManagementController.");
		auditUtil.setAuditRequestDto(PolicyManageEnum.UPDATE_POLICY_STATUS);
		ResponseWrapper<PolicyStatusUpdateResponseDto> response =  policyManagementService.
				updatePolicyStatus(statusUpdateRequest,policygroupId,policyId);	
		response.setId(requestDto.getId());
		response.setVersion(requestDto.getVersion());
		logger.info("Returning response from PolicyManagementController.");
		return response;
	}

	//@PreAuthorize("hasAnyRole('POLICYMANAGER','PARTNER_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpolicies())")
	@GetMapping
	public ResponseWrapper<List<PolicyResponseDto>> getPolicies()
			throws FileNotFoundException, IOException, ParseException {
		ResponseWrapper<List<PolicyResponseDto>> response = new ResponseWrapper<>();
		logger.info("Calling PolicyManagementService from PolicyManagementController.");
		auditUtil.setAuditRequestDto(PolicyManageEnum.GET_ALL_POLICIES);
		response.setResponse(policyManagementService.findAllPolicies());
		logger.info("Returning response from PolicyManagementController.");
		return response;
	}

	//@PreAuthorize("hasAnyRole('POLICYMANAGER','PARTNER_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpoliciespolicyid())")
	@GetMapping(value = "/{policyId}")
	public ResponseWrapper<PolicyResponseDto> getPolicy(@PathVariable String policyId) throws Exception {
		ResponseWrapper<PolicyResponseDto> response = new ResponseWrapper<>();
		logger.info("Calling PolicyManagementService from PolicyManageController.");
		auditUtil.setAuditRequestDto(PolicyManageEnum.GET_ALL_POLICIES);
		PolicyResponseDto responseDto = policyManagementService.findPolicy(policyId);
		response.setResponse(responseDto);
		auditUtil.setAuditRequestDto(PolicyManageEnum.GET_POLICY_SUCCESS);
		logger.info("Returning response from PolicyManagementController.");
		return response;
	}

	//@PreAuthorize("hasAnyRole('POLICYMANAGER','PARTNER_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpoliciesapikey())")
	@GetMapping(value = "/apikey/{apikey}")
	public ResponseWrapper<PolicyResponseDto> getPolicyAgainstApiKey(@PathVariable String apikey)
			throws FileNotFoundException, IOException, ParseException {
		ResponseWrapper<PolicyResponseDto> response = new ResponseWrapper<>();
		logger.info("Calling PolicyManagementService from PolicyManagementController.");
		auditUtil.setAuditRequestDto(PolicyManageEnum.GET_POLICY_BY_APIKEY);
		PolicyResponseDto policyGroup = policyManagementService.getAuthPolicyWithApiKey(apikey);
		response.setResponse(policyGroup);
		logger.info("Returning response from PolicyManagementController.");
		return response;
	}

	//@PreAuthorize("hasAnyRole('POLICYMANAGER','PARTNER_ADMIN','CREDENTIAL_ISSUANCE','CREATE_SHARE')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpoliciespolicyidpartnerpartnerid))")
	@GetMapping(value = "/{policyId}/partner/{partnerId}")
	public ResponseWrapper<PolicyResponseDto> getPartnersPolicy(@PathVariable String partnerId,
			@PathVariable String policyId) throws JsonParseException, JsonMappingException, IOException {
		ResponseWrapper<PolicyResponseDto> response = new ResponseWrapper<>();
		logger.info("Calling PolicyManagementService from PolicyManagementController.");
		auditUtil.setAuditRequestDto(PolicyManageEnum.GET_PARTNER_POLICY);
		PolicyResponseDto policyGroup = policyManagementService.getPartnerMappedPolicy(partnerId, policyId);
		response.setResponse(policyGroup);
		logger.info("Returning response from PolicyManagementController.");
		return response;
	}

	//@PreAuthorize("hasAnyRole('POLICYMANAGER','PARTNER_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpoliciesgrouppolicygroupid))")
	@GetMapping(value = "/group/{policygroupId}")
	public ResponseWrapper<PolicyWithAuthPolicyDto> getPolicyGroup(@PathVariable String policygroupId)
			throws JsonParseException, JsonMappingException, IOException {
		ResponseWrapper<PolicyWithAuthPolicyDto> response = new ResponseWrapper<>();
		auditUtil.setAuditRequestDto(PolicyManageEnum.GET_POLICY_GROUP);
		response.setResponse(policyManagementService.getPolicyGroupPolicy(policygroupId));
		return response;
	}

	//@PreAuthorize("hasAnyRole('POLICYMANAGER','PARTNER_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpoliciesgroupall))")
	@GetMapping(value = "/group/all")
	public ResponseWrapper<List<PolicyWithAuthPolicyDto>> getPolicyGroup()
			throws JsonParseException, JsonMappingException, IOException {
		ResponseWrapper<List<PolicyWithAuthPolicyDto>> response = new ResponseWrapper<>();
		logger.info("Calling PolicyManagementService from PolicyManagementController.");		
		auditUtil.setAuditRequestDto(PolicyManageEnum.GET_POLICY_GROUP);
		response.setResponse(policyManagementService.getPolicyGroup());
		logger.info("Returning response from PolicyManagementController.");
		return response;
	}

	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostpoliciesgroupsearch))")
	@PostMapping("/group/search")
	//@PreAuthorize("hasAnyRole('PARTNER','AUTH_PARTNER','CREDENTIAL_PARTNER','POLICYMANAGER','PARTNER_ADMIN')")
	public ResponseWrapper<PageResponseDto<PolicyGroup>> searchPolicyGroup(
			@RequestBody @Valid RequestWrapper<SearchDto> request) {
		ResponseWrapper<PageResponseDto<PolicyGroup>> responseWrapper = new ResponseWrapper<>();
		auditUtil.setAuditRequestDto(PolicyManageEnum.SEARCH_POLICY_GROUP);
		responseWrapper.setResponse(policyManagementService.searchPolicyGroup(request.getRequest()));
		return responseWrapper;
	}

	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostpoliciessearch))")
	@PostMapping("/search")
	//@PreAuthorize("hasAnyRole('PARTNER','AUTH_PARTNER','CREDENTIAL_PARTNER','CREDENTIAL_ISSUANCE','POLICYMANAGER','PARTNER_ADMIN')")
	public ResponseWrapper<PageResponseDto<SearchAuthPolicy>> searchPolicy(
			@RequestBody @Valid RequestWrapper<PolicySearchDto> request) {
		ResponseWrapper<PageResponseDto<SearchAuthPolicy>> responseWrapper = new ResponseWrapper<>();
		auditUtil.setAuditRequestDto(PolicyManageEnum.SEARCH_POLICY);
		responseWrapper.setResponse(policyManagementService.searchPolicy(request.getRequest()));
		return responseWrapper;
	}

	//@PreAuthorize("hasAnyRole('POLICYMANAGER','PARTNER_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpoliciesconfigkey))")
	@GetMapping(value = "/config/{key}")
	public ResponseWrapper<KeyValuePair<String, Object>> getValueForKey(@PathVariable String key) {
		ResponseWrapper<KeyValuePair<String, Object>> responseWrapper = new ResponseWrapper<>();
		auditUtil.setAuditRequestDto(PolicyManageEnum.GET_CONFIG_VALUE);
		responseWrapper.setResponse(policyManagementService.getValueForKey(key));
		return responseWrapper;
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostpoliciesgroupfiltervalues))")
	@PostMapping("/group/filtervalues")
	//@PreAuthorize("hasAnyRole('PARTNER','PMS_USER','AUTH_PARTNER','CREDENTIAL_PARTNER','POLICYMANAGER','PARTNER_ADMIN')")
	public ResponseWrapper<FilterResponseCodeDto> PolicyGroupFilterValues(
			@RequestBody @Valid RequestWrapper<FilterValueDto> requestWrapper) {
		ResponseWrapper<FilterResponseCodeDto> responseWrapper = new ResponseWrapper<>();
		auditUtil.setAuditRequestDto(PolicyManageEnum.FILTERVALUES_POLICY_GROUP);
		responseWrapper.setResponse(policyManagementService.policyGroupFilterValues(requestWrapper.getRequest()));
		return responseWrapper;
	}
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostpoliciesfiltervalues))")
	@PostMapping("/filtervalues")
	//@PreAuthorize("hasAnyRole('PARTNER','PMS_USER','AUTH_PARTNER','CREDENTIAL_PARTNER','POLICYMANAGER','PARTNER_ADMIN')")
	public ResponseWrapper<FilterResponseCodeDto> PolicyFilterValues(
			@RequestBody @Valid RequestWrapper<PolicyFilterValueDto> requestWrapper) {
		ResponseWrapper<FilterResponseCodeDto> responseWrapper = new ResponseWrapper<>();
		auditUtil.setAuditRequestDto(PolicyManageEnum.FILTERVALUES_POLICY);
		responseWrapper.setResponse(policyManagementService.policyFilterValues(requestWrapper.getRequest()));
		return responseWrapper;
	}
}
