package io.mosip.pmp.partnermanagement.constant;

/**
 * @author sanjeev.shrivastava
 *
 */
public enum PartnerAPIKeyDoesNotExistConstant {
	
	PARTNER_API_KEY_DOES_NOT_EXIST_EXCEPTION("PMS_PMP_011","Partner api key does not exist"),
	PARTNER_API_KEY_NOT_MAPPED("PMS_PMP_009", "For given partner and apikey mapping not exists.");

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
	PartnerAPIKeyDoesNotExistConstant(String errorCode, String errorMessage) {
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
