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
	private String Id;
	
	/**
	 * policy name
	 */
	private String name;
	
	/**
	 * policy description
	 */
	private String desc;
	
	/**
	 * Object of Auth and kyc attributes
	 */
	private PolicyDto policies;

}
