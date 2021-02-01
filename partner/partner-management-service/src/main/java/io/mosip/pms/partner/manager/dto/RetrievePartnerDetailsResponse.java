package io.mosip.pms.partner.manager.dto;

import java.util.List;

import lombok.Data;

/**
 * @author sanjeev.shrivastava
 *
 */

@Data
public class RetrievePartnerDetailsResponse {
	
	private List<RetrievePartnersDetails> partners; 
}
