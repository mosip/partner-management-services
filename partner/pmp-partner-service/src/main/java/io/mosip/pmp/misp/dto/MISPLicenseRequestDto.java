package io.mosip.pmp.misp.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class MISPLicenseRequestDto {
	
	@NotBlank
	@NotNull
	private String providerId;
}
