package io.mosip.pms.partner.manager.dto;

import java.util.List;

import lombok.Data;

@Data
public class Policies {

	/** The list of auth type attributes. */
	private List<AuthPolicyAttributes> authPolicies;
	
	/** The list of KYC attributes. */
	private List<KYCAttributes> allowedKycAttributes;
	
	/** */
	private String authTokenType;
}
