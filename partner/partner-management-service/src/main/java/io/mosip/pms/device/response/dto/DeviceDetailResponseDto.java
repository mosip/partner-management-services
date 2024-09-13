package io.mosip.pms.device.response.dto;

import lombok.Data;

@Data
public class DeviceDetailResponseDto {
    private String deviceId;
    private String status;
    private boolean isActive;
}
