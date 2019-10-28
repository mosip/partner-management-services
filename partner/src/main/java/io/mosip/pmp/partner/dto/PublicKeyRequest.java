package io.mosip.pmp.partner.dto;

import lombok.Data;

@Data
public class PublicKeyRequest {
	
	public String applicationId;
	public String timestamp;
	public String referenceId;
}
