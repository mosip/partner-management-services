package io.mosip.pms.policy.errorMessages;

import io.mosip.kernel.core.exception.BaseUncheckedException;

public class DataViolationException extends BaseUncheckedException {	

	private static final long serialVersionUID = -5811206135953350643L;
	
	public DataViolationException(String errorCode, String errorMessage)
	{
		super(errorCode, errorMessage);
	}
	
	public DataViolationException(String errorCode, String errorMessage, Throwable rootCause)
	{
		super(errorCode, errorMessage, rootCause);
	}

}
