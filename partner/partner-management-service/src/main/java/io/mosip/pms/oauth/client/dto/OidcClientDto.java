package io.mosip.pms.oauth.client.dto;

import lombok.Data;

import java.util.List;

@Data
public class OidcClientDto {
    private String UserId;
    private String partnerId;
    private String OidcClientId;
    private String name;
    private String policyGroupName;
    private String policyName;
    private String relyingPartyId;
    private String logoUri;
    private List<String> redirectUris;
    private String publicKey;
    private String status;
    private List<String> grantTypes;
}
