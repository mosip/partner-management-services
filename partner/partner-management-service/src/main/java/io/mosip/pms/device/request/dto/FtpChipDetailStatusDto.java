package io.mosip.pms.device.request.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class FtpChipDetailStatusDto {
	
	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 36, message = "Length should be in between 1 and 36 chars")
	@ApiModelProperty(value = "ftpChipDetailId", required = true, dataType = "java.lang.String")
	private String ftpChipDetailId;
	
	@NotNull(message="value is empty or null")		
	private Boolean approvalStatus;		
}
