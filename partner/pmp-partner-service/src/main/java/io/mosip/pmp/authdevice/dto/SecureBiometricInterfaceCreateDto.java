package io.mosip.pmp.authdevice.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
@Data
public class SecureBiometricInterfaceCreateDto {
	
	@NotNull
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "softBinaryHash", required = true, dataType = "java.lang.String")
	private String swBinaryHash;
	
	@NotNull
	@Size(min = 1, max = 64)
	@ApiModelProperty(value = "softwareVersion", required = true, dataType = "java.lang.String")
	private String swVersion;
	
	@NotNull
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "deviceDetailId", required = true, dataType = "java.lang.String")
	private String deviceDetailId;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime swCreateDateTime;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime swExpiryDateTime;
	
	@NotNull
	private Boolean isItForRegistrationDevice;


	public Boolean getIsItForRegistrationDevice() {
		return isItForRegistrationDevice;
	}

	public void setIsItForRegistrationDevice(Boolean isItForRegistrationDevice) {
		this.isItForRegistrationDevice = isItForRegistrationDevice;
	}

	public String getSwBinaryHash() {
		return swBinaryHash;
	}

	public void setSwBinaryHash(String swBinaryHash) {
		this.swBinaryHash = swBinaryHash;
	}

	public String getSwVersion() {
		return swVersion;
	}

	public void setSwVersion(String swVersion) {
		this.swVersion = swVersion;
	}

	public String getDeviceDetailId() {
		return deviceDetailId;
	}

	public void setDeviceDetailId(String deviceDetailId) {
		this.deviceDetailId = deviceDetailId;
	}

	public LocalDateTime getSwCreateDateTime() {
		return swCreateDateTime;
	}

	public void setSwCreateDateTime(LocalDateTime swCreateDateTime) {
		this.swCreateDateTime = swCreateDateTime;
	}

	public LocalDateTime getSwExpiryDateTime() {
		return swExpiryDateTime;
	}

	public void setSwExpiryDateTime(LocalDateTime swExpiryDateTime) {
		this.swExpiryDateTime = swExpiryDateTime;
	}

	
}
