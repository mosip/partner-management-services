package io.mosip.pms.policy.errorMessages;

/**
 * <p> This enum contains all the error messages with codes.</p>
 * 
 * @author Nagarjuna Kuchi
 * @version 1.0
 */

public enum ErrorMessages {
		
	POLICY_MANAGER_NOT_FOUND_EXCEPTION("PMS_POL_001","Policy Manager does not exist"),
	POLICY_MANAGER_WRONG_CREDENTIALS_EXCEPTION("PMS_POL_002","Mismatch of Policy Manager Credentials"),
	POLICY_NAME_DUPLICATE_EXCEPTION("PMS_POL_004","Policy Name already exists in the policy Group.Name : "),
	UNSUPPORTED_KYC_ATTRIBUTE("PMS_POL_005","Unsupported KYC attribute in the Policy File"),
	UNSUPPORTED_AUTH_TYPE("PMS_POL_006","Unsupported Authentication Type in the Policy File"),
	EKYC_ATTRIBUTE_MISSING("PMS_POL_007","eKYC attribute missing in the policy file"),
	POLICY_ID_NOT_EXISTS("PMS_POL_008","Policy ID does not exist"),
	AUTH_POLICY_NAME_DUPLICATE_EXCEPTION("PMS_POL_009","Auth policy exists with name : "),
	MISSING_INPUT_PARAMETER("PMS_COR_001","Missing Input Parameter - "),
	INVALID_INPUT_PARAMETER("PMS_COR_002","Invalid Input Parameter - "),
	INTERNAL_SERVER_ERROR("PMS_COR_003","Could not process the request"),
	AUTH_POLICIES_NOT_DEFINED("PMS_POL_010", "Auth Policies are not defined against to policy"),
	NO_POLICY_AGAINST_APIKEY("PMS_POL_013","No policy available for given PartnerAPIKey"),
	POLICY_GROUP_NAME_DUPLICATE("PMS_POL_014","Policy group exists with name : "),
	POLICY_GROUP_POLICY_NOT_MAPPED("PMS_POL_015","Policy group and policy not mapped."),
	POLICY_GROUP_ID_NOT_EXISTS("PMS_POL_016","Policy Group ID does not exist"),
	NO_POLICY_AGAINST_PARTNER("PMS_POL_017","No policy available for given partner"),
	PARTNER_POLICY_NOT_MAPPED("PMS_POL_018","Given partner and policy are not mapped."),
	POLICY_GROUP_NAME_NOT_EXISTS("PMS_POL_019","Policy group name does not exist"),
	POLICY_PUBLISHED("PMS_POL_020","Can not publish the published policy"),
	AUTH_TYPES_NOT_REQUIRED("PMS_POL_021","allowed auth types are not required for policyType "),
	AUTH_TOKEN_TYPE_NOT_ALLOWED("PMS_POL_022","Given auth token type is not allowed"),
	POLICY_TYPE_NOT_ALLOWED("PMS_POL_023","Given policy type is not allowed "), 
	POLICY_STATUS_CODE_EXCEPTION("PMS_POL_045","Policy Status either Active or De-active."),
	DATASHARE_ATTRIBUTES_NOT_REQUIRED("PMS_POL_046","Datashare attributes are not required for given policyType "),
	SHAREABLE_ATTRIBUTES_NOT_REQUIRED("PMS_POL_047","Shareable attributes are not required for given policyType "),
	ALLOWED_KYC_ATTRIBUTES_NOT_REQUIRED("PMS_POL_048","AllowedKYC attributes are not required for given policyType "),
	VERSION_NOT_ALLOWED("PMS_POL_050","The given version is not available.Allowed versions are : "),
	SCHEMA_POLICY_NOT_MATCHING("PMS_POL_051","Policy Schema and policy are not matching"),
	POLICY_PARSING_ERROR("PMS_POL_052","Error occured while parsing policy string to json object"),
	POLICY_GROUP_NOT_ACTIVE("PMS_POL_053","Policy group is not active."),
	DRAFTED_POLICY_NOT_ACTIVE("PMS_POL_054","Cannot activate unpublished policy."),
	PUBLISHED_POLICY_NOT_UPDATED("PMS_POL_055","Published policy cannot be updated."),
	ACTIVE_POLICY_EXISTS_UNDER_POLICY_GROUP("PMS_POL_056","Active policies exists under the policy group."),
	ACTIVE_APIKEY_EXISTS_UNDER_POLICY("PMS_POL_057","Active apiKey exists under the policy."),
	PUBLISHED_POLICY_STATUS_UPDATE("PMS_POL_058","Status cannot be changed for published policy."),
	POLICY_GROUPS_NOT_AVAILABLE("PMS_POL_059", "There are no active policy groups.");
	
	
	private final String errorCode;
	private final String errorMessage;

	/**
	 * Constructs a new errorMessages enum with the specified detail message and
	 * error code and error message.
	 *
	 * 
	 * @param errorCode    the error code
	 * @param errorMessage the detail message.
	 * @param rootCause    the specified cause
	 */
	
	private ErrorMessages(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 * This method bring the error code.
	 * @return string 
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * This method brings the error message.
	 * @return string 
	 */
	public String getErrorMessage() {
		return errorMessage;
	}


}
