package io.mosip.pms.policy.dto;



public enum PolicyManageEnum {

	CREATE_POLICY_GROUP("PMS_PRT_115", AuditConstant.AUDIT_SYSTEM, "CREATE",
			"Creating policy group ", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	CREATE_POLICY_GROUP_SUCCESS("PMS_PRT_215", AuditConstant.AUDIT_SYSTEM, "CREATE",
			"Creating policy group success ", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	CREATE_POLICY_GROUP_FAILURE("PMS_PRT_415", AuditConstant.AUDIT_SYSTEM, "CREATE",
			"Creating policy group failed", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	GET_POLICY_GROUP("PMS_PRT_116", AuditConstant.AUDIT_SYSTEM, "GET",
			"Retrieving policy group(s) ", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	GET_POLICY_GROUP_SUCCESS("PMS_PRT_216", AuditConstant.AUDIT_SYSTEM, "GET",
			"Retrieving policy group(s) success ", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	GET_POLICY_GROUP_FAILURE("PMS_PRT_416", AuditConstant.AUDIT_SYSTEM, "GET",
			"Retrieving policy group(s) failure", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	UPDATE_POLICY_GROUP("PMS_PRT_156", AuditConstant.AUDIT_SYSTEM, "UPDATE",
			"Updating policy group ", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	UPDATE_POLICY_GROUP_SUCCESS("PMS_PRT_256", AuditConstant.AUDIT_SYSTEM, "UPDATE",
			"Updating policy group success ", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	UPDATE_POLICY_GROUP_FAILURE("PMS_PRT_456", AuditConstant.AUDIT_SYSTEM, "UPDATE",
			"Updating policy group failed", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	CREATE_POLICY("PMS_PRT_137", AuditConstant.AUDIT_SYSTEM, "CREATE",
			"Creating policy", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	CREATE_POLICY_SUCCESS("PMS_PRT_237", AuditConstant.AUDIT_SYSTEM, "CREATE",
			"Creating policy success", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	CREATE_POLICY_FAILURE("PMS_PRT_437", AuditConstant.AUDIT_SYSTEM, "CREATE",
			"Creating policy failed", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	GET_POLICY_BY_APIKEY("PMS_PRT_188", AuditConstant.AUDIT_SYSTEM, "GET",
			"Retrieving policy for apikey", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	GET_POLICY_BY_APIKEY_FAILURE("PMS_PRT_488", AuditConstant.AUDIT_SYSTEM, "GET",
			"Retrieving policy for apikey failed", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	GET_POLICY_BY_APIKEY_SUCCESS("PMS_PRT_288", AuditConstant.AUDIT_SYSTEM, "GET",
			"Retrieving policy for apikey success", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	GET_PARTNER_POLICY("PMS_PRT_189", AuditConstant.AUDIT_SYSTEM, "GET",
			"Retrieving partner mapped policy", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	GET_PARTNER_POLICY_SUCCESS("PMS_PRT_289", AuditConstant.AUDIT_SYSTEM, "GET",
			"Retrieving partner mapped policy sucess", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	GET_PARTNER_POLICY_FAILURE("PMS_PRT_489", AuditConstant.AUDIT_SYSTEM, "GET",
			"Retrieving partner mapped policy failure", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	SEARCH_POLICY("PMS_PRT_190", AuditConstant.AUDIT_SYSTEM, "SEARCH",
			"Searching policy", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	SEARCH_POLICY_SUCCESS("PMS_PRT_290", AuditConstant.AUDIT_SYSTEM, "SEARCH",
			"Searching policy success", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	SEARCH_POLICY_FAILURE("PMS_PRT_190", AuditConstant.AUDIT_SYSTEM, "SEARCH",
			"Searching policy failed", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	GET_CONFIG_VALUE("PMS_PRT_191", AuditConstant.AUDIT_SYSTEM, "GET",
			"Retrieving config value", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	GET_CONFIG_VALUE_SUCCESS("PMS_PRT_291", AuditConstant.AUDIT_SYSTEM, "GET",
			"Retrieving config value success", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	GET_CONFIG_VALUE_FAILURE("PMS_PRT_491", AuditConstant.AUDIT_SYSTEM, "GET",
			"Retrieving config value failure", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	FILTERVALUES_POLICY_GROUP("PMS_PRT_192", AuditConstant.AUDIT_SYSTEM, "FILTERVALUES",
			"Filtering policy group", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	FILTERVALUES_POLICY_GROUP_SUCCESS("PMS_PRT_292", AuditConstant.AUDIT_SYSTEM, "FILTERVALUES",
			"Filtering policy group success", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	FILTERVALUES_POLICY_GROUP_FAILURE("PMS_PRT_492", AuditConstant.AUDIT_SYSTEM, "FILTERVALUES",
			"Filtering policy group failed", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	FILTERVALUES_POLICY("PMS_PRT_193", AuditConstant.AUDIT_SYSTEM, "FILTERVALUES",
			"Filtering policy", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	FILTERVALUES_POLICY_SUCCESS("PMS_PRT_293", AuditConstant.AUDIT_SYSTEM, "FILTERVALUES",
			"Filtering policy success", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	FILTERVALUES_POLICY_FAILURE("PMS_PRT_493", AuditConstant.AUDIT_SYSTEM, "FILTERVALUES",
			"Filtering policy failed", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	SEARCH_POLICY_GROUP("PMS_PRT_194", AuditConstant.AUDIT_SYSTEM, "SEARCH",
			"Searching policy group(s)", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	SEARCH_POLICY_GROUP_SUCCESS("PMS_PRT_294", AuditConstant.AUDIT_SYSTEM, "SEARCH",
			"Searching policy group(s) sucess", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	SEARCH_POLICY_GROUP_FAILURE("PMS_PRT_494", AuditConstant.AUDIT_SYSTEM, "SEARCH",
			"Searching policy group(s) failed", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	GET_ALL_POLICIES("PMS_PRT_187", AuditConstant.AUDIT_SYSTEM, "GET",
			"Retrieving policy/policies", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	GET_POLICY_SUCCESS("PMS_PRT_287", AuditConstant.AUDIT_SYSTEM, "GET",
			"Retrieving policy/policies success ", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	GET_POLICY_FAILURE("PMS_PRT_487", AuditConstant.AUDIT_SYSTEM, "GET",
			"Retrieving policy/policies failure", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	UPDATE_POLICY("PMS_PRT_183", AuditConstant.AUDIT_SYSTEM, "UPDATE",
			"Updating policy", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	UPDATE_POLICY_STATUS("PMS_PRT_184", AuditConstant.AUDIT_SYSTEM, "UPDATE",
			"Updating policy status", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	UPDATE_POLICY_STATUS_SUCCESS("PMS_PRT_284", AuditConstant.AUDIT_SYSTEM, "UPDATE",
			"Update policy status success", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	UPDATE_POLICY_STATUS_FAILURE("PMS_PRT_484", AuditConstant.AUDIT_SYSTEM, "UPDATE",
			"Update policy status failed", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	UPDATE_POLICY_SUCCESS("PMS_PRT_283", AuditConstant.AUDIT_SYSTEM, "UPDATE",
			"Updating policy success ", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	UPDATE_POLICY_FAILURE("PMS_PRT_483", AuditConstant.AUDIT_SYSTEM, "UPDATE",
			"Updating policy failed", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	PUBLISH_POLICY("PMS_PRT_157", AuditConstant.AUDIT_SYSTEM, "PUBLISH",
			"Publishing policy", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	PUBLISH_POLICY_SUCCESS("PMS_PRT_257", AuditConstant.AUDIT_SYSTEM, "PUBLISH",
			"Publishing policy success", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	PUBLISH_POLICY_FAILURE("PMS_PRT_457", AuditConstant.AUDIT_SYSTEM, "PUBLISH",
			"Publishing policy failed", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),;
	

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

	private PolicyManageEnum(String eventId, String type, String name, String description, String moduleId,
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

	public static PolicyManageEnum getPartnerManageEnumWithValue(PolicyManageEnum e, String s) {
		e.setDescription(String.format(e.getDescription(), s));
		if (e.getId().equalsIgnoreCase("%s"))
			e.setId(s);
		return e;
	}

}