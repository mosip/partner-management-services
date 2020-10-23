package io.mosip.pmp.policy.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.json.simple.JSONObject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p> Defines an object to provide policy details to create policy.</p>
 * @author Nagarjuna Kuchi
 * @version 1.0
 *
 */
@Data
@ApiModel(value= "PolicyRequestDto", description = " Policy Create Request Representation")
public class PolicyCreateRequestDto {	

	@NotBlank(message = "Policy group name should not be empty.")
	@Size(min = 1, max = 128, message = "Policy group name length should be in between 1 and 128 chars")
	@Type(type="string")
	public String policyGroupName;

	@ApiModelProperty(notes = "policy type is used to differebtiate policies.", required = true, example = "Auth/DataShare/CredentialIssuance")
	@NotBlank(message = "Policy type should not be empty. Allowed values Auth/DataShare/CredentialIssuance")
	@Size(min = 1, max = 128, message = "Policy type length should be in between 1 and 128 chars")
	@Type(type="string")
	public String policyType;
	
	@NotBlank(message = "Policy name should not be empty.")
	@Size(min = 1, max = 128, message = "Policy name length should be in between 1 and 128 chars")
	@Type(type="string")
	public String name;
	
	@NotBlank(message = "Policy desc should not be blank.")
	@Size(min = 1, max = 128, message = "Policy desc length should be in between 1 and 128 chars")	
	public String desc;	
	
	@NotBlank(message = "version should not be blank")
	public String version;
	
	public JSONObject policies;
}
