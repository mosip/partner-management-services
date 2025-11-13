package io.mosip.pms.oauth.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateClientRequestDtoV3 extends CreateClientRequestDtoV2 {

    private Map<String, Object> additionalConfig;

    public CreateClientRequestDtoV3(String clientId, String clientName, Map<String, Object> publicKey, String relyingPartyId,
                                    List<String> userClaims, List<String> authContextRefs, String logoUri, List<String> redirectUris, List<String> grantTypes, List<String> clientAuthMethods,
                                    Map<String, String> clientNameLangMap, Map<String, Object> additionalConfig) {
        super(clientId, clientName, publicKey, relyingPartyId, userClaims, authContextRefs,
                logoUri, redirectUris, grantTypes, clientAuthMethods, clientNameLangMap);
        this.additionalConfig = additionalConfig;
    }

    public CreateClientRequestDtoV3(CreateClientRequestDtoV2 createRequest, Map<String, Object> additionalConfig) {
        super(createRequest.getClientId(), createRequest.getClientName(), createRequest.getPublicKey(), createRequest.getRelyingPartyId(), createRequest.getUserClaims(),
                createRequest.getAuthContextRefs(), createRequest.getLogoUri(), createRequest.getRedirectUris(), createRequest.getGrantTypes(), createRequest.getClientAuthMethods(),
                createRequest.getClientNameLangMap());
        this.additionalConfig = additionalConfig;
    }

}
