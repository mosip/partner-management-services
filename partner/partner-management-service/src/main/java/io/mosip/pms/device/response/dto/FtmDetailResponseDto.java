package io.mosip.pms.device.response.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FtmDetailResponseDto {

    @Schema(description = "Unique identifier for the FTM", example = "ftm456")
    private String ftmId;

    @Schema(description = "Status of the FTM Chip details. Possible values are pending_cert_upload, pending_approval, rejected, approved", example = "approved")
    private String status;

    @Schema(description = "True indicates that record is Active", example = "false")
    private boolean isActive;
}
