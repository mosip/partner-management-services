package io.mosip.pms.partner.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

/**
 * @author sanjeev.shrivastava
 *
 */

@Data
public class PartnerRequest{	
	
	@NotBlank(message="value is empty or null")
	public String partnerId;
	
	public String policyGroup;	
	
	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 128, message = "Length should be between 1 and 128 chars")
	public String organizationName;
	
	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 2000, message = "Length should be between 1 and 2000 chars")
	public String address;
	
	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 16, message = "Length should be between 1 and 16 chars")
	public String contactNumber;
	
	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 254, message = "Length should be between 1 and 254 chars")
	public String emailId;
	
	@NotBlank(message="value is empty or null")
	public String partnerType;	
	
	public String langCode;
}