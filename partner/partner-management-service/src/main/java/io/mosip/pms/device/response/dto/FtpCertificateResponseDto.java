package io.mosip.pms.device.response.dto;

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
public class FtpCertificateResponseDto {
    
    /**
	 * Field for certificate
	 */
	private String signedCertificateData;
	
	/**
	 * Field for certificateId
	 */
    private String certificateId;
    
    /**
	 * Field for Response time
	 */
	private LocalDateTime timestamp;

}