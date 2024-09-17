package io.mosip.pms.partner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class PolicyDto {
    @Schema(description = "Unique identifier for the partner", example = "partner123")
    private String partnerId;

    @Schema(description = "Type of partner (e.g., distributor, supplier, etc.)", example = "supplier")
    private String partnerType;

    @Schema(description = "Name of the policy associated with the partner", example = "Standard Policy")
    private String policyName;

    @Schema(description = "Date when the policy was created", example = "2024-07-15T10:00:00Z")
    private Date createdDateTime;

    @Schema(description = "Current status of the policy", example = "approved")
    private String status;

    @Schema(description = "Unique identifier for the policy group", example = "policyGroup456")
    private String policyGroupId;

    @Schema(description = "Description of the policy group", example = "Group of policies related to data protection")
    private String policyGroupDescription;

    @Schema(description = "Name of the policy group", example = "Data Protection Policies")
    private String policyGroupName;

    @Schema(description = "Unique identifier for the policy", example = "policy789")
    private String policyId;

    @Schema(description = "Description of the policy", example = "This policy outlines data protection measures.")
    private String policyDescription;

    @Schema(description = "Comments or notes from the partner about the policy", example = "Requires annual review.")
    private String partnerComments;

    @Schema(description = "Date and time when the policy was last updated", example = "2024-08-01T14:30:00Z")
    private Date updatedDateTime;
}