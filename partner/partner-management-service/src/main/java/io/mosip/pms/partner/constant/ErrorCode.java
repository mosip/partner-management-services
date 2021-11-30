package io.mosip.pms.partner.constant;

public enum ErrorCode {

	MISSING_PARTNER_INPUT_PARAMETER("PMS_COR_001","Missing Input Parameter"),
	POLICY_GROUP_DOES_NOT_EXIST("PMS_PRT_002", "Policy group does not exist"),
	PARTNER_TYPE_DOES_NOT_EXIST("PMS_PRT_010", "Partner Type does not exist"),
	INVALID_COLUMN("PMS-PRT-310", "Invalid column received : %s"),
	INVALID_PAGINATION_VALUE("PMS-PRT-313", "Invalid pagination value received pagestart:%d and pagefetch:%d"),
	FILTER_TYPE_NOT_AVAILABLE("PMS-PRT-312", "Filter type is missing"),
	MISSING_FILTER_COLUMN("PMS-PRT-311", "Column is missing in request"),
	INVALID_SORT_INPUT("PMS-PRT-314", "Missing sort field or sort type values"),
	INVALID_BETWEEN_VALUES("PMS-PRT-315", "Invalid fromValue or toValue"),
	INVALID_PAGINATION("PMS-PRT-356", "Pagination cannot be null"),
	INVALID_SORT_TYPE("PMS-PRT-358", "Sort type %s is not supported"),
	ERROR_OCCURED_WHILE_SORTING("PMS-PRT-359", "Error occured while sorting"),
	INVALID_SORT_FIELD("PMS-PRT-357", "Invalid sort field %s"), 
	INVALID_VALUE("KER-PRT-390", "Invalid filter value"),
	PARTNER_ALREADY_REGISTERED_EXCEPTION("PMS_PRT_001",  "A Partner is already registered with the same Name"),
	PARTNER_ALREADY_REGISTERED_WITH_ID_EXCEPTION("PMS_PRT_051", "A Partner is already registered with the same Id"),
	CERTIFICATE_NOT_UPLOADED_EXCEPTION("PMS_PRT_108","Certficate is not uploaded for the selected partner."),
	PARTNER_ID_LENGTH_EXCEPTION("PMS_PRT_052","PartnerId max length should be "),
	IO_EXCEPTION("PMS_ATH_053", "IO Exception occured while passing paging request"),
	USER_NOT_FOUND("PMS_ATH_054", "User not found"),
	SERVER_ERROR("PMS_ATH_500", "Server error occured,Please check the logs "),
	PARTNER_POLICY_MAPPING_NOT_EXISTS("PMS_PRT_061","Partner policy mapping not exists."),
	PARTNER_API_KEY_REQUEST_APPROVED("PMS_PRT_062","Partner api key request is approved already. Cann't add extractors now."),
	EXTRACTORS_ONLY_FOR_CREDENTIAL_PARTNER("PMS_PRT_063","Biometric extractors can be added only for:"),
	NO_DETAILS_FOUND("PMS_PRT_064","No details found"),
	CREDENTIAL_NOT_ALLOWED_PARTNERS("PMS_PRT_070","Credential mapping allowed only for :"),
	POLICY_PARSING_ERROR("PMS_PRT_071","Error occured while parsing policy string to json object"),
	CREDENTIAL_TYPE_NOT_ALLOWED("PMS_PRT_072","Given credential type is not allowed. Allowed credential types : "),
	POLICY_NOT_EXIST("PMS_PRT_073","Policy not exists."),
	EMAIL_EXISTS_IN_KEYCLOAK("PMS_PRT_074","User exists with same email(keycloak)"),
	PARTNER_DOES_NOT_EXIST_EXCEPTION("PMS_PRT_005","Partner does not exist"),
	PARTNER_API_KET_REQ_DOES_NOT_EXIST_EXCEPTION("PMS_PRT_005","No api key req exist"),
	PARTNER_API_NOT_CREATED_EXCEPTION("PMS_PRT_105","Partner API KEY is not Created"),
	PARTNER_ALREADY_REG_WITH_SAME_PLICYGROUP("PMS_PRT_001","Partner is already registered with Same policy Group"),
	INVALID_PARTNER_INPUT_PARAMETER("PMS_COR_002","Invalid Input Parameter - for all attributes not as per defined data definition"),
	EMAIL_ALREADY_EXISTS_EXCEPTION("PMS_PRT_011", "Email is already exists."),
	INVALID_EMAIL_ID_EXCEPTION("PMS_PRT_012", "Invalid emailId."),
	INVALID_MOBILE_NUMBER_EXCEPTION("PMS_PRT_013", "Invalid mobile number.Length should be less than ."),
	AUTHENTICATION_FAILED("PMS-ATH-401","Authentication Failed"),
	APIKEYREQIDSTATUSINPROGRESS("PMS_PRT_106","APIKeyReqID status is In-progress"),
	API_NOT_ACCESSIBLE_EXCEPTION("PMS_KKS_001", "API not accessible  "),
	API_NULL_RESPONSE_EXCEPTION("PMS_PRT_107","Responese from the api is null"),
	UNABLE_TO_PROCESS("PMS_PRT_500","Unable to process the request."),
	PARTNER_NOT_ACTIVE_EXCEPTION("PMS_PMP_016","Partner is not active."),
	POLICY_GROUP_POLICY_NOT_EXISTS("PMS_PRT_098","Given policy under partner's policy group not exists."),
	POLICY_NOT_ACTIVE_EXCEPTION("PMS_PMP_019","Policy is not active."),
	POLICY_GROUP_NOT_ACTIVE("PMS_PMP_023","Policy group is not active."),
	POLICY_EXPIRED_EXCEPTION("PMS_PMP_018","Policy expired."),
	DATASHARE_RESPONSE_NULL("PMS_DTS_001",  "DataShare response is null"),
	P7B_CERTDATA_PARSING_ERROR("PMS_PRT_045","Error occured while parsing P7B certificate data"),
	P7B_CERTDATA_ERROR("PMS_PRT_046","Error occured while extracting the leaf cert."),
	PARTNER_NOT_MAPPED_TO_POLICY_GROUP("PMS_PRT_047","Partner is not mapped to policy group"),
	POLICY_GROUP_NOT_REQUIRED("PMS_PRT_048","policy group mapping is not required for given partner"),
	POLICY_GROUP_ALREADY_MAPPED("PMS_PRT_049","policy group mapping exists for given partner."),
	POLICY_GROUP_NOT_MAP_ACTIVE_PARTNER("PMS_PRT_050","Policy group cannot be updated for approved partner"),
	POLICY_GROUP_NOT_MAPPED_PARTNER("PMS_PRT_051","Policy group not mapped for given partner"),
	LOGGEDIN_USER_NOT_AUTHORIZED("PMS_PRT_052","User not authorized."),
	PARTNER_POLICY_MAPPING_EXISTS("PMS_PRT_053","Active mapping exists for given policy and partner.");

	
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
	 * @param errorCode
	 *            the errorCode.
	 * @param errorMessage
	 *            the errorMessage.
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
