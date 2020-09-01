package io.mosip.pmp.policy.dto;

import java.util.List;

import lombok.Data;

/**
 * <p> It holds the objects of auth policy {@link AuthPolicyDto} and </p>
 * <p> allowed kyc {@link AllowedKycDto} </p>
 *  
 * @author Nagarjuna Kuchi
 * @version 1.0
 */
@Data
public class PolicyAttributesDto {
	
	/**
	 * list of auth policy dto's.
	 */
	private List<AuthPolicyDto> allowedAuthTypes;
	
	/**
	 * list of allowed Kyc dto's
	 */
	private List<AllowedKycDto> shareableAttributes;
	
	/**
	 *  Data share policies
	 */
	private DataShareDto dataSharePolicies;
	
	/**
	 * auth token type
	 */
	private String authTokenType;
}
