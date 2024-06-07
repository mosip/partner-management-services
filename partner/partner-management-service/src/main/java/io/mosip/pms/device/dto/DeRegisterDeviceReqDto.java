package io.mosip.pms.device.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;
@Data
public class DeRegisterDeviceReqDto {
	@NotNull
	@Size(min=1,max=36)
	private String deviceCode;
	@NotNull
	private String env;
	public String getDeviceCode() {
		return deviceCode;
	}
	public void setDeviceCode(String deviceCode) {
		this.deviceCode = deviceCode;
	}
	public String getEnv() {
		return env;
	}
	public void setEnv(String env) {
		this.env = env;
	}
}
