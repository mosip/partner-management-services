package io.mosip.pmp.authdevice.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
public class FTPChipCertificateRequestDto {
    
	/**
	 * ftp provider Id
	 */
	@NotBlank
	@NotNull
	String ftpProviderId;
	
	/**
	 * ftp chip id
	 */
	@NotBlank
	@NotNull
	String ftpChipDeatilId;	
	
	@NotNull
	Boolean isItForRegistrationDevice;
    /**
	 * Certificate Data of Partner.
	 */
	@ApiModelProperty(notes = "X509 Certificate Data", required = true)
	@NotBlank
	String certificateData;
	
	/**
	 * Certificate Data of Partner.
	 */
	@ApiModelProperty(notes = "Organization Name", required = true)
	@NotBlank
    String organizationName;    
    
	@NotBlank
	@NotNull
	String partnerDomain;
}