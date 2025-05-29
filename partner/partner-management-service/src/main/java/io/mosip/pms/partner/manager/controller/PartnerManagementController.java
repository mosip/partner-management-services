package io.mosip.pms.partner.manager.controller;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.partner.manager.dto.*;
import io.mosip.pms.partner.util.FeatureAvailabilityUtil;
import io.mosip.pms.partner.util.PartnerHelper;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.partner.manager.constant.PartnerManageEnum;
import io.mosip.pms.partner.manager.service.PartnerManagerService;
import io.mosip.pms.partner.request.dto.APIkeyStatusUpdateRequestDto;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;

/**
 * <p>This is the MOSIP Partner Management Service controller. This defines all the necessary operations</p>
 * 1.Partner ApiKey To Policy Mappings{{@link #PartnerApiKeyToPolicyMappings(RequestWrapper, String, String)}
 * 2.Activate Deactivate AuthEKYC Partners{{@link #activateDeactivateAuthEKYCPartners(String, RequestWrapper)}
 * 3.Activate Deactivate Partner APIKey for Given Partner{{@link #activateDeactivatePartnerAPIKeyGivenPartner(String, RequestWrapper, String)}
 * 4.Approve or Reject Partner APIKey Requests Based On APIKeyRequestId{{@link #approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(RequestWrapper, String)}
 * 5.Get All Auth EKYC Partners For PolicyGroup{{@link #getAllAuthEKYCPartnersForThePolicyGroup()}
 * 6.Get particular Auth EKYC PartnerDetails For GivenPartnerId{{@link #getparticularAuthEKYCPartnerDetailsForGivenPartnerId(String)}
 * 7.Get Partner APIKey To PolicyMappings{{@link #getPartnerAPIKeyToPolicyMappings(String, String)}
 * 8.Get All Partner APIKey Requests As Received By PartnerManager{{@link #getAllPartnerAPIKeyRequestsAsReceivedByPartnerManager()}
 * 9.Get Request For Partner APIKey To PolicyMappings For GivenRequestId{{@link #getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestId(String)}
 *
 * @author sanjeev.shrivastava
 *
 */

@RestController
@Api(tags = { "Partner Management Controller" })
public class PartnerManagementController {
	
	@Autowired
	PartnerManagerService partnerManagementService;
	
	@Autowired
	AuditUtil auditUtil;

	@Autowired
	PartnerHelper partnerHelper;

	@Autowired
	FeatureAvailabilityUtil featureAvailabilityUtil;
	
	String msg = "mosip.partnermanagement.partners.retrieve";
	String version = "1.0";


	/**
	 * This endpoint would be used by partner Manager, to update Partner API key to Policy Mappings.
	 * @param request this class contain oldPolicyID and newPolicyID
	 * @param partnerId this is unique id created after self registered by partner
	 * @param partnerApiKey this is unique id created by partner manager at the time of approving partner request
	 * @return response this class contains massage about API key created successfully
	 */
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPutpartnersapikeypolicies())")
	@RequestMapping(value = "/partners/{partnerId}/apikey/{apikey}/policies", method = RequestMethod.PUT)
	@Operation(summary = "Service to update policies against to API key", description = "Service to update policies against to API key")
	public ResponseEntity<ResponseWrapper<PartnersPolicyMappingResponse>> partnerApiKeyToPolicyMappings(
			@RequestBody @Valid RequestWrapper<PartnersPolicyMappingRequest> request, 
			@PathVariable String partnerId,
			@PathVariable String apikey) {		
		auditUtil.setAuditRequestDto(PartnerManageEnum.API_KEY_MAPPING);
		PartnersPolicyMappingRequest partnersPolicyMappingRequest = request.getRequest();
		ResponseWrapper<PartnersPolicyMappingResponse> response = new ResponseWrapper<>();
		PartnersPolicyMappingResponse partnersPolicyMappingResponse=null;
		response.setId(request.getId());
		response.setVersion(request.getVersion());
		partnersPolicyMappingResponse = partnerManagementService
				.updatePolicyAgainstApikey(partnersPolicyMappingRequest, partnerId, apikey);
		response.setResponse(partnersPolicyMappingResponse);
		auditUtil.setAuditRequestDto(PartnerManageEnum.API_KEY_MAPPING_SUCCESS);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	/**
	 * This endpoint would be used to activate/deactivate Auth/E-KYC Partners
	 * @param partnerId this is unique id created after self registered by partner
	 * @param request this class contains the status of activate/deactivate Auth/E-KYC Partners
	 * @return respons this class contains massage about Partner status updated successfully
	 */
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPatchpartners())")
	@RequestMapping(value = "/partners/{partnerId}", method = RequestMethod.PATCH)
	@Operation(summary = "Service to activate/de-activate partner", description = "Service to activate/de-activate partner")
	 public ResponseEntity<ResponseWrapper<PartnersPolicyMappingResponse>> activateDeactivatePartner(
			 @PathVariable String partnerId,
			 @RequestBody @Valid RequestWrapper<StatusRequestDto> request){
		ResponseWrapper<PartnersPolicyMappingResponse> response = new ResponseWrapper<>();
		PartnersPolicyMappingResponse partnersPolicyMappingResponse = null;
		response.setId(request.getId());
		response.setVersion(request.getVersion());
		auditUtil.setAuditRequestDto(PartnerManageEnum.ACTIVATE_DEACTIVATE_KYC_PARTNERS);
		StatusRequestDto activateDeactivatePartnerRequest = request.getRequest();
		partnersPolicyMappingResponse = partnerManagementService
				.activateDeactivateAuthEKYCPartner(partnerId,activateDeactivatePartnerRequest);
		response.setResponse(partnersPolicyMappingResponse);
		auditUtil.setAuditRequestDto(PartnerManageEnum.ACTIVATE_DEACTIVATE_KYC_PARTNERS_SUCCESS);
		return new ResponseEntity<>(response, HttpStatus.OK);
	 }

	/*
	 * This endpoint has been deprecated since the release-1.2.2.0
	 * It has been replaced by the new GET /partners/v3 endpoint.
	 * The functionality provided by this API is now available in the new endpoint.
	 * Please use the new endpoint for all future requests.
	 */
	@Deprecated(since = "release-1.2.2.0")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpartners())")
	@RequestMapping(value = "/partners", method = RequestMethod.GET)
	@Operation(summary = "Service to get partner details - deprecated since release-1.2.2.0.",
			description = "This endpoint has been deprecated since the release-1.2.2.0 and replaced by the GET /admin-partners endpoint")
	public ResponseEntity<ResponseWrapper<RetrievePartnerDetailsResponse>> getPartners(
			@RequestParam("partnerType") Optional<String> partnerType){
		ResponseWrapper<RetrievePartnerDetailsResponse> response=new ResponseWrapper<>();
		RetrievePartnerDetailsResponse retrievePartnerDetailsResponse = null;
		retrievePartnerDetailsResponse = partnerManagementService.getAllAuthEKYCPartnersForThePolicyGroup(partnerType);
		response.setId(msg);
		response.setVersion(version);
		response.setResponse(retrievePartnerDetailsResponse);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/*
	 * This endpoint has been deprecated since the release-1.2.2.0
	 * It has been replaced by the new GET /partners/v3 endpoint.
	 * The functionality provided by this API is now available in the new endpoint.
	 * Please use the new endpoint for all future requests.
	 */
	@Deprecated(since = "release-1.2.2.0")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpartnersnew())")
	@RequestMapping(value = "/partners/v2", method = RequestMethod.GET)
	@Operation(summary = "Service to get partner details - deprecated since release-1.2.2.0.",
			description = "This endpoint has been deprecated since the release-1.2.2.0 and replaced by the GET /admin-partners endpoint")
	public ResponseEntity<ResponseWrapper<PartnerDetailsResponse>> getPartnersDeatils(
			@RequestParam("partnerType") Optional<String> partnerType){
		ResponseWrapper<PartnerDetailsResponse> response=new ResponseWrapper<>();
		PartnerDetailsResponse retrievePartnerDetailsResponse = null;
		retrievePartnerDetailsResponse = partnerManagementService.getPartners(partnerType);
		response.setId(msg);
		response.setVersion(version);
		response.setResponse(retrievePartnerDetailsResponse);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	
	/**
	 * Partner managers would be using this request to retrieve the Partner API key
	 * to Policy Mappings. Partner management system would be able to validate
	 * Partner API Key pattern, validate expiry for Partner API Key and status
	 * details in background, while fetching Policy to Partner API mappings.
	 * 
	 * @param partnerId this is unique id created after self registered by partner
	 * @param partnerApiKey this is unique id created by partner manager at the time of approving partner request
	 * @return response this class contains partnerID and policyId
	 */
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpartnersapikey())")
	@RequestMapping(value = "/partners/{partnerId}/apikey/{apikey}" , method = RequestMethod.GET)
	@Operation(summary = "Service to get policy for given API key", description = "Service to get policy for given API key")
	public ResponseEntity<ResponseWrapper<PartnerAPIKeyToPolicyMappingsResponse>> getPolicyMappedToAPIKey(
			@PathVariable String partnerId,
			@PathVariable String apikey){
		ResponseWrapper<PartnerAPIKeyToPolicyMappingsResponse> response = new ResponseWrapper<>();
		PartnerAPIKeyToPolicyMappingsResponse partnerAPIKeyToPolicyMappingsResponse = null;
		partnerAPIKeyToPolicyMappingsResponse = partnerManagementService.getPartnerAPIKeyToPolicyMapping(partnerId,
				apikey);
		response.setId(msg);
		response.setVersion(version);
		response.setResponse(partnerAPIKeyToPolicyMappingsResponse);
		return new ResponseEntity<>(response , HttpStatus.OK);
	}

	/*
	 * This endpoint has been deprecated since the release-1.2.2.0
	 * It has been replaced by the new GET /partner-policy-requests endpoint.
	 * The functionality provided by this API is now available in the new endpoint.
	 * Please use the new endpoint for all future requests.
	 */
	@Deprecated(since = "release-1.2.2.0")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpartnersapikey())")
	@RequestMapping(value = "/partners/apikey" , method = RequestMethod.GET)
	@Operation(summary = "Service to get API key requests - deprecated since release-1.2.2.0.",
			description = "This endpoint has been deprecated since the release-1.2.2.0 and replaced by the GET /partner-policy-requests endpoint")
	public ResponseEntity<ResponseWrapper<PartnerAPIKeyRequestsResponse>> getAPIKeyRequests(){
		List<ApikeyRequests> apikeyRequests = null;
		ResponseWrapper<PartnerAPIKeyRequestsResponse> response = new ResponseWrapper<>();
		PartnerAPIKeyRequestsResponse partnerAPIKeyRequestsResponse = new PartnerAPIKeyRequestsResponse();
		apikeyRequests = partnerManagementService.getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagers();
		partnerAPIKeyRequestsResponse.setApikeyRequests(apikeyRequests);
		response.setId(msg);
		response.setVersion(version);
		response.setResponse(partnerAPIKeyRequestsResponse);
		return new ResponseEntity<>(response , HttpStatus.OK);
	}
	
	/**
	 * This endpoint would be used to retrieve the request for Partner API key to Policy Mappings for given request id.
	 * @param apiKeyReqId this is unique id created after partner request for Partner API Key
	 * @return response this class contains details related to Partner API key to Policy Mappings
	 */
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpartnersapikey())")
	@RequestMapping(value = "/partners/apikey/{apikey}" , method = RequestMethod.GET)
	@Operation(summary = "Service to get API key request", description = "Service to get API key request")
	public ResponseEntity<ResponseWrapper<ApikeyRequests>> getAPIKeyRequest(
			@PathVariable String apikey) {
		ResponseWrapper<ApikeyRequests> response = new ResponseWrapper<>();
		ApikeyRequests apikeyRequests=null;
		apikeyRequests = partnerManagementService.getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestId(apikey);
		response.setId(msg);
		response.setVersion(version);
		response.setResponse(apikeyRequests);
		return new ResponseEntity<>(response , HttpStatus.OK);
	}	
	
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPutpartnerspolicymapping())")
	@RequestMapping(value = "/partners/policy/{mappingkey}", method = RequestMethod.PUT)
	@Operation(summary = "Service to approve/reject partner policy mapping", description = "Service to approve/reject partner policy mapping")
	public ResponseEntity<ResponseWrapper<String>> approveRejectPolicyMappings(
			@RequestBody @Valid RequestWrapper<StatusRequestDto> request,
			@PathVariable String mappingkey){
		ResponseWrapper<String> response = new ResponseWrapper<>();		
		response.setId(request.getId());
		response.setVersion(request.getVersion());		
		auditUtil.setAuditRequestDto(PartnerManageEnum.APPROVE_REJECT_PARTNER_API);
		response.setResponse(partnerManagementService.approveRejectPartnerPolicyMapping(mappingkey, request.getRequest()));		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPatchpartnerspolicyapikeystatus())")
	@RequestMapping(value = "/partners/{partnerId}/policy/{policyId}/apiKey/status", method = RequestMethod.PATCH)
	@Operation(summary = "Service to activate/de-activate partner API key", description = "Service to activate/de-activate partner API key")
	public ResponseEntity<ResponseWrapper<String>> activateDeactivatePartnerAPIKey(@PathVariable String partnerId,
			@PathVariable String policyId, @RequestBody @Valid RequestWrapper<APIkeyStatusUpdateRequestDto> request) {
		ResponseWrapper<String> response = new ResponseWrapper<>();
		response.setId(request.getId());
		response.setVersion(request.getVersion());
		auditUtil.setAuditRequestDto(PartnerManageEnum.ACTIVATE_DEACTIVATE_API_PARTNERS);
		response.setResponse(partnerManagementService.updateAPIKeyStatus(partnerId, policyId, request.getRequest()));
		auditUtil.setAuditRequestDto(PartnerManageEnum.ACTIVATE_DEACTIVATE_API_PARTNERS_SUCCESS);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpartnerdetails())")
	@GetMapping(value = "/admin-partners/{partnerId}")
	@Operation(summary = "This endpoint retrieves all the details of the Partner based on Partner Id.",
	description = "Available since release-1.2.2.0. It is configured for the role PARTNER_ADMIN.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<PartnerDetailsV3Dto> getPartnerDetails(@PathVariable String partnerId) {
		return partnerManagementService.getPartnerDetails(partnerId);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetadminpartners())")
	@GetMapping(value = "/admin-partners")
	@Operation(summary = "This endpoint retrieves a list of all Partners.",
			description = "Available since release-1.2.2.0. This endpoint supports pagination, sorting, and filtering. It is configured for the role PARTNER_ADMIN.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<PageResponseV2Dto<PartnerSummaryDto>> getAdminPartners(
			@RequestParam(value = "sortFieldName", required = false) String sortFieldName,
			@RequestParam(value = "sortType", required = false) String sortType, // e.g., ASC or DESC
			@RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(value = "pageSize", defaultValue = "8") Integer pageSize,
			@RequestParam(value = "partnerId", required = false) String partnerId,
			@RequestParam(value = "partnerType", required = false) String partnerType,
			@RequestParam(value = "isActive", required = false) Boolean isActive,
			@RequestParam(value = "orgName", required = false) String orgName,
			@RequestParam(value = "emailAddress", required = false) String emailAddress,
			@Parameter(
					description = "Status of certificate upload",
					in = ParameterIn.QUERY,
					schema = @Schema(allowableValues = {"uploaded", "not_uploaded"})
			)
			@RequestParam(value = "certificateUploadStatus", required = false) String certificateUploadStatus,
			@RequestParam(value = "policyGroupName", required = false) String policyGroupName
	) {
		partnerHelper.validateRequestParameters(partnerHelper.partnerAliasToColumnMap, sortFieldName, sortType, pageNo, pageSize);
		PartnerFilterDto partnerFilterDto = new PartnerFilterDto();
		if (partnerId != null) {
			partnerFilterDto.setPartnerId(partnerId.toLowerCase());
		}
		if (partnerType != null) {
			partnerFilterDto.setPartnerTypeCode(partnerType.toLowerCase());
		}
		if (orgName != null) {
			partnerFilterDto.setOrganizationName(orgName.toLowerCase());
		}
		if (policyGroupName != null) {
			partnerFilterDto.setPolicyGroupName(policyGroupName.toLowerCase());
		}
		if (certificateUploadStatus != null) {
			partnerFilterDto.setCertificateUploadStatus(certificateUploadStatus);
		}
		if (emailAddress != null) {
			partnerFilterDto.setEmailAddress(emailAddress.toLowerCase());
		}
		if (isActive != null) {
			partnerFilterDto.setIsActive(isActive);
		}
		return partnerManagementService.getAdminPartners(sortFieldName, sortType, pageNo, pageSize, partnerFilterDto);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetallpartnerpolicymappingrequests())")
	@GetMapping(value = "/partner-policy-requests")
	@Operation(summary = "This endpoint fetches list of all the policy requests made by the partners.",
	description = "Available since release-1.2.2.0. This endpoint supports pagination, sorting, and filtering based on optional query parameters. If the token used to access this endpoint, does not have the PARTNER_ADMIN role, then it will fetch all the policy requests made by all the partners associated with the logged in user only.If the token used to access this endpoint, has PARTNER_ADMIN role, then it will fetch all the policy requests made by all the partners.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<PageResponseV2Dto<PartnerPolicyRequestSummaryDto>> getAllPartnerPolicyRequests(
			@RequestParam(value = "sortFieldName", required = false) String sortFieldName,
			@RequestParam(value = "sortType", required = false) String sortType,
			@RequestParam(value = "pageNo", required = false) Integer pageNo,
			@RequestParam(value = "pageSize", required = false) Integer pageSize,
			@RequestParam(value = "partnerId", required = false) String partnerId,
			@RequestParam(value = "partnerComment", required = false) String partnerComment,
			@RequestParam(value = "orgName", required = false) String orgName,
			@Parameter(
					description = "Status of request",
					in = ParameterIn.QUERY,
					schema = @Schema(allowableValues = {"approved", "rejected", "InProgress"})
			)
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "policyId", required = false) String policyId,
			@RequestParam(value = "policyName", required = false) String policyName,
			@RequestParam(value = "policyGroupName", required = false) String policyGroupName,
			@RequestParam(value = "partnerType", required = false) String partnerType
	) {
		partnerHelper.validateRequestParameters(partnerHelper.partnerPolicyMappingAliasToColumnMap, sortFieldName, sortType, pageNo, pageSize);
		PartnerPolicyRequestFilterDto filterDto = new PartnerPolicyRequestFilterDto();
		if (partnerId != null) {
			filterDto.setPartnerId(partnerId.toLowerCase());
		}
		if (partnerComment != null) {
			filterDto.setPartnerComment(partnerComment.toLowerCase());
		}
		if (orgName != null) {
			filterDto.setOrganizationName(orgName.toLowerCase());
		}
		if (status != null) {
			filterDto.setStatus(status);
		}
		if (policyId != null) {
			filterDto.setPolicyId(policyId.toLowerCase());
		}
		if (policyName != null) {
			filterDto.setPolicyName(policyName.toLowerCase());
		}
		if (policyGroupName != null) {
			filterDto.setPolicyGroupName(policyGroupName.toLowerCase());
		}
		if (partnerType != null) {
			filterDto.setPartnerType(partnerType.toLowerCase());
		}
		return partnerManagementService.getAllPartnerPolicyRequests(sortFieldName, sortType, pageNo, pageSize, filterDto);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpartnersapikeyrequests())")
	@GetMapping(value = "/partner-api-keys")
	@Operation(summary = "This endpoint retrieves a list of all the API keys created by the Auth Partners.",
	description = "Available since release-1.2.2.0. This endpoint supports pagination, sorting, and and filtering based on optional query parameters. If the token used to access this endpoint, does not have the PARTNER_ADMIN role, then it will fetch all the API keys created by all the partners associated with the logged in user only. If the token used to access this endpoint, has PARTNER_ADMIN role, then it will fetch all the API keys created by all the partners.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<PageResponseV2Dto<ApiKeyRequestSummaryDto>> getAllApiKeyRequests(
			@RequestParam(value = "sortFieldName", required = false) String sortFieldName,
			@RequestParam(value = "sortType", required = false) String sortType,
			@RequestParam(value = "pageNo",  required = false) Integer pageNo,
			@RequestParam(value = "pageSize",  required = false) Integer pageSize,
			@RequestParam(value = "partnerId", required = false) String partnerId,
			@RequestParam(value = "apiKeyLabel", required = false) String apiKeyLabel,
			@RequestParam(value = "orgName", required = false) String orgName,
			@Parameter(
					description = "Status of request",
					in = ParameterIn.QUERY,
					schema = @Schema(allowableValues = {"activated", "deactivated"})
			)
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "policyName", required = false) String policyName,
			@RequestParam(value = "policyGroupName", required = false) String policyGroupName
	) {
		partnerHelper.validateRequestParameters(partnerHelper.apiKeyAliasToColumnMap, sortFieldName, sortType, pageNo, pageSize);
		ApiKeyFilterDto filterDto = new ApiKeyFilterDto();
		if (partnerId != null) {
			filterDto.setPartnerId(partnerId.toLowerCase());
		}
		if (apiKeyLabel != null) {
			filterDto.setApiKeyLabel(apiKeyLabel.toLowerCase());
		}
		if (orgName != null) {
			filterDto.setOrgName(orgName.toLowerCase());
		}
		if (status != null) {
			filterDto.setStatus(status);
		}
		if (policyName != null) {
			filterDto.setPolicyName(policyName.toLowerCase());
		}
		if (policyGroupName != null) {
			filterDto.setPolicyGroupName(policyGroupName.toLowerCase());
		}
		return partnerManagementService.getAllApiKeyRequests(sortFieldName, sortType, pageNo, pageSize, filterDto);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGettrustcertificates())")
	@GetMapping(value = "/trust-chain-certificates")
	@Operation(summary = "This endpoint retrieves a list of all the Trust Certificates uploaded by the Partner Admin.",
	description = "Available since release-1.2.2.0. This endpoint supports pagination, sorting, and filtering. It is configured for the role PARTNER_ADMIN.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<PageResponseV2Dto<TrustCertificateSummaryDto>> getTrustCertificates(
			@RequestParam(value = "sortFieldName", required = false) String sortFieldName,
			@RequestParam(value = "sortType", required = false) String sortType, // e.g., ASC or DESC
			@RequestParam(value = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(value = "pageSize", defaultValue = "8") Integer pageSize,
			@Parameter(
					description = "Type of CA certificate",
					in = ParameterIn.QUERY,
					schema = @Schema(allowableValues = {"ROOT", "INTERMEDIATE"})
			)
			@RequestParam(value = "caCertificateType", required = false) String caCertificateType,
			@RequestParam(value = "certificateId", required = false) String certificateId,
			@Parameter(
					description = "Type of partner domain",
					in = ParameterIn.QUERY,
					schema = @Schema(allowableValues = {"FTM", "DEVICE", "AUTH"})
			)
			@RequestParam(value = "partnerDomain", required = false) String partnerDomain,
			@RequestParam(value = "issuedTo", required = false) String issuedTo,
			@RequestParam(value = "issuedBy", required = false) String issuedBy
	) {
		featureAvailabilityUtil.validateRootAndIntermediateCertificatesFeatureEnabled();
		partnerHelper.validateRequestParameters(partnerHelper.trustCertificateAliasToColumnMap, sortFieldName, sortType, pageNo, pageSize);
		TrustCertificateFilterDto filterDto = new TrustCertificateFilterDto();
		if (caCertificateType != null) {
			filterDto.setCaCertificateType(caCertificateType);
		}
		if (certificateId != null) {
			filterDto.setCertificateId(certificateId);
		}
		if (partnerDomain != null) {
			filterDto.setPartnerDomain(partnerDomain);
		}
		if (issuedTo != null) {
			filterDto.setIssuedTo(issuedTo);
		}
		if (issuedBy != null) {
			filterDto.setIssuedBy(issuedBy);
		}
		return partnerManagementService.getTrustCertificates(sortFieldName, sortType, pageNo, pageSize, filterDto);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetdownloadtrustcertificates())")
	@GetMapping(value = "/trust-chain-certificates/{certificateId}/certificateFile")
	@Operation(summary = "This endpoint will download p7b file for a CA / Intermediate CA certificate along with the trust chain based on Certificate Id.",
			description = "Available since release-1.2.2.0. It is configured for the role PARTNER_ADMIN.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	ResponseWrapperV2<TrustCertificateResponseDto> downloadTrustCertificates(
			@ApiParam("To download trust certificates.")  @PathVariable("certificateId") @NotNull String certificateId) {
		featureAvailabilityUtil.validateRootAndIntermediateCertificatesFeatureEnabled();
		return partnerManagementService.downloadTrustCertificates(certificateId);
	}
}
