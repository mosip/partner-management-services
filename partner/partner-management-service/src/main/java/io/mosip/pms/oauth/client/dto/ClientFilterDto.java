package io.mosip.pms.oauth.client.dto;

import lombok.Data;

@Data
public class ClientFilterDto {
    private String partnerId;
    private String orgName;
    private String policyGroupName;
    private String policyName;
    private String clientName;
    private String status;
}
