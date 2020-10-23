package io.mosip.pmp.partner.constant;

public enum PartnerExceptionConstants {

	CERTIFICATE_NOT_UPLOADED_EXCEPTION("PMS_PRT_108","Certficate is not uploaded for the selected partner."),
	PARTNER_ID_LENGTH_EXCEPTION("PMS_PRT_052","PartnerId max length should be "),
	IO_EXCEPTION("PMS_ATH_053", "IO Exception occured while passing paging request"),
	USER_NOT_FOUND("PMS_ATH_054", "User not found"),
	SERVER_ERROR("PMS_ATH_500", "Server error occured,Please check the logs "),
	PARTNER_POLICY_MAPPING_NOT_EXISTS("PMS_PRT_061","Partner policy mapping not exists."),
	PARTNER_API_KEY_REQUEST_APPROVED("PMS_PRT_062","Partner api key request is approved already. Cann't add extractors now."),
	EXTRACTORS_ONLY_FOR_CREDENTIAL_PARTNER("PMS_PRT_063","Biometric extractors can be added only for Credential_Partner"),
	NO_DETAILS_FOUND("PMS_PRT_064","No details found");
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
	PartnerExceptionConstants(String errorCode, String errorMessage) {
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
