package io.mosip.pms.partner.controller;

import java.io.IOException;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.pms.common.dto.FilterValueDto;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.common.dto.PartnerPolicySearchResponseDto;
import io.mosip.pms.common.dto.PolicyRequestSearchResponseDto;
import io.mosip.pms.common.dto.SearchDto;
import io.mosip.pms.common.entity.PartnerType;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.device.response.dto.FilterResponseCodeDto;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.partner.constant.PartnerServiceAuditEnum;
import io.mosip.pms.partner.request.dto.AddContactRequestDto;
import io.mosip.pms.partner.request.dto.CACertificateRequestDto;
import io.mosip.pms.partner.request.dto.ExtractorsDto;
import io.mosip.pms.partner.request.dto.PartnerAPIKeyRequest;
import io.mosip.pms.partner.request.dto.PartnerCertDownloadRequestDto;
import io.mosip.pms.partner.request.dto.PartnerCertificateUploadRequestDto;
import io.mosip.pms.partner.request.dto.PartnerRequest;
import io.mosip.pms.partner.request.dto.PartnerSearchDto;
import io.mosip.pms.partner.request.dto.PartnerUpdateRequest;
import io.mosip.pms.partner.response.dto.APIkeyRequests;
import io.mosip.pms.partner.response.dto.CACertificateResponseDto;
import io.mosip.pms.partner.response.dto.DownloadPartnerAPIkeyResponse;
import io.mosip.pms.partner.response.dto.PartnerAPIKeyResponse;
import io.mosip.pms.partner.response.dto.PartnerCertDownloadResponeDto;
import io.mosip.pms.partner.response.dto.PartnerCertificateResponseDto;
import io.mosip.pms.partner.response.dto.PartnerCredentialTypePolicyDto;
import io.mosip.pms.partner.response.dto.PartnerResponse;
import io.mosip.pms.partner.response.dto.PartnerSearchResponseDto;
import io.mosip.pms.partner.response.dto.RetrievePartnerDetailsResponse;
import io.mosip.pms.partner.service.PartnerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

/**
 * 
 * @author Nagarjuna
 *
 */
@RestController
@RequestMapping(value = "/partners")
@Api(tags = { "Partner Service Controller" })
public class PartnerServiceController {	

	@Autowired
	PartnerService partnerService;

	String msg = "mosip.partnermanagement.partners.retrieve";
	String version = "1.0";
	
	@Autowired
	AuditUtil auditUtil;
	
	/**
	 * This API would be used for self registration by partner to create Auth/E-KYC
	 * Partners. Partner Management module would be integrating with Kernel IAM
	 * module for generation of user id and password for partners.
	 * 
	 * @param request
	 *            this class contains partner details
	 * @return response this class contains partner response
	 */
	//@PreAuthorize("hasAnyRole('PARTNER','PARTNER_ADMIN','AUTH_PARTNER','CREDENTIAL_PARTNER')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostpartners())")
	@PostMapping
	public ResponseEntity<ResponseWrapper<PartnerResponse>> partnerSelfRegistration(
			@RequestBody @Valid RequestWrapper<PartnerRequest> request) {
		ResponseWrapper<PartnerResponse> response = new ResponseWrapper<>();
		PartnerResponse partnerResponse = null;
		PartnerRequest partnerRequest = null;
		partnerRequest = request.getRequest();
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.REGISTER_PARTNER);
		partnerResponse = partnerService.savePartner(partnerRequest);
		response.setId(request.getId());
		response.setVersion(request.getVersion());
		response.setResponse(partnerResponse);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * This API would be used to submit Partner api key request.
	 * 
	 * @param partnerId
	 *            this is unique id created after self registered by partner
	 * @param request
	 *            this class contains partner policy and policy description details
	 * @return partnerAPIKeyResponse this class contains partner request id and
	 *         massage details
	 */
	//@PreAuthorize("hasAnyRole('PARTNER','PARTNER_ADMIN','AUTH_PARTNER','CREDENTIAL_PARTNER','ONLINE_VERIFICATION_PARTNER')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPatchpartnerspartneridapikeyrequest())")
	@RequestMapping(value = "/{partnerId}/apikey/request", method = RequestMethod.PATCH)
	public ResponseEntity<ResponseWrapper<PartnerAPIKeyResponse>> requestAPIKey(
			@PathVariable String partnerId, @RequestBody @Valid RequestWrapper<PartnerAPIKeyRequest> request) {
		ResponseWrapper<PartnerAPIKeyResponse> response = new ResponseWrapper<>();
		PartnerAPIKeyResponse partnerAPIKeyResponse = null;
		PartnerAPIKeyRequest partnerAPIKeyRequest = request.getRequest();
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.SUBMIT_API_REQUEST);
		partnerAPIKeyResponse = partnerService.submitPartnerApiKeyReq(partnerAPIKeyRequest, partnerId);
		response.setId(request.getId());
		response.setVersion(request.getVersion());
		response.setResponse(partnerAPIKeyResponse);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @param partnerId
	 * @param policyId
	 * @param request
	 * @return
	 */
	//@PreAuthorize("hasAnyRole('PARTNER','CREDENTIAL_PARTNER','PARTNER_ADMIN','CREDENTIAL_ISSUANCE','CREATE_SHARE','ONLINE_VERIFICATION_PARTNER')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostpartnerspartneridbioextractorspolicyid())")
	@RequestMapping(value = "/{partnerId}/bioextractors/{policyId}", method = RequestMethod.POST)
	public ResponseEntity<ResponseWrapper<String>> addBiometricExtractors(@PathVariable String partnerId ,@PathVariable String policyId,
			@RequestBody @Valid RequestWrapper<ExtractorsDto> request){
		ResponseWrapper<String> response = new ResponseWrapper<>();
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.ADD_BIO_EXTRACTORS);
		response.setResponse(partnerService.addBiometricExtractors(partnerId, policyId, request.getRequest()));
		response.setId(request.getId());
		response.setVersion(request.getVersion());
		return new ResponseEntity<>(response, HttpStatus.OK);		
	}
	
	/**
	 * 
	 * @param partnerId
	 * @param policyId
	 * @return
	 */
	//@PreAuthorize("hasAnyRole('PARTNER','CREDENTIAL_PARTNER','PARTNER_ADMIN','CREDENTIAL_ISSUANCE','CREATE_SHARE','ONLINE_VERIFICATION_PARTNER')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpartnerspartneridbioextractorspolicyid())")
	@RequestMapping(value = "{partnerId}/bioextractors/{policyId}", method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<ExtractorsDto>> getBiometricExtractors(@PathVariable String partnerId ,@PathVariable String policyId){
		ResponseWrapper<ExtractorsDto> response = new ResponseWrapper<>();
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.RETRIVE_BIO_EXTRACTORS);
		ExtractorsDto extractors = partnerService.getBiometricExtractors(partnerId, policyId);
		response.setResponse(extractors);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @param partnerId
	 * @param policyId
	 * @param credentialType
	 * @return
	 */
	//@PreAuthorize("hasAnyRole('PARTNER','CREDENTIAL_PARTNER','CREDENTIAL_ISSUANCE','CREATE_SHARE','PARTNER_ADMIN','ONLINE_VERIFICATION_PARTNER')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostpartnerspartneridcredentialtypepolicyid())")
	@RequestMapping(value = "/{partnerId}/credentialtype/{credentialType}/policies/{policyId}",method = RequestMethod.POST)
	public ResponseEntity<ResponseWrapper<String>> mapPolicyToCredentialType(@PathVariable @Valid String partnerId ,@PathVariable @Valid String policyId,
			@PathVariable @Valid String credentialType){
		ResponseWrapper<String> response = new ResponseWrapper<>();
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.MAP_POLICY_CREDENTIAL_TYPE);
		response.setResponse(partnerService.mapPartnerPolicyCredentialType(credentialType, partnerId, policyId));
		return new ResponseEntity<>(response,HttpStatus.OK);
	}
	
	//@PreAuthorize("hasAnyRole('PARTNER','CREDENTIAL_PARTNER','CREDENTIAL_ISSUANCE','CREATE_SHARE','PARTNER_ADMIN','ONLINE_VERIFICATION_PARTNER')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpartnerspartneridcredentialtypepolicies())")
	@RequestMapping(value = "/{partnerId}/credentialtype/{credentialType}/policies",method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<PartnerCredentialTypePolicyDto>> getCredentialTypePolicy(@PathVariable @Valid String partnerId,@PathVariable @Valid String credentialType) throws JsonParseException, JsonMappingException, IOException{
		ResponseWrapper<PartnerCredentialTypePolicyDto> response = new ResponseWrapper<>();
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.RETRIVE_POLICY_CREDENTIAL_TYPE);
		response.setResponse(partnerService.getPartnerCredentialTypePolicy(credentialType, partnerId));
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	/**
	 * 
	 * @param partnerId
	 * @param request
	 * @return
	 */
	//@PreAuthorize("hasAnyRole('PARTNER','AUTH_PARTNER','CREDENTIAL_PARTNER','PARTNER_ADMIN','ONLINE_VERIFICATION_PARTNER')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostpartnerspartneridcontactadd())")
	@RequestMapping(value = "{partnerId}/contact/add", method = RequestMethod.POST)
	public ResponseEntity<ResponseWrapper<String>> addContact(@PathVariable String partnerId,@RequestBody @Valid RequestWrapper<AddContactRequestDto>request){
		ResponseWrapper<String> response = new ResponseWrapper<>();
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.ADD_CONTACTS);
		response.setResponse(partnerService.createAndUpdateContactDetails(request.getRequest(),partnerId));
		response.setId(request.getId());
		response.setVersion(request.getVersion());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	/**
	 * This API would be used to update Auth/E-KYC Partner's details.
	 * 
	 * @param request
	 *            this class contains partner updated details
	 * @param partnerId
	 *            this is unique id created after self registered by partner
	 * @return partnerResponse this class contains updated partner details
	 */
	//@PreAuthorize("hasAnyRole('PARTNER','AUTH_PARTNER','CREDENTIAL_PARTNER','PARTNER_ADMIN','ONLINE_VERIFICATION_PARTNER')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPutpartnerspartnerid())")
	@RequestMapping(value = "/{partnerId}", method = RequestMethod.PUT)
	public ResponseEntity<ResponseWrapper<PartnerResponse>> updatePartnerDetails(
			@RequestBody @Valid RequestWrapper<PartnerUpdateRequest> request, @PathVariable String partnerId) {
		ResponseWrapper<PartnerResponse> response = new ResponseWrapper<>();
		PartnerResponse partnerResponse = null;
		PartnerUpdateRequest partnerRequest = request.getRequest();
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPDATE_PARTNER);
		partnerResponse = partnerService.updatePartnerDetail(partnerRequest, partnerId);
		response.setId(request.getId());
		response.setVersion(request.getVersion());
		response.setResponse(partnerResponse);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	/**
	 * This API would be used to retrieve Auth/E-KYC Partner details
	 * 
	 * @param partnerId
	 *            this is unique id created after self registered by partner
	 * @return retrievePartnerDetailsResponse this class contains partner details
	 */
	//@PreAuthorize("hasAnyRole('PARTNER','AUTH_PARTNER','CREDENTIAL_PARTNER','RESIDENT','PARTNER_ADMIN','ONLINE_VERIFICATION_PARTNER')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpartnerspartnerid())")
	@RequestMapping(value = "/{partnerId}", method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<RetrievePartnerDetailsResponse>> retrievePartnerDetails(
			@PathVariable String partnerId) {
		ResponseWrapper<RetrievePartnerDetailsResponse> response = new ResponseWrapper<>();
		RetrievePartnerDetailsResponse retrievePartnerDetailsResponse = null;
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.RETRIVE_PARTNER);
		retrievePartnerDetailsResponse = partnerService.getPartnerDetails(partnerId);
		response.setId(msg);
		response.setVersion(version);
		response.setResponse(retrievePartnerDetailsResponse);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}			

	/**
	 * This API would be used to retrieve all API key requests submitted by partner
	 * till date.
	 * 
	 * @param partnerId
	 *            this is unique id created after self registered by partner
	 * @return partnersRetrieveApiKeyRequests this is a list of partner request for
	 *         creation of partner API Key
	 */
	//@PreAuthorize("hasAnyRole('PARTNER','AUTH_PARTNER','CREDENTIAL_PARTNER','PARTNER_ADMIN','ONLINE_VERIFICATION_PARTNER')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpartnerspartneridapikeyrequest())")
	@RequestMapping(value = "/{partnerId}/apikey/request", method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<List<APIkeyRequests>>> getAPIKeyRequestsOfPartner(
			@PathVariable String partnerId) {
		ResponseWrapper<List<APIkeyRequests>> response = new ResponseWrapper<>();
		List<APIkeyRequests> apikeyRequestsList = null;
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.RETRIVE_PARTNER_APIKEYS);
		apikeyRequestsList = partnerService.retrieveAllApiKeyRequestsSubmittedByPartner(partnerId);
		response.setId(msg);
		response.setVersion(version);
		response.setResponse(apikeyRequestsList);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * This API would be used to view API key request status and API key (in case
	 * request is approved).
	 * 
	 * @param partnerId
	 *            this is unique id created after self registered by partner
	 * @param apikeyreqId
	 *            this is unique id created after partner request for Partner API
	 *            Key
	 * @return response this class contains partnerApiKey apiKeyRequestStatus and
	 *         validity details
	 */
	//@PreAuthorize("hasAnyRole('PARTNER','AUTH_PARTNER','CREDENTIAL_PARTNER','PARTNER_ADMIN','ONLINE_VERIFICATION_PARTNER')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpartnerspartneridapikeyapikeyreqid())")
	@RequestMapping(value = "/{partnerId}/apikey/{apikeyreqId}", method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<DownloadPartnerAPIkeyResponse>> getAPIKeyRequestStatus(
			@PathVariable String partnerId, @PathVariable String apikeyreqId) {
		ResponseWrapper<DownloadPartnerAPIkeyResponse> response = new ResponseWrapper<>();
		DownloadPartnerAPIkeyResponse aPIkeyRequests = null;
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.RETRIVE_PARTNER_APIKEY_STATUS);
		aPIkeyRequests = partnerService.getApikeyFromRequestKey(partnerId, apikeyreqId);
		response.setId(msg);
		response.setVersion(version);
		response.setResponse(aPIkeyRequests);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	/**
	 * To Upload CA/Sub-CA certificates
	 * 
	 * @param caCertRequestDto {@link CACertificateRequestDto} request
	 * @return {@link CACertficateResponseDto} Upload Success
	 * @throws IOException 
	 * @throws JsonProcessingException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	//@PreAuthorize("hasAnyRole('PARTNERMANAGER','PARTNER_ADMIN','AUTH_PARTNER','PMS_USER','ID_AUTHENTICATION','ONLINE_VERIFICATION_PARTNER')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostpartnerscertificatecaupload())")
	@RequestMapping(value = "/certificate/ca/upload", method = RequestMethod.POST)
	public ResponseWrapper<CACertificateResponseDto> uploadCACertificate(
			@ApiParam("Upload CA/Sub-CA certificates.") @RequestBody @Valid RequestWrapper<CACertificateRequestDto> caCertRequestDto) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {
		ResponseWrapper<CACertificateResponseDto> response = new ResponseWrapper<>();
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPLOAD_CA_CERT);
		response.setResponse(partnerService.uploadCACertificate(caCertRequestDto.getRequest()));
		return response;
    }
    
    
	/**
	 * To Upload Partner Certificate.
	 * 
	 * @param partnerCertRequestDto {@link PartnerCertificateUploadRequestDto} request
	 * @return {@link PartnerCertificateResponseDto} signed certificate response
	 * @throws IOException 
	 * @throws JsonProcessingException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	//@PreAuthorize("hasAnyRole('PARTNER','PMS_USER','AUTH_PARTNER','DEVICE_PROVIDER','FTM_PROVIDER','CREDENTIAL_PARTNER','CREDENTIAL_ISSUANCE','ID_AUTHENTICATION','PARTNER_ADMIN','ONLINE_VERIFICATION_PARTNER')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostpartnerscertificateupload())")
	@RequestMapping(value = "/certificate/upload", method = RequestMethod.POST)
	public ResponseWrapper<PartnerCertificateResponseDto> uploadPartnerCertificate(
			@ApiParam("Upload Partner Certificates.") @RequestBody @Valid RequestWrapper<PartnerCertificateUploadRequestDto> partnerCertRequestDto) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {
		ResponseWrapper<PartnerCertificateResponseDto> response = new ResponseWrapper<>();
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPLOAD_PARTNER_CERT);
		response.setResponse(partnerService.uploadPartnerCertificate(partnerCertRequestDto.getRequest()));
		return response;
	}

    /**
	 * To Download Partner Certificate.
	 * 
	 * @param certDownloadRequestDto {@link PartnerCertDownloadRequestDto} request
	 * @return {@link PartnerCertDownloadResponeDto} encrypted Data
     * @throws IOException 
     * @throws JsonProcessingException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
	 */
	//@PreAuthorize("hasAnyRole('PARTNER','PMS_USER','AUTH_PARTNER','DEVICE_PROVIDER','FTM_PROVIDER','CREDENTIAL_PARTNER','CREDENTIAL_ISSUANCE','CREATE_SHARE','ID_AUTHENTICATION','PARTNER_ADMIN','ONLINE_VERIFICATION_PARTNER')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getGetpartnerspartneridcertificate())")
	@RequestMapping(value = "/{partnerId}/certificate", method = RequestMethod.GET)
	public ResponseWrapper<PartnerCertDownloadResponeDto> getPartnerCertificate(
			@ApiParam("To download resigned partner certificate.")  @PathVariable("partnerId") @NotNull String partnerId) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {		
		ResponseWrapper<PartnerCertDownloadResponeDto> response = new ResponseWrapper<>();
		PartnerCertDownloadRequestDto requestDto = new PartnerCertDownloadRequestDto();
		requestDto.setPartnerId(partnerId);
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.RETRIVE_PARTNER_CERT);
		response.setResponse(partnerService.getPartnerCertificate(requestDto));
		return response;
    }	
	
	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostpartnerssearch())")
	@PostMapping("/search")
	//@PreAuthorize("hasAnyRole('PARTNER','PMS_USER','AUTH_PARTNER','DEVICE_PROVIDER','FTM_PROVIDER','PARTNER_ADMIN','CREDENTIAL_PARTNER','ONLINE_VERIFICATION_PARTNER')")
	public ResponseWrapper<PageResponseDto<PartnerSearchResponseDto>> searchPartner(
			@RequestBody @Valid RequestWrapper<PartnerSearchDto> request) {
		ResponseWrapper<PageResponseDto<PartnerSearchResponseDto>> responseWrapper = new ResponseWrapper<>();
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.SEARCH_PARTNER);
		responseWrapper.setResponse(partnerService.searchPartner(request.getRequest()));
		return responseWrapper;
	}

	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostpartnerspartnertypesearch())")
	@PostMapping("/partnertype/search")
	//@PreAuthorize("hasAnyRole('PARTNER','PMS_USER','AUTH_PARTNER','DEVICE_PROVIDER','FTM_PROVIDER','CREDENTIAL_PARTNER','PARTNER_ADMIN','ONLINE_VERIFICATION_PARTNER')")
	public ResponseWrapper<PageResponseDto<PartnerType>> searchPartnerType(
			@RequestBody @Valid RequestWrapper<SearchDto> request) {
		ResponseWrapper<PageResponseDto<PartnerType>> responseWrapper = new ResponseWrapper<>();
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.SEARCH_PARTNER_TYPE);
		responseWrapper.setResponse(partnerService.searchPartnerType(request.getRequest()));
		return responseWrapper;
	}
	
	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostpartnersfiltervalues())")
	@PostMapping("/filtervalues")
	//@PreAuthorize("hasAnyRole('PARTNER','PARTNER_ADMIN','ONLINE_VERIFICATION_PARTNER')")
	public ResponseWrapper<FilterResponseCodeDto> filterValues(
			@RequestBody @Valid RequestWrapper<FilterValueDto> request) {
		ResponseWrapper<FilterResponseCodeDto> responseWrapper = new ResponseWrapper<>();
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.FILTER_PARTNER);
		responseWrapper.setResponse(partnerService.filterValues(request.getRequest()));
		return responseWrapper;
	}
	
	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostpartnersapikeyrequestfiltervalues())")
	@PostMapping("/apikey/request/filtervalues")
	//@PreAuthorize("hasAnyRole('PARTNER','PARTNER_ADMIN','AUTH_PARTNER','CREDENTIAL_PARTNER','ONLINE_VERIFICATION_PARTNER')")
	public ResponseWrapper<FilterResponseCodeDto> apikeyRequetsFilterValues(
			@RequestBody @Valid RequestWrapper<FilterValueDto> request) {
		ResponseWrapper<FilterResponseCodeDto> responseWrapper = new ResponseWrapper<>();
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.FILTER_PARTNER_APIKEY_REQUESTS);
		responseWrapper.setResponse(partnerService.apiKeyRequestFilter(request.getRequest()));
		return responseWrapper;
	}
	
	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostpartnersapikeyrequestsearch())")
	@PostMapping("/apikey/request/search")
	//@PreAuthorize("hasAnyRole('PARTNER','PMS_USER','AUTH_PARTNER','DEVICE_PROVIDER','FTM_PROVIDER','CREDENTIAL_PARTNER','PARTNER_ADMIN','ONLINE_VERIFICATION_PARTNER')")
	public ResponseWrapper<PageResponseDto<PolicyRequestSearchResponseDto>> searchApikeyRequest(
			@RequestBody @Valid RequestWrapper<SearchDto> request) {
		ResponseWrapper<PageResponseDto<PolicyRequestSearchResponseDto>> responseWrapper = new ResponseWrapper<>();
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.SEARCH_PARTNER_APIKEY);
		responseWrapper.setResponse(partnerService.searchPartnerApiKeyRequests(request.getRequest()));
		return responseWrapper;
	}
	
	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostpartnersapikeysearch())")
	@PostMapping("/apikey/search")
	//@PreAuthorize("hasAnyRole('PARTNER','PMS_USER','AUTH_PARTNER','DEVICE_PROVIDER','FTM_PROVIDER','CREDENTIAL_PARTNER','PARTNER_ADMIN','ONLINE_VERIFICATION_PARTNER')")
	public ResponseWrapper<PageResponseDto<PartnerPolicySearchResponseDto>> searchApikey(
			@RequestBody @Valid RequestWrapper<SearchDto> request) {
		ResponseWrapper<PageResponseDto<PartnerPolicySearchResponseDto>> responseWrapper = new ResponseWrapper<>();
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.SEARCH_PARTNER_APIKEY_REQUEST);
		responseWrapper.setResponse(partnerService.searchPartnerApiKeys(request.getRequest()));
		return responseWrapper;
	}
}