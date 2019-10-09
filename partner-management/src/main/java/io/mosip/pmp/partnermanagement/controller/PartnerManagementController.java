package io.mosip.pmp.partnermanagement.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.pmp.partnermanagement.core.RequestWrapper;
import io.mosip.pmp.partnermanagement.core.ResponseWrapper;
import io.mosip.pmp.partnermanagement.dto.ActivateDeactivatePartnerRequest;
import io.mosip.pmp.partnermanagement.dto.PartnerAPIKeyToPolicyMappingsResponse;
import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingRequest;
import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingResponse;
import io.mosip.pmp.partnermanagement.dto.RetrievePartnerDetailsResponse;
import io.mosip.pmp.partnermanagement.dto.RetrievePartnersDetails;
import io.mosip.pmp.partnermanagement.service.PartnerManagementService;

/**
 * @author sanjeev.shrivastava
 *
 */

@RestController
@RequestMapping(value = "/management")
public class PartnerManagementController {

	@Autowired
	PartnerManagementService partnerManagementService;

	@RequestMapping(value = "/{partnerID}/partnerMappingRequest/{partnerAPIKey}", method = RequestMethod.POST)
	public ResponseEntity<ResponseWrapper<PartnersPolicyMappingResponse>> PartnerApiKeyToPolicyMappings(
			@RequestBody @Valid RequestWrapper<PartnersPolicyMappingRequest> request, 
			@PathVariable String partnerID,
			@PathVariable String partnerAPIKey) {
		PartnersPolicyMappingRequest partnersPolicyMappingRequest = request.getRequest();
		ResponseWrapper<PartnersPolicyMappingResponse> response = new ResponseWrapper<PartnersPolicyMappingResponse>();
		PartnersPolicyMappingResponse partnersPolicyMappingResponse=null;
		response.setId(request.getId());
		response.setVersion(request.getVersion());
		partnersPolicyMappingResponse = partnerManagementService
				.partnerApiKeyPolicyMappings(partnersPolicyMappingRequest, partnerID, partnerAPIKey);
		response.setResponse(partnersPolicyMappingResponse);
		return new ResponseEntity<ResponseWrapper<PartnersPolicyMappingResponse>>(response, HttpStatus.CREATED);
	}
	
	@RequestMapping(value = "/{partnerID}/statusRequest", method = RequestMethod.PUT)
	 public ResponseEntity<ResponseWrapper<PartnersPolicyMappingResponse>> activateDeactivateAuthEKYCPartners(
			 @PathVariable String partnerID,
			 @RequestBody @Valid RequestWrapper<ActivateDeactivatePartnerRequest> request){
		ResponseWrapper<PartnersPolicyMappingResponse> response = new ResponseWrapper<PartnersPolicyMappingResponse>();
		PartnersPolicyMappingResponse partnersPolicyMappingResponse = null;
		response.setId(request.getId());
		response.setVersion(request.getVersion());
		ActivateDeactivatePartnerRequest activateDeactivatePartnerRequest = request.getRequest();
		partnersPolicyMappingResponse = partnerManagementService
				.activateDeactivateAuthEKYCPartner(partnerID,activateDeactivatePartnerRequest);
		response.setResponse(partnersPolicyMappingResponse);
		return new ResponseEntity<ResponseWrapper<PartnersPolicyMappingResponse>>(response, HttpStatus.OK);
	 }
	
	@RequestMapping(value = "/{partnerID}/statusRequest/{PartnerAPIKey}", method = RequestMethod.PUT)
	public ResponseEntity<ResponseWrapper<PartnersPolicyMappingResponse>> activateDeactivatePartnerAPIKeyGivenPartner(
			@PathVariable String partnerID,
			@RequestBody @Valid RequestWrapper<ActivateDeactivatePartnerRequest> request,
			@PathVariable String PartnerAPIKey){
		ResponseWrapper<PartnersPolicyMappingResponse> response = new ResponseWrapper<PartnersPolicyMappingResponse>();
		PartnersPolicyMappingResponse partnersPolicyMappingResponse = null;
		response.setId(request.getId());
		response.setVersion(request.getVersion());
		ActivateDeactivatePartnerRequest activateDeactivatePartnerRequest = request.getRequest();
		partnersPolicyMappingResponse = partnerManagementService
				.activateDeactivatePartnerAPIKeyGivenPartner(partnerID,activateDeactivatePartnerRequest,PartnerAPIKey);
		response.setResponse(partnersPolicyMappingResponse);
		return new ResponseEntity<ResponseWrapper<PartnersPolicyMappingResponse>>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value="", method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<RetrievePartnerDetailsResponse>> getAllAuthEKYCPartnersForThePolicyGroup(){
		ResponseWrapper<RetrievePartnerDetailsResponse> response=new ResponseWrapper<RetrievePartnerDetailsResponse>();
		RetrievePartnerDetailsResponse retrievePartnerDetailsResponse = null;
		retrievePartnerDetailsResponse = partnerManagementService.getAllAuthEKYCPartnersForThePolicyGroup();
		response.setResponse(retrievePartnerDetailsResponse);
		return new ResponseEntity<ResponseWrapper<RetrievePartnerDetailsResponse>>(response, HttpStatus.OK);
	}
	
	@RequestMapping(value="/{partnerID}", method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<RetrievePartnersDetails>> getparticularAuthEKYCPartnerDetailsForGivenPartnerId(
			@PathVariable String partnerID){
		ResponseWrapper<RetrievePartnersDetails> response = new ResponseWrapper<RetrievePartnersDetails>();
		RetrievePartnersDetails retrievePartnersDetails = null;
		retrievePartnersDetails = partnerManagementService.getparticularAuthEKYCPartnerDetailsForGivenPartnerId(partnerID);
		response.setResponse(retrievePartnersDetails);
		return new ResponseEntity<ResponseWrapper<RetrievePartnersDetails>>(response , HttpStatus.OK);
	}
	
	@RequestMapping(value = "/{partnerID}/{PartnerAPIKey}" , method = RequestMethod.GET)
	public ResponseEntity<ResponseWrapper<PartnerAPIKeyToPolicyMappingsResponse>> getPartnerAPIKeyToPolicyMappings(
			@PathVariable String partnerID,
			@PathVariable String PartnerAPIKey){
		ResponseWrapper<PartnerAPIKeyToPolicyMappingsResponse> response = new ResponseWrapper<PartnerAPIKeyToPolicyMappingsResponse>();
		PartnerAPIKeyToPolicyMappingsResponse partnerAPIKeyToPolicyMappingsResponse = null;
		partnerAPIKeyToPolicyMappingsResponse = partnerManagementService.getPartnerAPIKeyToPolicyMapping(partnerID,PartnerAPIKey);
		response.setResponse(partnerAPIKeyToPolicyMappingsResponse);
		return new ResponseEntity<ResponseWrapper<PartnerAPIKeyToPolicyMappingsResponse>>(response , HttpStatus.OK);
	}
}
