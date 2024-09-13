package io.mosip.pms.device.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DeactivateSbiRequestDto {

    @Schema(description = "Unique identifier for the SBI", example = "sbi456")
    private String sbiId;
}
