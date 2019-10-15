package io.mosip.pmp.policy.errorMessages;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * @author Nagarjuna
 *
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
		
	}

	public PolicyManagementServiceException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
		
	}

	public PolicyManagementServiceException(String errorMessage) {
		super(errorMessage);
		
	}
	
}
