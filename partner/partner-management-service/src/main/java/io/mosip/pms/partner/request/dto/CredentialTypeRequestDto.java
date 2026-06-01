package io.mosip.pms.partner.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CredentialTypeRequestDto {

	@NotBlank(message = "credentialType is missing")
	@Size(max = 128, message = "credentialType length must be <= 128")
	private String credentialType;
}

