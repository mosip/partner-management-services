package io.mosip.pmp.partner.core;

import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.mosip.pmp.partner.exception.ErrorResponse;
import lombok.Data;


/**
 * @author sanjeev.shrivastava
 *
 * @param <T>
 */

@Data
public class ResponseWrapper<T> {
	private String id;
	private String version;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime responsetime = LocalDateTime.now(ZoneId.of("UTC"));
	private Object metadata;
	@NotNull
	@Valid
	private T response;

	private ErrorResponse errors;

}
