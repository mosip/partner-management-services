package io.mosip.pms.policy.validator.constants;

public enum PolicyValidatorErrorConstant {
	SCHEMA_IO_EXCEPTION("PMS_PV_001", "Failed to read schema"),

	POLICY_VALIDATION_FAILED("PMS_PV_002", "Policy Object validation failed"),

	POLICY_PARSING_FAILED("PMS_PV_003", "Failed to parse/convert policy Object"),

	INVALID_POLICY_SCHEMA("PMS_PV_004", "Invalid policy schema"),
	
	INVALID_INPUT_PARAMETER("PMS_PV_005", "Invalid input parameter - %s in policy data"),

	MISSING_INPUT_PARAMETER("PMS_PV_006", "Missing input parameter - %s in policy data"),

	EXPECTED_INPUT_PARAMETER("PMS_PV_007", "Format Error: Expected format is %s."),

	EMPTY_ARRAY_INPUT_PARAMETER("PMS_PV_008", "Empty array is not allowed in %s."),

	EMPTY_STRING_INPUT_PARAMETER("PMS_PV_009", "Empty string is not allowed in %s.");

	private final String errorCode;

	private final String message;

	/**
	 * Instantiates a new json validator error constant.
	 *
	 * @param errorCode the error code
	 * @param message   the message
	 */
	PolicyValidatorErrorConstant(final String errorCode, final String message) {
		this.errorCode = errorCode;
		this.message = message;
	}

	/**
	 * Gets the error code.
	 *
	 * @return the error code
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

}
