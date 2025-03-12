package io.mosip.pms.common.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationsSummaryDto {

    private String notificationId;

    private String notificationPartnerId;

    private String notificationType;

    private String notificationStatus;

    private LocalDateTime createdDateTime;

    private String notificationDetails;

}
