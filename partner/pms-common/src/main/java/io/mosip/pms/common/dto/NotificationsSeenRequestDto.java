package io.mosip.pms.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class NotificationsSeenRequestDto {

    @Schema(description = "Indicates whether notifications have been seen (true if notifications is seen, false otherwise)", example = "true")
    private boolean notificationsSeen;
}
