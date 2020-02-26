package io.mosip.pmp.partnermanagement.dto;

import lombok.Data;

@Data
public class RetrievePartnersManagersDetails {
	
	public String partnerID;
	public String partnerStatus;
	public String policyID;
	public String policyName;
	public String apikeyReqID;
	public String apikeyReqIDStatus;
	public String partnerAPIKey;
	public String partnerAPIKeyStatus;

}
