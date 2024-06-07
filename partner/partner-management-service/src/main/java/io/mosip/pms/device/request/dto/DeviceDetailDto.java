package io.mosip.pms.device.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DeviceDetailDto {	
	
	@Size(min = 1, max = 36, message = "Length should be in between 1 and 36 chars")
	@ApiModelProperty(value = "id", required = true, dataType = "java.lang.String")
	private String id;

	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 36, message = "Length should be in between 1 and 36 chars")
	@ApiModelProperty(value = "deviceProviderId", required = true, dataType = "java.lang.String")
	private String deviceProviderId;

	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 36, message = "Length should be in between 1 and 36 chars")
	@ApiModelProperty(value = "deviceTypeCode", required = true, dataType = "java.lang.String")
	private String deviceTypeCode;

	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 36, message = "Length should be in between 1 and 36 chars")
	@ApiModelProperty(value = "deviceSubTypeCode", required = true, dataType = "java.lang.String")
	private String deviceSubTypeCode;


	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 36, message = "Length should be in between 1 and 36 chars")
	@ApiModelProperty(value = "make", required = true, dataType = "java.lang.String")
	private String make;


	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 36, message = "Length should be in between 1 and 36 chars")
	@ApiModelProperty(value = "model", required = true, dataType = "java.lang.String")
	private String model;	
}
