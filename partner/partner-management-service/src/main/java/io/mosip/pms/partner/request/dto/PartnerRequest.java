package io.mosip.pms.partner.request.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * @author sanjeev.shrivastava
 *
 */

@Data
public class PartnerRequest{
	
	@NotNull(message = "partnerId must not be null")
	@NotBlank(message="partnerId must not be empty")
	public String partnerId;
	
	public String policyGroup;
	
	@NotNull(message = "organizationName must not be null")
	@NotBlank(message="organizationName must not be empty")
	public String organizationName;
	
	@NotNull(message = "address must not be null")
	@NotBlank(message="address must not be empty")
	public String address;
	
	@NotNull(message = "contactNumber must not be null")
	@NotBlank(message="contactNumber must not be empty")
	public String contactNumber;
	
	@NotNull(message = "emailId must not be null")
	@NotBlank(message="emailId must not be empty")	
	public String emailId;
	
	@NotNull(message = "partnerType must not be null")
	@NotBlank(message="partnerType must not be empty")
	public String partnerType;
}