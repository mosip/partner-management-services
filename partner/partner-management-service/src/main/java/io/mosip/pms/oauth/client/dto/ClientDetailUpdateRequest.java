/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
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
public class ClientDetailUpdateRequest {

    @NotNull
    @NotBlank
    @URL
    @Size( max = 2048)
    private String logoUri;

    @NotNull
    @Size(min = 1, max = 5)
    private List<@NotBlank String> redirectUris;

    @NotNull
    @NotBlank
    @Pattern(regexp = "(ACTIVE)|(INACTIVE)", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String status;

    @NotNull
    @Size(min = 1, max = 3)
    private List<String> grantTypes;

    @NotNull
    @NotBlank
    private String clientName;

    @NotNull
    @Size(min = 1, max = 3)
    private List<String> clientAuthMethods;
}
