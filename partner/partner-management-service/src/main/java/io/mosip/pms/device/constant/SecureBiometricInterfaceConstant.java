package io.mosip.pms.device.constant;

public enum SecureBiometricInterfaceConstant {
	 
	DEVICE_DETAIL_INVALID("PMS_AUT_006","Invalid device detail"),
	SBI_NOT_FOUND("PMS_AUT_007","Secure Biometric Interface not found for the id %s"),
	SBI_STATUS_CODE("PMS_AUT_026","ApprovalStatus should be Activate / De-activate"),
	DIFFERENT_DEVICE_PROVIDERS("PMS_AUT_506","Provide device details of same provider."),
	SWCREATEDDATE_SHOULD_BE_LESSTHAN_EXPIRYDATE("PMS_AUT_507","ExpiryDate should be greaterthan createdDate"),
	EXPIRYDATE_SHOULD_BE_GREATERTHAN_TODAYSDATE("PMS_AUT_508","ExpiryDate should be greaterthan/equal Today."),
	DD_SBI_PROVIDER_NOT_MATCHING("PMS_AUT_509","Device Details and SBI do not belongs to same provider."),
	DD_SBI_MAPPING_NOT_EXISTS("PMS_AUT_510","Mapping not exists for given data."),
	SBI_NOT_APPROVED("PMS_AUT_511","Given sbi details are approved"),
	SBI_RECORDS_EXISTS("PMS_AUT_512","Sbi details exists for given version. Can't add the records with the same version.");
	
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
	SecureBiometricInterfaceConstant(String errorCode, String errorMessage) {
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
