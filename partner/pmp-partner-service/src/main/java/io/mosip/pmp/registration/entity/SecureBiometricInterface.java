package io.mosip.pmp.registration.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "secure_biometric_interface")
@NamedQuery(name = "SecureBiometricInterface.findAll", query = "SELECT a FROM SecureBiometricInterface a")
public class SecureBiometricInterface {
	@Id
	public String id;

	@Column(name = "sw_binary_hash")
	@Lob
	public byte[] swBinaryHash;

	@Column(name = "sw_version")
	public String swVersion;

	@Column(name = "device_detail_id")
	public String deviceDetailId;

	@Column(name = "sw_cr_dtimes")
	public Timestamp swCrDtimes;

	@Column(name = "sw_expiry_dtimes")
	public Timestamp swExpiryDtimes;

	@Column(name = "approval_status")
	public String approvalStatus;

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
}
