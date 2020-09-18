package io.mosip.pmp.authdevice.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data

@AllArgsConstructor
@NoArgsConstructor

public class DeviceSearchDto {

	
	private String id;
	
	private String name;
	
	private String serialNum;
	
	private String deviceSpecId;
	
	private String macAddress;
	
	private String ipAddress;
	
	private String langCode;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime validityDateTime;

	private String zoneCode;

	private String zone;

	private String deviceTypeName;

	private String mapStatus;
	
	
	
	private Boolean isActive;

	
	private String createdBy;

	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime createdDateTime;

	
	private String updatedBy;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime updatedDateTime;

	
	private Boolean isDeleted;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime deletedDateTime;

}
