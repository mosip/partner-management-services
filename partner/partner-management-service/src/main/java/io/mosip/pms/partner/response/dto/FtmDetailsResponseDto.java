package io.mosip.pms.partner.response.dto;

import lombok.Data;

@Data
public class FtmDetailsResponseDto {
    private String ftmId;
    private String status;
    private boolean active;
}
