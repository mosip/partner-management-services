package io.mosip.pmp.authdevice.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import lombok.Data;
@Data
public class IdDto implements Serializable{
	private static final long serialVersionUID = 1L;

	
	private String id;


	public IdDto() {
		super();
		// TODO Auto-generated constructor stub
	}


	public IdDto(String id) {
		super();
		this.id = id;
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}
}
