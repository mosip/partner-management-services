package io.mosip.pmp.partner.controller;

import javax.validation.Valid;


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
import io.mosip.pmp.partner.dto.DownloadPartnerAPIkeyResponse;
import io.mosip.pmp.partner.dto.PartnerAPIKeyRequest;
import io.mosip.pmp.partner.dto.PartnerAPIKeyResponse;
import io.mosip.pmp.partner.dto.PartnerRequest;
import io.mosip.pmp.partner.dto.PartnerResponse;
import io.mosip.pmp.partner.dto.PartnersRetrieveApiKeyRequests;
import io.mosip.pmp.partner.dto.RetrievePartnerDetailsResponse;
import io.mosip.pmp.partner.logger.Logger;
import io.mosip.pmp.partner.logger.PartnerServiceLogger;
import io.mosip.pmp.partner.service.PartnerService;

/**
 * @author sanjeev.shrivastava
 *
 */
@RestController
@RequestMapping(value = "/partners")
public class PartnerServiceController {
	
	private static final Logger LOGGER = PartnerServiceLogger.getLogger(PartnerServiceController.class);
	
	@Autowired
	PartnerService partnerService;
	
	/**
	 * This method is use for self registration by partner to create Auth/E-KYC Partners.
	 * @param partnerRequest.
	 * @return partnerResponse.
	 */
	
	@RequestMapping(value = "partnerReg", method = RequestMethod.POST)
	public ResponseEntity<ResponseWrapper<PartnerResponse>> partnerSelfRegistration(
			@RequestBody @Valid RequestWrapper<PartnerRequest> request) {
		LOGGER.info("sessionId", "idType", "id", "description");
		ResponseWrapper<PartnerResponse> response = new ResponseWrapper<PartnerResponse>();
		PartnerResponse partnerResponse = null;
		PartnerRequest partnerRequest = null;
		partnerRequest = request.getRequest();
		partnerResponse = partnerService.savePartner(partnerRequest);
		response.setResponse(partnerResponse);
		LOGGER.info("request", "idType", "id", "request");
		return new ResponseEntity<ResponseWrapper<PartnerResponse>>(response, HttpStatus.CREATED);
	}
	
	/**
	 * This API would be used to retrieve Auth/E-KYC Partner details
	 * @param partnerID
	 * @return retrievePartnerDetailsResponse.
	 */
	
	@RequestMapping(value = "/{partnerID}", method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<RetrievePartnerDetailsResponse>> retrievePartnerDetails(
			@PathVariable String partnerID) {

		ResponseWrapper<RetrievePartnerDetailsResponse> response = new ResponseWrapper<RetrievePartnerDetailsResponse>();
		RetrievePartnerDetailsResponse retrievePartnerDetailsResponse = null;
		retrievePartnerDetailsResponse = partnerService.getPartnerDetails(partnerID);
		response.setResponse(retrievePartnerDetailsResponse);
		return new ResponseEntity<ResponseWrapper<RetrievePartnerDetailsResponse>>(response, HttpStatus.OK);
	}

	
	/**
	 * @param request
	 * @param partnerID
	 * @return
	 */
	
	@RequestMapping(value = "/{partnerID}", method = RequestMethod.PUT)
	public ResponseEntity<ResponseWrapper<PartnerResponse>> updatePartnerDetails(
			@RequestBody @Valid RequestWrapper<PartnerRequest> request, @PathVariable String partnerID) {
		ResponseWrapper<PartnerResponse> response = new ResponseWrapper<PartnerResponse>();
		PartnerResponse partnerResponse = null;
		PartnerRequest partnerRequest = request.getRequest();
		partnerResponse = partnerService.updatePartnerDetail(partnerRequest, partnerID);
		response.setResponse(partnerResponse);
		return new ResponseEntity<ResponseWrapper<PartnerResponse>>(response, HttpStatus.OK);
	}

	/**
	 * This API would be used to submit Partner api key request.
	 * @param partnerID
	 * @param request
	 * @return partnerAPIKeyResponse.
	 */
	
	@RequestMapping(value = "/{partnerID}/submitPartApiKeyReq", method = RequestMethod.POST)
	public ResponseEntity<ResponseWrapper<PartnerAPIKeyResponse>> submitPartnerApiKeyRequest(
			@PathVariable String partnerID, 
			@RequestBody RequestWrapper<PartnerAPIKeyRequest> request) {
		ResponseWrapper<PartnerAPIKeyResponse> response = new ResponseWrapper<PartnerAPIKeyResponse>();
		PartnerAPIKeyResponse partnerAPIKeyResponse = null;
		PartnerAPIKeyRequest partnerAPIKeyRequest = request.getRequest();
		partnerAPIKeyResponse = partnerService.submitPartnerApiKeyReq(partnerAPIKeyRequest, partnerID);
		response.setResponse(partnerAPIKeyResponse);
		return new ResponseEntity<ResponseWrapper<PartnerAPIKeyResponse>>(response, HttpStatus.CREATED);
	}

	/**
	 *  This API would be used to download Partner API key for the given APIKeyReqID.
	 * @param partnerID
	 * @param aPIKeyReqID
	 * @return
	 */
	
	@RequestMapping(value = "/{partnerID}/partnerAPIKeyRequests/{aPIKeyReqID}", method = RequestMethod.POST)
	public ResponseEntity<ResponseWrapper<DownloadPartnerAPIkeyResponse>> downloadPartnerAPIkey(
			@PathVariable String partnerID, @PathVariable String aPIKeyReqID) {
		ResponseWrapper<DownloadPartnerAPIkeyResponse> response = new ResponseWrapper<DownloadPartnerAPIkeyResponse>();
		DownloadPartnerAPIkeyResponse resp = null;
		resp = partnerService.downloadPartnerAPIkey(partnerID, aPIKeyReqID);
		response.setResponse(resp);
		return new ResponseEntity<ResponseWrapper<DownloadPartnerAPIkeyResponse>>(response, HttpStatus.OK);
	}

	/**
	 * This API would be used to retrieve all API key requests submitted by partner.
	 * @param partnerID
	 * @return
	 */
	
	@RequestMapping(value = "/{partnerID}/partnerAPIKeyRequests", method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<PartnersRetrieveApiKeyRequests>> retrieveAllApiKeyRequestsSubmittedByPartnerTillDate(
			@PathVariable String partnerID) {
		ResponseWrapper<PartnersRetrieveApiKeyRequests> response = new ResponseWrapper<PartnersRetrieveApiKeyRequests>();
		PartnersRetrieveApiKeyRequests partnersRetrieveApiKeyRequests = null;
		partnersRetrieveApiKeyRequests = partnerService.retrieveAllApiKeyRequestsSubmittedByPartner(partnerID);
		response.setResponse(partnersRetrieveApiKeyRequests);
		return new ResponseEntity<ResponseWrapper<PartnersRetrieveApiKeyRequests>>(response, HttpStatus.OK);
	}

	/**
	 *  This API would be used to view API key request status and API key.
	 * @param partnerID.
	 * @param aPIKeyReqID.
	 * @return aPIkeyRequests.
	 */
	
	@RequestMapping(value = "/{partnerID}/partnerAPIKeyRequests/{aPIKeyReqID}", method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<APIkeyRequests>> viewApiKeyRequestStatusAndApiKey(
			@PathVariable String partnerID, @PathVariable String aPIKeyReqID) {
		ResponseWrapper<APIkeyRequests> response = new ResponseWrapper<APIkeyRequests>();
		APIkeyRequests aPIkeyRequests = null;
		aPIkeyRequests = partnerService.viewApiKeyRequestStatusApiKey(partnerID, aPIKeyReqID);
		response.setResponse(aPIkeyRequests);
		return new ResponseEntity<ResponseWrapper<APIkeyRequests>>(response, HttpStatus.OK);
	}

}
