package io.mosip.pms.partner.misp.dto;

import lombok.Data;

@Data
public class MISPFilterDto {
    private String partnerId;
    private String policyGroupName;
    private String policyName;
    private String status;
}
