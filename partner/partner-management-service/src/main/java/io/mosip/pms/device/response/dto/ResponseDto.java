package io.mosip.pms.device.response.dto;

import lombok.Data;

@Data
public class ResponseDto {
	private String status;

	/** The message. */
	private String message;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
