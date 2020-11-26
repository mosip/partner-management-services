package io.mosip.pmp.misp.utils;

import io.mosip.pmp.common.constant.AuditConstant;

public enum PartnerManageEnum {
	
	
	
	CREATE_MISP("PMS_MSP_101",AuditConstant.AUDIT_USER,"Register MISP","Calling API to create MISP",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"NO_ID","NO_ID_TYPE",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	CREATE_MISP_SUCCESS("PMS_MSP_200",AuditConstant.AUDIT_USER,"Register MISP","Calling API to create MISP is success",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"NO_ID","NO_ID_TYPE",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	CREATED_MISP("PMS_MSP_212",AuditConstant.AUDIT_USER,"Register MISP","Successfully created MISP id- %s",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"%s","MISP ID",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	MISP_INVALID_EMAIL_CREATE("PMS_MSP_401",AuditConstant.AUDIT_SYSTEM,"Create MISP request","Invalid email id",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"NO_ID","NO_ID_TYPE",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	MISP_INVALID_EMAIL_UPDATE("PMS_MSP_416",AuditConstant.AUDIT_SYSTEM,"Update MISP request","Invalid email id for MISP id - %s",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"%s","MISP ID",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	ORGANIZATION_NAME_EXISTS_UPDATE("PMS_MSP_402",AuditConstant.AUDIT_SYSTEM,"Update MISP request","MISP is already registered with organization name for id - %s",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"%s","MISP ID",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	ORGANIZATION_NAME_EXISTS_CREATE("PMS_MSP_417",AuditConstant.AUDIT_SYSTEM,"Create MISP request","MISP is already registered with organization name",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"NO_ID","NO_ID_TYPE",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	GENERATING_MISP_ID("PMS_MSP_102",AuditConstant.AUDIT_SYSTEM,"Register MISP","Generating MISP id by using kernel MISP id generator",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"NO_ID","NO_ID_TYPE",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	GENERATED_MISP_DETAILS("PMS_MSP_103",AuditConstant.AUDIT_SYSTEM,"Register MISP","MISP details created",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"NO_ID","NO_ID_TYPE",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	PROCESS_MISP("PMS_MSP_104",AuditConstant.AUDIT_SYSTEM,"Processing MISP status request","Calling API to update MISP status with id - %s",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"%s","MISP ID",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	PROCESS_MISP_SUCCESS("PMS_MSP_201",AuditConstant.AUDIT_SYSTEM,"Processing MISP status request","Calling API to update MISP status with id - %s is success",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"%s","MISP ID",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
    STATUS_EXCEPTION("PMS_MSP_403",AuditConstant.AUDIT_SYSTEM,"Processing MISP status request","MISP status must either be approved or rejected for id -%s",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"%s","MISP ID",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
    VALIDATE_MISPID("PMS_MSP_104",AuditConstant.AUDIT_SYSTEM,"Processing MISP status request","Validating MISP id -%s",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"%s","MISP ID",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
    MISP_EXCEPTION("PMS_MSP_404",AuditConstant.AUDIT_SYSTEM,"Processing MISP status request","MISP exception",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"NO_ID","NO_ID_TYPE",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	MISP_DEACTIVATED("PMS_MSP_405",AuditConstant.AUDIT_SYSTEM,"Processing MISP status request","MISP is de-activated. Cannot approve the de-activated MISP with id-%s",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"%s","MISP ID",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	UPDATED_MISP_STATUS_PROCESS("PMS_MSP_202",AuditConstant.AUDIT_SYSTEM,"Process MISP status request","MISP status is updated for id - %s",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"%s","MISP",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	UPDATED_MISP_STATUS("PMS_MSP_211",AuditConstant.AUDIT_SYSTEM,"Update MISP status","MISP status is updated for id - %s",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"%s","MISP",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	REJECTED_MISP_LICENSE("PMS_MSP_105",AuditConstant.AUDIT_SYSTEM,"Processing MISP status request","MISP license key is not generated for rejected MISP with id-%s",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"%s","MISP ID",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	UPDATE_MISP_LICENSE("PMS_MSP_106",AuditConstant.AUDIT_SYSTEM,"Processing MISP status request","Updating MISP license status for id- %s",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"%s","MISP ID",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	UPDATE_MISP("PMS_MSP_107",AuditConstant.AUDIT_SYSTEM,"Update MISP request","Calling API to update MISP request",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"NO_ID","NO_ID_TYPE",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	UPDATE_MISP_SUCCESS("PMS_MSP_203",AuditConstant.AUDIT_SYSTEM,"Update MISP request","Calling API to update MISP request is success",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"NO_ID","NO_ID_TYPE",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	UPDATING_MISP_STATUS("PMS_MSP_108",AuditConstant.AUDIT_SYSTEM,"Update MISP request","Updated MISP status",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"NO_ID","NO_ID_TYPE",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	VALIDATE_LICENSE_KEY("PMS_MSP_109",AuditConstant.AUDIT_SYSTEM,"Validate license key","Calling API to validate MISP license key",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"NO_ID","NO_ID_TYPE",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	VALIDATE_LICENSE_KEY_SUCCESS("PMS_MSP_204",AuditConstant.AUDIT_SYSTEM,"Update MISP request","Calling API to validate MISP license key is success",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"NO_ID","NO_ID_TYPE",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	LICENSE_KEY_NOT_FOUND("PMS_MSP_406",AuditConstant.AUDIT_SYSTEM,"Validate license key","No details found for license key",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"NO_ID","NO_ID_TYPE",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	FETCH_LICENSE_KEY_DETAILS("PMS_MSP_110",AuditConstant.AUDIT_SYSTEM,"Validate license key","Fetching license key details for MISP id-%s",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"%s","MISP ID",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	LICENSE_KEY_STATUS("PMS_MSP_111",AuditConstant.AUDIT_SYSTEM,"Validate license key","License key for MISP is %s",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"NO_ID","NO_ID_TYPE",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	ACTIVE_STATUS_EXCEPTION_UPDATE("PMS_MSP_418",AuditConstant.AUDIT_SYSTEM,"Update MISP license key request","MISP status must either be Active or De-active for id - %s",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"%s","MISP ID",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	ACTIVE_STATUS_EXCEPTION_STATUS("PMS_MSP_407",AuditConstant.AUDIT_SYSTEM,"Update MISP status request","MISP status must either be Active or De-active for id - %s",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"%s","MISP ID",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	UPDATE_MISP_STATUS("PMS_MSP_112",AuditConstant.AUDIT_SYSTEM,"Update MISP status request","Calling API to update MISP status ",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"NO_ID","NO_ID_TYPE",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	UPDATE_MISP_STATUS_SUCCESS("PMS_MSP_205",AuditConstant.AUDIT_SYSTEM,"Update MISP status request","Calling API to update MISP status is success",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"NO_ID","NO_ID_TYPE",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	MISP_ACTIVE_STATUS("PMS_MSP_113",AuditConstant.AUDIT_SYSTEM,"Update MISP status request","MISP status is %s",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"NO_ID","NO_ID_TYPE",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	UPDATE_MISP_LICENSE_KEY_STATUS("PMS_MSP_114",AuditConstant.AUDIT_SYSTEM,"Update MISP license key request","Calling API to update MISP license key",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"NO_ID","NO_ID_TYPE",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	UPDATE_MISP_LICENSE_KEY_STATUS_SUCCESS("PMS_MSP_206",AuditConstant.AUDIT_SYSTEM,"Update MISP license key request","Calling API to update MISP license key is success",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"NO_ID","NO_ID_TYPE",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	MISP_ID_NO_LICENSE_KEY("PMS_MSP_408",AuditConstant.AUDIT_SYSTEM,"Update MISP license key request","MISP license key not associated to id - %s",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"%s","MISP ID",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	CANNOT_ACTIVATE_LICENSE_KEY("PMS_MSP_419",AuditConstant.AUDIT_SYSTEM,"Update MISP license key request","MISP license is expired.Cannot activate the same for MISP id-%s",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"%s","MISP ID",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	UPDATED_LICENSE_KEY("PMS_MSP_115",AuditConstant.AUDIT_SYSTEM,"Update MISP license key request","Updating the MISP license status for id - %s",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"%s","MISP ID",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	GET_ALL_MISPS("PMS_MSP_116",AuditConstant.AUDIT_SYSTEM,"Get all MISP reqest","Calling API to get all MISPs",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"NO_ID","NO_ID_TYPE",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	GET_ALL_MISPS_SUCCESS("PMS_MSP_207",AuditConstant.AUDIT_SYSTEM,"Get all MISP request","Calling API to get all MISPs is success",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"NO_ID","NO_ID_TYPE",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	GET_MISP_BY_ID("PMS_MSP_117",AuditConstant.AUDIT_SYSTEM,"Get MISP by id request","Calling API to get MISP based on id",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"NO_ID","NO_ID_TYPE",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	GET_MISP_BY_ID_SUCCESS("PMS_MSP_208",AuditConstant.AUDIT_SYSTEM,"Get MISP by id request","Calling API to get MISP based on id is success",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"NO_ID","NO_ID_TYPE",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	NO_MISP_DETAILS("PMS_MSP_409",AuditConstant.AUDIT_SYSTEM,"Get all MISP request","No MISP details found",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"NO_ID","NO_ID_TYPE",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	FETCH_MISP("PMS_MSP_118",AuditConstant.AUDIT_SYSTEM,"Get MISP by id request","Fetched MISP for id -%s",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"%s","MISP ID",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	GET_MISP_BY_ORGANIZATION_NAME("PMS_MSP_119",AuditConstant.AUDIT_SYSTEM,"Get MISP by organization name","Calling API to get MISP based on organization name",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"NO_ID","NO_ID_TYPE",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	GET_MISP_BY_ORGANIZATION_NAME_SUCCESS("PMS_MSP_209",AuditConstant.AUDIT_SYSTEM,"Get MISP by organization name","Calling API to get MISP based on organization name is success",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"NO_ID","NO_ID_TYPE",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	NO_MISP_BY_ORGANIZATION_NAME("PMS_MSP_410",AuditConstant.AUDIT_SYSTEM,"Get MISP by organization name","No MISP found while fetching MISP details by organization name",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"NO_ID","NO_ID_TYPE",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	DOWNLOAD_LICENSE_KEY("PMS_MSP_120",AuditConstant.AUDIT_SYSTEM,"Request for downloading license key","Calling API to download license key",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"NO_ID","NO_ID_TYPE",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	DOWNLOAD_LICENSE_KEY_SUCCESS("PMS_MSP_210",AuditConstant.AUDIT_SYSTEM,"Request for downloading license key","Calling API to download license key is success",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"NO_ID","NO_ID_TYPE",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	MISP_NOT_APPROVED("PMS_MSP_410",AuditConstant.AUDIT_SYSTEM,"Request for downloading license key","MISP is not yet approved for id - %s",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"%s","MISP ID",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	MISP_INACTIVE("PMS_MSP_411",AuditConstant.AUDIT_SYSTEM,"Request for downloading license key","MISP is not active for id - %s",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"%s","MISP ID",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	MISP_LICENSE_NOT_EXISTS("PMS_MSP_412",AuditConstant.AUDIT_SYSTEM,"Request for downloading license key","MISP license Key does not exists for id - %s",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"%s","MISP ID",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	MISP_ALL_LICENSE_INACTIVE("PMS_MSP_413",AuditConstant.AUDIT_SYSTEM,"Request for downloading license key","MISP license all are inactive for id - %s",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"%s","MISP ID",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	GENERATE_LICENSE("PMS_MSP_121",AuditConstant.AUDIT_SYSTEM,"Request for downloading license key","Started generating license for MISP id - %s",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"%s","MISP ID",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	NO_MISP_ID("PMS_MSP_414",AuditConstant.AUDIT_SYSTEM,"Finding MISP by id","No details found for MISP id-%s",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"%s","MISP ID",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID),
	VALIDATING_LICENSE_KEY("PMS_MSP_415",AuditConstant.AUDIT_SYSTEM,"Validating license key","No details found for combination of MISP license key %s",AuditConstant.MISP_MODULE_ID,AuditConstant.MISP_MODULE_NAME,"%s","MISP ID",AuditConstant.APPLICATION_NAME,AuditConstant.APPLICATION_ID);
	
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

	private PartnerManageEnum(String eventId, String type, String name, String description,String moduleId,String moduleName,String id,String idType,String applicationId,String applicationName) {
		this.eventId = eventId;
		this.type = type;
		this.name = name;
		this.description = description;
		this.moduleId=moduleId;
		this.moduleName=moduleName;
		this.id=id;
		this.idType=idType;
		this.applicationId=applicationId;
		this.applicationName=applicationName;
		
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
	
	public void setDescription(String des)
	{
		this.description=des;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setId(String id) {
		this.id=id;
	}
	
	public void setName(String name) {
		this.name=name;
	}

	public String getApplicationName() {
		return applicationName;
	}

	/*
	 * Replace %s value in description and id with second parameter passed
	 */

	public static PartnerManageEnum getPartnerManageEnumWithValue(PartnerManageEnum e,String s)
	{
		e.setDescription(String.format(e.getDescription(),s));
		if(e.getId().equalsIgnoreCase("%s"))
			e.setId(s);
		return e;
	}
	
	
}
