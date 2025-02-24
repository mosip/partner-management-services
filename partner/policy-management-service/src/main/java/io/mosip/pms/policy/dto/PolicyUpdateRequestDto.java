package io.mosip.pms.policy.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.json.simple.JSONObject;

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
	
	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 128, message = "Length should be in between 1 and 128 chars")
	@Type(type="string")
	private String policyGroupName;	
	
	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 128, message = "Length should be in between 1 and 128 chars")
	@Type(type="string")
	private String name;
	
	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 128, message = "Length should be in between 1 and 128 chars")	
	private String desc;	
	
	private JSONObject policies;

	@NotBlank(message="value is empty or null")
	private String version;
	
}
