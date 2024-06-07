package io.mosip.pms.common.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name="device_detail_sbi")
public class DeviceDetailSBI implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6587350884850245583L;
	
	@EmbeddedId
	private DeviceDetailSBIPK id; 
	
//	@Id
//	@Column(name = "device_detail_id")
//	private String deviceDetailId;
//	
//	@Column(name = "sbi_id")
//	private String sbiId;
	
	@Column(name = "dprovider_id")
	private String providerId;
	
	@Column(name = "partner_org_name")
	private String partnerName;

	@Column(name="is_active")
	private Boolean isActive;
	
	@Column(name="is_deleted")
	private Boolean isDeleted;
	
	@Column(name="cr_by")
	private String crBy;

	@Column(name="cr_dtimes")
	private Timestamp crDtimes;

	@Column(name="del_dtimes")
	private Timestamp delDtimes;
	
	@Column(name="upd_by")
	private String updBy;

	@Column(name="upd_dtimes")
	private Timestamp updDtimes;
}
