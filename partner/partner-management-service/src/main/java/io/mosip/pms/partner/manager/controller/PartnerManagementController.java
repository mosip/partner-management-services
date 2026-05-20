package io.mosip.pms.partner.manager.controller;

import java.util.List;
import java.util.Optional;

import io.mosip.pms.common.dto.TrustCertificateSummaryDto;
import io.mosip.pms.common.request.dto.RequestWrapperV2;
import io.mosip.pms.common.util.RequestValidator;
import io.mosip.pms.common.validator.InputValidator;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto;
import io.mosip.pms.partner.request.dto.BioExtractorsRequestDto;
import io.mosip.pms.partner.response.dto.APIKeyUpdateResponseDto;
import io.mosip.pms.partner.request.dto.BioextractorConfigurationDeleteRequestDto;
import io.mosip.pms.partner.request.dto.BioextractorConfigurationRequestDto;
import io.mosip.pms.partner.response.dto.BioextractorConfigurationDetailDto;
import io.mosip.pms.partner.response.dto.BioextractorConfigurationResponseDto;
import io.mosip.pms.partner.request.dto.LinkPolicyGroupRequestDto;
import io.mosip.pms.partner.request.dto.LinkPolicyGroupResponseDto;
import io.mosip.pms.partner.util.FeatureAvailabilityUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.common.constant.ValidationErrorCode;
import io.mosip.pms.partner.manager.dto.*;
import io.mosip.pms.partner.util.PartnerHelper;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.partner.manager.constant.PartnerManageEnum;
import io.mosip.pms.partner.manager.service.PartnerManagerService;
import io.mosip.pms.partner.request.dto.CredentialTypeRequestDto;
import io.mosip.pms.partner.request.dto.APIkeyStatusUpdateRequestDto;
import io.mosip.pms.partner.response.dto.BioExtractorsResponseWrapperV2;
import io.mosip.pms.partner.response.dto.CredentialTypesResponseWrapperV2;
import io.mosip.pms.partner.service.PartnerService;
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
	PartnerService partnerService;
	
	@Autowired
	AuditUtil auditUtil;

	@Autowired
	PartnerHelper partnerHelper;

	@Autowired
	RequestValidator requestValidator;

	@Autowired
	FeatureAvailabilityUtil featureAvailabilityUtil;

	@Autowired
	private InputValidator inputValidator;

	@Value("${mosip.pms.certificate.id.regex}")
	private String certificateIdRegex;

	@Value("${mosip.pms.api.id.partners.bioextractors.request.post}")
	private String postPartnerBioextractorsRequestId;

	@Value("${mosip.pms.api.id.link.policy.group.post}")
	private String postLinkPolicyGroup;

    @Value("${mosip.pms.api.id.update.api.key.patch}")
    private String patchUpdateApiKey;

	@Value("${mosip.pms.api.id.bioextractor.configurations.post}")
	private String postBioextractorConfigurationsId;
	
	@Value("${mosip.pms.api.id.bioextractor.configuration.delete.patch}")
	private String patchDeleteBioextractorConfigurationId;

	@Value("${mosip.pms.api.id.partners.credentialtypes.request.post}")
	private String postPartnerCredentialTypesRequestId;

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
	@PutMapping(value = "/partners/policy/{mappingkey}")
	@Operation(summary = "Service to approve/reject partner policy mapping", description = "Service to approve/reject partner policy mapping")
	public ResponseEntity<ResponseWrapper<String>> approveRejectPolicyMappings(
			@RequestBody @Valid RequestWrapper<StatusRequestDto> request,
			@PathVariable String mappingkey){
		requestValidator.validateReqTime(request.getRequesttime());
		ResponseWrapper<String> response = new ResponseWrapper<>();		
		response.setId(request.getId());
		response.setVersion(request.getVersion());		
		auditUtil.setAuditRequestDto(PartnerManageEnum.APPROVE_REJECT_PARTNER_API);
		response.setResponse(partnerManagementService.approveRejectPartnerPolicyMapping(mappingkey, request.getRequest()));		
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	/*
	 * This endpoint has been deprecated since the release-1.3.0-beta.4
	 * It has been replaced by the new PATCH /partners/{partnerId}/policies/{policyId}/api-keys/{apiKeyName} endpoint.
	 * Please use the new endpoint for all future requests.
	 */
	@Deprecated(since = "release-1.3.0-beta.4")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPatchpartnerspolicyapikeystatus())")
	@PatchMapping(value = "/partners/{partnerId}/policy/{policyId}/apiKey/status")
	@Operation(summary = "Service to activate/de-activate partner API key - deprecated since release-1.3.0-beta.4",
			description = "This endpoint has been deprecated since the release-1.3.0-beta.4 and replaced by the PATCH /partners/{partnerId}/policies/{policyId}/api-keys/{apiKeyName} endpoint")
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
	description = "Available since release-1.2.2.0. This endpoint upgrades the earlier GET endpoint /partners/{partnerId} by adding new features like Policy Group Details in Response and Certificate Details in Response. It is configured for the role PARTNER_ADMIN.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<PartnerDetailsV3Dto> getPartnerDetails(@PathVariable String partnerId) {
		inputValidator.validateRequestInput("partnerId", partnerId);
		return partnerManagementService.getPartnerDetails(partnerId);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetadminpartners())")
	@GetMapping(value = "/admin-partners")
	@Operation(summary = "This endpoint retrieves a list of all Partners.",
			description = "Available since release-1.2.2.0. This endpoint upgrades the earlier GET endpoints /partners and /partners/v2 by adding new features like pagination, sorting, and filtering. It is configured for the role PARTNER_ADMIN.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<PageResponseV2Dto<PartnerSummaryDto>> getAdminPartners(
			@RequestParam(value = "sortFieldName", required = false) String sortFieldName,
			@RequestParam(value = "sortType", required = false) String sortType, // e.g., ASC or DESC
			@RequestParam(value = "pageNo", defaultValue = "0") String pageNo,
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
            @Parameter(
					description = "Approval status of partner",
					in = ParameterIn.QUERY,
					schema = @Schema(allowableValues = {"active", "deactivated", "inactive"})
			)   
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "policyGroupName", required = false) String policyGroupName
	) {
		inputValidator.validateRequestInput("sortFieldName", sortFieldName);
		inputValidator.validateRequestInput("sortType", sortType);
		inputValidator.validateRequestInput("partnerId", partnerId);
		inputValidator.validateRequestInput("partnerType", partnerType);
		inputValidator.validateRequestInput("orgName", orgName);
		inputValidator.validateRequestInput("emailAddress", emailAddress);
		inputValidator.validateRequestInput("certificateUploadStatus", certificateUploadStatus);
		inputValidator.validateRequestInput("policyGroupName", policyGroupName);
		inputValidator.validateRequestInput("status", status);
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
		if (status != null) {
			partnerFilterDto.setStatus(status);
		}
		return partnerManagementService.getAdminPartners(sortFieldName, sortType,
				partnerHelper.parsePageNo(pageNo),
				pageSize, partnerFilterDto);
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
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "pageSize", required = false) Integer pageSize,
			@RequestParam(value = "partnerId", required = false) String partnerId,
			@Parameter(
					description = "Search type for partnerId. Applicable only if 'partnerId' is provided. Default is 'contains'.",
					in = ParameterIn.QUERY,
					schema = @Schema(allowableValues = {"equals"})
			)
			@RequestParam(value = "partnerIdSearchType", required = false, defaultValue = "contains") String partnerIdSearchType,
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
		inputValidator.validateRequestInput("sortFieldName", sortFieldName);
		inputValidator.validateRequestInput("sortType", sortType);
		inputValidator.validateRequestInput("partnerId", partnerId);
		inputValidator.validateRequestInput("partnerIdSearchType", partnerIdSearchType);
		inputValidator.validateRequestInput("partnerComment", partnerComment);
		inputValidator.validateRequestInput("orgName", orgName);
		inputValidator.validateRequestInput("status", status);
		inputValidator.validateRequestInput("policyId", policyId);
		inputValidator.validateRequestInput("policyName", policyName);
		inputValidator.validateRequestInput("policyGroupName", policyGroupName);
		inputValidator.validateRequestInput("partnerType", partnerType);
		PartnerPolicyRequestFilterDto filterDto = new PartnerPolicyRequestFilterDto();
		boolean isEqualSearch = false;
		if (partnerIdSearchType != null) {
			filterDto.setPartnerIdSearchType(partnerIdSearchType.toLowerCase());
			if (partnerIdSearchType.equals("equals")) {
				isEqualSearch = true;
			}
		}
		if (partnerId != null) {
			if (isEqualSearch) {
				filterDto.setPartnerId(partnerId);
			} else {
				filterDto.setPartnerId(partnerId.toLowerCase());
			}
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
		return partnerManagementService.getAllPartnerPolicyRequests(sortFieldName, sortType,
				partnerHelper.parsePageNo(pageNo),
				pageSize, filterDto);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostpartnersbioextractors())")
	@PostMapping(value = "/partner-policy-requests/{requestId}/bio-extractors-request")
	@Operation(summary = "Service to submit bio extractors request", description = "Persists bio extractor requests against an in-progress partner policy mapping request")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<String> submitBioExtractorsRequest(
			@PathVariable("requestId") String requestId,
			@RequestBody @Valid RequestWrapperV2<BioExtractorsRequestDto> requestWrapper) {
		Optional<ResponseWrapperV2<String>> validationResponse =
				requestValidator.validate(postPartnerBioextractorsRequestId, requestWrapper);
		if (validationResponse.isPresent()) {
			return validationResponse.get();
		}
		inputValidator.validateRequestInput("requestId", requestId);
		requestWrapper.getRequest().getExtractors().forEach(extractor -> {
			inputValidator.validateRequestInput("attributeName", extractor.getAttributeName());
			inputValidator.validateRequestInput("biometric", extractor.getBiometric());
			inputValidator.validateRequestInput("biometricSubTypes", extractor.getBiometricSubTypes());
			inputValidator.validateRequestInput("extractorProvider", extractor.getExtractorProvider());
			inputValidator.validateRequestInput("extractorProviderVersion", extractor.getExtractorProviderVersion());
		});
		ResponseWrapperV2<String> response = new ResponseWrapperV2<>();
		response.setResponse(partnerManagementService.submitBioExtractorsRequest(requestId, requestWrapper.getRequest()));
		response.setId(requestWrapper.getId());
		response.setVersion(requestWrapper.getVersion());
		return response;
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpartnersbioextractors())")
	@GetMapping(value = "/partner-policy-requests/{requestId}/bio-extractors-request")
	@Operation(summary = "Get bio-extractor requests for a partner-policy request",
			description = "Fetches all bio-extractor request rows (non-deleted) submitted against the given partner policy mapping request id.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public BioExtractorsResponseWrapperV2 getPartnerPolicyRequestBioExtractors(
			@PathVariable("requestId") String requestId) {
		inputValidator.validateRequestInput("requestId", requestId);
		return partnerManagementService.getPartnerPolicyRequestBioExtractors(requestId);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpartnersbioextractors())")
	@GetMapping(value = "/partner-policy-requests/{requestId}/credential-types-request")
	@Operation(summary = "Get credential type request for a partner-policy request",
			description = "Fetches the credential type request row (if any) submitted against the given partner policy mapping request id (`req_id`).")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public CredentialTypesResponseWrapperV2 getPartnerPolicyRequestCredentialTypes(
			@PathVariable("requestId") String requestId) {
		inputValidator.validateRequestInput("requestId", requestId);
		return partnerManagementService.getPartnerPolicyRequestCredentialTypes(requestId);
	}
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostpartnersbioextractors())")
	@PostMapping(value = "/partner-policy-requests/{requestId}/credential-types-request")
	@Operation(summary = "Service to submit credential types request",
			description = "Persists credential type request against an in-progress partner policy mapping request")
	public ResponseWrapperV2<String> submitCredentialTypesRequest(
			@PathVariable("requestId") String requestId,
			@RequestBody @Valid RequestWrapperV2<CredentialTypeRequestDto> requestWrapper) {
		Optional<ResponseWrapperV2<String>> validationResponse =
				requestValidator.validate(postPartnerCredentialTypesRequestId, requestWrapper);
		if (validationResponse.isPresent()) {
			return validationResponse.get();
		}
		inputValidator.validateRequestInput("requestId", requestId);
		requestWrapper.getRequest().setPartnerPolicyRequestId(requestId);
		ResponseWrapperV2<String> response = new ResponseWrapperV2<>();
		response.setResponse(partnerService.submitCredentialTypesRequest(requestWrapper.getRequest()));
		response.setId(requestWrapper.getId());
		response.setVersion(requestWrapper.getVersion());
		return response;
	}

	@Deprecated(since = "release-1.3.0-beta.2")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpartnersapikeyrequests())")
	@GetMapping(value = "/partner-api-keys")
	@Operation(summary = "This endpoint retrieves a list of all the API keys created by the Auth Partners - deprecated since the release-1.3.0-beta.2",
	description = "This endpoint has been deprecated since the release-1.3.0-beta.2 and replaced by the GET /partner-api-keys/v2 endpoint")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<PageResponseV2Dto<ApiKeyRequestSummaryDto>> getAllApiKeyRequests(
			@RequestParam(value = "sortFieldName", required = false) String sortFieldName,
			@RequestParam(value = "sortType", required = false) String sortType,
			@RequestParam(value = "pageNo",  required = false) String pageNo,
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
		inputValidator.validateRequestInput("sortFieldName", sortFieldName);
		inputValidator.validateRequestInput("sortType", sortType);
		inputValidator.validateRequestInput("partnerId", partnerId);
		inputValidator.validateRequestInput("apiKeyLabel", apiKeyLabel);
		inputValidator.validateRequestInput("orgName", orgName);
		inputValidator.validateRequestInput("status", status);
		inputValidator.validateRequestInput("policyName", policyName);
		inputValidator.validateRequestInput("policyGroupName", policyGroupName);
		ApiKeyFilterDto filterDto = populateApiKeyFilterDto(partnerId, apiKeyLabel, orgName, status, policyName, policyGroupName, null, null);
		return partnerManagementService.getAllApiKeyRequests(sortFieldName, sortType,
				partnerHelper.parsePageNo(pageNo),
				pageSize, filterDto);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpartnersapikeyrequests())")
	@GetMapping(value = "/partner-api-keys/v2")
	@Operation(summary = "This endpoint retrieves a list of all the API keys created by the Auth Partners.",
			description = "Available since release-1.3.0-beta.1. This endpoint supports pagination, sorting, and and filtering based on optional query parameters. If the token used to access this endpoint, does not have the PARTNER_ADMIN role, then it will fetch all the API keys created by all the partners associated with the logged in user only. If the token used to access this endpoint, has PARTNER_ADMIN role, then it will fetch all the API keys created by all the partners.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<PageResponseV2Dto<ApiKeyRequestSummaryV2Dto>> getAllApiKeyRequestsV2(
			@RequestParam(value = "sortFieldName", required = false) String sortFieldName,
			@RequestParam(value = "sortType", required = false) String sortType,
			@RequestParam(value = "pageNo",  required = false) String pageNo,
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
			@RequestParam(value = "policyGroupName", required = false) String policyGroupName,
			@Parameter(description = "Filter by partner type (e.g. Auth_Partner, Manual_Adjudication)", in = ParameterIn.QUERY)
			@RequestParam(value = "partnerType", required = false) String partnerType,
			@RequestParam(value = "expiryPeriod", required = false)
			@Min(value = 1, message = "Expiry period must be at least 1 day.")
			@Max(value = 30, message = "Expiry period cannot be more than 30 days.")
			Integer expiryPeriod
	) {
		inputValidator.validateRequestInput("sortFieldName", sortFieldName);
		inputValidator.validateRequestInput("sortType", sortType);
		inputValidator.validateRequestInput("partnerId", partnerId);
		inputValidator.validateRequestInput("apiKeyLabel", apiKeyLabel);
		inputValidator.validateRequestInput("orgName", orgName);
		inputValidator.validateRequestInput("status", status);
		inputValidator.validateRequestInput("policyName", policyName);
		inputValidator.validateRequestInput("policyGroupName", policyGroupName);
		inputValidator.validateRequestInput("partnerType", partnerType);
		ApiKeyFilterDto filterDto = populateApiKeyFilterDto(partnerId, apiKeyLabel, orgName, status, policyName, policyGroupName, partnerType, expiryPeriod);
		return partnerManagementService.getAllApiKeyRequestsV2(sortFieldName, sortType,
				partnerHelper.parsePageNo(pageNo),
				pageSize, filterDto);
	}

	private ApiKeyFilterDto populateApiKeyFilterDto(String partnerId, String apiKeyLabel, String orgName, String status,
													String policyName, String policyGroupName, String partnerType, Integer expiryPeriod) {
		ApiKeyFilterDto filterDto = new ApiKeyFilterDto();
		if (partnerId != null) filterDto.setPartnerId(partnerId.toLowerCase());
		if (apiKeyLabel != null) filterDto.setApiKeyLabel(apiKeyLabel.toLowerCase());
		if (orgName != null) filterDto.setOrgName(orgName.toLowerCase());
		if (status != null) filterDto.setStatus(status);
		if (policyName != null) filterDto.setPolicyName(policyName.toLowerCase());
		if (policyGroupName != null) filterDto.setPolicyGroupName(policyGroupName.toLowerCase());
		if (partnerType != null && !partnerType.isBlank()) filterDto.setPartnerType(partnerType.toLowerCase());
		if (expiryPeriod != null) filterDto.setExpiryPeriod(expiryPeriod);
		return filterDto;
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
			@RequestParam(value = "pageNo", defaultValue = "0") String pageNo,
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
			@RequestParam(value = "issuedBy", required = false) String issuedBy,
			@RequestParam(value = "expiryPeriod", required = false)
			@Min(value = 1, message = "Expiry period must be at least 1 day.")
			@Max(value = 30, message = "Expiry period cannot be more than 30 days.")
			Integer expiryPeriod) {
		featureAvailabilityUtil.validateRootAndIntermediateCertificatesFeatureEnabled();
		inputValidator.validateRequestInput("sortFieldName", sortFieldName);
		inputValidator.validateRequestInput("sortType", sortType);
		inputValidator.validateRequestInput("caCertificateType", caCertificateType);
		inputValidator.validateRequestInput("certificateId", certificateId);
		inputValidator.validateRequestInput("partnerDomain", partnerDomain);
		inputValidator.validateRequestInput("issuedBy", issuedBy);
		inputValidator.validateRequestInput("issuedTo", issuedTo);
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
		if (expiryPeriod != null){
			filterDto.setExpiryPeriod(expiryPeriod);
		}
		return partnerManagementService.getTrustCertificates(sortFieldName, sortType,
				partnerHelper.parsePageNo(pageNo),
				pageSize, filterDto);
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
		if (!certificateId.matches(certificateIdRegex)) {
			throw new PartnerServiceException(
					ErrorCode.INVALID_INPUT_FORMAT.getErrorCode(),
					String.format(
							ErrorCode.INVALID_INPUT_FORMAT.getErrorMessage(),
							"certificateId",
							"Only alphanumeric characters (A–Z, a–z, 0–9) and hyphens (-) are allowed with a maximum length of 36 characters"
					)
			);
		}
		return partnerManagementService.downloadTrustCertificates(certificateId);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostlinkpolicygrouptopartner())")
	@PostMapping(value = "/{partnerId}/policy-group")
	@Operation(summary = "This endpoint is used for linking a policy group to a partner.",
			description = "Available since release-1.3.0-beta.3. This endpoint is available for users with the PARTNER_ADMIN role. It is used to link a policy group to a partner with the MISP_PARTNER role, if not already linked.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Policy group linked successfully"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<LinkPolicyGroupResponseDto> linkPolicyGroup(
			@PathVariable("partnerId") String partnerId,
			@RequestBody @Valid RequestWrapperV2<LinkPolicyGroupRequestDto> requestWrapper) {
		Optional<ResponseWrapperV2<LinkPolicyGroupResponseDto>> validationResponse =
				requestValidator.validate(postLinkPolicyGroup, requestWrapper);
		if (validationResponse.isPresent()) {
			return validationResponse.get();
		}
		return partnerManagementService.linkPolicyGroup(partnerId, requestWrapper.getRequest());
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getPatchupdatepartnerapikey())")
	@PatchMapping(value = "/partners/{partnerId}/policies/{policyId}/api-keys/{apiKeyName}")
	@Operation(summary = "Service to deactivate API key and/or update expiry date",
            description = "This endpoint is used to deactivate an API key or update its expiry date and time.")
    @ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<APIKeyUpdateResponseDto> updateAPIKey(
			@PathVariable String partnerId,
			@PathVariable String policyId,
			@PathVariable String apiKeyName,
			@RequestBody @Valid RequestWrapperV2<APIKeyUpdateRequestDto> requestWrapper) {
		inputValidator.validateRequestInput("partnerId", partnerId);
		inputValidator.validateRequestInput("policyId", policyId);
		inputValidator.validateRequestInput("apiKeyName", apiKeyName);
        Optional<ResponseWrapperV2<APIKeyUpdateResponseDto>> validationResponse = requestValidator.validate(patchUpdateApiKey, requestWrapper);
        if (validationResponse.isPresent()) {
            return validationResponse.get();
        }
		return partnerManagementService.updateAPIKey(partnerId, policyId, apiKeyName, requestWrapper.getRequest());
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostbioextractorconfigurations())")
	@PostMapping(value = "/bio-extractor-configurations")
	@Operation(summary = "Create a new bio-extractor configuration",
			description = "Creates a new bio-extractor configuration. Config name must be unique. Available for PARTNER_ADMIN role.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<BioextractorConfigurationResponseDto> createBioextractorConfiguration(
			@RequestBody @Valid RequestWrapperV2<BioextractorConfigurationRequestDto> requestWrapper) {
		Optional<ResponseWrapperV2<BioextractorConfigurationResponseDto>> validationResponse =
				requestValidator.validate(postBioextractorConfigurationsId, requestWrapper);
		if (validationResponse.isPresent()) {
			return validationResponse.get();
		}
		BioextractorConfigurationRequestDto request = requestWrapper.getRequest();
		inputValidator.validateRequestInput("configName", request.getConfigName());
		inputValidator.validateRequestInput("bioextractorProviderName", request.getBioextractorProviderName());
		inputValidator.validateRequestInput("bioextractorProviderVersion", request.getBioextractorProviderVersion());
		return partnerManagementService.createBioextractorConfiguration(request);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetbioextractorconfigurations())")
	@GetMapping(value = "/bio-extractor-configurations")
	@Operation(summary = "Get all bio-extractor configurations",
			description = "Fetches all bio-extractor configurations available in the database. Available for PARTNER_ADMIN role.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<PageResponseV2Dto<BioextractorConfigurationDetailDto>> getBioextractorConfigurations(
			@RequestParam(value = "sortFieldName", required = false) String sortFieldName,
			@RequestParam(value = "sortType", required = false) String sortType,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "pageSize", required = false) Integer pageSize,
			@RequestParam(value = "configName", required = false) String configName,
			@RequestParam(value = "bioextractorProviderName", required = false) String bioextractorProviderName,
			@RequestParam(value = "bioextractorProviderVersion", required = false) String bioextractorProviderVersion,
			@RequestParam(value = "bioModality", required = false) String bioModality
	) {
		Integer normalizedPageNo = partnerHelper.parsePageNo(pageNo);
		BioextractorConfigurationFilterDto filterDto = populateBioextractorConfigurationFilterDto(
				sortFieldName, sortType, normalizedPageNo, pageSize, configName, bioextractorProviderName,
				bioextractorProviderVersion, bioModality);
		return partnerManagementService.getBioextractorConfigurations(
				sortFieldName, sortType, normalizedPageNo, pageSize, filterDto);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetbioextractorconfigurationdetails())")
	@GetMapping(value = "/bio-extractor-configurations/{bioExtractorConfigurationId}")
	@Operation(summary = "Get bio-extractor configuration details by ID",
			description = "Fetches bio-extractor configuration details for the given configuration ID. Available for PARTNER_ADMIN role.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<BioextractorConfigurationDetailDto> getBioextractorConfigurationById(
			@PathVariable("bioExtractorConfigurationId") String bioExtractorConfigurationId) {
		inputValidator.validateRequestInput("bioExtractorConfigurationId", bioExtractorConfigurationId);
		return partnerManagementService.getBioextractorConfigurationById(bioExtractorConfigurationId);
	}
	
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPatchbioextractorconfigurationdelete())")
	@PatchMapping(value = "/bio-extractor-configurations/{bioExtractorConfigurationId}")
	@Operation(summary = "Delete bio-extractor configuration ",
			description = "Deletes the bio-extractor configuration. Available for PARTNER_ADMIN role.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<BioextractorConfigurationResponseDto> deleteBioextractorConfiguration(
			@PathVariable("bioExtractorConfigurationId") String bioExtractorConfigurationId,
			@RequestBody @Valid RequestWrapperV2<BioextractorConfigurationDeleteRequestDto> requestWrapper) {
		if (requestWrapper == null) {
			ResponseWrapperV2<BioextractorConfigurationResponseDto> responseWrapper = new ResponseWrapperV2<>();
			responseWrapper.setId(patchDeleteBioextractorConfigurationId);
			responseWrapper.setVersion(RequestValidator.VERSION);
			responseWrapper.setErrors(RequestValidator.setErrorResponse(
					ValidationErrorCode.INVALID_REQUEST_BODY.getErrorCode(),
					ValidationErrorCode.INVALID_REQUEST_BODY.getErrorMessage()));
			return responseWrapper;
		}
		Optional<ResponseWrapperV2<BioextractorConfigurationResponseDto>> validationResponse =
				requestValidator.validate(patchDeleteBioextractorConfigurationId, requestWrapper);
		if (validationResponse.isPresent()) {
			return validationResponse.get();
		}
		if (requestWrapper.getRequest() == null) {
			ResponseWrapperV2<BioextractorConfigurationResponseDto> responseWrapper = new ResponseWrapperV2<>();
			responseWrapper.setId(patchDeleteBioextractorConfigurationId);
			responseWrapper.setVersion(RequestValidator.VERSION);
			responseWrapper.setErrors(RequestValidator.setErrorResponse(
					ValidationErrorCode.INVALID_REQUEST_BODY.getErrorCode(),
					ValidationErrorCode.INVALID_REQUEST_BODY.getErrorMessage()));
			return responseWrapper;
		}
		inputValidator.validateRequestInput("bioExtractorConfigurationId", bioExtractorConfigurationId);
		inputValidator.validateRequestInput("status", requestWrapper.getRequest().getStatus());
		return partnerManagementService.deleteBioextractorConfiguration(bioExtractorConfigurationId, requestWrapper.getRequest());
	}

	private BioextractorConfigurationFilterDto populateBioextractorConfigurationFilterDto(
			String sortFieldName, String sortType, Integer pageNo, Integer pageSize,
			String configName, String bioextractorProviderName, String bioextractorProviderVersion,
			String bioModality) {
		inputValidator.validateRequestInput("sortFieldName", sortFieldName);
		inputValidator.validateRequestInput("sortType", sortType);
		inputValidator.validateRequestInput("configName", configName);
		inputValidator.validateRequestInput("bioextractorProviderName", bioextractorProviderName);
		inputValidator.validateRequestInput("bioextractorProviderVersion", bioextractorProviderVersion);


		BioextractorConfigurationFilterDto filterDto = new BioextractorConfigurationFilterDto();
		if (configName != null) filterDto.setConfigName(configName.toLowerCase());
		if (bioextractorProviderName != null) filterDto.setBioextractorProviderName(bioextractorProviderName.toLowerCase());
		if (bioextractorProviderVersion != null) filterDto.setBioextractorProviderVersion(bioextractorProviderVersion.toLowerCase());
		if (bioModality != null) filterDto.setBioModality(bioModality.toLowerCase());
		return filterDto;
	}
}
