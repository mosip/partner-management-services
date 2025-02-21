package io.mosip.pms.partner.manager.dto;

import lombok.Data;

@Data
public class PartnerPolicyRequestFilterDto {
    private String partnerId;
    private String partnerType;
    private String partnerComment;
    private String organizationName;
    private String policyId;
    private String policyName;
    private String status;
    private String policyGroupName;
}
