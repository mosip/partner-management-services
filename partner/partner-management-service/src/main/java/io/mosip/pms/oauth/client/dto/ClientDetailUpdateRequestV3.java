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
public class ClientDetailUpdateRequestV3 extends ClientDetailUpdateRequestV2 {

    private AdditionalConfigDto additionalConfig;

    public ClientDetailUpdateRequestV3(String logoUri, List<String> redirectUris, String status, List<String> grantTypes, String clientName, List<String> clientAuthMethods,
                                       Map<String, String> clientNameLangMap,
                                       AdditionalConfigDto additionalConfig) {
        super(logoUri, redirectUris, status, grantTypes, clientName, clientAuthMethods, clientNameLangMap);
        this.additionalConfig = additionalConfig;
    }
}
