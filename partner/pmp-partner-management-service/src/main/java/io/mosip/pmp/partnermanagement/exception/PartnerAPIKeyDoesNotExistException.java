package io.mosip.pmp.partnermanagement.exception;

public class PartnerAPIKeyDoesNotExistException extends BaseUncheckedException {

	private static final long serialVersionUID = 1L;
	
	public PartnerAPIKeyDoesNotExistException() {
	}

	public PartnerAPIKeyDoesNotExistException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
