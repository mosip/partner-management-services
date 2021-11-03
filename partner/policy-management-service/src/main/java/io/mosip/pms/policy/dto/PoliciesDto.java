package io.mosip.pms.policy.dto;

import java.util.List;

import lombok.Data;

@Data
public class PoliciesDto {

	private List<PolicyWithAuthPolicyDto> policies;
}
