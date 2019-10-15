package io.mosip.pmp.misp.dto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.mosip.pmp.misp.exception.ServiceError;
import lombok.Data;

/**
 * 
 * @author Nagarjuna Kuchi
 * @version 1.0
 * 
 * Defines an object to hold the response details.
 *
 */

@Data
public class ResponseWrapper<T> {
	
	public String id;
	
	public String version;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	public LocalDateTime responsetime = LocalDateTime.now(ZoneId.of("UTC"));
	
	public Object metadata;
	
	@NotNull
	@Valid
	public T response;	
	
	public List<ServiceError> errors = new ArrayList<>();

}
