package io.mosip.pms.partner.response.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class BioextractorConfigurationDetailDto {

	private String id;

	private String configName;

	private String bioextractorProviderName;

	private String bioextractorProviderVersion;

	private String bioModality;

	private String attributeName;

	private String credentialDataFormat;

	private LocalDateTime createdDateTime;
}
