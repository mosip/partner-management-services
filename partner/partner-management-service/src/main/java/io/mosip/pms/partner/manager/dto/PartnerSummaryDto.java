package io.mosip.pms.partner.manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
public class PartnerSummaryDto {

    @Schema(description = "Unique identifier for the partner", example = "partner123")
    private String partnerId;

    @Schema(description = "Type of partner (e.g., Auth_Partner, Device_Provider, etc.)", example = "Auth_partner")
    private String partnerType;

    @Schema(description = "Name of the partner organisation", example = "abc")
    private String orgName;

    @Schema(description = "Unique Id for policy group", example = "12345")
    private String policyGroupId;

    @Schema(description = "Name of the policy group", example = "Compliance Policies")
    private String policyGroupName;

    @Schema(description = "Partner email address", example = "abc@gmail.com")
    private String emailAddress;

    @Schema(description = "Status of partner certificate (e.g. uploaded, notUploaded)", example = "uploaded")
    private String certificateUploadStatus;

    @Schema(description = "Status of the partner (e.g., approved, InProgress)", example = "approved")
    private String status;

    @Schema(description = "Indicates whether the partner is active (true if active, false otherwise)", example = "true")
    private Boolean isActive;

    @Schema(description = "Date and time when the record was created", example = "2024-08-01T14:30:00Z")
    private Date createdDateTime;

}
