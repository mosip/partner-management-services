package io.mosip.pms.policy.errorMessages;


import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.util.PMSLogger;

/**
 * 
 * @author Nagarjuna
 * @version 1.0
 */
public class PolicyManagementServiceException extends BaseUncheckedException {

	private static final Logger logger = PMSLogger.getLogger(PolicyManagementServiceException.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5832589186950826222L;

	public PolicyManagementServiceException() {
		super();
		
	}

	public PolicyManagementServiceException(String errorCode, String errorMessage, Throwable cause) {
		super(errorCode, errorMessage, cause);
		logger.error(errorCode,errorMessage);
		logger.error(cause.getMessage());
		
	}

	public PolicyManagementServiceException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
		logger.error(errorCode,errorMessage);
	}

	public PolicyManagementServiceException(String errorMessage) {
		super(errorMessage);
		logger.error(errorMessage);
	}
	
}
