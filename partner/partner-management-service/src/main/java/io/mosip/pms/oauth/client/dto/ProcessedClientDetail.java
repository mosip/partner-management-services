package io.mosip.pms.oauth.client.dto;

import io.mosip.pms.common.entity.AuthPolicy;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.entity.ClientDetail;
import lombok.Data;

@Data
public class ProcessedClientDetail {
	
	private ClientDetail clientDetail;
	private Partner partner;
	private AuthPolicy policy;
	
}
