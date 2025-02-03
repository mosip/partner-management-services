package io.mosip.pms.partner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class DeviceProviderDto {
    @Schema(description = "Unique identifier for the partner", example = "partner123")
    private String partnerId;
}
