package io.mosip.pms.partner.request.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * @author sanjeev.shrivastava
 *
 */

@Data
public class PartnerAPIKeyRequest {
	
		@NotNull
		public String policyName;
		@NotNull
		public String useCaseDescription;
}
