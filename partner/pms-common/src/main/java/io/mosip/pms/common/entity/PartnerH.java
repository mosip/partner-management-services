package io.mosip.pms.common.entity;

import java.io.Serializable;
import jakarta.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the partner_h database table.
 * 
 */
@Entity
@Table(name="partner_h")
@NamedQuery(name="PartnerH.findAll", query="SELECT p FROM PartnerH p")
public class PartnerH implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private PartnerHPK id;

	private String address;

	@Column(name="approval_status")
	private String approvalStatus;

	@Column(name="certificate_alias")
	private String certificateAlias;

	@Column(name="contact_no")
	private String contactNo;

	@Column(name="cr_by")
	private String crBy;

	@Column(name="cr_dtimes")
	private Timestamp crDtimes;

	@Column(name="del_dtimes")
	private Timestamp delDtimes;

	@Column(name="email_id")
	private String emailId;

	@Column(name="is_active")
	private Boolean isActive;

	@Column(name="is_deleted")
	private Boolean isDeleted;

	private String name;

	@Column(name="partner_type_code")
	private String partnerTypeCode;

	@Column(name="policy_group_id")
	private String policyGroupId;

	@Column(name="upd_by")
	private String updBy;

	@Column(name="upd_dtimes")
	private Timestamp updDtimes;

	@Column(name="user_id")
	private String userId;
	
	@Column(name = "logo_url")
	private String logoUrl;
	
	@Column(name = "addl_info")
	private String additionalInfo;

	public PartnerH() {
	}

	public PartnerHPK getId() {
		return this.id;
	}

	public void setId(PartnerHPK id) {
		this.id = id;
	}

	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getApprovalStatus() {
		return this.approvalStatus;
	}

	public void setApprovalStatus(String approvalStatus) {
		this.approvalStatus = approvalStatus;
	}

	public String getCertificateAlias() {
		return this.certificateAlias;
	}

	public void setCertificateAlias(String certificateAlias) {
		this.certificateAlias = certificateAlias;
	}

	public String getContactNo() {
		return this.contactNo;
	}

	public void setContactNo(String contactNo) {
		this.contactNo = contactNo;
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
	
	public String getLogoUrl() {
		return this.logoUrl;
	}
	
	public String getAdditionalInfo() {
		return this.additionalInfo;
	}

	public String getEmailId() {
		return this.emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
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

	public String getPartnerTypeCode() {
		return this.partnerTypeCode;
	}

	public void setPartnerTypeCode(String partnerTypeCode) {
		this.partnerTypeCode = partnerTypeCode;
	}

	public String getPolicyGroupId() {
		return this.policyGroupId;
	}
	
	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}
	
	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	public void setPolicyGroupId(String policyGroupId) {
		this.policyGroupId = policyGroupId;
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