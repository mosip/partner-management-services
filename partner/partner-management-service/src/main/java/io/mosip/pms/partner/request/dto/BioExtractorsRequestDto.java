package io.mosip.pms.partner.request.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BioExtractorsRequestDto {

	/**
	 * Partner policy request id ({@code pms.partner_policy_request.id}).
	 */
	@NotBlank(message = "partnerPolicyRequestId is missing")
	@Size(max = 36, message = "partnerPolicyRequestId length must be <= 36")
	private String partnerPolicyRequestId;

	@Valid
	@NotNull(message = "extractors is missing")
	@NotEmpty(message = "extractors must not be empty")
	private List<@NotNull @Valid BioExtractorsDto> extractors;
}

