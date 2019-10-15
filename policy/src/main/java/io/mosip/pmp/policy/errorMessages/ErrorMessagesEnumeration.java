package io.mosip.pmp.policy.errorMessages;

public enum ErrorMessagesEnumeration {
		
	POLICY_MANAGER_NOT_FOUND_EXCEPTION("PMS_POL_001","Policy Manager does not exist"),
	POLICY_MANAGER_WRONG_CREDENTIALS_EXCEPTION("PMS_POL_002","Mismatch of Policy Manager Credentials"),
	POLICY_NAME_DUPLICATE_EXCEPTION("PMS_POL_004","Policy Name already exists in the policy Group.Name : "),
	UNSUPPORTED_KYC_ATTRIBUTE("PMS_POL_005","Unsupported KYC attribute in the Policy File"),
	UNSUPPORTED_AUTH_TYPE("PMS_POL_006","Unsupported Authentication Type in the Policy File"),
	EKYC_ATTRIBUTE_MISSING("PMS_POL_007","eKYC attribute missing in the policy file"),
	POLICY_ID_NOT_EXISTS("PMS_POL_008","Policy ID does not exist"),
	AUTH_POLICY_NAME_DUPLICATE_EXCEPTION("PMS_POL_009","Auth policy Name already exists in the auth policy Group.Name : "),
	MISSING_INPUT_PARAMETER("PMS_COR_001","Missing Input Parameter - "),
	INVALID_INPUT_PARAMETER("PMS_COR_002","Invalid Input Parameter - "),
	INTERNAL_SERVER_ERROR("PMS_COR_003","Could not process the request");
	
	private final String errorCode;
	private final String errorMessage;

	private ErrorMessagesEnumeration(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

}
