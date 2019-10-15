package io.mosip.pmp.policy.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(value= "PolicyUpdateRequestDto", description = " Policy Update Request Representation")
public class PolicyUpdateRequestDto {

	private String Id;
	
	private String name;
	
	private String desc;
	
	private PolicyDto policies;

}
