package io.mosip.pms.device.response.dto;

import lombok.Data;

@Data
public class FtmDetailResponseDto {
    private String ftmId;
    private String status;
    private boolean isActive;
}
