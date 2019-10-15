package io.mosip.pmp.policy.errorMessages;

import io.mosip.kernel.core.exception.BaseUncheckedException;

public class RequestException extends BaseUncheckedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3893526559365169912L;

	/**
	 * 
	 */
	public RequestException() {
		super();

	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public RequestException(String arg0, String arg1, Throwable arg2) {
		super(arg0, arg1, arg2);

	}

	/**
	 * @param errorCode
	 * @param errorMessage
	 */
	public RequestException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);

	}

	/**
	 * @param errorMessage
	 */
	public RequestException(String errorMessage) {
		super(errorMessage);

	}

}
