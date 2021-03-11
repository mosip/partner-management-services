package io.mosip.pmp.authdevice.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author Nagarjuna 
 * @since 1.2.0
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JWTSignatureResponseDto {

	/**
	 * encrypted data
	 */
	private String jwtSignedData;

	/**
	 * response time.
	 */
	private LocalDateTime timestamp;
}
