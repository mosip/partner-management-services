package io.mosip.pmp.misp.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "MISPValidatelKeyResponseDto", description= "MISP lecense key validation response representation")
public class MISPValidatelKeyResponseDto {
	
	@ApiModelProperty(value ="message", required = false, dataType = "java.lang.String")
	private String message;

}
