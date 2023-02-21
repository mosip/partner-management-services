package io.mosip.pms.common.constant;

/**
 * Error codes for masterdata search
 * 
 * @author Abhishek Kumar
 * @since 1.0.0
 */
public enum SearchErrorCode {
	INVALID_COLUMN("PMS-MSD-310", "Invalid column received : %s"),
	INVALID_PAGINATION_VALUE("PMS-MSD-313", "Invalid pagination value received pagestart:%d and pagefetch:%d"),
	FILTER_TYPE_NOT_AVAILABLE("PMS-MSD-312", "Filter type is missing"),
	MISSING_FILTER_COLUMN("PMS-MSD-311", "Column is missing in request"),
	INVALID_SORT_INPUT("PMS-MSD-314", "Missing sort field or sort type values"),
	INVALID_BETWEEN_VALUES("PMS-MSD-315", "Invalid fromValue or toValue"),
	INVALID_PAGINATION("PMS-MSD-356", "Pagination cannot be null"),
	INVALID_SORT_TYPE("PMS-MSD-358", "Sort type %s is not supported"),
	ERROR_OCCURED_WHILE_SORTING("PMS-MSD-359", "Error occured while sorting"),
	INVALID_COLUMN_VALUE("PMS-MSD-316", "Invalid value present for the given column"),
	INVALID_SORT_FIELD("PMS-MSD-357", "Invalid sort field %s"), 
	INVALID_VALUE("PMS-MSD-390", "Invalid filter value"),
	INVALID_VALUES("PMS-MSD-391", "Invalid filter values"),
	FAILED_TO_FETCH_CLAIMS("PMS-MSD-393","Failed to fetch claims from mapping file"),
	FAILED_TO_FETCH_ACRVALUES("PMS-MSD-394","failed to fetch acr values from mapping file"),
	INVALID_VALUE_VALUES("PMS-MSD-392", "Both value and values cannot be present");
     
	/**
	 * The error code.
	 */
	private final String errorCode;
	/**
	 * The error message.
	 */
	private final String errorMessage;

	/**
	 * Constructor for MasterdataSearchErrorCode.
	 * 
	 * @param errorCode    the error code.
	 * @param errorMessage the error message.
	 */
	private SearchErrorCode(final String errorCode, final String errorMessage) {
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
