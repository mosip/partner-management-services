package io.mosip.pmp.policy.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(value= "PolicyStatusUpdateRequestDto", description = " Policy Status Update Request Representation")
public class PolicyStatusUpdateRequestDto {
	
	private String Id;
	
	private String Status;

}
