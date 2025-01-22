package io.mosip.pms.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyCountDto {

    private Long draftPoliciesCount = 0L;
    private Long activePoliciesCount = 0L;

}
