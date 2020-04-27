package io.mosip.pmp.partnermanagement.dto;

import lombok.Data;

@Data
public class PolicyDTO {
	
	private String policyId;
	
	/** The policies. */
	private Policies policies;
}
