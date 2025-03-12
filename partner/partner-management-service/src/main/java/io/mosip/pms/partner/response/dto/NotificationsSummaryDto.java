package io.mosip.pms.partner.response.dto;

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
