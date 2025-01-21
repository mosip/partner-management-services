package io.mosip.pms.device.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class FtmChipDetailsDto {

    @Schema(description = "Unique identifier for the FTM Chip details added by the partner", example = "ftp-001")
    private String ftmId;

    @Schema(description = "Unique identifier for the partner", example = "partner123")
    private String partnerId;

    @Schema(description = "Current status of the partner. Possible values are approved, deactivated and InProgress", example = "approved")
    private String partnerStatus;

    @Schema(description = "Make of the FTM chip", example = "make-123")
    private String make;

    @Schema(description = "Model of the FTM chip", example = "model-123")
    private String model;

    @Schema(description = "Current status of the FTM chip details added by the partner. Possible values are pending_cert_upload, pending_approval, rejected, approved", example = "pending_approval")
    private String status;

    @Schema(description = "Indicates whether the FTM chip details is active (true if active, false otherwise)", example = "false")
    private Boolean isActive;

    @Schema(description = "Indicates whether the FTM Chip certificate is available (true if available, false otherwise)", example = "true")
    private Boolean isCertificateAvailable;

    @Schema(description = "Date and time in ISO format indicating when the FTM Chip certificate was uploaded", example = "2024-07-15T10:00:00Z")
    private LocalDateTime certificateUploadDateTime;

    @Schema(description = "Date and time in ISO format indicating when the FTM Chip certificate will expire", example = "2025-07-15T10:00:00Z")
    private LocalDateTime certificateExpiryDateTime;

    @Schema(description = "Indicates whether the FTM certificate is expired (true if expired, false otherwise)", example = "false")
    private Boolean isCertificateExpired;

    @Schema(description = "Date and time in ISO format indicating when the FTM Chip details were added by the partner", example = "2024-08-01T14:30:00Z")
    private LocalDateTime createdDateTime;
}
