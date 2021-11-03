package io.mosip.pms.common.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * @author Nagarjuna
 *
 */

public class ApiAccessibleException extends BaseUncheckedException {

	private static final long serialVersionUID = 1L;

	public ApiAccessibleException() {}
	
	public ApiAccessibleException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
