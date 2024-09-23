package io.mosip.pms.partner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class CertificateDto {
    @Schema(description = "Unique identifier for the partner", example = "partner123")
    private String partnerId;

    @Schema(description = "Name of the certificate issued to the partner", example = "cert-001")
    private String certificateIssuedTo;

    @Schema(description = "Date when the certificate was uploaded", example = "2024-07-15T10:00:00Z")
    private Date certificateUploadDateTime;

    @Schema(description = "Date when the certificate expires", example = "2025-07-15T10:00:00Z")
    private Date certificateExpiryDateTime;

    @Schema(description = "Type of partner (e.g., distributor, supplier, etc.)", example = "distributor")
    private String partnerType;

    @Schema(description = "Indicates whether the certificate is available (true if available, false otherwise)", example = "true")
    private Boolean isCertificateAvailable;
}
