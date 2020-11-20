package io.mosip.pmp.misp.exception;

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

	private List<ServiceError> errors;

	public ValidationException(List<ServiceError> errors) {
		this.errors = errors;
	}

	public List<ServiceError> getErrors() {
		return errors;
	}

}
