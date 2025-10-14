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
	MISP_POLICY_NOT_EXISTS("PMS_MSP_419", "Policy not exists."),
	ERROR_FETCHING_MISP_DETAILS("PMS_MSP_420","Error while fetching all MISP Licence details."),
	ERROR_GENERATING_MISP_LICENSE("PMS_MSP_421","Error while generating MISP license key."),
	INVALID_LICENSE_KEY_NAME("PMS_MSP_422","Invalid license key name provided."),
	INVALID_PARTNER_ID("PMS_MSP_423","Invalid Partner ID provided."),
	MISP_LICENSE_KEY_NAME_EXISTS("PMS_MSP_424","MISP License key name already exists."),
	EXPIRYDATE_SHOULD_BE_GREATER_THAN_TODAYS_DATE("PMS_MSP_425","Expiry date cannot be the same or earlier than the current date."),
	ERROR_FETCHING_INDIVIDUAL_MISP_DETAILS("PMS_MSP_426","Error while fetching MISP Licence details."),
	MISP_LICENSE_NOT_EXISTS("PMS_MSP_427","No matching MISP License Key exists for the specified Partner ID, Policy ID, and MISP License Key Name."),
	MULTIPLE_MISP_LICENSES_FOUND("PMS_MSP_428","Multiple MISP License key matches were found for the provided Partner ID, Policy ID, and MISP License Key Name. Please use the correct MISP License Key Name and Policy ID."),
	PARTNER_ID_NOT_EXISTS("PMS_MSP_429","Partner ID does not exist."),
	PARTNER_NOT_ACTIVE("PMS_MSP_430","Partner is not active."),
	INVALID_PARTNER_TYPE("PMS_MSP_431","Invalid Partner Type. The Partner Type should be 'MISP_Partner'."),
	ERROR_DEACTIVATING_MISP_LICENSE("PMS_MSP_432","Error while deactivating MISP License key."),
	DEACTIVATE_STATUS_CODE("PMS_MSP_433", "Request status should be De-Activate"),
	MISP_LICENSE_ALREADY_DEACTIVATED("PMS_MSP_434","MISP License key is already deactivated."),
	ERROR_REGENERATING_MISP_LICENSE("PMS_MSP_435","Error while regenerating MISP License key."),
	INVALID_EXPIRY_DATE("PMS_MSP_436","Expiry date cannot be empty or null."),
	POLICY_ID_NOT_EXISTS("PMS_MSP_437","The entered policy ID is invalid or does not exist in the database."),
	PARTNER_POLICY_NOT_APPROVED("PMS_MSP_438","The policy ID provided has not been approved for partner policy linking."),
	MISP_LICENSE_NOT_FOUND("PMS_MSP_439","No Active MISP License key found for the given Partner ID and Policy ID.");

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
