package io.mosip.pmp.partnermanagement.dto;

import java.sql.Timestamp;
import java.time.LocalDateTime;

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
	
	private LocalDateTime mispExpiresOn;
	
	private Timestamp apiKeyExpiresOn;
	
	private LocalDateTime policyExpiresOn;
	
	private String certificateData;	
}
