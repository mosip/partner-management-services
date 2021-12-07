package io.mosip.pms.device.request.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class DeviceDetailSBIMappingDto {

	@NotBlank(message="value is empty or null")
	@NotNull(message="value is empty or null")
	private String deviceDetailId;

	@NotBlank(message="value is empty or null")
	@NotNull(message="value is empty or null")
	private String sbiId;
}
