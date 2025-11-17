package io.mosip.pms.oauth.client.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class UpdateClientRequestDtoV3 extends UpdateClientRequestDto{

    private Map<String, String> clientNameLangMap;

    private Map<String, Object> additionalConfig;

    public UpdateClientRequestDtoV3(String logoUri, @NotNull List<@NotBlank String> redirectUris, String status,
                                    @NotNull List<String> grantTypes, String clientName, List<String> userClaims, List<String> authContextRefs,
                                    @NotNull List<String> clientAuthMethods, Map<String, String> clientNameLangMap, Map<String, Object> additionalConfig) {
        super(logoUri, redirectUris, status, grantTypes, clientName, userClaims, authContextRefs, clientAuthMethods);
        this.clientNameLangMap = clientNameLangMap;
        this.additionalConfig = additionalConfig;
    }

    public UpdateClientRequestDtoV3(UpdateClientRequestDto updateRequest, Map<String, String> clientNameLangMap, Map<String, Object> additionalConfig) {
        super(updateRequest.getLogoUri(), updateRequest.getRedirectUris(), updateRequest.getStatus(), updateRequest.getGrantTypes(),
                updateRequest.getClientName(), updateRequest.getUserClaims(), updateRequest.getAuthContextRefs(),
                updateRequest.getClientAuthMethods());
        this.clientNameLangMap = clientNameLangMap;
        this.additionalConfig = additionalConfig;
    }


}
