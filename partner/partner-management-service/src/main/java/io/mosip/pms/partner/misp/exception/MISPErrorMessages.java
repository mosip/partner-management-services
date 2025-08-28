package io.mosip.pms.partner.misp.exception;

public enum MISPErrorMessages {
	
	MISP_EXISTS("PMS_MSP_402","MISP already registred with name :"),
	INTERNAL_SERVER_ERROR("PMS_COR_003","Could not process the request"),
	MISSING_INPUT_PARAMETER("PMS_COR_001","Missing Input Parameter - "),
	INVALID_INPUT_PARAMETER("PMS_COR_002","Invalid Input Parameter - "),
	MISP_ID_NOT_EXISTS("PMS_MSP_005","MISP Partner does not exist"),
	MISP_ID_NOT_VALID("PMS_MSP_009","Given provider is not valid"),
	MISP_LICENSE_KEY_NOT_ASSOCIATED_MISP_ID("PMS_MSP_408","MISP Partner and License key combintaion not exists."),
	MISP_IS_INACTIVE("PMS_MSP_405", "MISP partner is not active."),
	MISP_STATUS_CODE_EXCEPTION("PMS_MSP_407","mispStatus either Active or De-active."),
	MISP_LICENSE_ARE_NOT_ACTIVE("PMS_MSP_413","misp license all are inactive."),
	MISPID_FETCH_EXCEPTION("PMP-MSP-001", "Error Occur While Fetching Id"),
	MISP_LICENSE_KEY_EXISTS("PMS_MSP_416","License key exists for the given provider."),
	MISP_POLICY_NOT_MAPPED("PMS_MSP_417", "Policy not mapped."),
	MISP_POLICY_NOT_APPROVED("PMS_MSP_418", "Policy not approved."),
	MISP_POLICY_NOT_EXISTS("PMS_MSP_419", "Policy not exists.");


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
