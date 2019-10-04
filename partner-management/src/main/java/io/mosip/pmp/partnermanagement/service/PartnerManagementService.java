package io.mosip.pmp.partnermanagement.service;

import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingRequest;
import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingResponse;

/**
 * @author sanjeev.shrivastava
 *
 */

public interface PartnerManagementService {

	public PartnersPolicyMappingResponse partnerApiKeyPolicyMappings(PartnersPolicyMappingRequest request,
			String partnerID, String partnerAPIKey);
}
