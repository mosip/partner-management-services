package io.mosip.pms.partner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class SbiDetailsDto {
    @Schema(description = "Unique identifier for the SBI", example = "sbi-001")
    private String sbiId;

    @Schema(description = "Version of the SBI", example = "1.0.0")
    private String sbiVersion;

    @Schema(description = "Unique identifier for the partner", example = "partner123")
    private String partnerId;

    @Schema(description = "Current status of the SBI", example = "approved")
    private String status;

    @Schema(description = "Indicates whether the SBI is active (true if active, false otherwise)", example = "false")
    private boolean isSbiActive;

    @Schema(description = "Indicates whether the SBI is expired (true if expired, false otherwise)", example = "false")
    private boolean isSbiExpired;

    @Schema(description = "Number of approved devices associated with the SBI", example = "29")
    private String countOfApprovedDevices;

    @Schema(description = "Number of pending devices associated with the SBI", example = "10")
    private String countOfPendingDevices;

    @Schema(description = "Date and time when the SBI was created", example = "2024-07-15T10:00:00Z")
    private LocalDateTime sbiCreatedDateTime;

    @Schema(description = "Date and time when the SBI expires", example = "2025-07-15T10:00:00Z")
    private LocalDateTime sbiExpiryDateTime;

    @Schema(description = "Date and time when the record was created", example = "2024-08-01T14:30:00Z")
    private LocalDateTime createdDateTime;

}
