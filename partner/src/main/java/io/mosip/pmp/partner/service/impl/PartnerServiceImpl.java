package io.mosip.pmp.partner.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.pmp.partner.constant.APIKeyReqIdStatusInProgressConstant;
import io.mosip.pmp.partner.constant.PartnerAPIDoesNotExistConstant;
import io.mosip.pmp.partner.constant.PartnerAPIKeyIsNotCreatedConstant;
import io.mosip.pmp.partner.constant.PartnerDoesNotExistExceptionConstant;
import io.mosip.pmp.partner.constant.PartnerIdExceptionConstant;
import io.mosip.pmp.partner.constant.PolicyGroupDoesNotExistConstant;
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
import io.mosip.pmp.partner.exception.APIKeyReqIdStatusInProgressException;
import io.mosip.pmp.partner.exception.PartnerAPIKeyIsNotCreatedException;
import io.mosip.pmp.partner.exception.PartnerAPIKeyReqIDDoesNotExistException;
import io.mosip.pmp.partner.exception.PartnerAlreadyRegisteredException;
import io.mosip.pmp.partner.exception.PartnerDoesNotExistException;
import io.mosip.pmp.partner.exception.PartnerDoesNotExistsException;
import io.mosip.pmp.partner.exception.PolicyGroupDoesNotExistException;
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
			throw new PolicyGroupDoesNotExistException(
					PolicyGroupDoesNotExistConstant.POLICY_GROUP_DOES_NOT_EXIST.getErrorCode(),
					PolicyGroupDoesNotExistConstant.POLICY_GROUP_DOES_NOT_EXIST.getErrorMessage());
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
			throw new PartnerDoesNotExistsException(
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
		
		
		if(policyGroup==null) {
			//TODO
			System.out.println("Need to through the exception policyGroup not exist");
		}
		
		PartnerPolicyRequest partnerPolicyRequest = new PartnerPolicyRequest();
		String Partner_Policy_Request_Id = PartnerUtil.createPartnerId();
		partnerPolicyRequest.setId(Partner_Policy_Request_Id);
		partnerPolicyRequest.setStatus_code("in-progress");

		Optional<Partner> findById = partnerRepository.findById(partnerID);
		if(!findById.isPresent()) {
			throw new PartnerDoesNotExistsException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		Partner partner = findById.get();
		partnerPolicyRequest.setPolicy_id(partner.getPolicy_group_id());
		partnerPolicyRequest.setPart_id(partnerID);
		//TODO
		// Need add column (api_desc) in Partner_policy_request Table
		partnerPolicyRequest.setDel_dtimes(request.getUseCaseDescription());
		
		partnerPolicyRequestRepository.save(partnerPolicyRequest);
		PartnerAPIKeyResponse partnerAPIKeyResponse = new PartnerAPIKeyResponse();
		
		
		partnerAPIKeyResponse.setApiRequestId(partnerPolicyRequest.getId());
		partnerAPIKeyResponse.setMessage("partnerAPIKeyRequest successfully created");
		return partnerAPIKeyResponse;
	}

	@Override
	public DownloadPartnerAPIkeyResponse downloadPartnerAPIkey(String partnerID, String aPIKeyReqID) {
		PartnerPolicy partnerPolicy = null;
		DownloadPartnerAPIkeyResponse downloadPartnerAPIkeyResponse = new DownloadPartnerAPIkeyResponse();
		Optional<PartnerPolicyRequest> partner_request = partnerPolicyRequestRepository.findById(aPIKeyReqID);
		
		if(partner_request.isPresent() && partner_request!=null) {
			
			PartnerPolicyRequest partnerPolicyRequest = partner_request.get();
			if(partnerPolicyRequest.getPart_id().equals(partnerID)) {
				// && partnerPolicyRequest.getStatus_code().equalsIgnoreCase("approved")
				partnerPolicy = partnerPolicyRepository.findByPartnerId(partnerID);
				if(partnerPolicy!=null) {
					downloadPartnerAPIkeyResponse.setPartnerAPIKey(partnerPolicy.getPolicy_api_key());
				}else {
					//TODO
					System.out.println("throw Partner_API_Key is not created / request is not approved by partnetmanager");
					throw new PartnerAPIKeyIsNotCreatedException(
							PartnerAPIKeyIsNotCreatedConstant.PARTNER_API_NOT_CREATED_EXCEPTION.getErrorCode(),
							PartnerAPIKeyIsNotCreatedConstant.PARTNER_API_NOT_CREATED_EXCEPTION.getErrorMessage());
				}
			}else {
				throw new PartnerDoesNotExistsException(
						PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
						PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
			}
		}else {
			//TODO
			System.out.println("throw APIKeyReqID does not exist (PMS_PRT_005)");
			
			throw new PartnerAPIKeyReqIDDoesNotExistException(
					PartnerAPIDoesNotExistConstant.PARTNER_API_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerAPIDoesNotExistConstant.PARTNER_API_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		return downloadPartnerAPIkeyResponse;
	}

	@Override
	public PartnersRetrieveApiKeyRequests retrieveAllApiKeyRequestsSubmittedByPartner(String partnerID) {

		List<PartnerPolicyRequest> findByPartnerId = partnerPolicyRequestRepository.findByPartnerId(partnerID);
		PartnersRetrieveApiKeyRequests response = new PartnersRetrieveApiKeyRequests();
		List<APIkeyRequests> listAPIkeyRequests = new ArrayList<APIkeyRequests>();
		PartnerPolicyRequest partnerPolicyRequest = null;
		if (!findByPartnerId.isEmpty() && findByPartnerId != null) {

			Iterator<PartnerPolicyRequest> it = findByPartnerId.iterator();
			while (it.hasNext()) {
				partnerPolicyRequest = it.next();
				if (partnerPolicyRequest.getStatus_code().equalsIgnoreCase("approved")) {

					APIkeyRequests approvedRequest = new APIkeyRequests();
					approvedRequest.setApiKeyReqID(partnerPolicyRequest.getId());
					approvedRequest.setApiKeyRequestStatus(partnerPolicyRequest.getStatus_code());
					//TODO 
					//need to get the info from partnerPolicyRepository table
					//String partnerId = partnerPolicyRequest.getPart_id();
					PartnerPolicy findByPartner_Id = partnerPolicyRepository.findByPartnerId(partnerID);
					if(findByPartner_Id == null) {
						System.out.println(partnerPolicyRequest.getId() + ": PARTNER_API_KEY is not created");
						throw new PartnerAPIKeyIsNotCreatedException(
								PartnerAPIKeyIsNotCreatedConstant.PARTNER_API_NOT_CREATED_EXCEPTION.getErrorCode(),
								PartnerAPIKeyIsNotCreatedConstant.PARTNER_API_NOT_CREATED_EXCEPTION.getErrorMessage());
					}
					approvedRequest.setPartnerApiKey(findByPartner_Id.getPolicy_api_key());
					
					
					//approvedRequest.setPartnerApiKey("fa604-affcd-33201-04770");
					approvedRequest.setValidityTill(LocalDate.now().plusDays(60).toString());

					listAPIkeyRequests.add(approvedRequest);
				} else {
					APIkeyRequests approvedRequest = new APIkeyRequests();
					approvedRequest.setApiKeyReqID(partnerPolicyRequest.getId());
					approvedRequest.setApiKeyRequestStatus(partnerPolicyRequest.getStatus_code());
					listAPIkeyRequests.add(approvedRequest);
				}

				response.setAPIkeyRequests(listAPIkeyRequests);
			}

		} else {
			throw new PartnerDoesNotExistsException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		return response;
	}


	@Override
	public APIkeyRequests viewApiKeyRequestStatusApiKey(String partnerID, String aPIKeyReqID) {
		PartnerPolicy partnerPolicy = null;
		Optional<PartnerPolicyRequest> findById = partnerPolicyRequestRepository.findById(aPIKeyReqID);
		APIkeyRequests aPIkeyRequests = new APIkeyRequests();
		if(findById.isPresent() && findById!=null) {
				
			PartnerPolicyRequest partnerPolicyRequest = findById.get();
			
			if(partnerPolicyRequest.getPart_id().equals(partnerID)) {
					
				String status_code = partnerPolicyRequest.getStatus_code();
				if(status_code.equalsIgnoreCase("Approved")) {
					aPIkeyRequests.setApiKeyReqID(partnerPolicyRequest.getId());
					aPIkeyRequests.setApiKeyRequestStatus(status_code);
					partnerPolicy = partnerPolicyRepository.findByPartnerId(partnerID);
					aPIkeyRequests.setValidityTill(partnerPolicy.getValid_to_datetime());
					aPIkeyRequests.setPartnerApiKey(partnerPolicy.getPolicy_api_key());
				}else {
					//TODO
					System.out.println("throw the exception aPIKeyReqID status is In-progress");
					//throw the exception aPIKeyReqID status is In-progress
					throw new APIKeyReqIdStatusInProgressException(
							APIKeyReqIdStatusInProgressConstant.APIKEYREQIDSTATUSINPROGRESS.getErrorCode(),
							APIKeyReqIdStatusInProgressConstant.APIKEYREQIDSTATUSINPROGRESS.getErrorMessage());
				}
				
			}else {
				throw new PartnerDoesNotExistsException(
						PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
						PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
			}
			
		}else {
			//TODO
			System.out.println("throw the exception aPIKeyReqID is not exist (\"errorCode\": \"PMS_PRT_006\",)");
			// throw the exception aPIKeyReqID is not exist ("errorCode": "PMS_PRT_006",)
			throw new PartnerAPIKeyReqIDDoesNotExistException(
					PartnerAPIDoesNotExistConstant.PARTNER_API_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerAPIDoesNotExistConstant.PARTNER_API_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		return aPIkeyRequests;
	}
}
