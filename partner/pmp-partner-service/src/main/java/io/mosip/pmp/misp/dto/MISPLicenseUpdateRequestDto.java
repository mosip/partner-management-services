package io.mosip.pmp.misp.dto;

import lombok.Data;

@Data
public class MISPLicenseUpdateRequestDto {

	private String providerId;
	
	private String licenseKeyStatus;
	
	private String licenseKey;
}
