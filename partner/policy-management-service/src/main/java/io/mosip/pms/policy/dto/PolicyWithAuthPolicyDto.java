package io.mosip.pms.policy.dto;

import java.util.List;

import io.mosip.pms.common.entity.PolicyGroup;
import lombok.Data;

/**
 *  
 * @author Nagarjuna Kuchi
 *
 */
@Data
public class PolicyWithAuthPolicyDto {

	private PolicyGroup policyGroup;
	
	private List<PolicyDto> policies;
}
