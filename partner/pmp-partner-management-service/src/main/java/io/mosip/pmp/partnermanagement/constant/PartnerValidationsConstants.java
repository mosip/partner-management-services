package io.mosip.pmp.partnermanagement.constant;

/**
 * @author sanjeev.shrivastava
 *
 */
public enum PartnerValidationsConstants {
	
	PARTNER_NOT_ACTIVE_EXCEPTION("PMS_PMP_016","Partner is not active."),
	PARTNER_NOT_MAPPED_TO_POLICY_EXCEPTION("PMS_PMP_017","Partner is not mapped to any policy."),
	PARTNER_POLICY_EXPIRED_EXCEPTION("PMS_PMP_018","Partner policy got expired."),
	PARTNER_POLICY_NOT_ACTIVE_EXCEPTION("PMS_PMP_019","Partner policy is not active."),
	MISP_LICENSE_KEY_NOT_EXISTS("PMS_PMP_020","MISP license key not exists."),
	MISP_LICENSE_KEY_EXPIRED("PMS_PMP_021","MISP license key is expired."),
	POLICY_GROUP_NOT_EXISTS("PMS_PMP_022","Policy not exists."),
	POLICY_GROUP_NOT_ACTIVE("PMS_PMP_023","Policy is not active."),
	POLICY_FILE_PARSING_ERROR("PMS_PMP_024","Policy file is corrupted."),
	MISP_IS_BLOCKED("PMS_PMP_025","License key of MISP is blocked");
	
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
	PartnerValidationsConstants(String errorCode, String errorMessage) {
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
