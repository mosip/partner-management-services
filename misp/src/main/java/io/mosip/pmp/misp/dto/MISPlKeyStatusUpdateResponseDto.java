package io.mosip.pmp.misp.dto;

import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value= "MISPlKeyStatusUpdateResponseDto", description = "MISP license key response representation")
public class MISPlKeyStatusUpdateResponseDto {

	@Size(min = 1, max = 5)
	@ApiModelProperty(value ="mispLicenseKeyStatus", required = true, dataType = "java.lang.String")	
	private String mispLicenseKeyStatus;

}
