package io.mosip.pms.common.dto;

import java.io.Serializable;
import java.sql.Timestamp;

import lombok.Data;

@Data
public class PartnerPolicySearchResponseDto implements Serializable{

	private static final long serialVersionUID = 1L;

	
	private String label;
	
	
	private String crBy;

	
	private Timestamp crDtimes;

	
	private Timestamp delDtimes;

	
	private Boolean isActive;

	
	private Boolean isDeleted;

	
	private String policyId;
	
	private String policyName;

	
	private String updBy;

	
	private Timestamp updDtimes;

	
	private Timestamp validFromDatetime;

	
	private Timestamp validToDatetime;

	
	private String partnerId;
	
	private String partnerName;
}
