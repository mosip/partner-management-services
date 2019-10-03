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
import io.mosip.pmp.partner.dto.PartnersRetrieveApiKeyRequests;
import io.mosip.pmp.partner.dto.RetrievePartnerDetailsResponse;
import io.mosip.pmp.partner.entity.Partner;
import io.mosip.pmp.partner.entity.PartnerPolicy;
import io.mosip.pmp.partner.entity.PolicyGroup;
import io.mosip.pmp.partner.exception.PartnerAlreadyRegisteredException;
import io.mosip.pmp.partner.exception.PartnerDoesNotExistException;
import io.mosip.pmp.partner.repository.PartnerPolicyRepository;
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

	/* (non-Javadoc)
	 * @see io.mosip.pmp.partner.service.PartnerService#savePartner(io.mosip.pmp.partner.dto.PartnerRequest)
	 */
	@Override
	public PartnerResponse savePartner(PartnerRequest request) {
		Partner partner = new Partner();
		partner.setId(PartnerUtil.createPartnerId());
		partner.setPolicy_group_id(request.getPolicyGroup());
		partner.setName(request.getOrganizationName());
		partner.setAddress(request.getAddress());
		partner.setContact_no(request.getContactNumber());
		partner.setEmail_id(request.getEmailId());
		partner.setIs_active("Active");
		
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

	/* (non-Javadoc)
	 * @see io.mosip.pmp.partner.service.PartnerService#getPartnerDetails(java.lang.String)
	 */
	@Override
	public RetrievePartnerDetailsResponse getPartnerDetails(String partnerID) {
		RetrievePartnerDetailsResponse response = new RetrievePartnerDetailsResponse();
		Optional<Partner> findById = partnerRepository.findById(partnerID);
		Partner partner = null;
		
		if(findById.isPresent() && findById!=null) {
			partner = findById.get();
			response.setPartnerID(partner.getId());
			response.setAddress(partner.getAddress());
			response.setContactNumber(partner.getContact_no());
			response.setEmailId(partner.getEmail_id());
			response.setOrganizationName(partner.getName());
			response.setPolicyGroup(partner.getPolicy_group_id());
			return response;
		}else {
			throw new PartnerDoesNotExistException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
	}

	/* (non-Javadoc)
	 * @see io.mosip.pmp.partner.service.PartnerService#updatePartnerDetail(io.mosip.pmp.partner.dto.PartnerRequest, java.lang.String)
	 */
	@Override
	public PartnerResponse updatePartnerDetail(PartnerRequest request, String partnerID) {
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

	/* (non-Javadoc)
	 * @see io.mosip.pmp.partner.service.PartnerService#submitPartnerApiKeyReq(io.mosip.pmp.partner.dto.PartnerAPIKeyRequest, java.lang.String)
	 */
	@Override
	public PartnerAPIKeyResponse submitPartnerApiKeyReq(PartnerAPIKeyRequest request, String partnerID) {
		Optional<Partner> findById = partnerRepository.findById(partnerID);
		PolicyGroup findByName = policyGroupRepository.findByName(request.getPolicyName());
		PartnerAPIKeyResponse partnerAPIKeyResponse = new PartnerAPIKeyResponse();

		PartnerPolicy partnerPolicy = new PartnerPolicy();

		if (findById.isPresent() && findByName != null) {
			partnerAPIKeyResponse.setApiRequestId("873276828663");
			partnerAPIKeyResponse.setMessage("partnerAPIKeyRequest successfully created");
			partnerPolicy.setPolicy_api_key(partnerAPIKeyResponse.getApiRequestId());
			partnerPolicy.setPart_id(partnerID);
			partnerPolicyRepository.save(partnerPolicy);
		}
		return partnerAPIKeyResponse;
	}

	/* (non-Javadoc)
	 * @see io.mosip.pmp.partner.service.PartnerService#downloadPartnerAPIkey(java.lang.String, java.lang.String)
	 */
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

	/* (non-Javadoc)
	 * @see io.mosip.pmp.partner.service.PartnerService#retrieveAllApiKeyRequestsSubmittedByPartner(java.lang.String)
	 */
	@Override
	public PartnersRetrieveApiKeyRequests retrieveAllApiKeyRequestsSubmittedByPartner(String partnerID) {
		PartnerPolicy partnerPolicy=null;
		PartnersRetrieveApiKeyRequests requests=new PartnersRetrieveApiKeyRequests();
		List<APIkeyRequests> listAPIkeyRequests=new ArrayList<APIkeyRequests>();
		List<PartnerPolicy> findAll = partnerPolicyRepository.findAll();
		for (int i = 0; i < findAll.size(); i++) {
			partnerPolicy=findAll.get(i);
			APIkeyRequests  apIkeyRequests= new APIkeyRequests();
			if(partnerPolicy.getPart_id().equals(partnerID) && partnerPolicy.getIs_active().equals("approved")) {
				apIkeyRequests.setApiKeyReqID(partnerPolicy.getPolicy_api_key());
				apIkeyRequests.setApiKeyRequestStatus(partnerPolicy.getIs_active());
				apIkeyRequests.setPartnerApiKey(partnerPolicy.getPolicy_api_key());
				apIkeyRequests.setValidityTill(partnerPolicy.getValid_from_datetime());
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

	/* (non-Javadoc)
	 * @see io.mosip.pmp.partner.service.PartnerService#viewApiKeyRequestStatusApiKey(java.lang.String, java.lang.String)
	 */
	@Override
	public APIkeyRequests viewApiKeyRequestStatusApiKey(String partnerID, String aPIKeyReqID) {
		// TODO Auto-generated method stub
		
		return null;
	}
}
