package io.mosip.pms.device.exception;

import java.util.List;


import io.mosip.kernel.core.exception.ServiceError;

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
