package io.mosip.pms.policy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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

	public String policyId;
	
	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 128, message = "Length should be in between 1 and 128 chars")
	public String policyGroupName;

	@ApiModelProperty(notes = "policy type is used to differebtiate policies.", required = true, example = "Auth/DataShare/CredentialIssuance")
	@NotBlank(message = "value is empty or null. Allowed values Auth/DataShare/CredentialIssuance")
	@Size(min = 1, max = 128, message = "Length should be in between 1 and 128 chars")
	public String policyType;
	
	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 128, message = "Length should be in between 1 and 128 chars")
	public String name;
	
	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 128, message = "Length should be in between 1 and 128 chars")	
	public String desc;	
	
	@NotBlank(message="value is empty or null")
	public String version;
	
	public JSONObject policies;
}
