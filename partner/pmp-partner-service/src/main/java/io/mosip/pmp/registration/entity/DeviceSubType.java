package io.mosip.pmp.registration.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "device_sub_type")
@NamedQuery(name = "DeviceSubType.findAll", query = "SELECT a FROM DeviceSubType a")
public class DeviceSubType {
	@Id
	public String code;

	@Column(name = "name")
	public String name;

	@Column(name = "descr")
	@Lob
	public String description;

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
	
	
	@Column(name="device_type_code")
	public String deviceTypeCode; 

}
