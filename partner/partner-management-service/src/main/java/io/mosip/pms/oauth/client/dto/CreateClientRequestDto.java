package io.mosip.pms.oauth.client.dto;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
