package io.mosip.pms.partner.manager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrustCertificateSummaryDto {

    @Schema(description = "Type of the CA certificate (e.g., ROOT, INTERMEDIATE)", example = "ROOT")
    private String caCertificateType;

    @Schema(description = "Unique identifier for the CA certificate", example = "cert123")
    private String certId;

    @Schema(description = "Type of the partner domain (e.g., FTM, DEVICE, and AUTH)", example = "FTM")
    private String partnerDomain;

    @Schema(description = "The entity or person receiving the certificate.", example = "Company A")
    private String issuedTo;

    @Schema(description = "The entity or person issuing the certificate.", example = "Certificate Authority X")
    private String issuedBy;

    @Schema(description = "The unique hash of the certificate.", example = "xyx-abc-123")
    private String certThumbprint;

    @Schema(description = "The start date and time of the certificate's validity period", example = "2023-12-01T00:00:00")
    private LocalDateTime validFromDate;

    @Schema(description = "The end date and time of the certificate's validity period", example = "2024-12-01T00:00:00")
    private LocalDateTime validTillDate;

    @Schema(description = "The date and time when the certificate was uploaded", example = "2023-11-30T10:15:30")
    private LocalDateTime uploadTime;

    @Schema(description = "Indicates whether the certificate is active (true if active, false otherwise)", example = "true")
    private Boolean status;
}
