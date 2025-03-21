package io.mosip.pms.common.constant;

public enum ValidationErrorCode {


	NO_FILTER_FOUND("PMS-MSD-316", "Column %s doesn't support filter"),
	COLUMN_DOESNT_EXIST("PMS-MSD-317", "Column %s doesn't exist for the searched entity"),
	FILTER_NOT_SUPPORTED("PMS-MSD-318", "Column %s doesn't support filter type %s"),
	INVALID_COLUMN_VALUE("PMS-MSD-319", "Column value is null or empty"),
	NO_FILTER_COLUMN_FOUND("PMS-MSD-322", "Filter Type cannot be empty or null"),
	FILTER_COLUMN_DOESNT_EXIST("PMS-MSD-323", "Filter Type for column %s is not supported"),
	FILTER_COLUMN_NOT_SUPPORTED("PMS-MSD-324", "Received Filter Type not supported"),
	COLUMN_DOESNT_EXIST_FILTER("PMS-MSD-348", "Received column does not support filter"),
	CONSTRAINT_VIOLATION("PMS-MSD-xxx", "Contraint Violation-"),
	INVALID_COLUMN_NAME("PMS-MSD-325", "Invalid Column Name passed"),
	INVALID_REQUEST_PARAM("PMS_REQUEST_ERROR_001", "Invalid request."),
	INVALID_REQUEST_ID("PMS_REQUEST_ERROR_002", "Invalid Request Id"),
	INVALID_REQUEST_VERSION("PMS_REQUEST_ERROR_003", "Request version is invalid"),
	INVALID_REQUEST_DATETIME("PMS_REQUEST_ERROR_004", "Invalid request time"),
	INVALID_REQUEST_BODY("PMS_REQUEST_ERROR_005", "Request body is invalid"),
	INVALID_REQUEST_DATETIME_NOT_CURRENT_DATE("PMS_REQUEST_ERROR_006", "Request date should be current date");

	/**
	 * Error Code
	 */
	private final String errorCode;
	/**
	 * Error Message
	 */
	private final String errorMessage;

	/**
	 * Constructor to initialize
	 * 
	 * @param errorCode    validation error code
	 * @param errorMessage validation error message
	 */
	private ValidationErrorCode(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * Method to fetch error code
	 * 
	 * @return error Code
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Method to fetch error message
	 * 
	 * @return error message
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
}
