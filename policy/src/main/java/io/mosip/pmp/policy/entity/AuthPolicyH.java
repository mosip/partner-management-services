package io.mosip.pmp.policy.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


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

	@EmbeddedId
	private AuthPolicyHPK id;

	@Column(name="cr_by")
	private String crBy;

	@Column(name="cr_dtimes")
	private Timestamp crDtimes;

	@Column(name="del_dtimes")
	private Timestamp delDtimes;

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
	private Timestamp updDtimes;

	//bi-directional many-to-one association to PolicyGroup
//	@ManyToOne
//	@JoinColumn(name="policy_group_id")
//	private PolicyGroup policyGroup;

	public AuthPolicyH() {
	}

	public AuthPolicyHPK getId() {
		return this.id;
	}

	public void setId(AuthPolicyHPK id) {
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

	public Timestamp getDelDtimes() {
		return this.delDtimes;
	}

	public void setDelDtimes(Timestamp delDtimes) {
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

	public Timestamp getUpdDtimes() {
		return this.updDtimes;
	}

	public void setUpdDtimes(Timestamp updDtimes) {
		this.updDtimes = updDtimes;
	}

//	public PolicyGroup getPolicyGroup() {
//		return this.policyGroup;
//	}
//
//	public void setPolicyGroup(PolicyGroup policyGroup) {
//		this.policyGroup = policyGroup;
//	}

}