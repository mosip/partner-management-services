package io.mosip.pms.partner.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ExtractorProviderDto {
	
	@NotBlank(message = "provider is missing")
	@Size(max = 128, message = "provider length must be <= 128")
	private String provider;
	
	@Size(max = 8, message = "version length must be <= 8")
	private String version;

}
