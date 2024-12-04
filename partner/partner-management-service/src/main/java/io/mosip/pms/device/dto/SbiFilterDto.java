package io.mosip.pms.device.dto;

import lombok.Data;

@Data
public class SbiFilterDto {

    private String partnerId;
    private String orgName;
    private String sbiVersion;
    private String status;
}
