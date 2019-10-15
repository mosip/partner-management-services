package io.mosip.pmp.policy.dto;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class AllowedKycDto {

	@NotBlank(message = "attributeName should not be empty in allowedKyc.")
	private String attributeName;
	
	@NotBlank(message = "required should contain 'true' or 'false' values in allowedKyc.")
	private boolean required; 
}
