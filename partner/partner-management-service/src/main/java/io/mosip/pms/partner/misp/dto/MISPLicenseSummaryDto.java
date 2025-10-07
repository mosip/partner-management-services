package io.mosip.pms.partner.misp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MISPLicenseSummaryDto {

    @Schema(description = "Unique identifier for the partner", example = "partner123")
    private String partnerId;

    @Schema(description = "Name of the partner organization", example = "Example Organization")
    private String orgName;

    @Schema(description = "Unique identifier for the policy group", example = "policyGroup789")
    private String policyGroupId;

    @Schema(description = "Name of the policy group", example = "Security Policies")
    private String policyGroupName;

    @Schema(description = "Description of the policy group", example = "Security Policies")
    private String policyGroupDescription;

    @Schema(description = "Unique identifier for the policy", example = "policy789")
    private String policyId;

    @Schema(description = "Name of the policy", example = "Data Protection Policy")
    private String policyName;

    @Schema(description = "Description of the policy", example = "Data Protection Policy")
    private String policyDescription;

    @Schema(description = "Name of the MISP license key", example = "MISP License Key 1")
    private String mispLicenseKeyName;

    @Schema(description = "Unique identifier for the MISP partner", example = "xyz")
    private String mispLicenseKey;

    @Schema(description = "Date and time when the MISP License expires", example = "2025-07-15T10:00:00Z")
    private LocalDateTime expiryDateTime;

    @Schema(description = "Current status of the OIDC client", example = "active")
    private String status;

    @Schema(description = "Date and time when the record was created", example = "2024-08-08T10:00:00Z")
    private LocalDateTime createdDateTime;
}
