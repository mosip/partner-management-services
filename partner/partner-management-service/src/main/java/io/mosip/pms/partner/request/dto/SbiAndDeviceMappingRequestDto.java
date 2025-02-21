package io.mosip.pms.partner.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SbiAndDeviceMappingRequestDto {
    @Schema(description = "Unique identifier for the partner", example = "partner123")
    private String partnerId;

    @Schema(description = "Unique identifier for the SBI", example = "sbi456")
    private String sbiId;

    @Schema(description = "status of the SBI and Device mapping request", example = "approved")
    private String status;
}
