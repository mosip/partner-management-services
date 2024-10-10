package io.mosip.pms.device.response.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SbiDetailsResponseDto {

    @Schema(description = "Unique identifier for the SBI", example = "sbi-001")
    private String sbiId;

    @Schema(description = "Version of the SBI", example = "1.0.0")
    private String sbiVersion;

    @Schema(description = "Status of SBI (e.g., approved, pending, rejected)", example = "approved")
    private String status;

    @Schema(description = "Indicates whether the device is active (true if active, false otherwise)", example = "true")
    @JsonProperty("isActive")
    private boolean isActive;
}
