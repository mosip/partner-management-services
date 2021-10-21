package io.mosip.pms.policy.dto;

import lombok.Data;

/**
 * <p> Holds the response of policy update.</p>
 * 
 * @author Nagarjuna Kuchi
 * @version 1.0
 *
 */
@Data
public class PolicyStatusUpdateResponseDto {

	/**
	 * Contains information regarding the policy update.(Active or DeActive).
	 */
	private String message;
}
