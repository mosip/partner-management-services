package io.mosip.pms.partner.request.dto;

import lombok.Data;

@Data
public class SbiAndDeviceMappingRequestDto {
    private String partnerId;
    private String sbiId;
    private String deviceDetailId;
}
