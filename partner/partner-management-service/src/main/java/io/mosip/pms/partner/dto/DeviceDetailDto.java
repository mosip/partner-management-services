package io.mosip.pms.partner.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class DeviceDetailDto {

    @Schema(description = "Code representing the id of the device. The id can be a unique ID given by the user, or if it's null, the service will generate one automatically.", example = "12345")
    private String id;

    @Schema(description = "Code representing the type of device", example = "DT001")
    private String deviceTypeCode;

    @Schema(description = "Code representing the subtype of the device", example = "DST001")
    private String deviceSubTypeCode;

    @Schema(description = "Unique identifier for the device provider", example = "provider123")
    private String deviceProviderId;

    @Schema(description = "Manufacturer of the device", example = "AcmeCorp")
    private String make;

    @Schema(description = "Model of the device", example = "X1000")
    private String model;

    @Schema(description = "Approval status of the device (e.g., approved, pending, rejected)", example = "approved")
    private String approvalStatus;

    @Schema(description = "Indicates whether the device is active (true if active, false otherwise)", example = "true")
    @JsonProperty("isActive")
    private boolean isActive;

    @Schema(description = "Identifier for the user who created the record", example = "user456")
    private String crBy;

    @Schema(description = "Date and time when the record was created", example = "2024-08-08T10:00:00Z")
    private LocalDateTime crDtimes;

}
