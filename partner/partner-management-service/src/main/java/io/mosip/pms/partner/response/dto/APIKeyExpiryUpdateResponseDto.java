package io.mosip.pms.partner.response.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class APIKeyExpiryUpdateResponseDto {

	@Schema(description = "Unique identifier for the partner", example = "partner123")
	private String partnerId;

	@Schema(description = "Unique identifier for the policy", example = "policy456")
	private String policyId;

	@Schema(description = "Label of the API key", example = "Sample API Key")
	private String apiKeyName;

	@Schema(description = "Updated expiry date and time for the API key", example = "2024-12-31T23:59:59.000Z")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime apiKeyExpiryDateTime;

}


