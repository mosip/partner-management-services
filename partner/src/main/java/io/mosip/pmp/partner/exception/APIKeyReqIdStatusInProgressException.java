package io.mosip.pmp.partner.exception;

public class APIKeyReqIdStatusInProgressException extends BaseUncheckedException{
	
	private static final long serialVersionUID = 1L;

	public APIKeyReqIdStatusInProgressException() {}
	
	public APIKeyReqIdStatusInProgressException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
