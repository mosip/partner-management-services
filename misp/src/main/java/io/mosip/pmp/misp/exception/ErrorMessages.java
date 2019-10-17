package io.mosip.pmp.misp.exception;

/**
 * <p> This enum contains all the error messages with codes.</p>
 * 
 * @author Nagarjuna Kuchi
 * @version 1.0
 */
public enum ErrorMessages {
	
	MISP_EXISTS("PMS_MSP_003","MISP already registred with name :"),
	INTERNAL_SERVER_ERROR("PMS_COR_003","Could not process the request"),
	MISSING_INPUT_PARAMETER("PMS_COR_001","Missing Input Parameter - "),
	INVALID_INPUT_PARAMETER("PMS_COR_002","Invalid Input Parameter - "),
	MISP_ID_NOT_EXISTS("PMS_MSP_005","MISP ID does not exist"),
	MISP_LICENSE_KEY_NOT_EXISTS("PMS_MSP_006","MISP License Key does not exis"),
	MISP_LICENSE_KEY_NOT_ASSOCIATED_MISP_ID("PMS_MSP_007","MISP License key not associated to MISP ID"),
	NO_MISP_DETAILS("PMS_MSP_012", "No MISP details found");

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

private ErrorMessages(final String errorCode, final String errorMessage) {
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
