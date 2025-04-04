package io.mosip.pms.common.constant;

public enum ApiAccessibleExceptionConstant {

	API_NOT_ACCESSIBLE_EXCEPTION("PMS_KKS_001", "Error while accessing the API.Please check the logs."),
	API_NULL_RESPONSE_EXCEPTION("PMS_PRT_107","Responese from the api is null"),
	UNABLE_TO_PROCESS("PMS_PRT_500","Unable to process the request."),
	TEMPLATE_NOT_FOUND("PMS_PRT_501","Template not found"),
	PARTNER_CERTIFICATE_FETCH_ERROR("PMS-BJ-003", "Error while fetching partner certificate"),
	TRUST_CERTIFICATES_FETCH_ERROR("PMS_CERTIFICATE_ERROR_010", "Error while fetching trust certificates."),
	DECRYPT_DATA_ERROR("PMS-BJ-012", "Failed to decrypt data using Key Manager"),
	ENCRYPT_DATA_ERROR("PMS-BJ-013", "Failed to encrypt data using Key Manager");

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
