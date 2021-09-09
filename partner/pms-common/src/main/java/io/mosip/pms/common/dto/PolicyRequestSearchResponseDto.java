package io.mosip.pms.common.dto;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Id;


public class PolicyRequestSearchResponseDto implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	private String apikeyRequestId;

	@Column(name="cr_by")
	private String crBy;

	@Column(name="cr_dtimes")
	private Timestamp crDtimes;

	@Column(name="del_dtimes")
	private Timestamp delDtimes;

	@Column(name="is_deleted")
	private Boolean isDeleted;

	@Column(name="policy_id")
	private String policyId;

	@Column(name="request_datetimes")
	private Timestamp requestDatetimes;

	@Column(name="request_detail")
	private String requestDetail;

	@Column(name="status_code")
	private String statusCode;

	@Column(name="upd_by")
	private String updBy;

	@Column(name="upd_dtimes")
	private Timestamp updDtimes;

	@Column(name="part_id")
	private String partnerId;
	
	private String partnerName;
	
	private String policyName;

	public PolicyRequestSearchResponseDto() {
	}

	public String getApikeyRequestId() {
		return this.apikeyRequestId;
	}

	public void setApikeyRequestId(String apiRequestId) {
		this.apikeyRequestId = apiRequestId;
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

	public Timestamp getRequestDatetimes() {
		return this.requestDatetimes;
	}

	public void setRequestDatetimes(Timestamp requestDatetimes) {
		this.requestDatetimes = requestDatetimes;
	}

	public String getRequestDetail() {
		return this.requestDetail;
	}

	public void setRequestDetail(String requestDetail) {
		this.requestDetail = requestDetail;
	}

	public String getStatusCode() {
		return this.statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
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

	public String getPartnerId() {
		return this.partnerId;
	}

	public void setPartnerId(String partnerId) {
		this.partnerId = partnerId;
	}
	
	public String getPartnerName() {
		return this.partnerName;
	}

	public void setPartnerName(String partnerName) {
		this.partnerName = partnerName;
	}

	public String getPolicyName() {
		return this.policyName;
	}

	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}

}
