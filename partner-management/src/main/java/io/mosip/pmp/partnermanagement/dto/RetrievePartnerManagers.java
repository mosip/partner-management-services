package io.mosip.pmp.partnermanagement.dto;

import java.util.List;

import lombok.Data;

/**
 * @author sanjeev.shrivastava
 *
 */

@Data
public class RetrievePartnerManagers {
	
	public List<RetrievePartnersManagersDetails> partners; 
}
