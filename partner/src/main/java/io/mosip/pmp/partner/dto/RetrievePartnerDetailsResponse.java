package io.mosip.pmp.partner.dto;

import lombok.Data;

/**
 * @author sanjeev.shrivastava
 *
 */

@Data
public class RetrievePartnerDetailsResponse {
		
	public String partnerID;
	public String policyGroup;
	public String organizationName;
	public String address;
	public String contactNumber;
	public String emailId;
}
