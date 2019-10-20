package io.mosip.pmp.partner.service;

import io.mosip.pmp.partner.dto.APIkeyRequests;
import io.mosip.pmp.partner.dto.DownloadPartnerAPIkeyResponse;
import io.mosip.pmp.partner.dto.PartnerAPIKeyRequest;
import io.mosip.pmp.partner.dto.PartnerAPIKeyResponse;
import io.mosip.pmp.partner.dto.PartnerRequest;
import io.mosip.pmp.partner.dto.PartnerResponse;
import io.mosip.pmp.partner.dto.PartnerUpdateRequest;
import io.mosip.pmp.partner.dto.PartnersRetrieveApiKeyRequests;
import io.mosip.pmp.partner.dto.RetrievePartnerDetailsResponse;

/**
 * This interface provides the methods which can be used for PartnerService
 * @author sanjeev.shrivastava
 *
 */

public interface PartnerService {
	
	/** This method is used  for self registration by partner to create Auth/E-KYC Partners.
	 * @param request.
	 * @return partnerResponse.
	 */
	public PartnerResponse savePartner(PartnerRequest request);
	
	/** This method is used to retrieve Auth/E-KYC Partner details.
	 * @param partnerID.
	 * @return retrievePartnerDetailsResponse.
	 */
	public RetrievePartnerDetailsResponse getPartnerDetails(String partnerID);
	
	/**This method is used to update Auth/E-KYC Partner's details
	 * @param request
	 * @param partnerID
	 * @return partnerResponse.
	 */
	public PartnerResponse updatePartnerDetail(PartnerUpdateRequest request,String partnerID);
	
	/**
	 * This method is used to submit Partner api key request.
	 * @param request
	 * @param partnerID
	 * @return partnerAPIKeyResponse.
	 */
	public PartnerAPIKeyResponse submitPartnerApiKeyReq(PartnerAPIKeyRequest request,String partnerID);
	
	/**
	 * This method is used to download Partner API key for the given APIKeyReqID.
	 * @param partnerID
	 * @param aPIKeyReqID
	 * @return downloadPartnerAPIkeyResponse.
	 */
	public DownloadPartnerAPIkeyResponse downloadPartnerAPIkey(String partnerID,String aPIKeyReqID);
	
	/**
	 * This method is used to retrieve all API key requests submitted by partner till date.
	 * @param partnerID
	 * @return partnersRetrieveApiKeyRequests.
	 */
	public PartnersRetrieveApiKeyRequests retrieveAllApiKeyRequestsSubmittedByPartner(String partnerID);
	
	/**
	 * This method is used to view API key request status and API key (in case request is approved).
	 * @param partnerID
	 * @param aPIKeyReqID
	 * @return
	 */
	public APIkeyRequests viewApiKeyRequestStatusApiKey(String partnerID,String aPIKeyReqID);
}
