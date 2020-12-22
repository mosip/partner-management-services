package io.mosip.pmp.misp.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MISPLicenseResponseDto {

	private String providerId;
	
	private String licenseKey;
	
	private String licenseKeyStatus;
	
	private LocalDateTime licenseKeyExpiry; 
}
