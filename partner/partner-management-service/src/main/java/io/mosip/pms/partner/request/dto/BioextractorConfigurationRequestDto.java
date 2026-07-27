package io.mosip.pms.partner.request.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BioextractorConfigurationRequestDto {

	@Size(max = 128, message = "configName length must be <= 128")
	private String configName;

	@Size(max = 128, message = "Bio extractor provider name length must be <= 128")
	private String bioextractorProviderName;

	@Size(max = 36, message = "Bio extractor provider version length must be <= 36")
	private String bioextractorProviderVersion;

	@Size(max = 64, message = "Bio Modality length must be <= 64")
	private String bioModality;

	@Size(max = 128, message = "Attribute name length must be <= 128")
	private String attributeName;

	@Size(max = 64, message = "Credential data format length must be <= 64")
	private String credentialDataFormat;
}
