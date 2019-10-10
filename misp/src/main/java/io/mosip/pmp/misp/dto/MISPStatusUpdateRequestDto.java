package io.mosip.pmp.misp.dto;

import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "MISPStatusUpdateRequestDto" , description = "MISP Status update request representation")
public class MISPStatusUpdateRequestDto {

	@Size(min= 1, max = 5)
	@ApiModelProperty(value ="mispStatus", required = true, dataType = "java.lang.String")
	private String mispStatus;
	
	private String mispId;
}
