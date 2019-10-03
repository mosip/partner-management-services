package io.mosip.pmp.partner.exception;

/**
 * @author sanjeev.shrivastava
 *
 */
public class PartnerAlreadyRegisteredException extends BaseUncheckedException {

	private static final long serialVersionUID = 1L;

	public PartnerAlreadyRegisteredException() {}
	
	public PartnerAlreadyRegisteredException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
