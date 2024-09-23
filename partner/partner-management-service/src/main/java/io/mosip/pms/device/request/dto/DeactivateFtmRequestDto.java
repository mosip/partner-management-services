package io.mosip.pms.device.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DeactivateFtmRequestDto {

    @Schema(description = "Unique identifier for the FTM", example = "ftm456")
    private String ftmId;
}
