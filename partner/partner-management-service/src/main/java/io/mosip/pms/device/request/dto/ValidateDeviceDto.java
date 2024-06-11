package io.mosip.pms.device.request.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Class ValidateDeviceDto.
 * 
 * @author Nagarjuna
 * @since 1.2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateDeviceDto {

	/** The device code. */
	@NotBlank
	private String deviceCode;

	/** The digital id. */
	@Valid
	private DigitalIdDto digitalId;

	/** The device service version. */
	@NotBlank
	private String deviceServiceVersion;
	

	@ApiParam(value = "2019-09-09T09:09:09.000Z", required = true)
	private String timeStamp;
	


	private String purpose;
}
