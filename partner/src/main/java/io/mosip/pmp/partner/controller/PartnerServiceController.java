package io.mosip.pmp.partner.controller;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.pmp.partner.core.RequestWrapper;
import io.mosip.pmp.partner.core.ResponseWrapper;
import io.mosip.pmp.partner.dto.APIkeyRequests;
import io.mosip.pmp.partner.dto.DigitalCertificateRequest;
import io.mosip.pmp.partner.dto.DigitalCertificateResponse;
import io.mosip.pmp.partner.dto.DownloadPartnerAPIkeyResponse;
import io.mosip.pmp.partner.dto.LoginUserRequest;
import io.mosip.pmp.partner.dto.LoginUserResponse;
import io.mosip.pmp.partner.dto.PartnerAPIKeyRequest;
import io.mosip.pmp.partner.dto.PartnerAPIKeyResponse;
import io.mosip.pmp.partner.dto.PartnerRequest;
import io.mosip.pmp.partner.dto.PartnerResponse;
import io.mosip.pmp.partner.dto.PartnerUpdateRequest;
import io.mosip.pmp.partner.dto.RetrievePartnerDetailsResponse;
import io.mosip.pmp.partner.dto.RetrievePartnerDetailsWithNameResponse;
import io.mosip.pmp.partner.dto.SignUserRequest;
import io.mosip.pmp.partner.dto.SignUserResponse;
import io.mosip.pmp.partner.service.PartnerService;


/**
 * <p>This is the MOSIP Partner Service controller. This defines all the necessary operations</p>
 * <p>required for Partner</p>.
 * Partner Service Controller is having following operations
 * 1. Partner SelfRegistration {{@link #partnerSelfRegistration(RequestWrapper)}
 * 2. Retrieve PartnerDetails {{@link #retrievePartnerDetails(String)}
 * 3. Update PartnerDetails {{@link #updatePartnerDetails(RequestWrapper, String)}
 * 4. Submitting Partner API Key Request{{@link #submitPartnerApiKeyRequest(String, RequestWrapper)}
 * 5. Downloading Partner API Key {{@link #downloadPartnerAPIkey(String, String)}
 * 6. Retrieve All ApiKeyRequest Submitted By Partner Till Date {{@link #retrieveAllApiKeyRequestsSubmittedByPartnerTillDate(String)}
 * 7. View ApiKeyRequest Status And ApiKey {{@link #viewApiKeyRequestStatusAndApiKey(String, String)}
 * 
 * @author sanjeev.shrivastava
 * @version 1.0
 */


@RestController
@RequestMapping(value = "/partners")
public class PartnerServiceController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PartnerServiceController.class);
	
	@Autowired
	PartnerService partnerService;
	
	
	/**
	 * This API would be used for testing of partners application up and running
	 * @return partners application is up and running
	 */
	
	@RequestMapping(value="/test", method = RequestMethod.GET)
	public String test(){
		return "partners application is up and running";
	}
	
	/**
	 * This API would be used for self registration by partner to create Auth/E-KYC
	 * Partners. Partner Management module would be integrating with Kernel IAM
	 * module for generation of user id and password for partners.
	 * @param request this class contains partner details
	 * @return response this class contains partner response
	 */

	@RequestMapping(value = "partnerReg", method = RequestMethod.POST)
	public ResponseEntity<ResponseWrapper<PartnerResponse>> partnerSelfRegistration(
			@RequestBody @Valid RequestWrapper<PartnerRequest> request) {
		LOGGER.info("+++++++++++++++++partner self registration++++++++++++++++++++");
		ResponseWrapper<PartnerResponse> response = new ResponseWrapper<PartnerResponse>();
		PartnerResponse partnerResponse = null;
		PartnerRequest partnerRequest = null;
		partnerRequest = request.getRequest();
		LOGGER.info("+++++++++++++++++calling savePartner method++++++++++++++++++++");
		partnerResponse = partnerService.savePartner(partnerRequest);
		LOGGER.info(partnerResponse + " : response of savePartner method");
		response.setId(request.getId());
		response.setVersion(request.getVersion());
		response.setResponse(partnerResponse);
		return new ResponseEntity<ResponseWrapper<PartnerResponse>>(response, HttpStatus.OK);
	}
	
	/**
	 * This API would be used to retrieve Auth/E-KYC Partner details
	 * @param partnerID this is unique id created after self registered by partner
	 * @return retrievePartnerDetailsResponse this class contains partner details
	 */
	
	@RequestMapping(value = "/{partnerID}", method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<RetrievePartnerDetailsResponse>> retrievePartnerDetails(
			@PathVariable String partnerID) {

		ResponseWrapper<RetrievePartnerDetailsResponse> response = new ResponseWrapper<RetrievePartnerDetailsResponse>();
		RetrievePartnerDetailsResponse retrievePartnerDetailsResponse = null;
		retrievePartnerDetailsResponse = partnerService.getPartnerDetails(partnerID);
		response.setId("mosip.partnermanagement.partners.retrieve");
		response.setVersion("1.0");
		response.setResponse(retrievePartnerDetailsResponse);
		return new ResponseEntity<ResponseWrapper<RetrievePartnerDetailsResponse>>(response, HttpStatus.OK);
	}
	
	
	@RequestMapping(value = "/findbyname/{partnerName}", method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<RetrievePartnerDetailsWithNameResponse>> retrievePartnerDetailsWithName(
			@PathVariable String partnerName) {
		//TODO
		ResponseWrapper<RetrievePartnerDetailsWithNameResponse> response = new ResponseWrapper<RetrievePartnerDetailsWithNameResponse>();
		RetrievePartnerDetailsWithNameResponse retrievePartnerDetailsWithNameResponse = null;
		retrievePartnerDetailsWithNameResponse = partnerService.getPartnerDetailsWithName(partnerName);
		response.setId("mosip.partnermanagement.partners.retrieve");
		response.setVersion("1.0");
		response.setResponse(retrievePartnerDetailsWithNameResponse);
		return new ResponseEntity<ResponseWrapper<RetrievePartnerDetailsWithNameResponse>>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/findbypolicyname/{policyName}", method = RequestMethod.GET)
	public ResponseEntity<String> retrievePolicyIdByPolicyName(
			@PathVariable String policyName){
		String policyId = null;
		policyId = partnerService.getPolicyId(policyName);
		return new ResponseEntity<String>(policyId, HttpStatus.OK);
	}
	
	/**
	 * This API would be used to update Auth/E-KYC Partner's details.
	 * @param request this class contains partner updated details 
	 * @param partnerID this is unique id created after self registered by partner
	 * @return partnerResponse this class contains updated partner details  
	 */
	
	@RequestMapping(value = "/{partnerID}", method = RequestMethod.PUT)
	public ResponseEntity<ResponseWrapper<PartnerResponse>> updatePartnerDetails(
			@RequestBody @Valid RequestWrapper<PartnerUpdateRequest> request, @PathVariable String partnerID) {
		ResponseWrapper<PartnerResponse> response = new ResponseWrapper<PartnerResponse>();
		PartnerResponse partnerResponse = null;
		PartnerUpdateRequest partnerRequest = request.getRequest();
		partnerResponse = partnerService.updatePartnerDetail(partnerRequest, partnerID);
		response.setId(request.getId());
		response.setVersion(request.getVersion());
		response.setResponse(partnerResponse);
		return new ResponseEntity<ResponseWrapper<PartnerResponse>>(response, HttpStatus.OK);
	}

	/**
	 * This API would be used to submit Partner api key request.
	 * @param partnerID this is unique id created after self registered by partner
	 * @param request this class contains partner policy and policy description details 
	 * @return partnerAPIKeyResponse this class contains partner request id and massage details 
	 */
	
	@RequestMapping(value = "/{partnerID}/partnerAPIKeyRequests", method = RequestMethod.POST)
	public ResponseEntity<ResponseWrapper<PartnerAPIKeyResponse>> submitPartnerApiKeyRequest(
			@PathVariable String partnerID, 
			@RequestBody @Valid RequestWrapper<PartnerAPIKeyRequest> request) {
		ResponseWrapper<PartnerAPIKeyResponse> response = new ResponseWrapper<PartnerAPIKeyResponse>();
		PartnerAPIKeyResponse partnerAPIKeyResponse = null;
		PartnerAPIKeyRequest partnerAPIKeyRequest = request.getRequest();
		partnerAPIKeyResponse = partnerService.submitPartnerApiKeyReq(partnerAPIKeyRequest, partnerID);
		response.setId(request.getId());
		response.setVersion(request.getVersion());
		response.setResponse(partnerAPIKeyResponse);
		return new ResponseEntity<ResponseWrapper<PartnerAPIKeyResponse>>(response, HttpStatus.OK);
	}

	/**
	 *  This API would be used to download Partner API key for the given APIKeyReqID.
	 * @param partnerID this is unique id created after self registered by partner
	 * @param aPIKeyReqID this is unique id created after partner request for Partner API Key
	 * @return downloadPartnerAPIkeyResponse this is unique id created once partner manager approved the partner API request
	 */
	
	@RequestMapping(value = "/{partnerID}/partnerAPIKeyRequests/{aPIKeyReqID}", method = RequestMethod.POST)
	public ResponseEntity<ResponseWrapper<DownloadPartnerAPIkeyResponse>> downloadPartnerAPIkey(
			@PathVariable String partnerID, @PathVariable String aPIKeyReqID) {
		ResponseWrapper<DownloadPartnerAPIkeyResponse> response = new ResponseWrapper<DownloadPartnerAPIkeyResponse>();
		DownloadPartnerAPIkeyResponse resp = null;
		resp = partnerService.downloadPartnerAPIkey(partnerID, aPIKeyReqID);
		response.setId("mosip.partnermanagement.partnerAPIKey.download");
		response.setVersion("1.0");
		response.setResponse(resp);
		return new ResponseEntity<ResponseWrapper<DownloadPartnerAPIkeyResponse>>(response, HttpStatus.OK);
	}

	/**
	 * This API would be used to retrieve all API key requests submitted by partner till date.
	 * @param partnerID this is unique id created after self registered by partner
	 * @return partnersRetrieveApiKeyRequests this is a list of partner request for creation of partner API Key
	 */
	
	/*@RequestMapping(value = "/{partnerID}/partnerAPIKeyRequests", method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<PartnersRetrieveApiKeyRequests>> retrieveAllApiKeyRequestsSubmittedByPartnerTillDate(
			@PathVariable String partnerID) {
		ResponseWrapper<PartnersRetrieveApiKeyRequests> response = new ResponseWrapper<PartnersRetrieveApiKeyRequests>();
		PartnersRetrieveApiKeyRequests partnersRetrieveApiKeyRequests = null;
		partnersRetrieveApiKeyRequests = partnerService.retrieveAllApiKeyRequestsSubmittedByPartner(partnerID);
		response.setId("mosip.partnermanagement.partners.retrieve.apiKeyRequests");
		response.setVersion("1.0");
		response.setResponse(partnersRetrieveApiKeyRequests);
		return new ResponseEntity<ResponseWrapper<PartnersRetrieveApiKeyRequests>>(response, HttpStatus.OK);
	}*/
	
	
	@RequestMapping(value = "/{partnerID}/partnerAPIKeyRequests", method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<List<APIkeyRequests>>> retrieveAllApiKeyRequestsSubmittedByPartnerTillDate(
			@PathVariable String partnerID) {
		ResponseWrapper<List<APIkeyRequests>> response = new ResponseWrapper<List<APIkeyRequests>>();
		List<APIkeyRequests> APIkeyRequests_list = null;
		APIkeyRequests_list = partnerService.retrieveAllApiKeyRequestsSubmittedByPartner(partnerID);
		response.setId("mosip.partnermanagement.partners.retrieve.apiKeyRequests");
		response.setVersion("1.0");
		response.setResponse(APIkeyRequests_list);
		return new ResponseEntity<ResponseWrapper<List<APIkeyRequests>>>(response, HttpStatus.OK);
	}

	/**
	 *  This API would be used to view API key request status and API key (in case request is approved).
	 * @param partnerID this is unique id created after self registered by partner
	 * @param aPIKeyReqID this is unique id created after partner request for Partner API Key
	 * @return response this class contains partnerApiKey apiKeyRequestStatus and validity details 
	 */
	
	@RequestMapping(value = "/{partnerID}/partnerAPIKeyRequests/{aPIKeyReqID}", method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<APIkeyRequests>> viewApiKeyRequestStatusAndApiKey(
			@PathVariable String partnerID, @PathVariable String aPIKeyReqID) {
		ResponseWrapper<APIkeyRequests> response = new ResponseWrapper<APIkeyRequests>();
		APIkeyRequests aPIkeyRequests = null;
		aPIkeyRequests = partnerService.viewApiKeyRequestStatusApiKey(partnerID, aPIKeyReqID);
		response.setId("mosip.partnermanagement.partners.apikey.status");
		response.setVersion("1.0");
		response.setResponse(aPIkeyRequests);
		return new ResponseEntity<ResponseWrapper<APIkeyRequests>>(response, HttpStatus.OK);
	}
	
	// validate the Digital Certificate (Certification Authority as a MOSIP) to Partner Management using this API.
	
	/**
	 * As the MOSIP system Partner Management module would integrate with Kernel for validation of partner's digital certificate. 
	 * In case where MOSIP would act as certification authority for partners, 
	 * MOSIP would be able to sign and resign partner digital certificates. 
	 * Partner management module would depend on Kernel services for signing and re-signing of partner digital certificates. 
	 * Kernel Signature service would be utilized to validate signature : Kernel Signature Service
	 * 
	 * @param request this class contains digitalCertificate details
	 * @return DigitalCertificateResponse this class contains massage
	 */
	@RequestMapping(value = "/validatedigitalcertificate", method = RequestMethod.PUT)
	public ResponseEntity<ResponseWrapper<DigitalCertificateResponse>> validateDigitalCertificate(
			@RequestBody RequestWrapper<DigitalCertificateRequest> request){
		ResponseWrapper<DigitalCertificateResponse> response = new ResponseWrapper<DigitalCertificateResponse>();
		DigitalCertificateResponse digitalCertificateResponse = null;
		digitalCertificateResponse = partnerService.validateDigitalCertificate(request);
		response.setId(request.getId());
		response.setVersion(request.getVersion());
		response.setResponse(digitalCertificateResponse);
		return new ResponseEntity<ResponseWrapper<DigitalCertificateResponse>>(response , HttpStatus.OK);
	}
	
	// upload the digital certificates (Certification Authority) to Partner Management using this API.
	
	/*@RequestMapping(value = "/createdigitalcertificate", method = RequestMethod.POST)
	public ResponseEntity<ResponseWrapper<DigitalCertificateResponse>> uploadDigitalCertificate(
			@RequestBody RequestWrapper<DigitalCertificateRequest> request) {
		ResponseWrapper<DigitalCertificateResponse> response = new ResponseWrapper<DigitalCertificateResponse>();
		DigitalCertificateResponse digitalCertificateResponse = null;
		DigitalCertificateRequest digitalCertificateRequest = request.getRequest();
		digitalCertificateResponse = partnerService.uploadDigitalCertificate(digitalCertificateRequest);
		response.setResponse(digitalCertificateResponse);
		response.setId(request.getId());
		response.setVersion(request.getVersion());
		response.setMetadata(request.getMetadata());
		return new ResponseEntity<ResponseWrapper<DigitalCertificateResponse>>(response , HttpStatus.OK);
	}*/
	
	/**
	 * This method is use for userLogin when need to validate the digital certificate 
	 * @param request this class contains LoginUserRequest
	 * @return loginUserResponse this class contains LoginUserResponse
	 * 
	 */
	
	@RequestMapping(value = "/loginUser", method = RequestMethod.POST)
	public ResponseEntity<ResponseWrapper<LoginUserResponse>> userLoginInKernal(
			@RequestBody RequestWrapper<LoginUserRequest> request){
		ResponseWrapper<LoginUserResponse> response = new ResponseWrapper<LoginUserResponse>();
		LoginUserResponse loginUserResponse=null;
		loginUserResponse = partnerService.userLoginInKernal(request);
		response.setResponse(loginUserResponse);
		response.setId(request.getId());
		response.setVersion(request.getVersion());
		response.setMetadata(request.getMetadata());
		return new ResponseEntity<ResponseWrapper<LoginUserResponse>>(response , HttpStatus.OK);
	}
	
	/**
	 * This API Use for signUser to get user signature and timestamp.
	 * @param request this class contains digitalCertificate details
	 * @return SignUserResponse this class contains signature and timestamp.
	 */
	
	@RequestMapping(value = "/signUser", method = RequestMethod.POST)
	public ResponseEntity<ResponseWrapper<SignUserResponse>> signUserInDigitalCertificates(
			@RequestBody RequestWrapper<SignUserRequest> request){
		ResponseWrapper<SignUserResponse> response = new ResponseWrapper<SignUserResponse>();
		SignUserResponse signUserResponse=null;
		signUserResponse = partnerService.signUserInDigitalCertificates(request);
		response.setResponse(signUserResponse);
		response.setId(request.getId());
		response.setVersion(request.getVersion());
		response.setMetadata(request.getMetadata());
		return new ResponseEntity<ResponseWrapper<SignUserResponse>>(response , HttpStatus.OK);
	}
}
