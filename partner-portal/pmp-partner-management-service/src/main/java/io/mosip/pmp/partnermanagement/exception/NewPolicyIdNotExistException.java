package io.mosip.pmp.partnermanagement.exception;

public class NewPolicyIdNotExistException extends BaseUncheckedException{
	
	private static final long serialVersionUID = 1L;

	public NewPolicyIdNotExistException() {}
	
	public NewPolicyIdNotExistException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
