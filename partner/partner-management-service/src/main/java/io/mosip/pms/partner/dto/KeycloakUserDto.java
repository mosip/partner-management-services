package io.mosip.pms.partner.dto;

import lombok.Data;

/**
 * DTO is used for fetching user details from Keycloak.
 */
@Data
public class KeycloakUserDto {

    private String username;
    private String email;
    private String firstName;
    private String lastName;

}
