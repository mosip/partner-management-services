package io.mosip.pmp.authdevice.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
	
	@NotNull
	private Boolean isItForRegistrationDevice;


	public Boolean getIsItForRegistrationDevice() {
		return isItForRegistrationDevice;
	}

	public void setIsItForRegistrationDevice(Boolean isItForRegistrationDevice) {
		this.isItForRegistrationDevice = isItForRegistrationDevice;
	}
}
