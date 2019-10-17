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
	public String policyId;
	
	/**
	 * policy name
	 */
	@NotNull
	public String name;
	
	/**
	 * policy description.
	 */
	@NotNull
	public String descr;
	
	/**
	 * list of auth policy dto's.
	 */
	public List<AuthPolicyDto> authPolicies;
	
	/**
	 * list of allowed Kyc dto's
	 */
	public List<AllowedKycDto> allowedKycAttributes;
}
