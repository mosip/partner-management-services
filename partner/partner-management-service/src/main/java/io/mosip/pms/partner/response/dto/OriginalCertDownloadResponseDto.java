package io.mosip.pms.partner.response.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OriginalCertDownloadResponseDto {

    /**
     * CA Signed Partner Certificate Data.
     */
    @Schema(description = "Data for the CA (Certificate Authority) signed partner certificate", example = "MIICIjANBgkqhkiG9w0BAQEFA...") // Example value should be a realistic but truncated certificate string
    private String caSignedCertificateData;

    @Schema(description = "Indicates whether the CA signed certificate is expired (true if expired, false otherwise)", example = "false")
    private Boolean isCaSignedCertificateExpired;

    /**
     * MOSIP Signed Partner Certificate Data.
     */
    @Schema(description = "Data for the MOSIP signed partner certificate", example = "MIICIjANBgkqhkiG9w0BAQEFA...") // Example value should be a realistic but truncated certificate string
    private String mosipSignedCertificateData;

    @Schema(description = "Indicates whether the MOSIP signed certificate is expired (true if expired, false otherwise)", example = "true")
    private Boolean isMosipSignedCertificateExpired;
    /**
     * Response timestamp.
     */

}
