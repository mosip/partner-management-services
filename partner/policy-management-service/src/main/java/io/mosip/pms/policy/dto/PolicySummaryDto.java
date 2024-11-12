package io.mosip.pms.policy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
public class PolicySummaryDto {

    @Schema(description = "Unique identifier for the policy", example = "12345")
    private String policyId;

    @Schema(description = "Name of the policy", example = "abc")
    private String policyName;

    @Schema(description = "Description of the policy", example = "desc")
    private String policyDescription;

    @Schema(description = "Unique identifier for the policy group", example = "23456")
    private String policyGroupId;

    @Schema(description = "Name of the policy group", example = "abc")
    private String policyGroupName;

    @Schema(description = "Indicates whether the policy is active (true if active, false otherwise)", example = "true")
    private Boolean isActive;

    @Schema(description = "Date and time when the record was created", example = "2024-08-01T14:30:00Z")
    private Date createdDateTime;
}
