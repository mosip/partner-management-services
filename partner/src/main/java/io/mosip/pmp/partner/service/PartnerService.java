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
	
	public PartnerResponse savePartner(PartnerRequest request);
	public RetrievePartnerDetailsResponse getPartnerDetails(String partnerID);
	public PartnerResponse updatePartnerDetail(PartnerUpdateRequest request,String partnerID);
	public PartnerAPIKeyResponse submitPartnerApiKeyReq(PartnerAPIKeyRequest request,String partnerID);
	public DownloadPartnerAPIkeyResponse downloadPartnerAPIkey(String partnerID,String aPIKeyReqID);
	public PartnersRetrieveApiKeyRequests retrieveAllApiKeyRequestsSubmittedByPartner(String partnerID);
	public APIkeyRequests viewApiKeyRequestStatusApiKey(String partnerID,String aPIKeyReqID);
}
