package io.mosip.pmp.partner.dto;

import java.util.List;

import lombok.Data;

@Data
public class GetPartnerDetailsResponse {
	
	public List<PartnersDetails> partners;
}
