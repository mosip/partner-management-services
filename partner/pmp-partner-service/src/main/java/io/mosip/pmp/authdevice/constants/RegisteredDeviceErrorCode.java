package io.mosip.pmp.authdevice.constants;

public enum RegisteredDeviceErrorCode {
	FOUNDATIONAL_VALUE("PMS_AUT_008", "Error occured while validating Foundational"), 
	PURPOSE_VALIDATION_EXCEPTION("PMS_AUT_009", "Error occured while validating Foundational"),
	CERTIFICATION_LEVEL_VALIDATION_EXCEPTION("PMS_AUT_010", "Error occured while validating Certification Level"),
	DEVICE_DATA_NOT_EXIST("PMS_AUT_011", "Device data can't be null"),
	TIMESTAMP_AFTER_CURRENTTIME("PMS_AUT_012", "Time Stamp input is %s min after the current timestamp"),
	TIMESTAMP_BEFORE_CURRENTTIME("PMS_AUT_013", "Time Stamp input is %s min before the current timestamp"),
	DEVICE_DETAIL_NOT_EXIST("PMS_AUT_014", "Device Detail  does not exist in the list of Registered Device Details"),
	SERIALNO_DEVICEDETAIL_ALREADY_EXIST("PMS_AUT_015", "Serial no and Device detail already exist"),
	REGISTERED_DEVICE_INSERTION_EXCEPTION("PMS_AUT_016", "error occured while saving data"),
	DEVICE_REGISTER_NOT_FOUND_EXCEPTION("PMS_AUT_016", "Registered device not found"), 
	DEVICE_DE_REGISTERED_ALREADY("PMS_AUT_024", "device already deregistered"),
	DEVICE_CODE_EXCEEDS_LENGTH("PMS_AUT_025", "device code greater than the accepted length"),
	INVALID_ENV("PMS_AUT_024", "invalid environment"),
	DEVICE_REGISTER_DELETED_EXCEPTION("PMS_AUT_017", "error occured while de registering device"),
	DEVICE_DETAIL_NOT_FOUND("PMS_AUT_018", "device detail not found"),
	FTP_NOT_FOUND("PMS_AUT_019", "FTP not found"), 
	REGISTERED_DEVICE_SIGN_VALIDATION_EXCEPTION("PMS_AUT_021", "error occured while doing signature validation"),
	REGISTERED_DEVICE_SIGN_VALIDATION_FAILURE("PMS_AUT_022", " signature validation failed -"),
	SERIALNUM_NOT_EXIST("PMS_AUT_020", "serial number does not exist"),
	API_RESOURCE_EXCEPTION("PMS_AUT_023","error occured while fetching api resource -->");
	

	private final String errorCode;
	private final String errorMessage;

	private RegisteredDeviceErrorCode(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
}
