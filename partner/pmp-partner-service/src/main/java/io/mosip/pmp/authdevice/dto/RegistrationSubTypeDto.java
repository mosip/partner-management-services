package io.mosip.pmp.authdevice.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class RegistrationSubTypeDto {

	private String code;

	private String deviceType;

	private String name;

	private String desciption;

	private boolean isActive;

	private Boolean isDeleted;

	private String crBy;

	private LocalDateTime crDtimes;

	private LocalDateTime delDtimes;

	private String updBy;

	private LocalDateTime updDtimes;

}
