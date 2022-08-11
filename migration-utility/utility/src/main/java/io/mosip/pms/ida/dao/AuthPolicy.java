package io.mosip.pms.ida.dao;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


/**
 * The persistent class for the auth_policy database table.
 * 
 */

@Entity
@Table(name="auth_policy")
@NamedQuery(name="AuthPolicy.findAll", query="SELECT a FROM AuthPolicy a")
public class AuthPolicy implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String id;

	@Column(name="cr_by")
	private String crBy;

	@Column(name="cr_dtimes")
	private Timestamp crDtimes;

	@Column(name="del_dtimes")
	private LocalDateTime delDtimes;

	private String descr;

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
	
	@Column(name = "valid_from_date")
	private LocalDateTime validFromDate;	

	@Column(name = "valid_to_date")	
	private LocalDateTime validToDate;
	
	@Column(name="version")
	private String version;
	
	@Column(name="policy_type")
	private String policyType;
	
	@Column(name="policy_schema")
	private String schema;

	//bi-directional many-to-one association to PolicyGroup
	@ManyToOne
	@JoinColumn(name="policy_group_id")
	private PolicyGroup policyGroup;

	public AuthPolicy() {
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

	public Timestamp getCrDtimes() {
		return this.crDtimes;
	}

	public void setCrDtimes(Timestamp crDtimes) {
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

	public String getPolicyFileId() {
		return this.policyFileId;
	}

	public void setPolicyFileId(String policyFileId) {
		this.policyFileId = policyFileId;
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

	public PolicyGroup getPolicyGroup() {
		return this.policyGroup;
	}

	public void setPolicyGroup(PolicyGroup policyGroup) {
		this.policyGroup = policyGroup;
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
	
	public void setPolicy_type(String policyType) {
		this.policyType = policyType;
	}
	
	public String getPolicy_type() {
		return this.policyType;
	}
	
	public void setPolicySchema(String schema) {
		this.schema = schema;
	}
	
	public String getPolicySchema() {
		return this.schema;
	}
}