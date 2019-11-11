package io.mosip.pmp.partner.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pmp.partner.constant.APIKeyReqIdStatusInProgressConstant;
import io.mosip.pmp.partner.constant.PartnerAPIKeyIsNotCreatedConstant;
import io.mosip.pmp.partner.constant.PartnerAPIKeyReqDoesNotExistConstant;
import io.mosip.pmp.partner.constant.PartnerAlreadyRegisteredWithSamePolicyGroupConstant;
import io.mosip.pmp.partner.constant.PartnerDoesNotExistExceptionConstant;
import io.mosip.pmp.partner.constant.PartnerIdExceptionConstant;
import io.mosip.pmp.partner.constant.PolicyGroupDoesNotExistConstant;
import io.mosip.pmp.partner.core.RequestWrapper;
import io.mosip.pmp.partner.dto.APIkeyRequests;
import io.mosip.pmp.partner.dto.DigitalCertificateRequest;
import io.mosip.pmp.partner.dto.DigitalCertificateRequestPreparation;
import io.mosip.pmp.partner.dto.DigitalCertificateResponse;
import io.mosip.pmp.partner.dto.DownloadPartnerAPIkeyResponse;
import io.mosip.pmp.partner.dto.LoginUserRequest;
import io.mosip.pmp.partner.dto.LoginUserResponse;
import io.mosip.pmp.partner.dto.PartnerAPIKeyRequest;
import io.mosip.pmp.partner.dto.PartnerAPIKeyResponse;
import io.mosip.pmp.partner.dto.PartnerRequest;
import io.mosip.pmp.partner.dto.PartnerResponse;
import io.mosip.pmp.partner.dto.PartnerUpdateRequest;
import io.mosip.pmp.partner.dto.RetrievePartnerDetailsResponse;
import io.mosip.pmp.partner.dto.RetrievePartnerDetailsWithNameResponse;
import io.mosip.pmp.partner.dto.SignUserRequest;
import io.mosip.pmp.partner.dto.SignUserResponse;
import io.mosip.pmp.partner.entity.Partner;
import io.mosip.pmp.partner.entity.PartnerPolicy;
import io.mosip.pmp.partner.entity.PartnerPolicyRequest;
import io.mosip.pmp.partner.entity.PolicyGroup;
import io.mosip.pmp.partner.exception.APIKeyReqIdStatusInProgressException;
import io.mosip.pmp.partner.exception.PartnerAPIKeyIsNotCreatedException;
import io.mosip.pmp.partner.exception.PartnerAPIKeyReqIDDoesNotExistException;
import io.mosip.pmp.partner.exception.PartnerAlreadyRegisteredException;
import io.mosip.pmp.partner.exception.PartnerAlreadyRegisteredWithSamePolicyGroupException;
import io.mosip.pmp.partner.exception.PartnerDoesNotExistException;
import io.mosip.pmp.partner.exception.PartnerDoesNotExistsException;
import io.mosip.pmp.partner.exception.PolicyGroupDoesNotExistException;
import io.mosip.pmp.partner.repository.AuthPolicyRepository;
import io.mosip.pmp.partner.repository.PartnerPolicyRepository;
import io.mosip.pmp.partner.repository.PartnerPolicyRequestRepository;
import io.mosip.pmp.partner.repository.PartnerServiceRepository;
import io.mosip.pmp.partner.repository.PolicyGroupRepository;
import io.mosip.pmp.partner.service.PartnerService;
import io.mosip.pmp.partner.util.HeaderRequestInterceptor;
import io.mosip.pmp.partner.util.PartnerUtil;

/**
 * @author sanjeev.shrivastava
 *
 */

@Service
@Transactional
public class PartnerServiceImpl implements PartnerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PartnerServiceImpl.class);
	
	@Autowired
	PartnerServiceRepository partnerRepository;

	@Autowired
	PolicyGroupRepository policyGroupRepository;

	@Autowired
	PartnerPolicyRepository partnerPolicyRepository;

	@Autowired
	PartnerPolicyRequestRepository partnerPolicyRequestRepository;
	
	@Autowired
	AuthPolicyRepository authPolicyRepository;
	
	//@Autowired
	//PartnerIdGenerator<String>  partnerIdGenerator;
	
	//@Autowired
	//KeymanagerService keymanagerService;
	
	@Autowired
	RestTemplate restTemplate;
	
	String response_cookies = null;
	String signature_value = null;
	
	
	@Override
	public String getPolicyId(String PolicyName) {
		
		String policyId = null;
		PolicyGroup policyGroup = policyGroupRepository.findByName(PolicyName);
		if(policyGroup!=null) {
			policyId = policyGroup.getId();
		}else {
			LOGGER.info("Invalied Policy Name : "+ PolicyName);
		}
		return policyId;
	}
	
	@Override
	public PartnerResponse savePartner(PartnerRequest request) {
		
		Partner partner_name = partnerRepository.findByName(request.getOrganizationName());
		String part_id = PartnerUtil.createPartnerId();
		if(partner_name == null) {
			Partner partner = new Partner();
			partner.setId(part_id);
			//partner.setId(partnerIdGenerator.generateId());
			PolicyGroup policyGroup = null;
			LOGGER.info("***************validating the policy group********************");
			policyGroup = policyGroupRepository.findByName(request.getPolicyGroup());
			LocalDateTime now = LocalDateTime.now();
			if (policyGroup != null) {
				LOGGER.info(request.getPolicyGroup() + " : Policy Group is available for the partner");
				partner.setPolicyGroupId(policyGroup.getId());
				partner.setName(request.getOrganizationName());
				partner.setAddress(request.getAddress());
				partner.setContactNo(request.getContactNumber());
				partner.setEmailId(request.getEmailId());
				partner.setIsActive(true);
				partner.setUserId("110083");
				partner.setCrBy("Partner Service");
				partner.setCrDtimes(Timestamp.valueOf(now));
				
				LOGGER.info(request.getOrganizationName() +" : this is unique partner");
				LOGGER.info(" +++++++++++++++++++++Saving the partner+++++++++++++++++++++++ ");
				partnerRepository.save(partner);
			} else {
				LOGGER.error(request.getPolicyGroup() + " : Policy Group is not availavle for the partner");
				throw new PolicyGroupDoesNotExistException(
						PolicyGroupDoesNotExistConstant.POLICY_GROUP_DOES_NOT_EXIST.getErrorCode(),
						PolicyGroupDoesNotExistConstant.POLICY_GROUP_DOES_NOT_EXIST.getErrorMessage());
			}
		}else {
			LOGGER.error(request.getOrganizationName() +" : this is duplicate partner");
			throw new PartnerAlreadyRegisteredException(
					PartnerIdExceptionConstant.PARTNER_ALREADY_REGISTERED_EXCEPTION.getErrorCode(),
					PartnerIdExceptionConstant.PARTNER_ALREADY_REGISTERED_EXCEPTION.getErrorMessage());
		}
		PartnerResponse partnerResponse = new PartnerResponse();
		partnerResponse.setPartnerId(part_id);
		partnerResponse.setStatus("Active");
		
		return partnerResponse;
	}
	
	@Override
	public RetrievePartnerDetailsResponse getPartnerDetails(String partnerID) {
		RetrievePartnerDetailsResponse response = new RetrievePartnerDetailsResponse();
		Optional<Partner> findByIdPartner = partnerRepository.findById(partnerID);
		Partner partner = null;
		Optional<PolicyGroup> findByIdpolicyGroup = null;
		PolicyGroup policyGroup = null;

		if (findByIdPartner.isPresent() && findByIdPartner != null) {
			LOGGER.info(partnerID +": Partner is available");
			partner = findByIdPartner.get();
			response.setPartnerID(partner.getId());
			response.setAddress(partner.getAddress());
			response.setContactNumber(partner.getContactNo());
			response.setEmailId(partner.getEmailId());
			response.setOrganizationName(partner.getName());

			LOGGER.info("++++++++++++Retriving the name of policy group+++++++++++++");
			findByIdpolicyGroup = policyGroupRepository.findById(partner.getPolicyGroupId());
			policyGroup = findByIdpolicyGroup.get();
			
			response.setPolicyGroup(policyGroup.getName());
			return response;
		} else {
			LOGGER.info(partnerID +": Partner is not available");
			throw new PartnerDoesNotExistsException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
	}


	@Override
	public RetrievePartnerDetailsWithNameResponse getPartnerDetailsWithName(String partnerName) {
		RetrievePartnerDetailsWithNameResponse response = new RetrievePartnerDetailsWithNameResponse();
		Partner Partner_ByName = partnerRepository.findByName(partnerName);
		Optional<PolicyGroup> findByIdpolicyGroup = null;
		PolicyGroup policyGroup = null;
		if(Partner_ByName!=null) {
			response.setId(Partner_ByName.getId());
			response.setAddress(Partner_ByName.getAddress());
			response.setContactNo(Partner_ByName.getContactNo());
			response.setCrBy(Partner_ByName.getCrBy());
			response.setCrDtimes(Partner_ByName.getCrDtimes());
			response.setEmailId(Partner_ByName.getEmailId());
			response.setIsActive(Partner_ByName.getIsActive());
			response.setName(Partner_ByName.getName());
			response.setUpdBy(Partner_ByName.getUpdBy());
			response.setUpdDtimes(Partner_ByName.getUpdDtimes());
			response.setUserId(Partner_ByName.getUserId());
			
			LOGGER.info("++++++++++++Retriving the name of policy group+++++++++++++");
			findByIdpolicyGroup = policyGroupRepository.findById(Partner_ByName.getPolicyGroupId());
			policyGroup = findByIdpolicyGroup.get();
			
			response.setPolicyGroupName(policyGroup.getName());
		}else {
			LOGGER.info(partnerName +": Partner is not available");
			throw new PartnerDoesNotExistsException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		return response;
	}
	
	@Override
	public PartnerResponse updatePartnerDetail(PartnerUpdateRequest request, String partnerID) {
		Optional<Partner> findById = partnerRepository.findById(partnerID);
		Partner partner = null;
		LocalDateTime now = LocalDateTime.now(); 
		if (findById.isPresent() && findById != null) {
			LOGGER.info(partnerID +": Partner is available");
			partner = findById.get();
			if(partner.getName().equalsIgnoreCase(request.getOrganizationName())) {
				partner.setAddress(request.getAddress());
				partner.setContactNo(request.getContactNumber());
				partner.setEmailId(request.getEmailId());
				partner.setName(request.getOrganizationName());
				partner.setUpdBy("Partner Service");
				partner.setUpdDtimes(Timestamp.valueOf(now));
				LOGGER.info("++++++++++++++++Saving the updated Partner++++++++++++++++++++++");
				partnerRepository.save(partner);
			}else {
				LOGGER.info("++++++++++++++++Checking Name about duplicate/Unique++++++++++++++++++++++");
				Partner findByName = partnerRepository.findByName(request.getOrganizationName());
				
				if(findByName == null) {
					LOGGER.info(request.getOrganizationName() +" : this is Unique name");
					partner.setAddress(request.getAddress());
					partner.setContactNo(request.getContactNumber());
					partner.setEmailId(request.getEmailId());
					partner.setName(request.getOrganizationName());
					partner.setUpdBy("Partner Service");
					partner.setUpdDtimes(Timestamp.valueOf(now));
					LOGGER.info("++++++++++++++++Saving the updated Partner++++++++++++++++++++++");
					partnerRepository.save(partner);
				}else {
					LOGGER.info(request.getOrganizationName() +" : this is duplicate name");
					throw new PartnerAlreadyRegisteredException(
							PartnerIdExceptionConstant.PARTNER_ALREADY_REGISTERED_EXCEPTION.getErrorCode(),
							PartnerIdExceptionConstant.PARTNER_ALREADY_REGISTERED_EXCEPTION.getErrorMessage());
				}
			}
			
			PartnerResponse partnerResponse = new PartnerResponse();
			partnerResponse.setPartnerId(partner.getId());
			Boolean bul = partner.getIsActive();
			if (bul) {
				partnerResponse.setStatus("Active");
			}else {
				partnerResponse.setStatus("De-Active");
			}
			return partnerResponse;
		} else {
			LOGGER.info(partnerID +": Partner is not available");
			throw new PartnerDoesNotExistException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
	}

	@Override
	public PartnerAPIKeyResponse submitPartnerApiKeyReq(PartnerAPIKeyRequest request, String partnerID) {
		PolicyGroup policyGroup = null;
		PartnerPolicyRequest partnerPolicyRequest = null;
		
		policyGroup = policyGroupRepository.findByName(request.getPolicyName());

		if (policyGroup == null) {
			LOGGER.info(request.getPolicyName() + ": Invalied Policy Group");
			throw new PolicyGroupDoesNotExistException(
					PolicyGroupDoesNotExistConstant.POLICY_GROUP_DOES_NOT_EXIST.getErrorCode(),
					PolicyGroupDoesNotExistConstant.POLICY_GROUP_DOES_NOT_EXIST.getErrorMessage());
		}
		
		LOGGER.info(request.getPolicyName() + " : this is valied Policy Group ");

		Optional<Partner> findByPartnerId = partnerRepository.findById(partnerID);
		if (!findByPartnerId.isPresent()) {
			LOGGER.info(partnerID + " : Invalied partnerID");
			throw new PartnerDoesNotExistsException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		
		LOGGER.info(partnerID + " : Valied partnerID");
		Partner partner = findByPartnerId.get();
		
		LOGGER.info("+++++++++++++fetching all record from partner_Policy_Request by given partnerId +++++++++++++");
		List<String> policy_list = new ArrayList<>();
		String existing_policy_id = null;
		List<PartnerPolicyRequest> list_partner_policy_request = partnerPolicyRequestRepository.findByPartnerId(partnerID);
		PartnerPolicyRequest partnerPolicyRequest_obj = null;
		Iterator<PartnerPolicyRequest> it = list_partner_policy_request.iterator();
		while (it.hasNext()) {
			partnerPolicyRequest_obj = it.next();
			existing_policy_id = partnerPolicyRequest_obj.getPolicyId();
			
			policy_list.add(existing_policy_id);
		}
		
		Iterator<String> it_policy = policy_list.iterator();
		while (it_policy.hasNext()) {
			
			if(it_policy.next().equals(policyGroup.getId())) {
				throw new PartnerAlreadyRegisteredWithSamePolicyGroupException(
						PartnerAlreadyRegisteredWithSamePolicyGroupConstant.PARTNER_ALREADY_REG_WITH_SAME_PLICYGROUP.getErrorCode(),
						PartnerAlreadyRegisteredWithSamePolicyGroupConstant.PARTNER_ALREADY_REG_WITH_SAME_PLICYGROUP.getErrorMessage());
			}
		}
		
		LOGGER.info("+++++++++++++Preparing request for partner_Policy_Request+++++++++++++");
		partnerPolicyRequest = new PartnerPolicyRequest();
		String Partner_Policy_Request_Id = PartnerUtil.createPartnerPolicyRequestId();
		partnerPolicyRequest.setId(Partner_Policy_Request_Id);
		partnerPolicyRequest.setStatusCode("in-progress");
		
		partnerPolicyRequest.setPolicyId(policyGroup.getId());
		partnerPolicyRequest.setPartner(partner);
		partnerPolicyRequest.setCrDtimes(partner.getCrDtimes());

		LocalDateTime now = LocalDateTime.now();
		partnerPolicyRequest.setRequestDatetimes(Timestamp.valueOf(now));
		partnerPolicyRequest.setRequestDetail(request.getUseCaseDescription());
		partnerPolicyRequest.setCrBy(partner.getCrBy());

		LOGGER.info("+++++++++++++Saving request for partner_Policy_Request+++++++++++++");
		partnerPolicyRequestRepository.save(partnerPolicyRequest);
		
		LOGGER.info("+++++++++++++Preparing Response for Partner_APIKey_Response+++++++++++++");
		
		PartnerAPIKeyResponse partnerAPIKeyResponse = new PartnerAPIKeyResponse();
		partnerAPIKeyResponse.setApiRequestId(partnerPolicyRequest.getId());
		partnerAPIKeyResponse.setMessage("partnerAPIKeyRequest successfully created");
		LOGGER.info("+++++++++++++partnerAPIKeyRequest successfully created+++++++++++++");
		return partnerAPIKeyResponse;
	}

	@Override
	public DownloadPartnerAPIkeyResponse downloadPartnerAPIkey(String partnerID, String aPIKeyReqID) {
		PartnerPolicy partnerPolicy = null;
		DownloadPartnerAPIkeyResponse downloadPartnerAPIkeyResponse = new DownloadPartnerAPIkeyResponse();
		Optional<PartnerPolicyRequest> partner_request = partnerPolicyRequestRepository.findById(aPIKeyReqID);

		if (partner_request.isPresent() && partner_request != null) {
			LOGGER.info(aPIKeyReqID +" : Valied APIKeyReqID");
			PartnerPolicyRequest partnerPolicyRequest = partner_request.get();
			if (partnerPolicyRequest.getPartner().getId().equals(partnerID)) {
				LOGGER.info(partnerID +" : Valied Partner");
				partnerPolicy = partnerPolicyRepository.findByPartnerId(partnerID);
				if (partnerPolicy != null) {
					downloadPartnerAPIkeyResponse.setPartnerAPIKey(partnerPolicy.getPolicyApiKey());
				} else {
					LOGGER.info(partnerID +" : Partner API Key is not created for given partnerID");
					throw new PartnerAPIKeyIsNotCreatedException(
							PartnerAPIKeyIsNotCreatedConstant.PARTNER_API_NOT_CREATED_EXCEPTION.getErrorCode(),
							PartnerAPIKeyIsNotCreatedConstant.PARTNER_API_NOT_CREATED_EXCEPTION.getErrorMessage());
				}
			} else {
				LOGGER.info(partnerID +" : Invalied Partner");
				throw new PartnerDoesNotExistsException(
						PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
						PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
			}
		} else {
			LOGGER.info(aPIKeyReqID +" : Invalied APIKeyReqID");
			throw new PartnerAPIKeyReqIDDoesNotExistException(
					PartnerAPIKeyReqDoesNotExistConstant.PARTNER_API_KET_REQ_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerAPIKeyReqDoesNotExistConstant.PARTNER_API_KET_REQ_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		return downloadPartnerAPIkeyResponse;
	}

	/*@Override
	public PartnersRetrieveApiKeyRequests retrieveAllApiKeyRequestsSubmittedByPartner(String partnerID) {

		List<PartnerPolicyRequest> findByPartnerId = partnerPolicyRequestRepository.findByPartnerId(partnerID);
		PartnersRetrieveApiKeyRequests response = new PartnersRetrieveApiKeyRequests();
		List<APIkeyRequests> listAPIkeyRequests = new ArrayList<APIkeyRequests>();
		PartnerPolicyRequest partnerPolicyRequest = null;
		if (!findByPartnerId.isEmpty() && findByPartnerId != null) {
			
			LOGGER.info(partnerID +" : Valied PartnerId");
			LOGGER.info(findByPartnerId.size() +" : Number of recods found");
			
			Iterator<PartnerPolicyRequest> it = findByPartnerId.iterator();
			while (it.hasNext()) {
				partnerPolicyRequest = it.next();
				if (partnerPolicyRequest.getStatusCode().equalsIgnoreCase("approved")) {
					APIkeyRequests approvedRequest = new APIkeyRequests();
					approvedRequest.setApiKeyReqID(partnerPolicyRequest.getId());
					approvedRequest.setApiKeyRequestStatus(partnerPolicyRequest.getStatusCode());
					
					PartnerPolicy findByPartner_Id = partnerPolicyRepository.findByPartnerId(partnerID);
					
					approvedRequest.setPartnerApiKey(findByPartner_Id.getPolicyApiKey());
					approvedRequest.setValidityTill(findByPartner_Id.getValidToDatetime());
					listAPIkeyRequests.add(approvedRequest);
				} else {
					APIkeyRequests approvedRequest = new APIkeyRequests();
					approvedRequest.setApiKeyReqID(partnerPolicyRequest.getId());
					approvedRequest.setApiKeyRequestStatus(partnerPolicyRequest.getStatusCode());
					listAPIkeyRequests.add(approvedRequest);
				}
				response.setAPIkeyRequests(listAPIkeyRequests);
			}
		} else {
			LOGGER.info(partnerID +" : Invalied PartnerId");
			throw new PartnerDoesNotExistsException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		return response;
	}*/
	
	
	@Override
	public List<APIkeyRequests> retrieveAllApiKeyRequestsSubmittedByPartner(String partnerID) {
		List<PartnerPolicyRequest> findByPartnerId = partnerPolicyRequestRepository.findByPartnerId(partnerID);
		List<APIkeyRequests> listAPIkeyRequests = new ArrayList<APIkeyRequests>();
		PartnerPolicyRequest partnerPolicyRequest = null;
		if (!findByPartnerId.isEmpty() && findByPartnerId != null) {
			
			LOGGER.info(partnerID +" : Valied PartnerId");
			LOGGER.info(findByPartnerId.size() +" : Number of recods found");
			
			Iterator<PartnerPolicyRequest> it = findByPartnerId.iterator();
			while (it.hasNext()) {
				partnerPolicyRequest = it.next();
				if (partnerPolicyRequest.getStatusCode().equalsIgnoreCase("approved")) {
					APIkeyRequests approvedRequest = new APIkeyRequests();
					approvedRequest.setApiKeyReqID(partnerPolicyRequest.getId());
					approvedRequest.setApiKeyRequestStatus(partnerPolicyRequest.getStatusCode());
					
					PartnerPolicy findByPartner_Id = partnerPolicyRepository.findByPartnerId(partnerID);
					
					approvedRequest.setPartnerApiKey(findByPartner_Id.getPolicyApiKey());
					approvedRequest.setValidityTill(findByPartner_Id.getValidToDatetime());
					listAPIkeyRequests.add(approvedRequest);
				} else {
					APIkeyRequests approvedRequest = new APIkeyRequests();
					approvedRequest.setApiKeyReqID(partnerPolicyRequest.getId());
					approvedRequest.setApiKeyRequestStatus(partnerPolicyRequest.getStatusCode());
					listAPIkeyRequests.add(approvedRequest);
				}
			}
		} else {
			LOGGER.info(partnerID +" : Invalied PartnerId");
			throw new PartnerDoesNotExistsException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		return listAPIkeyRequests;
	}

	@Override
	public APIkeyRequests viewApiKeyRequestStatusApiKey(String partnerID, String aPIKeyReqID) {
		PartnerPolicy partnerPolicy = null;
		Optional<PartnerPolicyRequest> findById = partnerPolicyRequestRepository.findById(aPIKeyReqID);
		APIkeyRequests aPIkeyRequests = new APIkeyRequests();
		if (findById.isPresent() && findById != null) {
			LOGGER.info(aPIKeyReqID +" : Valied APIKeyReqID");
			PartnerPolicyRequest partnerPolicyRequest = findById.get();

			if (partnerPolicyRequest.getPartner().getId().equals(partnerID)) {
				LOGGER.info(partnerID +" : Valied PartnerId");
				String status_code = partnerPolicyRequest.getStatusCode();
				if (status_code.equalsIgnoreCase("Approved")) {
					aPIkeyRequests.setApiKeyReqID(partnerPolicyRequest.getId());
					aPIkeyRequests.setApiKeyRequestStatus(status_code);
					partnerPolicy = partnerPolicyRepository.findByPartnerId(partnerID);
					aPIkeyRequests.setValidityTill(partnerPolicy.getValidToDatetime());
					aPIkeyRequests.setPartnerApiKey(partnerPolicy.getPolicyApiKey());
				} else {
					LOGGER.info("APIKeyReqID is not Approved");
					throw new APIKeyReqIdStatusInProgressException(
							APIKeyReqIdStatusInProgressConstant.APIKEYREQIDSTATUSINPROGRESS.getErrorCode(),
							APIKeyReqIdStatusInProgressConstant.APIKEYREQIDSTATUSINPROGRESS.getErrorMessage());
				}

			} else {
				LOGGER.info(partnerID +" : Invalied PartnerId");
				throw new PartnerDoesNotExistsException(
						PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
						PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
			}

		} else {
			LOGGER.info(aPIKeyReqID +" : Invalied APIKeyReqID");
			throw new PartnerAPIKeyReqIDDoesNotExistException(
					PartnerAPIKeyReqDoesNotExistConstant.PARTNER_API_KET_REQ_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerAPIKeyReqDoesNotExistConstant.PARTNER_API_KET_REQ_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		return aPIkeyRequests;
	}

	
	@Override
	public DigitalCertificateResponse validateDigitalCertificate(RequestWrapper<DigitalCertificateRequest> request) {
		DigitalCertificateResponse digitalCertificateResponse = new DigitalCertificateResponse();
		
		LOGGER.info("Request Preparation for DigitalCertificate");
		
		LocalDateTime now = LocalDateTime.now();
		DigitalCertificateRequestPreparation digitalCertificateRequestPreparation = new DigitalCertificateRequestPreparation();
		digitalCertificateRequestPreparation.setData(request.getRequest().getPartnerCertificate());
		digitalCertificateRequestPreparation.setSignature(signature_value);
		digitalCertificateRequestPreparation.setTimestamp(Timestamp.valueOf(now));
		RequestWrapper<DigitalCertificateRequestPreparation> digital_request = new RequestWrapper<DigitalCertificateRequestPreparation>();
		digital_request.setId(request.getId());
		digital_request.setMetadata(request.getMetadata());
		digital_request.setVersion(request.getVersion());
		digital_request.setRequesttime(request.getRequesttime());
		digital_request.setRequest(digitalCertificateRequestPreparation);
		
		System.out.println(digital_request.toString());
		
		ResponseEntity<Map> response = null;

		List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>();
		interceptors.add(new HeaderRequestInterceptor("Cookie", "Authorization=" + response_cookies));
		restTemplate.setInterceptors(interceptors);
		
		final String uri = "https://nginxtf.southeastasia.cloudapp.azure.com/v1/signature/validate";
		HttpEntity<RequestWrapper<DigitalCertificateRequestPreparation>> certificate_entity = new HttpEntity<>(digital_request);
		response = restTemplate.postForEntity(uri, certificate_entity, Map.class);
		Map map = response.getBody();
		
		Object response_map = null;
		String status_value = null;
		String message_value = null;
		Iterator<Map.Entry<Object, Object>> itr = map.entrySet().iterator(); 
		while(itr.hasNext()) {
			Map.Entry<Object, Object> entry = itr.next();
			if(entry.getKey().equals("response")) {
				response_map = entry.getValue();
			}
		}
		ObjectMapper mapper = new ObjectMapper();
		Map<String,String> convertValue = mapper.convertValue(response_map, Map.class);
		Iterator<Entry<String, String>> iterator = convertValue.entrySet().iterator(); 
		while(iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			if(entry.getKey().equals("status")) {
				status_value = entry.getValue();
			}
			if(entry.getKey().equals("message")) {
				message_value = entry.getValue();
				}
			}
		
		if(status_value!=null && message_value!=null) {
			digitalCertificateResponse.setMessage("successfully validated partner's digital certificate");
		}
		return digitalCertificateResponse;
	}

	/*@Override
	public DigitalCertificateResponse uploadDigitalCertificate(DigitalCertificateRequest request) {
		DigitalCertificateResponse response = new DigitalCertificateResponse();

		//TODO
		// doing sign-in using "kernel-keymanager-service"

		ResponseEntity<SignatureResponseDto> validate_result = null;
		SignatureRequestDto signatureRequestDto = new SignatureRequestDto();
		signatureRequestDto.setData(request.getPartnerCertificate());
		signatureRequestDto.setApplicationId("REGISTRATION");
		signatureRequestDto.setReferenceId("REF01");
		signatureRequestDto.setTimeStamp("2018-12-10T06:12:52.994Z");
		final String uriv = "http://localhost:8092/v1/signature/validate";
		HttpEntity<SignatureRequestDto> validate_entity = new HttpEntity<SignatureRequestDto>(signatureRequestDto);
		validate_result = restTemplate.exchange(uriv, HttpMethod.POST, validate_entity, SignatureResponseDto.class);

		SignatureResponseDto signatureResponseDto = validate_result.getBody();
		String data = signatureResponseDto.getData();
		
		if(data.equalsIgnoreCase(request.getPartnerCertificate())) {
			response.setMessage("successfully uploaded partner's digital certificate");
		}
		
		return response;
	}*/

	@Override
	public LoginUserResponse userLoginInKernal(RequestWrapper<LoginUserRequest> request) {
		LoginUserResponse loginUserResponse = new LoginUserResponse();
		ResponseEntity<Map> response = null;
		final String uri = "https://nginxtf.southeastasia.cloudapp.azure.com/v1/authmanager/authenticate/useridPwd";
		HttpEntity<RequestWrapper<LoginUserRequest>> certificate_entity = new HttpEntity<>(request);
		response = restTemplate.postForEntity(uri, certificate_entity, Map.class);
		response_cookies = response.getHeaders().getFirst("Authorization");
		Map map = response.getBody();
		Object response_map = null;
		String status_value = null;
		String message_value = null;
		Iterator<Map.Entry<Object, Object>> itr = map.entrySet().iterator(); 
		while(itr.hasNext()) {
			Map.Entry<Object, Object> entry = itr.next();
			if(entry.getKey().equals("response")) {
				response_map = entry.getValue();
			}
		}
		ObjectMapper mapper = new ObjectMapper();
		Map<String,String> convertValue = mapper.convertValue(response_map, Map.class);
		
		Iterator<Entry<String, String>> iterator = convertValue.entrySet().iterator(); 
		while(iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			if(entry.getKey().equals("status")) {
				status_value = entry.getValue();
			}
			if(entry.getKey().equals("message")) {
				message_value = entry.getValue();
				}
			}
		loginUserResponse.setStatus(status_value);
		loginUserResponse.setMessage(message_value);
		return loginUserResponse;
	}

	@Override
	public SignUserResponse signUserInDigitalCertificates(RequestWrapper<SignUserRequest> request) {
		SignUserResponse signUserResponse = new SignUserResponse();
		ResponseEntity<Map> response = null;
		
		List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>();
		interceptors.add(new HeaderRequestInterceptor("Cookie", "Authorization=" + response_cookies));
		restTemplate.setInterceptors(interceptors);
		
		final String uri = "https://nginxtf.southeastasia.cloudapp.azure.com/v1/signature/sign";
		HttpEntity<RequestWrapper<SignUserRequest>> certificate_entity = new HttpEntity<>(request);
		response = restTemplate.postForEntity(uri, certificate_entity, Map.class);
		Map map = response.getBody();
		Object sign_response_map = null;
		String timestamp_value = null;
		Iterator<Map.Entry<Object, Object>> itr = map.entrySet().iterator(); 
		while(itr.hasNext()) {
			Map.Entry<Object, Object> entry = itr.next();
			if(entry.getKey().equals("response")) {
				sign_response_map = entry.getValue();
			}
		}
		ObjectMapper mapper = new ObjectMapper();
		Map<String,String> convertValue = mapper.convertValue(sign_response_map, Map.class);
		
		Iterator<Entry<String, String>> iterator = convertValue.entrySet().iterator(); 
		while(iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			if(entry.getKey().equals("signature")) {
				signature_value = entry.getValue();
			}
			if(entry.getKey().equals("timestamp")) {
				timestamp_value = entry.getValue();
			}
		}	
		signUserResponse.setSignature(signature_value);
		signUserResponse.setTimestamp(timestamp_value);
		return signUserResponse;
	}
	
}
