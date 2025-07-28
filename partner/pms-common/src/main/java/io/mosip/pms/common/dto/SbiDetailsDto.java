package io.mosip.pms.common.dto;

import lombok.Data;

@Data
public class SbiDetailsDto {
    private String sbiId;
    private String sbiVersion;
    private String partnerId;
    private String expiryDateTime;
    private String expiryPeriod;
    private String sbiBinaryHash;
    private String sbiCreationDate;
}
