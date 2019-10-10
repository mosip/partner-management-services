package io.mosip.pmp.partnermanagement.exception;

public class NoPartnerApiKeyRequestsException extends BaseUncheckedException{
	
	private static final long serialVersionUID = 1L;

	public NoPartnerApiKeyRequestsException() {}
	
	public NoPartnerApiKeyRequestsException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
