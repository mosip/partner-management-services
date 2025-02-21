package io.mosip.pms.device.response.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DeviceDetailResponseDto {
    @Schema(description = "Unique identifier of Device", example = "12345")
    private String deviceId;

    @Schema(description = "Status of the Device (e.g., approved, pending, rejected)", example = "approved")
    private String status;

    @Schema(description = "Indicates whether the device is active (true if active, false otherwise)", example = "true")
    @JsonProperty("isActive")
    private boolean isActive;
}
