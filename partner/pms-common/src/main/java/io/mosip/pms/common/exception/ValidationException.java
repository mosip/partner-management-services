package io.mosip.pms.common.exception;

import java.util.ArrayList;
import java.util.List;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ServiceError;

/**
 * Exception to hold validation errors
 * 
 */
public class ValidationException extends BaseUncheckedException {

	/**
	 * Generated serialVersionUID
	 */
	private static final long serialVersionUID = 8764526395763989084L;

	private List<ServiceError> errors = new ArrayList<>();

	public ValidationException(List<ServiceError> errors) {
		errors.stream().forEach(error -> super.addInfo(error.getErrorCode(), error.getMessage()));
		//errors.stream().forEach(error -> errors.add(new ServiceError(error.getErrorCode(), error.getMessage())));
		this.errors = errors;
	}

	public List<ServiceError> getErrors() {
		return errors;
	}

}
