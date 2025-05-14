package io.mosip.pms.common.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;


/**
 * The persistent class for the auth_policy_h database table.
 * 
 */
/**
 * @author Nagarjuna Kuchi
 *
 */
@Entity
@Table(name="auth_policy_h")
@NamedQuery(name="AuthPolicyH.findAll", query="SELECT a FROM AuthPolicyH a")
public class AuthPolicyH implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id	
	@Column(name ="id")	
	public String id;

	@Column(name="policy_group_id")
	private String policy_group_id;
	
	@Column(name="cr_by")
	private String crBy;

	@Column(name="cr_dtimes")
	private LocalDateTime crDtimes;

	@Column(name="del_dtimes")
	private LocalDateTime delDtimes;

	@Column(name = "valid_from_date")
	private LocalDateTime validFromDate;	

	@Column(name = "valid_to_date")	
	private LocalDateTime validToDate;
	
	@Column(name="version")
	private String version;
	
	@Column(name="policy_schema")
	private String schema;

	private String descr;

	@Column(name="policy_type")
	private String policyType;
	
	@Column(name="is_active")
	private Boolean isActive;

	@Column(name="is_deleted")
	private Boolean isDeleted;

	private String name;

	@Column(name="policy_file_id")
	private String policyFileId;

	@Column(name="upd_by")
	private String updBy;

	@Column(name="upd_dtimes")
	private LocalDateTime updDtimes;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="eff_dtimes")
	private java.util.Date effDtimes;
	
	
	public AuthPolicyH() {
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCrBy() {
		return this.crBy;
	}

	public void setCrBy(String crBy) {
		this.crBy = crBy;
	}

	public LocalDateTime getCrDtimes() {
		return this.crDtimes;
	}

	public void setCrDtimes(LocalDateTime crDtimes) {
		this.crDtimes = crDtimes;
	}

	public LocalDateTime getDelDtimes() {
		return this.delDtimes;
	}

	public void setDelDtimes(LocalDateTime delDtimes) {
		this.delDtimes = delDtimes;
	}

	public String getDescr() {
		return this.descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public Boolean getIsActive() {
		return this.isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public Boolean getIsDeleted() {
		return this.isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public java.util.Date getEffDtimes() {
		return this.effDtimes;
	}
	
	public void setEffDtimes(java.util.Date effDtimes) {
		this.effDtimes = effDtimes;
	}
	
	public String getPolicyFileId() {
		return this.policyFileId;
	}

	public void setPolicyFileId(String policyFileId) {
		this.policyFileId = policyFileId;
	}

	public String getPolicy_group_id() {
		return this.policy_group_id;
	}
	
	public void setPolicy_group_id(String policy_group_id) {
		this.policy_group_id = policy_group_id;
	}
	
	public String getUpdBy() {
		return this.updBy;
	}

	public void setUpdBy(String updBy) {
		this.updBy = updBy;
	}

	public LocalDateTime getUpdDtimes() {
		return this.updDtimes;
	}

	public void setUpdDtimes(LocalDateTime updDtimes) {
		this.updDtimes = updDtimes;
	}
	
	public void setPolicy_type(String policyType) {
		this.policyType = policyType;
	}
	
	public String getPolicy_type(String policyType) {
		return this.policyType;
	}
	
	public void SetPolicySchema(String schema) {
		this.schema = schema;
	}
	
	public String getPolicySchema() {
		return this.schema;
	}
	
	public void SetVersion(String version) {
		this.version = version;
	}
	
	public String getVersion() {
		return this.version;
	}
	
	public LocalDateTime getValidFromDate() {
		return this.validFromDate;
	}

	public void setValidFromDate(LocalDateTime validFromDate) {
		this.validFromDate=validFromDate;
	}

	public LocalDateTime getValidToDate() {
		return this.validToDate;
	}

	public void setValidToDate(LocalDateTime validToDate) {
		this.validToDate=validToDate;
	}
}