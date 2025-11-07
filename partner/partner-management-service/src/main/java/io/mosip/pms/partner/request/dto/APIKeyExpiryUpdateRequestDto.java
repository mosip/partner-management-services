package io.mosip.pms.partner.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class APIKeyExpiryUpdateRequestDto {

	@Schema(description = "Name/label of the API key", example = "Sample API Key")
	private String apiKeyName;
	
	@Schema(description = "Expiry date and time for the API key", example = "2024-12-31T23:59:59.000Z")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
	private OffsetDateTime apiKeyExpiryDateTime;
}

