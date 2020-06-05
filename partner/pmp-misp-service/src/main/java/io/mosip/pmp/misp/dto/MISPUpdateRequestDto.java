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
 * Defines an object to hold the misp update request details.
 *
 */

@Data
@ApiModel(value = "MISPUpdateRequestDto", description = "MISP Status update request representation")
public class MISPUpdateRequestDto {
	
	@Size(min = 1, max = 36)
	@ApiModelProperty(value ="mispID", required = false, dataType = "java.lang.String")	
	public String mispID;	

	
	@NotBlank
	@Size(min = 1, max = 128)
	@ApiModelProperty(value ="name", required = false, dataType = "java.lang.String")	
	public String name;	
	

	@Size(min = 0, max = 16)
	@ApiModelProperty(value= "contactNumber", required = false, dataType = "java.lang.String")
	public String contactNumber;
	
	
	@Size(min = 1, max = 256)
	@ApiModelProperty(value = "emailID", required = false, dataType = "java.lang.String")
	public String emailId;
	
	
	@Size(min = 1, max= 2000)
	@ApiModelProperty(value = "address", required = false, dataType = "java.lang.String")
	public String address;
	
	@ApiModelProperty(value = "status_code", required = false, dataType = "java.lang.String")
	public String status_code;
}
