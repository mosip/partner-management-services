package io.mosip.pmp.partnermanagement.dto;

import lombok.Data;

@Data
public class AuthPolicyAttributes{

	/**  authentication type used. */
	private String authType;
	
	/**  authentication subType used. */
	private String authSubType;
	
	/**  mandatory attribute value */
	private boolean mandatory;
}
