package io.mosip.pms.common.dto;

import lombok.Data;

@Data
public class ExpiryCertCountResponseDto {
    private String CertificateType;
    private long count;
    private Integer expiryPeriod;
}
