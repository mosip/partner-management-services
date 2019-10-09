package io.mosip.pmp.partnermanagement.exception;

/**
 * @author sanjeev.shrivastava
 *
 */

public class PartnerDoesNotExistException extends BaseUncheckedException{
	
	private static final long serialVersionUID = 1L;

	public PartnerDoesNotExistException() {}
	
	public PartnerDoesNotExistException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
