package io.mosip.pms.partner.manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
public class PartnerPolicyRequestSummaryDto {

    @Schema(description = "Unique identifier for the partner", example = "partner123")
    private String id;

    @Schema(description = "Unique identifier for the partner", example = "partner123")
    private String partnerId;

    @Schema(description = "Name of the partner organisation", example = "abc")
    private String orgName;

    @Schema(description = "Name of the policy", example = "Compliance Policy")
    private String policyName;

    @Schema(description = "Name of the policy group", example = "Compliance Policy group")
    private String policyGroupName;

    @Schema(description = "Request details about partner-policy mapping", example = "Requesting for compliance policies")
    private String requestDetail;

    @Schema(description = "Status of the partner policy mapping request (e.g., approved, InProgress)", example = "approved")
    private String status;

    @Schema(description = "Date and time when the request was created", example = "2024-08-01T14:30:00Z")
    private Date createdDateTime;

    @Schema(description = "Type of partner (e.g., Auth_Partner, Device_Provider, etc.)", example = "Auth_partner")
    private String partnerType;

    @Schema(description = "Unique Id for policy", example = "12345")
    private String policyId;

}
