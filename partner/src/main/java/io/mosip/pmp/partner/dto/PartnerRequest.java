package io.mosip.pmp.partner.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * @author sanjeev.shrivastava
 *
 */

@Data
public class PartnerRequest{
	
	@NotEmpty(message = "Please provide policyGroup")
	private String policyGroup;
	@NotEmpty(message = "Please provide organizationName")
	private String organizationName;
	@NotEmpty(message = "Please provide address")
	private String address;
	@NotEmpty(message = "Please provide contactNumber")
	private String contactNumber;
	@NotEmpty(message = "Please provide emailId")
	private String emailId;
}