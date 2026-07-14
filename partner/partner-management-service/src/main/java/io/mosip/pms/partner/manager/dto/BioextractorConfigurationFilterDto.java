package io.mosip.pms.partner.manager.dto;

import lombok.Data;

@Data
public class BioextractorConfigurationFilterDto {

	private String configName;

	private String bioextractorProviderName;

	private String bioextractorProviderVersion;

	private String bioModality;

	private String attributeName;
}

