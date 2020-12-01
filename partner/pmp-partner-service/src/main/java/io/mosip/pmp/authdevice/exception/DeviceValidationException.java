package io.mosip.pmp.authdevice.exception;

import java.util.List;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.pmp.partner.exception.BaseUncheckedException;

public class DeviceValidationException extends BaseUncheckedException {
	private static final long serialVersionUID = 8764526395763989084L;

	private List<ServiceError> errors;

	public DeviceValidationException(List<ServiceError> errors) {
		this.errors = errors;
	}

	public List<ServiceError> getErrors() {
		return errors;
	}

}
