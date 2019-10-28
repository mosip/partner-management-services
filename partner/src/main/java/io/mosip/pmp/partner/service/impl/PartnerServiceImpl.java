package io.mosip.pmp.partner.service.impl;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import io.mosip.pmp.partner.constant.APIKeyReqIdStatusInProgressConstant;
import io.mosip.pmp.partner.constant.PartnerAPIDoesNotExistConstant;
import io.mosip.pmp.partner.constant.PartnerAPIKeyIsNotCreatedConstant;
import io.mosip.pmp.partner.constant.PartnerDoesNotExistExceptionConstant;
import io.mosip.pmp.partner.constant.PartnerIdExceptionConstant;
import io.mosip.pmp.partner.constant.PolicyGroupDoesNotExistConstant;
import io.mosip.pmp.partner.dto.APIkeyRequests;
import io.mosip.pmp.partner.dto.DigitalCertificateRequest;
import io.mosip.pmp.partner.dto.DigitalCertificateResponse;
import io.mosip.pmp.partner.dto.DownloadPartnerAPIkeyResponse;
import io.mosip.pmp.partner.dto.PartnerAPIKeyRequest;
import io.mosip.pmp.partner.dto.PartnerAPIKeyResponse;
import io.mosip.pmp.partner.dto.PartnerRequest;
import io.mosip.pmp.partner.dto.PartnerResponse;
import io.mosip.pmp.partner.dto.PartnerUpdateRequest;
import io.mosip.pmp.partner.dto.PartnersRetrieveApiKeyRequests;
import io.mosip.pmp.partner.dto.RetrievePartnerDetailsResponse;
import io.mosip.pmp.partner.dto.SignResponseDto;
import io.mosip.pmp.partner.dto.SignatureRequestDto;
import io.mosip.pmp.partner.dto.SignatureResponseDto;
import io.mosip.pmp.partner.dto.TimestampRequestDto;
import io.mosip.pmp.partner.dto.ValidatorResponseDto;
import io.mosip.pmp.partner.entity.AuthPolicy;
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
import io.mosip.pmp.partner.repository.AuthPolicyRepository;
import io.mosip.pmp.partner.repository.PartnerPolicyRepository;
import io.mosip.pmp.partner.repository.PartnerPolicyRequestRepository;
import io.mosip.pmp.partner.repository.PartnerServiceRepository;
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
	
	/* Save Partner which wants to self register */

	@Override
	public PartnerResponse savePartner(PartnerRequest request) {
		Partner partner = new Partner();
		partner.setId(PartnerUtil.createPartnerId());
		//partner.setId(partnerIdGenerator.generateId());
		PolicyGroup policyGroup = null;
		policyGroup = policyGroupRepository.findByName(request.getPolicyGroup());

		if (policyGroup != null) {
			partner.setPolicyGroupId(policyGroup.getId());
			partner.setName(request.getOrganizationName());
			partner.setAddress(request.getAddress());
			partner.setContactNo(request.getContactNumber());
			partner.setEmailId(request.getEmailId());

			partner.setIsActive(policyGroup.getIsActive());
			partner.setUserId(policyGroup.getUserId());
			partner.setCrBy(policyGroup.getCrBy());
			partner.setCrDtimes(policyGroup.getCrDtimes());
		} else {
			throw new PolicyGroupDoesNotExistException(
					PolicyGroupDoesNotExistConstant.POLICY_GROUP_DOES_NOT_EXIST.getErrorCode(),
					PolicyGroupDoesNotExistConstant.POLICY_GROUP_DOES_NOT_EXIST.getErrorMessage());
		}

		PartnerResponse partnerResponse = new PartnerResponse();
		List<Partner> list = partnerRepository.findByName(partner.getName());

		if (list.isEmpty()) {
			partnerRepository.save(partner);
			partnerResponse.setPartnerID(partner.getId());
			Boolean bul = partner.getIsActive();
			if (bul) {
				partnerResponse.setStatus("Active");
			}
			// partnerResponse.setStatus(partner.getIsActive());
		} else {
			throw new PartnerAlreadyRegisteredException(
					PartnerIdExceptionConstant.PARTNER_ALREADY_REGISTERED_EXCEPTION.getErrorCode(),
					PartnerIdExceptionConstant.PARTNER_ALREADY_REGISTERED_EXCEPTION.getErrorMessage());
		}
		return partnerResponse;
	}
	
	/* Get Partner Details as per given Partner ID */

	@Override
	public RetrievePartnerDetailsResponse getPartnerDetails(String partnerID) {
		RetrievePartnerDetailsResponse response = new RetrievePartnerDetailsResponse();
		Optional<Partner> findById = partnerRepository.findById(partnerID);
		Partner partner = null;
		Optional<PolicyGroup> findById2 = null;
		PolicyGroup policyGroup = null;

		if (findById.isPresent() && findById != null) {
			partner = findById.get();
			response.setPartnerID(partner.getId());
			response.setAddress(partner.getAddress());
			response.setContactNumber(partner.getContactNo());
			response.setEmailId(partner.getEmailId());
			response.setOrganizationName(partner.getName());

			findById2 = policyGroupRepository.findById(partner.getPolicyGroupId());
			policyGroup = findById2.get();
			response.setPolicyGroup(policyGroup.getName());
			return response;
		} else {
			throw new PartnerDoesNotExistsException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
	}

	/* Updating Partner Details as per given Partner ID and Partner Details */

	@Override
	public PartnerResponse updatePartnerDetail(PartnerUpdateRequest request, String partnerID) {
		Optional<Partner> findById = partnerRepository.findById(partnerID);
		Partner partner = null;

		if (findById.isPresent() && findById != null) {
			partner = findById.get();
			partner.setAddress(request.getAddress());
			partner.setContactNo(request.getContactNumber());
			partner.setEmailId(request.getEmailId());
			partner.setName(request.getOrganizationName());
			partnerRepository.save(partner);
			PartnerResponse partnerResponse = new PartnerResponse();
			partnerResponse.setPartnerID(partner.getId());
			Boolean bul = partner.getIsActive();
			if (bul) {
				partnerResponse.setStatus("Active");
			}
			// partnerResponse.setStatus(partner.getIsActive());
			return partnerResponse;
		} else {
			throw new PartnerDoesNotExistException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
	}

	/*
	 * API used to submit Partner api key request Need to take
	 * 1.Partner_Policy_Request Table and 2.Policy Group Table
	 * 
	 */

	@Override
	public PartnerAPIKeyResponse submitPartnerApiKeyReq(PartnerAPIKeyRequest request, String partnerID) {
		PolicyGroup policyGroup = null;
		PartnerPolicyRequest partnerPolicyRequest = null;
		policyGroup = policyGroupRepository.findByName(request.getPolicyName());

		if (policyGroup == null) {
			// TODO
			System.out.println("Need to through the exception policyGroup not exist");
		}
		
		partnerPolicyRequest = new PartnerPolicyRequest();
		String Partner_Policy_Request_Id = PartnerUtil.createPartnerId();
		partnerPolicyRequest.setId(Partner_Policy_Request_Id);
		partnerPolicyRequest.setStatusCode("in-progress");

		Optional<Partner> findById = partnerRepository.findById(partnerID);
		if (!findById.isPresent()) {
			throw new PartnerDoesNotExistsException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		Partner partner = findById.get();
		partnerPolicyRequest.setPolicyId(partner.getPolicyGroupId());
		partnerPolicyRequest.setPartner(partner);
		partnerPolicyRequest.setCrDtimes(partner.getCrDtimes());

		LocalDateTime now = LocalDateTime.now();
		partnerPolicyRequest.setRequestDatetimes(Timestamp.valueOf(now));
		partnerPolicyRequest.setRequestDetail(request.getUseCaseDescription());
		partnerPolicyRequest.setCrBy(partner.getCrBy());

		partnerPolicyRequestRepository.save(partnerPolicyRequest);
		
		// Creating Data for Mapping policy_id into auth_policy table
		// In case Auth_Policy Creation module will come that time we need to remove the below Auth_policy Code.
		
				AuthPolicy authPolicy =new AuthPolicy();
				
				String auth_policy_id = PartnerUtil.createAuthPolicyId();
				Optional<AuthPolicy> findByAuthId = authPolicyRepository.findById(auth_policy_id);
				if(findByAuthId.isPresent()) {
					//TODO
					//Log the error duplicate auth_policy_id
				}else {
					authPolicy.setId(auth_policy_id);
					authPolicy.setPolicyGroup(policyGroup);
					authPolicy.setName(policyGroup.getName());
					authPolicy.setDescr(policyGroup.getDescr());
					authPolicy.setPolicyFileId("PolicyFileId-101");
					authPolicy.setIsActive(policyGroup.getIsActive());
					authPolicy.setCrBy(policyGroup.getCrBy());
					authPolicy.setCrDtimes(policyGroup.getCrDtimes());
					
					authPolicyRepository.save(authPolicy);
				}
				
		
		// Creating Response
		
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

		if (partner_request.isPresent() && partner_request != null) {

			PartnerPolicyRequest partnerPolicyRequest = partner_request.get();
			if (partnerPolicyRequest.getPartner().getId().equals(partnerID)) {
				// && partnerPolicyRequest.getStatus_code().equalsIgnoreCase("approved")
				partnerPolicy = partnerPolicyRepository.findByPartnerId(partnerID);
				if (partnerPolicy != null) {
					downloadPartnerAPIkeyResponse.setPartnerAPIKey(partnerPolicy.getPolicyApiKey());
				} else {
					System.out.println(
							"throw Partner_API_Key is not created / request is not approved by partnetmanager");
					throw new PartnerAPIKeyIsNotCreatedException(
							PartnerAPIKeyIsNotCreatedConstant.PARTNER_API_NOT_CREATED_EXCEPTION.getErrorCode(),
							PartnerAPIKeyIsNotCreatedConstant.PARTNER_API_NOT_CREATED_EXCEPTION.getErrorMessage());
				}
			} else {
				throw new PartnerDoesNotExistsException(
						PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
						PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
			}
		} else {
			// TODO
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
				if (partnerPolicyRequest.getStatusCode().equalsIgnoreCase("approved")) {
					APIkeyRequests approvedRequest = new APIkeyRequests();
					approvedRequest.setApiKeyReqID(partnerPolicyRequest.getId());
					approvedRequest.setApiKeyRequestStatus(partnerPolicyRequest.getStatusCode());
					// TODO
					// need to get the info from partnerPolicyRepository table
					// String partnerId = partnerPolicyRequest.getPart_id();
					PartnerPolicy findByPartner_Id = partnerPolicyRepository.findByPartnerId(partnerID);
					if (findByPartner_Id == null) {
						System.out.println(partnerPolicyRequest.getId() + ": PARTNER_API_KEY is not created");
						throw new PartnerAPIKeyIsNotCreatedException(
								PartnerAPIKeyIsNotCreatedConstant.PARTNER_API_NOT_CREATED_EXCEPTION.getErrorCode(),
								PartnerAPIKeyIsNotCreatedConstant.PARTNER_API_NOT_CREATED_EXCEPTION.getErrorMessage());
					}
					approvedRequest.setPartnerApiKey(findByPartner_Id.getPolicyApiKey());
					approvedRequest.setValidityTill(LocalDate.now().plusDays(60).toString());
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
		if (findById.isPresent() && findById != null) {

			PartnerPolicyRequest partnerPolicyRequest = findById.get();

			if (partnerPolicyRequest.getPartner().getId().equals(partnerID)) {

				String status_code = partnerPolicyRequest.getStatusCode();
				if (status_code.equalsIgnoreCase("Approved")) {
					aPIkeyRequests.setApiKeyReqID(partnerPolicyRequest.getId());
					aPIkeyRequests.setApiKeyRequestStatus(status_code);
					partnerPolicy = partnerPolicyRepository.findByPartnerId(partnerID);
					// aPIkeyRequests.setValidityTill(setValidityTill(partnerPolicy.getValidToDatetime());
					aPIkeyRequests.setPartnerApiKey(partnerPolicy.getPolicyApiKey());
				} else {
					// TODO
					System.out.println("throw the exception aPIKeyReqID status is In-progress");
					// throw the exception aPIKeyReqID status is In-progress
					throw new APIKeyReqIdStatusInProgressException(
							APIKeyReqIdStatusInProgressConstant.APIKEYREQIDSTATUSINPROGRESS.getErrorCode(),
							APIKeyReqIdStatusInProgressConstant.APIKEYREQIDSTATUSINPROGRESS.getErrorMessage());
				}

			} else {
				throw new PartnerDoesNotExistsException(
						PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
						PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
			}

		} else {
			// TODO
			System.out.println("throw the exception aPIKeyReqID is not exist (\"errorCode\": \"PMS_PRT_006\",)");
			// throw the exception aPIKeyReqID is not exist ("errorCode": "PMS_PRT_006",)
			throw new PartnerAPIKeyReqIDDoesNotExistException(
					PartnerAPIDoesNotExistConstant.PARTNER_API_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerAPIDoesNotExistConstant.PARTNER_API_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		return aPIkeyRequests;
	}

	@Override
	public DigitalCertificateResponse validateDigitalCertificate(DigitalCertificateRequest request) {
		DigitalCertificateResponse response = new DigitalCertificateResponse();

		// Getting the signature from "kernel-signature-service"

		RestTemplate restTemplate = new RestTemplate();
		final String uri = "http://localhost:8092/v1/signature/sign";
		ResponseEntity<SignResponseDto> result = null;
		HttpEntity<DigitalCertificateRequest> certificate_entity = new HttpEntity<DigitalCertificateRequest>(
				request);
		result = restTemplate.exchange(uri, HttpMethod.POST, certificate_entity, SignResponseDto.class);
		SignResponseDto signResponseDto = result.getBody();

		// Validation of Partner Digital Certificate which created by MOSIP CA using "kernel-signature-service".

		ResponseEntity<ValidatorResponseDto> validate_result = null;
		TimestampRequestDto timestampRequestDto = new TimestampRequestDto();
		timestampRequestDto.setData(request.getPartnerCertificate());
		timestampRequestDto.setSignature(signResponseDto.getSignature());
		timestampRequestDto.setTimestamp(signResponseDto.getTimestamp());
		final String uriv = "http://localhost:8092/v1/signature/validate";
		HttpEntity<TimestampRequestDto> validate_entity = new HttpEntity<TimestampRequestDto>(timestampRequestDto);
		validate_result = restTemplate.exchange(uriv, HttpMethod.POST, validate_entity, ValidatorResponseDto.class);
		ValidatorResponseDto validatorResponseDto = validate_result.getBody();

		response.setMessage(validatorResponseDto.getMessage());
		
		response.setMessage("successfully validated partner's digital certificate");
		return response;
	}

	@Override
	public DigitalCertificateResponse uploadDigitalCertificate(DigitalCertificateRequest request) {
		DigitalCertificateResponse response = new DigitalCertificateResponse();

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
	}
}
