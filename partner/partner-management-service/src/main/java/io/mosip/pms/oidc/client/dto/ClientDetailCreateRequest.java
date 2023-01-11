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

    @NotBlank
    @Size(min = 1, max = 256)
    private String name;
    
    @NotBlank
    @Size(min = 1, max = 36)
    private String policyId;

    @NotEmpty
    private Map<String, @NotNull Object> publicKey;

    @NotBlank
    @Size(min = 1, max = 100)
    private String authPartnerId;

    @NotBlank
    @URL   
    @Size(max = 2048)
    private String logoUri;

    @NotNull
    @Size(min = 1, max = 5)
    private List<String> redirectUris;
    
    @NotNull
    @Size(min = 1, max = 3)
    private List<String> grantTypes;

    @NotNull
    @Size(min = 1, max = 3)
    private List<String> clientAuthMethods;

}
