package io.mosip.pmp.partnermanagement.exception;

public class PartnerApiKeyDoesNotBelongToThePolicyGroupOfThePartnerMangerException extends BaseUncheckedException{
	
	private static final long serialVersionUID = 1L;

	public PartnerApiKeyDoesNotBelongToThePolicyGroupOfThePartnerMangerException() {}
	
	public PartnerApiKeyDoesNotBelongToThePolicyGroupOfThePartnerMangerException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

}
