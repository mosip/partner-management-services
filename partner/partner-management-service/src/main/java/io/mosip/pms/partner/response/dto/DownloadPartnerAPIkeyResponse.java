package io.mosip.pms.partner.response.dto;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * @author sanjeev.shrivastava
 *
 */

@Data
public class DownloadPartnerAPIkeyResponse {
		
		private String apiRequestKey;
		private String partnerAPIKey;
		private String apikeyReqStatus;
		private boolean apikeyStatus;
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
		private Timestamp validityTill;
}
