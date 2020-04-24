package io.mosip.pmp.partner.dto;

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
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Timestamp validityTill;
	
	
	
	
}
