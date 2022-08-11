package io.mosip.pms.ida.dto;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * DTO class for download of partner certificate response.
 * 
 * @author Nagarjuna 
 * @since 1.2.0
 *
 */
@Data
public class PartnerCertDownloadResponeDto {
    
    /**
	 * Partner Certificate Data.
	 */
	private String certificateData;

	/**
	 * Response timestamp.
	 */
	private LocalDateTime timestamp;
}