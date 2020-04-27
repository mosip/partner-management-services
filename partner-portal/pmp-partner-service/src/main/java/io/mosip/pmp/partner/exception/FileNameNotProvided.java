package io.mosip.pmp.partner.exception;

/**
 * {@link Exception} to be file name is empty or null
 * 
 * @author sanjeev.shrivastava
 */

public class FileNameNotProvided extends BaseUncheckedException {

	/**
	 * Unique id for serialization
	 */
	private static final long serialVersionUID = 105555532L;

	/**
	 * @param errorCode    unique exception code
	 * @param errorMessage exception message
	 */
	public FileNameNotProvided(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

}
