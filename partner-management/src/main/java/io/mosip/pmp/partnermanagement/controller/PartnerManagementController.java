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
import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingRequest;
import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingResponse;
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
			@RequestBody @Valid RequestWrapper<PartnersPolicyMappingRequest> request, @PathVariable String partnerID,
			@PathVariable String partnerAPIKey) {
		PartnersPolicyMappingRequest partnersPolicyMappingRequest = request.getRequest();
		ResponseWrapper<PartnersPolicyMappingResponse> response = new ResponseWrapper<PartnersPolicyMappingResponse>();
		response.setId(request.getId());
		response.setVersion(request.getVersion());
		PartnersPolicyMappingResponse partnersPolicyMappingResponse = partnerManagementService
				.partnerApiKeyPolicyMappings(partnersPolicyMappingRequest, partnerID, partnerAPIKey);
		response.setResponse(partnersPolicyMappingResponse);
		return new ResponseEntity<ResponseWrapper<PartnersPolicyMappingResponse>>(response, HttpStatus.CREATED);
	}

}
