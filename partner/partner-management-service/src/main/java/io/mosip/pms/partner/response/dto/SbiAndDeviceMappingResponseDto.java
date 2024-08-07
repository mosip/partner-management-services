package io.mosip.pms.partner.response.dto;

import lombok.Data;

import javax.persistence.Column;
import java.sql.Timestamp;

@Data
public class SbiAndDeviceMappingResponseDto {

    private String providerId;

    private String sbiId;

    private String deviceDetailId;

    private Boolean isActive;

}
