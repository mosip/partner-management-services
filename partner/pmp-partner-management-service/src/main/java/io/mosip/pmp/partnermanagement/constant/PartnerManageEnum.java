package io.mosip.pmp.partnermanagement.constant;



public enum PartnerManageEnum {


	API_KEY_MAPPING("PMS_PRT_179", AuditConstant.AUDIT_SYSTEM, "GET API KEY MAPPING",
			"Getting api and key mappings", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	API_KEY_MAPPING_SUCCESS("PMS_PRT_279", AuditConstant.AUDIT_SYSTEM, "GET API KEY MAPPING",
			"Getting api and key mappings", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	ACTIVATE_DEACTIVATE_KYC_PARTNERS("PMS_PRT_189", AuditConstant.AUDIT_SYSTEM, " AUTH KYC Partners",
			"Getting kyc partners", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	ACTIVATE_DEACTIVATE_KYC_PARTNERS_SUCCESS("PMS_PRT_289", AuditConstant.AUDIT_SYSTEM, " AUTH KYC Partners",
			"Getting kyc partners", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	ACTIVATE_DEACTIVATE_API_PARTNERS("PMS_PRT_199", AuditConstant.AUDIT_SYSTEM, "API KYC Partners",
			"Getting api partners", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	ACTIVATE_DEACTIVATE_API_PARTNERS_SUCCESS("PMS_PRT_299", AuditConstant.AUDIT_SYSTEM, "API KYC Partners",
			"Getting api partners", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	GET_PARTNER_POLICY_MAPPING("PMS_PRT_191", AuditConstant.AUDIT_SYSTEM, "GET Partners Policy Mapping",
			"Getting partners policy mapping", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	GET_PARTNER_POLICY_MAPPING_SUCCESS("PMS_PRT_291", AuditConstant.AUDIT_SYSTEM, "GET Partners Policy Mapping",
			"Getting partners policy mapping", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	APPROVE_REJECT_PARTNER_API("PMS_PRT_181", AuditConstant.AUDIT_SYSTEM, "Approve Reject Partner",
			"Approve Reject Partner", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	APPROVE_REJECT_PARTNER_API_SUCCESS("PMS_PRT_281", AuditConstant.AUDIT_SYSTEM, "Approve Reject Partner",
			"Approve Reject Partner", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	GET_PARTNER_POLICY_GROUP("PMS_PRT_171", AuditConstant.AUDIT_SYSTEM, "Get Partner",
			"Get partner for policy group", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	GET_PARTNER_POLICY_GROUP_SUCCESS("PMS_PRT_271", AuditConstant.AUDIT_SYSTEM, "Get Partner",
			"Get partner for policy group", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	GET_PARTNER_FOR_ID("PMS_PRT_161", AuditConstant.AUDIT_SYSTEM, "Get Partner",
			"Get partner for partner id", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	GET_PARTNER_FOR_ID_SUCCESS("PMS_PRT_261", AuditConstant.AUDIT_SYSTEM, "Get Partner",
			"Get partner for partner id", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	GET_PARTNER("PMS_PRT_151", AuditConstant.AUDIT_SYSTEM, "Get Partner",
			"Get partner ", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	GET_PARTNER_SUCCESS("PMS_PRT_251", AuditConstant.AUDIT_SYSTEM, "Get Partner",
			"Get partner ", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID);

	private final String eventId;

	private final String type;

	private String name;

	private String description;

	private String moduleId;

	private String moduleName;

	private String id;

	private String idType;

	private String applicationId;

	private String applicationName;

	private PartnerManageEnum(String eventId, String type, String name, String description, String moduleId,
			String moduleName, String id, String idType, String applicationId, String applicationName) {
		this.eventId = eventId;
		this.type = type;
		this.name = name;
		this.description = description;
		this.moduleId = moduleId;
		this.moduleName = moduleName;
		this.id = id;
		this.idType = idType;
		this.applicationId = applicationId;
		this.applicationName = applicationName;

	}

	public String getEventId() {
		return eventId;
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getModuleId() {
		return moduleId;
	}

	public String getModuleName() {
		return moduleName;
	}

	public String getId() {
		return id;
	}

	public String getIdType() {
		return idType;
	}

	public void setDescription(String des) {
		this.description = des;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getApplicationName() {
		return applicationName;
	}

	/*
	 * Replace %s value in description and id with second parameter passed
	 */

	public static PartnerManageEnum getPartnerManageEnumWithValue(PartnerManageEnum e, String s) {
		e.setDescription(String.format(e.getDescription(), s));
		if (e.getId().equalsIgnoreCase("%s"))
			e.setId(s);
		return e;
	}

}
