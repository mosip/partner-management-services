package io.mosip.pms.common.request.dto;

import lombok.Data;

/**
 * @author sanjeev.shrivastava
 *
 */

@Data
public class ErrorResponse {
		
	private String errorCode;
    private String message;
}
