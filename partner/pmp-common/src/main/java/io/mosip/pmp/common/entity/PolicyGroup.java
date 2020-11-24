package io.mosip.pmp.common.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


/**
 * The persistent class for the policy_group database table.
 * 
 */
/**
 * @author Nagarjuna Kuchi
 *
 */

@Entity
@Table(name="policy_group")
@NamedQuery(name="PolicyGroup.findAll", query="SELECT p FROM PolicyGroup p")
public class PolicyGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String id;

	@Column(name="cr_by")
	private String crBy;

	@Column(name="cr_dtimes")
	private LocalDateTime crDtimes;

	@Column(name="del_dtimes")
	private LocalDateTime delDtimes;

	private String descr;

	@Column(name="is_active")
	private Boolean isActive;

	@Column(name="is_deleted")
	private Boolean isDeleted;

	private String name;

	@Column(name="upd_by")
	private String updBy;

	@Column(name="upd_dtimes")
	private LocalDateTime updDtimes;

	@Column(name="user_id")
	private String userId;	

	public PolicyGroup() {
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

	public String getUserId() {
		return this.userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

//	public List<AuthPolicy> getAuthPolicies() {
//		return this.authPolicies;
//	}

//	public void setAuthPolicies(List<AuthPolicy> authPolicies) {
//		this.authPolicies = authPolicies;
//	}

//	public AuthPolicy addAuthPolicy(AuthPolicy authPolicy) {
//		getAuthPolicies().add(authPolicy);
//		authPolicy.setPolicyGroup(this);
//
//		return authPolicy;
//	}

//	public AuthPolicy removeAuthPolicy(AuthPolicy authPolicy) {
//		getAuthPolicies().remove(authPolicy);
//		authPolicy.setPolicyGroup(null);
//
//		return authPolicy;
//	}

//	public List<AuthPolicyH> getAuthPolicyHs() {
//		return this.authPolicyHs;
//	}

//	public void setAuthPolicyHs(List<AuthPolicyH> authPolicyHs) {
//		this.authPolicyHs = authPolicyHs;
//	}
//
//	public AuthPolicyH addAuthPolicyH(AuthPolicyH authPolicyH) {
//		getAuthPolicyHs().add(authPolicyH);
//		authPolicyH.setPolicyGroup(this);
//
//		return authPolicyH;
//	}
//
//	public AuthPolicyH removeAuthPolicyH(AuthPolicyH authPolicyH) {
//		getAuthPolicyHs().remove(authPolicyH);
//		authPolicyH.setPolicyGroup(null);
//
//		return authPolicyH;
//	}

}