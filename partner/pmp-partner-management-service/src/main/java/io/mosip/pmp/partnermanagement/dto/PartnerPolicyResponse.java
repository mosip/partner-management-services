package io.mosip.pmp.partnermanagement.dto;

import lombok.Data;

@Data
public class PartnerPolicyResponse {

	private String policyId;
	
	private String policyName;
	
	private String policyDescription;
	
	private boolean policyStatus;
	
	private PolicyDTO policy;
	
	private String partnerId;
	
	private String partnerName;
	
}
