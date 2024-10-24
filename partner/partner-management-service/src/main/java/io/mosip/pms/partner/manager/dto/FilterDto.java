package io.mosip.pms.partner.manager.dto;

import lombok.Data;

@Data
public class FilterDto {
    private String partnerId;
    private String partnerTypeCode;
    private String organizationName;
    private String policyGroupName;
    private String certificateUploadStatus;
    private String emailAddress;
    private Boolean isActive;
}
