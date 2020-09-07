package io.mosip.pmp.authdevice.constants;

public enum SecureBiometricInterfaceConstant {
	 
	DEVICE_DETAIL_INVALID("PMS_AUT_006","Invalid device detail"),
	SBI_NOT_FOUND("PMS_AUT_007","Secure Biometric Interface not found for the id %s"),
	SBI_STATUS_CODE("PMS_AUT_026","ApprovalStatus should be Activate / De-activate");
	
	/**
	 * The error code.
	 */
	private String errorCode;

	/**
	 * The error message.
	 */
	private String errorMessage;

	/**
	 * Constructor for partnerIdExceptionConstant.
	 * 
	 * @param errorCode
	 *            the errorCode.
	 * @param errorMessage
	 *            the errorMessage.
	 */
	SecureBiometricInterfaceConstant(String errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Getter for error code.
	 * 
	 * @return the error code.
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Getter for error message.
	 * 
	 * @return the error message.
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
}
