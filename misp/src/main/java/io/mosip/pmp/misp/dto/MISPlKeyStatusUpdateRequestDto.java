package io.mosip.pmp.misp.dto;

import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * @author Nagarjuna Kuchi
 * @version 1.0
 * 
 * Defines an object to hold the misp licese key update request details.
 *
 */
@Data
@ApiModel(value = "MISPlKeyStatusUpdateResponse", description = "MISP license key request representation")
public class MISPlKeyStatusUpdateRequestDto {
	
	public String mispId;
	
	@Size(min = 1, max = 36)
	@ApiModelProperty(value ="mispStatus", required = true, dataType = "java.lang.String")	
	public String mispStatus;

	@Size(min = 1, max = 128)
	@ApiModelProperty(value ="mispLicenseKey", required = true, dataType = "java.lang.String")	
	public String mispLicenseKey;
	
	@Size(min = 1, max = 128)
	@ApiModelProperty(value ="mispLicenseKeyStatus", required = true, dataType = "java.lang.String")	
	public String mispLicenseKeyStatus;
}
