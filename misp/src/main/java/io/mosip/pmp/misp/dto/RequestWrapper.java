package io.mosip.pmp.misp.dto;

import java.time.LocalDateTime;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * @author Nagarjuna Kuchi
 * @version 1.0
 * 
 * Defines an object to hold the request details.
 *
 */

@Data
public class RequestWrapper<T> {
	public String id;
	
	public String version;

	@ApiModelProperty(notes = "Request Timestamp", example = "2018-12-10T06:12:52.994Z", required = true)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	public LocalDateTime requesttime;

	public Object metadata;

	@NotNull
	@Valid
	public T request;
}