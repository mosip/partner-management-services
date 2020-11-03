package io.mosip.pmp.partner.exception;

public class PartnerTypeDoesNotExistException extends BaseUncheckedException{
	
	static final long serialVersionUID = 1L;

	public PartnerTypeDoesNotExistException() {}
	
	public PartnerTypeDoesNotExistException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

}
