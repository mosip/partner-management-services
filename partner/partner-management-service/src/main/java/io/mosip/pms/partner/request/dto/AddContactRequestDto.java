package io.mosip.pms.partner.request.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class AddContactRequestDto {

	@NotBlank(message="value is empty or null")
	public String address;
	@NotBlank(message="value is empty or null")
	public String contactNumber;
	@NotBlank(message="value is empty or null")
	public String emailId;	
	public Boolean is_Active;
}
