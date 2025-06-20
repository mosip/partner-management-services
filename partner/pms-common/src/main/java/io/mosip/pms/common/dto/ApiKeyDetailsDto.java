package io.mosip.pms.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiKeyDetailsDto {
    private String apiKeyName;
    private String partnerId;
    private String expiryDateTime;
    private String expiryPeriod;
    private String policyGroup;
    private String policyName;
}
