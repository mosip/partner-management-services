package io.mosip.pmp.misp.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * @author Nagarjuna Kuchi
 * @version 1.0
 * 
 * Defines an object to hold the misp license key validate response details.
 *
 */

@Data
@ApiModel(value = "MISPValidatelKeyResponseDto", description= "MISP lecense key validation response representation")
public class MISPValidatelKeyResponseDto {
	
	@ApiModelProperty(value ="message", required = false, dataType = "java.lang.String")
	public String message;

}
