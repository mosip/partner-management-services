package io.mosip.pmp.misp.dto;

import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 
 * @author Nagarjuna Kuchi
 * @version 1.0
 * 
 * Defines an object to hold the misp license key validate response details.
 *
 */

@Data
@ApiModel(value = "MISPValidatelKeyResponseDto", description= "MISP lecense key validation response representation")
public class MISPValidatelKeyResponseDto {
	
	@ApiModelProperty(value ="message", required = false, dataType = "java.lang.String")
	public String message;
	
	@ApiModelProperty(value="misp_id", required = false)
	public String misp_id;
	
	@ApiModelProperty(value="isActive", required = false)
	public boolean isActive;
	
	@ApiModelProperty(value="isValid", required = false)
	public boolean isValid;
	
	@ApiModelProperty(value="licenseKey", required = false)
	public String licenseKey;
	
	@ApiModelProperty(value="validFrom", required = false)
	public LocalDateTime validFrom;

	@ApiModelProperty(value="validTo", required = false)
	public LocalDateTime validTo;

}
