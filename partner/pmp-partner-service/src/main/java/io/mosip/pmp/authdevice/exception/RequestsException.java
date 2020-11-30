package io.mosip.pmp.authdevice.exception;

import java.util.List;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.pmp.partner.exception.BaseUncheckedException;

public class RequestsException extends BaseUncheckedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8072241272700356545L;

	
	private List<ServiceError> errors;

	public RequestsException(List<ServiceError> errors) {
		this.errors = errors;
	}

	public List<ServiceError> getErrors() {
		return errors;
	}
}
