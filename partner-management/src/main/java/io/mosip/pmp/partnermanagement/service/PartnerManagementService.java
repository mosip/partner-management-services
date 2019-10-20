package io.mosip.pmp.partnermanagement.service;

import java.util.List;

import io.mosip.pmp.partnermanagement.dto.ActivateDeactivatePartnerRequest;
import io.mosip.pmp.partnermanagement.dto.ApikeyRequests;
import io.mosip.pmp.partnermanagement.dto.PartnerAPIKeyToPolicyMappingsResponse;
import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingRequest;
import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingResponse;
import io.mosip.pmp.partnermanagement.dto.RetrievePartnerDetailsResponse;
import io.mosip.pmp.partnermanagement.dto.RetrievePartnersDetails;

/**
 * This interface provides the methods which can be used for PartnerManagementService.
 * @author sanjeev.shrivastava
 *
 */

public interface PartnerManagementService {

	/**
	 * This method would be used by partner Manager, to update Partner api key to Policy Mappings.
	 * @param request  contains partners details.
	 * @param partnerID is a unique id for partners.
	 * @param partnerAPIKey is the key for partner.
	 * @return partnersPolicyMappingResponse.
	 */
	public PartnersPolicyMappingResponse partnerApiKeyPolicyMappings(PartnersPolicyMappingRequest request,
			String partnerID, String partnerAPIKey);
	
	/**
	 * This method would be used to activate/deactivate Auth/E-KYC Partners.
	 * @param partnerID
	 * @param request 
	 * @return partnersPolicyMappingResponse.
	 */
	public PartnersPolicyMappingResponse activateDeactivateAuthEKYCPartner(String partnerID, 
			ActivateDeactivatePartnerRequest request);
	
	/**
	 * Partner Manager would be using this method to activate OR de-activate PartnerAPIKey for given partner.
	 * @param partnerID
	 * @param request
	 * @param partnerAPIKey
	 * @return partnersPolicyMappingResponse.
	 */
	public PartnersPolicyMappingResponse activateDeactivatePartnerAPIKeyGivenPartner(String partnerID, 
			ActivateDeactivatePartnerRequest request, String partnerAPIKey);
	
	/**
	 * This method would be used to retrieve all Auth/E-KYC Partners for the policy group.
	 * @return retrievePartnerDetailsResponse.
	 */
	public RetrievePartnerDetailsResponse getAllAuthEKYCPartnersForThePolicyGroup();
	
	/**
	 * This method would be used to retrieve the particular Auth/E-KYC Partner details for given partner id.
	 * @param partnerID
	 * @return retrievePartnersDetails.
	 */
	
	public RetrievePartnersDetails getparticularAuthEKYCPartnerDetailsForGivenPartnerId(String partnerID);
	
	/**
	 *  Partner managers would be using this request to retrieve the Partner API key
	 * to Policy Mappings. Partner management system would be able to validate
	 * Partner API Key pattern, validate expiry for Partner API Key and status
	 * details in background, while fetching Policy to Partner API mappings.

	 * @param partnerID
	 * @param PartnerAPIKey
	 * @return partnerAPIKeyToPolicyMappingsResponse.
	 */
	public PartnerAPIKeyToPolicyMappingsResponse getPartnerAPIKeyToPolicyMapping(String partnerID, String PartnerAPIKey);
	
	/**
	 * This method would be used to retrieve all Partner API Key requests as received by partner manager.
	 * @return List<ApikeyRequests>.
	 */
	public List<ApikeyRequests> getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagers();
	
	/**
	 * This method would be used to retrieve the request for Partner API key to Policy Mappings for given request id.
	 * @param APIKeyReqID
	 * @return apikeyRequests.
	 */
	public ApikeyRequests getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestId(String APIKeyReqID);
	
	/**
	 *  Partner Manager would be using this API to approve OR reject partner API key
	 * requests based on API key request id. During approval process of the request
	 * unique PartnerAPI Key is generated in Partner Management module, which is
	 * mapped to requested policies. Partner API Key would be having default active
	 * status, expiry of which would configurable.
	 * 
	 * @param request
	 * @param partnerAPIKey
	 * @return partnersPolicyMappingResponse.
	 */
	public PartnersPolicyMappingResponse approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(
			ActivateDeactivatePartnerRequest request, String partnerAPIKey);
}
