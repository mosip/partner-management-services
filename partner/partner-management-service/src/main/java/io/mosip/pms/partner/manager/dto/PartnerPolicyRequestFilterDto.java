package io.mosip.pms.partner.manager.dto;

import lombok.Data;

@Data
public class PartnerPolicyRequestFilterDto {
    private String partnerId;
    private String partnerTypeCode;
    private String requestDetails;
    private String organizationName;
    private String policyName;
    private String status;
    private String policyGroupName;
}
