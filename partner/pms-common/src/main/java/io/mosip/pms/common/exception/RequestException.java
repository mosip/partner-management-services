package io.mosip.pms.common.exception;

import java.util.ArrayList;
import java.util.List;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ServiceError;

public class RequestException extends BaseUncheckedException {
	
	private static final long serialVersionUID = -8072241272700356545L;

	private List<ServiceError> errors = new ArrayList<>();
	
	public RequestException(String errorCode, String errorMessage) {
		errors.add(new ServiceError(errorCode, errorMessage));
	}
	
	public RequestException(List<ServiceError> errors) {
		errors.stream().forEach(error -> super.addInfo(error.getErrorCode(), error.getMessage()));
		//errors.stream().forEach(error -> errors.add(new ServiceError(error.getErrorCode(), error.getMessage())));
		this.errors = errors;
	}
	
	public List<ServiceError> getErrors() {
		return errors;
	}
}
