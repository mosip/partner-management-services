package io.mosip.pms.device.response.dto;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import io.mosip.pms.device.authdevice.entity.FoundationalTrustProvider;

public class FTPSearchResponseDto {
	
	@Id 
	@Column(name = "id", length = 36,nullable=false)
	private String ftpChipDetailId;
	
	@Column(name = "foundational_trust_provider_id", length = 36)
	private String ftpProviderId;
	
	@Column(name="make",length=36)
	private String make;
	
	@Column(name="model",length=36)
	private String model;
	
	@Column(name="certificate_alias",length=36,nullable=false)
	private String certificateAlias;
	
	@Column(name="partner_org_name",length=128)
	private String partnerOrganizationName;
	
	@Column(name="is_active",nullable=false)
	private boolean isActive;
	
	@Column(name="is_deleted")
	private boolean isDeleted;
	
	@Column(name="cr_by",length=256,nullable=false)
	private String crBy;

	@Column(name="cr_dtimes",nullable=false)
	private LocalDateTime crDtimes;

	@Column(name="del_dtimes")
	private LocalDateTime delDtimes;
	
	@Column(name="upd_by",length=256)
	private String updBy;

	@Column(name="upd_dtimes")
	private LocalDateTime updDtimes;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "foundational_trust_provider_id", referencedColumnName = "id", insertable = false, updatable = false)
	private FoundationalTrustProvider ftpProvider;

	public String getFtpChipDetailId() {
		return ftpChipDetailId;
	}

	public void setFtpChipDetailId(String ftpChipDetailId) {
		this.ftpChipDetailId = ftpChipDetailId;
	}

	public String getFtpProviderId() {
		return ftpProviderId;
	}

	public void setFtpProviderId(String ftpProviderId) {
		this.ftpProviderId = ftpProviderId;
	}

	public String getMake() {
		return make;
	}

	public void setMake(String make) {
		this.make = make;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getCertificateAlias() {
		return certificateAlias;
	}

	public void setCertificateAlias(String certificateAlias) {
		this.certificateAlias = certificateAlias;
	}

	public String getPartnerOrganizationName() {
		return partnerOrganizationName;
	}

	public void setPartnerOrganizationName(String partnerOrganizationName) {
		this.partnerOrganizationName = partnerOrganizationName;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public String getCrBy() {
		return crBy;
	}

	public void setCrBy(String crBy) {
		this.crBy = crBy;
	}

	public LocalDateTime getCrDtimes() {
		return crDtimes;
	}

	public void setCrDtimes(LocalDateTime crDtimes) {
		this.crDtimes = crDtimes;
	}

	public LocalDateTime getDelDtimes() {
		return delDtimes;
	}

	public void setDelDtimes(LocalDateTime delDtimes) {
		this.delDtimes = delDtimes;
	}

	public String getUpdBy() {
		return updBy;
	}

	public void setUpdBy(String updBy) {
		this.updBy = updBy;
	}

	public LocalDateTime getUpdDtimes() {
		return updDtimes;
	}

	public void setUpdDtimes(LocalDateTime updDtimes) {
		this.updDtimes = updDtimes;
	}
}
