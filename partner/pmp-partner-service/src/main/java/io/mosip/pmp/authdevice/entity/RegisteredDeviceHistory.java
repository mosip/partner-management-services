package io.mosip.pmp.authdevice.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "registered_device_master_h")
public class RegisteredDeviceHistory {
	@Id	
	@Column(name = "code", nullable = false,length = 36)
	private String code;
	
	@Column(name = "eff_dtimes", nullable = false)
	private LocalDateTime effectDateTime;
	
	@Column(name = "status_code", length = 64,nullable=false)
	private String statusCode;
	
	@Column(name = "device_id", length = 256,nullable=false)
	private String deviceId;
	
	@Column(name = "device_sub_id", length = 256,nullable=false)
	private String deviceSubId;

	@Column(name = "digital_id", length = 1024,nullable=false)
	private String digitalId;
	
	@Column(name = "serial_number", unique = true, length = 64,nullable=false)
	private String serialNo;
	
	@Column(name = "device_detail_id", length = 36,nullable=false)
	private String deviceDetailId;

	@Column(name = "purpose", length = 64,nullable=false)
	private String purpose;

	@Column(name = "firmware", length = 128)
	private String firmware;

	@Column(name = "expiry_date")
	private LocalDateTime expiryDate;

	@Column(name = "certification_level", length = 3)
	private String certificationLevel;

	@Column(name = "foundational_trust_provider_id", length = 36)
	private String foundationalTPId;
	
	@Column(name="hotlisted",nullable=false)
	private boolean hotlisted;
	
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

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public LocalDateTime getEffectDateTime() {
		return effectDateTime;
	}

	public void setEffectDateTime(LocalDateTime effectDateTime) {
		this.effectDateTime = effectDateTime;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getDeviceSubId() {
		return deviceSubId;
	}

	public void setDeviceSubId(String deviceSubId) {
		this.deviceSubId = deviceSubId;
	}

	public String getDigitalId() {
		return digitalId;
	}

	public void setDigitalId(String digitalId) {
		this.digitalId = digitalId;
	}

	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	public String getDeviceDetailId() {
		return deviceDetailId;
	}

	public void setDeviceDetailId(String deviceDetailId) {
		this.deviceDetailId = deviceDetailId;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public String getFirmware() {
		return firmware;
	}

	public void setFirmware(String firmware) {
		this.firmware = firmware;
	}

	public LocalDateTime getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(LocalDateTime expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getCertificationLevel() {
		return certificationLevel;
	}

	public void setCertificationLevel(String certificationLevel) {
		this.certificationLevel = certificationLevel;
	}

	public String getFoundationalTPId() {
		return foundationalTPId;
	}

	public void setFoundationalTPId(String foundationalTPId) {
		this.foundationalTPId = foundationalTPId;
	}

	public boolean isHotlisted() {
		return hotlisted;
	}

	public void setHotlisted(boolean hotlisted) {
		this.hotlisted = hotlisted;
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
