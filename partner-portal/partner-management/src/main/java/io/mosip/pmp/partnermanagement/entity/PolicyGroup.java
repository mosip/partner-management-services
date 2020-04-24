package io.mosip.pmp.partnermanagement.entity;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the policy_group database table.
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
	private Timestamp crDtimes;

	@Column(name="del_dtimes")
	private Timestamp delDtimes;

	private String descr;

	@Column(name="is_active")
	private Boolean isActive;

	@Column(name="is_deleted")
	private Boolean isDeleted;

	private String name;

	@Column(name="upd_by")
	private String updBy;

	@Column(name="upd_dtimes")
	private Timestamp updDtimes;

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

	public String getUserId() {
		return this.userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}