package io.mosip.pmp.partner.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PartnerUpdateRequest {
	
	@NotNull(message = "Please provide organizationName")
	private String organizationName;
	@NotNull(message = "Please provide address")
	private String address;
	@NotNull(message = "Please provide contactNumber")
	private String contactNumber;
	@NotNull(message = "Please provide emailId")
	private String emailId;
}
