package io.mosip.pmp.partner.exception;

/**
 * @author sanjeev.shrivastava
 *
 */

public class ApiAccessibleException extends BaseUncheckedException {

	private static final long serialVersionUID = 1L;

	public ApiAccessibleException() {}
	
	public ApiAccessibleException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
