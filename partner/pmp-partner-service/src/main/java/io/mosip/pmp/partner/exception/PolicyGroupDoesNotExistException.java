package io.mosip.pmp.partner.exception;

public class PolicyGroupDoesNotExistException extends BaseUncheckedException{
	
	static final long serialVersionUID = 1L;

	public PolicyGroupDoesNotExistException() {}
	
	public PolicyGroupDoesNotExistException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

}
