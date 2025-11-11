package io.mosip.pms.oauth.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientDetailCreateRequestV3 extends ClientDetailCreateRequestV2{

    private AdditionalConfigDto additionalConfig;

    public ClientDetailCreateRequestV3(String name, String policyId, Map<String, Object> publicKey, String authPartnerId,
                                       String logoUri, List<String> redirectUris, List<String> grantTypes, List<String> clientAuthMethods,
                                       Map<String, String> clientNameLangMap, AdditionalConfigDto additionalConfig) {
        super(name, policyId, publicKey, authPartnerId, logoUri, redirectUris, grantTypes, clientAuthMethods, clientNameLangMap);
        this.additionalConfig = additionalConfig;
    }

}
