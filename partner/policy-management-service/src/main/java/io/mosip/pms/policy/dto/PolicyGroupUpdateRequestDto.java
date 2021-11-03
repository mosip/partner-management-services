package io.mosip.pms.policy.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;

import lombok.Data;

@Data
public class PolicyGroupUpdateRequestDto {

	@NotBlank(message = "Policy name should not be empty.")
	@Size(min = 1, max = 128, message = "Policy name length should be in between 1 and 128 chars")
	@Type(type="string")
	private String name;
	
	@NotBlank(message = "Policy desc should not be blank.")
	@Size(min = 1, max = 128, message = "Policy desc length should be in between 1 and 128 chars")	
	private String desc;	
	
	private boolean isActive;
}
