package io.mosip.pms.partner.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DeactivateDeviceRequestDto {

    @Schema(description = "Unique identifier for the device details", example = "device789")
    private String deviceId;
}
