package io.mosip.pms.policy.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
	
	@NotBlank(message = "Policy group name should not be empty")
	@NotNull(message = "Policy group name must not be null")
	@Size(min = 1, max = 128, message = "Policy group name length should be in between 1 and 128 chars")
	@Type(type="string")
	private String policyGroupName;	
	
	@NotBlank(message = "Policy name should not be empty.")
	@NotNull(message = "name must not be null")
	@Size(min = 1, max = 128, message = "Policy name length should be in between 1 and 128 chars")
	@Type(type="string")
	private String name;
	
	@NotBlank(message = "Policy desc should not be blank.")
	@NotNull(message = "desc must not be null")
	@Size(min = 1, max = 128, message = "Policy desc length should be in between 1 and 128 chars")	
	private String desc;	
	
	private JSONObject policies;

	@NotBlank(message = "version should not be blank")
	@NotNull(message = "version must not be null")
	private String version;
	
}
