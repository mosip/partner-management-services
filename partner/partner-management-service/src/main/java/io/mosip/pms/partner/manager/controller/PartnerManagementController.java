package io.mosip.pms.partner.manager.controller;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.partner.manager.dto.CaCertificateFilterDto;
import io.mosip.pms.partner.manager.dto.*;
import io.mosip.pms.partner.manager.dto.CaCertificateSummaryDto;
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
@RequestMapping(value = "/partners")
@Api(tags = { "Partner Management Controller" })
public class PartnerManagementController {
	
	@Autowired
	PartnerManagerService partnerManagementService;
	
	@Autowired
	AuditUtil auditUtil;

	@Autowired
	PartnerHelper partnerHelper;
	
	String msg = "mosip.partnermanagement.partners.retrieve";
	String version = "1.0";


	/**
	 * This API would be used by partner Manager, to update Partner api key to Policy Mappings.
	 * @param request this class contain oldPolicyID and newPolicyID
	 * @param partnerId this is unique id created after self registered by partner
	 * @param partnerApiKey this is unique id created by partner manager at the time of approving partner request
	 * @return response this class contains massage about API key created successfully
	 */
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPutpartnersapikeypolicies())")
	@RequestMapping(value = "/{partnerId}/apikey/{apikey}/policies", method = RequestMethod.PUT)
	@Operation(summary = "Service to update policies against to api key", description = "Service to update policies against to api key")
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
	 * This API would be used to activate/deactivate Auth/E-KYC Partners
	 * @param partnerId this is unique id created after self registered by partner
	 * @param request this class contains the status of activate/deactivate Auth/E-KYC Partners
	 * @return respons this class contains massage about Partner status updated successfully
	 */
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPatchpartners())")
	@RequestMapping(value = "/{partnerId}", method = RequestMethod.PATCH)
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
	
	/** 
	 * This API would be used to retrieve all Auth/E-KYC Partners for the policy group.
	 * @return response this class contains list of Auth/E-KYC Partners for the policy group
	 */
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpartners())")
	@RequestMapping(method = RequestMethod.GET)
	@Operation(summary = "Service to get partner details", description = "Service to get partner details")
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
	
	/** 
	 * This API would be used to retrieve all Auth/E-KYC Partners for the policy group.
	 * @return response this class contains list of Auth/E-KYC Partners for the policy group
	 */
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpartnersnew())")
	@RequestMapping(value = "/v2", method = RequestMethod.GET)
	@Operation(summary = "Service to get partner details", description = "Service to get partners details")
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
	@RequestMapping(value = "/{partnerId}/apikey/{apikey}" , method = RequestMethod.GET)
	@Operation(summary = "Service to get policy for given api key", description = "Service to get policy for given api key")
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
	
	/**
	 * This API would be used to retrieve all Partner API Key requests as received by partner manager
	 * @return response this class contains all Partner API Key requests as received by partner manager
	 */
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpartnersapikey())")
	@RequestMapping(value = "/apikey" , method = RequestMethod.GET)
	@Operation(summary = "Service to get api key requests", description = "Service to get api key requests")
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
	 * This API would be used to retrieve the request for Partner API key to Policy Mappings for given request id.
	 * @param apiKeyReqId this is unique id created after partner request for Partner API Key
	 * @return response this class contains details related to Partner API key to Policy Mappings
	 */
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpartnersapikey())")
	@RequestMapping(value = "/apikey/{apikey}" , method = RequestMethod.GET)
	@Operation(summary = "Service to get api key request", description = "Service to get api key request")
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
	@RequestMapping(value = "/policy/{mappingkey}", method = RequestMethod.PUT)
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
	@RequestMapping(value = "/{partnerId}/policy/{policyId}/apiKey/status", method = RequestMethod.PATCH)
	@Operation(summary = "Service to activate/de-activate partner api key", description = "Service to activate/de-activate partner api key")
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
	@GetMapping(value = "/{partnerId}/v2")
	@Operation(summary = "Get Partner details.", description = "This endpoint will fetch partner details for the provided partner Id.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<PartnerDetailsV3Dto> getPartnerDetails(@PathVariable String partnerId) {
		return partnerManagementService.getPartnerDetails(partnerId);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetallpartners())")
	@GetMapping(value = "/v3")
	@Operation(summary = "Get all partner details", description = "This endpoint will fetch a list of all the partner details")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<PageResponseV2Dto<PartnerSummaryDto>> getAllPartners(
			@RequestParam(value = "sortFieldName", required = false) String sortFieldName,
			@RequestParam(value = "sortType", required = false) String sortType, // e.g., ASC or DESC
			@RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
			@RequestParam(value = "pageSize", defaultValue = "8") int pageSize,
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
		return partnerManagementService.getAllPartners(sortFieldName, sortType, pageNo, pageSize, partnerFilterDto);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetallpartnerpolicymappingrequests())")
	@GetMapping(value = "/partner-policy-requests")
	@Operation(summary = "Get all partner policy mapping requests", description = "This endpoint will fetch a list of all the partner policy mapping requests")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<PageResponseV2Dto<PartnerPolicyRequestSummaryDto>> getAllPartnerPolicyRequests(
			@RequestParam(value = "sortFieldName", required = false) String sortFieldName,
			@RequestParam(value = "sortType", required = false) String sortType,
			@RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
			@RequestParam(value = "pageSize", defaultValue = "8") int pageSize,
			@RequestParam(value = "partnerId", required = false) String partnerId,
			@RequestParam(value = "requestDetails", required = false) String requestDetails,
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
		if (requestDetails != null) {
			filterDto.setRequestDetails(requestDetails.toLowerCase());
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
	@GetMapping(value = "/apikey/search/v2")
	@Operation(summary = "Get all api key requests", description = "This endpoint will fetch a list of all api key requests")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<PageResponseV2Dto<ApiKeyRequestSummaryDto>> getAllApiKeyRequests(
			@RequestParam(value = "sortFieldName", required = false) String sortFieldName,
			@RequestParam(value = "sortType", required = false) String sortType,
			@RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
			@RequestParam(value = "pageSize", defaultValue = "8") int pageSize,
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

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetallcacertificates())")
	@GetMapping(value = "/root-certificates")
	@Operation(summary = "Get all root certificate details", description = "This endpoint will fetch a list of all the root certificate details")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapperV2<PageResponseV2Dto<CaCertificateSummaryDto>> getCaCertificates(
			@RequestParam(value = "sortFieldName", required = false) String sortFieldName,
			@RequestParam(value = "sortType", required = false) String sortType, // e.g., ASC or DESC
			@RequestParam(value = "pageNo", defaultValue = "0") int pageNo,
			@RequestParam(value = "pageSize", defaultValue = "8") int pageSize,
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
		partnerHelper.validateRequestParameters(partnerHelper.caCertificateAliasToColumnMap, sortFieldName, sortType, pageNo, pageSize);
		CaCertificateFilterDto filterDto = new CaCertificateFilterDto();
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
		return partnerManagementService.getCaCertificates(sortFieldName, sortType, pageNo, pageSize, filterDto);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetdownloadrootcertificate())")
	@GetMapping(value = "/download-root-certificate/{certificateId}")
	@Operation(summary = "Download root certificate", description = "This endpoint will download p7b file for a CA / Intermediate CA certificate along with the trust chain.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true)))
	})
	ResponseWrapperV2<CACertificateResponseDto> downloadRootCertificate(
			@ApiParam("To download root certificate.")  @PathVariable("certificateId") @NotNull String certificateId) {
		return partnerManagementService.downloadRootCertificate(certificateId);
	}
}
