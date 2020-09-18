package io.mosip.pmp.partnermanagement.constant;

public enum InvalidInputParameterConstant {
	
	INVALIED_INPUT_PARAMETER("PMS_COR_002","Invalid Input Parameter"),
	POLICY_REQUEST_ALREADY_APPROVED("PMS_PM_034","Policy request already approved."),
  POLICY_REQUEST_ALREADY_REJECTED("PMS_PM_034","Policy request already rejected.");

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
	InvalidInputParameterConstant(String errorCode, String errorMessage) {
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
