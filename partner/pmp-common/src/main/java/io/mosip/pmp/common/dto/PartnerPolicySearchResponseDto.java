package io.mosip.pmp.common.dto;

import java.io.Serializable;
import java.sql.Timestamp;

import lombok.Data;

@Data
public class PartnerPolicySearchResponseDto implements Serializable{

	private static final long serialVersionUID = 1L;

	
	private String policyApiKey;

	
	private String crBy;

	
	private Timestamp crDtimes;

	
	private Timestamp delDtimes;

	
	private Boolean isActive;

	
	private Boolean isDeleted;

	
	private String policyId;

	
	private String updBy;

	
	private Timestamp updDtimes;

	
	private Timestamp validFromDatetime;

	
	private Timestamp validToDatetime;

	
	private String partnerId;

	public PartnerPolicySearchResponseDto() {
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

	public String getPartnerId() {
		return this.partnerId;
	}

	public void setPartnerId(String partnerId) {
		this.partnerId = partnerId;
	}
}
