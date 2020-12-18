package io.mosip.pmp.misp.exception;

public enum MISPErrorMessages {
	
	MISP_EXISTS("PMS_MSP_402","MISP already registred with name :"),
	INTERNAL_SERVER_ERROR("PMS_COR_003","Could not process the request"),
	MISSING_INPUT_PARAMETER("PMS_COR_001","Missing Input Parameter - "),
	INVALID_INPUT_PARAMETER("PMS_COR_002","Invalid Input Parameter - "),
	MISP_ID_NOT_EXISTS("PMS_MSP_005","MISP Partner does not exist"),
	MISP_LICENSE_KEY_NOT_EXISTS("PMS_MSP_406","MISP Partner License Key does not exists"),
	MISP_LICENSE_KEY_NOT_ASSOCIATED_MISP_ID("PMS_MSP_408","MISP Partner and License key combintaion not exists."),
	NO_MISP_DETAILS("PMS_MSP_409", "No MISP Partner details found"),
	MISP_IS_INACTIVE("PMS_MSP_405", "MISP partner is not active."),
	MISP_STATUS_CHENAGE_REQUEST_EXCEPTION("PMS_MSP_404", "Misp already "),
	STATUS_CODE_EXCEPTION("PMS_MSP_403","mispStatus either approved or rejected."),
	MISP_STATUS_CODE_EXCEPTION("PMS_MSP_407","mispStatus either Active or De-active."),
	MISP_LICENSE_KEY_STATUS_EXCEPTION("PMS_MSP_418","mispLicenseKeyStatus either Active or De-active."),
	MISP_LICENSE_EXPIRED_NOT_ACTIVATE("PMS_MSP_419","misp license is expired.Cannot activate the same."),
	MISP_NOT_APPROVED("PMS_MSP_419","misp is not yet approved."),
	MISP_LICENSE_ARE_NOT_ACTIVE("PMS_MSP_413","misp license all are inactive."),
	MISPID_FETCH_EXCEPTION("PMP-MSP-001", "Error Occur While Fetching Id"),
	MISPID_INSERTION_EXCEPTION("PMP-MSP-002", "Error Occur While Inserting Id"),
	INVALID_EMAIL_ID_EXCEPTION("PMS_MSP_401","Invalid email id."),
	MISP_LICENSE_KEY_EXISTS("PMS_MSP_416","License key exists for the given provider.");


	private final String errorCode;
	private final String errorMessage;

	/**
	 * Constructs a new errorMessages enum with the specified detail message and
	 * error code and error message.
	 *
	 * 
	 * @param errorCode    the error code
	 * @param errorMessage the detail message.
	 * @param rootCause    the specified cause
	 */

	private MISPErrorMessages(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * This method bring the error code.
	 * @return string 
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * This method brings the error message.
	 * @return string 
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
}
