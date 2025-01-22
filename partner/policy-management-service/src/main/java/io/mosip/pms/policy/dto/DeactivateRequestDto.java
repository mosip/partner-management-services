package io.mosip.pms.policy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DeactivateRequestDto {

    @Schema(description = "Status for deactivate policy", example = "De-Activate")
    private String status;
}
