package io.mosip.pms.device.dto;

import lombok.Data;

@Data
public class FtmChipFilterDto {
    private String partnerId;
    private String orgName;
    private String ftmId;
    private String make;
    private String model;
    private String status;
}