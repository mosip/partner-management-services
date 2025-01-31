package io.mosip.pms.oauth.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
public class ClientSummaryDto {

    @Schema(description = "Unique identifier for the partner", example = "partner123")
    private String partnerId;

    @Schema(description = "Name of the partner organisation", example = "abc")
    private String orgName;

    @Schema(description = "Unique identifier for the policy group", example = "policyGroup789")
    private String policyGroupId;

    @Schema(description = "Name of the policy group", example = "Security Policies")
    private String policyGroupName;

    @Schema(description = "Description of the policy group", example = "Security Policies")
    private String policyGroupDescription;

    @Schema(description = "Name of the policy", example = "Data Protection Policy")
    private String policyName;

    @Schema(description = "Description of the policy", example = "Data Protection Policy")
    private String policyDescription;

    @Schema(description = "Unique identifier for the OIDC (OpenID Connect) client", example = "oidcClient456")
    private String clientId;

    @Schema(description = "Name of the OIDC client", example = "MyOIDCClient")
    private String clientName;

    @Schema(description = "Current status of the OIDC client", example = "active")
    private String status;

    @Schema(description = "Date and time when the record was last updated", example = "2024-08-08T12:00:00Z")
    private Date updatedDateTime;

    @Schema(description = "Date and time when the record was created", example = "2024-08-08T10:00:00Z")
    private Date createdDateTime;
}
