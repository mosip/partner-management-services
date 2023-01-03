package io.mosip.pms.partner.manager.dto;

import java.util.List;

import lombok.Data;

@Data
public class PartnerDetailsResponse {
	
	private List<PartnerDetailsDto> partners;
}
