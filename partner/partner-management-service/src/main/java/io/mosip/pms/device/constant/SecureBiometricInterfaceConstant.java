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
	SBI_NOT_APPROVED("PMS_AUT_511","SBI for which device is being mapped is not approved"),
	SBI_RECORDS_EXISTS("PMS_AUT_512","SBI details exists for given SBI Version. Multiple SBI with same SBI Version cannot be added."),
	SBI_EXPIRED("PMS_AUT_513", "SBI for which device is being mapped is expired"),
	SWCREATEDDATE_SHOULD_BE_PAST_OR_TODAY("PMS_AUT_514","The created date should be less than or equal to the current date."),
	EXPIRYDATE_SHOULD_NOT_BE_GREATER_THAN_TEN_YEARS("PMS_AUT_515", "Expiry date should not be greater than %s years from today"),
	CREATEDDATE_SHOULD_NOT_BE_LESS_THAN_TEN_YEARS("PMS_AUT_516", "Created date should not be less than %s years from today"),
	SBI_ALREADY_APPROVED("PMS_AUT_517", "The selected SBI has been already approved"),
	SBI_ALREADY_REJECTED("PMS_AUT_518", "The selected SBI has been already rejected");
	
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
