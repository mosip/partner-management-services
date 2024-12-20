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
public class DeviceDto {

    @Schema(description = "Code representing the id of the device. The id can either be provided by the user as a unique ID or left as null. If it's provided, that unique ID will be used as the device_detail_id. If it's null, the service will automatically generate a unique ID for the device_detail_id.", example = "12345")
    private String deviceId;

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
    private String status;

    @Schema(description = "Indicates whether the device is active (true if active, false otherwise)", example = "true")
    @JsonProperty("isActive")
    private boolean isActive;

    @Schema(description = "Date and time when the record was created", example = "2024-08-08T10:00:00Z")
    private LocalDateTime createdDateTime;

}
