package io.mosip.pmp.partner.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.pmp.partner.constant.PartnerDoesNotExistExceptionConstant;
import io.mosip.pmp.partner.constant.PartnerIdExceptionConstant;
import io.mosip.pmp.partner.dto.APIkeyRequests;
import io.mosip.pmp.partner.dto.DownloadPartnerAPIkeyResponse;
import io.mosip.pmp.partner.dto.PartnerAPIKeyRequest;
import io.mosip.pmp.partner.dto.PartnerAPIKeyResponse;
import io.mosip.pmp.partner.dto.PartnerRequest;
import io.mosip.pmp.partner.dto.PartnerResponse;
import io.mosip.pmp.partner.dto.PartnerUpdateRequest;
import io.mosip.pmp.partner.dto.PartnersRetrieveApiKeyRequests;
import io.mosip.pmp.partner.dto.RetrievePartnerDetailsResponse;
import io.mosip.pmp.partner.entity.Partner;
import io.mosip.pmp.partner.entity.PartnerPolicy;
import io.mosip.pmp.partner.entity.PartnerPolicyRequest;
import io.mosip.pmp.partner.entity.PolicyGroup;
import io.mosip.pmp.partner.exception.PartnerAlreadyRegisteredException;
import io.mosip.pmp.partner.exception.PartnerDoesNotExistException;
import io.mosip.pmp.partner.repository.PartnerPolicyRepository;
import io.mosip.pmp.partner.repository.PartnerPolicyRequestRepository;
import io.mosip.pmp.partner.repository.PartnerRepository;
import io.mosip.pmp.partner.repository.PolicyGroupRepository;
import io.mosip.pmp.partner.service.PartnerService;
import io.mosip.pmp.partner.util.PartnerUtil;

/**
 * @author sanjeev.shrivastava
 *
 */

@Service
@Transactional
public class PartnerServiceImpl implements PartnerService {
	
	@Autowired
	PartnerRepository partnerRepository;

	@Autowired
	PolicyGroupRepository policyGroupRepository;

	@Autowired
	PartnerPolicyRepository partnerPolicyRepository;
	
	@Autowired
	PartnerPolicyRequestRepository partnerPolicyRequestRepository;

	/*Save Partner which wants to self register*/
	
	@Override
	public PartnerResponse savePartner(PartnerRequest request) {
		Partner partner = new Partner();
		partner.setId(PartnerUtil.createPartnerId());
		PolicyGroup policyGroup = null;
		policyGroup = policyGroupRepository.findByName(request.getPolicyGroup());
		
		
		if(policyGroup!=null) {
			partner.setPolicy_group_id(policyGroup.getId());
			partner.setName(request.getOrganizationName());
			partner.setAddress(request.getAddress());
			partner.setContact_no(request.getContactNumber());
			partner.setEmail_id(request.getEmailId());
			
			partner.setIs_active(policyGroup.getIs_active());
		}else {
			throw new RuntimeException();
		}
		
		PartnerResponse partnerResponse = new PartnerResponse();
		List<Partner> list = partnerRepository.findByName(partner.getName());
		
		if(list.isEmpty()) {
			partnerRepository.save(partner);
			partnerResponse.setPartnerID(partner.getId());
			partnerResponse.setStatus(partner.getIs_active());
			return partnerResponse;
		}else{
			throw new PartnerAlreadyRegisteredException(
					PartnerIdExceptionConstant.PARTNER_ALREADY_REGISTERED_EXCEPTION.getErrorCode(),
					PartnerIdExceptionConstant.PARTNER_ALREADY_REGISTERED_EXCEPTION.getErrorMessage());
		}
	}
	

	/*Get Partner Details as per given Partner ID*/
	
	@Override
	public RetrievePartnerDetailsResponse getPartnerDetails(String partnerID) {
		RetrievePartnerDetailsResponse response = new RetrievePartnerDetailsResponse();
		Optional<Partner> findById = partnerRepository.findById(partnerID);
		Partner partner = null;
		Optional<PolicyGroup> findById2 = null;
		PolicyGroup policyGroup = null;
		
		if(findById.isPresent() && findById!=null) {
			partner = findById.get();
			response.setPartnerID(partner.getId());
			response.setAddress(partner.getAddress());
			response.setContactNumber(partner.getContact_no());
			response.setEmailId(partner.getEmail_id());
			response.setOrganizationName(partner.getName());
			
			findById2 = policyGroupRepository.findById(partner.getPolicy_group_id());
			policyGroup = findById2.get();
			response.setPolicyGroup(policyGroup.getName());
			return response;
		}else {
			throw new PartnerDoesNotExistException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
	}

	/*Updating Partner Details as per given Partner ID and Partner Details*/
	
	@Override
	public PartnerResponse updatePartnerDetail(PartnerUpdateRequest request, String partnerID) {
		Optional<Partner> findById = partnerRepository.findById(partnerID);
		Partner partner = null;
		
		if (findById.isPresent() && findById!=null) {
			partner = findById.get();
			partner.setAddress(request.getAddress());
			partner.setContact_no(request.getContactNumber());
			partner.setEmail_id(request.getEmailId());
			partner.setName(request.getOrganizationName());
			partnerRepository.save(partner);
			PartnerResponse partnerResponse = new PartnerResponse();
			partnerResponse.setPartnerID(partner.getId());
			partnerResponse.setStatus(partner.getIs_active());
			return partnerResponse;
		} else {
			throw new PartnerDoesNotExistException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
	}

	/*API used to submit Partner api key request
	 * Need to take 1.Partner_Policy_Request Table and 2.Policy Group Table
	 * 
	 * */
	
	@Override
	public PartnerAPIKeyResponse submitPartnerApiKeyReq(PartnerAPIKeyRequest request, String partnerID) {
		PolicyGroup policyGroup = policyGroupRepository.findByName(request.getPolicyName());
		
		
		PartnerPolicyRequest partnerPolicyRequest = new PartnerPolicyRequest();
		String Partner_Policy_Request_Id = PartnerUtil.createPartnerId();
		partnerPolicyRequest.setId(Partner_Policy_Request_Id);
		partnerPolicyRequest.setPart_id(partnerID);
		
		policyGroup.setDescr(request.getUseCaseDescription());
		policyGroupRepository.save(policyGroup);
		partnerPolicyRequestRepository.save(partnerPolicyRequest);
		PartnerAPIKeyResponse partnerAPIKeyResponse = new PartnerAPIKeyResponse();
		
		
		partnerAPIKeyResponse.setApiRequestId(partnerPolicyRequest.getId());
		partnerAPIKeyResponse.setMessage("partnerAPIKeyRequest successfully created");
		return partnerAPIKeyResponse;
	}

	@Override
	public DownloadPartnerAPIkeyResponse downloadPartnerAPIkey(String partnerID, String aPIKeyReqID) {
		Optional<PartnerPolicy> findById = partnerPolicyRepository.findById(aPIKeyReqID);
		DownloadPartnerAPIkeyResponse downloadPartnerAPIkeyResponse = new DownloadPartnerAPIkeyResponse();
		PartnerPolicy partnerPolicy = findById.get();
		if(partnerID.equals(partnerPolicy.getPart_id())){
			downloadPartnerAPIkeyResponse.setPartnerAPIKey(aPIKeyReqID);
		}
		return downloadPartnerAPIkeyResponse;
	}

	@Override
	public PartnersRetrieveApiKeyRequests retrieveAllApiKeyRequestsSubmittedByPartner(String partnerID) {
		PartnerPolicy partnerPolicy=null;
		PartnersRetrieveApiKeyRequests requests=new PartnersRetrieveApiKeyRequests();
		List<APIkeyRequests> listAPIkeyRequests=new ArrayList<APIkeyRequests>();
		List<PartnerPolicy> findAll = partnerPolicyRepository.findAll();
		//PartnerPolicyRequest findByPartnerId = partnerPolicyRequestRepository.findByPartnerId(partnerID);
		for (int i = 0; i < findAll.size(); i++) {
			partnerPolicy=findAll.get(i);
			APIkeyRequests  apIkeyRequests= new APIkeyRequests();
			if(partnerPolicy.getPart_id().equals(partnerID)) {
				//apIkeyRequests.setApiKeyReqID(findByPartnerId.getId());
				//apIkeyRequests.setApiKeyReqID(partnerPolicy.getPolicy_api_key());
				apIkeyRequests.setApiKeyRequestStatus(partnerPolicy.getIs_active());
				apIkeyRequests.setPartnerApiKey(partnerPolicy.getPolicy_api_key());
				apIkeyRequests.setValidityTill(partnerPolicy.getValid_to_datetime());
				listAPIkeyRequests.add(apIkeyRequests);
			}
			else {
				apIkeyRequests.setApiKeyReqID(partnerPolicy.getPolicy_api_key());
				apIkeyRequests.setApiKeyRequestStatus(partnerPolicy.getIs_active());
				listAPIkeyRequests.add(apIkeyRequests);
			}
		}
		requests.setAPIkeyRequests(listAPIkeyRequests);
		return requests;
	}


	@Override
	public APIkeyRequests viewApiKeyRequestStatusApiKey(String partnerID, String aPIKeyReqID) {
		// TODO Auto-generated method stub
		System.out.println("@@@@@@@@@@@@@@@@@ Need to implement after clarification @@@@@@@@@@@@@@@@");
		return null;
	}
}
