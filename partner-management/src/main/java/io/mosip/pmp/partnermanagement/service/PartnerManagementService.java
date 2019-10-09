package io.mosip.pmp.partnermanagement.service;

import io.mosip.pmp.partnermanagement.dto.ActivateDeactivatePartnerRequest;
import io.mosip.pmp.partnermanagement.dto.PartnerAPIKeyToPolicyMappingsResponse;
import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingRequest;
import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingResponse;
import io.mosip.pmp.partnermanagement.dto.RetrievePartnerDetailsResponse;
import io.mosip.pmp.partnermanagement.dto.RetrievePartnersDetails;

/**
 * @author sanjeev.shrivastava
 *
 */

public interface PartnerManagementService {

	public PartnersPolicyMappingResponse partnerApiKeyPolicyMappings(PartnersPolicyMappingRequest request,
			String partnerID, String partnerAPIKey);
	public PartnersPolicyMappingResponse activateDeactivateAuthEKYCPartner(String partnerID, 
			ActivateDeactivatePartnerRequest request);
	public PartnersPolicyMappingResponse activateDeactivatePartnerAPIKeyGivenPartner(String partnerID, 
			ActivateDeactivatePartnerRequest request, String partnerAPIKey);
	public RetrievePartnerDetailsResponse getAllAuthEKYCPartnersForThePolicyGroup();
	public RetrievePartnersDetails getparticularAuthEKYCPartnerDetailsForGivenPartnerId(String partnerID);
	public PartnerAPIKeyToPolicyMappingsResponse getPartnerAPIKeyToPolicyMapping(String partnerID, String PartnerAPIKey);
}
