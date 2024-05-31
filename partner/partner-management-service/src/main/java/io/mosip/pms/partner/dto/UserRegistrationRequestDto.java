package io.mosip.pms.partner.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationRequestDto {
	
	@NotBlank(message = "should not be null or empty")
	private String userName;

	private String firstName;

	private String lastName;

	@NotBlank(message = "should not be null or empty")
	private String contactNo;

	@NotBlank(message = "should not be null or empty")
	private String emailID;	

	private String organizationName;

	private String role;
	
	private String partnerId;

	private String userPassword;
}
