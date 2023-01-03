package io.mosip.pms.oidc.client.dto;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class CreateClientRequestDto {    
    private String clientId;
    private String clientName;
    private Map<String, @NotNull Object> publicKey;
    private String relyingPartyId;
    private List<String> userClaims;
    private List<String> authContextRefs;
    private String logoUri;
    private List<String> redirectUris;
    private List<String> grantTypes;
    private List<String> clientAuthMethods;
 
}
