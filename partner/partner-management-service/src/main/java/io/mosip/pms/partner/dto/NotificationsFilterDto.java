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
    private String createdFromDate;
    private String createdToDate;
    private String ftmId;
    private String make;
    private String model;
    private String apiKeyName;
    private String policyName;
}
