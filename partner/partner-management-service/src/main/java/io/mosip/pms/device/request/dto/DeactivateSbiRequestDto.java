package io.mosip.pms.device.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DeactivateSbiRequestDto {

    @Schema(description = "Status for deactivate SBI", example = "De-Activate")
    private String status;
}
