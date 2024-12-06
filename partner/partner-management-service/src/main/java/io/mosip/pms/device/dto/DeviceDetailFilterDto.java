package io.mosip.pms.device.dto;

import lombok.Data;

@Data
public class DeviceDetailFilterDto {
    private String deviceId;
    private String partnerId;
    private String orgName;
    private String deviceType;
    private String deviceSubType;
    private String status;
    private String make;
    private String model;
    private String sbiId;
    private String sbiVersion;

}
