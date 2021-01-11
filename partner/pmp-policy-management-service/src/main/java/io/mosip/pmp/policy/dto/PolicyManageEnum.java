package io.mosip.pmp.policy.dto;



public enum PolicyManageEnum {

	CREATE_POLICY_GROUP("PMS_PRT_115", AuditConstant.AUDIT_SYSTEM, "CREATE POLICY GROUP",
			"Creating policy group ", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	CREATE_POLICY_GROUP_SUCCESS("PMS_PRT_215", AuditConstant.AUDIT_SYSTEM, "CREATE POLICY GROUP",
			"Created policy group ", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	CREATE_POLICY_GROUP_FAILURE("PMS_PRT_475", AuditConstant.AUDIT_SYSTEM, "CREATE POLICY GROUP",
			"Created policy group ", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	GET_POLICY_GROUP("PMS_PRT_116", AuditConstant.AUDIT_SYSTEM, "CREATE POLICY GROUP",
			"Getting policy group ", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	GET_POLICY_GROUP_SUCCESS("PMS_PRT_216", AuditConstant.AUDIT_SYSTEM, "CREATE POLICY GROUP",
			"GET policy group success ", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	GET_POLICY_GROUP_FAILURE("PMS_PRT_416", AuditConstant.AUDIT_SYSTEM, "CREATE POLICY GROUP",
			"getting policy group failure", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	UPDATE_POLICY_GROUP("PMS_PRT_156", AuditConstant.AUDIT_SYSTEM, "UPDATE POLICY GROUP",
			"Updating policy group ", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	UPDATE_POLICY_GROUP_SUCCESS("PMS_PRT_256", AuditConstant.AUDIT_SYSTEM, "CREATE POLICY GROUP",
			"UPDATE policy group success ", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	UPDATE_POLICY_GROUP_FAILURE("PMS_PRT_456", AuditConstant.AUDIT_SYSTEM, "CREATE POLICY GROUP",
			"UPDATE policy group failure", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	CREATE_POLICY("PMS_PRT_137", AuditConstant.AUDIT_SYSTEM, "CREATE POLICY",
			"Creating policy", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	CREATE_POLICY_SUCCESS("PMS_PRT_237", AuditConstant.AUDIT_SYSTEM, "CREATE POLICY",
			"Created policy success", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	CREATE_POLICY_FAILURE("PMS_PRT_437", AuditConstant.AUDIT_SYSTEM, "CREATE POLICY ",
			"Created policy failed", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	GET_POLICY("PMS_PRT_187", AuditConstant.AUDIT_SYSTEM, "CREATE POLICY ",
			"Getting policy", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	GET_POLICY_SUCCESS("PMS_PRT_287", AuditConstant.AUDIT_SYSTEM, "CREATE POLICY ",
			"GET policysuccess ", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	GET_POLICY_FAILURE("PMS_PRT_487", AuditConstant.AUDIT_SYSTEM, "CREATE POLICY ",
			"getting policy failure", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	UPDATE_POLICY("PMS_PRT_183", AuditConstant.AUDIT_SYSTEM, "UPDATE POLICY ",
			"Updating policy", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	UPDATE_POLICY_SUCCESS("PMS_PRT_283", AuditConstant.AUDIT_SYSTEM, "UPDATE POLICY ",
			"UPDATE policy success ", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
			"NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
	UPDATE_POLICY_FAILURE("PMS_PRT_483", AuditConstant.AUDIT_SYSTEM, "UPDATE POLICY ",
			"UPDATE policy failure", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
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
