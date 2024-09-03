package io.mosip.pms.partner.service;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.mosip.pms.common.dto.FilterValueDto;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.common.dto.PartnerPolicySearchResponseDto;
import io.mosip.pms.common.dto.PolicyRequestSearchResponseDto;
import io.mosip.pms.common.dto.SearchDto;
import io.mosip.pms.common.entity.PartnerType;
import io.mosip.pms.device.response.dto.FilterResponseCodeDto;
import io.mosip.pms.partner.dto.PartnerPolicyMappingResponseDto;
import io.mosip.pms.partner.request.dto.AddContactRequestDto;
import io.mosip.pms.partner.request.dto.CACertificateRequestDto;
import io.mosip.pms.partner.request.dto.ExtractorsDto;
import io.mosip.pms.partner.request.dto.PartnerPolicyMappingRequest;
import io.mosip.pms.partner.request.dto.PartnerCertDownloadRequestDto;
import io.mosip.pms.partner.request.dto.PartnerCertificateUploadRequestDto;
import io.mosip.pms.partner.request.dto.PartnerRequest;
import io.mosip.pms.partner.request.dto.PartnerRequestDto;
import io.mosip.pms.partner.request.dto.PartnerSearchDto;
import io.mosip.pms.partner.request.dto.PartnerUpdateDto;
import io.mosip.pms.partner.request.dto.PartnerUpdateRequest;
import io.mosip.pms.partner.response.dto.APIkeyRequests;
import io.mosip.pms.partner.response.dto.CACertificateResponseDto;
import io.mosip.pms.partner.response.dto.EmailVerificationResponseDto;
import io.mosip.pms.partner.response.dto.PartnerCertDownloadResponeDto;
import io.mosip.pms.partner.response.dto.PartnerCertificateResponseDto;
import io.mosip.pms.partner.response.dto.PartnerCredentialTypePolicyDto;
import io.mosip.pms.partner.response.dto.PartnerResponse;
import io.mosip.pms.partner.response.dto.PartnerSearchResponseDto;
import io.mosip.pms.partner.response.dto.RetrievePartnerDetailsResponse;
import io.mosip.pms.partner.response.dto.OriginalCertDownloadResponseDto;

public interface PartnerService {
	
	/** This method is used  for self registration by partner to create Auth/E-KYC Partners.
	 * @param request this class contains partner details
	 * @return partnerResponse this class contains status related to partner is registered successfully or not
	 */
	
	public PartnerResponse savePartner(PartnerRequest request);
	
	/** This method is used to retrieve Auth/E-KYC Partner details.
	 * @param partnerID this is unique id created after self registered by partner
	 * @return retrievePartnerDetailsResponse this class contains partner details
	 */
	public RetrievePartnerDetailsResponse getPartnerDetails(String partnerId);
	
	
	/**This method is used to update Auth/E-KYC Partner's details
	 * @param partnerUpdateRequest this class contains updated partner details
	 * @param partnerID this is unique id created after self registered by partner
	 * @return partnerResponse this class contains status of partner
	 */
	public PartnerResponse updatePartnerDetail(PartnerUpdateRequest partnerUpdateRequest,String partnerId);
	public PartnerResponse updatePartnerDetails(PartnerUpdateDto partnerUpdateRequest,String partnerId);
	
	/**
	 * This method is used to retrieve all API key requests submitted by partner till date.
	 * @param partnerID this is unique id created after self registered by partner
	 * @return partnersRetrieveApiKeyRequests this is a list of partner request for creation of partner API Key
	 */
	public List<APIkeyRequests> retrieveAllApiKeyRequestsSubmittedByPartner(String partnerId);
	
	/**
	 * 
	 * @param request
	 * @param partnerId
	 * @return
	 */
	public String createAndUpdateContactDetails(AddContactRequestDto request, String partnerId);
	
	/**
	 * Function to Upload CA/Sub-CA certificates
	 * 
	 * @param CACertificateRequestDto caCertResponseDto
	 * @return {@link CACertificateResponseDto} instance
	 * @throws IOException 
	 * @throws JsonProcessingException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
    public CACertificateResponseDto uploadCACertificate(CACertificateRequestDto caCertResponseDto) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException;

    /**
     * Function to Upload Partner certificates
     * 
     * @param PartnerCertificateUploadRequestDto partnerCertResponseDto
     * @return {@link PartnerCertificateResponseDto} instance
     * @throws IOException 
     * @throws JsonProcessingException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
    */
    public PartnerCertificateResponseDto uploadPartnerCertificate(PartnerCertificateUploadRequestDto partnerCertResponseDto) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException;

    /**
     * Function to Download Partner certificates
     * 
     * @param FTPChipCertDownloadRequestDto certDownloadRequestDto
     * @return {@link PartnerCertDownloadResponeDto} instance
     * @throws IOException 
     * @throws JsonProcessingException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
    */
    public PartnerCertDownloadResponeDto getPartnerCertificate(PartnerCertDownloadRequestDto certDownloadRequestDto) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException;

	/**
	 * Function to Download Original Partner certificates
	 *
	 * @param PartnerCertDownloadRequestDto certDownloadRequestDto
	 * @return {@link PartnerCertDownloadResponeDto} instance
	 * @throws IOException
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public OriginalCertDownloadResponseDto getOriginalPartnerCertificate(PartnerCertDownloadRequestDto certDownloadRequestDto) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException, CertificateException;

    /**
     * Function to add biometric extractors 
     * @param partnerId
     * @param policyId
     * @param extractors
     * @return
     */
    public String addBiometricExtractors(String partnerId, String policyId, ExtractorsDto extractors);
    
    /**
     * Function to get biometric extractors of partner and policy
     * @param partnerId
     * @param policyId
     * @return
     */
    public ExtractorsDto getBiometricExtractors(String partnerId, String policyId);
    
    /**
     * 
     * @param dto
     * @return
     */
	public PageResponseDto<PartnerSearchResponseDto> searchPartner(PartnerSearchDto dto);

	/**
	 * 
	 * @param dto
	 * @return
	 */
	public PageResponseDto<PartnerType> searchPartnerType(SearchDto dto);
	
	/**
	 * 
	 * @return
	 */
	public String mapPartnerPolicyCredentialType(String credentialType,String partnerId,String policyName);
	
	/**
	 * 
	 * @param credentialType
	 * @param partnerId
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public PartnerCredentialTypePolicyDto getPartnerCredentialTypePolicy(String credentialType,String partnerId) throws JsonParseException, JsonMappingException, IOException;
	
	/**
	 * 
	 * @param deviceFilterValueDto
	 * @return
	 */
	public FilterResponseCodeDto filterValues(FilterValueDto filterValueDto);
	
	/**
	 * 
	 * @param filterValueDto
	 * @return
	 */
	public FilterResponseCodeDto apiKeyRequestFilter(FilterValueDto filterValueDto);
	
	/**
	 * 
	 * @param dto
	 * @return
	 */
	public PageResponseDto<PartnerPolicySearchResponseDto> searchPartnerApiKeys(SearchDto dto);
	
	/**
	 * 
	 * @param dto
	 * @return
	 */
	public PageResponseDto<PolicyRequestSearchResponseDto> searchPartnerApiKeyRequests(SearchDto dto);
	
	/**
	 *  This one updates the policy group for a partner
	 * @param partnerId
	 * @param policyGroupId
	 * @return
	 */
	public String updatePolicyGroup(String partnerId, String policygroupName);
	
	/**
	 * This method will check weather a record exists with given email.
	 * @param emailId
	 * @return
	 */
	public EmailVerificationResponseDto isPartnerExistsWithEmail(String emailId);
	
	/**
	 * This method will request for policy and partner mapping
	 * @param partnerAPIKeyRequest
	 * @param partnerId
	 * @return
	 */
	public PartnerPolicyMappingResponseDto requestForPolicyMapping(PartnerPolicyMappingRequest partnerAPIKeyRequest,String partnerId);
	
	/**
	 * 	
	 * @param request
	 * @return
	 */
	public PartnerResponse registerPartner(PartnerRequestDto request);

}
