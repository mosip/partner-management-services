package io.mosip.pms.device.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeviceDetailSummaryDto {

    @Schema(description = "Unique identifier for the device", example = "device12")
    private String deviceId;

    @Schema(description = "Unique identifier for the partner", example = "partner123")
    private String partnerId;

    @Schema(description = "Name of the partner organization", example = "Organization ABC")
    private String orgName;

    @Schema(description = "Type of the device (e.g., FACE, FINGER, IRIS)", example = "FINGER")
    private String deviceType;

    @Schema(description = "Subtype of the device (e.g., single, double)", example = "single")
    private String deviceSubType;

    @Schema(description = "Status of the device (e.g., approved, rejected, pending_approval, deactivated)", allowableValues = {"approved", "rejected", "pending_approval", "deactivated"}, example = "approved")
    private String status;

    @Schema(description = "Make of the device", example = "Make123")
    private String make;

    @Schema(description = "Model of the device", example = "Model02")
    private String model;

    @Schema(description = "Date and time when the record was created", example = "2024-08-01T14:30:00Z")
    private LocalDateTime createdDateTime;

    @Schema(description = "Unique identifier for the Secure Biometric Interface (SBI) associated with the device.", example = "sbi12")
    private String sbiId;

    @Schema(description = "Software version of the Secure Biometric Interface (SBI).", example = "sw12")
    private String sbiVersion;

}
