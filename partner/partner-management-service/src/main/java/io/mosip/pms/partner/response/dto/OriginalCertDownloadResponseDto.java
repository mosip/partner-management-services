package io.mosip.pms.partner.response.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OriginalCertDownloadResponseDto {
    /**
     * CA Signed Partner Certificate Data.
     */
    private String caSignedCertificateData;
    private Boolean isCaSignedCertificateExpired;
    /**
     * MOSIP Signed Partner Certificate Data.
     */
    private String mosipSignedCertificateData;
    private Boolean isMosipSignedCertificateExpired;
    /**
     * Response timestamp.
     */
    private LocalDateTime timestamp;
}
