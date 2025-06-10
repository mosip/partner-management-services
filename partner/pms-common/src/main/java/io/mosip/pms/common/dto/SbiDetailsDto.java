package io.mosip.pms.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SbiDetailsDto {
    private String sbiId;
    private String sbiVersion;
    private String partnerId;
    private String expiryDateTime;
    private String expiryPeriod;
}
