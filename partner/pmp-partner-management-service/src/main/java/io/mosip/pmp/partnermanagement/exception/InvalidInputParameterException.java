package io.mosip.pmp.partnermanagement.exception;

public class InvalidInputParameterException extends BaseUncheckedException{
	
	private static final long serialVersionUID = 1L;

	public InvalidInputParameterException() {}
	
	public InvalidInputParameterException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

}
