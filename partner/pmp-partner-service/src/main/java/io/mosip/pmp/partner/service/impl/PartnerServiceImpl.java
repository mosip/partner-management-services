package io.mosip.pmp.partner.service.impl;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.pmp.authdevice.dto.MosipUserDto;
import io.mosip.pmp.authdevice.dto.PageResponseDto;
import io.mosip.pmp.authdevice.dto.UserRegistrationRequestDto;
import io.mosip.pmp.authdevice.util.dto.Type;
import io.mosip.pmp.common.dto.SearchDto;
import io.mosip.pmp.common.dto.SearchFilter;
import io.mosip.pmp.common.helper.SearchHelper;
import io.mosip.pmp.common.util.MapperUtils;
import io.mosip.pmp.keycloak.impl.KeycloakImpl;
import io.mosip.pmp.partner.constant.APIKeyReqIdStatusInProgressConstant;
import io.mosip.pmp.partner.constant.ApiAccessibleExceptionConstant;
import io.mosip.pmp.partner.constant.EmailIdExceptionConstant;
import io.mosip.pmp.partner.constant.EventType;
import io.mosip.pmp.partner.constant.PartnerAPIKeyIsNotCreatedConstant;
import io.mosip.pmp.partner.constant.PartnerAPIKeyReqDoesNotExistConstant;
import io.mosip.pmp.partner.constant.PartnerDoesNotExistExceptionConstant;
import io.mosip.pmp.partner.constant.PartnerExceptionConstants;
import io.mosip.pmp.partner.constant.PartnerIdExceptionConstant;
import io.mosip.pmp.partner.constant.PartnerTypeDoesNotExistConstant;
import io.mosip.pmp.partner.constant.PolicyGroupDoesNotExistConstant;
import io.mosip.pmp.partner.core.RequestWrapper;
import io.mosip.pmp.partner.dto.APIkeyRequests;
import io.mosip.pmp.partner.dto.AddContactRequestDto;
import io.mosip.pmp.partner.dto.CACertificateRequestDto;
import io.mosip.pmp.partner.dto.CACertificateResponseDto;
import io.mosip.pmp.partner.dto.DownloadPartnerAPIkeyResponse;
import io.mosip.pmp.partner.dto.ExtractorDto;
import io.mosip.pmp.partner.dto.ExtractorProviderDto;
import io.mosip.pmp.partner.dto.ExtractorsDto;
import io.mosip.pmp.partner.dto.PartnerAPIKeyRequest;
import io.mosip.pmp.partner.dto.PartnerAPIKeyResponse;
import io.mosip.pmp.partner.dto.PartnerCertDownloadRequestDto;
import io.mosip.pmp.partner.dto.PartnerCertDownloadResponeDto;
import io.mosip.pmp.partner.dto.PartnerCertificateRequestDto;
import io.mosip.pmp.partner.dto.PartnerCertificateResponseDto;
import io.mosip.pmp.partner.dto.PartnerCredentialTypePolicyDto;
import io.mosip.pmp.partner.dto.PartnerRequest;
import io.mosip.pmp.partner.dto.PartnerResponse;
import io.mosip.pmp.partner.dto.PartnerSearchDto;
import io.mosip.pmp.partner.dto.PartnerSearchResponseDto;
import io.mosip.pmp.partner.dto.PartnerUpdateRequest;
import io.mosip.pmp.partner.dto.PolicyIdResponse;
import io.mosip.pmp.partner.dto.RetrievePartnerDetailsResponse;
import io.mosip.pmp.partner.dto.RetrievePartnerDetailsWithNameResponse;
import io.mosip.pmp.partner.dto.UploadCertificateRequestDto;
import io.mosip.pmp.partner.entity.AuthPolicy;
import io.mosip.pmp.partner.entity.BiometricExtractorProvider;
import io.mosip.pmp.partner.entity.Partner;
import io.mosip.pmp.partner.entity.PartnerContact;
import io.mosip.pmp.partner.entity.PartnerH;
import io.mosip.pmp.partner.entity.PartnerHPK;
import io.mosip.pmp.partner.entity.PartnerPolicy;
import io.mosip.pmp.partner.entity.PartnerPolicyCredentialType;
import io.mosip.pmp.partner.entity.PartnerPolicyCredentialTypePK;
import io.mosip.pmp.partner.entity.PartnerPolicyRequest;
import io.mosip.pmp.partner.entity.PartnerType;
import io.mosip.pmp.partner.entity.PolicyGroup;
import io.mosip.pmp.partner.exception.APIKeyReqIdStatusInProgressException;
import io.mosip.pmp.partner.exception.ApiAccessibleException;
import io.mosip.pmp.partner.exception.EmailIdAlreadyExistException;
import io.mosip.pmp.partner.exception.PartnerAPIKeyIsNotCreatedException;
import io.mosip.pmp.partner.exception.PartnerAPIKeyReqIDDoesNotExistException;
import io.mosip.pmp.partner.exception.PartnerAlreadyRegisteredException;
import io.mosip.pmp.partner.exception.PartnerDoesNotExistException;
import io.mosip.pmp.partner.exception.PartnerDoesNotExistsException;
import io.mosip.pmp.partner.exception.PartnerServiceException;
import io.mosip.pmp.partner.exception.PartnerTypeDoesNotExistException;
import io.mosip.pmp.partner.exception.PolicyGroupDoesNotExistException;
import io.mosip.pmp.partner.repository.AuthPolicyRepository;
import io.mosip.pmp.partner.repository.BiometricExtractorProviderRepository;
import io.mosip.pmp.partner.repository.PartnerContactRepository;
import io.mosip.pmp.partner.repository.PartnerHRepository;
import io.mosip.pmp.partner.repository.PartnerPolicyCredentialTypeRepository;
import io.mosip.pmp.partner.repository.PartnerPolicyRepository;
import io.mosip.pmp.partner.repository.PartnerPolicyRequestRepository;
import io.mosip.pmp.partner.repository.PartnerServiceRepository;
import io.mosip.pmp.partner.repository.PartnerTypeRepository;
import io.mosip.pmp.partner.repository.PolicyGroupRepository;
import io.mosip.pmp.partner.service.PartnerService;
import io.mosip.pmp.partner.util.PartnerUtil;
import io.mosip.pmp.partner.util.RestUtil;
import io.mosip.pmp.partner.util.SearchHelper;
import io.mosip.pmp.partner.util.WebSubPublisher;

/**
 * @author sanjeev.shrivastava
 * @since 1.2.0
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

	@Autowired
	PartnerTypeRepository partnerTypeRepository;

	@Autowired
	PartnerContactRepository partnerContactRepository;

	@Autowired
	PartnerHRepository partnerHRepository;

	@Autowired
	BiometricExtractorProviderRepository extractorProviderRepository;

	@Autowired
	PartnerPolicyCredentialTypeRepository partnerCredentialTypePolicyRepo;

	@Autowired
	RestUtil restUtil;

	@Autowired
	KeycloakImpl keycloakImpl;

	@Autowired
	private Environment environment;

	@Autowired
	SearchHelper partnerSearchHelper;
	
	@Autowired
	private WebSubPublisher webSubPublisher;

	@Autowired
	private ObjectMapper mapper;

	@Value("${pmp.partner.valid.email.address.regex}")
	private String emailRegex;

	@Value("${pmp.partner.partnerId.max.length}")
	private int partnerIdMaxLength;

	@Value("${pmp.bioextractors.required.partner.types}")
	private String biometricExtractorsRequiredPartnerTypes;

	@Value("${pmp.allowed.credential.types}")
	private String allowedCredentialTypes;

	@Value("${policy.credential.type.mapping.allowed.partner.types}")
	private String credentialTypesRequiredPartnerTypes;

	@Value("${application.id:PMS}")
	private String applicationId;

	private static final String ERRORS = "errors";

	private static final String ERRORCODE = "errorCode";

	private static final String ERRORMESSAGE = "message";

	private static final String APPROVEDSTATUS = "Approved";

	private static final String ALL = "all";

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
		if (!emailValidator(request.getEmailId())) {
			LOGGER.error(request.getEmailId() + " : this is invalid email");
			throw new EmailIdAlreadyExistException(EmailIdExceptionConstant.INVALID_EMAIL_ID_EXCEPTION.getErrorCode(),
					EmailIdExceptionConstant.INVALID_EMAIL_ID_EXCEPTION.getErrorMessage());

		}
		if (request.getPartnerId().length() > partnerIdMaxLength) {
			LOGGER.error(request.getOrganizationName() + " : this is duplicate partner");
			throw new PartnerServiceException(PartnerExceptionConstants.PARTNER_ID_LENGTH_EXCEPTION.getErrorCode(),
					PartnerExceptionConstants.PARTNER_ID_LENGTH_EXCEPTION.getErrorMessage() + partnerIdMaxLength);
		}
		Optional<Partner> partnerById = partnerRepository.findById(request.getPartnerId());
		if (!partnerById.isEmpty()) {
			LOGGER.error(request.getOrganizationName() + " : this is duplicate partner");
			throw new PartnerAlreadyRegisteredException(
					PartnerIdExceptionConstant.PARTNER_ALREADY_REGISTERED_WITH_ID_EXCEPTION.getErrorCode(),
					PartnerIdExceptionConstant.PARTNER_ALREADY_REGISTERED_WITH_ID_EXCEPTION.getErrorMessage());
		}
		Partner partnerFromDb = findPartnerByEmail(request.getEmailId());
		if (partnerFromDb != null) {
			LOGGER.error(request.getEmailId() + " : this is duplicate email");
			throw new EmailIdAlreadyExistException(
					EmailIdExceptionConstant.EMAIL_ALREADY_EXISTS_EXCEPTION.getErrorCode(),
					EmailIdExceptionConstant.EMAIL_ALREADY_EXISTS_EXCEPTION.getErrorMessage());

		}
		Optional<PartnerType> partnerType = partnerTypeRepository.findById(request.getPartnerType());
		if (partnerType.isEmpty()) {
			LOGGER.error(request.getPolicyGroup() + " : Policy Group is not availavle for the partner");
			throw new PartnerTypeDoesNotExistException(
					PartnerTypeDoesNotExistConstant.PARTNER_TYPE_DOES_NOT_EXIST.getErrorCode(),
					PartnerTypeDoesNotExistConstant.PARTNER_TYPE_DOES_NOT_EXIST.getErrorMessage());
		}
		PolicyGroup policyGroup = null;
		if (partnerType.get().getIsPolicyRequired()) {
			LOGGER.info("Validating the policy group");
			policyGroup = policyGroupRepository.findByName(request.getPolicyGroup());
			if (policyGroup == null) {
				LOGGER.error(request.getPolicyGroup() + " : Policy Group is not availavle for the partner");
				throw new PolicyGroupDoesNotExistException(
						PolicyGroupDoesNotExistConstant.POLICY_GROUP_DOES_NOT_EXIST.getErrorCode(),
						PolicyGroupDoesNotExistConstant.POLICY_GROUP_DOES_NOT_EXIST.getErrorMessage());
			}
		}
		Partner partner = new Partner();
		partner.setId(request.getPartnerId());
		partner.setPolicyGroupId(policyGroup != null ? policyGroup.getId() : null);
		partner.setName(request.getOrganizationName());
		partner.setAddress(request.getAddress());
		partner.setContactNo(request.getContactNumber());
		partner.setPartnerTypeCode(request.getPartnerType());
		partner.setEmailId(request.getEmailId());
		partner.setIsActive(true);
		partner.setUserId(request.getPartnerId());
		partner.setCrBy(getUser());
		partner.setApprovalStatus("Activated");
		partner.setCrDtimes(Timestamp.valueOf(LocalDateTime.now()));
		RegisterUser(partner);
		partnerRepository.save(partner);
		saveToPartnerH(partner);
		PartnerResponse partnerResponse = new PartnerResponse();
		partnerResponse.setPartnerId(partner.getId());
		partnerResponse.setStatus("Active");
		return partnerResponse;
	}

	private MosipUserDto RegisterUser(Partner partner) {
		UserRegistrationRequestDto userRegistrationRequestDto = new UserRegistrationRequestDto();
		userRegistrationRequestDto.setAppId("PARTNER_MANAGEMENT");
		userRegistrationRequestDto.setContactNo(partner.getContactNo());
		userRegistrationRequestDto.setEmailID(partner.getEmailId());
		userRegistrationRequestDto.setFirstName(partner.getName());
		userRegistrationRequestDto.setRole(partner.getPartnerTypeCode().toUpperCase());
		userRegistrationRequestDto.setUserPassword(partner.getId());
		userRegistrationRequestDto.setUserName(partner.getId().toLowerCase());
		return keycloakImpl.registerUser(userRegistrationRequestDto);
	}

	@Override
	public RetrievePartnerDetailsResponse getPartnerDetails(String partnerID) {
		RetrievePartnerDetailsResponse response = new RetrievePartnerDetailsResponse();
		Optional<Partner> findByIdPartner = partnerRepository.findById(partnerID);
		Partner partner = null;
		Optional<PolicyGroup> findByIdpolicyGroup = null;

		if (findByIdPartner.isPresent()) {
			LOGGER.info(partnerID + ": Partner is available");
			partner = findByIdPartner.get();
			response.setPartnerID(partner.getId());
			response.setAddress(partner.getAddress());
			response.setContactNumber(partner.getContactNo());
			response.setEmailId(partner.getEmailId());
			response.setOrganizationName(partner.getName());
			if (findByIdPartner.get().getPolicyGroupId() != null) {
				LOGGER.info("Retriving the name of policy group");
				findByIdpolicyGroup = policyGroupRepository.findById(partner.getPolicyGroupId());

				if (findByIdpolicyGroup.isPresent() && findByIdpolicyGroup.get() != null) {
					response.setPolicyGroup(findByIdpolicyGroup.get().getName());
				}
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
		if (partnerByName == null) {
			LOGGER.info(partnerName + ": Partner is not available");
			throw new PartnerDoesNotExistsException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		String policyGroupName = null;
		if (partnerByName.getPolicyGroupId() != null) {
			Optional<PolicyGroup> findByIdpolicyGroup = policyGroupRepository
					.findById(partnerByName.getPolicyGroupId());
			policyGroupName = findByIdpolicyGroup.get().getName();
		}
		if (partnerByName != null) {
			response.setId(partnerByName.getId());
			response.setAddress(partnerByName.getAddress());
			response.setContactNo(partnerByName.getContactNo());
			response.setCrBy(partnerByName.getCrBy());
			response.setCrDtimes(partnerByName.getCrDtimes());
			response.setEmailId(partnerByName.getEmailId());
			response.setIsActive(partnerByName.getIsActive());
			response.setName(partnerByName.getName());
			response.setUpdBy(getUser());
			response.setUpdDtimes(partnerByName.getUpdDtimes());
			response.setUserId(partnerByName.getUserId());
			response.setPolicyGroupName(policyGroupName);
		}
		return response;
	}

	@Override
	public PartnerResponse updatePartnerDetail(PartnerUpdateRequest request, String partnerID) {
		Optional<Partner> partnerFromDb = partnerRepository.findById(partnerID);
		if (partnerFromDb.isEmpty()) {
			LOGGER.info(partnerID + ": Partner is not available");
			throw new PartnerDoesNotExistException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		Partner partner = partnerFromDb.get();
		LocalDateTime now = LocalDateTime.now();
		partner.setAddress(request.getAddress());
		partner.setContactNo(request.getContactNumber());
		partner.setUpdBy(getUser());
		partner.setUpdDtimes(Timestamp.valueOf(now));
		partnerRepository.save(partner);
		saveToPartnerH(partner);
		PartnerResponse partnerResponse = new PartnerResponse();
		partnerResponse.setPartnerId(partner.getId());
		Boolean isPartnerActive = partner.getIsActive();
		if (isPartnerActive) {
			partnerResponse.setStatus("Active");
		} else {
			partnerResponse.setStatus("De-Active");
		}
		return partnerResponse;
	}

	/**
	 * 
	 * @param partnerId
	 * @param emailId
	 * @param address
	 * @param contactNo
	 */
	public String createAndUpdateContactDetails(AddContactRequestDto request, String partnerId) {
		if (!emailValidator(request.getEmailId())) {
			LOGGER.error(request.getEmailId() + " : this is invalid email");
			throw new EmailIdAlreadyExistException(EmailIdExceptionConstant.INVALID_EMAIL_ID_EXCEPTION.getErrorCode(),
					EmailIdExceptionConstant.INVALID_EMAIL_ID_EXCEPTION.getErrorMessage());
		}
		PartnerContact contactsFromDb = partnerContactRepository.findByPartnerAndEmail(partnerId, request.getEmailId());
		String resultMessage;
		if (contactsFromDb != null) {
			contactsFromDb.setAddress(request.getAddress());
			contactsFromDb.setContactNo(request.getContactNumber());
			contactsFromDb.setIsActive(request.getIs_Active());
			contactsFromDb.setUpdBy(getUser());
			contactsFromDb.setUpdDtimes(LocalDateTime.now());
			resultMessage = "Contacts details updated successfully.";
		} else {
			Optional<Partner> partnerFromDb = partnerRepository.findById(partnerId);
			if (partnerFromDb.isEmpty()) {
				LOGGER.info(partnerId + ": Partner is not available");
				throw new PartnerDoesNotExistException(
						PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
						PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
			}
			contactsFromDb = new PartnerContact();
			contactsFromDb.setId(PartnerUtil.createPartnerId());
			contactsFromDb.setAddress(request.getAddress());
			contactsFromDb.setContactNo(request.getContactNumber());
			contactsFromDb.setCrBy(getUser());
			contactsFromDb.setCrDtimes(LocalDateTime.now());
			contactsFromDb.setPartner(partnerFromDb.get());
			contactsFromDb.setEmailId(request.getEmailId());
			contactsFromDb.setIsActive(request.getIs_Active());
			resultMessage = "Contacts details added successfully.";
		}

		partnerContactRepository.save(contactsFromDb);
		return resultMessage;
	}

	/**
	 * 
	 * @param partner
	 */
	public void saveToPartnerH(Partner partner) {
		PartnerH partnerHistory = new PartnerH();
		PartnerHPK partnerHPK = new PartnerHPK();
		partnerHPK.setId(PartnerUtil.createPartnerId());
		partnerHPK.setEffDtimes(new Date());
		LocalDateTime now = LocalDateTime.now();
		partnerHistory.setPolicyGroupId(partner.getPolicyGroupId());
		partnerHistory.setName(partner.getName());
		partnerHistory.setAddress(partner.getAddress());
		partnerHistory.setContactNo(partner.getContactNo());
		partnerHistory.setPartnerTypeCode(partner.getPartnerTypeCode());
		partnerHistory.setApprovalStatus(partner.getApprovalStatus());
		partnerHistory.setEmailId(partner.getEmailId());
		partnerHistory.setIsActive(partner.getIsActive());
		partnerHistory.setUserId(partner.getUserId());
		partnerHistory.setCrBy(partner.getCrBy());
		partnerHistory.setCrDtimes(Timestamp.valueOf(now));
		partnerHistory.setId(partnerHPK);
		partnerHRepository.save(partnerHistory);
	}

	@Override
	public PartnerAPIKeyResponse submitPartnerApiKeyReq(PartnerAPIKeyRequest request, String partnerID) {
		Optional<Partner> partnerFromDb = partnerRepository.findById(partnerID);
		if (partnerFromDb.isEmpty()) {
			throw new PartnerDoesNotExistsException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		if (partnerFromDb.get().getIsActive() == false) {
			throw new PartnerDoesNotExistsException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		AuthPolicy authPolicyFromDb = authPolicyRepository
				.findByPolicyGroupAndName(partnerFromDb.get().getPolicyGroupId(), request.getPolicyName());
		if (authPolicyFromDb == null) {
			LOGGER.info(request.getPolicyName() + ": Invalied Policy Group");
			throw new PolicyGroupDoesNotExistException(
					PolicyGroupDoesNotExistConstant.POLICY_GROUP_DOES_NOT_EXIST.getErrorCode(),
					PolicyGroupDoesNotExistConstant.POLICY_GROUP_DOES_NOT_EXIST.getErrorMessage());
		}
		PartnerPolicyRequest partnerPolicyRequest = new PartnerPolicyRequest();
		partnerPolicyRequest.setCrBy(getUser());
		partnerPolicyRequest.setCrDtimes(Timestamp.valueOf(LocalDateTime.now()));
		partnerPolicyRequest.setId(PartnerUtil.createPartnerPolicyRequestId());
		partnerPolicyRequest.setPartner(partnerFromDb.get());
		partnerPolicyRequest.setPolicyId(authPolicyFromDb.getId());
		partnerPolicyRequest.setRequestDatetimes(Timestamp.valueOf(LocalDateTime.now()));
		partnerPolicyRequest.setRequestDetail(request.getUseCaseDescription());
		partnerPolicyRequest.setStatusCode("In-Progress");
		partnerPolicyRequestRepository.save(partnerPolicyRequest);
		PartnerAPIKeyResponse partnerAPIKeyResponse = new PartnerAPIKeyResponse();
		partnerAPIKeyResponse.setApiRequestId(partnerPolicyRequest.getId());
		partnerAPIKeyResponse.setMessage("PartnerAPIKeyRequest successfully created");
		LOGGER.info("PartnerAPIKeyRequest Successfully created");
		return partnerAPIKeyResponse;
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

	/**
	 * 
	 * @return
	 */
	public String getUser() {
		if (Objects.nonNull(SecurityContextHolder.getContext())
				&& Objects.nonNull(SecurityContextHolder.getContext().getAuthentication())
				&& Objects.nonNull(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				&& SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof AuthUserDetails) {
			return ((AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
					.getUserId();
		} else {
			return "system";
		}
	}

	/**
	 * 
	 * @param email
	 * @return
	 */
	private Partner findPartnerByEmail(String email) {
		return partnerRepository.findByEmailId(email);
	}

	/**
	 * 
	 * @param email
	 * @return
	 */
	public boolean emailValidator(String email) {
		return email.matches(emailRegex);
	}

	@SuppressWarnings("unchecked")
	@Override
	public CACertificateResponseDto uploadCACertificate(CACertificateRequestDto caCertRequestDto)
			throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {
		RequestWrapper<CACertificateRequestDto> request = new RequestWrapper<>();
		request.setRequest(caCertRequestDto);
		request.setRequesttime(LocalDateTime.now());
		CACertificateResponseDto responseObject = null;
		Map<String, Object> uploadApiResponse = restUtil.postApi(
				environment.getProperty("pmp.ca.certificaticate.upload.rest.uri"), null, "", "",
				MediaType.APPLICATION_JSON, request, Map.class);
		LOGGER.info("Calling the upload ca certificate api");
		responseObject = mapper.readValue(mapper.writeValueAsString(uploadApiResponse.get("response")),
				CACertificateResponseDto.class);
		if (responseObject == null && uploadApiResponse.containsKey(ERRORS)) {
			List<Map<String, Object>> certServiceErrorList = (List<Map<String, Object>>) uploadApiResponse.get(ERRORS);
			if (!certServiceErrorList.isEmpty()) {
				throw new ApiAccessibleException(certServiceErrorList.get(0).get(ERRORCODE).toString(),
						certServiceErrorList.get(0).get(ERRORMESSAGE).toString());
			} else {
				throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
						ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage());
			}
		}
		if (responseObject == null) {
			throw new ApiAccessibleException(ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorCode(),
					ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorMessage());
		}
		notify(caCertRequestDto.getCertificateData(), caCertRequestDto.getPartnerDomain());
		return responseObject;
	}

	@SuppressWarnings("unchecked")
	@Override
	public PartnerCertificateResponseDto uploadPartnerCertificate(PartnerCertificateRequestDto partnerCertRequesteDto)
			throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {
		Optional<Partner> partnerFromDb = partnerRepository.findById(partnerCertRequesteDto.getPartnerId());
		if (partnerFromDb.isEmpty()) {
			throw new PartnerDoesNotExistsException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		RequestWrapper<PartnerCertificateRequestDto> request = new RequestWrapper<>();
		request.setRequest(partnerCertRequesteDto);
		PartnerCertificateResponseDto responseObject = null;
		Map<String, Object> uploadApiResponse = restUtil.postApi(
				environment.getProperty("pmp.partner.certificaticate.upload.rest.uri"), null, "", "",
				MediaType.APPLICATION_JSON, request, Map.class);
		responseObject = mapper.readValue(mapper.writeValueAsString(uploadApiResponse.get("response")),
				PartnerCertificateResponseDto.class);
		if (responseObject == null && uploadApiResponse.containsKey(ERRORS)) {
			List<Map<String, Object>> certServiceErrorList = (List<Map<String, Object>>) uploadApiResponse.get(ERRORS);
			if (!certServiceErrorList.isEmpty()) {
				throw new ApiAccessibleException(certServiceErrorList.get(0).get(ERRORCODE).toString(),
						certServiceErrorList.get(0).get(ERRORMESSAGE).toString());
			} else {
				throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
						ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage());
			}
		}
		if (responseObject == null) {
			throw new ApiAccessibleException(ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorCode(),
					ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorMessage());
		}

		uploadOtherDomainCertificate(responseObject.getSignedCertificateData(), partnerCertRequesteDto.getPartnerId());
		Partner updateObject = partnerFromDb.get();
		updateObject.setUpdBy(getUser());
		updateObject.setUpdDtimes(Timestamp.valueOf(LocalDateTime.now()));
		updateObject.setCertificateAlias(responseObject.getCertificateId());
		partnerRepository.save(updateObject);
		notify(partnerCertRequesteDto.getPartnerId());
		return responseObject;
	}

	@SuppressWarnings("unchecked")
	private void uploadOtherDomainCertificate(String certData, String partnerId) {
		RequestWrapper<UploadCertificateRequestDto> request = new RequestWrapper<>();
		UploadCertificateRequestDto requestDto = new UploadCertificateRequestDto();
		CACertificateResponseDto responseObject = null;
		requestDto.setApplicationId(applicationId);
		requestDto.setCertificateData(certData);
		requestDto.setReferenceId(partnerId);
		request.setRequest(requestDto);
		Map<String, Object> uploadApiResponse = restUtil.postApi(
				environment.getProperty("pmp-keymanager.upload.other.domain.cert.rest.uri"), null, "", "",
				MediaType.APPLICATION_JSON, request, Map.class);
		try {
			responseObject = mapper.readValue(mapper.writeValueAsString(uploadApiResponse.get("response")),
					CACertificateResponseDto.class);
		} catch (Exception e) {
			LOGGER.error(
					("Error occured while mapping the response of upload other domain cert api " + e.getStackTrace()));
			throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
					ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage() + e.getMessage());
		}
		if (responseObject == null && uploadApiResponse.containsKey(ERRORS)) {
			List<Map<String, Object>> certServiceErrorList = (List<Map<String, Object>>) uploadApiResponse.get(ERRORS);
			if (!certServiceErrorList.isEmpty()) {
				throw new ApiAccessibleException(certServiceErrorList.get(0).get(ERRORCODE).toString(),
						certServiceErrorList.get(0).get(ERRORMESSAGE).toString());
			} else {
				throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
						ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage());
			}
		}
		if (responseObject == null) {
			throw new ApiAccessibleException(ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorCode(),
					ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public PartnerCertDownloadResponeDto getPartnerCertificate(PartnerCertDownloadRequestDto certDownloadRequestDto)
			throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {
		Optional<Partner> partnerFromDb = partnerRepository.findById(certDownloadRequestDto.getPartnerId());
		if (partnerFromDb.isEmpty()) {
			throw new PartnerDoesNotExistsException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		if (partnerFromDb.get().getCertificateAlias() == null) {
			throw new PartnerServiceException(
					PartnerExceptionConstants.CERTIFICATE_NOT_UPLOADED_EXCEPTION.getErrorCode(),
					PartnerExceptionConstants.CERTIFICATE_NOT_UPLOADED_EXCEPTION.getErrorMessage());
		}
		PartnerCertDownloadResponeDto responseObject = null;
		Map<String, String> pathsegments = new HashMap<>();
		pathsegments.put("partnerCertId", partnerFromDb.get().getCertificateAlias());
		Map<String, Object> getApiResponse = restUtil
				.getApi(environment.getProperty("pmp.partner.certificaticate.get.rest.uri"), pathsegments, Map.class);
		responseObject = mapper.readValue(mapper.writeValueAsString(getApiResponse.get("response")),
				PartnerCertDownloadResponeDto.class);
		if (responseObject == null && getApiResponse.containsKey(ERRORS)) {
			List<Map<String, Object>> certServiceErrorList = (List<Map<String, Object>>) getApiResponse.get(ERRORS);
			if (!certServiceErrorList.isEmpty()) {
				throw new ApiAccessibleException(certServiceErrorList.get(0).get(ERRORCODE).toString(),
						certServiceErrorList.get(0).get(ERRORMESSAGE).toString());
			} else {
				throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
						ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage());
			}
		}
		if (responseObject == null) {
			throw new ApiAccessibleException(ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorCode(),
					ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorMessage());
		}

		return responseObject;
	}

	/**
	 * 
	 */
	@Override
	public String addBiometricExtractors(String partnerId, String policyId, ExtractorsDto extractors) {
		if (!EmptyCheckUtils.isNullEmpty(partnerId)) {
			Optional<Partner> partnerFromDb = partnerRepository.findById(partnerId);
			if (partnerFromDb.isEmpty()) {
				throw new PartnerDoesNotExistsException(
						PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
						PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
			}

			if (!Arrays.stream(biometricExtractorsRequiredPartnerTypes.split(","))
					.anyMatch(partnerFromDb.get().getPartnerTypeCode()::equalsIgnoreCase)) {
				throw new PartnerServiceException(
						PartnerExceptionConstants.EXTRACTORS_ONLY_FOR_CREDENTIAL_PARTNER.getErrorCode(),
						PartnerExceptionConstants.EXTRACTORS_ONLY_FOR_CREDENTIAL_PARTNER.getErrorMessage()
								+ biometricExtractorsRequiredPartnerTypes);
			}
		}

		if (!EmptyCheckUtils.isNullEmpty(policyId)) {
			Optional<AuthPolicy> authPolicy = authPolicyRepository.findById(policyId);
			if (authPolicy.isEmpty()) {
				throw new PolicyGroupDoesNotExistException(
						PolicyGroupDoesNotExistConstant.POLICY_GROUP_DOES_NOT_EXIST.getErrorCode(),
						PolicyGroupDoesNotExistConstant.POLICY_GROUP_DOES_NOT_EXIST.getErrorMessage());
			}
		}

		PartnerPolicyRequest partnerPolicyRequest = partnerPolicyRequestRepository.findByPartnerIdAndPolicyId(partnerId,
				policyId);
		if (partnerPolicyRequest == null) {
			throw new PartnerServiceException(
					PartnerExceptionConstants.PARTNER_POLICY_MAPPING_NOT_EXISTS.getErrorCode(),
					PartnerExceptionConstants.PARTNER_POLICY_MAPPING_NOT_EXISTS.getErrorMessage());
		}
		if (partnerPolicyRequest.getStatusCode().equalsIgnoreCase(APPROVEDSTATUS)) {
			throw new PartnerServiceException(PartnerExceptionConstants.PARTNER_API_KEY_REQUEST_APPROVED.getErrorCode(),
					PartnerExceptionConstants.PARTNER_API_KEY_REQUEST_APPROVED.getErrorMessage());
		}

		BiometricExtractorProvider extractorProvider = null;
		for (ExtractorDto extractor : extractors.getExtractors()) {
			extractorProvider = new BiometricExtractorProvider();
			extractorProvider.setPartnerId(partnerId);
			extractorProvider.setPolicyId(policyId);
			extractorProvider.setId(PartnerUtil.generateId());
			extractorProvider.setAttributeName(extractor.getAttributeName());
			extractorProvider.setBiometricModality(extractor.getBiometric().split("\\[")[0]);
			if (extractor.getBiometric().split("\\[").length > 1) {
				extractorProvider.setBiometricSubTypes(extractor.getBiometric().split("\\[")[1].split("\\]")[0]);
			}
			extractorProvider.setExtractorProvider(extractor.getExtractor().getProvider());
			extractorProvider.setExtractorProviderVersion(extractor.getExtractor().getVersion());
			extractorProvider.setCrBy(getUser());
			extractorProvider.setCrDtimes(Timestamp.valueOf(LocalDateTime.now()));
			BiometricExtractorProvider extractorsFromDb = extractorProviderRepository
					.findByPartnerAndPolicyIdAndAttributeName(partnerId, policyId, extractor.getAttributeName());
			if (extractorsFromDb != null) {
				extractorProvider.setId(extractorsFromDb.getId());
				extractorProvider.setUpdBy(getUser());
				extractorProvider.setUpdDtimes(Timestamp.valueOf(LocalDateTime.now()));
				extractorProvider.setCrBy(extractorsFromDb.getCrBy());
				extractorProvider.setCrDtimes(extractorsFromDb.getCrDtimes());
			}
			extractorProviderRepository.save(extractorProvider);
		}

		return "Extractors added successfully.";
	}

	/**
	 * 
	 */
	@Override
	public ExtractorsDto getBiometricExtractors(String partnerId, String policyId) {
		List<BiometricExtractorProvider> extractorsFromDb = extractorProviderRepository
				.findByPartnerAndPolicyId(partnerId, policyId);
		if (extractorsFromDb.isEmpty()) {
			throw new PartnerServiceException(PartnerExceptionConstants.NO_DETAILS_FOUND.getErrorCode(),
					PartnerExceptionConstants.NO_DETAILS_FOUND.getErrorMessage());
		}
		ExtractorsDto response = new ExtractorsDto();
		List<ExtractorDto> extractors = new ArrayList<>();
		ExtractorDto extractor = null;
		ExtractorProviderDto provider = null;
		for (BiometricExtractorProvider biometricExtractor : extractorsFromDb) {
			extractor = new ExtractorDto();
			provider = new ExtractorProviderDto();
			extractor.setAttributeName(biometricExtractor.getAttributeName());
			extractor.setBiometric(biometricExtractor.getBiometricModality());
			provider.setProvider(biometricExtractor.getExtractorProvider());
			provider.setVersion(biometricExtractor.getExtractorProviderVersion());
			extractor.setExtractor(provider);
			if (biometricExtractor.getBiometricSubTypes() != null) {
				extractor.setBiometric(biometricExtractor.getBiometricModality() + "["
						+ biometricExtractor.getBiometricSubTypes() + "]");
			}
			extractors.add(extractor);
		}
		response.setExtractors(extractors);
		return response;
	}

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public PageResponseDto<PartnerSearchResponseDto> searchPartner(PartnerSearchDto dto) {
		List<PartnerSearchResponseDto> partners = new ArrayList<>();
		PageResponseDto<PartnerSearchResponseDto> pageDto = new PageResponseDto<>();
		if (!dto.getPartnerType().equalsIgnoreCase(ALL)) {
			List<SearchFilter> filters = new ArrayList<>();
			SearchFilter partnerTypeSearch = new SearchFilter();
			partnerTypeSearch.setColumnName("partnerTypeCode");
			partnerTypeSearch.setValue(dto.getPartnerType());
			partnerTypeSearch.setType("equals");
			filters.addAll(dto.getFilters());
			filters.add(partnerTypeSearch);
			dto.setFilters(filters);
		}
		Page<Partner> page = partnerSearchHelper.search(entityManager, Partner.class, dto);
		if (page.getContent() != null && !page.getContent().isEmpty()) {
			partners = MapperUtils.mapAll(page.getContent(), PartnerSearchResponseDto.class);
		}
		pageDto.setData(partners);
		pageDto.setFromRecord(0);
		pageDto.setToRecord(page.getContent().size());
		pageDto.setTotalRecord(page.getContent().size());
		return pageDto;
	}

	@Override
	public PageResponseDto<PartnerType> searchPartnerType(SearchDto dto) {
		List<PartnerType> partners = new ArrayList<>();
		PageResponseDto<PartnerType> pageDto = new PageResponseDto<>();
		Page<PartnerType> page = partnerSearchHelper.search(entityManager, PartnerType.class, dto);
		if (page.getContent() != null && !page.getContent().isEmpty()) {
			partners = MapperUtils.mapAll(page.getContent(), PartnerType.class);
		}
		pageDto.setData(partners);
		pageDto.setFromRecord(0);
		pageDto.setToRecord(page.getContent().size());
		pageDto.setTotalRecord(page.getContent().size());
		return pageDto;
	}

	@Override
	public String mapPartnerPolicyCredentialType(String credentialType, String partnerId, String policyId) {
		validateCredentialTypes(credentialType);
		Optional<Partner> partnerFromDb = partnerRepository.findById(partnerId);
		if (partnerFromDb.isEmpty()) {
			throw new PartnerDoesNotExistsException(
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					PartnerDoesNotExistExceptionConstant.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		if (!Arrays.stream(credentialTypesRequiredPartnerTypes.split(","))
				.anyMatch(partnerFromDb.get().getPartnerTypeCode()::equalsIgnoreCase)) {
			throw new PartnerServiceException(PartnerExceptionConstants.CREDENTIAL_NOT_ALLOWED_PARTNERS.getErrorCode(),
					PartnerExceptionConstants.CREDENTIAL_NOT_ALLOWED_PARTNERS.getErrorMessage()
							+ credentialTypesRequiredPartnerTypes);
		}
		Optional<AuthPolicy> authPolicy = authPolicyRepository.findById(policyId);
		if (authPolicy.isEmpty()) {
			throw new PartnerServiceException(PartnerExceptionConstants.POLICY_NOT_EXIST.getErrorCode(),
					PartnerExceptionConstants.POLICY_NOT_EXIST.getErrorMessage());
		}
		PartnerPolicyCredentialType entity = new PartnerPolicyCredentialType();
		PartnerPolicyCredentialTypePK key = new PartnerPolicyCredentialTypePK();
		key.setCredentialType(credentialType);
		key.setPartId(partnerId);
		key.setPolicyId(policyId);
		entity.setId(key);
		entity.setCrBy(getUser());
		entity.setCrDtimes(Timestamp.valueOf(LocalDateTime.now()));
		entity.setIsActive(true);
		entity.setIsDeleted(false);
		partnerCredentialTypePolicyRepo.save(entity);
		return "Partner, policy and credentialType mapping done successfully.";
	}

	/**
	 * 
	 */
	@Override
	public PartnerCredentialTypePolicyDto getPartnerCredentialTypePolicy(String credentialType, String partnerId)
			throws JsonParseException, JsonMappingException, IOException {
		PartnerPolicyCredentialType partnerCredentialTypePolicy = partnerCredentialTypePolicyRepo
				.findByPartnerIdAndCrdentialType(partnerId, credentialType);
		if (partnerCredentialTypePolicy == null) {
			throw new PartnerServiceException(PartnerExceptionConstants.NO_DETAILS_FOUND.getErrorCode(),
					PartnerExceptionConstants.NO_DETAILS_FOUND.getErrorMessage());
		}
		Optional<AuthPolicy> authPolicy = authPolicyRepository
				.findById(partnerCredentialTypePolicy.getId().getPolicyId());
		if (authPolicy.isEmpty()) {
			throw new PartnerServiceException(PartnerExceptionConstants.POLICY_NOT_EXIST.getErrorCode(),
					PartnerExceptionConstants.POLICY_NOT_EXIST.getErrorMessage());
		}

		return mapPolicyToResponseDto(authPolicy.get(), partnerId, credentialType);
	}

	/**
	 * 
	 * @param credentialType
	 */
	private void validateCredentialTypes(String credentialType) {
		if (!Arrays.stream(allowedCredentialTypes.split(",")).anyMatch(credentialType::equalsIgnoreCase)) {
			throw new PartnerServiceException(PartnerExceptionConstants.CREDENTIAL_TYPE_NOT_ALLOWED.getErrorCode(),
					PartnerExceptionConstants.CREDENTIAL_TYPE_NOT_ALLOWED.getErrorMessage() + allowedCredentialTypes);
		}
	}

	/**
	 * 
	 * @param authPolicy
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	private PartnerCredentialTypePolicyDto mapPolicyToResponseDto(AuthPolicy authPolicy, String partnerId,
			String credentialType) throws JsonParseException, JsonMappingException, IOException {
		PartnerCredentialTypePolicyDto response = new PartnerCredentialTypePolicyDto();
		response.setPartnerId(partnerId);
		response.setCredentialType(credentialType);
		response.setCr_by(authPolicy.getCrBy());
		response.setCr_dtimes(getLocalDateTime(authPolicy.getCrDtimes()));
		response.setIs_Active(authPolicy.getIsActive());
		response.setPolicyDesc(authPolicy.getDescr());
		response.setPolicyId(authPolicy.getId());
		response.setPolicyName(authPolicy.getName());
		response.setPolicyType(authPolicy.getPolicy_type());
		response.setPublishDate(authPolicy.getValidFromDate());
		response.setValidTill(authPolicy.getValidToDate());
		response.setSchema(authPolicy.getPolicySchema());
		response.setStatus(authPolicy.getIsActive() == true ? "PUBLISHED" : "DRAFTED");
		response.setUp_by(authPolicy.getUpdBy());
		response.setUpd_dtimes(authPolicy.getUpdDtimes());
		response.setVersion(authPolicy.getVersion());
		response.setPolicies(getPolicyObject(authPolicy.getPolicyFileId()));
		return response;
	}

	/**
	 * 
	 * @param policy
	 * @return
	 */
	private JSONObject getPolicyObject(String policy) {
		JSONParser parser = new JSONParser();
		String error = null;
		try {
			return ((JSONObject) parser.parse(policy));
		} catch (ParseException e) {
			error = e.getMessage();
		}
		throw new PartnerServiceException(PartnerExceptionConstants.POLICY_PARSING_ERROR.getErrorCode(),
				PartnerExceptionConstants.POLICY_PARSING_ERROR.getErrorMessage() + error);
	}

	/**
	 * 
	 * @param date
	 * @return
	 */
	private LocalDateTime getLocalDateTime(Timestamp date) {
		if (date != null) {
			return date.toLocalDateTime();
		}
		return LocalDateTime.now();
	}
	
	/**
	 * 
	 * @param mispLicenseKey
	 */
	private void notify(String certData,String partnerDomain) {
		Type type = new io.mosip.pmp.authdevice.util.dto.Type();
		type.setName("PartnerServiceImpl");
		type.setNamespace("io.mosip.pmp.partner.service.impl.PartnerServiceImpl");
		Map<String,Object> data = new HashMap<>();
		data.put("certificateData", certData);
		data.put("partnerDomain", partnerDomain);
		webSubPublisher.notify(EventType.CA_CERTIFICATE_UPLOADED,data,type);
	}
	
	private void notify(String partnerId) {
		Type type = new io.mosip.pmp.authdevice.util.dto.Type();
		type.setName("PartnerServiceImpl");
		type.setNamespace("io.mosip.pmp.partner.service.impl.PartnerServiceImpl");
		Map<String,Object> data = new HashMap<>();
		data.put("partnerId", partnerId);
		webSubPublisher.notify(EventType.PARTNER_UPDATED,data,type);
	}
}

//String updatedParam = (isApiKeyUpdated == true) ? "apiKey" : "";