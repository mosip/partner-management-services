package io.mosip.pmp.partner.dto;

import lombok.Data;

/**
 * @author sanjeev.shrivastava
 *
 */

@Data
public class RetrievePartnerDetailsResponse {
		
	private String partnerID;
	private String policyGroup;
	private String organizationName;
	private String address;
	private String contactNumber;
	private String emailId;
}
