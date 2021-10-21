package io.mosip.pms.partner.manager.dto;

import lombok.Data;

@Data
public class ApikeyRequests {
	
	private String partnerID;
	private String status;
	private String organizationName;
	private String policyId;
	private String policyDesc;
	private String apiKeyReqNo;
}
