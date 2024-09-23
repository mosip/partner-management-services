package io.mosip.pms.partner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class ApprovedPolicyDto {
    @Schema(description = "Unique identifier for the partner", example = "partner123")
    private String partnerId;

    @Schema(description = "Unique identifier for the policy group", example = "policyGroup456")
    private String policyGroupId;

    @Schema(description = "Description of the policy group", example = "Group of policies related to compliance and regulations")
    private String policyGroupDescription;

    @Schema(description = "Name of the policy group", example = "Compliance Policies")
    private String policyGroupName;

    @Schema(description = "List of active policies associated with the partner")
    private List<ActivePolicyDto> activePolicies;
}