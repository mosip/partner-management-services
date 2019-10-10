package io.mosip.pmp.misp.exception;

public class BaseExceptionClass extends BaseUncheckedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8403716478830080286L;

	public BaseExceptionClass(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	public BaseExceptionClass(String errorCode, String errorMessage, Throwable cause) {
		super(errorCode, errorMessage, cause);
	}

}
