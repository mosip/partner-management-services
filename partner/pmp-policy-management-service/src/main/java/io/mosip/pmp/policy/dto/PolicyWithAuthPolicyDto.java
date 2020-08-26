package io.mosip.pmp.policy.dto;

import java.util.List;

import io.mosip.pmp.policy.entity.PolicyGroup;
import lombok.Data;

/**
 * <p>This class contains objects of policyCreateResponseDto @link {@link PolicyCreateResponseDto}</p>
 * <p> list of auth policies {@link PolicyAttributesDto}.</p>
 * 
 * @author Nagarjuna Kuchi
 *
 */
@Data
public class PolicyWithAuthPolicyDto {

	private PolicyGroup policyGroup;
	
	private List<PolicyDto> policies;
}
