package io.mosip.pms.oauth.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateClientRequestDtoV3 extends CreateClientRequestDto {

    private Map<String, String> clientNameLangMap;

    private Map<String, Object> additionalConfig;

    public CreateClientRequestDtoV3(String clientId, String clientName, Map<String, Object> publicKey, String relyingPartyId,
                                    List<String> userClaims, List<String> authContextRefs, String logoUri, List<String> redirectUris, List<String> grantTypes, List<String> clientAuthMethods,
                                    Map<String, String> clientNameLangMap, Map<String, Object> additionalConfig) {
        super(clientId, clientName, publicKey, relyingPartyId, userClaims, authContextRefs,
                logoUri, redirectUris, grantTypes, clientAuthMethods);
        this.clientNameLangMap = clientNameLangMap;
        this.additionalConfig = additionalConfig;
    }

    public CreateClientRequestDtoV3(CreateClientRequestDto createRequest, Map<String, String> clientNameLangMap, Map<String, Object> additionalConfig) {
        super(createRequest.getClientId(), createRequest.getClientName(), createRequest.getPublicKey(), createRequest.getRelyingPartyId(), createRequest.getUserClaims(),
                createRequest.getAuthContextRefs(), createRequest.getLogoUri(), createRequest.getRedirectUris(), createRequest.getGrantTypes(), createRequest.getClientAuthMethods());
        this.clientNameLangMap = clientNameLangMap;
        this.additionalConfig = additionalConfig;
    }

}
