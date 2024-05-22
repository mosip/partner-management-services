package io.mosip.pms.device.authdevice.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="device_detail")
public class DeviceDetail implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id",length=36,nullable=false)
	private String id;
	
	@Column(name="dtype_code",length=36,nullable=false)
	private String deviceTypeCode;
	
	@Column(name="dstype_code",length=36,nullable=false)
	private String deviceSubTypeCode;
	
	@Column(name="dprovider_id",length=36,nullable=false)
	private String deviceProviderId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({
	@JoinColumn(name = "dtype_code", referencedColumnName = "dtyp_code", insertable = false, updatable = false),
	@JoinColumn(name = "dstype_code", referencedColumnName = "code", insertable = false, updatable = false)
	})
	private RegistrationDeviceSubType deviceSubType;
	
	@Column(name="make",length=36,nullable=false)
	private String make;
	
	@Column(name="model",length=36,nullable=false)
	private String model;
	
	@Column(name="approval_status",length=36,nullable=false)
	private String approvalStatus;
	
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDeviceProviderId() {
		return deviceProviderId;
	}

	public void setDeviceProviderId(String deviceProviderId) {
		this.deviceProviderId = deviceProviderId;
	}

	

	public String getDeviceTypeCode() {
		return deviceTypeCode;
	}

	public void setDeviceTypeCode(String deviceTypeCode) {
		this.deviceTypeCode = deviceTypeCode;
	}

	public String getDeviceSubTypeCode() {
		return deviceSubTypeCode;
	}

	public void setDeviceSubTypeCode(String deviceSubTypeCode) {
		this.deviceSubTypeCode = deviceSubTypeCode;
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

	public String getApprovalStatus() {
		return approvalStatus;
	}

	public void setApprovalStatus(String approvalStatus) {
		this.approvalStatus = approvalStatus;
	}

	public String getPartnerOrganizationName() {
		return partnerOrganizationName;
	}

	public void setPartnerOrganizationName(String partnerOrganizationName) {
		this.partnerOrganizationName = partnerOrganizationName;
	}

	public boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(boolean isDeleted) {
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
