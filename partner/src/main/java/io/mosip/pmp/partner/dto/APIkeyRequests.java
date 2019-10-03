package io.mosip.pmp.partner.dto;

import lombok.Data;

/**
 * @author sanjeev.shrivastava
 *
 */
@Data
public class APIkeyRequests {
	
	private String apiKeyReqID;
	private String apiKeyRequestStatus;
	private String partnerApiKey;
	private String validityTill;
}
