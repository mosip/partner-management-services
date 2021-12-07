package io.mosip.pms.partner.response.dto;

import lombok.Data;

@Data
public class APIKeyGenerateResponseDto {

	private String apiKey;
	
	private String label;
	
	private String policyId;
	
	private String partnerId;
}
