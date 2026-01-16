package io.mosip.pms.oauth.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateClientRequestDto {
    
	@NotBlank
    @URL
    private String logoUri;

    @NotNull
    @Size(min = 1)
    private List<@NotBlank String> redirectUris;

    @NotBlank
    @Pattern(regexp = "(ACTIVE)|(INACTIVE)")
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
