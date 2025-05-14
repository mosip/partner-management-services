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

    @Schema(description = "First name of the partner", example = "John")
    private String firstName;

    @Schema(description = "Last name of the partner", example = "Doe")
    private String lastName;

    @Schema(description = "Status of the partner", example = "approved")
    private String approvalStatus;

    @Schema(description = "Created Date time of the partner Id.", example = "2024-07-15T10:00:00Z")
    private LocalDateTime createdDateTime;

    @Schema(description = "Type of partner (e.g., distributor, supplier, etc.)", example = "distributor")
    private String partnerType;

    @Schema(description = "Name of the organization the partner belongs to", example = "Org")
    private String organizationName;

    @Schema(description = "Contact Number of the partner", example = "8983983332")
    private String contactNumber;

    @Schema(description = "Name of the policy group name the partner has selected", example = "Policy123")
    private String policyGroupName;

    @Schema(description = "Description of the policy group the partner has selected", example = "PolicyGroup123")
    private String policyGroupDescription;

    @Schema(description = "Email address of the partner", example = "partner@email.com")
    private String emailId;

    @Schema(description = "Date when the certificate was uploaded", example = "2024-07-15T10:00:00Z")
    private Date certificateUploadDateTime;

    @Schema(description = "Date when the certificate expires", example = "2025-07-15T10:00:00Z")
    private Date certificateExpiryDateTime;

    @Schema(description = "Indicates whether the certificate is available (true if available, false otherwise)", example = "true")
    private Boolean isCertificateAvailable;

    @Schema(description = "Indicates whether the partner is active (true if active, false otherwise)", example = "true")
    private Boolean isActive;
}
