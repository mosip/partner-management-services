package io.mosip.pms.device.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DigitalId {

	/** The serial no. */
	@NotBlank
	@Size(min = 1, max = 64)
	@ApiModelProperty(value = "serialNumber", required = true, dataType = "java.lang.String")
	private String serialNo;

	/** The Device Provider Name. */
	@NotBlank
	@Size(min = 1, max = 128)
	@ApiModelProperty(value = "providerName", required = true, dataType = "java.lang.String")
	private String deviceProvider;

	/** The Device Provider id. */
	@NotBlank
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "providerId", required = true, dataType = "java.lang.String")
	private String deviceProviderId;

	/** The make. */
	@NotBlank
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "make", required = true, dataType = "java.lang.String")
	private String make;

	/** The model. */
	@NotBlank
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "model", required = true, dataType = "java.lang.String")
	private String model;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime dateTime;

	/**
	 * Field for deviceTypeCode
	 */
	@NotBlank
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "type", required = true, dataType = "java.lang.String")
	private String type;

	/**
	 * Field for deviceSubTypeCode
	 */
	@NotBlank
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "deviceSubType", required = true, dataType = "java.lang.String")
	private String deviceSubType;

}

