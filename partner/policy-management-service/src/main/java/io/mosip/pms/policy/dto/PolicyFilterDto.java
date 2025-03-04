package io.mosip.pms.policy.dto;

import lombok.Data;
import java.util.Date;

@Data
public class PolicyFilterDto {

    private String policyId;

    private String policyType;

    private String policyName;

    private String policyDescription;

    private String policyGroupName;

    private String status;
}
