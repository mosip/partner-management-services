package io.mosip.pms.partner.request.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PartnerUpdateRequest {	
	@NotNull(message = "Please provide address")
	public String address;
	@NotNull(message = "Please provide contactNumber")
	public String contactNumber;	
}
