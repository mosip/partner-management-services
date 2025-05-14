package io.mosip.pms.common.dto;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * DTO class for download of partner certificate response.
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