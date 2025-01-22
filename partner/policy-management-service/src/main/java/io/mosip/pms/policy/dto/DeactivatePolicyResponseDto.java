package io.mosip.pms.policy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DeactivatePolicyResponseDto {

    @Schema(description = "Unique identifier for the policy", example = "12345")
    private String policyId;

    @Schema(description = "Indicates whether the policy is active (true if active, false otherwise)", example = "true")
    private Boolean isActive;
}
