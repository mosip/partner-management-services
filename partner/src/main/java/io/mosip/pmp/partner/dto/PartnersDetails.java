package io.mosip.pmp.partner.dto;

import lombok.Data;

@Data
public class PartnersDetails {
	
	private String partnerID;      
	private String organizationName; 
	private String contactNumber;
	private String policyName;
	private String emailId;
	private String status;
	private String address;
	private String createdBy;
	private String createdDateTime;
	private String updatedBy;
	private String updatedDateTime;
	private String apiKeyRequestStatus;
	
}
