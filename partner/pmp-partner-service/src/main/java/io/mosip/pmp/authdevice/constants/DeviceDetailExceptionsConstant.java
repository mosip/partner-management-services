package io.mosip.pmp.authdevice.constants;


public enum DeviceDetailExceptionsConstant {

	REG_DEVICE_SUB_TYPE_NOT_FOUND("PMS_AUT_001","Reg Device Sub Type Code not found in the list of Reg Device Sub Types"), 
	DEVICE_PROVIDER_NOT_FOUND("PMS_AUT_002","Device provider id not found from the partners list"), 
	DEVICE_DETAIL_EXIST("PMS_AUT_003","Device Details already exists"),
	DUPLICATE_REQUEST("PMS_AUT_004","Duplicate request received"), 
	DEVICE_DETAIL_NOT_FOUND("PMS_AUT_005","Device detail for id %s does not exist"),
	DEVICE_STATUS_CODE("PMS_AUT_025","ApprovalStatus should be Activate / De-activate");
	
	/**
	 * The error code.
	 */
	private String errorCode;

	/**
	 * The error message.
	 */
	private String errorMessage;

	/**
	 * Constructor for partnerIdExceptionConstant.
	 * 
	 * @param errorCode
	 *            the errorCode.
	 * @param errorMessage
	 *            the errorMessage.
	 */
	DeviceDetailExceptionsConstant(String errorCode, String errorMessage) {
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
