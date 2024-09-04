package io.mosip.pms.partner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Getter
@Setter
public class FtmProviderDetailsDto {

    @Schema(description = "Unique identifier for the FTM", example = "ftp-001")
    private String ftmId;

    @Schema(description = "Unique identifier for the partner", example = "partner123")
    private String partnerId;

    @Schema(description = "Type of partner", example = "FTM_PROVIDER")
    private String partnerType;

    @Schema(description = "Make of the FTM", example = "make-123")
    private String make;

    @Schema(description = "Model of the FTM", example = "model-123")
    private String model;

    @Schema(description = "Current status of the FTM", example = "approved")
    private String status;

    @Schema(description = "Indicates whether the FTM is active (true if active, false otherwise)", example = "false")
    private Boolean isActive;

    @Schema(description = "Indicates whether the certificate is available (true if available, false otherwise)", example = "true")
    private Boolean isCertificateAvailable;

    @Schema(description = "Name of the certificate issued to the partner", example = "cert-001")
    private String certificateName;

    @Schema(description = "Date when the certificate was uploaded", example = "2024-07-15T10:00:00Z")
    private Date certificateUploadDate;

    @Schema(description = "Date when the certificate expires", example = "2025-07-15T10:00:00Z")
    private Date certificateExpiryDate;

    @Schema(description = "Indicates whether the FTM certificate is expired (true if expired, false otherwise)", example = "false")
    private Boolean isCertificateExpired;

    @Schema(description = "Date and time when the record was created", example = "2024-08-01T14:30:00Z")
    private LocalDateTime crDtimes;
}
