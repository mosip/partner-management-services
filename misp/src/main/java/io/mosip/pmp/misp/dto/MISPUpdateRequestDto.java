package io.mosip.pmp.misp.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "MISPUpdateRequestDto", description = "MISP Status update request representation")
public class MISPUpdateRequestDto {
	
	@Size(min = 1, max = 36)
	@ApiModelProperty(value ="mispID", required = false, dataType = "java.lang.String")	
	private String mispID;	

	
	@NotBlank
	@Size(min = 1, max = 36)
	@ApiModelProperty(value ="name", required = false, dataType = "java.lang.String")	
	private String name;	
	

	@Size(min = 0, max = 16)
	@ApiModelProperty(value= "contactNumber", required = false, dataType = "java.lang.String")
	private String contactNumber;
	
	
	@Size(min = 1, max = 256)
	@ApiModelProperty(value = "emailID", required = false, dataType = "java.lang.String")
	private String emailID;
	
	
	@Size(min = 1, max= 2000)
	@ApiModelProperty(value = "address", required = false, dataType = "java.lang.String")
	private String address;


}
