package io.mosip.pmp.partner.dto;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

/**
 * @author sanjeev.shrivastava
 *
 */

@Data
public class PartnerRequest{
	
	@NotEmpty(message = "Please provide policyGroup")
	public String policyGroup;
	@NotEmpty(message = "Please provide organizationName")
	public String organizationName;
	@NotEmpty(message = "Please provide address")
	public String address;
	@NotEmpty(message = "Please provide contactNumber")
	public String contactNumber;
	@NotEmpty(message = "Please provide emailId")
	public String emailId;
	@NotEmpty(message="Please provide partner Type")
	public String partnerType;
}