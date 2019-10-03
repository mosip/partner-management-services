package io.mosip.pmp.partner.exception;

public class MissingPartnerInputParameterException extends BaseUncheckedException{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MissingPartnerInputParameterException() {}
	
	public MissingPartnerInputParameterException(
			String errorCode, 
			String errorMessage) {
		super(errorCode, errorMessage);
	}
}
