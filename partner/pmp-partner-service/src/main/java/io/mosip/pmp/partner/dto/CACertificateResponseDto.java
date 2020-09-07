package io.mosip.pmp.partner.dto;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * DTO class for upload certificate response.
 * 
 * @author Nagarjuna 
 * @since 1.2.0
 *
 */
@Data
public class CACertificateResponseDto {
    
    /**
	 * Status of upload certificate.
	 */
	private String status;

	/**
	 * Response timestamp.
	 */
	private LocalDateTime timestamp;
}