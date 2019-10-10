package io.mosip.pmp.partnermanagement.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.mosip.pmp.partnermanagement.constant.NoPartnerApiKeyRequestsConstant;
import io.mosip.pmp.partnermanagement.constant.PartnerAPIDoesNotExistConstant;
import io.mosip.pmp.partnermanagement.constant.PartnerAPIKeyDoesNotExistConstant;
import io.mosip.pmp.partnermanagement.constant.PartnerApiKeyDoesNotBelongToThePolicyGroupOfThePartnerMangerConstant;
import io.mosip.pmp.partnermanagement.constant.PartnerDoesNotExistExceptionConstant;
import io.mosip.pmp.partnermanagement.constant.PartnerIdDoesNotExistExceptionConstant;
import io.mosip.pmp.partnermanagement.constant.PolicyNotExistConstant;
import io.mosip.pmp.partnermanagement.dto.ActivateDeactivatePartnerRequest;
import io.mosip.pmp.partnermanagement.dto.ApikeyRequests;
import io.mosip.pmp.partnermanagement.dto.Partner;
import io.mosip.pmp.partnermanagement.dto.PartnerAPIKeyToPolicyMappingsResponse;
import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingRequest;
import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingResponse;
import io.mosip.pmp.partnermanagement.dto.RetrievePartnerDetailsResponse;
import io.mosip.pmp.partnermanagement.dto.RetrievePartnersDetails;
import io.mosip.pmp.partnermanagement.entity.PartnerPolicy;
import io.mosip.pmp.partnermanagement.exception.NoPartnerApiKeyRequestsException;
import io.mosip.pmp.partnermanagement.exception.PartnerAPIDoesNotExistException;
import io.mosip.pmp.partnermanagement.exception.PartnerAPIKeyDoesNotExistException;
import io.mosip.pmp.partnermanagement.exception.PartnerApiKeyDoesNotBelongToThePolicyGroupOfThePartnerMangerException;
import io.mosip.pmp.partnermanagement.exception.PartnerDoesNotExistException;
import io.mosip.pmp.partnermanagement.exception.PartnerIdDoesNotExistException;
import io.mosip.pmp.partnermanagement.exception.PolicyNotExistException;
import io.mosip.pmp.partnermanagement.repository.PartnerPolicyRepository;
import io.mosip.pmp.partnermanagement.repository.PartnerRepository;
import io.mosip.pmp.partnermanagement.service.PartnerManagementService;

/**
 * @author sanjeev.shrivastava
 *
 */

@Service
public class PartnerManagementServiceImpl implements PartnerManagementService {

	@Autowired
	PartnerPolicyRepository partnerPolicyRepository;

	@Autowired
	PartnerRepository partnerRepository;

	@Override
	public PartnersPolicyMappingResponse partnerApiKeyPolicyMappings(PartnersPolicyMappingRequest request,
			String partnerID, String partnerAPIKey) {

		Optional<PartnerPolicy> findById = partnerPolicyRepository.findById(partnerAPIKey);
		PartnerPolicy partnerPolicy = null;
		if (findById.isPresent() && findById != null) {
			partnerPolicy = findById.get();
			if (request.getOldPolicyID().equals(partnerPolicy.getPolicy_id())) {
				partnerPolicy.setIs_active("Approved");
				partnerPolicy.setPolicy_id(request.getNewPolicyID());
				partnerPolicyRepository.save(partnerPolicy);
			} else {
				throw new PolicyNotExistException(PolicyNotExistConstant.POLICY_NOT_EXIST_EXCEPTION.getErrorCode(),
						PolicyNotExistConstant.POLICY_NOT_EXIST_EXCEPTION.getErrorMessage());
			}
		} else {
			throw new PartnerAPIKeyDoesNotExistException(
					PartnerAPIKeyDoesNotExistConstant.PARTNER_API_KEY_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerAPIKeyDoesNotExistConstant.PARTNER_API_KEY_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		PartnersPolicyMappingResponse partnersPolicyMappingResponse = new PartnersPolicyMappingResponse();
		partnersPolicyMappingResponse.setMessage("Partner api key to Policy Mappings updated successfully");
		return partnersPolicyMappingResponse;
	}

	@Override
	public PartnersPolicyMappingResponse activateDeactivateAuthEKYCPartner(String partnerID,
			ActivateDeactivatePartnerRequest request) {
		PartnersPolicyMappingResponse partnersPolicyMappingResponse = new PartnersPolicyMappingResponse();
		Optional<Partner> findById = partnerRepository.findById(partnerID);
		if (findById.isPresent() && findById != null) {
			Partner partner = findById.get();
			partner.setIs_active(request.getStatus());
			partnerRepository.save(partner);
		} else {
			throw new PartnerIdDoesNotExistException(
					PartnerIdDoesNotExistExceptionConstant.PARTNER_ID_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerIdDoesNotExistExceptionConstant.PARTNER_ID_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		partnersPolicyMappingResponse.setMessage("Partner status updated successfully");
		return partnersPolicyMappingResponse;
	}

	@Override
	public PartnersPolicyMappingResponse activateDeactivatePartnerAPIKeyGivenPartner(String partnerID,
			ActivateDeactivatePartnerRequest request, String partnerAPIKey) {
		Optional<PartnerPolicy> findById = partnerPolicyRepository.findById(partnerAPIKey);

		if (findById.isPresent() && findById != null) {
			PartnerPolicy partnerPolicy = findById.get();
			if (partnerPolicy.getPart_id().equals(partnerID)) {
				partnerPolicy.setIs_active(request.getStatus());
				partnerPolicyRepository.save(partnerPolicy);
			} else {
				throw new PartnerDoesNotExistException(
						PartnerIdDoesNotExistExceptionConstant.PARTNER_ID_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
						PartnerIdDoesNotExistExceptionConstant.PARTNER_ID_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
			}
		} else {
			throw new PartnerAPIKeyDoesNotExistException(
					PartnerAPIKeyDoesNotExistConstant.PARTNER_API_KEY_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerAPIKeyDoesNotExistConstant.PARTNER_API_KEY_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		PartnersPolicyMappingResponse partnersPolicyMappingResponse = new PartnersPolicyMappingResponse();
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
		Iterator<Partner> partner_iterat = list_part.iterator();

		while (partner_iterat.hasNext()) {
			RetrievePartnersDetails retrievePartnersDetails = new RetrievePartnersDetails();
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
		if (findById.isPresent() && findById != null) {
			Partner partner = findById.get();

			retrievePartnersDetails.setPartnerID(partner.getId());
			retrievePartnersDetails.setStatus(partner.getIs_active());
			retrievePartnersDetails.setOrganizationName(partner.getName());
			retrievePartnersDetails.setContactNumber(partner.getContact_no());
			retrievePartnersDetails.setEmailId(partner.getEmail_id());
			retrievePartnersDetails.setAddress(partner.getAddress());

		} else {
			throw new PartnerDoesNotExistException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		return retrievePartnersDetails;
	}

	@Override
	public PartnerAPIKeyToPolicyMappingsResponse getPartnerAPIKeyToPolicyMapping(String partnerID,
			String PartnerAPIKey) {
		Optional<PartnerPolicy> findById = partnerPolicyRepository.findById(PartnerAPIKey);
		PartnerAPIKeyToPolicyMappingsResponse response = new PartnerAPIKeyToPolicyMappingsResponse();
		if (findById.isPresent() && findById != null) {
			PartnerPolicy partnerPolicy = findById.get();
			if (partnerPolicy.getPart_id().equals(partnerID)) {
				response.setPartnerID(partnerID);
				response.setPolicyId(partnerPolicy.getPolicy_id());
			} else {
				throw new PartnerApiKeyDoesNotBelongToThePolicyGroupOfThePartnerMangerException(
						PartnerApiKeyDoesNotBelongToThePolicyGroupOfThePartnerMangerConstant.PARTNER_API_DOES_NOT_BELONGS_TO_THE_POLICYGROUP_OF_PARTNERMANAGER_EXCEPTION
								.getErrorCode(),
						PartnerApiKeyDoesNotBelongToThePolicyGroupOfThePartnerMangerConstant.PARTNER_API_DOES_NOT_BELONGS_TO_THE_POLICYGROUP_OF_PARTNERMANAGER_EXCEPTION
								.getErrorMessage());
			}
		} else {
			throw new PartnerAPIDoesNotExistException(
					PartnerAPIDoesNotExistConstant.PARTNER_API_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerAPIDoesNotExistConstant.PARTNER_API_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		return response;
	}

	@Override
	public List<ApikeyRequests> getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagers() {
		
		List<ApikeyRequests> requests = new ArrayList<ApikeyRequests>();
		
		List<Partner> list_part = null;
		list_part = partnerRepository.findAll();
		Partner partner = null;
		
		if(!list_part.isEmpty() && list_part!=null) {
			
			Iterator<Partner> partner_iterat = list_part.iterator();
			while (partner_iterat.hasNext()) {
				ApikeyRequests ApikeyRequests = new ApikeyRequests();
				partner = partner_iterat.next();

				ApikeyRequests.setPartnerID(partner.getId());
				ApikeyRequests.setStatus(partner.getIs_active());
				ApikeyRequests.setOrganizationName(partner.getName());
				ApikeyRequests.setPolicyName(partner.getPolicy_group_id());
				ApikeyRequests.setPolicyDesc("Desc about policy");
				ApikeyRequests.setApiKeyReqNo("903276828609");

				requests.add(ApikeyRequests);
			}
		}else {
			throw new NoPartnerApiKeyRequestsException(
					NoPartnerApiKeyRequestsConstant.NO_PARTNER_API_KEY_REQUEST_EXCEPTION.getErrorCode(),
					NoPartnerApiKeyRequestsConstant.NO_PARTNER_API_KEY_REQUEST_EXCEPTION.getErrorMessage());
		}
		return requests;
	}

	@Override
	public ApikeyRequests getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestId(String APIKeyReqID) {
		ApikeyRequests apikeyRequests = new ApikeyRequests();
		Optional<PartnerPolicy> findById = partnerPolicyRepository.findById(APIKeyReqID);
		Partner partner = null;
		if(findById.isPresent() && findById!=null) {
			PartnerPolicy partnerPolicy = findById.get();
			Optional<Partner> findByPartnerId = partnerRepository.findById(partnerPolicy.getPart_id());
			if(findByPartnerId!=null) {
				partner = findByPartnerId.get();
				
				apikeyRequests.setPartnerID(partner.getId());
				apikeyRequests.setOrganizationName(partner.getName());
				apikeyRequests.setStatus(partner.getIs_active());
				apikeyRequests.setPolicyName("Insurance Policy");
				apikeyRequests.setPolicyDesc("Desc about policy");
				
			}else {
				System.out.println("Need to throw the Exception");
			}
		}else {
			throw new NoPartnerApiKeyRequestsException(
					NoPartnerApiKeyRequestsConstant.NO_PARTNER_API_KEY_REQUEST_EXCEPTION.getErrorCode(),
					NoPartnerApiKeyRequestsConstant.NO_PARTNER_API_KEY_REQUEST_EXCEPTION.getErrorMessage()); 
		}
		return apikeyRequests;
	}
}
