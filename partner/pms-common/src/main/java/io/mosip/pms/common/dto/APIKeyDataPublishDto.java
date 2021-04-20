package io.mosip.pms.common.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class APIKeyDataPublishDto {

	private String apiKeyId;
	
	private LocalDateTime apiKeyCommenceOn;
	
	private LocalDateTime apiKeyExpiresOn;
	
	private String apiKeyStatus;
}
