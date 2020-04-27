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
 * Defines an object to hold the misp license key validate request details.
 *
 */

@Data
@ApiModel(value = "MISPValidatelKeyRequestDto", description = "MISP license key request representation")
public class MISPValidatelKeyRequestDto {

	@Size(min =1, max = 128)
	@ApiModelProperty(value ="mispLicenseKey", required = false, dataType = "java.lang.String")
	public String mispLicenseKey;
}
