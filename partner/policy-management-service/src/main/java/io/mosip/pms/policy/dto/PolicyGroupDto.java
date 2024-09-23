package io.mosip.pms.policy.dto;

import lombok.Data;

@Data
public class PolicyGroupDto {
    private String id;
    private String description;
    private Boolean isActive;
    private String name;
}