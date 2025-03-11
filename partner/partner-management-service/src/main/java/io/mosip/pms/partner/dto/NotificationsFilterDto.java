package io.mosip.pms.partner.dto;

import lombok.Data;

@Data
public class NotificationsFilterDto {

    private String filterBy;
    private String notificationStatus;
    private String notificationType;
}
