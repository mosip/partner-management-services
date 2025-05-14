package io.mosip.pms.common.dto;

import lombok.Data;

@Data
public class DismissNotificationResponseDto {
    private String id;
    private String partnerId;
    private String notificationType;
    private String notificationStatus;
}
