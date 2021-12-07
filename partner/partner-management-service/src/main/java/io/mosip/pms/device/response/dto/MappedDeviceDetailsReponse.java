package io.mosip.pms.device.response.dto;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class MappedDeviceDetailsReponse {		
	
	private String deviceDetailId;	
	
	private String sbiId;
	
	private String providerId;	
	
	private String providerName;

	private String make;
	
	private String model;
	
	private String deviceTypeCode;
	
	private String deviceSubTypeCode;
	
	private byte[] swBinaryHash;
	
	private String swVersion;

	private String crBy;

	private Timestamp crDtimes;

}
