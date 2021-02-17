package io.mosip.pms.common.dto;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SearchAuthPolicy  {


	private String id;

	private String crBy;

	private Timestamp crDtimes;

	private LocalDateTime delDtimes;

	private String desc;

	private Boolean isActive;

	private Boolean isDeleted;

	private String name;

	private String policies;

	private String updBy;

	private LocalDateTime updDtimes;
	
	private LocalDateTime validFromDate;

	private LocalDateTime validToDate;

	private String version;

	private String policyType;

	private String schema;

	private String policyGroupId;
	
	private String policyGroupName;

	public String getCrBy() {
		return crBy;
	}

	public Timestamp getCrDtimes() {
		return crDtimes;
	}

	public LocalDateTime getDelDtimes() {
		return delDtimes;
	}

	public String getDesc() {
		return desc;
	}

	public String getId() {
		return id;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public String getName() {
		return name;
	}

	public String getPolicies() {
		return policies;
	}

	public String getPolicyGroupId() {
		return policyGroupId;
	}

	public String getPolicyType() {
		return policyType;
	}

	public String getSchema() {
		return schema;
	}

	public String getUpdBy() {
		return updBy;
	}

	public LocalDateTime getUpdDtimes() {
		return updDtimes;
	}

	public LocalDateTime getValidFromDate() {
		return validFromDate;
	}

	public LocalDateTime getValidToDate() {
		return validToDate;
	}

	public String getVersion() {
		return version;
	}

	public void setCrBy(String crBy) {
		this.crBy = crBy;
	}

	public void setCrDtimes(Timestamp crDtimes) {
		this.crDtimes = crDtimes;
	}

	public void setDelDtimes(LocalDateTime delDtimes) {
		this.delDtimes = delDtimes;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPolicies(String policies) {
		this.policies = policies;
	}

	public void setPolicyGroupId(String policyGroupId) {
		this.policyGroupId = policyGroupId;
	}

	public void setPolicyType(String policyType) {
		this.policyType = policyType;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public void setUpdBy(String updBy) {
		this.updBy = updBy;
	}

	public void setUpdDtimes(LocalDateTime updDtimes) {
		this.updDtimes = updDtimes;
	}

	public void setValidFromDate(LocalDateTime validFromDate) {
		this.validFromDate = validFromDate;
	}

	public void setValidToDate(LocalDateTime validToDate) {
		this.validToDate = validToDate;
	}

	
	public void setVersion(String version) {
		this.version = version;
	}

}
