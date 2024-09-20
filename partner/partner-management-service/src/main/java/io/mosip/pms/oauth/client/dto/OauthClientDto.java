package io.mosip.pms.oauth.client.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OauthClientDto {
    private String UserId;
    private String partnerId;
    private String clientId;
    private String clientName;
    private String policyGroupId;
    private String policyGroupName;
    private String policyGroupDescription;
    private String policyId;
    private String policyName;
    private String policyDescription;
    private String relyingPartyId;
    private String logoUri;
    private List<String> redirectUris;
    private String publicKey;
    private String status;
    private List<String> grantTypes;
    private LocalDateTime createdDateTime;
    private LocalDateTime updatedDateTime;
    private List<String> clientAuthMethods;
}
