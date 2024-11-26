package io.mosip.pms.partner.manager.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ApiKeyFilterDto {

    private String partnerId;

    private String apiKeyLabel;

    private String orgName;

    private String policyName;

    private String policyGroupName;

    private String status;

}
