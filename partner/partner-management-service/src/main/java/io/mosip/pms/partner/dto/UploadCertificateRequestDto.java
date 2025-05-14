package io.mosip.pms.partner.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CSR-Request model
 * 
 * @author Nagarjuna
 *
 * @since 1.0.10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing a Uploading CA signed Certificate Request")
public class UploadCertificateRequestDto {

    	
	/**
	 * Application Id For Generating KeyPair
	 */
	@ApiModelProperty(notes = "Application ID", example = "KERNEL", required = true)
	@NotBlank(message = "applicationId cannot be null")
	private String applicationId;
	
	/**
	 * Reference Id For Generating KeyPair
	 */
	@ApiModelProperty(notes = "Reference ID", example = "", required = false)
    private String referenceId;

	/**
	 * Certificate Data
	 */
	@ApiModelProperty(notes = "X509 PEM Encoded Certificate", example = "", required = true)
	@NotBlank(message = "certificateData cannot be blank")
	private String certificateData;

}
