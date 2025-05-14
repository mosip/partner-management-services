package io.mosip.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationsResponseDto {

    private String notificationId;

    private String notificationPartnerId;

    private String notificationType;

    private String notificationStatus;

    private LocalDateTime createdDateTime;

    private NotificationDetailsDto notificationDetails;

}
