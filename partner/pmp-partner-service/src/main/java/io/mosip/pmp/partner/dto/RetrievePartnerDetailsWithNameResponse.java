package io.mosip.pmp.partner.dto;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class RetrievePartnerDetailsWithNameResponse {
	
	private String id;
	private String address;
	private String contactNo;
	private String crBy;
	private Timestamp crDtimes;
	private String delDtimes;
	private String emailId;
	private Boolean isActive;
	private String name;
	private String policyGroupName;
	private String updBy;
	private Timestamp updDtimes;
	private String userId;
}
