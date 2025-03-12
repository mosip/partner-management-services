package io.mosip.pms.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiKeyDetailsDto {
    private String apiKeyId;
    private String partnerId;
    private LocalDateTime expiryDateTime;
    private int expiryPeriod;
}
