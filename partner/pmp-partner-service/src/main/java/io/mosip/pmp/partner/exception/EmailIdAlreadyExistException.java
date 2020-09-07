package io.mosip.pmp.partner.exception;

/**
 * @author sanjeev.shrivastava
 *
 */

public class EmailIdAlreadyExistException extends BaseUncheckedException {

	private static final long serialVersionUID = 1L;

	public EmailIdAlreadyExistException() {}
	
	public EmailIdAlreadyExistException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
