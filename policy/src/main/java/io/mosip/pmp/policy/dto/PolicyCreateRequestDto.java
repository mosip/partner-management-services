package io.mosip.pmp.policy.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(value= "PolicyRequestDto", description = " Policy Create Request Representation")
public class PolicyCreateRequestDto {
	
	@NotBlank(message = "Policy name should not be empty.")
	@Size(min = 1, max = 128, message = "Policy name length should be in between 1 and 128 chars")
	@Type(type="string")
	private String name;
	
	@NotBlank(message = "Policy desc should not be blank.")
	@Size(min = 1, max = 128, message = "Policy desc length should be in between 1 and 128 chars")	
	private String desc;	
}
