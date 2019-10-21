package io.mosip.pmp.partnermanagement.dto;

import lombok.Data;

@Data
public class RetrievePartnersDetails {
	
	public String partnerID;
	public String status;
	public String organizationName;
	public String contactNumber;
	public String emailId;
	public String address;

}
