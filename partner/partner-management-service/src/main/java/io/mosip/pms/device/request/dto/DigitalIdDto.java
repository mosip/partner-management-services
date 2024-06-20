package io.mosip.pms.device.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Innner Json for DigitalId
 * 
 * @author Srinivasan
 * @author Megha Tanga
 * @since 1.0.0
 *
 */
@Data
public class DigitalIdDto {

	/** The serial no. */

	@NotNull
	@ApiModelProperty(value = "serialNo", required = true, dataType = "java.lang.String")
	private String serialNo;

	/** The Device Provider Name. */
	@NotNull
	@ApiModelProperty(value = "dp", required = true, dataType = "java.lang.String")
	private String dp;

	@NotNull
	@ApiModelProperty(value = "dpId", required = true, dataType = "java.lang.String")
	private String dpId;

	/** The make. */

	@NotNull
	@ApiModelProperty(value = "make", required = true, dataType = "java.lang.String")
	private String make;

	/** The model. */

	@NotNull
	@ApiModelProperty(value = "mpdel", required = true, dataType = "java.lang.String")
	private String model;

	@NotBlank
	@ApiModelProperty(value = "deviceSubType", dataType = "java.lang.String")
	private String deviceSubType;

	@NotBlank
	@ApiModelProperty(value = "type", dataType = "java.lang.String")
	private String type;

	/** The date time. */

	private String dateTime;

}

