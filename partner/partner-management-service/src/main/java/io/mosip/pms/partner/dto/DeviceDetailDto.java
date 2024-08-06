package io.mosip.pms.partner.dto;

import io.mosip.pms.device.authdevice.entity.RegistrationDeviceSubType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class DeviceDetailDto {

    private String deviceTypeCode;

    private String deviceSubTypeCode;

    private String deviceProviderId;

    private String make;

    private String model;

    private String approvalStatus;

    private boolean isActive;

    private String crBy;

    private LocalDateTime crDtimes;
}
