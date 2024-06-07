package io.mosip.pms.policy.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

/**
 * <p> Defines an object to provide required authentication types .</p>
 *  
 *  <p>
 *  Note: While creating auth policies authPolicies should contain parameters names as defined below.</br>
 *  authType, authSubType, mandatory.</br>
 *  ex: {"authType":"demo","authSubType":null,"mandatory":false} </br>
 *  </p>
 *  
 * @author Nagarjuna Kuchi
 * @version 1.0
 *
 */
@Data
public class AuthPolicyDto {

	@NotBlank(message = "authType should not be empty.")
	private String authType;
	
	private String authSubType;
	
	@NotBlank(message = "mandatory should contain 'true' or 'false' values")
	private boolean mandatory;
}
