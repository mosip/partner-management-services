package io.mosip.pmp.common.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

public class RequestException extends BaseUncheckedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8072241272700356545L;

	
	public RequestException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
