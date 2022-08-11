package io.mosip.pms.ida.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MISPDataPublishDto {

	private String mispId;
	
	private String licenseKey;
	
	private LocalDateTime mispCommenceOn;
	
	private LocalDateTime mispExpiresOn;
	
	private String mispStatus;
}
