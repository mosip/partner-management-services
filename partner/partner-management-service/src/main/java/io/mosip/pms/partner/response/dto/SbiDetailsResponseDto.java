package io.mosip.pms.partner.response.dto;

import lombok.Data;

@Data
public class SbiDetailsResponseDto {
    private String sbiId;
    private String sbiVersion;
    private String status;
    private boolean active;
}
