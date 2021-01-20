package io.mosip.pmp.authdevice.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DeviceDetailUpdateDto {
	@NotNull
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "id", required = true, dataType = "java.lang.String")
	private String id;


	@NotNull
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "deviceProviderId", required = true, dataType = "java.lang.String")
	private String deviceProviderId;

	@NotNull
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "deviceTypeCode", required = true, dataType = "java.lang.String")
	private String deviceTypeCode;

	@NotNull
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "deviceSubTypeCode", required = true, dataType = "java.lang.String")
	private String deviceSubTypeCode;


	@NotNull
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "make", required = true, dataType = "java.lang.String")
	private String make;


	@NotNull
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "model", required = true, dataType = "java.lang.String")
	private String model;

	@Size(min = 0, max = 128)
	@ApiModelProperty(value = "partnerOrganizationName", required = true, dataType = "java.lang.String")
	private String partnerOrganizationName;
	
	@NotNull
	private Boolean isItForRegistrationDevice;
}
