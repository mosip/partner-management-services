package io.mosip.pmp.authdevice.dto;

import lombok.Data;

@Data
public class ValidatorResponseDto {
	/**
	 * The validation request status.
	 */
	private String status;
	/**
	 * The validation request message.
	 */
	private String message;
}