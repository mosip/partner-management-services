package io.mosip.pms.partner.manager.service;

import java.util.List;
import java.util.Optional;

import io.mosip.pms.partner.manager.dto.StatusRequestDto;
import io.mosip.pms.partner.manager.dto.ApikeyRequests;
import io.mosip.pms.partner.manager.dto.PartnerAPIKeyToPolicyMappingsResponse;
import io.mosip.pms.partner.manager.dto.PartnerPolicyResponse;
import io.mosip.pms.partner.manager.dto.PartnersPolicyMappingRequest;
import io.mosip.pms.partner.manager.dto.PartnersPolicyMappingResponse;
import io.mosip.pms.partner.manager.dto.RetrievePartnerDetailsResponse;
import io.mosip.pms.partner.request.dto.APIKeyGenerateRequestDto;
import io.mosip.pms.partner.request.dto.APIkeyStatusUpdateRequestDto;
import io.mosip.pms.partner.response.dto.APIKeyGenerateResponseDto;

public interface PartnerManagerService {

	/**
	 * 
	 * @param request
	 * @param partnerID
	 * @param partnerAPIKey
	 * @return
	 */
	public PartnersPolicyMappingResponse updatePolicyAgainstApikey(PartnersPolicyMappingRequest request,
			String partnerID, String partnerApikey);
	
	/**
	 * 
	 * @param partnerID
	 * @param request
	 * @return
	 */
	public PartnersPolicyMappingResponse activateDeactivateAuthEKYCPartner(String partnerID, 
			StatusRequestDto request);
	
	/**
	 * 
	 * @param partnerID
	 * @param request
	 * @param partnerAPIKey
	 * @return
	 */
	public PartnersPolicyMappingResponse activateDeactivatePartnerAPIKeyGivenPartner(String partnerID, 
			StatusRequestDto request, String partnerAPIKey);
	
	/**
	 * 
	 * @param partnerType
	 * @return
	 */
	public RetrievePartnerDetailsResponse getAllAuthEKYCPartnersForThePolicyGroup(Optional<String> partnerType);	
	
	/**
	 * 
	 * @param partnerID
	 * @param partnerAPIKey
	 * @return
	 */
	public PartnerAPIKeyToPolicyMappingsResponse getPartnerAPIKeyToPolicyMapping(String partnerID, String partnerAPIKey);

	/**
	 * 
	 * @return
	 */
	public List<ApikeyRequests> getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagers();	
	
	/**
	 * 
	 * @param apiKeyReqID
	 * @return
	 */
	public ApikeyRequests getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestId(String apiKeyReqID);
	
	/**
	 * 
	 * @param request
	 * @param partnerAPIKey
	 * @return
	 */
	public PartnersPolicyMappingResponse approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(
			StatusRequestDto request, String partnerAPIKey);

	/**
	 * 
	 * @param mispLicenseKey
	 * @param policy_api_key
	 * @param partnerId
	 * @param needPartnerCert
	 * @return
	 */
	public PartnerPolicyResponse getPartnerMappedPolicyFile(String mispLicenseKey,String policy_api_key, String partnerId,boolean needPartnerCert);
	
	/**
	 * This method will generate apikey for approved partner policy mapping
	 * @param partnerId
	 * @return
	 */
	public APIKeyGenerateResponseDto generateAPIKey(String partnerId, APIKeyGenerateRequestDto requestDto);	
	
	/**
	 * 
	 * @param mappingkey
	 * @param statusRequest
	 * @return
	 */
	public String approveRejectPartnerPolicyMapping(String mappingkey, StatusRequestDto statusRequest);
	
	/**
	 * 
	 * @param partnerId
	 * @param policyId
	 * @param label
	 * @return
	 */
	public String updateAPIKeyStatus(String partnerId, String policyId, APIkeyStatusUpdateRequestDto request);

}
