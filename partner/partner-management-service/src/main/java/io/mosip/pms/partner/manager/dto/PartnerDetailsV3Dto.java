package io.mosip.pms.partner.manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Getter
@Setter
public class PartnerDetailsV3Dto {

    @Schema(description = "Unique identifier for the partner", example = "partner123")
    private String partnerId;

    @Schema(description = "Status of the partner", example = "approved")
    private String approvalStatus;

    @Schema(description = "Created Date time of the partner Id.", example = "2024-07-15T10:00:00Z")
    private LocalDateTime createdDateTime;

    @Schema(description = "Type of partner (e.g., distributor, supplier, etc.)", example = "distributor")
    private String partnerType;

    @Schema(description = "Name of the organization the partner belongs to", example = "Org")
    private String organizationName;

    @Schema(description = "Name of the policy group name the partner has selected", example = "Policy123")
    private String policyGroupName;

    @Schema(description = "Email address of the partner", example = "partner@email.com")
    private String emailId;

    @Schema(description = "Name of the certificate issued to the partner", example = "cert-001")
    private String certificateIssuedTo;

    @Schema(description = "Date when the certificate was uploaded", example = "2024-07-15T10:00:00Z")
    private Date certificateUploadDateTime;

    @Schema(description = "Date when the certificate expires", example = "2025-07-15T10:00:00Z")
    private Date certificateExpiryDateTime;

    @Schema(description = "Indicates whether the certificate is available (true if available, false otherwise)", example = "true")
    private Boolean isCertificateAvailable;
}
