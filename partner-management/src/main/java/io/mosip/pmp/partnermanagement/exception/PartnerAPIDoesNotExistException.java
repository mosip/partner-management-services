package io.mosip.pmp.partnermanagement.exception;

public class PartnerAPIDoesNotExistException extends BaseUncheckedException{
	
	private static final long serialVersionUID = 1L;

	public PartnerAPIDoesNotExistException() {}
	
	public PartnerAPIDoesNotExistException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
