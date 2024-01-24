package io.mosip.pms.oauth.client.dto;

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
public class ClientDetailCreateRequestV2 extends ClientDetailCreateRequest{

	 @NotEmpty
	 private Map<@Size(min=3, max=3) String, @NotBlank String> clientNameLangMap;
	   
	 public ClientDetailCreateRequestV2(String name, String policyId, Map<String, Object> publicKey, String authPartnerId,
			 String logoUri, List<String> redirectUris, List<String> grantTypes, List<String> clientAuthMethods,
             Map<String, String> clientNameLangMap) {
				super(name, policyId, publicKey, authPartnerId, logoUri, redirectUris,
				grantTypes, clientAuthMethods);
				this.clientNameLangMap = clientNameLangMap;
			}
	 
	}
