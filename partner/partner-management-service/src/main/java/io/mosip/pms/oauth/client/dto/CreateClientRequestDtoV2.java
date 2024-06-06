package io.mosip.pms.oauth.client.dto;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateClientRequestDtoV2 extends CreateClientRequestDto {

	@NotEmpty
	 private Map<@Size(min=3, max=3) String, @NotBlank String> clientNameLangMap;
	
	 public CreateClientRequestDtoV2(String clientId, String clientName, Map<String, Object> publicKey, String relyingPartyId,
			 List<String> userClaims, List<String> authContextRefs, String logoUri, List<String> redirectUris, List<String> grantTypes, List<String> clientAuthMethods,
             Map<String, String> clientNameLangMap) {
				super(clientId, clientName, publicKey, relyingPartyId, userClaims, authContextRefs,
				logoUri, redirectUris, grantTypes, clientAuthMethods);
				this.clientNameLangMap = clientNameLangMap;
			}

	public CreateClientRequestDtoV2(CreateClientRequestDto createRequest, Map<String, String> clientNameLangMap) {
		super(createRequest.getClientId(), createRequest.getClientName(), createRequest.getPublicKey(), createRequest.getRelyingPartyId(), createRequest.getUserClaims(), 
				createRequest.getAuthContextRefs(), createRequest.getLogoUri(), createRequest.getRedirectUris(), createRequest.getGrantTypes(), createRequest.getClientAuthMethods());
				this.clientNameLangMap = clientNameLangMap;
	}
	
}
