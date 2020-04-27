package io.mosip.pmp.partnermanagement.exception;

/**
 * @author sanjeev.shrivastava
 *
 */

public class PartnerValidationException extends BaseUncheckedException{
	
	private static final long serialVersionUID = 1L;

	public PartnerValidationException() {}
	
	public PartnerValidationException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
