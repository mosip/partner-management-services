package io.mosip.pms.device.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DeactivateDeviceRequestDto {

    @Schema(description = "Status for deactivate device", example = "De-Activate")
    private String status;
}
