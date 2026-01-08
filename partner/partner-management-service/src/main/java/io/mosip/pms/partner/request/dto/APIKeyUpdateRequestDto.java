package io.mosip.pms.partner.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class APIKeyUpdateRequestDto {

	@Schema(description = "Status of the API key. Use 'INACTIVE' to deactivate the API key", example = "INACTIVE")
	private String status;
	
	@Schema(description = "Expiry date and time for the API key", example = "2024-12-31T23:59:59.000Z")
	private OffsetDateTime expiryDateTime;
}

