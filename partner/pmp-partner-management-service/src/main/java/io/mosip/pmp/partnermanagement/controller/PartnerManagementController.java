package io.mosip.pmp.partnermanagement.controller;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import io.mosip.pmp.partnermanagement.constant.PartnerManageEnum;
import io.mosip.pmp.partnermanagement.core.RequestWrapper;
import io.mosip.pmp.partnermanagement.core.ResponseWrapper;
import io.mosip.pmp.partnermanagement.dto.ActivateDeactivatePartnerRequest;
import io.mosip.pmp.partnermanagement.dto.ApikeyRequests;
import io.mosip.pmp.partnermanagement.dto.PartnerAPIKeyRequestsResponse;
import io.mosip.pmp.partnermanagement.dto.PartnerAPIKeyToPolicyMappingsResponse;
import io.mosip.pmp.partnermanagement.dto.PartnerPolicyResponse;
import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingRequest;
import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingResponse;
import io.mosip.pmp.partnermanagement.dto.RetrievePartnerDetailsResponse;
import io.mosip.pmp.partnermanagement.dto.RetrievePartnersDetails;
import io.mosip.pmp.partnermanagement.service.PartnerManagementService;
import io.mosip.pmp.partnermanagement.util.AuditUtil;

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
@RequestMapping(value = "/pmpartners")
public class PartnerManagementController {

	private static final Logger LOGGER = LoggerFactory.getLogger(PartnerManagementController.class);
	
	@Autowired
	PartnerManagementService partnerManagementService;
	
	@Autowired
	AuditUtil auditUtil;
	
	String msg = "mosip.partnermanagement.partners.retrieve";
	String version = "1.0";
	

	/**
	 * This API would be used by partner Manager, to update Partner api key to Policy Mappings.
	 * @param request this class contain oldPolicyID and newPolicyID
	 * @param partnerId this is unique id created after self registered by partner
	 * @param partnerApiKey this is unique id created by partner manager at the time of approving partner request
	 * @return response this class contains massage about API key created successfully
	 */
	@PreAuthorize("hasAnyRole('PARTNERMANAGER','partnermanager')")
	@RequestMapping(value = "/{partnerId}/{partnerApiKey}", method = RequestMethod.PUT)
	public ResponseEntity<ResponseWrapper<PartnersPolicyMappingResponse>> partnerApiKeyToPolicyMappings(
			@RequestBody @Valid RequestWrapper<PartnersPolicyMappingRequest> request, 
			@PathVariable String partnerId,
			@PathVariable String partnerApiKey) {
		LOGGER.info("Requesting for updating the Policy Group");
		auditUtil.setAuditRequestDto(PartnerManageEnum.API_KEY_MAPPING);
		PartnersPolicyMappingRequest partnersPolicyMappingRequest = request.getRequest();
		ResponseWrapper<PartnersPolicyMappingResponse> response = new ResponseWrapper<>();
		PartnersPolicyMappingResponse partnersPolicyMappingResponse=null;
		response.setId(request.getId());
		response.setVersion(request.getVersion());
		partnersPolicyMappingResponse = partnerManagementService
				.partnerApiKeyPolicyMappings(partnersPolicyMappingRequest, partnerId, partnerApiKey);
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
	@PreAuthorize("hasAnyRole('PARTNERMANAGER','partnermanager')")
	@RequestMapping(value = "/{partnerId}", method = RequestMethod.PATCH)
	 public ResponseEntity<ResponseWrapper<PartnersPolicyMappingResponse>> activateDeactivateAuthEKYCPartners(
			 @PathVariable String partnerId,
			 @RequestBody @Valid RequestWrapper<ActivateDeactivatePartnerRequest> request){
		ResponseWrapper<PartnersPolicyMappingResponse> response = new ResponseWrapper<>();
		PartnersPolicyMappingResponse partnersPolicyMappingResponse = null;
		response.setId(request.getId());
		response.setVersion(request.getVersion());
		auditUtil.setAuditRequestDto(PartnerManageEnum.ACTIVATE_DEACTIVATE_KYC_PARTNERS);
		ActivateDeactivatePartnerRequest activateDeactivatePartnerRequest = request.getRequest();
		partnersPolicyMappingResponse = partnerManagementService
				.activateDeactivateAuthEKYCPartner(partnerId,activateDeactivatePartnerRequest);
		response.setResponse(partnersPolicyMappingResponse);
		auditUtil.setAuditRequestDto(PartnerManageEnum.ACTIVATE_DEACTIVATE_KYC_PARTNERS_SUCCESS);
		return new ResponseEntity<>(response, HttpStatus.OK);
	 }
	
	/**
	 * Partner Manager would be using this API to activate OR de-activate PartnerAPIKey for given partner.
	 * @param partnerId this is unique id created after self registered by partner
	 * @param request this class contains the status about activate OR de-activate PartnerAPIKey for given partner
	 * @param partnerApiKey this is unique id created by partner manager at the time of approving partner request
	 * @return response this class contains massage about Partner API Key status updated successfully
	 */
	@PreAuthorize("hasAnyRole('PARTNERMANAGER','partnermanager')")
	@RequestMapping(value = "/{partnerId}/{partnerApiKey}", method = RequestMethod.PATCH)
	public ResponseEntity<ResponseWrapper<PartnersPolicyMappingResponse>> activateDeactivatePartnerAPIKeyGivenPartner(
			@PathVariable String partnerId,
			@RequestBody @Valid RequestWrapper<ActivateDeactivatePartnerRequest> request,
			@PathVariable String partnerApiKey){
		ResponseWrapper<PartnersPolicyMappingResponse> response = new ResponseWrapper<>();
		PartnersPolicyMappingResponse partnersPolicyMappingResponse = null;
		response.setId(request.getId());
		response.setVersion(request.getVersion());
		auditUtil.setAuditRequestDto(PartnerManageEnum.ACTIVATE_DEACTIVATE_API_PARTNERS);
		ActivateDeactivatePartnerRequest activateDeactivatePartnerRequest = request.getRequest();
		partnersPolicyMappingResponse = partnerManagementService
				.activateDeactivatePartnerAPIKeyGivenPartner(partnerId,activateDeactivatePartnerRequest,partnerApiKey);
		response.setResponse(partnersPolicyMappingResponse);
		auditUtil.setAuditRequestDto(PartnerManageEnum.ACTIVATE_DEACTIVATE_API_PARTNERS_SUCCESS);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @param partnerId
	 * @param partner_api_key
	 * @param misp_license_key
	 * @return
	 */
	@PreAuthorize("hasAnyRole('PARTNERMANAGER','partnermanager','ID_AUTHENTICATION','CREDENTIAL_ISSUANCE')")
	@RequestMapping(value = "/validatePartnerMisp/partnerId/{partnerId}/partnerApiKey/{partner_api_key}/mispLicenseKey/{misp_license_key}", 
			method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<PartnerPolicyResponse>> validatePartnerAndMisp(
			@PathVariable String partnerId, @PathVariable String partner_api_key,
			@PathVariable String misp_license_key,@RequestParam(defaultValue = "false", name ="needPartnerCert") boolean needPartnerCert){
		ResponseWrapper<PartnerPolicyResponse> response = new ResponseWrapper<>();
		auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_POLICY_MAPPING);
		PartnerPolicyResponse responseFromService = partnerManagementService.getPartnerMappedPolicyFile(misp_license_key,partner_api_key,partnerId,needPartnerCert);
		response.setResponse(responseFromService);
		auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_POLICY_MAPPING_SUCCESS);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	/**
	 * Partner Manager would be using this API to approve OR reject partner API key
	 * requests based on API key request id. During approval process of the request
	 * unique PartnerAPI Key is generated in Partner Management module, which is
	 * mapped to requested policies. Partner API Key would be having default active
	 * status, expiry of which would configurable.
	 * 
	 * @param request this class contains the status about approve OR reject partner API key requests
	 * @param apiKeyReqId this is unique id created after partner request for Partner API Key
	 * @return response this class contains massage about PartnerAPIKey approved successfully
	 */
	@PreAuthorize("hasAnyRole('PARTNERMANAGER','partnermanager')")
	@RequestMapping(value = "/PartnerAPIKeyRequests/{apiKeyReqId}", method = RequestMethod.PATCH)
	public ResponseEntity<ResponseWrapper<PartnersPolicyMappingResponse>> approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(
			@RequestBody @Valid RequestWrapper<ActivateDeactivatePartnerRequest> request,
			@PathVariable String apiKeyReqId){
		ResponseWrapper<PartnersPolicyMappingResponse> response = new ResponseWrapper<>();
		PartnersPolicyMappingResponse partnersPolicyMappingResponse = null;
		response.setId(request.getId());
		response.setVersion(request.getVersion());
		ActivateDeactivatePartnerRequest activateDeactivatePartnerRequest = request.getRequest();
		auditUtil.setAuditRequestDto(PartnerManageEnum.APPROVE_REJECT_PARTNER_API);
		partnersPolicyMappingResponse = partnerManagementService
				.approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(activateDeactivatePartnerRequest,apiKeyReqId);
		response.setResponse(partnersPolicyMappingResponse);
		auditUtil.setAuditRequestDto(PartnerManageEnum.APPROVE_REJECT_PARTNER_API_SUCCESS);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	/** 
	 * This API would be used to retrieve all Auth/E-KYC Partners for the policy group.
	 * @return response this class contains list of Auth/E-KYC Partners for the policy group
	 */
	@PreAuthorize("hasAnyRole('PARTNERMANAGER','partnermanager','ID_AUTHENTICATION','REGISTRATION_PROCESSOR','RESIDENT','CREDENTIAL_ISSUANCE')")
	@RequestMapping(value="", method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<RetrievePartnerDetailsResponse>> getAllAuthEKYCPartnersForThePolicyGroup(
			@RequestParam("partnerType") Optional<String> partnerType){
		ResponseWrapper<RetrievePartnerDetailsResponse> response=new ResponseWrapper<>();
		RetrievePartnerDetailsResponse retrievePartnerDetailsResponse = null;
		auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_POLICY_GROUP);
		retrievePartnerDetailsResponse = partnerManagementService.getAllAuthEKYCPartnersForThePolicyGroup(partnerType);
		response.setId(msg);
		response.setVersion(version);
		response.setResponse(retrievePartnerDetailsResponse);
		auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_POLICY_GROUP_SUCCESS);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	/**
	 * This API would be used to retrieve the particular Auth/E-KYC Partner details for given partner id.
	 * @param partnerID this is unique id created after self registered by partner
	 * @return response this class contains Auth/E-KYC Partner details for given partner id
	 */
	@PreAuthorize("hasAnyRole('PARTNERMANAGER','partnermanager')")
	@RequestMapping(value="/{partnerId}", method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<RetrievePartnersDetails>> getparticularAuthEKYCPartnerDetailsForGivenPartnerId(
			@PathVariable String partnerId){
		ResponseWrapper<RetrievePartnersDetails> response = new ResponseWrapper<>();
		RetrievePartnersDetails retrievePartnersDetails = null;
		auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_FOR_ID);
		retrievePartnersDetails = partnerManagementService.getparticularAuthEKYCPartnerDetailsForGivenPartnerId(partnerId);
		response.setId(msg);
		response.setVersion(version);
		response.setResponse(retrievePartnersDetails);
		auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_FOR_ID_SUCCESS);
		return new ResponseEntity<>(response , HttpStatus.OK);
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
	@PreAuthorize("hasAnyRole('PARTNERMANAGER','partnermanager')")
	@RequestMapping(value = "/{partnerId}/{partnerApiKey}" , method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<PartnerAPIKeyToPolicyMappingsResponse>> getValidatPartnerAPIKeyPatternAndExpiry(
			@PathVariable String partnerId,
			@PathVariable String partnerApiKey){
		ResponseWrapper<PartnerAPIKeyToPolicyMappingsResponse> response = new ResponseWrapper<>();
		PartnerAPIKeyToPolicyMappingsResponse partnerAPIKeyToPolicyMappingsResponse = null;
		auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER);
		partnerAPIKeyToPolicyMappingsResponse = partnerManagementService.getPartnerAPIKeyToPolicyMapping(partnerId,partnerApiKey);
		response.setId(msg);
		response.setVersion(version);
		response.setResponse(partnerAPIKeyToPolicyMappingsResponse);
		auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_SUCCESS);
		return new ResponseEntity<>(response , HttpStatus.OK);
	}
	
	/**
	 * This API would be used to retrieve all Partner API Key requests as received by partner manager
	 * @return response this class contains all Partner API Key requests as received by partner manager
	 */
	@PreAuthorize("hasAnyRole('PARTNERMANAGER','partnermanager')")
	@RequestMapping(value = "/PartnerAPIKeyRequests" , method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<PartnerAPIKeyRequestsResponse>> getAllPartnerAPIKeyRequestsAsReceivedByPartnerManager(){
		List<ApikeyRequests> apikeyRequests = null;
		ResponseWrapper<PartnerAPIKeyRequestsResponse> response = new ResponseWrapper<>();
		PartnerAPIKeyRequestsResponse partnerAPIKeyRequestsResponse = new PartnerAPIKeyRequestsResponse();
		auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER);
		apikeyRequests = partnerManagementService.getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagers();
		partnerAPIKeyRequestsResponse.setApikeyRequests(apikeyRequests);
		response.setId(msg);
		response.setVersion(version);
		response.setResponse(partnerAPIKeyRequestsResponse);
		auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_SUCCESS);
		return new ResponseEntity<>(response , HttpStatus.OK);
	}
	
	/**
	 * This API would be used to retrieve the request for Partner API key to Policy Mappings for given request id.
	 * @param apiKeyReqId this is unique id created after partner request for Partner API Key
	 * @return response this class contains details related to Partner API key to Policy Mappings
	 */
	@PreAuthorize("hasAnyRole('PARTNERMANAGER','partnermanager')")
	@RequestMapping(value = "/PartnerAPIKeyRequests/{apiKeyReqId}" , method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<ApikeyRequests>> getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestId(
			@PathVariable String apiKeyReqId) {
		ResponseWrapper<ApikeyRequests> response = new ResponseWrapper<>();
		ApikeyRequests apikeyRequests=null;
		auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER);
		apikeyRequests = partnerManagementService.getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestId(apiKeyReqId);
		response.setId(msg);
		response.setVersion(version);
		response.setResponse(apikeyRequests);
		auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_SUCCESS);
		return new ResponseEntity<>(response , HttpStatus.OK);
	}	
}
