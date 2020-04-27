package io.mosip.pmp.partner.exception;

public class PartnerAlreadyRegisteredWithSamePolicyGroupException extends BaseUncheckedException{
	
	private static final long serialVersionUID = 1L;

	public PartnerAlreadyRegisteredWithSamePolicyGroupException() {}
	
	public PartnerAlreadyRegisteredWithSamePolicyGroupException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
