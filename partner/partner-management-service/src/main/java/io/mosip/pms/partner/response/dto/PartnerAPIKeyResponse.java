package io.mosip.pms.partner.response.dto;

import lombok.Data;

/**
 * @author sanjeev.shrivastava
 *
 */

@Data
public class PartnerAPIKeyResponse {
	
	private String apiRequestId;
	private String message;
	private String apikeyId;
		
}
