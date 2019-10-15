package io.mosip.pmp.policy.dto;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class AuthPolicyDto {

	@NotBlank(message = "authType should not be empty.")
	private String authType;
	
	private String authSubType;
	
	@NotBlank(message = "mandatory should contain 'true' or 'false' values")
	private boolean mandatory;
}
