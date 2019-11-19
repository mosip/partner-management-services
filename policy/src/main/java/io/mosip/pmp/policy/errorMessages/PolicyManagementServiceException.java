package io.mosip.pmp.policy.errorMessages;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * 
 * @author Nagarjuna
 * @version 1.0
 */
public class PolicyManagementServiceException extends BaseUncheckedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5832589186950826222L;

	public PolicyManagementServiceException() {
		super();
		
	}

	public PolicyManagementServiceException(String errorCode, String errorMessage, Throwable cause) {
		super(errorCode, errorMessage, cause);
		PolicyServiceLogger.error(errorCode,errorMessage);
		PolicyServiceLogger.error(cause.getMessage());
		
	}

	public PolicyManagementServiceException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
		PolicyServiceLogger.error(errorCode,errorMessage);
	}

	public PolicyManagementServiceException(String errorMessage) {
		super(errorMessage);
		PolicyServiceLogger.error(errorMessage);
	}
	
}
