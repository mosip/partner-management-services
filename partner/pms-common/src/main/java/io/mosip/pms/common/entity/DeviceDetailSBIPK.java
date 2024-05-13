package io.mosip.pms.common.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Data;

@Embeddable
@Data
public class DeviceDetailSBIPK implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1931522822517596895L;
	
	@Column(name = "device_detail_id")
	private String deviceDetailId;
	
	@Column(name = "sbi_id")
	private String sbiId;

}
