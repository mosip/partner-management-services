package io.mosip.pmp.policy.dto;

import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * <p> It holds the objects of auth policy {@link AuthPolicyDto} and </p>
 * <p> allowed kyc {@link AllowedKycDto} </p>
 *  
 * @author Nagarjuna Kuchi
 * @version 1.0
 */
@Data
public class PolicyDto {

	/**
	 * policy Id
	 */
	private String policyId;
	
	/**
	 * policy name
	 */
	@NotNull
	private String name;
	
	/**
	 * policy description.
	 */
	@NotNull
	private String descr;
	
	/**
	 * list of auth policy dto's.
	 */
	private List<AuthPolicyDto> authPolicies;
	
	/**
	 * list of allowed Kyc dto's
	 */
	private List<AllowedKycDto> allowedKycAttributes;
}
