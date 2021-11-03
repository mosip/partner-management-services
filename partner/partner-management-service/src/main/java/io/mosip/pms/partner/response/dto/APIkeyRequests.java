package io.mosip.pms.partner.response.dto;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

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
	private boolean apikeyStatus;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp validityTill;
	
	
	
	
}
