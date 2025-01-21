package io.mosip.pms.partner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FtmProviderDto {

    @Schema(description = "Unique identifier for the partner", example = "partner123")
    private String partnerId;
}
