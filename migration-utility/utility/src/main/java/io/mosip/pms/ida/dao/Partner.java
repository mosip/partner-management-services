package io.mosip.pms.ida.dao;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.*;

/**
 * The persistent class for the partner database table.
 * 
 */
@Entity
@Table(name="partner")
@NamedQuery(name="Partner.findAll", query="SELECT p FROM Partner p")
public class Partner implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String id;

	@Column(name="address")
	private String address;

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

	@Column(name="name")
	private String name;

	@Column(name="policy_group_id")
	private String policyGroupId;

	@Column(name="certificate_alias")
	private String certificateAlias;

	@Column(name = "partner_type_code")
	private String partnerTypeCode;
	
	@Column(name="approval_status")
	private String approvalStatus;
	
	@Column(name="upd_by")
	private String updBy;

	@Column(name="upd_dtimes")
	private Timestamp updDtimes;

	@Column(name="user_id")
	private String userId;
	
	@Column(name = "lang_code")
	private String langCode;

	//bi-directional many-to-one association to PartnerPolicy
	@OneToMany(mappedBy="partner")
	private List<PartnerPolicy> partnerPolicies;

	//bi-directional many-to-one association to PartnerPolicyRequest
	@OneToMany(mappedBy="partner")
	private List<PartnerPolicyRequest> partnerPolicyRequests;

	public Partner() {
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
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
	
	public String getLangCode() {
		return this.langCode;
	}

	public void setLangCode(String langCode) {
		this.langCode = langCode;
	}
	
	public void setPartnerTypeCode(String partnerTypeCode) {
		this.partnerTypeCode = partnerTypeCode;
	}
	
	public String getPartnerTypeCode() {
		return this.partnerTypeCode;
	}
	
	public void setApprovalStatus(String approvalStatus) {
		this.approvalStatus = approvalStatus;
	}
	
	public String getApprovalStatus() {
		return this.approvalStatus;
	}

	public String getPolicyGroupId() {
		return this.policyGroupId;
	}

	public void setPolicyGroupId(String policyGroupId) {
		this.policyGroupId = policyGroupId;
	}

	public String getCertificateAlias() {
		return this.certificateAlias;
	}

	public void setCertificateAlias(String certificateAlias) {
		this.certificateAlias = certificateAlias;
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

	public List<PartnerPolicy> getPartnerPolicies() {
		return this.partnerPolicies;
	}

	public void setPartnerPolicies(List<PartnerPolicy> partnerPolicies) {
		this.partnerPolicies = partnerPolicies;
	}

	public PartnerPolicy addPartnerPolicy(PartnerPolicy partnerPolicy) {
		getPartnerPolicies().add(partnerPolicy);
		partnerPolicy.setPartner(this);

		return partnerPolicy;
	}

	public PartnerPolicy removePartnerPolicy(PartnerPolicy partnerPolicy) {
		getPartnerPolicies().remove(partnerPolicy);
		partnerPolicy.setPartner(null);

		return partnerPolicy;
	}

	public List<PartnerPolicyRequest> getPartnerPolicyRequests() {
		return this.partnerPolicyRequests;
	}

	public void setPartnerPolicyRequests(List<PartnerPolicyRequest> partnerPolicyRequests) {
		this.partnerPolicyRequests = partnerPolicyRequests;
	}

	public PartnerPolicyRequest addPartnerPolicyRequest(PartnerPolicyRequest partnerPolicyRequest) {
		getPartnerPolicyRequests().add(partnerPolicyRequest);
		partnerPolicyRequest.setPartner(this);

		return partnerPolicyRequest;
	}

	public PartnerPolicyRequest removePartnerPolicyRequest(PartnerPolicyRequest partnerPolicyRequest) {
		getPartnerPolicyRequests().remove(partnerPolicyRequest);
		partnerPolicyRequest.setPartner(null);
		return partnerPolicyRequest;
	}

}