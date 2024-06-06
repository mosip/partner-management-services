package io.mosip.pms.partner.misp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class MISPLicenseRequestDto {
	
	@NotBlank
	@NotNull
	private String providerId;
}
