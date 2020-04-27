package io.mosip.pmp.policy.dto;

import io.mosip.pmp.policy.entity.PolicyGroup;
import lombok.Data;

/**
 * <p>This class contains objects of policyCreateResponseDto @link {@link PolicyCreateResponseDto}</p>
 * <p> list of auth policies {@link PolicyDto}.</p>
 * 
 * @author Nagarjuna Kuchi
 *
 */
@Data
public class PolicyWithAuthPolicyDto {

	private PolicyGroup policy;
	
	private PolicyDto policies;
}
