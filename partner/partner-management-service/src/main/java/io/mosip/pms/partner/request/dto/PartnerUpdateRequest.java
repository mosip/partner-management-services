package io.mosip.pms.partner.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class PartnerUpdateRequest {	
	
	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 2000, message = "Length should be between 1 and 2000 chars")
	public String address;
	
	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 16, message = "Length should be between 1 and 16 chars")
	public String contactNumber;	
}
