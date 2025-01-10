package io.mosip.pms.partner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PartnerDtoV3 {
    @Schema(description = "Unique identifier for the partner", example = "partner123")
    private String partnerId;

    @Schema(description = "Type of partner", example = "AUTH_PARTNER")
    private String partnerType;

    @Schema(description = "Unique identifier for the policy group", example = "policyGroup456")
    private String policyGroupId;

    @Schema(description = "Name of the policy group", example = "Compliance Policies")
    private String policyGroupName;

    @Schema(description = "Description of the policy group", example = "Group of policies related to regulatory compliance and standards")
    private String policyGroupDescription;
}
