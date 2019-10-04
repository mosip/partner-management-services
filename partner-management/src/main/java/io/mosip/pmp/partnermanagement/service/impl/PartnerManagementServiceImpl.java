package io.mosip.pmp.partnermanagement.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.mosip.pmp.partnermanagement.constant.PartnerAPIKeyDoesNotExistConstant;
import io.mosip.pmp.partnermanagement.constant.PolicyNotExistConstant;
import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingRequest;
import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingResponse;
import io.mosip.pmp.partnermanagement.entity.PartnerPolicy;
import io.mosip.pmp.partnermanagement.exception.PartnerAPIKeyDoesNotExistException;
import io.mosip.pmp.partnermanagement.exception.PolicyNotExistException;
import io.mosip.pmp.partnermanagement.repository.PartnerPolicyRepository;
import io.mosip.pmp.partnermanagement.service.PartnerManagementService;

/**
 * @author sanjeev.shrivastava
 *
 */

@Service
public class PartnerManagementServiceImpl implements PartnerManagementService{

	@Autowired
	PartnerPolicyRepository partnerPolicyRepository;
	
	/* (non-Javadoc)
	 * @see io.mosip.pmp.partnermanagement.service.PartnerManagementService#partnerApiKeyPolicyMappings(io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingRequest, java.lang.String, java.lang.String)
	 */
	
	@Override
	public PartnersPolicyMappingResponse partnerApiKeyPolicyMappings(PartnersPolicyMappingRequest request,
			String partnerID, String partnerAPIKey) {
		
		Optional<PartnerPolicy> findById = partnerPolicyRepository.findById(partnerAPIKey);
		PartnerPolicy partnerPolicy=null;
		if(findById.isPresent() && findById!=null) {
			partnerPolicy = findById.get();
			if(request.getOldPolicyID().equals(partnerPolicy.getPolicy_id())){
				partnerPolicy.setIs_active("Approved");
				partnerPolicy.setPolicy_id(request.getNewPolicyID());
				partnerPolicyRepository.save(partnerPolicy);
			}else {
				throw new PolicyNotExistException(
						PartnerAPIKeyDoesNotExistConstant.PARTNER_API_KEY_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
						PartnerAPIKeyDoesNotExistConstant.PARTNER_API_KEY_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
			}
		}else {
			throw new PartnerAPIKeyDoesNotExistException(
					PolicyNotExistConstant.POLICY_NOT_EXIST_EXCEPTION.getErrorCode(),
					PolicyNotExistConstant.POLICY_NOT_EXIST_EXCEPTION.getErrorMessage()); 
		}
		PartnersPolicyMappingResponse partnersPolicyMappingResponse=new PartnersPolicyMappingResponse();
		partnersPolicyMappingResponse.setMessage("Partner api key to Policy Mappings updated successfully");
		return partnersPolicyMappingResponse;
	}
}
