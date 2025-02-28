package io.mosip.pms.partner.response.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FtmCertificateDownloadResponseDto {
    /**
     * CA Signed Partner Certificate Data.
     */
    @Schema(description = "Data for the CA (Certificate Authority) signed partner certificate", example = "MIICIjANBgkqhkiG9w0BAQEFA...") // Example value should be a realistic but truncated certificate string
    private String caSignedCertificateData;

    @Schema(description = "DateTime when the CA certificate was uploaded", example = "2024-07-15T10:00:00Z")
    private LocalDateTime caSignedCertUploadDateTime;

    @Schema(description = "DateTime when the CA certificate will get expired", example = "2024-07-15T10:00:00Z")
    private LocalDateTime caSignedCertExpiryDateTime;

    @Schema(description = "Indicates whether the CA signed certificate is expired (true if expired, false otherwise)", example = "false")
    private Boolean isCaSignedCertificateExpired;

    /**
     * MOSIP Signed Partner Certificate Data.
     */
    @Schema(description = "Data for the MOSIP signed partner certificate", example = "MIICIjANBgkqhkiG9w0BAQEFA...") // Example value should be a realistic but truncated certificate string
    private String mosipSignedCertificateData;

    @Schema(description = "DateTime when the mosip certificate was uploaded", example = "2024-07-15T10:00:00Z")
    private LocalDateTime mosipSignedCertUploadDateTime;

    @Schema(description = "DateTime when the mosip certificate will get expired", example = "2024-07-15T10:00:00Z")
    private LocalDateTime mosipSignedCertExpiryDateTime;

    @Schema(description = "Indicates whether the MOSIP signed certificate is expired (true if expired, false otherwise)", example = "true")
    private Boolean isMosipSignedCertificateExpired;
    /**
     * Response timestamp.
     */
}
