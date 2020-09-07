package io.mosip.pmp.partner.dto;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CA/Sub-CA Certificate Request DTO.
 * 
 * @author Nagarjuna 
 * @since 1.2.0
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing request to upload CA/Sub-CA certificates.")
public class CACertificateRequestDto {
    
    /**
	 * Certificate Data of CA or Sub-CA.
	 */
	@ApiModelProperty(notes = "X509 Certificate Data", required = true)
	@NotBlank
	String certificateData;
}