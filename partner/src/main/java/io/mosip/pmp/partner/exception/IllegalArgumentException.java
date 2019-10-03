package io.mosip.pmp.partner.exception;

/**
 * Base class for all preconditions violation exceptions.
 * 
 * @author sanjeev.shrivastava
 */

public class IllegalArgumentException extends BaseUncheckedException {
	/** Serializable version Id. */
	private static final long serialVersionUID = 924722202110630628L;

	public IllegalArgumentException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);

	}

	public IllegalArgumentException(String errorCode, String errorMessage, Throwable cause) {
		super(errorCode, errorMessage, cause);

	}

}
