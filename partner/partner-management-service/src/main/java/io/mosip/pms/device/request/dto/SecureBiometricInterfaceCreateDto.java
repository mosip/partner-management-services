package io.mosip.pms.device.request.dto;

import java.time.LocalDateTime;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
@Data
public class SecureBiometricInterfaceCreateDto {
	
	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 36, message = "Length should be in between 1 and 36 chars")
	@ApiModelProperty(value = "softBinaryHash", required = true, dataType = "java.lang.String")
	private String swBinaryHash;
	
	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 64, message = "Length should be in between 1 and 64 chars")
	@ApiModelProperty(value = "softwareVersion", required = true, dataType = "java.lang.String")
	private String swVersion;
	
	@NotNull(message="value is empty or null")
	@NotEmpty(message="value is empty or null")
	@Size(min = 1, max = 200000, message = "Length should be in between 1 and 200000 chars")
	@ApiModelProperty(value = "deviceDetailId", required = true, dataType = "java.lang.String")
	private List<String> deviceDetailIds;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime swCreateDateTime;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime swExpiryDateTime;
	
	@NotNull(message="value is empty or null")
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

	public List<String> getDeviceDetailIds() {
		return deviceDetailIds;
	}

	public void setDeviceDetailIds(List<String> deviceDetailIds) {
		this.deviceDetailIds = deviceDetailIds;
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
