package io.mosip.pmp.misp.exception;

/**
 * Custom class for DataAccessLayerException
 * 
 * @author Nagarjuna Kuchi
 * @since 1.0.0
 *
 */
public class DataAccessLayerException extends RuntimeException {

	/**
	 * Generated serialVersionUID
	 */
	private static final long serialVersionUID = 5074628123959874252L;

	/**
	 * Constructor for DataAccessLayerException
	 * 
	 * @param errorCode    The errorcode
	 * @param errorMessage The errormessage
	 * @param cause        The cause
	 */
	public DataAccessLayerException(String errorMessage, Throwable cause) {
		super(errorMessage, cause);
	}
}

