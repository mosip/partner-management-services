package io.mosip.pms.partner.request.dto;

import lombok.Data;

@Data
public class ExtractorDto {
	
	private String attributeName;
	
	private String biometric;

	/**
	 * Optional biometric sub types.
	 */
	private String biometricSubTypes;
	
	private ExtractorProviderDto extractor;
}
