package io.mosip.pms.partner.request.dto;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class APIKeyGenerateRequestDto {

	@NotBlank
	private String policyName;
	
	@NotBlank
	private String label;
}
