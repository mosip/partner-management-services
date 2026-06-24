package io.mosip.pms.partner.manager.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminPartnerDetailsDto extends PartnerDetailsV3Dto {

    @Schema(description = "URL of the partner logo", example = "https://logo.com")
    private String logoUrl;

    @Schema(description = "Partner specific configuration and metadata", example = "{\"orderRedirectUrl\":\"https://example.io/order\"}")
    private JsonNode additionalInfo;

}
