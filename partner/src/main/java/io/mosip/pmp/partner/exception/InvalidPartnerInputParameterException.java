package io.mosip.pmp.partner.exception;

/**
 * @author sanjeev.shrivastava
 *
 */
public class InvalidPartnerInputParameterException extends BaseUncheckedException{

	private static final long serialVersionUID = 1L;

	public InvalidPartnerInputParameterException() {}
	
	public InvalidPartnerInputParameterException(
			String errorCode, 
			String errorMessage) {
		super(errorCode, errorMessage);
	}
}
