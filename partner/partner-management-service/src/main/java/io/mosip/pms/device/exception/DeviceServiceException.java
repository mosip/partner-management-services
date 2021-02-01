package io.mosip.pms.device.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

public class DeviceServiceException extends BaseUncheckedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5802805689689326166L;

	public DeviceServiceException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
