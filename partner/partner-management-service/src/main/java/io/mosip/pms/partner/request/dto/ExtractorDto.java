package io.mosip.pms.partner.request.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ExtractorDto {
	
	@NotBlank(message = "attributeName is missing")
	@Size(max = 128, message = "attributeName length must be <= 128")
	private String attributeName;
	
	@NotBlank(message = "biometric is missing")
	@Size(max = 64, message = "biometric length must be <= 64")
	private String biometric;

	/**
	 * Optional biometric sub types.
	 */
	@Size(max = 64, message = "biometricSubTypes length must be <= 64")
	private String biometricSubTypes;
	
	@Valid
	@NotNull(message = "extractor is missing")
	private ExtractorProviderDto extractor;
}
