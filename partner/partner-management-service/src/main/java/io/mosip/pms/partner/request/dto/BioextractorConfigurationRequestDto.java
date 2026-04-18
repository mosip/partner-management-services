package io.mosip.pms.partner.request.dto;

import lombok.Data;

@Data
public class BioextractorConfigurationRequestDto {

	private String configName;

	private String bioextractorProviderName;

	private String bioextractorProviderVersion;

	private String bioModality;
}
