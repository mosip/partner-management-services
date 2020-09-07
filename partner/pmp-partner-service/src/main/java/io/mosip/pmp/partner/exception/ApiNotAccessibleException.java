package io.mosip.pmp.partner.exception;

/**
 * @author sanjeev.shrivastava
 *
 */

public class ApiNotAccessibleException extends BaseUncheckedException {

	private static final long serialVersionUID = 1L;

	public ApiNotAccessibleException() {}
	
	public ApiNotAccessibleException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
