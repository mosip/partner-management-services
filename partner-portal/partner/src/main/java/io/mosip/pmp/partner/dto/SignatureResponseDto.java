package io.mosip.pmp.partner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignatureResponseDto {
	/**
	 * Data Encrypted/Decrypted in BASE64 encoding
	 */
	private String data;
	
}
