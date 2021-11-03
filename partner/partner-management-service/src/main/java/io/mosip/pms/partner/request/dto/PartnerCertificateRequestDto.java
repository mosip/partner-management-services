package io.mosip.pms.partner.request.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Partner Certificates Request DTO.
 * 
 * @author Nagarjuna
 * @since 1.2.0
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing request to upload Partner certificates.")
public class PartnerCertificateRequestDto {
    
	/**
	 * 
	 */
	@NotBlank
	@NotNull
	String partnerId;
	
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
    
    /**
	 * Partner Type.
	 */
	@ApiModelProperty(notes = "Partner Type", required = true)
	@NotBlank
	String partnerType;
	
	@NotBlank
	@NotNull
	String partnerDomain;
}