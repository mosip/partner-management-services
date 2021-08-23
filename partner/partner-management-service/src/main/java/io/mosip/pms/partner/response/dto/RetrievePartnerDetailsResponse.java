package io.mosip.pms.partner.response.dto;

import lombok.Data;

/**
 * @author sanjeev.shrivastava
 *
 */

@Data
public class RetrievePartnerDetailsResponse {
		
	private String partnerID;
	private String status;
	private String organizationName;
	private String contactNumber;
	private String emailId;
	private String address;
	private String partnerType;
	private String policyGroup;
	private Boolean isActive;
}
