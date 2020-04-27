package io.mosip.pmp.partner.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import io.mosip.pmp.partner.core.RequestWrapper;
import io.mosip.pmp.partner.core.ResponseWrapper;
import io.mosip.pmp.partner.dto.APIkeyRequests;
import io.mosip.pmp.partner.dto.DigitalCertificateRequest;
import io.mosip.pmp.partner.dto.DigitalCertificateResponse;
import io.mosip.pmp.partner.dto.PartnerAPIKeyRequest;
import io.mosip.pmp.partner.dto.PartnerAPIKeyResponse;
import io.mosip.pmp.partner.dto.PartnerRequest;
import io.mosip.pmp.partner.dto.PartnerResponse;
import io.mosip.pmp.partner.dto.PartnerUpdateRequest;
import io.mosip.pmp.partner.dto.RetrievePartnerDetailsResponse;
import io.mosip.pmp.partner.service.PartnerService;
import io.swagger.annotations.ApiOperation;

/**
 * <p>
 * This is the MOSIP Partner Service controller. This defines all the necessary
 * operations
 * </p>
 * <p>
 * required for Partner
 * </p>
 * . Partner Service Controller is having following operations 1. Partner
 * SelfRegistration {{@link #partnerSelfRegistration(RequestWrapper)} 2.
 * Retrieve PartnerDetails {{@link #retrievePartnerDetails(String)} 3. Update
 * PartnerDetails {{@link #updatePartnerDetails(RequestWrapper, String)} 4.
 * Submitting Partner API Key
 * Request{{@link #submitPartnerApiKeyRequest(String, RequestWrapper)} 5.
 * Downloading Partner API Key {{@link #downloadPartnerAPIkey(String, String)}
 * 6. Retrieve All ApiKeyRequest Submitted By Partner Till Date
 * {{@link #retrieveAllApiKeyRequestsSubmittedByPartnerTillDate(String)} 7. View
 * ApiKeyRequest Status And ApiKey
 * {{@link #viewApiKeyRequestStatusAndApiKey(String, String)}
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

	String msg = "mosip.partnermanagement.partners.retrieve";
	String version = "1.0";
	
	/**
	 * This API would be used for self registration by partner to create Auth/E-KYC
	 * Partners. Partner Management module would be integrating with Kernel IAM
	 * module for generation of user id and password for partners.
	 * 
	 * @param request
	 *            this class contains partner details
	 * @return response this class contains partner response
	 */
	@PreAuthorize("hasAnyRole('PARTNER')")
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<ResponseWrapper<PartnerResponse>> partnerSelfRegistration(
			@RequestBody @Valid RequestWrapper<PartnerRequest> request) {
		LOGGER.info("partner self registration");
		ResponseWrapper<PartnerResponse> response = new ResponseWrapper<>();
		PartnerResponse partnerResponse = null;
		PartnerRequest partnerRequest = null;
		partnerRequest = request.getRequest();
		LOGGER.info("calling savePartner method");
		partnerResponse = partnerService.savePartner(partnerRequest);
		LOGGER.info(partnerResponse + " : response of savePartner method");
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
	@PreAuthorize("hasAnyRole('PARTNER')")
	@RequestMapping(value = "/{partnerId}/partnerAPIKeyRequests", method = RequestMethod.PATCH)
	public ResponseEntity<ResponseWrapper<PartnerAPIKeyResponse>> submitPartnerApiKeyRequest(
			@PathVariable String partnerId, @RequestBody @Valid RequestWrapper<PartnerAPIKeyRequest> request) {
		ResponseWrapper<PartnerAPIKeyResponse> response = new ResponseWrapper<>();
		PartnerAPIKeyResponse partnerAPIKeyResponse = null;
		PartnerAPIKeyRequest partnerAPIKeyRequest = request.getRequest();
		partnerAPIKeyResponse = partnerService.submitPartnerApiKeyReq(partnerAPIKeyRequest, partnerId);
		response.setId(request.getId());
		response.setVersion(request.getVersion());
		response.setResponse(partnerAPIKeyResponse);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}	
	
	/**
	 * Validation of digital certificate without PublicKey
	 * 
	 * Partners would be procuring digital certificates from Certification Authority
	 * (CA), And upload the same to Partner Management using this API. Partner
	 * Management would depend on Kernel to manage partner certificates, Validation
	 * of partner certificates. Appropriate error messages would be sent back to
	 * Partners, In cases where digital certificates expires, certificate validation
	 * error happens.
	 * 
	 * @param request
	 *            this class contains digitalCertificate details
	 * @return DigitalCertificateResponse this class contains massage
	 */
	@PreAuthorize("hasAnyRole('PARTNER')")
	@RequestMapping(value = "/digitalcertificate", method = RequestMethod.POST)
	public ResponseEntity<ResponseWrapper<DigitalCertificateResponse>> validateDigitalCertificatewithoutPublicKey(
			@RequestBody RequestWrapper<DigitalCertificateRequest> request) {
		ResponseWrapper<DigitalCertificateResponse> response = new ResponseWrapper<>();
		DigitalCertificateResponse digitalCertificateResponse = null;
		digitalCertificateResponse = partnerService.uploadDigitalCertificate(request);
		response.setResponse(digitalCertificateResponse);
		response.setId(request.getId());
		response.setVersion(request.getVersion());
		response.setMetadata(request.getMetadata());
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
	@PreAuthorize("hasAnyRole('PARTNER')")
	@RequestMapping(value = "/{partnerId}", method = RequestMethod.PUT)
	public ResponseEntity<ResponseWrapper<PartnerResponse>> updatePartnerDetails(
			@RequestBody @Valid RequestWrapper<PartnerUpdateRequest> request, @PathVariable String partnerId) {
		ResponseWrapper<PartnerResponse> response = new ResponseWrapper<>();
		PartnerResponse partnerResponse = null;
		PartnerUpdateRequest partnerRequest = request.getRequest();
		partnerResponse = partnerService.updatePartnerDetail(partnerRequest, partnerId);
		response.setId(request.getId());
		response.setVersion(request.getVersion());
		response.setResponse(partnerResponse);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	/**
	 * Validation of digital certificate with PublicKey
	 * 
	 * As the MOSIP system Partner Management module would integrate with Kernel for
	 * validation of partner's digital certificate. In case where MOSIP would act as
	 * certification authority for partners, MOSIP would be able to sign and resign
	 * partner digital certificates. Partner management module would depend on
	 * Kernel services for signing and re-signing of partner digital certificates.
	 * Kernel Signature service would be utilized to validate signature : Kernel
	 * Signature Service
	 * 
	 * @param request
	 *            this class contains digitalCertificate details
	 * @return DigitalCertificateResponse this class contains massage
	 */
	@PreAuthorize("hasAnyRole('PARTNER')")
	@RequestMapping(value = "/digitalcertificate", method = RequestMethod.PUT)
	public ResponseEntity<ResponseWrapper<DigitalCertificateResponse>> validateDigitalCertificateWithPublicKey(
			@RequestBody RequestWrapper<DigitalCertificateRequest> request) {
		ResponseWrapper<DigitalCertificateResponse> response = new ResponseWrapper<>();
		DigitalCertificateResponse digitalCertificateResponse = null;
		digitalCertificateResponse = partnerService.validateDigitalCertificate(request);
		response.setId(request.getId());
		response.setVersion(request.getVersion());
		response.setResponse(digitalCertificateResponse);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	/**
	 * This API would be used to retrieve Auth/E-KYC Partner details
	 * 
	 * @param partnerId
	 *            this is unique id created after self registered by partner
	 * @return retrievePartnerDetailsResponse this class contains partner details
	 */
	@PreAuthorize("hasAnyRole('PARTNER')")
	@RequestMapping(value = "/{partnerId}", method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<RetrievePartnerDetailsResponse>> retrievePartnerDetails(
			@PathVariable String partnerId) {
		ResponseWrapper<RetrievePartnerDetailsResponse> response = new ResponseWrapper<>();
		RetrievePartnerDetailsResponse retrievePartnerDetailsResponse = null;
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
	@PreAuthorize("hasAnyRole('PARTNER')")
	@RequestMapping(value = "/{partnerId}/partnerAPIKeyRequests", method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<List<APIkeyRequests>>> retrieveAllApiKeyRequestsSubmittedByPartnerTillDate(
			@PathVariable String partnerId) {
		ResponseWrapper<List<APIkeyRequests>> response = new ResponseWrapper<>();
		List<APIkeyRequests> apikeyRequestsList = null;
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
	 * @param apiKeyReqId
	 *            this is unique id created after partner request for Partner API
	 *            Key
	 * @return response this class contains partnerApiKey apiKeyRequestStatus and
	 *         validity details
	 */
	@PreAuthorize("hasAnyRole('PARTNER')")
	@RequestMapping(value = "/{partnerId}/partnerAPIKeyRequests/{apiKeyReqId}", method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<APIkeyRequests>> viewApiKeyRequestStatusAndApiKey(
			@PathVariable String partnerId, @PathVariable String apiKeyReqId) {
		ResponseWrapper<APIkeyRequests> response = new ResponseWrapper<>();
		APIkeyRequests aPIkeyRequests = null;
		aPIkeyRequests = partnerService.viewApiKeyRequestStatusApiKey(partnerId, apiKeyReqId);
		response.setId(msg);
		response.setVersion(version);
		response.setResponse(aPIkeyRequests);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}		
}
