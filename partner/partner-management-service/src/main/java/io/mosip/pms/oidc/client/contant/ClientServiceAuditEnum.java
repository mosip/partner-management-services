package io.mosip.pms.oidc.client.contant;

import io.mosip.pms.partner.manager.constant.AuditConstant;

public enum ClientServiceAuditEnum {

    CREATE_CLIENT("PMS_PRT_300", AuditConstant.AUDIT_SYSTEM, "POST CREATE OIDC CLIENT",
            "Creating the OIDC Client", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
            "NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
    CREATE_CLIENT_SUCCESS("PMS_PRT_301", AuditConstant.AUDIT_SYSTEM, "POST CREATE OIDC CLIENT",
            "Creating the OIDC Client Success", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
            "NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
    CREATE_CLIENT_FAILURE("PMS_PRT_302", AuditConstant.AUDIT_SYSTEM, "POST CREATE OIDC CLIENT",
            "Creating the OIDC Client Failed", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
            "NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
    UPDATE_CLIENT("PMS_PRT_303", AuditConstant.AUDIT_SYSTEM, "PUT UPDATE OIDC CLIENT",
            "Updating OIDC Client", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
            "NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
    UPDATE_CLIENT_SUCCESS("PMS_PRT_304", AuditConstant.AUDIT_SYSTEM, "PUT UPDATE OIDC CLIENT",
            "Updating OIDC Client Success", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
            "NO_ID_TYPE", AuditConstant.APPLICATION_NAME, AuditConstant.APPLICATION_ID),
    UPDATE_CLIENT_FAILURE("PMS_PRT_305", AuditConstant.AUDIT_SYSTEM, "PUT UPDATE OIDC CLIENT",
            "Updating OIDC Client Failed", AuditConstant.PARTNER_MODULE_ID, AuditConstant.PARTNER_MODULE_NAME, "NO_ID",
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

    public String getApplicationId() {
        return applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    ClientServiceAuditEnum(String eventId, String type, String name, String description, String moduleId,
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

}