package io.mosip.pms.device.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class RegisteredDevicePostDto {

	@NotNull
	@NotBlank
	private String deviceData;

	public String getDeviceData() {
		return deviceData;
	}

	public void setDeviceData(String deviceData) {
		this.deviceData = deviceData;
	}
}
