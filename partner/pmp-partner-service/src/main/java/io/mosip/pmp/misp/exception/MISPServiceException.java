package io.mosip.pmp.misp.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.kernel.core.exception.BaseUncheckedException;

public class MISPServiceException extends BaseUncheckedException {
	
	private static final Logger logger = LoggerFactory.getLogger(MISPServiceException.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -816564124301416073L;

	public MISPServiceException(String errorCode, String errorMessage)
	{		
		super(errorCode, errorMessage);
		logger.error(errorCode,errorMessage);
	}
	
	public MISPServiceException(String errorCode, String errorMessage, Throwable rootCause)
	{
		super(errorCode, errorMessage, rootCause);
		logger.error(errorCode,errorMessage);
	}

	public MISPServiceException(String errorCode, Exception e) {
		super(errorCode,errorCode,e);		
	}
}
