package io.mosip.pms.oauth.client.dto;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateClientRequestDtoV2 extends UpdateClientRequestDto{

	@NotEmpty
	private Map<@Size(min=3, max=3) String, @NotBlank String> clientNameLangMap;

	public UpdateClientRequestDtoV2(String logoUri, @NotNull List<@NotBlank String> redirectUris, String status,
			@NotNull List<String> grantTypes, String clientName, List<String> userClaims, List<String> authContextRefs,
			@NotNull List<String> clientAuthMethods, Map<String, String> clientNameLangMap) {
		super(logoUri, redirectUris, status, grantTypes, clientName, userClaims, authContextRefs, clientAuthMethods);
		this.clientNameLangMap = clientNameLangMap;
	}

	public UpdateClientRequestDtoV2(UpdateClientRequestDto updateRequest, Map<String, String> clientNameLangMap) {
		super(updateRequest.getLogoUri(), updateRequest.getRedirectUris(), updateRequest.getStatus(), updateRequest.getGrantTypes(), updateRequest.getClientName(), 
				updateRequest.getUserClaims(), updateRequest.getAuthContextRefs(), updateRequest.getClientAuthMethods());
		this.clientNameLangMap = clientNameLangMap;
	}
	
	
}
