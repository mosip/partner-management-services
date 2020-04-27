package io.mosip.pmp.partnermanagement.exception;

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
