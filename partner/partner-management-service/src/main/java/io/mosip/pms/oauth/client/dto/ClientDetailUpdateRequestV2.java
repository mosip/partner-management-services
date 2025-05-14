/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.pms.oauth.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientDetailUpdateRequestV2 extends ClientDetailUpdateRequest{

	
	@NotEmpty
	private Map<@Size(min=3, max=3) String, @NotBlank String> clientNameLangMap;

	public ClientDetailUpdateRequestV2(String logoUri, List<String> redirectUris, String status, List<String> grantTypes, String clientName, List<String> clientAuthMethods,
             Map<String, String> clientNameLangMap) {
				super(logoUri, redirectUris, status, grantTypes, clientName, clientAuthMethods);
				this.clientNameLangMap = clientNameLangMap;
			}
}
