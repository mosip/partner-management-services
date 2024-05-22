package io.mosip.pms.common.response.dto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.mosip.pms.common.request.dto.ErrorResponse;
import lombok.Data;


/**
 * @author sanjeev.shrivastava
 *
 * @param <T> any type of object as a parameter
 */

@Data
public class ValidateResponseWrapper<T> {
	private String id;
	private String version;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private LocalDateTime responsetime = LocalDateTime.now(ZoneId.of("UTC"));
	private Object metadata;
	@NotNull
	@Valid
	private T response;

	private List<ErrorResponse> errors = new ArrayList<>();

}
