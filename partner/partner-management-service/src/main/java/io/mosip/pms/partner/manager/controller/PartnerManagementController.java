package io.mosip.pms.partner.manager.controller;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

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

import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.partner.manager.constant.PartnerManageEnum;
import io.mosip.pms.partner.manager.dto.ActivateDeactivatePartnerRequest;
import io.mosip.pms.partner.manager.dto.ApikeyRequests;
import io.mosip.pms.partner.manager.dto.PartnerAPIKeyRequestsResponse;
import io.mosip.pms.partner.manager.dto.PartnerAPIKeyToPolicyMappingsResponse;
import io.mosip.pms.partner.manager.dto.PartnerPolicyResponse;
import io.mosip.pms.partner.manager.dto.PartnersPolicyMappingRequest;
import io.mosip.pms.partner.manager.dto.PartnersPolicyMappingResponse;
import io.mosip.pms.partner.manager.dto.RetrievePartnerDetailsResponse;
import io.mosip.pms.partner.manager.service.PartnerManagerService;
import io.swagger.annotations.Api;

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
	
	String msg = "mosip.partnermanagement.partners.retrieve";
	String version = "1.0";
	

	/**
	 * This API would be used by partner Manager, to update Partner api key to Policy Mappings.
	 * @param request this class contain oldPolicyID and newPolicyID
	 * @param partnerId this is unique id created after self registered by partner
	 * @param partnerApiKey this is unique id created by partner manager at the time of approving partner request
	 * @return response this class contains massage about API key created successfully
	 */
	//@PreAuthorize("hasAnyRole('PARTNERMANAGER','PARTNER_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPutpartnerspartneridapikeypolicies())")
	@RequestMapping(value = "/{partnerId}/apikey/{apikey}/policies", method = RequestMethod.PUT)
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
	//@PreAuthorize("hasAnyRole('PARTNERMANAGER','PARTNER_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPatchpartnerspartnerid())")
	@RequestMapping(value = "/{partnerId}", method = RequestMethod.PATCH)
	 public ResponseEntity<ResponseWrapper<PartnersPolicyMappingResponse>> activateDeactivatePartner(
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
	//@PreAuthorize("hasAnyRole('PARTNERMANAGER','PARTNER_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPatchpartnerspartneridapikey())")
	@RequestMapping(value = "/{partnerId}/apikey/{apikey}", method = RequestMethod.PATCH)
	public ResponseEntity<ResponseWrapper<PartnersPolicyMappingResponse>> activateDeactivateAPIKey(
			@PathVariable String partnerId,
			@RequestBody @Valid RequestWrapper<ActivateDeactivatePartnerRequest> request,
			@PathVariable String apikey){
		ResponseWrapper<PartnersPolicyMappingResponse> response = new ResponseWrapper<>();
		PartnersPolicyMappingResponse partnersPolicyMappingResponse = null;
		response.setId(request.getId());
		response.setVersion(request.getVersion());
		auditUtil.setAuditRequestDto(PartnerManageEnum.ACTIVATE_DEACTIVATE_API_PARTNERS);
		ActivateDeactivatePartnerRequest activateDeactivatePartnerRequest = request.getRequest();
		partnersPolicyMappingResponse = partnerManagementService
				.activateDeactivatePartnerAPIKeyGivenPartner(partnerId,activateDeactivatePartnerRequest,apikey);
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
	//@PreAuthorize("hasAnyRole('PARTNERMANAGER','PARTNER_ADMIN','ID_AUTHENTICATION','CREDENTIAL_ISSUANCE')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpartnerspartneridapikeymisplicensekey())")
	@RequestMapping(value = "/{partnerId}/apikey/{apikey}/misp/{misplicensekey}/validate", 
			method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<PartnerPolicyResponse>> validatePartnerAndMisp(
			@PathVariable String partnerId, @PathVariable String apikey,
			@PathVariable String misplicensekey,@RequestParam(defaultValue = "false", name ="needPartnerCert") boolean needPartnerCert){
		ResponseWrapper<PartnerPolicyResponse> response = new ResponseWrapper<>();
		auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_POLICY_MAPPING);
		PartnerPolicyResponse responseFromService = partnerManagementService.getPartnerMappedPolicyFile(misplicensekey,apikey,partnerId,needPartnerCert);
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
	//@PreAuthorize("hasAnyRole('PARTNERMANAGER','PARTNER_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPatchpartnersapikey())")
	@RequestMapping(value = "/apikey/{apikey}", method = RequestMethod.PATCH)
	public ResponseEntity<ResponseWrapper<PartnersPolicyMappingResponse>> approveRejectAPIKeyRequest(
			@RequestBody @Valid RequestWrapper<ActivateDeactivatePartnerRequest> request,
			@PathVariable String apikey){
		ResponseWrapper<PartnersPolicyMappingResponse> response = new ResponseWrapper<>();
		PartnersPolicyMappingResponse partnersPolicyMappingResponse = null;
		response.setId(request.getId());
		response.setVersion(request.getVersion());
		ActivateDeactivatePartnerRequest activateDeactivatePartnerRequest = request.getRequest();
		auditUtil.setAuditRequestDto(PartnerManageEnum.APPROVE_REJECT_PARTNER_API);
		partnersPolicyMappingResponse = partnerManagementService
				.approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(activateDeactivatePartnerRequest,apikey);
		response.setResponse(partnersPolicyMappingResponse);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	/** 
	 * This API would be used to retrieve all Auth/E-KYC Partners for the policy group.
	 * @return response this class contains list of Auth/E-KYC Partners for the policy group
	 */
	//@PreAuthorize("hasAnyRole('PARTNERMANAGER','PARTNER_ADMIN','partnermanager','ID_AUTHENTICATION','REGISTRATION_PROCESSOR','RESIDENT','CREDENTIAL_ISSUANCE')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpartners())")
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<RetrievePartnerDetailsResponse>> getPartners(
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
	 * Partner managers would be using this request to retrieve the Partner API key
	 * to Policy Mappings. Partner management system would be able to validate
	 * Partner API Key pattern, validate expiry for Partner API Key and status
	 * details in background, while fetching Policy to Partner API mappings.
	 * 
	 * @param partnerId this is unique id created after self registered by partner
	 * @param partnerApiKey this is unique id created by partner manager at the time of approving partner request
	 * @return response this class contains partnerID and policyId
	 */
	//@PreAuthorize("hasAnyRole('PARTNERMANAGER','PARTNER_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpartnerspartneridapikey())")
	@RequestMapping(value = "/{partnerId}/apikey/{apikey}" , method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<PartnerAPIKeyToPolicyMappingsResponse>> getPolicyMappedToAPIKey(
			@PathVariable String partnerId,
			@PathVariable String apikey){
		ResponseWrapper<PartnerAPIKeyToPolicyMappingsResponse> response = new ResponseWrapper<>();
		PartnerAPIKeyToPolicyMappingsResponse partnerAPIKeyToPolicyMappingsResponse = null;
		auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER);
		partnerAPIKeyToPolicyMappingsResponse = partnerManagementService.getPartnerAPIKeyToPolicyMapping(partnerId,apikey);
		response.setId(msg);
		response.setVersion(version);
		response.setResponse(partnerAPIKeyToPolicyMappingsResponse);
		return new ResponseEntity<>(response , HttpStatus.OK);
	}
	
	/**
	 * This API would be used to retrieve all Partner API Key requests as received by partner manager
	 * @return response this class contains all Partner API Key requests as received by partner manager
	 */
	//@PreAuthorize("hasAnyRole('PARTNERMANAGER','PARTNER_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpartnersapikey())")
	@RequestMapping(value = "/apikey" , method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<PartnerAPIKeyRequestsResponse>> getAPIKeyRequests(){
		List<ApikeyRequests> apikeyRequests = null;
		ResponseWrapper<PartnerAPIKeyRequestsResponse> response = new ResponseWrapper<>();
		PartnerAPIKeyRequestsResponse partnerAPIKeyRequestsResponse = new PartnerAPIKeyRequestsResponse();
		auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER);
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
	//@PreAuthorize("hasAnyRole('PARTNERMANAGER','PARTNER_ADMIN')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpartnersapikeykey())")
	@RequestMapping(value = "/apikey/{apikey}" , method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<ApikeyRequests>> getAPIKeyRequest(
			@PathVariable String apikey) {
		ResponseWrapper<ApikeyRequests> response = new ResponseWrapper<>();
		ApikeyRequests apikeyRequests=null;
		auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER);
		apikeyRequests = partnerManagementService.getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestId(apikey);
		response.setId(msg);
		response.setVersion(version);
		response.setResponse(apikeyRequests);
		return new ResponseEntity<>(response , HttpStatus.OK);
	}	
}
