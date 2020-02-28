package io.mosip.pmp.partnermanagement.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.mosip.pmp.partnermanagement.constant.InvalidInputParameterConstant;
import io.mosip.pmp.partnermanagement.constant.NewPolicyIdNotExistConstant;
import io.mosip.pmp.partnermanagement.constant.NoPartnerApiKeyRequestsConstant;
import io.mosip.pmp.partnermanagement.constant.PartnerAPIDoesNotExistConstant;
import io.mosip.pmp.partnermanagement.constant.PartnerAPIKeyDoesNotExistConstant;
import io.mosip.pmp.partnermanagement.constant.PartnerApiKeyDoesNotBelongToThePolicyGroupOfThePartnerMangerConstant;
import io.mosip.pmp.partnermanagement.constant.PartnerDoesNotExistExceptionConstant;
import io.mosip.pmp.partnermanagement.constant.PartnerIdDoesNotExistExceptionConstant;
import io.mosip.pmp.partnermanagement.constant.PolicyNotExistConstant;
import io.mosip.pmp.partnermanagement.controller.PartnerManagementController;
import io.mosip.pmp.partnermanagement.dto.ActivateDeactivatePartnerRequest;
import io.mosip.pmp.partnermanagement.dto.ApikeyRequests;
import io.mosip.pmp.partnermanagement.dto.PartnerAPIKeyToPolicyMappingsResponse;
import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingRequest;
import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingResponse;
import io.mosip.pmp.partnermanagement.dto.PolicyIDResponse;
import io.mosip.pmp.partnermanagement.dto.RetrievePartnerDetailsResponse;
import io.mosip.pmp.partnermanagement.dto.RetrievePartnerManagers;
import io.mosip.pmp.partnermanagement.dto.RetrievePartnersDetails;
import io.mosip.pmp.partnermanagement.dto.RetrievePartnersManagersDetails;
import io.mosip.pmp.partnermanagement.entity.AuthPolicy;
import io.mosip.pmp.partnermanagement.entity.Partner;
import io.mosip.pmp.partnermanagement.entity.PartnerPolicy;
import io.mosip.pmp.partnermanagement.entity.PartnerPolicyRequest;
import io.mosip.pmp.partnermanagement.entity.PolicyGroup;
import io.mosip.pmp.partnermanagement.exception.InvalidInputParameterException;
import io.mosip.pmp.partnermanagement.exception.NewPolicyIdNotExistException;
import io.mosip.pmp.partnermanagement.exception.NoPartnerApiKeyRequestsException;
import io.mosip.pmp.partnermanagement.exception.PartnerAPIDoesNotExistException;
import io.mosip.pmp.partnermanagement.exception.PartnerAPIKeyDoesNotExistException;
import io.mosip.pmp.partnermanagement.exception.PartnerApiKeyDoesNotBelongToThePolicyGroupOfThePartnerMangerException;
import io.mosip.pmp.partnermanagement.exception.PartnerDoesNotExistException;
import io.mosip.pmp.partnermanagement.exception.PartnerIdDoesNotExistException;
import io.mosip.pmp.partnermanagement.exception.PolicyNotExistException;
import io.mosip.pmp.partnermanagement.repository.AuthPolicyRepository;
import io.mosip.pmp.partnermanagement.repository.PartnerPolicyRepository;
import io.mosip.pmp.partnermanagement.repository.PartnerPolicyRequestRepository;
import io.mosip.pmp.partnermanagement.repository.PartnerRepository;
import io.mosip.pmp.partnermanagement.repository.PolicyGroupRepository;
import io.mosip.pmp.partnermanagement.service.PartnerManagementService;
import io.mosip.pmp.partnermanagement.util.PartnerUtil;

/**
 * @author sanjeev.shrivastava
 *
 */

@Service
public class PartnerManagementServiceImpl implements PartnerManagementService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PartnerManagementController.class);
	
	@Autowired
	PartnerPolicyRepository partnerPolicyRepository;

	@Autowired
	PartnerRepository partnerRepository;
	
	@Autowired
	PartnerPolicyRequestRepository partnerPolicyRequestRepository;
	
	@Autowired
	PolicyGroupRepository policyGroupRepository;
	
	@Autowired
	AuthPolicyRepository authPolicyRepository;

	@Override
	public PartnersPolicyMappingResponse partnerApiKeyPolicyMappings(PartnersPolicyMappingRequest request,
			String partnerID, String PolicyAPIKey) {
		LocalDateTime now = LocalDateTime.now(); 
		LOGGER.info("Getting details from partner Policy for given API_KEY and PartnerID :" + PolicyAPIKey
				+ " and " + partnerID);
		Optional<PartnerPolicy> findById = partnerPolicyRepository.findById(PolicyAPIKey);
		PartnerPolicy partnerPolicy = null;
		PolicyGroup new_policyGroup = null;
		AuthPolicy new_authPolicy = null;
		if (findById.isPresent() && findById != null) {
			LOGGER.info(PolicyAPIKey + " : Valied PartnerAPIKey");
			partnerPolicy = findById.get();

			String old_auth_pilicy_id = partnerPolicy.getPolicyId();
			String part_id = partnerPolicy.getPartner().getId();

			if (old_auth_pilicy_id != null && part_id.equalsIgnoreCase(partnerID)) {
				LOGGER.info(partnerID +" : Valied PartnerID");
				LOGGER.info("Getting details from auth Policy table");
				Optional<AuthPolicy> old_findByAuthId = authPolicyRepository.findById(old_auth_pilicy_id);

				AuthPolicy old_authPolicy = old_findByAuthId.get();

				String old_policy_id = old_authPolicy.getPolicyGroup().getId();
				LOGGER.info(old_policy_id + " : Existing Policy ID");
				if (old_policy_id.equalsIgnoreCase(request.getOldPolicyID())) {
					LOGGER.info(request.getOldPolicyID() + " : This is valied Existing or Old Policy ID");
					Optional<PolicyGroup> new_findByGroupId = policyGroupRepository.findById(request.getNewPolicyID());
					if (new_findByGroupId.isPresent()) {
						LOGGER.info(request.getNewPolicyID() + " : This is valied new Policy ID");
						new_policyGroup = new_findByGroupId.get();
						
						new_authPolicy = authPolicyRepository.findByPolicyId(new_policyGroup.getId());
						
					} else {
						LOGGER.info(request.getNewPolicyID() + " : This is Invalied new Policy ID");
						throw new NewPolicyIdNotExistException(
								NewPolicyIdNotExistConstant.NEW_POLICY_ID_NOT_EXIST.getErrorCode(),
								NewPolicyIdNotExistConstant.NEW_POLICY_ID_NOT_EXIST.getErrorMessage());
					}
					
					partnerPolicy.setPolicyId(new_authPolicy.getId());
					partnerPolicyRepository.save(partnerPolicy);
					LOGGER.info(partnerPolicy.getPolicyApiKey() + " : Updated Successfully");
				} else {
					LOGGER.info(request.getOldPolicyID() + " : This is Invalied Existing or Old Policy ID");
					throw new PolicyNotExistException(PolicyNotExistConstant.POLICY_NOT_EXIST_EXCEPTION.getErrorCode(),
							PolicyNotExistConstant.POLICY_NOT_EXIST_EXCEPTION.getErrorMessage());
				}

			} else {
				LOGGER.info(partnerID + " : Invalied Partner Id");
				throw new PartnerDoesNotExistException(
						PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
						PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
			}
		} else {
			LOGGER.info(PolicyAPIKey + " : Invalied PartnerAPIKey");
			throw new PartnerAPIKeyDoesNotExistException(
					PartnerAPIKeyDoesNotExistConstant.PARTNER_API_KEY_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerAPIKeyDoesNotExistConstant.PARTNER_API_KEY_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		
		
		/*
		 * Updating Partner Policy in Partner Table..!
		 */
		
		Optional<Partner> partner_findById = partnerRepository.findById(partnerID);
		Partner partner = partner_findById.get();
		partner.setPolicyGroupId(new_policyGroup.getId());
		partner.setUpdBy("Partner Manager");
		partner.setUpdDtimes(Timestamp.valueOf(now));
		partnerRepository.save(partner);
		
		
		
		
		PartnersPolicyMappingResponse partnersPolicyMappingResponse = new PartnersPolicyMappingResponse();
		partnersPolicyMappingResponse.setMessage("Partner api key to Policy Mappings updated successfully");
		return partnersPolicyMappingResponse;
	}

	@Override
	public PartnersPolicyMappingResponse activateDeactivateAuthEKYCPartner(String partnerID,
			ActivateDeactivatePartnerRequest request) {
		PartnersPolicyMappingResponse partnersPolicyMappingResponse = new PartnersPolicyMappingResponse();
		Optional<Partner> findById = partnerRepository.findById(partnerID);
		LocalDateTime now = LocalDateTime.now();
		if (findById.isPresent() && findById != null) {
			Partner partner = findById.get();
			String partner_status = request.getStatus();
			
			if(partner_status.equalsIgnoreCase("Active") || partner_status.equalsIgnoreCase("De-Active")) {
				if(partner_status.equalsIgnoreCase("Active")) {
					partner.setIsActive(true);
				} else if (partner_status.equalsIgnoreCase("De-Active")) {
					partner.setIsActive(false);
				}
			}else {
				LOGGER.info(partner_status + " : is Invalid Input Parameter, it should be (Active/De-Active)");
				throw new InvalidInputParameterException(
						InvalidInputParameterConstant.INVALIED_INPUT_PARAMETER.getErrorCode(),
						InvalidInputParameterConstant.INVALIED_INPUT_PARAMETER.getErrorMessage());
			}
				partner.setUpdBy("Partner_Manager");	
				partner.setUpdDtimes(Timestamp.valueOf(now));	
			partnerRepository.save(partner);
		} else {
			LOGGER.info(partnerID + " : Partner Id Does Not Exist");
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
		LocalDateTime now = LocalDateTime.now();
		if (findById.isPresent() && findById != null) {
			PartnerPolicy partnerPolicy = findById.get();
			if (partnerPolicy.getPartner().getId().equals(partnerID)) {
				
				if(request.getStatus().equalsIgnoreCase("Active") || request.getStatus().equalsIgnoreCase("De-Active")) {
					if(request.getStatus().equalsIgnoreCase("Active")) {
						partnerPolicy.setIsActive(true);
					}else if(request.getStatus().equalsIgnoreCase("De-Active")){
						partnerPolicy.setIsActive(false);
					}
				}else {
					LOGGER.info("Invalid Input Parameter, it should be (Active/De-Active)");
					throw new InvalidInputParameterException(
							InvalidInputParameterConstant.INVALIED_INPUT_PARAMETER.getErrorCode(),
							InvalidInputParameterConstant.INVALIED_INPUT_PARAMETER.getErrorMessage()); 
				}
				partnerPolicy.setUpdBy("Partner_Manager");
				partnerPolicy.setUpdDtimes(Timestamp.valueOf(now));
				partnerPolicyRepository.save(partnerPolicy);
				LOGGER.info(partnerAPIKey + " : API KEY Status Updated Successfully");
			} else {
				LOGGER.info(partnerID + " : Partner Id Does Not Exist");
				throw new PartnerDoesNotExistException(
						PartnerIdDoesNotExistExceptionConstant.PARTNER_ID_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
						PartnerIdDoesNotExistExceptionConstant.PARTNER_ID_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
			}
		} else {
			LOGGER.info(partnerAPIKey + " : partnerAPIKey Does Not Exist");
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
		if(list_part == null) {
			throw new PartnerDoesNotExistException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		Iterator<Partner> partner_iterat = list_part.iterator();
		while (partner_iterat.hasNext()) {
			RetrievePartnersDetails retrievePartnersDetails = new RetrievePartnersDetails();
			partner = partner_iterat.next();

			retrievePartnersDetails.setPartnerID(partner.getId());
			retrievePartnersDetails.setStatus(partner.getIsActive()== true ? "Active" : "De-Active");
			retrievePartnersDetails.setOrganizationName(partner.getName());
			retrievePartnersDetails.setContactNumber(partner.getContactNo());
			retrievePartnersDetails.setEmailId(partner.getEmailId());
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
			retrievePartnersDetails.setStatus(partner.getIsActive()== true ? "Active" : "De-Active");
			retrievePartnersDetails.setOrganizationName(partner.getName());
			retrievePartnersDetails.setContactNumber(partner.getContactNo());
			retrievePartnersDetails.setEmailId(partner.getEmailId());
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
		
		//TODO Partner API Key pattern, validate expiry for Partner API Key and status
		
		AuthPolicy authPolicy = null;
		Optional<PartnerPolicy> findById = partnerPolicyRepository.findById(PartnerAPIKey);
		PartnerAPIKeyToPolicyMappingsResponse response = new PartnerAPIKeyToPolicyMappingsResponse();
		if (findById.isPresent() && findById != null) {
			PartnerPolicy partnerPolicy = findById.get();
			if (partnerPolicy.getPartner().getId().equals(partnerID)) {
				response.setPartnerID(partnerID);
				String auth_id = partnerPolicy.getPolicyId();
				Optional<AuthPolicy> findByAuthId = authPolicyRepository.findById(auth_id);
				if(findByAuthId.isPresent()) {
					authPolicy =findByAuthId.get();
					response.setPolicyId(authPolicy.getPolicyGroup().getId());
				}else {
					throw new PartnerApiKeyDoesNotBelongToThePolicyGroupOfThePartnerMangerException(
							PartnerApiKeyDoesNotBelongToThePolicyGroupOfThePartnerMangerConstant.PARTNER_API_DOES_NOT_BELONGS_TO_THE_POLICYGROUP_OF_PARTNERMANAGER_EXCEPTION
									.getErrorCode(),
							PartnerApiKeyDoesNotBelongToThePolicyGroupOfThePartnerMangerConstant.PARTNER_API_DOES_NOT_BELONGS_TO_THE_POLICYGROUP_OF_PARTNERMANAGER_EXCEPTION
									.getErrorMessage());
				}
			} else {
				LOGGER.info(partnerID + " : Partner Not Exist Exception");
				throw new PartnerIdDoesNotExistException(
						PartnerIdDoesNotExistExceptionConstant.PARTNER_ID_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
						PartnerIdDoesNotExistExceptionConstant.PARTNER_ID_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
			}
		} else {
			LOGGER.info(PartnerAPIKey + " : Partner API KEY Not Exist");
			throw new PartnerAPIDoesNotExistException(
					PartnerAPIKeyDoesNotExistConstant.PARTNER_API_KEY_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerAPIKeyDoesNotExistConstant.PARTNER_API_KEY_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		return response;
	}

	@Override
	public List<ApikeyRequests> getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagers() {
		
		List<ApikeyRequests> requests = new ArrayList<ApikeyRequests>();
		
		List<PartnerPolicyRequest> findAll = partnerPolicyRequestRepository.findAll();
		
		PartnerPolicyRequest partnerPolicyRequest = new PartnerPolicyRequest();
		
		String parnerId = null;
		if(!findAll.isEmpty() && findAll!=null) {
			
			Iterator<PartnerPolicyRequest> partnerPolicyRequest_iterat = findAll.iterator();
			while (partnerPolicyRequest_iterat.hasNext()) {
				ApikeyRequests ApikeyRequests = new ApikeyRequests();
				partnerPolicyRequest = partnerPolicyRequest_iterat.next();
				parnerId = partnerPolicyRequest.getPartner().getId();
				ApikeyRequests.setPartnerID(parnerId);
				ApikeyRequests.setStatus(partnerPolicyRequest.getStatusCode());
				
				Optional<Partner> findBy_PartnerId = partnerRepository.findById(parnerId);
				
				if(!findBy_PartnerId.isPresent()) {
					throw new PartnerDoesNotExistException(
							PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
							PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
				}
				
				Partner partner = findBy_PartnerId.get();
				
				ApikeyRequests.setOrganizationName(partner.getName());
				
				String policy_group_id = partner.getPolicyGroupId();
				Optional<PolicyGroup> findById = policyGroupRepository.findById(policy_group_id);
				PolicyGroup policyGroup = findById.get();
				
				ApikeyRequests.setPolicyName(policyGroup.getName());
				ApikeyRequests.setPolicyDesc(policyGroup.getDescr());
				ApikeyRequests.setApiKeyReqNo(partnerPolicyRequest.getId());

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
		Optional<PartnerPolicyRequest> findById = partnerPolicyRequestRepository.findById(APIKeyReqID);
		if(findById.isPresent() && findById!=null) {
			PartnerPolicyRequest partnerPolicyRequest = findById.get();
			
			String partnerId = partnerPolicyRequest.getPartner().getId();
			String policy_Id = partnerPolicyRequest.getPolicyId();
			
			Optional<Partner> findByPartnerId = partnerRepository.findById(partnerId);
			Partner partner = findByPartnerId.get();
			
			Optional<PolicyGroup> findByPolicyId = policyGroupRepository.findById(policy_Id);			
			PolicyGroup policyGroup = findByPolicyId.get();
			
			apikeyRequests.setPartnerID(partner.getId());
			apikeyRequests.setStatus(partnerPolicyRequest.getStatusCode());
			apikeyRequests.setOrganizationName(partner.getName());
			apikeyRequests.setPolicyName(policyGroup.getName());
			apikeyRequests.setPolicyDesc(policyGroup.getDescr());
			apikeyRequests.setApiKeyReqNo(partnerPolicyRequest.getId());
		}else {
			throw new NoPartnerApiKeyRequestsException(
					NoPartnerApiKeyRequestsConstant.NO_PARTNER_API_KEY_REQUEST_EXCEPTION.getErrorCode(),
					NoPartnerApiKeyRequestsConstant.NO_PARTNER_API_KEY_REQUEST_EXCEPTION.getErrorMessage());
		}
		
		return apikeyRequests;
	}

	@Override
	public PartnersPolicyMappingResponse approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(
			ActivateDeactivatePartnerRequest request, String partnerKeyReqId) {
		PartnerPolicyRequest partnerPolicyRequest=null;
		String partner_id = null;
		LocalDateTime now = LocalDateTime.now();
		PartnersPolicyMappingResponse partnersPolicyMappingResponse = new PartnersPolicyMappingResponse();
		Optional<PartnerPolicyRequest> findById = partnerPolicyRequestRepository.findById(partnerKeyReqId);
		
		if(!findById.isPresent()) {
			throw new PartnerAPIDoesNotExistException(
					PartnerAPIDoesNotExistConstant.PARTNER_API_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerAPIDoesNotExistConstant.PARTNER_API_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		
		partnerPolicyRequest = findById.get();
		
		if(partnerPolicyRequest.getStatusCode().equalsIgnoreCase("in-progress") && (request.getStatus().equalsIgnoreCase("Approved") || request.getStatus().equalsIgnoreCase("Rejected"))) {
			partnerPolicyRequest.setStatusCode(request.getStatus());
		}else {
			LOGGER.info(request.getStatus() + " : Invalid Input Parameter (status should be Approved/Rejected)");
			throw new InvalidInputParameterException(
					InvalidInputParameterConstant.INVALIED_INPUT_PARAMETER.getErrorCode(),
					InvalidInputParameterConstant.INVALIED_INPUT_PARAMETER.getErrorMessage());
		}
		partnerPolicyRequest.setUpdBy("Partner_Manager");
		partnerPolicyRequest.setUpdDtimes(Timestamp.valueOf(now));
		partnerPolicyRequestRepository.save(partnerPolicyRequest);
		
		if(request.getStatus().equalsIgnoreCase("Approved")) {
			LOGGER.info("++++++++++++++++++++++Creating Partner_API_Key+++++++++++++++++++++++++");
			LOGGER.info("Partner_API_Key should be unique for same partner");
			PartnerPolicy partnerPolicy = new PartnerPolicy();
			partner_id = partnerPolicyRequest.getPartner().getId();
			PartnerPolicy findByPartnerId = partnerPolicyRepository.findByPartnerId(partner_id);
			
			if(findByPartnerId!=null) {
				LOGGER.info(partnerKeyReqId + " : this partnerKeyReqId already have PartnerAPIKey");
			}else {
				String Partner_API_Key = PartnerUtil.createPartnerApiKey(); 
	 			Optional<PartnerPolicy> detail_Partner_API_Key = partnerPolicyRepository.findById(Partner_API_Key);
				if(detail_Partner_API_Key.isPresent()) {
					LOGGER.info(Partner_API_Key + " : this is duplicate PartnerAPIKey");
				}else {
					partnerPolicy.setPolicyApiKey(Partner_API_Key);
					partnerPolicy.setPartner(partnerPolicyRequest.getPartner());
					
					LOGGER.info("+++++++++++++++++++++Need to check auth_policy_id++++++++++++++++++++++");
					String policy_id = partnerPolicyRequest.getPolicyId();
					
					AuthPolicy findByPolicyId = authPolicyRepository.findByPolicyId(policy_id);
					if(findByPolicyId == null) {
						LOGGER.info(policy_id + "Invalied Policy Id");
					}
					
					partnerPolicy.setPolicyId(findByPolicyId.getId());
					partnerPolicy.setIsActive(true);
					partnerPolicy.setValidFromDatetime(Timestamp.valueOf(now));
					partnerPolicy.setValidToDatetime(Timestamp.valueOf(now.plusDays(60)));
					partnerPolicy.setCrBy(partnerPolicyRequest.getCrBy());
					partnerPolicy.setCrDtimes(partnerPolicyRequest.getCrDtimes());
					partnerPolicyRepository.save(partnerPolicy);
					LOGGER.info(Partner_API_Key + " : APIKEY Successfully created");
				}
			}
		}
		partnersPolicyMappingResponse.setMessage("PartnerAPIKey Updated successfully");
		return partnersPolicyMappingResponse;
	}

	@Override
	public RetrievePartnerManagers getPartnerManager() {
		
		RetrievePartnerManagers partnersResponse = new RetrievePartnerManagers();
		List<RetrievePartnersManagersDetails> partners = new ArrayList<RetrievePartnersManagersDetails>();

		List<Partner> list_part = null;
		list_part = partnerRepository.findAll();
		Partner partner = null;
		if(list_part == null) {
			throw new PartnerDoesNotExistException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		Iterator<Partner> partner_iterat = list_part.iterator();
		while (partner_iterat.hasNext()) {
			RetrievePartnersManagersDetails retrievePartnersManagersDetails = new RetrievePartnersManagersDetails();
			partner = partner_iterat.next();
			
			retrievePartnersManagersDetails.setPartnerID(partner.getId());
			retrievePartnersManagersDetails.setPartnerStatus(partner.getIsActive()== true ? "Active" : "De-Active");
			
			
			retrievePartnersManagersDetails.setPolicyID(partner.getPolicyGroupId());
			
			Optional<PolicyGroup> findByIdpolicyGroup = policyGroupRepository.findById(partner.getPolicyGroupId());
			
			if(findByIdpolicyGroup!=null) {
				retrievePartnersManagersDetails.setPolicyName(findByIdpolicyGroup.get().getName());
			}
			
			
			
			String status_code = null;
			String aPIKeyReqID = null;
			
			if(!partnerPolicyRequestRepository.findByPartnerId(partner.getId()).isEmpty()) {
				status_code = partnerPolicyRequestRepository.findByPartnerId(partner.getId()).get(0).getStatusCode();
				aPIKeyReqID = partnerPolicyRequestRepository.findByPartnerId(partner.getId()).get(0).getId();
				
			}else {
				status_code = "YET TO SUBMIT";
				aPIKeyReqID = "NOT CREATED";
			}
			
			retrievePartnersManagersDetails.setApikeyReqIDStatus(status_code);
			retrievePartnersManagersDetails.setApikeyReqID(aPIKeyReqID);
			
			
			String PolicyId = null;
			String PolicyIdStatus = null;
			
			PartnerPolicy partnerPolicy = null;
			partnerPolicy = partnerPolicyRepository.findByPartnerId(partner.getId());
			if(partnerPolicy!=null) {
				PolicyId = partnerPolicy.getPolicyApiKey();
				PolicyIdStatus = partnerPolicy.getIsActive() == true ? "Active" : "De-Active" ;
			}else {
				PolicyId = "NOT CREATED";
				PolicyIdStatus = "UNDEFINED";
			}
			
			retrievePartnersManagersDetails.setPartnerAPIKey(PolicyId);
			retrievePartnersManagersDetails.setPartnerAPIKeyStatus(PolicyIdStatus);
			
			
			partners.add(retrievePartnersManagersDetails);
		}
		partnersResponse.setPartners(partners);
		return partnersResponse;
	}

	@Override
	public PolicyIDResponse getPartnerPolicyID(String PolicyName) {
		
		PolicyGroup policyGroup = null;
		PolicyIDResponse policyIDResponse = new PolicyIDResponse();
		LOGGER.info("***************validating the policy group********************");
		policyGroup = policyGroupRepository.findByName(PolicyName);
		if (policyGroup != null) {
			LOGGER.info(PolicyName + " : Policy Group is available for the partner");
			policyIDResponse.setPolicyID(policyGroup.getId());
		}
		return policyIDResponse;
	}
}
