/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.pms.oauth.client.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ClientDetail {

    private String id;
    private String name;
    private String policyId;
    private String policyName;
    private String relyingPartyId;
    private String logoUri;
    private List<String> redirectUris;
    private String publicKey;
    private List<String> claims;
    private List<String> acrValues;
    private String status;
    private List<String> grantTypes;
    private List<String> clientAuthMethods;
}
