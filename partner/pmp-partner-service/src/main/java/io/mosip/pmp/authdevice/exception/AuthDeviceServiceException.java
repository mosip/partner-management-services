package io.mosip.pmp.authdevice.exception;

import io.mosip.pmp.partner.exception.BaseUncheckedException;

public class AuthDeviceServiceException extends BaseUncheckedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5802805689689326166L;

	public AuthDeviceServiceException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
