package io.mosip.pms.partner.manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
public class ApiKeyRequestSummaryDto {

    @Schema(description = "Unique identifier for the API key", example = "apiKey123")
    private String apiKeyId;

    @Schema(description = "Unique identifier for the partner", example = "partner123")
    private String partnerId;

    @Schema(description = "Name of the API key", example = "Sample API Key")
    private String apiKeyName;

    @Schema(description = "Name of the partner organization", example = "Organization ABC")
    private String orgName;

    @Schema(description = "Name of the associated policy", example = "Access Control Policy")
    private String policyName;

    @Schema(description = "Name of the policy group", example = "Security Policies")
    private String policyGroupName;

    @Schema(description = "Status of the API key (e.g., activated, deactivated)", example = "activated")
    private String status;

    @Schema(description = "Date and time when the API key was created", example = "2024-08-01T14:30:00Z")
    private Date createdDateTime;

}
