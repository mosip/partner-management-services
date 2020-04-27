package io.mosip.pmp.partner.exception;

public class PartnerAPIKeyIsNotCreatedException extends BaseUncheckedException{
	
	private static final long serialVersionUID = 1L;

	public PartnerAPIKeyIsNotCreatedException() {}
	
	public PartnerAPIKeyIsNotCreatedException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

}
