package io.mosip.pms.device.request.dto;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Partner Certificate Download Request DTO.
 * 
 * @author Nagarjuna 
 * @since 1.2.0
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing request to download partner certificates.")
public class FtpCertDownloadRequestDto {
    
    /**
	 * Certificate ID of Partner.
	 */
	@ApiModelProperty(notes = "Partner ID", required = true)
	@NotBlank
	String partnerId;
}