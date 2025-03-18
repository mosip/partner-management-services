package io.mosip.pms.partner.dto;

import lombok.Data;


@Data
public class NotificationsFilterDto {

    private String certificateId;
    private String expiryDate;
    private String issuedBy;
    private String issuedTo;
    private String notificationStatus;
    private String notificationType;
    private Integer pageNo;
    private Integer pageSize;
    private String partnerDomain;
}
