package io.mosip.pms.partner.request.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class CreateAPIKeyRequestDto {

	@NotBlank
	private String apiKeyName;
}
