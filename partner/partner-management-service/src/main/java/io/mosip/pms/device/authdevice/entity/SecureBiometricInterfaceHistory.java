package io.mosip.pms.device.authdevice.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="secure_biometric_interface_h")
public class SecureBiometricInterfaceHistory {
	@Id
	@Column(name = "id", nullable = false,length = 36)
	private String id;

	@Column(name = "eff_dtimes", nullable = false)
	private LocalDateTime effectDateTime;	
	
	@Column(name="sw_binary_hash",nullable=false)
	private byte[] swBinaryHAsh;
	
	@Column(name = "sw_version", nullable = false, length = 64)
	private String swVersion;
	
	@Column(name = "sw_cr_dtimes")
	private LocalDateTime swCreateDateTime;

	@Column(name = "sw_expiry_dtimes")
	private LocalDateTime swExpiryDateTime;
	
	@Column(name="approval_status",length=36,nullable=false)
	private String approvalStatus;
	
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

	@Column(name = "provider_id")
	private String providerId;
	
	@Column(name="partner_org_name")
	private String partnerOrgName;
	
	public String getProviderId() {
		return providerId;
	}
	
	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}
	
	public void setPartnerOrgName(String partnerOrgName) {
		this.partnerOrgName = partnerOrgName;
	}
	
	public String getPartnerOrgName() {
		return partnerOrgName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public LocalDateTime getEffectDateTime() {
		return effectDateTime;
	}

	public void setEffectDateTime(LocalDateTime effectDateTime) {
		this.effectDateTime = effectDateTime;
	}

	public byte[] getSwBinaryHAsh() {
		return swBinaryHAsh;
	}

	public void setSwBinaryHAsh(byte[] swBinaryHAsh) {
		this.swBinaryHAsh = swBinaryHAsh;
	}

	public String getSwVersion() {
		return swVersion;
	}

	public void setSwVersion(String swVersion) {
		this.swVersion = swVersion;
	}

	public LocalDateTime getSwCreateDateTime() {
		return swCreateDateTime;
	}

	public void setSwCreateDateTime(LocalDateTime swCreateDateTime) {
		this.swCreateDateTime = swCreateDateTime;
	}

	public LocalDateTime getSwExpiryDateTime() {
		return swExpiryDateTime;
	}

	public void setSwExpiryDateTime(LocalDateTime swExpiryDateTime) {
		this.swExpiryDateTime = swExpiryDateTime;
	}

	public String getApprovalStatus() {
		return approvalStatus;
	}

	public void setApprovalStatus(String approvalStatus) {
		this.approvalStatus = approvalStatus;
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

