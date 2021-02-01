package io.mosip.pms.partner.manager.exception;

import io.mosip.pms.device.exception.BaseUncheckedException;

public class PartnerManagerServiceException extends BaseUncheckedException {

	private static final long serialVersionUID = 1L;

	public PartnerManagerServiceException() {
	}

	public PartnerManagerServiceException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
