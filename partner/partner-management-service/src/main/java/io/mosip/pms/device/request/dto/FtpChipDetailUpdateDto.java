package io.mosip.pms.device.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
@Data
public class FtpChipDetailUpdateDto {
	
	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 36, message = "Length should be in between 1 and 36 chars")
	@ApiModelProperty(value = "ftpChipDetailId", required = true, dataType = "java.lang.String")
	private String ftpChipDetailId;

	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 36, message = "Length should be in between 1 and 36 chars")
	@ApiModelProperty(value = "make", required = true, dataType = "java.lang.String")
	private String make;

	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 36, message = "Length should be in between 1 and 36 chars")
	@ApiModelProperty(value = "model", required = true, dataType = "java.lang.String")
	private String model;	
}
