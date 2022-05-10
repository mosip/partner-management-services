package io.mosip.pms.partner.dto;

import java.time.LocalDate;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonFormat;

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

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateOfBirth;

	private String gender;

	private String role;

	@NotBlank(message = "should not be null or empty")
	private String appId;

	private String userPassword;
}
