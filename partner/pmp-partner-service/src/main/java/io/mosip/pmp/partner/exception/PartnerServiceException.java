package io.mosip.pmp.partner.exception;

/**
 * @author sanjeev.shrivastava
 *
 */

public class PartnerServiceException extends BaseUncheckedException{
	
	private static final long serialVersionUID = 1L;

	public PartnerServiceException() {}
	
	public PartnerServiceException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
