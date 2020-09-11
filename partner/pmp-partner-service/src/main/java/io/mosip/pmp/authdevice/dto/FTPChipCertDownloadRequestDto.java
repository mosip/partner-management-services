package io.mosip.pmp.authdevice.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ftp chip Certificate Download Request DTO.
 * 
 * @author Nagarjuna 
 * @since 1.2.0
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing request to download ftp chip certificates.")
public class FTPChipCertDownloadRequestDto {
    
    /**
	 * Certificate ID of Partner.
	 */
	@ApiModelProperty(notes = "ftpChipDetailId", required = true)
	@NotBlank
	String ftpChipDetailId;
	
	@NotNull
	Boolean isItForRegistrationDevice;
}