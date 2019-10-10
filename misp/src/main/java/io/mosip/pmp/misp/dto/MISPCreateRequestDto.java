package io.mosip.pmp.misp.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value ="MISPCreateRequest" ,description = "MISP create request representation")
public class MISPCreateRequestDto {
	
	//@NotBlank
	@Size(min = 1, max = 36)
	@ApiModelProperty(value ="organizationName", required = true, dataType = "java.lang.String")	
	private String organizationName;	
	

	@NotBlank
	@NotNull
	@Size(min = 0, max = 16)
	@ApiModelProperty(value= "contactNumber", required = true, dataType = "java.lang.String")
	private String contactNumber;
	
	@NotBlank
	@NotNull
	@Size(min = 1, max = 256)
	@ApiModelProperty(value = "emailId", required = true, dataType = "java.lang.String")
	private String emailId;
	
	@NotBlank
	@NotNull
	@Size(min = 1, max= 2000)
	@ApiModelProperty(value = "address", required = true, dataType = "java.lang.String")
	private String address;
	
	@NotBlank
	@NotNull
	@Size(min = 1, max= 2000)
	@ApiModelProperty(value = "name", required = true, dataType = "java.lang.String")
	private String name;

}
