package io.mosip.pmp.policy.dto;

import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PolicyDto {

	
	private String policyId;
	
	@NotNull
	private String name;
	
	@NotNull
	private String descr;
	
	
	private List<AuthPolicyDto> authPolicies;
	
	
	private List<AllowedKycDto> allowedKycAttributes;
}
