package io.mosip.pmp.partner.exception;

public class PartnerAPIKeyReqIDDoesNotExistException extends BaseUncheckedException{
	
	private static final long serialVersionUID = 1L;

	public PartnerAPIKeyReqIDDoesNotExistException() {}
	
	public PartnerAPIKeyReqIDDoesNotExistException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
