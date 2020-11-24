package io.mosip.pmp.misp.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * @author Nagarjuna Kuchi
 * @version 1.0
 * @since 2019-Oct-11
 * 
 * Defines an object to provide client request information to controller.
 */
@Data
@ApiModel(value ="MISPCreateRequest" ,description = "MISP create request representation")
public class MISPCreateRequestDto {
	
    /**
     *  organizationName carries misp organiization name<br/>
     *  Ex : Banking, Insurance, telecom <br/>  
     */	
	@NotNull
	@NotBlank
	@Size(min = 1, max = 128)
	@ApiModelProperty(value ="organizationName", required = true, dataType = "java.lang.String")	
	public String organizationName;	
	
	/**
	 *  contactNumber carries mobile number of the misp.
	 */

	@NotBlank
	@NotNull
	@Size(min = 0, max = 16)
	@ApiModelProperty(value= "contactNumber", required = true, dataType = "java.lang.String")
	public String contactNumber;
	
	/**
	 * emailId carries the mail id of the misp.
	 */
	@NotBlank
	@NotNull
	@Size(min = 1, max = 256)
	@ApiModelProperty(value = "emailId", required = true, dataType = "java.lang.String")
	public String emailId;
	
	/**
	 * address carries the location details of the misp.
	 */
	@NotBlank
	@NotNull
	@Size(min = 1, max= 2000)
	@ApiModelProperty(value = "address", required = true, dataType = "java.lang.String")
	public String address;
}
