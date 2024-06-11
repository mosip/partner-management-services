package io.mosip.pms.device.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
