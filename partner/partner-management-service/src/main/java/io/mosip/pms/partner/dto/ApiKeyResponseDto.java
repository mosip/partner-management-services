package io.mosip.pms.partner.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class ApiKeyResponseDto {
    private String status;
    private String apiKeyLabel;
    private String partnerId;
    private String policyGroupId;
    private String policyGroupName;
    private String policyGroupDescription;
    private String policyId;
    private String policyName;
    private String policyNameDescription;
    private Date crDtimes;
    private Date updDtimes;
}
