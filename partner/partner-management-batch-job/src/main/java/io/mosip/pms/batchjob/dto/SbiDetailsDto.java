package io.mosip.pms.batchjob.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SbiDetailsDto {
    private String sbiId;
    private String partnerId;
    private LocalDateTime expiryDateTime;
    private int expiryPeriod;
}
