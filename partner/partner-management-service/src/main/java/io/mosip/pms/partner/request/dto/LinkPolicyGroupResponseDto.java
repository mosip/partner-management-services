package io.mosip.pms.partner.request.dto;

import lombok.Data;

@Data
public class LinkPolicyGroupResponseDto {
    private String partnerId;
    private String policyGroupId;
    private String policyGroupName;
}
