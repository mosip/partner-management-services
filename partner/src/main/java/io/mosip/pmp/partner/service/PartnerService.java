package io.mosip.pmp.partner.service;

import java.util.List;

import io.mosip.pmp.partner.core.RequestWrapper;
import io.mosip.pmp.partner.dto.APIkeyRequests;
import io.mosip.pmp.partner.dto.DigitalCertificateRequest;
import io.mosip.pmp.partner.dto.DigitalCertificateResponse;
import io.mosip.pmp.partner.dto.DownloadPartnerAPIkeyResponse;
import io.mosip.pmp.partner.dto.LoginUserRequest;
import io.mosip.pmp.partner.dto.LoginUserResponse;
import io.mosip.pmp.partner.dto.PartnerAPIKeyRequest;
import io.mosip.pmp.partner.dto.PartnerAPIKeyResponse;
import io.mosip.pmp.partner.dto.PartnerRequest;
import io.mosip.pmp.partner.dto.PartnerResponse;
import io.mosip.pmp.partner.dto.PartnerUpdateRequest;
import io.mosip.pmp.partner.dto.PolicyIdResponse;
import io.mosip.pmp.partner.dto.RetrievePartnerDetailsResponse;
import io.mosip.pmp.partner.dto.RetrievePartnerDetailsWithNameResponse;
import io.mosip.pmp.partner.dto.SignUserRequest;
import io.mosip.pmp.partner.dto.SignUserResponse;

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
	 * @param request this class contains digitalCertificate details
	 * @return DigitalCertificateResponse this class contains massage
	 */
	
	public DigitalCertificateResponse validateDigitalCertificate(RequestWrapper<DigitalCertificateRequest> request);
	
	//public DigitalCertificateResponse uploadDigitalCertificate(DigitalCertificateRequest request);
	
	/**
	 * This method is use for userLogin when need to validate the digital certificate 
	 * @param request this class contains LoginUserRequest
	 * @return loginUserResponse this class contains LoginUserResponse
	 * 
	 */
	
	public LoginUserResponse userLoginInKernal(RequestWrapper<LoginUserRequest> request);
	
	/**
	 * @param request this class contains digitalCertificate details
	 * @return SignUserResponse this class contains signature and timestamp.
	 */
	
	public SignUserResponse signUserInDigitalCertificates(RequestWrapper<SignUserRequest> request);
}
