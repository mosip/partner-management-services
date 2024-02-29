package io.mosip.pms.oauth.client.dto;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
