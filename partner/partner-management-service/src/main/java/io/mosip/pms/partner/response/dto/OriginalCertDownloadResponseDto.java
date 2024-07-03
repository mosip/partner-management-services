package io.mosip.pms.partner.response.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OriginalCertDownloadResponseDto {
    /**
     * CA Signed Partner Certificate Data.
     */
    private String caSignedCertificateData;
    /**
     * MOSIP Signed Partner Certificate Data.
     */
    private String mosipSignedCertificateData;
    /**
     * Response timestamp.
     */
    private LocalDateTime timestamp;
}
