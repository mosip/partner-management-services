package io.mosip.pmp.registration.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


@Entity
@Table(name = "device_master_h")
@NamedQuery(name = "DeviceMasterH.findAll", query = "SELECT a FROM DeviceMasterH a")
public class DeviceMasterH {
	@Id
	public String code;

	@Column(name = "status_code")
	public String statusCode;

	@Column(name = "device_id")
	public String deviceId;

	@Column(name = "device_sub_id")
	public String deviceSubId;

	@Column(name = "digital_id")
	public String digitalId;

	@Column(name = "serial_number")
	public String serialNumber;

	@Column(name = "device_detail_id")
	public String deviceDetailId;

	@Column(name = "purpose")
	public String purpose;

	@Column(name = "firmware")
	public String firmware;

	@Column(name = "expiry_date")
	public Timestamp expiryDate;

	@Column(name = "certification_level")
	public String certificationLevel;

	@Column(name = "foundational_trust_provider_id")
	public String foundationalTrustProviderId;

	@Column(name = "hotlisted")
	public boolean hotlisted;

	@Column(name = "is_active")
	public boolean isActive;

	@Column(name = "cr_by")
	public String crBy;

	@Column(name = "cr_dtimes")
	public Timestamp crDtimes;

	@Column(name = "upd_by")
	public String updBy;

	@Column(name = "upd_dtimes")
	public Timestamp updDtimes;

	@Column(name = "is_deleted")
	public boolean isDeleted;

	@Column(name = "del_dtimes")
	public Timestamp delDtimes;
	
	@Column(name="eff_dtimes")
	public Timestamp effDtimes;
}
