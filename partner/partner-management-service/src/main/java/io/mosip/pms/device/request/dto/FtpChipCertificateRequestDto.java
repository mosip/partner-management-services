package io.mosip.pms.device.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ftp Chip Certificates Request DTO.
 * 
 * @author Nagarjuna
 * @since 1.2.0
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing request to upload ftp chip certificates.")
public class FtpChipCertificateRequestDto {
    
	/**
	 * ftp provider Id
	 */
	@NotBlank(message="value is empty or null")
	String ftpProviderId;
	
	/**
	 * ftp chip id
	 */
	@NotBlank(message="value is empty or null")
	String ftpChipDeatilId;	
	
	@NotNull(message="value is empty or null")
	Boolean isItForRegistrationDevice;
    /**
	 * Certificate Data of Partner.
	 */
	@ApiModelProperty(notes = "X509 Certificate Data", required = true)
	@NotBlank(message="value is empty or null")
	String certificateData;
	
	/**
	 * Certificate Data of Partner.
	 */
	@ApiModelProperty(notes = "Organization Name", required = true)
	@NotBlank(message="value is empty or null")
    String organizationName;    
    
	@NotBlank(message="value is empty or null")
	String partnerDomain;
}