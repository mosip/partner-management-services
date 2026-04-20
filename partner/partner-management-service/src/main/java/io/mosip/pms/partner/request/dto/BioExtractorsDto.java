package io.mosip.pms.partner.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BioExtractorsDto {
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

	@NotBlank(message = "extractorProvider is missing")
	@Size(max = 128, message = "extractorProvider length must be <= 128")
	private String extractorProvider;

	@Size(max = 8, message = "extractorProviderVersion length must be <= 8")
	private String extractorProviderVersion;
}

