package io.mosip.pms.oidc.client.dto;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateClientRequestV2Dto extends CreateClientRequestDto {

	@NotEmpty
	 private Map<@Size(min=3, max=3) String, @NotBlank String> clientNameLangMap;
	
	 public CreateClientRequestV2Dto(String clientId, String clientName, Map<String, Object> publicKey, String relyingPartyId,
			 List<String> userClaims, List<String> authContextRefs, String logoUri, List<String> redirectUris, List<String> grantTypes, List<String> clientAuthMethods,
             Map<String, String> clientNameLangMap) {
				super(clientId, clientName, publicKey, relyingPartyId, userClaims, authContextRefs,
				logoUri, redirectUris, grantTypes, clientAuthMethods);
				this.clientNameLangMap = clientNameLangMap;
			}
	
}
