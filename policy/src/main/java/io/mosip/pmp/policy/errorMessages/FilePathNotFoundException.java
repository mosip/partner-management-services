package io.mosip.pmp.policy.errorMessages;

import io.mosip.kernel.core.exception.BaseUncheckedException;

public class FilePathNotFoundException extends BaseUncheckedException {
	

	private static final long serialVersionUID = -5811206135953350643L;
	
	public FilePathNotFoundException(String errorCode, String errorMessage)
	{
		super(errorCode, errorMessage);
	}
	
	public FilePathNotFoundException(String errorCode, String errorMessage, Throwable rootCause)
	{
		super(errorCode, errorMessage, rootCause);
	}

}
