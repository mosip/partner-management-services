package io.mosip.pmp.policy.dto;

import java.util.List;

import lombok.Data;

@Data
public class PoliciesDto {

	public List<PolicyWithAuthPolicyDto> policies;
}
