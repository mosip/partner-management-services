package io.mosip.pmp.authdevice.exception;

import org.springframework.stereotype.Component;

import io.mosip.pmp.partner.exception.BaseUncheckedException;

public class RequestException extends BaseUncheckedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8072241272700356545L;

	
	public RequestException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
