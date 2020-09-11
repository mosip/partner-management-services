package io.mosip.pmp.partner.service;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.mosip.pmp.partner.dto.APIkeyRequests;
import io.mosip.pmp.partner.dto.AddContactRequestDto;
import io.mosip.pmp.partner.dto.CACertificateRequestDto;
import io.mosip.pmp.partner.dto.CACertificateResponseDto;
import io.mosip.pmp.partner.dto.DownloadPartnerAPIkeyResponse;
import io.mosip.pmp.partner.dto.PartnerAPIKeyRequest;
import io.mosip.pmp.partner.dto.PartnerAPIKeyResponse;
import io.mosip.pmp.partner.dto.PartnerCertDownloadRequestDto;
import io.mosip.pmp.partner.dto.PartnerCertDownloadResponeDto;
import io.mosip.pmp.partner.dto.PartnerCertificateRequestDto;
import io.mosip.pmp.partner.dto.PartnerCertificateResponseDto;
import io.mosip.pmp.partner.dto.PartnerRequest;
import io.mosip.pmp.partner.dto.PartnerResponse;
import io.mosip.pmp.partner.dto.PartnerUpdateRequest;
import io.mosip.pmp.partner.dto.PolicyIdResponse;
import io.mosip.pmp.partner.dto.RetrievePartnerDetailsResponse;
import io.mosip.pmp.partner.dto.RetrievePartnerDetailsWithNameResponse;

/**
 * This interface provides the methods which can be used for PartnerService
 * @author sanjeev.shrivastava
 *
 */

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
	public RetrievePartnerDetailsResponse getPartnerDetails(String partnerID);
	
	/**
	 * This API would be used to retrieve Partner details by Partner Name
	 * @param partnerName this is unique Partner Name
	 * @return retrievePartnerDetailsWithNameResponse this class contains partner details
	 */
	public RetrievePartnerDetailsWithNameResponse getPartnerDetailsWithName(String partnerName);
	
	/**This method is used to update Auth/E-KYC Partner's details
	 * @param partnerUpdateRequest this class contains updated partner details
	 * @param partnerID this is unique id created after self registered by partner
	 * @return partnerResponse this class contains status of partner
	 */
	public PartnerResponse updatePartnerDetail(PartnerUpdateRequest partnerUpdateRequest,String partnerID);
	
	/**
	 * This method is used to submit Partner api key request.
	 * @param partnerAPIKeyRequest this class contains partner policy and policy description details 
	 * @param partnerID this is unique id created after self registered by partner
	 * @return partnerAPIKeyResponse this class contains partner request id and massage details
	 */
	public PartnerAPIKeyResponse submitPartnerApiKeyReq(PartnerAPIKeyRequest partnerAPIKeyRequest,String partnerID);
	
	/**
	 * This method is used to download Partner API key for the given APIKeyReqID.
	 * @param partnerID this is unique id created after self registered by partner
	 * @param aPIKeyReqID this is unique id created after partner request for Partner API Key
	 * @return downloadPartnerAPIkeyResponse this is unique id created once partner manager approved the partner API request
	 */
	
	public DownloadPartnerAPIkeyResponse downloadPartnerAPIkey(String partnerID,String aPIKeyReqID);
	
	/**
	 * This method is used to retrieve all API key requests submitted by partner till date.
	 * @param partnerID this is unique id created after self registered by partner
	 * @return partnersRetrieveApiKeyRequests this is a list of partner request for creation of partner API Key
	 */
	//public PartnersRetrieveApiKeyRequests retrieveAllApiKeyRequestsSubmittedByPartner(String partnerID);
	public List<APIkeyRequests> retrieveAllApiKeyRequestsSubmittedByPartner(String partnerID);
	
	/**
	 * This API would be used to retrieve Policy details by Policy Name
	 * @param PolicyName this is unique Policy Name
	 * @return PolicyIdResponse this class contains PolicyId
	 */
	public PolicyIdResponse getPolicyId(String PolicyName);
	
	/**
	 * This method is used to view API key request status and API key (in case request is approved).
	 * @param partnerID this is unique id created after self registered by partner
	 * @param aPIKeyReqID this is unique id created after partner request for Partner API Key
	 * @return aPIkeyRequests this class contains partnerApiKey apiKeyRequestStatus and validity details
	 */
	
	public APIkeyRequests viewApiKeyRequestStatusApiKey(String partnerID,String aPIKeyReqID);
	
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
     * @param PartnerCertificateRequestDto partnerCertResponseDto
     * @return {@link PartnerCertificateResponseDto} instance
     * @throws IOException 
     * @throws JsonProcessingException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
    */
    public PartnerCertificateResponseDto uploadPartnerCertificate(PartnerCertificateRequestDto partnerCertResponseDto) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException;

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
	
}
