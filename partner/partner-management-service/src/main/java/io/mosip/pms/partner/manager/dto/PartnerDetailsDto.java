package io.mosip.pms.partner.manager.dto;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PartnerDetailsDto extends RetrievePartnersDetails {

	public String logoUrl;
	
	public JsonNode additionalInfo;
}
