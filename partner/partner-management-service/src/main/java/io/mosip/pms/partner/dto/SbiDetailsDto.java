package io.mosip.pms.partner.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class SbiDetailsDto {
    private String sbiId;
    private String sbiVersion;
    private String partnerId;
    private String partnerType;
    private String status;
    private String countOfDevices;
    private LocalDateTime sbiSoftwareCreatedDtimes;
    private LocalDateTime sbiSoftwareExpiryDtimes;
    private LocalDateTime crDtimes;
}
