package io.mosip.pmp.authdevice.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;

import org.springframework.stereotype.Component;

import lombok.Data;
@Data
public class DeRegisterDevicePostDto {
	@NotBlank
	private String device;

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}
}
