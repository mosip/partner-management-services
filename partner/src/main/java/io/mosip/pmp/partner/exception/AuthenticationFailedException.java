package io.mosip.pmp.partner.exception;

public class AuthenticationFailedException extends BaseUncheckedException{
	
	private static final long serialVersionUID = 1L;

	public AuthenticationFailedException() {}
	
	public AuthenticationFailedException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

}
