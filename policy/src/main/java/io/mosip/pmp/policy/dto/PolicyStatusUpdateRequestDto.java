package io.mosip.pmp.policy.dto;

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
	 * policy group id.
	 */
	public String Id;
	
	/**
	 * status ( "Active" Or "De-Active")
	 * 
	 */
	public String Status;

}
