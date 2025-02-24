package io.mosip.pms.partner.manager.constant;

public enum ErrorCode {

	INVALIED_INPUT_PARAMETER("PMS_COR_002", "Invalid Input Parameter"),
	POLICY_REQUEST_ALREADY_APPROVED("PMS_PM_034", "Policy request already approved."),
	POLICY_REQUEST_ALREADY_REJECTED("PMS_PM_035", "Policy request already rejected."),
	INVALID_STATUS_CODE("PMS_PM_045", "Status should be either Approved or Rejected"),
	EXTRACTORS_NOT_PRESENT("PMS_PM_051", "Extractors are not present. Please add extractors."),
	INVALID_STATUS_CODE_ACTIVE_DEACTIVE("PMS_PM_058", "Status should be either Active or De-Active"),
	NEW_POLICY_ID_NOT_EXIST("PMS_PMP_010","Policy does not belong to the Policy Group of the Partner Manger"),
	NO_PARTNER_API_KEY_REQUEST_EXCEPTION("PMS_PMP_015","No Partner api key requests for the Policy Group"),
	PARTNER_API_DOES_NOT_EXIST_EXCEPTION("PMS_PMP_007","Partner api key does not exist"),
	PARTNER_API_DOES_NOT_BELONGS_TO_THE_POLICYGROUP_OF_PARTNERMANAGER_EXCEPTION(
			"PMS_PMP_009","Partner api key does not belong to the Policy Group of the Partner Manger"),
	PARTNER_API_KEY_DOES_NOT_EXIST_EXCEPTION("PMS_PMP_011","Partner api key does not exist"),
	PARTNER_API_KEY_NOT_MAPPED("PMS_PMP_009", "For given partner and apikey mapping not exists."),
	PARTNER_DOES_NOT_EXIST_EXCEPTION("PMS_PMP_013","Partner does not exist"),
	PARTNER_ID_DOES_NOT_EXIST_EXCEPTION("PMS_PMP_005","Partner ID does not exist"),
	MISSING_PARTNER_MANAGEMENT_INPUT_PARAMETER("PMS_COR_001","Missing Input Parameter"),
	PARTNER_NOT_ACTIVE_EXCEPTION("PMS_PMP_016","Partner is not active."),
	PARTNER_NOT_MAPPED_TO_POLICY_EXCEPTION("PMS_PMP_017","Partner is not mapped to any policy."),
	PARTNER_POLICY_EXPIRED_EXCEPTION("PMS_PMP_018","Partner policy got expired."),
	PARTNER_POLICY_NOT_ACTIVE_EXCEPTION("PMS_PMP_019","Partner policy is not active."),
	MISP_LICENSE_KEY_NOT_EXISTS("PMS_PMP_020","MISP license key not exists."),
	MISP_LICENSE_KEY_EXPIRED("PMS_PMP_021","MISP license key is expired."),
	POLICY_GROUP_NOT_EXISTS("PMS_PMP_022","Policy group not exists."),
	POLICY_GROUP_NOT_ACTIVE("PMS_PMP_023","Policy group is not active."),
	POLICY_FILE_PARSING_ERROR("PMS_PMP_024","Policy file is corrupted."),
	MISP_IS_BLOCKED("PMS_PMP_025","License key of MISP is blocked"),
	PARTNER_APIKEY_POLICY_MAPPING_NOTEXISTS("PMS_PMP_50","Given apikey,partner and policy mapping not exists."),
	POLICY_NOT_BELONGS_TO_PARTNER_POLICY_GROUP("PMS_PMP_51","Given policy is not belongs to partner's policy group"),
	PARTNER_CERTIFICATE_NOT_EXISTS("PMS_PMP_052","Partner Certificate not available"),
	POLICY_NOT_EXIST_EXCEPTION("PMS_PMP_014","Policy does not exist"),
	PARTNER_APIKEY_NOT_ACTIVE_EXCEPTION("PMS_PMS_022","Given Partner api key is not active."),
	POLICY_NOT_ACTIVE_EXCEPTION("PMS_PMP_019","Policy is not active."),
	POLICY_EXPIRED_EXCEPTION("PMS_PMP_018","Policy expired."),
	POLICY_PARSING_ERROR("PMS_POL_052","Error occured while parsing policy string to json object"),
	CERTIFICATE_NOT_UPLOADED_EXCEPTION("PMS_PRT_108","Certficate is not uploaded for the given partner.Cannot activate the same."),
	PARTNER_POLICY_MAPPING_NOT_EXISTS("PMS_PRT_109","Given policy is not mapped to partner"),
	PARTNER_POLICY_LABEL_EXISTS("PMS_PRT_110","Given label already exists.Provide unique label."),
	PARTNER_POLICY_LABEL_NOT_EXISTS("PMS_PRT_111","API key not exists for the given combination"),
	LOGGEDIN_USER_NOT_AUTHORIZED("PMS_PRT_055","User not authorized."),
	JSON_NOT_VALID("PMS_PRT_096","Json is not valid");
	

	/**
	 * The error code.
	 */
	private String errorCode;

	/**
	 * The error message.
	 */
	private String errorMessage;

	/**
	 * 
	 * 
	 * @param errorCode    the errorCode.
	 * @param errorMessage the errorMessage.
	 */
	ErrorCode(String errorCode, String errorMessage) {
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
