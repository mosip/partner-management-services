package io.mosip.pms.policy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class PolicyGroupUpdateRequestDto {

	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 128, message = "Length should be between 1 and 128 chars")
	private String name;
	
	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 128, message = "Length should be between 1 and 128 chars")	
	private String desc;
	
	private Boolean isActive;
}
