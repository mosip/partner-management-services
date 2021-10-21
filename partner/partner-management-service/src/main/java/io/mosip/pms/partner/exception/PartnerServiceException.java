package io.mosip.pms.partner.exception;

import io.mosip.pms.device.exception.BaseUncheckedException;

public class PartnerServiceException extends BaseUncheckedException{
	
	private static final long serialVersionUID = 1L;

	public PartnerServiceException() {}
	
	public PartnerServiceException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
