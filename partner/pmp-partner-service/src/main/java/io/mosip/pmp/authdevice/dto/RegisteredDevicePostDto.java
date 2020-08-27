package io.mosip.pmp.authdevice.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;

import org.springframework.stereotype.Component;

import lombok.Data;
@Data
public class RegisteredDevicePostDto {
	@NotBlank
	private String deviceData;

	public String getDeviceData() {
		return deviceData;
	}

	public void setDeviceData(String deviceData) {
		this.deviceData = deviceData;
	}
}
