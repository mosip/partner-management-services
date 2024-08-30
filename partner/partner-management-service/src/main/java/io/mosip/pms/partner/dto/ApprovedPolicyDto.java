package io.mosip.pms.partner.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class ApprovedPolicyDto {
    private String partnerId;
    private String partnerType;
    private String policyGroupId;
    private String policyGroupDescription;
    private String policyGroupName;
    List<ActivePolicyDto> activePolicies;
}