package io.mosip.pmp.partner.dto;

import lombok.Data;

@Data
public class PublicKeyRequest {
	
	private String applicationId;
	private String timestamp;
	private String referenceId;
}
