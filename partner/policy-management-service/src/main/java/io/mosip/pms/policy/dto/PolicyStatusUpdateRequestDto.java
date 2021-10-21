package io.mosip.pms.policy.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * <p> PolicyStatusUpdateRequestDto holds parameters required to update policy status.</p>
 * 
 * @author Nagarjuna Kuchi
 * @version 1.0
 */
@Data
@ApiModel(value= "PolicyStatusUpdateRequestDto", description = " Policy Status Update Request Representation")
public class PolicyStatusUpdateRequestDto {
	
	/**
	 * status ( "Active" Or "De-Active")
	 * 
	 */
	private String Status;

}
