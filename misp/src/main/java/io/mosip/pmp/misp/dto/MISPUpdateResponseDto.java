package io.mosip.pmp.misp.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * @author Nagarjuna Kuchi
 * @version 1.0
 * 
 * Defines an object to hold the misp update response details.
 *
 */

@Data
@ApiModel(value ="MISPUpdateResponseDto", description="MISP Update response representation")
public class MISPUpdateResponseDto {

	@NotBlank
	@Size(min = 1, max = 36)
	@ApiModelProperty(value ="organizationName", required = false, dataType = "java.lang.String")	
	private String organizationName;	
	

	@Size(min = 0, max = 16)
	@ApiModelProperty(value= "contactNumber", required = false, dataType = "java.lang.String")
	private String contactNumber;
	
	@NotBlank
	@Size(min = 1, max = 256)
	@ApiModelProperty(value = "emailID", required = false, dataType = "java.lang.String")
	private String emailID;
	
	@NotBlank
	@Size(min = 1, max= 2000)
	@ApiModelProperty(value = "address", required = false, dataType = "java.lang.String")
	private String address;
	
	private String mispID;
	
	private String name;

}

