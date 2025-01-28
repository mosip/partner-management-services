package io.mosip.pms.partner.manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
public class PartnerPolicyRequestSummaryDto {

    @Schema(description = "Unique identifier for the partner policy mapping request", example = "1213")
    private String id;

    @Schema(description = "Unique identifier for the partner", example = "partner123")
    private String partnerId;

    @Schema(description = "Status of the partner (e.g., activated, InProgress, deactivated)", example = "activated")
    private String partnerStatus;

    @Schema(description = "Name of the partner organisation", example = "abc")
    private String orgName;

    @Schema(description = "Name of the policy", example = "Compliance Policy")
    private String policyName;

    @Schema(description = "Unique Id for policy group", example = "Compliance Policy group")
    private String policyGroupId;

    @Schema(description = "Name of the policy group", example = "Compliance Policy group")
    private String policyGroupName;

    @Schema(description = "Request details about partner-policy mapping", example = "Requesting for compliance policies")
    private String partnerComment;

    @Schema(description = "Status of the partner policy mapping request (e.g., approved, InProgress)", example = "approved")
    private String status;

    @Schema(description = "Date and time when the request was created", example = "2024-08-01T14:30:00Z")
    private Date createdDateTime;

    @Schema(description = "Type of partner (e.g., Auth_Partner, Device_Provider, etc.)", example = "Auth_partner")
    private String partnerType;

    @Schema(description = "Unique Id for policy", example = "12345")
    private String policyId;

    @Schema(description = "Date and time when the request was updated", example = "2024-08-01T14:30:00Z")
    private Date updatedDateTime;

    @Schema(description = "A description of the policy", example = "policy for compliance")
    private String policyDescription;

    @Schema(description = "A description of policy group", example = "contains all compliance policies")
    private String policyGroupDescription;

}
