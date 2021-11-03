package io.mosip.pms.device.request.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
	
	@NotNull
	private Boolean isItForRegistrationDevice;


	public Boolean getIsItForRegistrationDevice() {
		return isItForRegistrationDevice;
	}

	public void setIsItForRegistrationDevice(Boolean isItForRegistrationDevice) {
		this.isItForRegistrationDevice = isItForRegistrationDevice;
	}
}
