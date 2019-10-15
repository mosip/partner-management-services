package io.mosip.pmp.policy.dto;

import java.util.List;

import lombok.Data;

@Data
public class PoliciesDto {

	private PolicyCreateResponseDto policy;
	
	private List<PolicyDto> authPolicies;
}
