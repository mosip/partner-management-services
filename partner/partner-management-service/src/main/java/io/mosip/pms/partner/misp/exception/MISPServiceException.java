package io.mosip.pms.partner.misp.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.util.PMSLogger;

public class MISPServiceException extends BaseUncheckedException {
	
	private static final Logger logger = PMSLogger.getLogger(MISPServiceException.class);
	
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
