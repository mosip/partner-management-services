package io.mosip.pms.policy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * Defines an object to provide policy details to create policy group
 * @author Nagarjuna
 *
 */

@Data
@ApiModel(value= "PolicyGroupCreateRequestDto", description = " Policy Create Request Representation")
public class PolicyGroupCreateRequestDto {

	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 128, message = "Length should be in between 1 and 128 chars")
	public String name;
	
	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 128, message = "Length should be in between 1 and 128 chars")	
	public String desc;
}
