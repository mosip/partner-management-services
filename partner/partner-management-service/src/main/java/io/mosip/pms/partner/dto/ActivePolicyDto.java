package io.mosip.pms.partner.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ActivePolicyDto {
    private String policyName;
    private String policyId;
    private String policyDescription;
    private String status;
}