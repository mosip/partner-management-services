package io.mosip.pms.device.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.mosip.pms.device.validator.ValidCertificateLevel;
import lombok.Data;

@Data
public class DeviceInfo {

	private List<String> deviceSubId;

	@ValidCertificateLevel(message = "Invalid Certification level received")
	private String certification;

	private String digitalId;

	@NotNull
	@Size(min = 1, max = 128)
	private String firmware;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime deviceExpiry;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime timestamp;

}
