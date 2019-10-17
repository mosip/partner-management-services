package io.mosip.pmp.policy.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * <p> This class contains parameters required to update policy along with auth policies.</p>
 *  
 * @author Nagarjuna Kuchi
 * @version 1.0
 *
 */
@Data
@ApiModel(value= "PolicyUpdateRequestDto", description = " Policy Update Request Representation")
public class PolicyUpdateRequestDto {

	/**
	 *  policy id
	 */
	public String Id;
	
	/**
	 * policy name
	 */
	public String name;
	
	/**
	 * policy description
	 */
	public String desc;
	
	/**
	 * Object of Auth and kyc attributes
	 */
	public PolicyDto policies;

}
