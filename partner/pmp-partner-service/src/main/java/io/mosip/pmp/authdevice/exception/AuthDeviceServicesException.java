package io.mosip.pmp.authdevice.exception;

import java.util.List;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.pmp.partner.exception.BaseUncheckedException;

public class AuthDeviceServicesException extends BaseUncheckedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5802805689689326166L;

	private List<ServiceError> errors;

	public AuthDeviceServicesException(List<ServiceError> errors) {
		this.errors = errors;
	}

	public List<ServiceError> getErrors() {
		return errors;
	}
}
