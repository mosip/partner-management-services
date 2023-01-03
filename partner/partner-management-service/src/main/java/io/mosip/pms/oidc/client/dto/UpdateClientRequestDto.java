package io.mosip.pms.oidc.client.dto;

import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;
import java.util.List;

@Data
public class UpdateClientRequestDto {
    
	@NotBlank
    @URL
    private String logoUri;

    @NotNull
    @Size(min = 1)
    private List<@NotBlank String> redirectUris;

    @NotBlank
    @Pattern(regexp = "^(ACTIVE)|(INACTIVE)$")
    private String status;

    @NotNull
    @Size(min = 1)
    private List<String> grantTypes;

    @NotBlank
    private String clientName;

    private List<String> userClaims;
    
    private List<String> authContextRefs;
    
    @NotNull
    @Size(min = 1)
    private List<String> clientAuthMethods;


}
