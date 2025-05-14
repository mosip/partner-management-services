package io.mosip.pms.partner.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class PartnerUpdateDto {

	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 2000, message = "Length should be between 1 and 2000 chars")
	public String address;
	
	@NotBlank(message="value is empty or null")
	@Size(min = 1, max = 16, message = "Length should be between 1 and 16 chars")
	public String contactNumber;
	
	@ApiModelProperty(name = "Logo url", example = "https://logo.com")
	public String logoUrl;
	
	@ApiModelProperty(name = "additional Info", example = "{\"redirectUrl\":\"https://example.io\",\"statusUrl\":\"https://status.com\"}")
	public JsonNode additionalInfo;
}
