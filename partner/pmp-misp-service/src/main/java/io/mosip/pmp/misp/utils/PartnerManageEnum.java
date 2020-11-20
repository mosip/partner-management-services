package io.mosip.pmp.misp.utils;

import io.mosip.pmp.misp.constant.MispConstant;;

public enum PartnerManageEnum {
	
	
	
	CREATE_MISP("PMS_MSP_101",MispConstant.AUDIT_USER,"Register MISP","Calling API to create MISP",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"NO_ID","NO_ID_TYPE",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	CREATE_MISP_SUCCESS("PMS_MSP_200",MispConstant.AUDIT_USER,"Register MISP","Calling API to create MISP is success",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"NO_ID","NO_ID_TYPE",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	CREATED_MISP("PMS_MSP_212",MispConstant.AUDIT_USER,"Register MISP","Successfully created MISP id- %s",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"%s","MISP ID",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	MISP_INVALID_EMAIL_CREATE("PMS_MSP_401",MispConstant.AUDIT_SYSTEM,"Create MISP request","Invalid email id",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"NO_ID","NO_ID_TYPE",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	MISP_INVALID_EMAIL_UPDATE("PMS_MSP_416",MispConstant.AUDIT_SYSTEM,"Update MISP request","Invalid email id for MISP id - %s",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"%s","MISP ID",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	ORGANIZATION_NAME_EXISTS_UPDATE("PMS_MSP_402",MispConstant.AUDIT_SYSTEM,"Update MISP request","MISP is already registered with organization name for id - %s",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"%s","MISP ID",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	ORGANIZATION_NAME_EXISTS_CREATE("PMS_MSP_417",MispConstant.AUDIT_SYSTEM,"Create MISP request","MISP is already registered with organization name",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"NO_ID","NO_ID_TYPE",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	GENERATING_MISP_ID("PMS_MSP_102",MispConstant.AUDIT_SYSTEM,"Register MISP","Generating MISP id by using kernel MISP id generator",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"NO_ID","NO_ID_TYPE",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	GENERATED_MISP_DETAILS("PMS_MSP_103",MispConstant.AUDIT_SYSTEM,"Register MISP","MISP details created",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"NO_ID","NO_ID_TYPE",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	PROCESS_MISP("PMS_MSP_104",MispConstant.AUDIT_SYSTEM,"Processing MISP status request","Calling API to update MISP status with id - %s",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"%s","MISP ID",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	PROCESS_MISP_SUCCESS("PMS_MSP_201",MispConstant.AUDIT_SYSTEM,"Processing MISP status request","Calling API to update MISP status with id - %s is success",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"%s","MISP ID",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
    STATUS_EXCEPTION("PMS_MSP_403",MispConstant.AUDIT_SYSTEM,"Processing MISP status request","MISP status must either be approved or rejected for id -%s",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"%s","MISP ID",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
    VALIDATE_MISPID("PMS_MSP_104",MispConstant.AUDIT_SYSTEM,"Processing MISP status request","Validating MISP id -%s",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"%s","MISP ID",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
    MISP_EXCEPTION("PMS_MSP_404",MispConstant.AUDIT_SYSTEM,"Processing MISP status request","MISP exception",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"NO_ID","NO_ID_TYPE",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	MISP_DEACTIVATED("PMS_MSP_405",MispConstant.AUDIT_SYSTEM,"Processing MISP status request","MISP is de-activated. Cannot approve the de-activated MISP with id-%s",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"%s","MISP ID",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	UPDATED_MISP_STATUS_PROCESS("PMS_MSP_202",MispConstant.AUDIT_SYSTEM,"Process MISP status request","MISP status is updated for id - %s",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"%s","MISP",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	UPDATED_MISP_STATUS("PMS_MSP_211",MispConstant.AUDIT_SYSTEM,"Update MISP status","MISP status is updated for id - %s",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"%s","MISP",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	REJECTED_MISP_LICENSE("PMS_MSP_105",MispConstant.AUDIT_SYSTEM,"Processing MISP status request","MISP license key is not generated for rejected MISP with id-%s",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"%s","MISP ID",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	UPDATE_MISP_LICENSE("PMS_MSP_106",MispConstant.AUDIT_SYSTEM,"Processing MISP status request","Updating MISP license status for id- %s",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"%s","MISP ID",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	UPDATE_MISP("PMS_MSP_107",MispConstant.AUDIT_SYSTEM,"Update MISP request","Calling API to update MISP request",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"NO_ID","NO_ID_TYPE",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	UPDATE_MISP_SUCCESS("PMS_MSP_203",MispConstant.AUDIT_SYSTEM,"Update MISP request","Calling API to update MISP request is success",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"NO_ID","NO_ID_TYPE",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	UPDATING_MISP_STATUS("PMS_MSP_108",MispConstant.AUDIT_SYSTEM,"Update MISP request","Updated MISP status",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"NO_ID","NO_ID_TYPE",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	VALIDATE_LICENSE_KEY("PMS_MSP_109",MispConstant.AUDIT_SYSTEM,"Validate license key","Calling API to validate MISP license key",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"NO_ID","NO_ID_TYPE",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	VALIDATE_LICENSE_KEY_SUCCESS("PMS_MSP_204",MispConstant.AUDIT_SYSTEM,"Update MISP request","Calling API to validate MISP license key is success",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"NO_ID","NO_ID_TYPE",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	LICENSE_KEY_NOT_FOUND("PMS_MSP_406",MispConstant.AUDIT_SYSTEM,"Validate license key","No details found for license key",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"NO_ID","NO_ID_TYPE",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	FETCH_LICENSE_KEY_DETAILS("PMS_MSP_110",MispConstant.AUDIT_SYSTEM,"Validate license key","Fetching license key details for MISP id-%s",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"%s","MISP ID",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	LICENSE_KEY_STATUS("PMS_MSP_111",MispConstant.AUDIT_SYSTEM,"Validate license key","License key for MISP is %s",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"NO_ID","NO_ID_TYPE",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	ACTIVE_STATUS_EXCEPTION_UPDATE("PMS_MSP_418",MispConstant.AUDIT_SYSTEM,"Update MISP license key request","MISP status must either be Active or De-active for id - %s",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"%s","MISP ID",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	ACTIVE_STATUS_EXCEPTION_STATUS("PMS_MSP_407",MispConstant.AUDIT_SYSTEM,"Update MISP status request","MISP status must either be Active or De-active for id - %s",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"%s","MISP ID",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	UPDATE_MISP_STATUS("PMS_MSP_112",MispConstant.AUDIT_SYSTEM,"Update MISP status request","Calling API to update MISP status ",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"NO_ID","NO_ID_TYPE",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	UPDATE_MISP_STATUS_SUCCESS("PMS_MSP_205",MispConstant.AUDIT_SYSTEM,"Update MISP status request","Calling API to update MISP status is success",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"NO_ID","NO_ID_TYPE",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	MISP_ACTIVE_STATUS("PMS_MSP_113",MispConstant.AUDIT_SYSTEM,"Update MISP status request","MISP status is %s",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"NO_ID","NO_ID_TYPE",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	UPDATE_MISP_LICENSE_KEY_STATUS("PMS_MSP_114",MispConstant.AUDIT_SYSTEM,"Update MISP license key request","Calling API to update MISP license key",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"NO_ID","NO_ID_TYPE",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	UPDATE_MISP_LICENSE_KEY_STATUS_SUCCESS("PMS_MSP_206",MispConstant.AUDIT_SYSTEM,"Update MISP license key request","Calling API to update MISP license key is success",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"NO_ID","NO_ID_TYPE",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	MISP_ID_NO_LICENSE_KEY("PMS_MSP_408",MispConstant.AUDIT_SYSTEM,"Update MISP license key request","MISP license key not associated to id - %s",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"%s","MISP ID",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	CANNOT_ACTIVATE_LICENSE_KEY("PMS_MSP_419",MispConstant.AUDIT_SYSTEM,"Update MISP license key request","MISP license is expired.Cannot activate the same for MISP id-%s",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"%s","MISP ID",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	UPDATED_LICENSE_KEY("PMS_MSP_115",MispConstant.AUDIT_SYSTEM,"Update MISP license key request","Updating the MISP license status for id - %s",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"%s","MISP ID",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	GET_ALL_MISPS("PMS_MSP_116",MispConstant.AUDIT_SYSTEM,"Get all MISP reqest","Calling API to get all MISPs",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"NO_ID","NO_ID_TYPE",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	GET_ALL_MISPS_SUCCESS("PMS_MSP_207",MispConstant.AUDIT_SYSTEM,"Get all MISP request","Calling API to get all MISPs is success",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"NO_ID","NO_ID_TYPE",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	GET_MISP_BY_ID("PMS_MSP_117",MispConstant.AUDIT_SYSTEM,"Get MISP by id request","Calling API to get MISP based on id",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"NO_ID","NO_ID_TYPE",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	GET_MISP_BY_ID_SUCCESS("PMS_MSP_208",MispConstant.AUDIT_SYSTEM,"Get MISP by id request","Calling API to get MISP based on id is success",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"NO_ID","NO_ID_TYPE",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	NO_MISP_DETAILS("PMS_MSP_409",MispConstant.AUDIT_SYSTEM,"Get all MISP request","No MISP details found",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"NO_ID","NO_ID_TYPE",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	FETCH_MISP("PMS_MSP_118",MispConstant.AUDIT_SYSTEM,"Get MISP by id request","Fetched MISP for id -%s",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"%s","MISP ID",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	GET_MISP_BY_ORGANIZATION_NAME("PMS_MSP_119",MispConstant.AUDIT_SYSTEM,"Get MISP by organization name","Calling API to get MISP based on organization name",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"NO_ID","NO_ID_TYPE",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	GET_MISP_BY_ORGANIZATION_NAME_SUCCESS("PMS_MSP_209",MispConstant.AUDIT_SYSTEM,"Get MISP by organization name","Calling API to get MISP based on organization name is success",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"NO_ID","NO_ID_TYPE",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	NO_MISP_BY_ORGANIZATION_NAME("PMS_MSP_410",MispConstant.AUDIT_SYSTEM,"Get MISP by organization name","No MISP found while fetching MISP details by organization name",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"NO_ID","NO_ID_TYPE",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	DOWNLOAD_LICENSE_KEY("PMS_MSP_120",MispConstant.AUDIT_SYSTEM,"Request for downloading license key","Calling API to download license key",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"NO_ID","NO_ID_TYPE",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	DOWNLOAD_LICENSE_KEY_SUCCESS("PMS_MSP_210",MispConstant.AUDIT_SYSTEM,"Request for downloading license key","Calling API to download license key is success",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"NO_ID","NO_ID_TYPE",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	MISP_NOT_APPROVED("PMS_MSP_410",MispConstant.AUDIT_SYSTEM,"Request for downloading license key","MISP is not yet approved for id - %s",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"%s","MISP ID",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	MISP_INACTIVE("PMS_MSP_411",MispConstant.AUDIT_SYSTEM,"Request for downloading license key","MISP is not active for id - %s",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"%s","MISP ID",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	MISP_LICENSE_NOT_EXISTS("PMS_MSP_412",MispConstant.AUDIT_SYSTEM,"Request for downloading license key","MISP license Key does not exists for id - %s",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"%s","MISP ID",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	MISP_ALL_LICENSE_INACTIVE("PMS_MSP_413",MispConstant.AUDIT_SYSTEM,"Request for downloading license key","MISP license all are inactive for id - %s",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"%s","MISP ID",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	GENERATE_LICENSE("PMS_MSP_121",MispConstant.AUDIT_SYSTEM,"Request for downloading license key","Started generating license for MISP id - %s",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"%s","MISP ID",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	NO_MISP_ID("PMS_MSP_414",MispConstant.AUDIT_SYSTEM,"Finding MISP by id","No details found for MISP id-%s",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"%s","MISP ID",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID),
	VALIDATING_LICENSE_KEY("PMS_MSP_415",MispConstant.AUDIT_SYSTEM,"Validating license key","No details found for combination of MISP license key %s",MispConstant.MODULE_ID,MispConstant.MODULE_NAME,"%s","MISP ID",MispConstant.APPLICATION_NAME,MispConstant.APPLICATION_ID);
	
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
