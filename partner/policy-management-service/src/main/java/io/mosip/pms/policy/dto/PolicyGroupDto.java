package io.mosip.pms.policy.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PolicyGroupDto {
    private String id;
    private String description;
    private Boolean isActive;
    private String name;
    private String updBy;
    private LocalDateTime updDtimes;
}
