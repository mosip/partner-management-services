package io.mosip.pms.partner.request.dto;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class APIkeyStatusUpdateRequestDto {

	@NotBlank
	private String label;
	
	@NotBlank(message="value is empty or null")
	private String status;
}
