package io.mosip.pmp.partnermanagement.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.mosip.pmp.partnermanagement.constant.PartnerAPIKeyDoesNotExistConstant;
import io.mosip.pmp.partnermanagement.constant.PartnerDoesNotExistExceptionConstant;
import io.mosip.pmp.partnermanagement.constant.PolicyNotExistConstant;
import io.mosip.pmp.partnermanagement.dto.ActivateDeactivatePartnerRequest;
import io.mosip.pmp.partnermanagement.dto.Partner;
import io.mosip.pmp.partnermanagement.dto.PartnerAPIKeyToPolicyMappingsResponse;
import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingRequest;
import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingResponse;
import io.mosip.pmp.partnermanagement.dto.RetrievePartnerDetailsResponse;
import io.mosip.pmp.partnermanagement.dto.RetrievePartnersDetails;
import io.mosip.pmp.partnermanagement.entity.PartnerPolicy;
import io.mosip.pmp.partnermanagement.exception.PartnerAPIKeyDoesNotExistException;
import io.mosip.pmp.partnermanagement.exception.PartnerDoesNotExistException;
import io.mosip.pmp.partnermanagement.exception.PolicyNotExistException;
import io.mosip.pmp.partnermanagement.repository.PartnerPolicyRepository;
import io.mosip.pmp.partnermanagement.repository.PartnerRepository;
import io.mosip.pmp.partnermanagement.service.PartnerManagementService;

/**
 * @author sanjeev.shrivastava
 *
 */

@Service
public class PartnerManagementServiceImpl implements PartnerManagementService{

	@Autowired
	PartnerPolicyRepository partnerPolicyRepository;
	
	@Autowired
	PartnerRepository partnerRepository;
	
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
						PolicyNotExistConstant.POLICY_NOT_EXIST_EXCEPTION.getErrorCode(),
						PolicyNotExistConstant.POLICY_NOT_EXIST_EXCEPTION.getErrorMessage());
			}
		}else {
			throw new PartnerAPIKeyDoesNotExistException(
						PartnerAPIKeyDoesNotExistConstant.PARTNER_API_KEY_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
						PartnerAPIKeyDoesNotExistConstant.PARTNER_API_KEY_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		PartnersPolicyMappingResponse partnersPolicyMappingResponse=new PartnersPolicyMappingResponse();
		partnersPolicyMappingResponse.setMessage("Partner api key to Policy Mappings updated successfully");
		return partnersPolicyMappingResponse;
	}

	@Override
	public PartnersPolicyMappingResponse activateDeactivateAuthEKYCPartner(String partnerID,
			ActivateDeactivatePartnerRequest request) {
		PartnersPolicyMappingResponse partnersPolicyMappingResponse = new PartnersPolicyMappingResponse();
		Optional<Partner> findById = partnerRepository.findById(partnerID);
		if(findById.isPresent() && findById!=null) {
			Partner partner = findById.get();
			partner.setIs_active(request.getStatus());
			partnerRepository.save(partner);
		}else {
			throw new PartnerDoesNotExistException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		partnersPolicyMappingResponse.setMessage("Partner status updated successfully");
		return partnersPolicyMappingResponse;
	}

	@Override
	public PartnersPolicyMappingResponse activateDeactivatePartnerAPIKeyGivenPartner(String partnerID,
			ActivateDeactivatePartnerRequest request, String partnerAPIKey) {
		Optional<PartnerPolicy> findById = partnerPolicyRepository.findById(partnerAPIKey);
		
		if(findById.isPresent() && findById!=null) {
			PartnerPolicy partnerPolicy = findById.get();
			if(partnerPolicy.getPart_id().equals(partnerID)) {
				partnerPolicy.setIs_active(request.getStatus());
				partnerPolicyRepository.save(partnerPolicy);
			}else {
				throw new PartnerDoesNotExistException(
						PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
						PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
			}
		}else {
			throw new PartnerAPIKeyDoesNotExistException(
					PartnerAPIKeyDoesNotExistConstant.PARTNER_API_KEY_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerAPIKeyDoesNotExistConstant.PARTNER_API_KEY_DOES_NOT_EXIST_EXCEPTION.getErrorMessage()); 
		}
		PartnersPolicyMappingResponse partnersPolicyMappingResponse=new PartnersPolicyMappingResponse();
		partnersPolicyMappingResponse.setMessage("Partner API Key status updated successfully");
		return partnersPolicyMappingResponse;
	}

	@Override
	public RetrievePartnerDetailsResponse getAllAuthEKYCPartnersForThePolicyGroup() {
		RetrievePartnerDetailsResponse partnersResponse = new RetrievePartnerDetailsResponse();
		List<RetrievePartnersDetails> partners = new ArrayList<RetrievePartnersDetails>();
		
		List<Partner> list_part = null;
		list_part = partnerRepository.findAll();
		Partner partner = null;
			Iterator<Partner> partner_iterat=list_part.iterator();
			
			while(partner_iterat.hasNext()) {
				RetrievePartnersDetails retrievePartnersDetails=new RetrievePartnersDetails();
				partner = partner_iterat.next();
				
				retrievePartnersDetails.setPartnerID(partner.getId());
				retrievePartnersDetails.setStatus(partner.getIs_active());
				retrievePartnersDetails.setOrganizationName(partner.getName());
				retrievePartnersDetails.setContactNumber(partner.getContact_no());
				retrievePartnersDetails.setEmailId(partner.getEmail_id());
				retrievePartnersDetails.setAddress(partner.getAddress());
				
				partners.add(retrievePartnersDetails);
			}
			partnersResponse.setPartners(partners);
		return partnersResponse;
	}

	@Override
	public RetrievePartnersDetails getparticularAuthEKYCPartnerDetailsForGivenPartnerId(String partnerID) {
		Optional<Partner> findById = partnerRepository.findById(partnerID);
		RetrievePartnersDetails retrievePartnersDetails = new RetrievePartnersDetails();
		if(findById.isPresent() && findById!=null) {
			Partner partner = findById.get();
			
			retrievePartnersDetails.setPartnerID(partner.getId());
			retrievePartnersDetails.setStatus(partner.getIs_active());
			retrievePartnersDetails.setOrganizationName(partner.getName());
			retrievePartnersDetails.setContactNumber(partner.getContact_no());
			retrievePartnersDetails.setEmailId(partner.getEmail_id());
			retrievePartnersDetails.setAddress(partner.getAddress());
			
		}else {
			/*throw new PartnerDoesNotExistException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());*/
		}
		return retrievePartnersDetails;
	}

	@Override
	public PartnerAPIKeyToPolicyMappingsResponse getPartnerAPIKeyToPolicyMapping(String partnerID,
			String PartnerAPIKey) {
		Optional<PartnerPolicy> findById = partnerPolicyRepository.findById(PartnerAPIKey);
		PartnerAPIKeyToPolicyMappingsResponse response = new PartnerAPIKeyToPolicyMappingsResponse();
		if(findById.isPresent() && findById!=null) {
			PartnerPolicy partnerPolicy = findById.get();
			if(partnerPolicy.getPart_id().equals(partnerID)) {
				response.setPartnerID(partnerID);
				response.setPolicyId(partnerPolicy.getPolicy_id());
			}else {
				//TODO
				//throw new Partner api key does not belong to the Policy Group of the Partner Manger (PMS_PMP_009)
			}
		}else {
			//TODO
			//throw new Partner API Key does not exist (PMS_PMP_007)
		}
		return response;
	}
}
