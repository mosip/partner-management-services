package io.mosip.pms.device.response.dto;

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
public class FtpCertDownloadResponeDto {
    
    /**
	 * Partner Certificate Data.
	 */
	private String certificateData;

	/**
	 * Response timestamp.
	 */
	private LocalDateTime timestamp;
}