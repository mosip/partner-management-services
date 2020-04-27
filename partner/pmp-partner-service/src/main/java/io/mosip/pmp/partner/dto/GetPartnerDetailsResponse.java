package io.mosip.pmp.partner.dto;

import java.util.List;

import lombok.Data;

@Data
public class GetPartnerDetailsResponse {
	
	private List<PartnersDetails> partners;
}
