package io.mosip.pmp.registration.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


@Entity
@Table(name = "device_detail")
@NamedQuery(name = "DeviceDetail.findAll", query = "SELECT a FROM DeviceDetail a")
public class DeviceDetail {
	@Id
	public String id;

	@Column(name = "dprovider_id")
	public String dproviderId;

	@Column(name = "dtype_code")
	public String dtypeCode;

	@Column(name = "dstype_code")
	public String dstypeCode;

	@Column(name = "make")
	public String make;

	@Column(name = "model")
	public String model;

	@Column(name = "partner_org_name")
	public String partnerOrgName;

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
