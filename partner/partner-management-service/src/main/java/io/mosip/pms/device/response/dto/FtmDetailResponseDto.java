package io.mosip.pms.device.response.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FtmDetailResponseDto {

    @Schema(description = "Unique identifier of FTM Chip Details", example = "ftm456")
    private String ftmId;

    @Schema(description = "Status of the FTM Chip details. Possible values are pending_cert_upload, pending_approval, rejected, approved", example = "approved")
    private String status;

    @Schema(description = "True indicates that record is Active", example = "false")
    @JsonProperty("isActive")
    private boolean isActive;
}
