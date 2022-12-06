package io.mosip.pms.oidc.client.dto;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.URL;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientDetailCreateRequest {

    @NotNull
    @NotBlank
    private String name;
    
    @NotNull
    @NotBlank
    private String policyName;

    @NotEmpty
    private Map<String, @NotNull Object> publicKey;

    @NotBlank
    private String relyingPartyId;

    @NotNull
    @NotBlank
    @URL   
    private String logoUri;

    @NotNull
    @Size(min = 1)
    private List<String> redirectUris;
    
    @NotNull
    @Size(min = 1)
    private List<String> grantTypes;

    @NotNull
    @Size(min = 1)
    private List<String> clientAuthMethods;

}
