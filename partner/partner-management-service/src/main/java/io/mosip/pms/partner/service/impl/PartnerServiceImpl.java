package io.mosip.pms.partner.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.pms.common.constant.ApiAccessibleExceptionConstant;
import io.mosip.pms.common.constant.EventType;
import io.mosip.pms.common.dto.FilterData;
import io.mosip.pms.common.dto.FilterDto;
import io.mosip.pms.common.dto.FilterValueDto;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.common.dto.PartnerDataPublishDto;
import io.mosip.pms.common.dto.PartnerPolicySearchResponseDto;
import io.mosip.pms.common.dto.PolicyRequestSearchResponseDto;
import io.mosip.pms.common.dto.SearchDto;
import io.mosip.pms.common.dto.SearchFilter;
import io.mosip.pms.common.dto.Type;
import io.mosip.pms.common.entity.AuthPolicy;
import io.mosip.pms.common.entity.BiometricExtractorProvider;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.entity.PartnerContact;
import io.mosip.pms.common.entity.PartnerH;
import io.mosip.pms.common.entity.PartnerHPK;
import io.mosip.pms.common.entity.PartnerPolicy;
import io.mosip.pms.common.entity.PartnerPolicyCredentialType;
import io.mosip.pms.common.entity.PartnerPolicyCredentialTypePK;
import io.mosip.pms.common.entity.PartnerPolicyRequest;
import io.mosip.pms.common.entity.PartnerType;
import io.mosip.pms.common.entity.PolicyGroup;
import io.mosip.pms.common.exception.ApiAccessibleException;
import io.mosip.pms.common.helper.FilterHelper;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.repository.AuthPolicyRepository;
import io.mosip.pms.common.repository.BiometricExtractorProviderRepository;
import io.mosip.pms.common.repository.PartnerContactRepository;
import io.mosip.pms.common.repository.PartnerHRepository;
import io.mosip.pms.common.repository.PartnerPolicyCredentialTypeRepository;
import io.mosip.pms.common.repository.PartnerPolicyRepository;
import io.mosip.pms.common.repository.PartnerPolicyRequestRepository;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.repository.PartnerTypeRepository;
import io.mosip.pms.common.repository.PolicyGroupRepository;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.NotificationDto;
import io.mosip.pms.common.service.NotificatonService;
import io.mosip.pms.common.util.MapperUtils;
import io.mosip.pms.common.util.PageUtils;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.common.validator.FilterColumnValidator;
import io.mosip.pms.device.response.dto.ColumnCodeValue;
import io.mosip.pms.device.response.dto.FilterResponseCodeDto;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.constant.PartnerConstants;
import io.mosip.pms.partner.constant.PartnerServiceAuditEnum;
import io.mosip.pms.partner.dto.DataShareResponseDto;
import io.mosip.pms.partner.dto.MosipUserDto;
import io.mosip.pms.partner.dto.UploadCertificateRequestDto;
import io.mosip.pms.partner.dto.UserRegistrationRequestDto;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.keycloak.service.KeycloakImpl;
import io.mosip.pms.partner.manager.service.PartnerManagerService;
import io.mosip.pms.partner.request.dto.AddContactRequestDto;
import io.mosip.pms.partner.request.dto.CACertificateRequestDto;
import io.mosip.pms.partner.request.dto.ExtractorDto;
import io.mosip.pms.partner.request.dto.ExtractorProviderDto;
import io.mosip.pms.partner.request.dto.ExtractorsDto;
import io.mosip.pms.partner.request.dto.PartnerAPIKeyRequest;
import io.mosip.pms.partner.request.dto.PartnerCertDownloadRequestDto;
import io.mosip.pms.partner.request.dto.PartnerCertificateRequestDto;
import io.mosip.pms.partner.request.dto.PartnerCertificateUploadRequestDto;
import io.mosip.pms.partner.request.dto.PartnerRequest;
import io.mosip.pms.partner.request.dto.PartnerSearchDto;
import io.mosip.pms.partner.request.dto.PartnerUpdateRequest;
import io.mosip.pms.partner.response.dto.APIkeyRequests;
import io.mosip.pms.partner.response.dto.CACertificateResponseDto;
import io.mosip.pms.partner.response.dto.DownloadPartnerAPIkeyResponse;
import io.mosip.pms.partner.response.dto.PartnerAPIKeyResponse;
import io.mosip.pms.partner.response.dto.PartnerCertDownloadResponeDto;
import io.mosip.pms.partner.response.dto.PartnerCertificateResponseDto;
import io.mosip.pms.partner.response.dto.PartnerCredentialTypePolicyDto;
import io.mosip.pms.partner.response.dto.PartnerResponse;
import io.mosip.pms.partner.response.dto.PartnerSearchResponseDto;
import io.mosip.pms.partner.response.dto.RetrievePartnerDetailsResponse;
import io.mosip.pms.partner.service.PartnerService;
import io.mosip.pms.partner.util.PartnerUtil;

@Service
@Transactional
public class PartnerServiceImpl implements PartnerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PartnerServiceImpl.class);

	private static final String ALL = "all";

	private static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";

	private static final String END_CERTIFICATE = "-----END CERTIFICATE-----";

	private final static String LINE_SEPARATOR = "\n";

	@Autowired
	PartnerServiceRepository partnerRepository;

	@Autowired
	PartnerTypeRepository partnerTypeRepository;

	@Autowired
	PolicyGroupRepository policyGroupRepository;

	@Autowired
	PartnerHRepository partnerHRepository;

	@Autowired
	AuthPolicyRepository authPolicyRepository;

	@Autowired
	PartnerPolicyRequestRepository partnerPolicyRequestRepository;

	@Autowired
	PartnerPolicyRepository partnerPolicyRepository;

	@Autowired
	PartnerContactRepository partnerContactRepository;

	@Autowired
	BiometricExtractorProviderRepository extractorProviderRepository;

	@Autowired
	PartnerPolicyCredentialTypeRepository partnerCredentialTypePolicyRepo;

	@Autowired
	PartnerManagerService partnerManagerService;

	@Autowired
	SearchHelper partnerSearchHelper;

	@Autowired
	private PageUtils pageUtils;

	@Autowired
	FilterColumnValidator filterColumnValidator;

	@Autowired
	FilterHelper filterHelper;

	@Autowired
	RestUtil restUtil;

	@Autowired
	private WebSubPublisher webSubPublisher;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private Environment environment;

	@Autowired
	private NotificatonService notificationService;

	@Autowired
	private KeycloakImpl keycloakImpl;

	@Value("${pmp.partner.partnerId.max.length}")
	private int partnerIdMaxLength;

	@Value("${mosip.pmp.partner.policy.expiry.period.indays}")
	private int partnerPolicyExpiryInDays;

	@Value("${pmp.partner.valid.email.address.regex}")
	private String emailRegex;

	@Value("${pmp.allowed.credential.types}")
	private String allowedCredentialTypes;

	@Value("${policy.credential.type.mapping.allowed.partner.types}")
	private String credentialTypesRequiredPartnerTypes;

	@Value("${application.id:PARTNER}")
	private String applicationId;

	@Value("${pms.certs.datashare.policyId}")
	private String policyId;

	@Value("${pms.certs.datashare.subscriberId}")
	private String subscriberId;

	@Autowired
	AuditUtil auditUtil;

	@Value("${pmp.partner.mobileNumbe.max.length:16}")
	private int maxMobileNumberLength;

	@Override
	public PartnerResponse savePartner(PartnerRequest request) {
		if (!validateMobileNumeber(request.getContactNumber())) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.REGISTER_PARTNER_FAILURE);
			throw new PartnerServiceException(ErrorCode.INVALID_MOBILE_NUMBER_EXCEPTION.getErrorCode(),
					ErrorCode.INVALID_MOBILE_NUMBER_EXCEPTION.getErrorMessage() + maxMobileNumberLength);
		}

		if (!validateEmail(request.getEmailId())) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.REGISTER_PARTNER_FAILURE);
			throw new PartnerServiceException(ErrorCode.INVALID_EMAIL_ID_EXCEPTION.getErrorCode(),
					ErrorCode.INVALID_EMAIL_ID_EXCEPTION.getErrorMessage());
		}

		if (!validatePartnerIdLength(request.getPartnerId())) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.REGISTER_PARTNER_FAILURE);
			throw new PartnerServiceException(ErrorCode.PARTNER_ID_LENGTH_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_ID_LENGTH_EXCEPTION.getErrorMessage() + partnerIdMaxLength);
		}

		if (!validatePartnerByEmail(request.getEmailId())) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.REGISTER_PARTNER_FAILURE);
			throw new PartnerServiceException(ErrorCode.EMAIL_ALREADY_EXISTS_EXCEPTION.getErrorCode(),
					ErrorCode.EMAIL_ALREADY_EXISTS_EXCEPTION.getErrorMessage());
		}

		if (!validatePartnerId(request.getPartnerId())) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.REGISTER_PARTNER_FAILURE);
			throw new PartnerServiceException(ErrorCode.PARTNER_ALREADY_REGISTERED_WITH_ID_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_ALREADY_REGISTERED_WITH_ID_EXCEPTION.getErrorMessage());
		}

		PartnerType partnerType = validateAndGetPartnerType(request.getPartnerType());
		PolicyGroup policyGroup = null;
		if (partnerType.getIsPolicyRequired()) {
			policyGroup = validateAndGetPolicyGroupByName(request.getPolicyGroup());
		}
		Partner partner = mapPartnerFromRequest(request, policyGroup);
		RegisterUserInKeycloak(partner);
		partnerRepository.save(partner);
		saveToPartnerH(partner);
		PartnerResponse partnerResponse = new PartnerResponse();
		partnerResponse.setPartnerId(partner.getId());
		partnerResponse.setStatus(partner.getApprovalStatus());
		sendNotifications(EventType.PARTNER_REGISTRED, partner);
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.REGISTER_PARTNER_SUCCESS);
		return partnerResponse;
	}

	private void saveToPartnerH(Partner partner) {
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
		partnerHistory.setIsDeleted(partner.getIsDeleted());
		partnerHistory.setUserId(partner.getUserId());
		partnerHistory.setCrBy(partner.getCrBy());
		partnerHistory.setCrDtimes(Timestamp.valueOf(now));
		partnerHistory.setId(partnerHPK);
		partnerHRepository.save(partnerHistory);
	}

	private MosipUserDto RegisterUserInKeycloak(Partner partner) {
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

	private Partner mapPartnerFromRequest(PartnerRequest request, PolicyGroup policyGroup) {
		Partner partner = new Partner();
		partner.setId(request.getPartnerId());
		partner.setPolicyGroupId(policyGroup != null ? policyGroup.getId() : null);
		partner.setName(request.getOrganizationName());
		partner.setAddress(request.getAddress());
		partner.setContactNo(request.getContactNumber());
		partner.setPartnerTypeCode(request.getPartnerType());
		partner.setEmailId(request.getEmailId());
		partner.setIsActive(false);
		partner.setIsDeleted(false);
		partner.setUserId(request.getPartnerId());
		partner.setCrBy(getUser());
		partner.setApprovalStatus(PartnerConstants.IN_PROGRESS);
		partner.setCrDtimes(Timestamp.valueOf(LocalDateTime.now()));
		return partner;
	}

	private PolicyGroup validateAndGetPolicyGroupByName(String policyGroupName) {
		PolicyGroup policyGroupFromDb = policyGroupRepository.findByName(policyGroupName);
		if (policyGroupFromDb == null) {
			LOGGER.error(policyGroupName + " : Policy Group is not available");
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.REGISTER_PARTNER_FAILURE);
			throw new PartnerServiceException(ErrorCode.POLICY_GROUP_DOES_NOT_EXIST.getErrorCode(),
					ErrorCode.POLICY_GROUP_DOES_NOT_EXIST.getErrorMessage());
		}
		return policyGroupFromDb;
	}

	private PartnerType validateAndGetPartnerType(String partnerType) {
		Optional<PartnerType> partnerTypeFromDb = partnerTypeRepository.findById(partnerType);
		if (partnerTypeFromDb.isEmpty()) {
			LOGGER.error(partnerType + " : partnerType is not available.");
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.REGISTER_PARTNER_FAILURE);
			throw new PartnerServiceException(ErrorCode.PARTNER_TYPE_DOES_NOT_EXIST.getErrorCode(),
					ErrorCode.PARTNER_TYPE_DOES_NOT_EXIST.getErrorMessage());
		}
		return partnerTypeFromDb.get();
	}

	private boolean validatePartnerByEmail(String emailId) {
		Partner partnerFromDb = partnerRepository.findByEmailId(emailId);
		if (partnerFromDb != null) {
			LOGGER.error("Partner with email " + emailId + "already exists.");
			return false;
		}
		return true;
	}

	private boolean validatePartnerId(String partnerId) {
		Optional<Partner> partnerById = partnerRepository.findById(partnerId);
		if (!partnerById.isEmpty()) {
			LOGGER.error("Partner with id " + partnerId + "already exists.");
			return false;
		}
		return true;
	}

	private boolean validatePartnerIdLength(String partnerId) {
		if (partnerId.length() > partnerIdMaxLength) {
			LOGGER.error(
					"Length of partner id " + partnerId + " : is more than max length(" + partnerIdMaxLength + ")");
			return false;

		}
		return true;
	}

	/**
	 * Validates the email id
	 * 
	 * @param emailId
	 */
	private boolean validateEmail(String emailId) {
		if (!emailId.matches(emailRegex)) {
			return false;
		}
		return true;
	}

	/**
	 * Validates the mobile number length
	 * 
	 * @param contactNumber
	 */
	private boolean validateMobileNumeber(String contactNumber) {
		if (contactNumber.length() > maxMobileNumberLength) {
			return false;
		}
		return true;
	}

	@Override
	public RetrievePartnerDetailsResponse getPartnerDetails(String partnerId) {
		RetrievePartnerDetailsResponse response = new RetrievePartnerDetailsResponse();
		Partner partner = getValidPartner(partnerId, true);
		response.setPartnerID(partner.getId());
		response.setAddress(partner.getAddress());
		response.setContactNumber(partner.getContactNo());
		response.setEmailId(partner.getEmailId());
		response.setOrganizationName(partner.getName());
		response.setPartnerType(partner.getPartnerTypeCode());
		response.setStatus(partner.getApprovalStatus());
		if (partner.getPolicyGroupId() != null) {
			response.setPolicyGroup(validateAndGetPolicyGroupById(partner.getPolicyGroupId()).getName());
		}
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.RETRIVE_PARTNER_SUCCESS);
		return response;
	}

	private Partner getValidPartner(String partnerId, boolean isToRetrieve) {
		Optional<Partner> partnerById = partnerRepository.findById(partnerId);
		if (partnerById.isEmpty()) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.RETRIVE_PARTNER_FAILURE);
			throw new PartnerServiceException(ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		if (!isToRetrieve) {
			if (!partnerById.get().getIsActive()) {
				auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.RETRIVE_PARTNER_FAILURE);
				throw new PartnerServiceException(ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorCode(),
						ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorMessage());
			}
		}
		return partnerById.get();
	}

	private PolicyGroup validateAndGetPolicyGroupById(String policyGroupId) {
		Optional<PolicyGroup> policyGroupFromDb = policyGroupRepository.findById(policyGroupId);
		if (policyGroupFromDb.isEmpty()) {
			LOGGER.error(policyGroupId + " : Policy Group is not available");
			throw new PartnerServiceException(ErrorCode.POLICY_GROUP_DOES_NOT_EXIST.getErrorCode(),
					ErrorCode.POLICY_GROUP_DOES_NOT_EXIST.getErrorMessage());
		}
		return policyGroupFromDb.get();
	}

	@Override
	public PartnerResponse updatePartnerDetail(PartnerUpdateRequest partnerUpdateRequest, String partnerId) {
		if (!validateMobileNumeber(partnerUpdateRequest.getContactNumber())) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPDATE_PARTNER_FAILURE);
			throw new PartnerServiceException(ErrorCode.INVALID_MOBILE_NUMBER_EXCEPTION.getErrorCode(),
					ErrorCode.INVALID_MOBILE_NUMBER_EXCEPTION.getErrorMessage() + maxMobileNumberLength);
		}
		;
		Partner partner = getValidPartner(partnerId, false);
		partner.setAddress(partnerUpdateRequest.getAddress());
		partner.setContactNo(partnerUpdateRequest.getContactNumber());
		partner.setUpdBy(getUser());
		partner.setUpdDtimes(Timestamp.valueOf(LocalDateTime.now()));
		partnerRepository.save(partner);
		saveToPartnerH(partner);
		PartnerResponse updateResponse = new PartnerResponse();
		updateResponse.setPartnerId(partner.getId());
		updateResponse.setStatus(partner.getApprovalStatus());
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPDATE_PARTNER_SUCCESS);
		return updateResponse;
	}

	@Override
	public PartnerAPIKeyResponse submitPartnerApiKeyReq(PartnerAPIKeyRequest partnerAPIKeyRequest, String partnerId) {
		Map<String, Object> data = new HashMap<>();
		PartnerAPIKeyResponse partnerAPIKeyResponse = new PartnerAPIKeyResponse();
		Partner partner = getValidPartner(partnerId, false);
		if(partner.getPolicyGroupId() == null) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.SUBMIT_API_REQUEST_FAILURE);
			throw new PartnerServiceException(ErrorCode.PARTNER_NOT_MAPPED_TO_POLICY_GROUP.getErrorCode(),
					ErrorCode.PARTNER_NOT_MAPPED_TO_POLICY_GROUP.getErrorMessage());
		}
		AuthPolicy authPolicy = validatePolicyGroupAndPolicy(partner.getPolicyGroupId(),
				partnerAPIKeyRequest.getPolicyName());
		PartnerPolicyRequest partnerPolicyRequest = new PartnerPolicyRequest();
		partnerPolicyRequest.setStatusCode(PartnerConstants.IN_PROGRESS);
		partnerPolicyRequest.setCrBy(getUser());
		partnerPolicyRequest.setCrDtimes(Timestamp.valueOf(LocalDateTime.now()));
		partnerPolicyRequest.setId(PartnerUtil.createPartnerPolicyRequestId());
		partnerPolicyRequest.setPartner(partner);
		partnerPolicyRequest.setPolicyId(authPolicy.getId());
		partnerPolicyRequest.setRequestDatetimes(Timestamp.valueOf(LocalDateTime.now()));
		partnerPolicyRequest.setRequestDetail(partnerAPIKeyRequest.getUseCaseDescription());
		partnerPolicyRequest.setIsDeleted(false);
		if (!partnerPolicyRepository.findByPartnerIdAndPolicyIdAndIsActiveTrue(partnerId, authPolicy.getId())
				.isEmpty()) {
			partnerPolicyRequest.setStatusCode(PartnerConstants.APPROVED);
			partnerPolicyRequestRepository.save(partnerPolicyRequest);
			data.put("apiKeyData", MapperUtils.mapKeyDataToPublishDto(approvePartnerPolicy(partnerPolicyRequest)));
			PartnerCertDownloadRequestDto certDownloadRequestDto = new PartnerCertDownloadRequestDto();
			certDownloadRequestDto.setPartnerId(partnerPolicyRequest.getPartner().getId());
			try {
				data.put("partnerData", MapperUtils.mapDataToPublishDto(partnerPolicyRequest.getPartner(),
						getPartnerCertificate(certDownloadRequestDto).getCertificateData()));
			} catch (IOException e) {
				LOGGER.error("Error occurred while getting the partnercert and mappingdata", e);
				e.printStackTrace();
			}
			data.put("policyData",
					MapperUtils.mapPolicyToPublishDto(authPolicy, getPolicyObject(authPolicy.getPolicyFileId())));
			partnerAPIKeyResponse.setApiRequestId(partnerPolicyRequest.getId());
			partnerAPIKeyResponse.setApikeyId(partnerPolicyRequest.getId());
			partnerAPIKeyResponse.setMessage("PartnerAPIKeyRequest successfully submitted and approved.");
			return partnerAPIKeyResponse;
		}
		partnerPolicyRequestRepository.save(partnerPolicyRequest);
		partnerAPIKeyResponse.setApiRequestId(partnerPolicyRequest.getId());
		partnerAPIKeyResponse.setMessage("PartnerAPIKeyRequest successfully submitted.");
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.SUBMIT_API_REQUEST_SUCCESS);
		return partnerAPIKeyResponse;
	}

	private PartnerPolicy approvePartnerPolicy(PartnerPolicyRequest partnerPolicyRequest) {
		PartnerPolicy partnerPolicy = new PartnerPolicy();
		partnerPolicy.setPolicyApiKey(partnerPolicyRequest.getId());
		partnerPolicy.setPartner(partnerPolicyRequest.getPartner());
		partnerPolicy.setPolicyId(partnerPolicyRequest.getPolicyId());
		partnerPolicy.setIsActive(true);
		partnerPolicy.setIsDeleted(false);
		partnerPolicy.setValidFromDatetime(Timestamp.valueOf(LocalDateTime.now()));
		partnerPolicy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(partnerPolicyExpiryInDays)));
		partnerPolicy.setCrBy(partnerPolicyRequest.getCrBy());
		partnerPolicy.setCrDtimes(partnerPolicyRequest.getCrDtimes());
		partnerPolicyRepository.save(partnerPolicy);
		LOGGER.info("PartnerAPIKeyRequest successfully submitted and approved.");
		return partnerPolicy;
	}

	private AuthPolicy validatePolicyGroupAndPolicy(String policyGroupId, String policyName) {
		AuthPolicy authPolicyFromDb = authPolicyRepository.findByPolicyGroupAndName(policyGroupId, policyName);
		if (authPolicyFromDb == null) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.SUBMIT_API_REQUEST_FAILURE);
			throw new PartnerServiceException(ErrorCode.POLICY_GROUP_POLICY_NOT_EXISTS.getErrorCode(),
					ErrorCode.POLICY_GROUP_POLICY_NOT_EXISTS.getErrorMessage());
		}

		if (!authPolicyFromDb.getIsActive()) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.SUBMIT_API_REQUEST_FAILURE);
			throw new PartnerServiceException(ErrorCode.POLICY_NOT_ACTIVE_EXCEPTION.getErrorCode(),
					ErrorCode.POLICY_NOT_ACTIVE_EXCEPTION.getErrorMessage());

		}
		if (authPolicyFromDb.getValidToDate().isBefore(LocalDateTime.now())) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.SUBMIT_API_REQUEST_FAILURE);
			throw new PartnerServiceException(ErrorCode.POLICY_EXPIRED_EXCEPTION.getErrorCode(),
					ErrorCode.POLICY_EXPIRED_EXCEPTION.getErrorMessage());

		}
		if (!authPolicyFromDb.getPolicyGroup().getIsActive()) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.SUBMIT_API_REQUEST_FAILURE);
			throw new PartnerServiceException(ErrorCode.POLICY_GROUP_NOT_ACTIVE.getErrorCode(),
					ErrorCode.POLICY_GROUP_NOT_ACTIVE.getErrorMessage());
		}
		return authPolicyFromDb;
	}

	@Override
	public DownloadPartnerAPIkeyResponse getApikeyFromRequestKey(String partnerId, String apikeyReqId) {
		PartnerPolicyRequest partnerRequest = partnerPolicyRequestRepository.findByPartnerIdAndReqId(partnerId,
				apikeyReqId);
		if (partnerRequest == null) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.RETRIVE_PARTNER_APIKEY_STATUS_FAILURE);
			LOGGER.info(apikeyReqId + " : Invalid apikeyReqId");
			throw new PartnerServiceException(ErrorCode.PARTNER_API_KET_REQ_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_API_KET_REQ_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		DownloadPartnerAPIkeyResponse response = new DownloadPartnerAPIkeyResponse();
		response.setApikeyReqStatus(partnerRequest.getStatusCode());
		response.setApiRequestKey(apikeyReqId);
		if (partnerRequest.getStatusCode().equalsIgnoreCase(PartnerConstants.APPROVED)) {
			PartnerPolicy approvedPolicy = getPartnerMappedPolicy(apikeyReqId);
			response.setPartnerAPIKey(approvedPolicy.getPolicyApiKey());
			response.setValidityTill(approvedPolicy.getValidToDatetime());
			response.setApikeyStatus(approvedPolicy.getIsActive());
		}
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.RETRIVE_PARTNER_APIKEY_STATUS_SUCCESS);
		return response;
	}

	private PartnerPolicy getPartnerMappedPolicy(String apiKey) {
		return partnerPolicyRepository.findByApiKey(apiKey);
	}

	@Override
	public List<APIkeyRequests> retrieveAllApiKeyRequestsSubmittedByPartner(String partnerId) {
		List<PartnerPolicyRequest> apikeyRequestsByPartner = partnerPolicyRequestRepository.findByPartnerId(partnerId);
		if (apikeyRequestsByPartner.isEmpty()) {
			LOGGER.info("For partner " + partnerId + " : no apikey request exists.");
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.RETRIVE_PARTNER_APIKEYS_FAILURE);
			throw new PartnerServiceException(ErrorCode.PARTNER_API_KET_REQ_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_API_KET_REQ_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		List<APIkeyRequests> apikeyRequests = new ArrayList<>();
		for (PartnerPolicyRequest apIkeyRequest : apikeyRequestsByPartner) {
			APIkeyRequests approvedRequest = new APIkeyRequests();
			approvedRequest.setApiKeyReqID(apIkeyRequest.getId());
			approvedRequest.setApiKeyRequestStatus(apIkeyRequest.getStatusCode());
			if (apIkeyRequest.getStatusCode().equalsIgnoreCase(PartnerConstants.APPROVED)) {
				PartnerPolicy approvedPolicy = getPartnerMappedPolicy(apIkeyRequest.getId());
				approvedRequest.setPartnerApiKey(approvedPolicy.getPolicyApiKey());
				approvedRequest.setValidityTill(approvedPolicy.getValidToDatetime());
				approvedRequest.setApikeyStatus(approvedPolicy.getIsActive());
			}
			apikeyRequests.add(approvedRequest);
		}
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.RETRIVE_PARTNER_APIKEYS_SUCCESS);
		return apikeyRequests;
	}

	@Override
	public String createAndUpdateContactDetails(AddContactRequestDto request, String partnerId) {
		if (!validateEmail(request.getEmailId())) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.ADD_CONTACTS_FAILURE);
			throw new PartnerServiceException(ErrorCode.INVALID_EMAIL_ID_EXCEPTION.getErrorCode(),
					ErrorCode.INVALID_EMAIL_ID_EXCEPTION.getErrorMessage());
		}

		if (!validateMobileNumeber(request.getContactNumber())) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.REGISTER_PARTNER_FAILURE);
			throw new PartnerServiceException(ErrorCode.INVALID_MOBILE_NUMBER_EXCEPTION.getErrorCode(),
					ErrorCode.INVALID_MOBILE_NUMBER_EXCEPTION.getErrorMessage() + maxMobileNumberLength);
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
			Partner partnerFromDb = getValidPartner(partnerId, false);
			contactsFromDb = new PartnerContact();
			contactsFromDb.setId(PartnerUtil.createPartnerId());
			contactsFromDb.setAddress(request.getAddress());
			contactsFromDb.setContactNo(request.getContactNumber());
			contactsFromDb.setCrBy(getUser());
			contactsFromDb.setCrDtimes(LocalDateTime.now());
			contactsFromDb.setPartner(partnerFromDb);
			contactsFromDb.setEmailId(request.getEmailId());
			contactsFromDb.setIsActive(request.getIs_Active());
			contactsFromDb.setIsDeleted(false);
			resultMessage = "Contacts details added successfully.";
		}
		partnerContactRepository.save(contactsFromDb);
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.ADD_CONTACTS_SUCCESS);
		return resultMessage;
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
		if (responseObject == null && uploadApiResponse.containsKey(PartnerConstants.ERRORS)) {
			List<Map<String, Object>> certServiceErrorList = (List<Map<String, Object>>) uploadApiResponse
					.get(PartnerConstants.ERRORS);
			if (!certServiceErrorList.isEmpty()) {
				auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPLOAD_CA_CERT_FAILURE);
				throw new ApiAccessibleException(certServiceErrorList.get(0).get(PartnerConstants.ERRORCODE).toString(),
						certServiceErrorList.get(0).get(PartnerConstants.ERRORMESSAGE).toString());
			} else {
				auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPLOAD_CA_CERT_FAILURE);
				throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
						ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage());
			}
		}
		if (responseObject == null) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPLOAD_CA_CERT_FAILURE);
			throw new ApiAccessibleException(ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorCode(),
					ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorMessage());
		}
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPLOAD_CA_CERT_SUCCESS);
		return responseObject;
	}

	@Override
	public PartnerCertificateResponseDto uploadPartnerCertificate(
			PartnerCertificateUploadRequestDto partnerCertRequesteDto)
			throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {
		Partner partner = getValidPartner(partnerCertRequesteDto.getPartnerId(), true);
		PartnerCertificateRequestDto uploadRequest = new PartnerCertificateRequestDto();
		uploadRequest.setPartnerId(partnerCertRequesteDto.getPartnerId());
		uploadRequest.setOrganizationName(partner.getName());
		uploadRequest.setPartnerDomain(partnerCertRequesteDto.getPartnerDomain());
		uploadRequest.setPartnerType(partner.getPartnerTypeCode());
		uploadRequest.setCertificateData(partnerCertRequesteDto.getCertificateData());
		RequestWrapper<PartnerCertificateRequestDto> request = new RequestWrapper<>();
		request.setRequest(uploadRequest);
		PartnerCertificateResponseDto responseObject = null;
		Map<String, Object> uploadApiResponse = restUtil.postApi(
				environment.getProperty("pmp.partner.certificaticate.upload.rest.uri"), null, "", "",
				MediaType.APPLICATION_JSON, request, Map.class);
		responseObject = mapper.readValue(mapper.writeValueAsString(uploadApiResponse.get("response")),
				PartnerCertificateResponseDto.class);
		if (responseObject == null && uploadApiResponse.containsKey(PartnerConstants.ERRORS)) {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> certServiceErrorList = (List<Map<String, Object>>) uploadApiResponse
					.get(PartnerConstants.ERRORS);
			if (!certServiceErrorList.isEmpty()) {
				auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPLOAD_PARTNER_CERT_FAILURE);
				throw new ApiAccessibleException(certServiceErrorList.get(0).get(PartnerConstants.ERRORCODE).toString(),
						certServiceErrorList.get(0).get(PartnerConstants.ERRORMESSAGE).toString());
			} else {
				auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPLOAD_PARTNER_CERT_FAILURE);
				throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
						ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage());
			}
		}
		if (responseObject == null) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPLOAD_PARTNER_CERT_FAILURE);
			throw new ApiAccessibleException(ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorCode(),
					ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorMessage());
		}

		String signedPartnerCert = null;
		try {
			signedPartnerCert = getPartnerCertFromChain(responseObject.getSignedCertificateData());
		} catch (Exception ex) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPLOAD_PARTNER_CERT_FAILURE);
			LOGGER.error("Error occured while extracting the leaf cert", ex);
			throw new PartnerServiceException(ErrorCode.P7B_CERTDATA_ERROR.getErrorCode(),
					ErrorCode.P7B_CERTDATA_ERROR.getErrorMessage());
		}
		uploadOtherDomainCertificate(signedPartnerCert, partnerCertRequesteDto.getPartnerId());
		Partner updateObject = partner;
		updateObject.setUpdBy(getUser());
		updateObject.setUpdDtimes(Timestamp.valueOf(LocalDateTime.now()));
		updateObject.setCertificateAlias(responseObject.getCertificateId());
		updateObject.setIsActive(true);
		updateObject.setApprovalStatus(PartnerConstants.APPROVED);
		partnerRepository.save(updateObject);
		notify(MapperUtils.mapDataToPublishDto(updateObject, signedPartnerCert), EventType.PARTNER_UPDATED);
		notify(getDataShareurl(responseObject.getSignedCertificateData()), partnerCertRequesteDto.getPartnerDomain());
		responseObject.setSignedCertificateData(signedPartnerCert);
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPLOAD_PARTNER_CERT_SUCCESS);
		return responseObject;
	}	

	/**
	 * Uploading other domain certs
	 * 
	 * @param signedCertificateData
	 * @param partnerId
	 */
	private void uploadOtherDomainCertificate(String signedCertificateData, String partnerId) {
		RequestWrapper<UploadCertificateRequestDto> request = new RequestWrapper<>();
		UploadCertificateRequestDto requestDto = new UploadCertificateRequestDto();
		CACertificateResponseDto responseObject = null;
		requestDto.setApplicationId(applicationId);
		requestDto.setCertificateData(signedCertificateData);
		requestDto.setReferenceId(partnerId);
		request.setRequest(requestDto);
		Map<String, Object> uploadApiResponse = restUtil.postApi(
				environment.getProperty("pmp-keymanager.upload.other.domain.cert.rest.uri"), null, "", "",
				MediaType.APPLICATION_JSON, request, Map.class);
		try {
			responseObject = mapper.readValue(mapper.writeValueAsString(uploadApiResponse.get("response")),
					CACertificateResponseDto.class);
		} catch (Exception e) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPLOAD_PARTNER_CERT_FAILURE);
			LOGGER.error("Error occured while mapping the response of upload other domain cert api ", e);
			throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
					ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage() + e.getMessage());
		}
		if (responseObject == null && uploadApiResponse.containsKey(PartnerConstants.ERRORS)) {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> certServiceErrorList = (List<Map<String, Object>>) uploadApiResponse
					.get(PartnerConstants.ERRORS);
			if (!certServiceErrorList.isEmpty()) {
				auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPLOAD_PARTNER_CERT_FAILURE);
				throw new ApiAccessibleException(certServiceErrorList.get(0).get(PartnerConstants.ERRORCODE).toString(),
						certServiceErrorList.get(0).get(PartnerConstants.ERRORMESSAGE).toString());
			} else {
				auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPLOAD_PARTNER_CERT_FAILURE);
				LOGGER.error("Error occured while mapping the response of upload other domain cert api ",
						certServiceErrorList);
				throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
						ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage());
			}
		}
		if (responseObject == null) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPLOAD_PARTNER_CERT_FAILURE);
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
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.RETRIVE_PARTNER_CERT_FAILURE);
			throw new PartnerServiceException(ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		if (partnerFromDb.get().getCertificateAlias() == null || partnerFromDb.get().getCertificateAlias().isEmpty()) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.RETRIVE_PARTNER_CERT_FAILURE);
			throw new PartnerServiceException(ErrorCode.CERTIFICATE_NOT_UPLOADED_EXCEPTION.getErrorCode(),
					ErrorCode.CERTIFICATE_NOT_UPLOADED_EXCEPTION.getErrorMessage());
		}
		PartnerCertDownloadResponeDto responseObject = null;
		Map<String, String> pathsegments = new HashMap<>();
		pathsegments.put("partnerCertId", partnerFromDb.get().getCertificateAlias());
		Map<String, Object> getApiResponse = restUtil
				.getApi(environment.getProperty("pmp.partner.certificaticate.get.rest.uri"), pathsegments, Map.class);
		responseObject = mapper.readValue(mapper.writeValueAsString(getApiResponse.get("response")),
				PartnerCertDownloadResponeDto.class);
		if (responseObject == null && getApiResponse.containsKey(PartnerConstants.ERRORS)) {
			List<Map<String, Object>> certServiceErrorList = (List<Map<String, Object>>) getApiResponse
					.get(PartnerConstants.ERRORS);
			if (!certServiceErrorList.isEmpty()) {
				auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.RETRIVE_PARTNER_CERT_FAILURE);
				throw new ApiAccessibleException(certServiceErrorList.get(0).get(PartnerConstants.ERRORCODE).toString(),
						certServiceErrorList.get(0).get(PartnerConstants.ERRORMESSAGE).toString());
			} else {
				auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.RETRIVE_PARTNER_CERT_FAILURE);
				LOGGER.error("Error occurred while getting the cert {}", getApiResponse);
				throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
						ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage());
			}
		}
		if (responseObject == null) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.RETRIVE_PARTNER_CERT_FAILURE);
			throw new ApiAccessibleException(ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorCode(),
					ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorMessage());
		}
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.RETRIVE_PARTNER_CERT_SUCCESS);
		return responseObject;

	}

	@Override
	public String addBiometricExtractors(String partnerId, String policyId, ExtractorsDto extractors) {
		if (isApprovedPolicyRequestExists(partnerId, policyId)) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.ADD_BIO_EXTRACTORS_FAILURE);
			throw new PartnerServiceException(ErrorCode.PARTNER_API_KEY_REQUEST_APPROVED.getErrorCode(),
					ErrorCode.PARTNER_API_KEY_REQUEST_APPROVED.getErrorMessage());
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
			extractorProvider.setIsDeleted(false);
			extractorProviderRepository.save(extractorProvider);
		}
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.ADD_BIO_EXTRACTORS_SUCCESS);
		return "Extractors added successfully.";

	}

	/**
	 * Method to check weather approved policy exists for a given partner
	 * 
	 * @param partnerId
	 * @param policyId
	 * @return
	 */
	private boolean isApprovedPolicyRequestExists(String partnerId, String policyId) {
		List<PartnerPolicyRequest> partnerPolicyRequest = partnerPolicyRequestRepository
				.findByPartnerIdAndPolicyId(partnerId, policyId);
		if (partnerPolicyRequest.isEmpty()) {
			throw new PartnerServiceException(ErrorCode.PARTNER_POLICY_MAPPING_NOT_EXISTS.getErrorCode(),
					ErrorCode.PARTNER_POLICY_MAPPING_NOT_EXISTS.getErrorMessage());
		}
		if (partnerPolicyRequest.stream().filter(p -> p.getStatusCode().equalsIgnoreCase(PartnerConstants.APPROVED))
				.count() > 0) {
			return true;
		}
		;
		return false;
	}

	@Override
	public ExtractorsDto getBiometricExtractors(String partnerId, String policyId) {
		List<BiometricExtractorProvider> extractorsFromDb = extractorProviderRepository
				.findByPartnerAndPolicyId(partnerId, policyId);
		if (extractorsFromDb.isEmpty()) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.RETRIVE_BIO_EXTRACTORS_FAILURE);
			throw new PartnerServiceException(ErrorCode.NO_DETAILS_FOUND.getErrorCode(),
					ErrorCode.NO_DETAILS_FOUND.getErrorMessage());
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
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.RETRIVE_BIO_EXTRACTORS_SUCCESS);
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
			pageDto = pageUtils.sortPage(partners, dto.getSort(), dto.getPagination(), page.getTotalElements());
		}
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.SEARCH_PARTNER_SUCCESS);
		return pageDto;
	}

	@Override
	public PageResponseDto<PartnerType> searchPartnerType(SearchDto dto) {
		List<PartnerType> partnerTypes = new ArrayList<>();
		PageResponseDto<PartnerType> pageDto = new PageResponseDto<>();
		Page<PartnerType> page = partnerSearchHelper.search(entityManager, PartnerType.class, dto);
		if (page.getContent() != null && !page.getContent().isEmpty()) {
			partnerTypes = MapperUtils.mapAll(page.getContent(), PartnerType.class);
			pageDto = pageUtils.sortPage(partnerTypes, dto.getSort(), dto.getPagination(), page.getTotalElements());
		}
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.SEARCH_PARTNER_TYPE_SUCCESS);
		return pageDto;
	}

	@Override
	public String mapPartnerPolicyCredentialType(String credentialType, String partnerId, String policyId) {
		validateCredentialTypes(credentialType);
		Partner partner = getValidPartner(partnerId, false);
		if (!Arrays.stream(credentialTypesRequiredPartnerTypes.split(","))
				.anyMatch(partner.getPartnerTypeCode()::equalsIgnoreCase)) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.MAP_POLICY_CREDENTIAL_TYPE_FAILURE);
			throw new PartnerServiceException(ErrorCode.CREDENTIAL_NOT_ALLOWED_PARTNERS.getErrorCode(),
					ErrorCode.CREDENTIAL_NOT_ALLOWED_PARTNERS.getErrorMessage() + credentialTypesRequiredPartnerTypes);
		}
		validatePolicyGroupAndPolicy(partner.getPolicyGroupId(), policyId);
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
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.MAP_POLICY_CREDENTIAL_TYPE_SUCCESS);
		return "Partner, policy and credentialType mapping done successfully.";
	}

	private void validateCredentialTypes(String credentialType) {
		if (!Arrays.stream(allowedCredentialTypes.split(",")).anyMatch(credentialType::equalsIgnoreCase)) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.MAP_POLICY_CREDENTIAL_TYPE_FAILURE);
			throw new PartnerServiceException(ErrorCode.CREDENTIAL_TYPE_NOT_ALLOWED.getErrorCode(),
					ErrorCode.CREDENTIAL_TYPE_NOT_ALLOWED.getErrorMessage() + allowedCredentialTypes);
		}
	}

	@Override
	public PartnerCredentialTypePolicyDto getPartnerCredentialTypePolicy(String credentialType, String partnerId)
			throws JsonParseException, JsonMappingException, IOException {
		PartnerPolicyCredentialType partnerCredentialTypePolicy = partnerCredentialTypePolicyRepo
				.findByPartnerIdAndCrdentialType(partnerId, credentialType);
		if (partnerCredentialTypePolicy == null) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.RETRIVE_POLICY_CREDENTIAL_TYPE_FAILURE);
			throw new PartnerServiceException(ErrorCode.NO_DETAILS_FOUND.getErrorCode(),
					ErrorCode.NO_DETAILS_FOUND.getErrorMessage());
		}
		Optional<AuthPolicy> authPolicy = authPolicyRepository
				.findById(partnerCredentialTypePolicy.getId().getPolicyId());
		if (authPolicy.isEmpty()) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.RETRIVE_POLICY_CREDENTIAL_TYPE_FAILURE);
			throw new PartnerServiceException(ErrorCode.POLICY_NOT_EXIST.getErrorCode(),
					ErrorCode.POLICY_NOT_EXIST.getErrorMessage());
		}
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.RETRIVE_POLICY_CREDENTIAL_TYPE_SUCCESS);
		return mapPolicyToResponseDto(authPolicy.get(), partnerId, credentialType);
	}

	/**
	 * 
	 * @param authPolicy
	 * @param partnerId
	 * @param credentialType
	 * @return
	 */
	private PartnerCredentialTypePolicyDto mapPolicyToResponseDto(AuthPolicy authPolicy, String partnerId,
			String credentialType) {
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
	 * @param policyFileId
	 * @return
	 */
	private JSONObject getPolicyObject(String policyFileId) {
		JSONParser parser = new JSONParser();
		String error = null;
		try {
			return ((JSONObject) parser.parse(policyFileId));
		} catch (ParseException e) {
			error = e.getMessage();
		}
		throw new PartnerServiceException(ErrorCode.POLICY_PARSING_ERROR.getErrorCode(),
				ErrorCode.POLICY_PARSING_ERROR.getErrorMessage() + error);
	}

	@Override
	public FilterResponseCodeDto filterValues(FilterValueDto filterValueDto) {
		FilterResponseCodeDto filterResponseDto = new FilterResponseCodeDto();
		List<ColumnCodeValue> columnValueList = new ArrayList<>();
		if (filterColumnValidator.validate(FilterDto.class, filterValueDto.getFilters(), Partner.class)) {
			for (FilterDto filterDto : filterValueDto.getFilters()) {
				List<FilterData> filterValues = filterHelper.filterValuesWithCode(entityManager, Partner.class,
						filterDto, filterValueDto, "id");
				filterValues.forEach(filterValue -> {
					ColumnCodeValue columnValue = new ColumnCodeValue();
					columnValue.setFieldCode(filterValue.getFieldCode());
					columnValue.setFieldID(filterDto.getColumnName());
					columnValue.setFieldValue(filterValue.getFieldValue());
					columnValueList.add(columnValue);
				});
			}
			filterResponseDto.setFilters(columnValueList);
		}
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.FILTER_PARTNER_SUCCESS);
		return filterResponseDto;
	}

	@Override
	public FilterResponseCodeDto apiKeyRequestFilter(FilterValueDto filterValueDto) {
		FilterResponseCodeDto filterResponseDto = new FilterResponseCodeDto();
		List<ColumnCodeValue> columnValueList = new ArrayList<>();
		if (filterColumnValidator.validate(FilterDto.class, filterValueDto.getFilters(), PartnerPolicyRequest.class)) {
			for (FilterDto filterDto : filterValueDto.getFilters()) {
				List<FilterData> filterValues = filterHelper.filterValuesWithCode(entityManager,
						PartnerPolicyRequest.class, filterDto, filterValueDto, "id");
				filterValues.forEach(filterValue -> {
					ColumnCodeValue columnValue = new ColumnCodeValue();
					columnValue.setFieldCode(filterValue.getFieldCode());
					columnValue.setFieldID(filterDto.getColumnName());
					columnValue.setFieldValue(filterValue.getFieldValue());
					columnValueList.add(columnValue);
				});
			}
			filterResponseDto.setFilters(columnValueList);
		}
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.FILTER_PARTNER_APIKEY_REQUESTS_SUCCESS);
		return filterResponseDto;
	}

	@Override
	public PageResponseDto<PartnerPolicySearchResponseDto> searchPartnerApiKeys(SearchDto dto) {
		List<PartnerPolicySearchResponseDto> partnerMappedPolicies = new ArrayList<>();
		PageResponseDto<PartnerPolicySearchResponseDto> pageDto = new PageResponseDto<>();
		Page<PartnerPolicy> page = partnerSearchHelper.search(entityManager, PartnerPolicy.class, dto);
		if (page.getContent() != null && !page.getContent().isEmpty()) {
			partnerMappedPolicies = mapPartnerPolicies(page.getContent());
			pageDto = pageUtils.sortPage(partnerMappedPolicies, dto.getSort(), dto.getPagination(),
					page.getTotalElements());
		}
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.SEARCH_PARTNER_APIKEY_REQUEST_SUCCESS);
		return pageDto;
	}

	/**
	 * 
	 * @param content
	 * @return
	 */
	private List<PartnerPolicySearchResponseDto> mapPartnerPolicies(List<PartnerPolicy> content) {
		Objects.requireNonNull(content);
		List<PartnerPolicySearchResponseDto> partnerPolicyList = new ArrayList<>();
		content.forEach(partnerPolicy -> {
			PartnerPolicySearchResponseDto searchResponse = new PartnerPolicySearchResponseDto();
			searchResponse.setPolicyApiKey(partnerPolicy.getPolicyApiKey());
			searchResponse.setPartnerId(partnerPolicy.getPartner().getId());
			searchResponse.setPolicyId(partnerPolicy.getPolicyId());
			searchResponse.setValidFromDatetime(partnerPolicy.getValidFromDatetime());
			searchResponse.setValidToDatetime(partnerPolicy.getValidToDatetime());
			searchResponse.setIsActive(partnerPolicy.getIsActive());
			searchResponse.setIsDeleted(partnerPolicy.getIsDeleted());
			searchResponse.setCrBy(partnerPolicy.getCrBy());
			searchResponse.setCrDtimes(partnerPolicy.getCrDtimes());
			searchResponse.setUpdBy(partnerPolicy.getUpdBy());
			searchResponse.setUpdDtimes(partnerPolicy.getUpdDtimes());
			searchResponse.setDelDtimes(partnerPolicy.getDelDtimes());
			partnerPolicyList.add(searchResponse);
		});
		return partnerPolicyList;
	}

	@Override
	public PageResponseDto<PolicyRequestSearchResponseDto> searchPartnerApiKeyRequests(SearchDto dto) {
		List<PolicyRequestSearchResponseDto> partnerPolicyRequests = new ArrayList<>();
		PageResponseDto<PolicyRequestSearchResponseDto> pageDto = new PageResponseDto<>();
		Page<PartnerPolicyRequest> page = partnerSearchHelper.search(entityManager, PartnerPolicyRequest.class, dto);
		if (page.getContent() != null && !page.getContent().isEmpty()) {
			partnerPolicyRequests = mapPolicyRequests(page.getContent());
			pageDto = pageUtils.sortPage(partnerPolicyRequests, dto.getSort(), dto.getPagination(),
					page.getTotalElements());
		}
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.SEARCH_PARTNER_APIKEY_SUCCESS);
		return pageDto;
	}

	/**
	 * 
	 * @param content
	 * @return
	 */
	private List<PolicyRequestSearchResponseDto> mapPolicyRequests(List<PartnerPolicyRequest> content) {
		Objects.requireNonNull(content);
		List<PolicyRequestSearchResponseDto> policyRequestList = new ArrayList<>();
		content.forEach(policyRequest -> {
			PolicyRequestSearchResponseDto searchPolicyRequest = new PolicyRequestSearchResponseDto();
			searchPolicyRequest.setApikeyRequestId(policyRequest.getId());
			searchPolicyRequest.setPartnerId(policyRequest.getPartner().getId());
			searchPolicyRequest.setPolicyId(policyRequest.getPolicyId());
			searchPolicyRequest.setRequestDatetimes(policyRequest.getRequestDatetimes());
			searchPolicyRequest.setRequestDetail(policyRequest.getRequestDetail());
			searchPolicyRequest.setStatusCode(policyRequest.getStatusCode());
			searchPolicyRequest.setCrBy(policyRequest.getCrBy());
			searchPolicyRequest.setCrDtimes(policyRequest.getCrDtimes());
			searchPolicyRequest.setUpdBy(policyRequest.getUpdBy());
			searchPolicyRequest.setUpdDtimes(policyRequest.getUpdDtimes());
			searchPolicyRequest.setIsDeleted(policyRequest.getIsDeleted());
			searchPolicyRequest.setDelDtimes(policyRequest.getDelDtimes());
			policyRequestList.add(searchPolicyRequest);
		});
		return policyRequestList;
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
	 * @param certData
	 * @param partnerDomain
	 */
	private void notify(String certData, String partnerDomain) {
		Type type = new Type();
		type.setName("PartnerServiceImpl");
		type.setNamespace("io.mosip.pmp.partner.service.impl.PartnerServiceImpl");
		Map<String, Object> data = new HashMap<>();
		data.put(PartnerConstants.CERT_CHAIN_DATA_SHARE_URL, certData);
		data.put(PartnerConstants.PARTNER_DOMAIN, partnerDomain);
		webSubPublisher.notify(EventType.CA_CERTIFICATE_UPLOADED, data, type);
	}
	
	private void notify(PartnerDataPublishDto mapDataToPublishDto, EventType partnerUpdated) {
		Type type = new Type();
		type.setName("PartnerServiceImpl");
		type.setNamespace("io.mosip.pmp.partner.service.impl.PartnerServiceImpl");
		Map<String, Object> data = new HashMap<>();
		data.put(PartnerConstants.PARTNER_DATA, mapDataToPublishDto);
		webSubPublisher.notify(partnerUpdated, data, type);		
	}

	/**
	 * 
	 * @param eventType
	 * @param partner
	 */
	private void sendNotifications(EventType eventType, Partner partner) {
		List<NotificationDto> notificationDtos = new ArrayList<NotificationDto>();
		NotificationDto dto = new NotificationDto();
		dto.setPartnerId(partner.getId());
		dto.setPartnerName(partner.getName());
		dto.setEmailId(partner.getEmailId());
		dto.setPartnerStatus(partner.getIsActive() == true ? PartnerConstants.ACTIVE : PartnerConstants.DEACTIVE);
		notificationDtos.add(dto);
		try {
			notificationService.sendNotications(eventType, notificationDtos);
		} catch (Exception e) {
			LOGGER.error("Error occured while sending the partner register notifications.", e.getLocalizedMessage(),
					e.getMessage());
		}
	}

	/**
	 * Method to extract the leaf certificate from complete chain of a certificate.
	 * 
	 * @param certChain
	 * @return
	 * @throws Exception
	 */
	private String getPartnerCertFromChain(String certChain) throws Exception {
		byte[] p7bBytes = CryptoUtil.decodeBase64(certChain);
		try (ByteArrayInputStream certStream = new ByteArrayInputStream(p7bBytes)) {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			Collection<?> p7bCertList = cf.generateCertificates(certStream);
			List<Certificate> certList = new ArrayList<>();
			p7bCertList.forEach(cert -> {
				certList.add((Certificate) cert);
			});
			Base64.Encoder base64Encoder = Base64.getMimeEncoder(64, LINE_SEPARATOR.getBytes());
			byte[] certificateData = certList.get(0).getEncoded();
			String encodedCertificateData = new String(base64Encoder.encode(certificateData));
			StringBuilder leafSignedCert = new StringBuilder();
			leafSignedCert.append(BEGIN_CERTIFICATE);
			leafSignedCert.append(LINE_SEPARATOR);
			leafSignedCert.append(encodedCertificateData);
			leafSignedCert.append(LINE_SEPARATOR);
			leafSignedCert.append(END_CERTIFICATE);
			return leafSignedCert.toString();
		} catch (CertificateException | IOException exp) {
			LOGGER.error("Error Parsing P7B Certificate data.", exp);
			throw new PartnerServiceException(ErrorCode.P7B_CERTDATA_PARSING_ERROR.getErrorCode(),
					ErrorCode.P7B_CERTDATA_PARSING_ERROR.getErrorMessage());
		}
	}

	/**
	 * 
	 * @param certsChain
	 * @return
	 */
	private String getDataShareurl(String certsChain) {
		MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
		String fileName = "certsChain";
		map.add("name", fileName);
		map.add("filename", fileName);
		ByteArrayResource contentsAsResource = new ByteArrayResource(certsChain.getBytes()) {
			@Override
			public String getFilename() {
				return fileName;
			}
		};
		map.add("file", contentsAsResource);
		List<String> pathSegments = new ArrayList<>();
		pathSegments.add(policyId);
		pathSegments.add(subscriberId);
		DataShareResponseDto response = restUtil.postApi(
				environment.getProperty("pmp.certificaticate.datashare.rest.uri"), pathSegments, "", "",
				MediaType.MULTIPART_FORM_DATA, map, DataShareResponseDto.class);
		if (response == null) {
			throw new PartnerServiceException(ErrorCode.DATASHARE_RESPONSE_NULL.getErrorCode(),
					ErrorCode.DATASHARE_RESPONSE_NULL.getErrorMessage());
		}
		if ((response.getErrors() != null && response.getErrors().size() > 0)) {
			throw new PartnerServiceException(response.getErrors().get(0).getErrorCode(),
					response.getErrors().get(0).getMessage());
		}
		System.out.println(response.getDataShare().getUrl());
		return response.getDataShare().getUrl();
	}
}