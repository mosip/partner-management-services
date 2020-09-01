package io.mosip.pmp.partner.dto;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class AddContactRequestDto {

	@NotEmpty(message = "Please provide address")
	public String address;
	@NotEmpty(message = "Please provide contactNumber")
	public String contactNumber;
	@NotEmpty(message = "Please provide emailId")
	public String emailId;	
	public Boolean is_Active;
}
