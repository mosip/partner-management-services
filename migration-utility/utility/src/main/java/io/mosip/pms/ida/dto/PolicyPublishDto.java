package io.mosip.pms.ida.dto;

import java.time.LocalDateTime;

import org.json.simple.JSONObject;

import lombok.Data;

@Data
public class PolicyPublishDto {

	private String policyId;
	
	private JSONObject policy;
	
	private String policyName;
	
	private String policyStatus;
	
	private String policyDescription;
	
	private LocalDateTime policyCommenceOn;
	
	private LocalDateTime policyExpiresOn;
	
}
