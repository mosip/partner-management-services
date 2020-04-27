package io.mosip.pmp.partnermanagement.service;

import java.util.List;

import io.mosip.pmp.partnermanagement.dto.ActivateDeactivatePartnerRequest;
import io.mosip.pmp.partnermanagement.dto.ApikeyRequests;
import io.mosip.pmp.partnermanagement.dto.PartnerAPIKeyToPolicyMappingsResponse;
import io.mosip.pmp.partnermanagement.dto.PartnerPolicyResponse;
import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingRequest;
import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingResponse;
import io.mosip.pmp.partnermanagement.dto.PolicyIDResponse;
import io.mosip.pmp.partnermanagement.dto.RetrievePartnerDetailsResponse;
import io.mosip.pmp.partnermanagement.dto.RetrievePartnerManagers;
import io.mosip.pmp.partnermanagement.dto.RetrievePartnersDetails;

/**
 * This interface provides the methods which can be used for PartnerManagementService.
 * @author sanjeev.shrivastava
 *
 */

public interface PartnerManagementService {

	/**
	 * This method would be used by partner Manager, to update Partner api key to Policy Mappings.
	 * @param request  this class cintains oldPolicyID and newPolicyID
	 * @param partnerID this is unique id created after self registered by partner
	 * @param partnerAPIKey this is unique id created by partner manager at the time of approving partner request
	 * @return partnersPolicyMappingResponse this class contains massage about API key created successfully
	 */
	public PartnersPolicyMappingResponse partnerApiKeyPolicyMappings(PartnersPolicyMappingRequest request,
			String partnerID, String partnerAPIKey);
	
	/**
	 * This method would be used to activate/deactivate Auth/E-KYC Partners.
	 * @param partnerID this is unique id created after self registered by partner
	 * @param request this class contains the status of activate/deactivate Auth/E-KYC Partners
	 * @return partnersPolicyMappingResponse this class contains massage about Partner status updated successfully
	 */
	public PartnersPolicyMappingResponse activateDeactivateAuthEKYCPartner(String partnerID, 
			ActivateDeactivatePartnerRequest request);
	
	/**
	 * Partner Manager would be using this method to activate OR de-activate PartnerAPIKey for given partner.
	 * @param partnerID this is unique id created after self registered by partner
	 * @param request this class contains the status about activate OR de-activate PartnerAPIKey for given partner
	 * @param partnerAPIKey this is unique id created by partner manager at the time of approving partner request
	 * @return partnersPolicyMappingResponse this class contains massage about Partner API Key status updated successfully
	 */
	public PartnersPolicyMappingResponse activateDeactivatePartnerAPIKeyGivenPartner(String partnerID, 
			ActivateDeactivatePartnerRequest request, String partnerAPIKey);
	
	/**
	 * This method would be used to retrieve all Auth/E-KYC Partners for the policy group.
	 * @return retrievePartnerDetailsResponse this class contains list of Auth/E-KYC Partners for the policy group
	 */
	public RetrievePartnerDetailsResponse getAllAuthEKYCPartnersForThePolicyGroup();
	
	/**
	 * This method would be used to retrieve the particular Auth/E-KYC Partner details for given partner id.
	 * @param partnerID this is unique id created after self registered by partner
	 * @return retrievePartnersDetails this class contains Auth/E-KYC Partner details for given partner id
	 */
	
	public RetrievePartnersDetails getparticularAuthEKYCPartnerDetailsForGivenPartnerId(String partnerID);
	
	/**
	 *  Partner managers would be using this request to retrieve the Partner API key
	 * to Policy Mappings. Partner management system would be able to validate
	 * Partner API Key pattern, validate expiry for Partner API Key and status
	 * details in background, while fetching Policy to Partner API mappings.

	 * @param partnerID this is unique id created after self registered by partner
	 * @param PartnerAPIKey this is unique id created by partner manager at the time of approving partner request
	 * @return partnerAPIKeyToPolicyMappingsResponse this class contains partnerID and policyId
	 */
	public PartnerAPIKeyToPolicyMappingsResponse getPartnerAPIKeyToPolicyMapping(String partnerID, String partnerAPIKey);
	
	/**
	 * This method would be used to retrieve all Partner API Key requests as received by partner manager.
	 * @return this class contains list of Partner API Key requests as received by partner manager
	 */
	public List<ApikeyRequests> getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagers();
	
	/**
	 * This method would be used to retrieve the request for Partner API key to Policy Mappings for given request id.
	 * @param APIKeyReqID this is unique id created after partner request for Partner API Key
	 * @return apikeyRequests this class contains details relared to Partner API key to Policy Mappings
	 */
	public ApikeyRequests getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestId(String apiKeyReqID);
	
	/**
	 *  Partner Manager would be using this API to approve OR reject partner API key
	 * requests based on API key request id. During approval process of the request
	 * unique PartnerAPI Key is generated in Partner Management module, which is
	 * mapped to requested policies. Partner API Key would be having default active
	 * status, expiry of which would configurable.
	 * 
	 * @param request this class contains the status about approve OR reject partner API key requests
	 * @param partnerAPIKey this is unique id created after partner request for Partner API Key
	 * @return partnersPolicyMappingResponse this class contains massage about PartnerAPIKey approved successfully
	 */
	public PartnersPolicyMappingResponse approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(
			ActivateDeactivatePartnerRequest request, String partnerAPIKey);
	
	/**
	 * 
	 */

	public PartnerPolicyResponse getPartnerMappedPolicyFile(String mispLicenseKey,String policy_api_key, String partnerId);
	/**
	 * @return retrievePartnerManagers
	 */
	
	public RetrievePartnerManagers getPartnerManager();
	
	/**
	 * @param policyName 
	 * @return policyIDResponse
	 */
	
	public PolicyIDResponse getPartnerPolicyID(String policyName);
}
