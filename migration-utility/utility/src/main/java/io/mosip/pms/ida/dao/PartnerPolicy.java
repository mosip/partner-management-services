package io.mosip.pms.ida.dao;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


/**
 * The persistent class for the partner_policy database table.
 * 
 */
@Entity
@Table(name="partner_policy")
@NamedQuery(name="PartnerPolicy.findAll", query="SELECT p FROM PartnerPolicy p")
public class PartnerPolicy implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="policy_api_key")
	private String policyApiKey;

	@Column(name="cr_by")
	private String crBy;

	@Column(name="cr_dtimes")
	private Timestamp crDtimes;

	@Column(name="del_dtimes")
	private Timestamp delDtimes;

	@Column(name="is_active")
	private Boolean isActive;

	@Column(name="is_deleted")
	private Boolean isDeleted;

	@Column(name="policy_id")
	private String policyId;

	@Column(name="upd_by")
	private String updBy;

	@Column(name="upd_dtimes")
	private Timestamp updDtimes;

	@Column(name="valid_from_datetime")
	private Timestamp validFromDatetime;

	@Column(name="valid_to_datetime")
	private Timestamp validToDatetime;

	//bi-directional many-to-one association to Partner
	@ManyToOne
	@JoinColumn(name="part_id")
	private Partner partner;
	
	@Column(name = "label")
	private String label;

	public PartnerPolicy() {
	}

	public String getPolicyApiKey() {
		return this.policyApiKey;
	}

	public void setPolicyApiKey(String policyApiKey) {
		this.policyApiKey = policyApiKey;
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

	public String getPolicyId() {
		return this.policyId;
	}

	public void setPolicyId(String policyId) {
		this.policyId = policyId;
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

	public Timestamp getValidFromDatetime() {
		return this.validFromDatetime;
	}

	public void setValidFromDatetime(Timestamp validFromDatetime) {
		this.validFromDatetime = validFromDatetime;
	}

	public Timestamp getValidToDatetime() {
		return this.validToDatetime;
	}

	public void setValidToDatetime(Timestamp validToDatetime) {
		this.validToDatetime = validToDatetime;
	}

	public Partner getPartner() {
		return this.partner;
	}

	public void setPartner(Partner partner) {
		this.partner = partner;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return this.label;
	}

}