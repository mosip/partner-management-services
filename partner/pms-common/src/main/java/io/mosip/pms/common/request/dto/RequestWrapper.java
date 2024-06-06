package io.mosip.pms.common.request.dto;

import java.time.LocalDateTime;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;


/**
 * @author sanjeev.shrivastava
 *
 * @param <T> any type of object as a parameter
 */
@Data
public class RequestWrapper<T> {
	private String id;
	private String version;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime requesttime;

	private Object metadata;

	@NotNull
	@Valid
	private T request;
}
