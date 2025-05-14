package io.mosip.pms.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationsSeenResponseDto {

    @Schema(description = "Date and time when notifications was seen", example = "2024-08-08T10:00:00Z")
    private LocalDateTime notificationsSeenDtimes;
}
