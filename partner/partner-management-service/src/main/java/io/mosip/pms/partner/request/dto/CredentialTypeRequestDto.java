package io.mosip.pms.partner.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CredentialTypeRequestDto {

	/**
	 * Partner policy request id ({@code pms.partner_policy_request.id}).
	 */
	@NotBlank(message = "partnerPolicyRequestId is missing")
	@Size(max = 36, message = "partnerPolicyRequestId length must be <= 36")
	private String partnerPolicyRequestId;

	@NotBlank(message = "credentialType is missing")
	@Size(max = 128, message = "credentialType length must be <= 128")
	private String credentialType;
}

