package io.mosip.pms.policy.dto;

import java.time.LocalDateTime;
import java.time.ZoneId;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RequestWrapper<T> {
	private String id;
	private String version;
	@ApiModelProperty(notes = "Request Timestamp", example = "2018-12-10T06:12:52.994Z", required = true)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime requesttime = LocalDateTime.now(ZoneId.of("UTC"));

	private Object metadata;

	@NotNull
	@Valid
	private T request;
}