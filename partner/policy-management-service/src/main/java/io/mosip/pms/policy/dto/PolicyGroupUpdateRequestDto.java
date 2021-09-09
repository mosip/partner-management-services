package io.mosip.pms.policy.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;

import lombok.Data;

@Data
public class PolicyGroupUpdateRequestDto {

	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 128, message = "Length should be between 1 and 128 chars")
	@Type(type="string")
	private String name;
	
	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 128, message = "Length should be between 1 and 128 chars")	
	private String desc;
	
	private Boolean isActive;
}
