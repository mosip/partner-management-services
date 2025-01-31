package io.mosip.pms.partner.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrustCertificateResponseDto {

    /**
     * CA Certificate Data
     */
    private String p7bFile;

    /**
     * Response Timestamp
     */
    private LocalDateTime timestamp;
}
