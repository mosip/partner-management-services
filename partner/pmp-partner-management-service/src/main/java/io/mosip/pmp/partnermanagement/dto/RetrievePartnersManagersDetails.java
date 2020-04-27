package io.mosip.pmp.partnermanagement.dto;

import lombok.Data;

@Data
public class RetrievePartnersManagersDetails {
	
	private String partnerID;
	private String partnerStatus;
	private String policyID;
	private String policyName;
	private String apikeyReqID;
	private String apikeyReqIDStatus;
	private String partnerAPIKey;
	private String partnerAPIKeyStatus;

}
