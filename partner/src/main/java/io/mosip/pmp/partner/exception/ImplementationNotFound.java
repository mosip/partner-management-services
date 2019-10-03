package io.mosip.pmp.partner.exception;

/**
 * {@link Exception} to be thrown when implementation is not found
 * 
 * @author sanjeev.shrivastava
 */

public class ImplementationNotFound extends BaseUncheckedException {

	/**
	 * Unique id for serialization
	 */
	private static final long serialVersionUID = 105555533L;

	/**
	 * @param errorCode    unique exception code
	 * @param errorMessage exception message
	 */
	public ImplementationNotFound(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

}
