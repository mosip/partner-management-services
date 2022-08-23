package io.mosip.pms.partner.request.dto;

import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class PartnerRequestDto extends PartnerRequest{

	@ApiModelProperty(name = "Logo url", example = "https://logo.com")
	public String logoUrl;
	
	@ApiModelProperty(name = "additional Info", example = "{\"redirectUrl\":\"https://example.io\",\"statusUrl\":\"https://status.com\"}")
	public JsonNode additionalInfo;

}
