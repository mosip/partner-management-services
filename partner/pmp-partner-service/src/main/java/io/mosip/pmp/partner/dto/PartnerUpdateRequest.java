package io.mosip.pmp.partner.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PartnerUpdateRequest {
	
	@NotNull(message = "Please provide organizationName")
	public String organizationName;
	@NotNull(message = "Please provide address")
	public String address;
	@NotNull(message = "Please provide contactNumber")
	public String contactNumber;
	@NotNull(message = "Please provide emailId")
	public String emailId;
}
