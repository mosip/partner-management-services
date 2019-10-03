package io.mosip.pmp.partner.exception;

import lombok.Data;

@Data
public class ErrorResponse {
		
	private String errorCode;
    private String message;
}
