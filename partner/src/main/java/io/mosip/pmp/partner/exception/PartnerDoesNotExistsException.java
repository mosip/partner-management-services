package io.mosip.pmp.partner.exception;

public class PartnerDoesNotExistsException extends BaseUncheckedException{
	
	private static final long serialVersionUID = 1L;

	public PartnerDoesNotExistsException() {}
	
	public PartnerDoesNotExistsException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
