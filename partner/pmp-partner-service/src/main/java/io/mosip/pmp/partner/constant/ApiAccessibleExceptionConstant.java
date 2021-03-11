package io.mosip.pmp.partner.constant;

public enum ApiAccessibleExceptionConstant {

	API_NOT_ACCESSIBLE_EXCEPTION("PMS_KKS_001", "API not accessible  "),
	API_NULL_RESPONSE_EXCEPTION("PMS_PRT_107","Responese from the api is null"),
	UNABLE_TO_PROCESS("PMS_PRT_500","Unable to process the request.");

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
	ApiAccessibleExceptionConstant(String errorCode, String errorMessage) {
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
