package io.mosip.pms.partner.constant;

public enum ErrorCode {

	MISSING_PARTNER_INPUT_PARAMETER("PMS_COR_001","Missing Input Parameter"),
	POLICY_GROUP_DOES_NOT_EXIST("PMS_PRT_002", "Policy group does not exist"),
	POLICY_GROUP_IS_MANDATORY("PMS_PRT_108", "Policy group is Mandatory for %s"),
	PARTNER_TYPE_DOES_NOT_EXIST("PMS_PRT_010", "Partner Type does not exist"),
	INVALID_COLUMN("PMS_PRT_310", "Invalid column received : %s"),
	INVALID_PAGINATION_VALUE("PMS_PRT_313", "Invalid pagination value received pagestart:%d and pagefetch:%d"),
	FILTER_TYPE_NOT_AVAILABLE("PMS_PRT_312", "Filter type is missing"),
	MISSING_FILTER_COLUMN("PMS_PRT_311", "Column is missing in request"),
	INVALID_SORT_INPUT("PMS_PRT_314", "Missing sort field or sort type values"),
	INVALID_BETWEEN_VALUES("PMS_PRT_315", "Invalid fromValue or toValue"),
	INVALID_PAGINATION("PMS_PRT_356", "Pagination cannot be null"),
	INVALID_SORT_TYPE("PMS_PRT_358", "Sort type %s is not supported"),
	ERROR_OCCURED_WHILE_SORTING("PMS_PRT_359", "Error occured while sorting"),
	INVALID_SORT_FIELD("PMS_PRT_357", "Invalid sort field %s"),
	INVALID_PAGE_NO("PMS_PRT_360", "Invalid Page No"),
	INVALID_PAGE_SIZE("PMS_PRT_361", "Invalid page size"),
	INVALID_VALUE("KER_PRT_390", "Invalid filter value"),
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
	PARTNER_POLICY_TYPE_MISMATCH("PMS_PRT_057","Policy Type Mismatch. Only Auth policy can be used to create OIDC Client."),	
	EMAIL_EXISTS_IN_KEYCLOAK("PMS_PRT_074","User exists with same email(keycloak)"),
	PARTNER_DOES_NOT_EXIST_EXCEPTION("PMS_PRT_005","Partner does not exist"),
	PARTNER_API_KET_REQ_DOES_NOT_EXIST_EXCEPTION("PMS_PRT_006","No api key req exist"),
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
	POLICY_GROUP_POLICY_NOT_EXISTS("PMS_PRT_098","Given policy doesn't exists under partner's policy group."),
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
	POLICY_GROUP_NOT_MAPPED_PARTNER("PMS_PRT_054","Policy group not mapped for given partner"),
	LOGGEDIN_USER_NOT_AUTHORIZED("PMS_PRT_055","User not authorized."),
	PARTNER_POLICY_MAPPING_INPROGRESS("PMS_PRT_053","This policy is already pending for approval against your partner ID."),
	PARTNER_POLICY_MAPPING_APPROVED("PMS_PRT_060","This policy has already been approved against your partner ID."),
	PARTNER_LANG_CODE_NOT_SUPPORTED("PMS_PRT_056","Given langCode is not supported"),
	PARTNER_ID_CONTAINS_SPACES("PMS_PRT_066","PartnerId should not have any spaces"),
	JSON_NOT_VALID("PMS_PRT_096","Json is not valid"),
	INVALID_PARTNERID("PMS_PRT_058","AuthPartner doesn't exists with this Id - %s."),
	INVALID_PARTNER_TYPE("PMS_PRT_059","Only Auth_partner can Create OIDC Client. %s is not allowed to create OIDC Client."),
	DUPLICATE_CLIENT("PMS_ESI_001","Client public key already exists."),
	PARTNER_POLICY_NOT_APPROVED("PMS_ESI_002","Partner policy mapping is not approved. Please check mapping status."),
	POLICY_HAVING_MANDATORY_AUTHS("PMS_ESI_003","Policy having mandatory auths. So client can't be created for the same."),
	CLIENT_NOT_EXISTS("PMS_ESI_004","Client not exists with given id."),
	PARTNER_HAVING_NO_CLAIMS("PMS_ESI_005","Partner has no user claims"),
	PARTNER_HAVING_NO_ACRVALUES("PMS_ESI_006","Partner has no Authentication Context Refrences"),
	FAILED_TO_PROCESS_JWK("PMS_ESI_007","Failed to process the Public Key"),
	PARTNER_CERTIFICATES_FETCH_ERROR("PMS_CERTIFICATE_ERROR_001","Error while fetching partner certificates."),
	USER_ID_NOT_EXISTS("PMS_CERTIFICATE_ERROR_002","User Id does not exists."),
	PARTNER_ID_NOT_EXISTS("PMS_CERTIFICATE_ERROR_003","Partner Id is null or empty."),
	PARTNER_TYPE_NOT_EXISTS("PMS_CERTIFICATE_ERROR_004","Partner Type is null or empty"),
	APPROVAL_STATUS_NOT_EXISTS("PMS_CERTIFICATE_ERROR_005","Approval Status is null or empty"),
	UNABLE_TO_DECODE_CERTIFICATE("PMS_CERTIFICATE_ERROR_006", "Unable to decode the certificate data"),
	PARTNER_DOES_NOT_BELONG_TO_THE_USER("PMS_CERTIFICATE_ERROR_007", "The given partner ID does not belong to the user.So unable to get the original partner certificates"),
	CERTIFICATE_FETCH_ERROR("PMS_CERTIFICATE_ERROR_008","Error while fetching certificate."),
	DEACTIVATED_PARTNER_CERTIFICATE_DOWNLOAD_ERROR("PMS_CERTIFICATE_ERROR_009","Unable to download the certificate for a deactivated partner"),
	TRUST_CERTIFICATES_FETCH_ERROR("PMS_CERTIFICATE_ERROR_010", "Error while fetching trust certificates."),
	DOWNLOAD_TRUST_CERTIFICATE_ERROR("PMS_CERTIFICATE_ERROR_011", "Error while downloading trust certificates."),
	INVALID_CERTIFICATE_ID("PMS_CERTIFICATE_ERROR_012", "Certificate id is null or empty"),
	POLICY_GROUP_NOT_EXISTS("PMS_POLICY_ERROR_001","Policy Group does not exists."),
	PARTNER_POLICY_FETCH_ERROR("PMS_POLICY_ERROR_002","Error while fetching partner policies."),
	MATCHING_POLICY_NOT_FOUND("PMS_POLICY_ERROR_004", "Matching Policy not found."),
	POLICY_GROUP_ID_NOT_EXISTS("PMS_POLICY_ERROR_005","Policy Group Id is empty."),
	OIDC_CLIENTS_FETCH_ERROR("PMS_POLICY_ERROR_006","Error while fetching OIDC clients list."),
	API_KEY_REQUESTS_FETCH_ERROR("PMS_POLICY_ERROR_007", "Error while fetching API Key requests"),
	MATCHING_POLICY_GROUP_NOT_EXISTS("PMS_POLICY_ERROR_008", "No matching policy group was found for the provided partner"),
	PARTNER_NOT_BELONGS_TO_THE_USER_CREATE_OIDC("PMS_POLICY_ERROR_009", "The given partner ID does not belong to the user.So unable to create OIDC client"),
	PARTNER_NOT_BELONGS_TO_THE_USER_UPDATE_OIDC("PMS_POLICY_ERROR_010", "The given partner ID does not belong to the user.So unable to update OIDC client details"),
	PARTNER_NOT_BELONGS_TO_THE_USER_GET_OIDC("PMS_POLICY_ERROR_011", "The given partner ID does not belong to the user.So unable to get OIDC client details"),
	PMS_CONSENT_ERR("PMS_CONSENT_ERROR_001", "Error while fetching partner consent."),
	PMS_CONSENT_UNABLE_TO_ADD("PMS_CONSENT_ERROR_002", "Error while saving partner consent."),
	CLIENT_ALREADY_DEACTIVATED("PMS_ESI_008", "Client already deactivated."),
	SBI_DETAILS_LIST_FETCH_ERROR("PMS_SBI_ERROR_001", "Unable to fetch SBI details."),
	PARTNER_ID_NOT_ASSOCIATED_WITH_USER("PMS_DEVICE_ERROR_001", "Partner id is not associated with user."),
	SBI_NOT_ASSOCIATED_WITH_PARTNER_ID("PMS_DEVICE_ERROR_002", "SBI is not associated with partner Id."),
	INVALID_DEVICE_PARTNER_TYPE("PMS_DEVICE_ERROR_003", "Invalid partner type."),
	DEVICES_LIST_FOR_SBI_FETCH_ERROR("PMS_SF_ERROR_001", "Error while fetching devices list for SBI."),
	INVALID_REQUEST_PARAM("PMS_REQUEST_ERROR_001", "Invalid request."),
	SBI_DEVICE_MAPPING_ALREADY_EXIST("PMS_DEVICE_ERROR_004", "SBI and Device mapping already exists."),
	SBI_NOT_EXISTS("PMS_DEVICE_ERROR_005", "SBI do not exists."),
	DEVICE_NOT_EXISTS("PMS_DEVICE_ERROR_006", "Device do not exists."),
	DEVICE_NOT_ASSOCIATED_WITH_PARTNER_ID("PMS_DEVICE_ERROR_007", "Device is not associated with partner Id."),
	CREATE_DEVICE_ERROR("PMS_DEVICE_ERROR_008", "Error while adding device for sbi."),
	PENDING_APPROVAL_SBI("PMS_DEVICE_ERROR_009","Unable to add device because the associated SBI is not approved."),
	DEVICE_NOT_PENDING_FOR_APPROVAL("PMS_DEVICE_ERROR_010","Given device details are not in pending for approval status."),
	APPROVE_OR_REJECT_DEVICE_WITH_SBI_MAPPING_ERROR("PMS_DEVICE_ERROR_011", "Error while approving or rejecting device for Sbi Mapping."),
	SBI_DEVICE_MAPPING_NOT_EXISTS("PMS_DEVICE_ERROR_012","SBI and Device mapping does not exists."),
	INVALID_DEVICE_ID("PMS_DEVICE_ERROR_013", "Device Id is invalid"),
	DEVICE_ALREADY_DEACTIVATED("PMS_DEVICE_ERROR_014", "The selected device has been already deactivated."),
	DEACTIVATE_DEVICE_ERROR("PMS_DEVICE_ERROR_015", "Error while deactivating the device"),
	DEVICE_NOT_ASSOCIATED_WITH_USER("PMS_DEVICE_ERROR_016", "Device is not associated with user."),
	INVALID_SBI_ID("PMS_DEVICE_ERROR_017", "SBI Id is invalid"),
	DEACTIVATE_SBI_ERROR("PMS_DEVICE_ERROR_018", "Error while deactivating the SBI"),
	SBI_NOT_ASSOCIATED_WITH_USER("PMS_DEVICE_ERROR_019", "SBI is not associated with user."),
	SBI_ALREADY_DEACTIVATED("PMS_DEVICE_ERROR_020", "The selected SBI is already deactivated."),
	SBI_EXPIRED("PMS_DEVICE_ERROR_021", "SBI for which device is being added is expired"),
	SBI_NOT_APPROVED("PMS_DEVICE_ERROR_022", "The selected SBI is not in an approved status."),
	DEVICE_NOT_APPROVED("PMS_DEVICE_ERROR_023", "The selected Device is not in an approved status."),
	NO_SBI_FOUND_FOR_APPROVE("PMS_DEVICE_ERROR_024", "This device cannot be approved as it does not have any SBI associated to it."),
	NO_SBI_FOUND_FOR_REJECT("PMS_DEVICE_ERROR_025", "This device cannot be rejected as it does not have any SBI associated to it."),
	DEACTIVATE_STATUS_CODE("PMS_DEVICE_ERROR_026", "Request status should be De-Activate"),
	APPROVE_REJECT_STATUS_CODE("PMS_DEVICE_ERROR_027", "Request status should be either approved or rejected"),
	REJECTED_SBI("PMS_DEVICE_ERROR_028","Unable to add device because the associated SBI is already rejected"),
	DEACTIVATED_SBI("PMS_DEVICE_ERROR_029","Unable to add device because the associated SBI is already deactivated"),
	GET_ALL_DEVICE_DETAILS_FETCH_ERROR("PMS_FTM_ERROR_024", "Error occurred while retrieving all device details"),
	FTM_CHIP_DETAILS_LIST_FETCH_ERROR("PMS_FTM_ERROR_001", "Error while fetching the FTM chip details"),
	DEACTIVATE_FTM_ERROR("PMS_FTM_ERROR_003", "Error while deactivating the FTM"),
	INVALID_FTM_ID("PMS_FTM_ERROR_004", "FTM Id is invalid"),
	FTM_NOT_EXISTS("PMS_FTM_ERROR_005", "FTM Details do not exists."),
	FTM_NOT_ASSOCIATED_WITH_USER("PMS_FTM_ERROR_006", "FTM is not associated with user."),
	FTM_ALREADY_DEACTIVATED("PMS_FTM_ERROR_007", "The selected FTM is already deactivated."),
	DOWNLOAD_CERTIFICATE_FTM_INVALID_STATUS("PMS_FTM_ERROR_008", "The selected FTM must have a status of either pending_approval or approved."),
	DOWNLOAD_CERTIFICATE_FTM_DEACTIVATED_ERROR("PMS_FTM_ERROR_009", "Cannot download the certificate for a deactivated FTM"),
	UNABLE_TO_DOWNLOAD_ORIGINAL_FTM_CERTIFICATE("PMS_FTM_ERROR_009", "Unable to download original FTM certificate"),
	FTM_NOT_APPROVED("PMS_FTM_ERROR_010", "The selected FTM is not in an approved status."),
	PARTNERS_FETCH_ERROR("PMS_PS_ERROR_001", "Error while fetching partners list"),
	PARTNER_DOES_NOT_EXIST_ERROR("PMS_PS_ERROR_002", "Partner does not exists"),
	PARTNER_POLICIES_FETCH_ERROR("PMS_PS_ERROR_003", "Error while fetching partner policies"),
	PARTNER_NOT_APPROVED_ERROR("PMS_PS_ERROR_004", "The Given partner is not approved"),
	NOT_AUTH_PARTNER_TYPE_ERROR("PMS_PS_ERROR_005", "The specified partner is not of type Authentication Partner"),
	INVALID_PAGE_PARAMETERS("PMS_PS_ERROR_006", "Invalid pagination request: 'pageNo' and 'pageSize' must be specified together." ),
	INVALID_SORT_PARAMETERS("PMS_PS_ERROR_007", "Invalid Sorting request: 'sortType' and 'sortFieldName' must be specified together." ),
	MISSING_PAGINATION_FOR_SORT("PMS_PS_ERROR_008", "Please provide pagination parameters ('pageNo' and 'pageSize') when requesting sorted data."),
	ROOT_AND_INTERMEDIATE_CERTS_DISABLED("PMS_FEATURE_001", "Root and Intermediate Certificates list is not available in the current deployment."),
	OIDC_CLIENT_FEATURE_DISABLED("PMS_FEATURE_002", "OIDC client related features are not available in the current deployment."),
	CA_SIGNED_CERT_DISABLED("PMS_FEATURE_003", "Downloading CA signed certificate is not available in the current deployment.");
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
