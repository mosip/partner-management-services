package io.mosip.pmp.policy.errorMessages;

import io.mosip.kernel.core.exception.BaseUncheckedException;
 

public class MethodArgumentNotValidException extends BaseUncheckedException 
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1505620043097643048L;

	public MethodArgumentNotValidException(String errorCode, String errorMessage)
	{
		super(errorCode, errorMessage);
	}
	
	public MethodArgumentNotValidException(String errorCode, String errorMessage, Throwable rootCause)
	{
		super(errorCode, errorMessage, rootCause);
	}
}
