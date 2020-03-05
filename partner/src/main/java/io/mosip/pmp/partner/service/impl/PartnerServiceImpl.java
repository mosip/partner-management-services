package io.mosip.pmp.partner.service.impl;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pmp.partner.constant.APIKeyReqIdStatusInProgressConstant;
import io.mosip.pmp.partner.constant.AuthenticationFailedConstant;
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
import io.mosip.pmp.partner.dto.DigitalCertificateRequestPreparationWithPublicKey;
import io.mosip.pmp.partner.dto.DigitalCertificateResponse;
import io.mosip.pmp.partner.dto.DownloadPartnerAPIkeyResponse;
import io.mosip.pmp.partner.dto.GetPartnerDetailsResponse;
import io.mosip.pmp.partner.dto.LoginUserRequest;
import io.mosip.pmp.partner.dto.LoginUserResponse;
import io.mosip.pmp.partner.dto.PartnerAPIKeyRequest;
import io.mosip.pmp.partner.dto.PartnerAPIKeyResponse;
import io.mosip.pmp.partner.dto.PartnerRequest;
import io.mosip.pmp.partner.dto.PartnerResponse;
import io.mosip.pmp.partner.dto.PartnerUpdateRequest;
import io.mosip.pmp.partner.dto.PartnersDetails;
import io.mosip.pmp.partner.dto.PolicyIdResponse;
import io.mosip.pmp.partner.dto.RetrievePartnerDetailsResponse;
import io.mosip.pmp.partner.dto.RetrievePartnerDetailsWithNameResponse;
import io.mosip.pmp.partner.dto.SignUserRequest;
import io.mosip.pmp.partner.dto.SignUserResponse;
import io.mosip.pmp.partner.entity.Partner;
import io.mosip.pmp.partner.entity.PartnerPolicy;
import io.mosip.pmp.partner.entity.PartnerPolicyRequest;
import io.mosip.pmp.partner.entity.PolicyGroup;
import io.mosip.pmp.partner.exception.APIKeyReqIdStatusInProgressException;
import io.mosip.pmp.partner.exception.AuthenticationFailedException;
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
@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
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

	// @Autowired
	// PartnerIdGenerator<String> partnerIdGenerator;

	@Autowired
	RestTemplate restTemplate;

	String responseCookies = null;
	String signatureValue = null;

	@Value("${mosip.pmp.partnerservice.certificate.validate.appid}")
	private String appid;

	@Value("${mosip.pmp.partnerservice.certificate.validate.public.key}")
	private String publicKey;

	@Value("${mosip.pmp.partnerservice.certificate.validate.sign.key}")
	private String signKey;

	@Value("${mosip.pmp.partnerservice.certificate.validate.user.pwd.key}")
	private String userPwdKey;

	@Value("${mosip.pmp.partnerservice.certificate.validate.signature.key}")
	private String signatureKey;

	@Value("${mosip.pmp.partnerservice.certificate.validate.signature.public.key}")
	private String signaturePublicKey;

	@Override
	public PolicyIdResponse getPolicyId(String policyName) {

		PolicyIdResponse policyIdResponse = new PolicyIdResponse();
		PolicyGroup policyGroup = policyGroupRepository.findByName(policyName);
		if (policyGroup != null) {
			policyIdResponse.setPolicyId(policyGroup.getId());
		} else {
			LOGGER.info("Invalied Policy Name : " + policyName);
		}
		return policyIdResponse;
	}

	@Override
	public PartnerResponse savePartner(PartnerRequest request) {

		Partner partnerName = partnerRepository.findByName(request.getOrganizationName());
		String partId = PartnerUtil.createPartnerId();
		if (partnerName == null) {
			Partner partner = new Partner();
			partner.setId(partId);
			// partner.setId(partnerIdGenerator.generateId());
			PolicyGroup policyGroup = null;
			LOGGER.info("Validating the policy group");
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
				partner.setCrBy("System Admin");
				partner.setCrDtimes(Timestamp.valueOf(now));

				LOGGER.info(request.getOrganizationName() + " : this is unique partner");
				LOGGER.info("Saving the partner");
				partnerRepository.save(partner);
			} else {
				LOGGER.error(request.getPolicyGroup() + " : Policy Group is not availavle for the partner");
				throw new PolicyGroupDoesNotExistException(
						PolicyGroupDoesNotExistConstant.POLICY_GROUP_DOES_NOT_EXIST.getErrorCode(),
						PolicyGroupDoesNotExistConstant.POLICY_GROUP_DOES_NOT_EXIST.getErrorMessage());
			}
		} else {
			LOGGER.error(request.getOrganizationName() + " : this is duplicate partner");
			throw new PartnerAlreadyRegisteredException(
					PartnerIdExceptionConstant.PARTNER_ALREADY_REGISTERED_EXCEPTION.getErrorCode(),
					PartnerIdExceptionConstant.PARTNER_ALREADY_REGISTERED_EXCEPTION.getErrorMessage());
		}
		PartnerResponse partnerResponse = new PartnerResponse();
		partnerResponse.setPartnerId(partId);
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

		if (findByIdPartner.isPresent()) {
			LOGGER.info(partnerID + ": Partner is available");
			partner = findByIdPartner.get();
			response.setPartnerID(partner.getId());
			response.setAddress(partner.getAddress());
			response.setContactNumber(partner.getContactNo());
			response.setEmailId(partner.getEmailId());
			response.setOrganizationName(partner.getName());

			LOGGER.info("Retriving the name of policy group");
			findByIdpolicyGroup = policyGroupRepository.findById(partner.getPolicyGroupId());

			if (findByIdpolicyGroup.isPresent()) {
				policyGroup = findByIdpolicyGroup.get();
			}

			if (policyGroup != null) {
				response.setPolicyGroup(policyGroup.getName());
			}

			return response;
		} else {
			LOGGER.info(partnerID + ": Partner is not available");
			throw new PartnerDoesNotExistsException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
	}

	@Override
	public RetrievePartnerDetailsWithNameResponse getPartnerDetailsWithName(String partnerName) {
		RetrievePartnerDetailsWithNameResponse response = new RetrievePartnerDetailsWithNameResponse();
		Partner partnerByName = partnerRepository.findByName(partnerName);
		Optional<PolicyGroup> findByIdpolicyGroup = null;
		PolicyGroup policyGroup = null;
		if (partnerByName != null) {
			response.setId(partnerByName.getId());
			response.setAddress(partnerByName.getAddress());
			response.setContactNo(partnerByName.getContactNo());
			response.setCrBy(partnerByName.getCrBy());
			response.setCrDtimes(partnerByName.getCrDtimes());
			response.setEmailId(partnerByName.getEmailId());
			response.setIsActive(partnerByName.getIsActive());
			response.setName(partnerByName.getName());
			response.setUpdBy(partnerByName.getUpdBy());
			response.setUpdDtimes(partnerByName.getUpdDtimes());
			response.setUserId(partnerByName.getUserId());

			LOGGER.info("Retriving the name of policy group");
			findByIdpolicyGroup = policyGroupRepository.findById(partnerByName.getPolicyGroupId());
			if (findByIdpolicyGroup.isPresent()) {
				policyGroup = findByIdpolicyGroup.get();
			}
			if (policyGroup != null) {
				response.setPolicyGroupName(policyGroup.getName());
			}
		} else {
			LOGGER.info(partnerName + ": Partner is not available");
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
		if (findById.isPresent()) {
			LOGGER.info(partnerID + ": Partner is available");
			partner = findById.get();
			if (partner.getName().equalsIgnoreCase(request.getOrganizationName())) {
				partner.setAddress(request.getAddress());
				partner.setContactNo(request.getContactNumber());
				partner.setEmailId(request.getEmailId());
				partner.setName(request.getOrganizationName());
				partner.setUpdBy("Partner Service");
				partner.setUpdDtimes(Timestamp.valueOf(now));
				LOGGER.info("Saving the updated Partner");
				partnerRepository.save(partner);
			} else {
				LOGGER.info("Checking Name about duplicate/Unique");
				Partner findByName = partnerRepository.findByName(request.getOrganizationName());

				if (findByName == null) {
					LOGGER.info(request.getOrganizationName() + " : this is Unique name");
					partner.setAddress(request.getAddress());
					partner.setContactNo(request.getContactNumber());
					partner.setEmailId(request.getEmailId());
					partner.setName(request.getOrganizationName());
					partner.setUpdBy("Partner Service");
					partner.setUpdDtimes(Timestamp.valueOf(now));
					LOGGER.info("Saving the updated Partner");
					partnerRepository.save(partner);
				} else {
					LOGGER.info(request.getOrganizationName() + " : this is duplicate name");
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
			} else {
				partnerResponse.setStatus("De-Active");
			}
			return partnerResponse;
		} else {
			LOGGER.info(partnerID + ": Partner is not available");
			throw new PartnerDoesNotExistException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
	}

	@Override
	public PartnerAPIKeyResponse submitPartnerApiKeyReq(PartnerAPIKeyRequest request, String partnerID) {

		List<PartnerPolicyRequest> listPartnerApiKeyReq = partnerPolicyRequestRepository.findByPartnerId(partnerID);

		Optional<Partner> findByPartnerId = partnerRepository.findById(partnerID);
		if (!findByPartnerId.isPresent()) {
			LOGGER.info(partnerID + " : Invalied partnerID");
			throw new PartnerDoesNotExistsException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}

		LOGGER.info(partnerID + " : Valied Partner");
		Partner partner = findByPartnerId.get();

		if (partner.getIsActive() == false) {

			// TODO Need to implement Partner De-Activate Exception
			throw new PartnerDoesNotExistsException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}

		if (!listPartnerApiKeyReq.isEmpty()) {

			PartnerPolicyRequest partnerPolicyRequest = listPartnerApiKeyReq.get(0);

			if (partnerPolicyRequest.getStatusCode().equalsIgnoreCase("Rejected")) {
				partnerPolicyRequest.setStatusCode("in-progress");
				partnerPolicyRequestRepository.save(partnerPolicyRequest);
			}
			LOGGER.info("Preparing Response for PartnerAPIKeyReq");
			PartnerAPIKeyResponse partnerAPIKeyResponse = new PartnerAPIKeyResponse();
			partnerAPIKeyResponse.setApiRequestId(partnerPolicyRequest.getId());
			partnerAPIKeyResponse.setMessage("partnerAPIKeyRequest successfully created");
			LOGGER.info("partnerAPIKeyRequest successfully updated");
			return partnerAPIKeyResponse;

		} else {

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

			/*
			 * Optional<Partner> findByPartnerId = partnerRepository.findById(partnerID); if
			 * (!findByPartnerId.isPresent()) { LOGGER.info(partnerID +
			 * " : Invalied partnerID"); throw new PartnerDoesNotExistsException(
			 * PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.
			 * getErrorCode(),
			 * PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.
			 * getErrorMessage()); }
			 * 
			 * LOGGER.info(partnerID + " : Valied Partner"); Partner partner =
			 * findByPartnerId.get();
			 * 
			 * System.out.println(partner.getIsActive()); if(partner.getIsActive() == false)
			 * {
			 * 
			 * //TODO Need to implement Partner De-Activate Exception throw new
			 * PartnerDoesNotExistsException(
			 * PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.
			 * getErrorCode(),
			 * PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.
			 * getErrorMessage()); }
			 */

			LOGGER.info("fetching all record from partnerPolicyRequest by given partnerId");
			List<String> policyList = new ArrayList<>();
			String existingPolicyId = null;
			List<PartnerPolicyRequest> listPartnerPolicyRequest = partnerPolicyRequestRepository
					.findByPartnerId(partnerID);
			PartnerPolicyRequest partnerPolicyRequestObj = null;
			Iterator<PartnerPolicyRequest> it = listPartnerPolicyRequest.iterator();
			while (it.hasNext()) {
				partnerPolicyRequestObj = it.next();
				existingPolicyId = partnerPolicyRequestObj.getPolicyId();

				policyList.add(existingPolicyId);
			}

			Iterator<String> itPolicy = policyList.iterator();
			while (itPolicy.hasNext()) {

				if (itPolicy.next().equals(policyGroup.getId())) {
					throw new PartnerAlreadyRegisteredWithSamePolicyGroupException(
							PartnerAlreadyRegisteredWithSamePolicyGroupConstant.PARTNER_ALREADY_REG_WITH_SAME_PLICYGROUP
									.getErrorCode(),
							PartnerAlreadyRegisteredWithSamePolicyGroupConstant.PARTNER_ALREADY_REG_WITH_SAME_PLICYGROUP
									.getErrorMessage());
				}
			}

			LOGGER.info("Preparing request for partnerPolicyRequest");
			partnerPolicyRequest = new PartnerPolicyRequest();
			String partnerPolicyRequestId = PartnerUtil.createPartnerPolicyRequestId();
			partnerPolicyRequest.setId(partnerPolicyRequestId);
			partnerPolicyRequest.setStatusCode("in-progress");

			partnerPolicyRequest.setPolicyId(policyGroup.getId());
			partnerPolicyRequest.setPartner(partner);
			partnerPolicyRequest.setCrDtimes(partner.getCrDtimes());

			LocalDateTime now = LocalDateTime.now();
			partnerPolicyRequest.setRequestDatetimes(Timestamp.valueOf(now));
			partnerPolicyRequest.setRequestDetail(request.getUseCaseDescription());
			partnerPolicyRequest.setCrBy(partner.getCrBy());

			LOGGER.info("Saving request for partnerPolicyRequest");
			partnerPolicyRequestRepository.save(partnerPolicyRequest);

			LOGGER.info("Preparing Response for PartnerAPIKeyResponse");

			PartnerAPIKeyResponse partnerAPIKeyResponse = new PartnerAPIKeyResponse();
			partnerAPIKeyResponse.setApiRequestId(partnerPolicyRequest.getId());
			partnerAPIKeyResponse.setMessage("PartnerAPIKeyRequest successfully created");
			LOGGER.info("PartnerAPIKeyRequest Successfully created");
			return partnerAPIKeyResponse;
		}
	}

	@Override
	public DownloadPartnerAPIkeyResponse downloadPartnerAPIkey(String partnerID, String aPIKeyReqID) {
		PartnerPolicy partnerPolicy = null;
		DownloadPartnerAPIkeyResponse downloadPartnerAPIkeyResponse = new DownloadPartnerAPIkeyResponse();
		Optional<PartnerPolicyRequest> partnerRequest = partnerPolicyRequestRepository.findById(aPIKeyReqID);

		if (partnerRequest.isPresent()) {
			LOGGER.info(aPIKeyReqID + " : Valied APIKeyReqID");
			PartnerPolicyRequest partnerPolicyRequest = partnerRequest.get();
			if (partnerPolicyRequest.getPartner().getId().equals(partnerID)) {
				LOGGER.info(partnerID + " : Valied Partner");
				partnerPolicy = partnerPolicyRepository.findByPartnerId(partnerID);
				if (partnerPolicy != null) {
					downloadPartnerAPIkeyResponse.setPartnerAPIKey(partnerPolicy.getPolicyApiKey());
				} else {
					LOGGER.info(partnerID + " : Partner API Key is not created for given partnerID");
					throw new PartnerAPIKeyIsNotCreatedException(
							PartnerAPIKeyIsNotCreatedConstant.PARTNER_API_NOT_CREATED_EXCEPTION.getErrorCode(),
							PartnerAPIKeyIsNotCreatedConstant.PARTNER_API_NOT_CREATED_EXCEPTION.getErrorMessage());
				}
			} else {
				LOGGER.info(partnerID + " : Invalied Partner");
				throw new PartnerDoesNotExistsException(
						PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
						PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
			}
		} else {
			LOGGER.info(aPIKeyReqID + " : Invalied APIKeyReqID");
			throw new PartnerAPIKeyReqIDDoesNotExistException(
					PartnerAPIKeyReqDoesNotExistConstant.PARTNER_API_KET_REQ_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerAPIKeyReqDoesNotExistConstant.PARTNER_API_KET_REQ_DOES_NOT_EXIST_EXCEPTION
							.getErrorMessage());
		}
		return downloadPartnerAPIkeyResponse;
	}

	@Override
	public List<APIkeyRequests> retrieveAllApiKeyRequestsSubmittedByPartner(String partnerID) {
		List<PartnerPolicyRequest> findByPartnerId = partnerPolicyRequestRepository.findByPartnerId(partnerID);
		List<APIkeyRequests> listAPIkeyRequests = new ArrayList<>();
		PartnerPolicyRequest partnerPolicyRequest = null;
		if (!findByPartnerId.isEmpty()) {

			LOGGER.info(partnerID + " : Valied PartnerId");
			LOGGER.info(findByPartnerId.size() + " : Number of recods found");

			Iterator<PartnerPolicyRequest> it = findByPartnerId.iterator();
			while (it.hasNext()) {
				partnerPolicyRequest = it.next();
				if (partnerPolicyRequest.getStatusCode().equalsIgnoreCase("approved")) {
					APIkeyRequests approvedRequest = new APIkeyRequests();
					approvedRequest.setApiKeyReqID(partnerPolicyRequest.getId());
					approvedRequest.setApiKeyRequestStatus(partnerPolicyRequest.getStatusCode());

					PartnerPolicy findByPartId = partnerPolicyRepository.findByPartnerId(partnerID);

					approvedRequest.setPartnerApiKey(findByPartId.getPolicyApiKey());
					approvedRequest.setValidityTill(findByPartId.getValidToDatetime());
					listAPIkeyRequests.add(approvedRequest);
				} else {
					APIkeyRequests approvedRequest = new APIkeyRequests();
					approvedRequest.setApiKeyReqID(partnerPolicyRequest.getId());
					approvedRequest.setApiKeyRequestStatus(partnerPolicyRequest.getStatusCode());
					listAPIkeyRequests.add(approvedRequest);
				}
			}
		} else {
			LOGGER.info(partnerID + " : Invalied PartnerId");
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
		if (findById.isPresent()) {
			LOGGER.info(aPIKeyReqID + " : Valied APIKeyReqID");
			PartnerPolicyRequest partnerPolicyRequest = findById.get();

			if (partnerPolicyRequest.getPartner().getId().equals(partnerID)) {
				LOGGER.info(partnerID + " : Valied PartnerId");
				String statusCode = partnerPolicyRequest.getStatusCode();
				if (statusCode.equalsIgnoreCase("Approved")) {
					aPIkeyRequests.setApiKeyReqID(partnerPolicyRequest.getId());
					aPIkeyRequests.setApiKeyRequestStatus(statusCode);
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
				LOGGER.info(partnerID + " : Invalied PartnerId");
				throw new PartnerDoesNotExistsException(
						PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
						PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
			}

		} else {
			LOGGER.info(aPIKeyReqID + " : Invalied APIKeyReqID");
			throw new PartnerAPIKeyReqIDDoesNotExistException(
					PartnerAPIKeyReqDoesNotExistConstant.PARTNER_API_KET_REQ_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerAPIKeyReqDoesNotExistConstant.PARTNER_API_KET_REQ_DOES_NOT_EXIST_EXCEPTION
							.getErrorMessage());
		}
		return aPIkeyRequests;
	}

	@Override
	public DigitalCertificateResponse validateDigitalCertificate(RequestWrapper<DigitalCertificateRequest> request) {
		DigitalCertificateResponse digitalCertificateResponse = new DigitalCertificateResponse();

		LOGGER.info("Request Preparation for DigitalCertificate");

		DigitalCertificateRequestPreparationWithPublicKey digitalCertificateRequestPreparationWithPublicKey = new DigitalCertificateRequestPreparationWithPublicKey();
		digitalCertificateRequestPreparationWithPublicKey.setData(request.getRequest().getPartnerCertificate());
		digitalCertificateRequestPreparationWithPublicKey.setSignature(signatureValue);
		digitalCertificateRequestPreparationWithPublicKey.setPublickey(getPublicKey(appid));
		RequestWrapper<DigitalCertificateRequestPreparationWithPublicKey> digitalRequest = new RequestWrapper<>();
		digitalRequest.setId(request.getId());
		digitalRequest.setMetadata(request.getMetadata());
		digitalRequest.setVersion(request.getVersion());
		digitalRequest.setRequesttime(request.getRequesttime());
		digitalRequest.setRequest(digitalCertificateRequestPreparationWithPublicKey);

		ResponseEntity<Map> response = null;

		List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>();
		interceptors.add(new HeaderRequestInterceptor("Cookie", "Authorization=" + responseCookies));

		if (responseCookies != null) {
			restTemplate.setInterceptors(interceptors);
		} else {
			LOGGER.info("Authentication Failed");
			throw new AuthenticationFailedException(AuthenticationFailedConstant.AUTHENTICATION_FAILED.getErrorCode(),
					AuthenticationFailedConstant.AUTHENTICATION_FAILED.getErrorMessage());
		}

		HttpEntity<RequestWrapper<DigitalCertificateRequestPreparationWithPublicKey>> certificateEntity = new HttpEntity<>(
				digitalRequest);
		response = restTemplate.postForEntity(signaturePublicKey, certificateEntity, Map.class);
		Map map = response.getBody();

		Object responseMap = null;
		String statusValue = null;
		String messageValue = null;
		Iterator<Map.Entry<Object, Object>> itr = map.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<Object, Object> entry = itr.next();
			if (entry.getKey().equals("response")) {
				responseMap = entry.getValue();
			}
		}
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> convertValue = mapper.convertValue(responseMap, Map.class);
		Iterator<Entry<String, String>> iterator = convertValue.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			if (entry.getKey().equals("status")) {
				statusValue = entry.getValue();
			}
			if (entry.getKey().equals("message")) {
				messageValue = entry.getValue();
			}
		}

		if (statusValue != null && messageValue != null) {
			digitalCertificateResponse.setMessage("successfully validated partner's digital certificate");
		}
		return digitalCertificateResponse;
	}

	@Override
	public DigitalCertificateResponse uploadDigitalCertificate(RequestWrapper<DigitalCertificateRequest> request) {
		DigitalCertificateResponse digitalCertificateResponse = new DigitalCertificateResponse();

		LOGGER.info("Request Preparation for DigitalCertificate");

		LocalDateTime now = LocalDateTime.now();
		DigitalCertificateRequestPreparation digitalCertificateRequestPreparation = new DigitalCertificateRequestPreparation();
		digitalCertificateRequestPreparation.setData(request.getRequest().getPartnerCertificate());

		if (signatureValue != null) {
			digitalCertificateRequestPreparation.setSignature(signatureValue);
		} else {
			LOGGER.info("Decryption error, Sign Require");
			LOGGER.info("Signature Require");
			// TODO
			// "errorCode": "KER-CSS-102"
			// "message": "KER-FSE-003 --> data not valid (currupted,length is not valid
			// etc.); \nnested exception is javax.crypto.BadPaddingException: Decryption
			// error"
			// throw the exception
		}
		digitalCertificateRequestPreparation.setTimestamp(Timestamp.valueOf(now));
		RequestWrapper<DigitalCertificateRequestPreparation> digitalRequest = new RequestWrapper<>();
		digitalRequest.setId(request.getId());
		digitalRequest.setMetadata(request.getMetadata());
		digitalRequest.setVersion(request.getVersion());
		digitalRequest.setRequesttime(request.getRequesttime());
		digitalRequest.setRequest(digitalCertificateRequestPreparation);

		ResponseEntity<Map> response = null;

		List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
		interceptors.add(new HeaderRequestInterceptor("Cookie", "Authorization=" + responseCookies));

		if (responseCookies != null) {
			restTemplate.setInterceptors(interceptors);
		} else {
			LOGGER.info("Authentication Failed");
			throw new AuthenticationFailedException(AuthenticationFailedConstant.AUTHENTICATION_FAILED.getErrorCode(),
					AuthenticationFailedConstant.AUTHENTICATION_FAILED.getErrorMessage());
		}

		HttpEntity<RequestWrapper<DigitalCertificateRequestPreparation>> certificateEntity = new HttpEntity<>(
				digitalRequest);
		response = restTemplate.postForEntity(signatureKey, certificateEntity, Map.class);
		Map map = response.getBody();

		Object responseMap = null;
		String statusValue = null;
		String messageValue = null;
		Iterator<Map.Entry<Object, Object>> itr = map.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<Object, Object> entry = itr.next();
			if (entry.getKey().equals("response")) {
				responseMap = entry.getValue();
			}
		}
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> convertValue = mapper.convertValue(responseMap, Map.class);
		Iterator<Entry<String, String>> iterator = convertValue.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			if (entry.getKey().equals("status")) {
				statusValue = entry.getValue();
			}
			if (entry.getKey().equals("message")) {
				messageValue = entry.getValue();
			}
		}

		if (statusValue != null && messageValue != null) {
			digitalCertificateResponse.setMessage("successfully validated partner's digital certificate");
		}
		return digitalCertificateResponse;
	}

	@Override
	public LoginUserResponse userLoginInKernal(RequestWrapper<LoginUserRequest> request) {
		LoginUserResponse loginUserResponse = new LoginUserResponse();
		ResponseEntity<Map> response = null;

		HttpEntity<RequestWrapper<LoginUserRequest>> certificateEntity = new HttpEntity<>(request);
		response = restTemplate.postForEntity(userPwdKey, certificateEntity, Map.class);
		responseCookies = response.getHeaders().getFirst("Authorization");
		Map map = response.getBody();
		Object responseMap = null;
		String statusValue = null;
		String messageValue = null;
		Iterator<Map.Entry<Object, Object>> itr = map.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<Object, Object> entry = itr.next();
			if (entry.getKey().equals("response")) {
				responseMap = entry.getValue();
			}
		}
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> convertValue = mapper.convertValue(responseMap, Map.class);

		Iterator<Entry<String, String>> iterator = convertValue.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			if (entry.getKey().equals("status")) {
				statusValue = entry.getValue();
			}
			if (entry.getKey().equals("message")) {
				statusValue = entry.getValue();
			}
		}
		loginUserResponse.setStatus(statusValue);
		loginUserResponse.setMessage(messageValue);
		return loginUserResponse;
	}

	@Override
	public SignUserResponse signUserInDigitalCertificates(RequestWrapper<SignUserRequest> request) {

		SignUserResponse signUserResponse = new SignUserResponse();
		ResponseEntity<Map> response = null;

		List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
		interceptors.add(new HeaderRequestInterceptor("Cookie", "Authorization=" + responseCookies));
		if (responseCookies != null) {
			restTemplate.setInterceptors(interceptors);
		} else {
			LOGGER.info("Authentication Failed");
			throw new AuthenticationFailedException(AuthenticationFailedConstant.AUTHENTICATION_FAILED.getErrorCode(),
					AuthenticationFailedConstant.AUTHENTICATION_FAILED.getErrorMessage());
		}

		HttpEntity<RequestWrapper<SignUserRequest>> certificateEntity = new HttpEntity<>(request);
		response = restTemplate.postForEntity(signKey, certificateEntity, Map.class);
		Map map = response.getBody();
		Object signResponseMap = null;
		String timestampValue = null;
		Iterator<Map.Entry<Object, Object>> itr = map.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<Object, Object> entry = itr.next();
			if (entry.getKey().equals("response")) {
				signResponseMap = entry.getValue();
			}
		}
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> convertValue = mapper.convertValue(signResponseMap, Map.class);

		Iterator<Entry<String, String>> iterator = convertValue.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			if (entry.getKey().equals("signature")) {
				signatureValue = entry.getValue();
			}
			if (entry.getKey().equals("timestamp")) {
				timestampValue = entry.getValue();
			}
		}
		signUserResponse.setSignature(signatureValue);
		signUserResponse.setTimestamp(timestampValue);
		return signUserResponse;
	}

	public String getPublicKey(String applicationId) {

		LocalDateTime now = LocalDateTime.now();
		Timestamp timeStamps = Timestamp.valueOf(now);
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(timeStamps);

		List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
		interceptors.add(new HeaderRequestInterceptor("Cookie", "Authorization=" + responseCookies));
		restTemplate.setInterceptors(interceptors);

		final String uri = publicKey + applicationId + "?timeStamp=" + timeStamp;
		ResponseEntity<Map> response = null;
		response = restTemplate.getForEntity(uri, Map.class);
		Map map = response.getBody();
		Object keyResponseMap = null;
		String publicKeyValue = null;
		String issuedAtValue = null;
		String expiryAtValue = null;
		Iterator<Map.Entry<Object, Object>> itr = map.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<Object, Object> entry = itr.next();
			if (entry.getKey().equals("response")) {
				keyResponseMap = entry.getValue();
			}
		}

		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> convertValue = mapper.convertValue(keyResponseMap, Map.class);

		Iterator<Entry<String, String>> iterator = convertValue.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			if (entry.getKey().equals("publicKey")) {
				publicKeyValue = entry.getValue();
			}
			if (entry.getKey().equals("issuedAt")) {
				issuedAtValue = entry.getValue();
			}
			if (entry.getKey().equals("expiryAt")) {
				expiryAtValue = entry.getValue();
			}
		}

		DateTimeFormatter expiryAtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		//LocalDateTime expiryAtDateTime = LocalDateTime.parse(expiryAtValue, expiryAtFormatter);

		DateTimeFormatter issuedAtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		//LocalDateTime issuedAtDateTime = LocalDateTime.parse(issuedAtValue, issuedAtFormatter);

		// TODO
		// need to validate the expiry

		return publicKeyValue;
	}

	@Override
	public GetPartnerDetailsResponse getPartnerDetails() {
		GetPartnerDetailsResponse partnersResponse = new GetPartnerDetailsResponse();
		List<PartnersDetails> partners = new ArrayList<>();

		List<Partner> listPart = null;
		listPart = partnerRepository.findAll();
		Partner partner = null;
		if (listPart == null) {
			throw new PartnerDoesNotExistException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		Iterator<Partner> partnerIterat = listPart.iterator();
		while (partnerIterat.hasNext()) {
			PartnersDetails partnersDetails = new PartnersDetails();
			partner = partnerIterat.next();

			partnersDetails.setPartnerID(partner.getId());
			partnersDetails.setStatus(partner.getIsActive() == true ? "Active" : "De-Active");
			partnersDetails.setOrganizationName(partner.getName());
			partnersDetails.setContactNumber(partner.getContactNo());
			partnersDetails.setEmailId(partner.getEmailId());
			partnersDetails.setAddress(partner.getAddress());

			partnersDetails.setCreatedBy(partner.getCrBy());
			partnersDetails.setCreatedDateTime(partner.getCrDtimes().toString());
			partnersDetails.setUpdatedBy(partner.getUpdBy());

			// partnersDetails.setUpdatedDateTime(partner.getUpdDtimes()==null ? "NOT YET
			// UPDATED" : partner.getUpdDtimes().toString());

			partnersDetails.setUpdatedDateTime(partner.getUpdDtimes() + "");

			String statusCode = null;

			if (!partnerPolicyRequestRepository.findByPartnerId(partner.getId()).isEmpty()) {
				statusCode = partnerPolicyRequestRepository.findByPartnerId(partner.getId()).get(0).getStatusCode();

			} else {
				statusCode = "YET TO SUBMIT";
			}

			partnersDetails.setApiKeyRequestStatus(statusCode);

			Optional<PolicyGroup> findByIdpolicyGroup = policyGroupRepository.findById(partner.getPolicyGroupId());
			if (findByIdpolicyGroup.isPresent()) {
				partnersDetails.setPolicyName(findByIdpolicyGroup.get().getName());
			}
			partners.add(partnersDetails);
		}
		partnersResponse.setPartners(partners);
		return partnersResponse;
	}
}
