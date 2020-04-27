package io.mosip.pmp.partnermanagement.exception;

/**
 * @author sanjeev.shrivastava
 *
 */
public class PolicyNotExistException extends BaseUncheckedException{

	private static final long serialVersionUID = 1L;
	
	public PolicyNotExistException() {}
	
	public PolicyNotExistException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
