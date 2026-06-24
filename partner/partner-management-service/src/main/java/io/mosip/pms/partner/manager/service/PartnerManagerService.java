package io.mosip.pms.partner.manager.service;

import java.util.List;
import java.util.Optional;

import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.dto.TrustCertificateSummaryDto;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.partner.manager.dto.TrustCertificateFilterDto;
import io.mosip.pms.partner.manager.dto.*;
import io.mosip.pms.partner.request.dto.*;
import io.mosip.pms.partner.request.dto.APIKeyGenerateRequestDto;
import io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto;
import io.mosip.pms.partner.request.dto.APIkeyStatusUpdateRequestDto;
import io.mosip.pms.partner.request.dto.BioExtractorsRequestDto;
import io.mosip.pms.partner.request.dto.BioextractorConfigurationDeleteRequestDto;
import io.mosip.pms.partner.request.dto.BioextractorConfigurationRequestDto;
import io.mosip.pms.partner.request.dto.LinkPolicyGroupRequestDto;
import io.mosip.pms.partner.request.dto.LinkPolicyGroupResponseDto;
import io.mosip.pms.partner.response.dto.APIKeyUpdateResponseDto;
import io.mosip.pms.partner.response.dto.APIKeyGenerateResponseDto;
import io.mosip.pms.partner.response.dto.BioextractorConfigurationDetailDto;
import io.mosip.pms.partner.response.dto.BioextractorConfigurationResponseDto;
import io.mosip.pms.partner.response.dto.BioExtractorsResponseWrapperV2;
import io.mosip.pms.partner.response.dto.CredentialTypesResponseWrapperV2;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

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
	 * Submit bio-extractor request rows for a partner policy request.
	 * `@param` requestId business request id (req_id)
	 * `@param` request bio-extractor payload
	 * `@return` submission result identifier/message
	 */
	String submitBioExtractorsRequest(String requestId, BioExtractorsRequestDto request);

	BioExtractorsResponseWrapperV2 getPartnerPolicyRequestBioExtractors(String requestId);

	/**
	 * Fetch credential type request row (if any) submitted for a partner policy request.
	 *
	 * @param requestId business request id (req_id)
	 * @return credential type request row
	 */
	CredentialTypesResponseWrapperV2 getPartnerPolicyRequestCredentialTypes(String requestId);
	String submitCredentialTypesRequest(String requestId, CredentialTypeRequestDto request);

	/**
	 * 
	 * @param partnerId
	 * @param policyId
	 * @param label
	 * @return
	 */
	public String updateAPIKeyStatus(String partnerId, String policyId, APIkeyStatusUpdateRequestDto request);

	/**
	 * 
	 * @param partnerType
	 * @return
	 */
	public PartnerDetailsResponse getPartners(Optional<String> partnerType);

	public ResponseWrapperV2<PartnerDetailsV3Dto> getPartnerDetails(String partnerId);

	public ResponseWrapperV2<AdminPartnerDetailsDto> getPartnerDetailsV2(String partnerId);

	public ResponseWrapperV2<PageResponseV2Dto<PartnerSummaryDto>> getAdminPartners(String sortFieldName, String sortType, Integer pageNo, Integer pageSize, PartnerFilterDto partnerFilterDto);

	public ResponseWrapperV2<PageResponseV2Dto<PartnerSummaryV2Dto>> getAdminPartnersV2(String sortFieldName, String sortType, Integer pageNo, Integer pageSize, PartnerFilterDto partnerFilterDto);

	public ResponseWrapperV2<PageResponseV2Dto<PartnerPolicyRequestSummaryDto>> getAllPartnerPolicyRequests(String sortFieldName, String sortType, Integer pageNo, Integer pageSize, PartnerPolicyRequestFilterDto filterDto);

	public ResponseWrapperV2<PageResponseV2Dto<ApiKeyRequestSummaryDto>> getAllApiKeyRequests(String sortFieldName, String sortType, Integer pageNo, Integer pageSize, ApiKeyFilterDto filterDto);

	public ResponseWrapperV2<PageResponseV2Dto<ApiKeyRequestSummaryV2Dto>> getAllApiKeyRequestsV2(String sortFieldName, String sortType, Integer pageNo, Integer pageSize, ApiKeyFilterDto filterDto);

	public ResponseWrapperV2<PageResponseV2Dto<TrustCertificateSummaryDto>> getTrustCertificates(String sortFieldName, String sortType, Integer pageNo, Integer pageSize, TrustCertificateFilterDto filterDto);

	public ResponseWrapperV2<TrustCertificateResponseDto> downloadTrustCertificates(String certificateId);

    ResponseWrapperV2<LinkPolicyGroupResponseDto> linkPolicyGroup(String partnerId, @NotNull @Valid LinkPolicyGroupRequestDto request);

	ResponseWrapperV2<APIKeyUpdateResponseDto> updateAPIKey(String partnerId, String policyId, String apiKeyName, @NotNull @Valid APIKeyUpdateRequestDto request);

	ResponseWrapperV2<BioextractorConfigurationResponseDto> createBioextractorConfiguration(BioextractorConfigurationRequestDto request);

	ResponseWrapperV2<PageResponseV2Dto<BioextractorConfigurationDetailDto>> getBioextractorConfigurations(String sortFieldName, String sortType, Integer pageNo, Integer pageSize, BioextractorConfigurationFilterDto filterDto);

	ResponseWrapperV2<BioextractorConfigurationDetailDto> getBioextractorConfigurationById(String bioExtractorConfigurationId);
	
	ResponseWrapperV2<BioextractorConfigurationResponseDto> deleteBioextractorConfiguration(
			String bioExtractorConfigurationId, BioextractorConfigurationDeleteRequestDto request);

}
