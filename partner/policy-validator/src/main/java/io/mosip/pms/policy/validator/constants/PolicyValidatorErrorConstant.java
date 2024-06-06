package io.mosip.pms.policy.validator.constants;

import lombok.Getter;

@Getter
public enum PolicyValidatorErrorConstant {
	SCHEMA_IO_EXCEPTION("PMS_PV_001", "Failed to read schema"),

	POLICY_VALIDATION_FAILED("PMS_PV_002", "Policy Object validation failed"),

	POLICY_PARSING_FAILED("PMS_PV_003", "Failed to parse/convert policy Object"),

	INVALID_POLICY_SCHEMA("PMS_PV_004", "Invalid policy schema"),
	
	INVALID_INPUT_PARAMETER("PMS_PV_005", "Invalid input parameter - %s in policy data"),

	MISSING_INPUT_PARAMETER("PMS_PV_006", "Missing input parameter - %s in policy data");

    /**
     * -- GETTER --
     *  Gets the error code.
     *
     * @return the error code
     */
    private final String errorCode;

    /**
     * -- GETTER --
     *  Gets the message.
     *
     * @return the message
     */
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

}
