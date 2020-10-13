package io.mosip.pmp.policy.validator.constants;

public enum PolicyValidatorErrorConstant {
	SCHEMA_IO_EXCEPTION("PMS_PV_001", "Failed to read schema"),

	POLICY_VALIDATION_FAILED("PMS_PV_002", "Id Object validation failed"),

	POLICY_PARSING_FAILED("PMS_PV_003", "Failed to parse/convert Id Object"),

	INVALID_POLICY_SCHEMA("PMS_PV_004", "Invalid ID schema");

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
