package io.mosip.pms.policy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DeactivatePolicyGroupResponseDto {

    @Schema(description = "Unique identifier for the policy group", example = "12345")
    private String policyGroupId;

    @Schema(description = "Indicates whether the policy group is active (true if active, false otherwise)", example = "true")
    private Boolean isActive;
}
