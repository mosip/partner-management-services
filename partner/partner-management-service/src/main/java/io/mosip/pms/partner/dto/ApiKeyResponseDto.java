package io.mosip.pms.partner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class ApiKeyResponseDto {
    @Schema(description = "Current status of the entity", example = "active")
    private String status;

    @Schema(description = "Label or identifier for the API key", example = "api-key-001")
    private String apiKeyLabel;

    @Schema(description = "Unique identifier for the partner", example = "partner123")
    private String partnerId;

    @Schema(description = "Unique identifier for the policy group", example = "policyGroup456")
    private String policyGroupId;

    @Schema(description = "Name of the policy group", example = "Compliance Policies")
    private String policyGroupName;

    @Schema(description = "Description of the policy group", example = "Group of policies related to regulatory compliance and standards")
    private String policyGroupDescription;

    @Schema(description = "Unique identifier for the policy", example = "policy789")
    private String policyId;

    @Schema(description = "Name of the policy", example = "Data Protection Policy")
    private String policyName;

    @Schema(description = "Description of the policy name", example = "Policy that outlines data protection measures")
    private String policyDescription;

    @Schema(description = "Date and time when the record was created", example = "2024-07-15T10:00:00Z")
    private Date createdDateTime;
}
