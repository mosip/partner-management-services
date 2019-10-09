package io.mosip.pmp.partnermanagement.exception;

/**
 * @author sanjeev.shrivastava
 *
 */
public class PartnerIdDoesNotExistException extends BaseUncheckedException{
	
	private static final long serialVersionUID = 1L;

	public PartnerIdDoesNotExistException() {}
	
	public PartnerIdDoesNotExistException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
