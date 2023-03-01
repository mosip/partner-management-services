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
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.logger.spi.Logger;
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
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.common.util.PageUtils;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.common.util.UserDetailUtil;
import io.mosip.pms.common.validator.FilterColumnValidator;
import io.mosip.pms.device.response.dto.ColumnCodeValue;
import io.mosip.pms.device.response.dto.FilterResponseCodeDto;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.constant.PartnerConstants;
import io.mosip.pms.partner.constant.PartnerServiceAuditEnum;
import io.mosip.pms.partner.dto.DataShareResponseDto;
import io.mosip.pms.partner.dto.PartnerPolicyMappingResponseDto;
import io.mosip.pms.partner.dto.UploadCertificateRequestDto;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.request.dto.AddContactRequestDto;
import io.mosip.pms.partner.request.dto.CACertificateRequestDto;
import io.mosip.pms.partner.request.dto.ExtractorDto;
import io.mosip.pms.partner.request.dto.ExtractorProviderDto;
import io.mosip.pms.partner.request.dto.ExtractorsDto;
import io.mosip.pms.partner.request.dto.PartnerCertDownloadRequestDto;
import io.mosip.pms.partner.request.dto.PartnerCertificateRequestDto;
import io.mosip.pms.partner.request.dto.PartnerCertificateUploadRequestDto;
import io.mosip.pms.partner.request.dto.PartnerPolicyMappingRequest;
import io.mosip.pms.partner.request.dto.PartnerRequest;
import io.mosip.pms.partner.request.dto.PartnerRequestDto;
import io.mosip.pms.partner.request.dto.PartnerSearchDto;
import io.mosip.pms.partner.request.dto.PartnerUpdateDto;
import io.mosip.pms.partner.request.dto.PartnerUpdateRequest;
import io.mosip.pms.partner.response.dto.APIkeyRequests;
import io.mosip.pms.partner.response.dto.CACertificateResponseDto;
import io.mosip.pms.partner.response.dto.EmailVerificationResponseDto;
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

	private static final Logger LOGGER = PMSLogger.getLogger(PartnerServiceImpl.class);

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
	
	@Value("${mosip.mandatory-languages}")
	private String mandatoryLanguges;

	@Value("${mosip.optional-languages}")
	private String optionalLanguges;

	@Autowired
	AuditUtil auditUtil;

	@Value("${pmp.partner.mobileNumber.max.length:16}")
	private int maxMobileNumberLength;

	private String emptySpacesRegex = ".*\\s.*";
	
	@Override
	public PartnerResponse registerPartner(PartnerRequestDto request) {
		// Registered partner cannot create another partner 
		String loggedInUserEmail = getLoggedInUserEmail();
		if(loggedInUserEmail != null && isPartnerExistsWithEmail(loggedInUserEmail).getEmailExists()) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.REGISTER_PARTNER_FAILURE, request.getPartnerId(),
					"partnerId");
			throw new PartnerServiceException(ErrorCode.LOGGEDIN_USER_NOT_AUTHORIZED.getErrorCode(),
					ErrorCode.LOGGEDIN_USER_NOT_AUTHORIZED.getErrorMessage());
		}
		if (!validateMobileNumeber(request.getContactNumber())) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.REGISTER_PARTNER_FAILURE, request.getPartnerId(),
					"partnerId");
			throw new PartnerServiceException(ErrorCode.INVALID_MOBILE_NUMBER_EXCEPTION.getErrorCode(),
					ErrorCode.INVALID_MOBILE_NUMBER_EXCEPTION.getErrorMessage() + maxMobileNumberLength);
		}

		if (!validateEmail(request.getEmailId())) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.REGISTER_PARTNER_FAILURE, request.getPartnerId(),
					"partnerId");
			throw new PartnerServiceException(ErrorCode.INVALID_EMAIL_ID_EXCEPTION.getErrorCode(),
					ErrorCode.INVALID_EMAIL_ID_EXCEPTION.getErrorMessage());
		}
		if(isInputStringContainsSpaces(request.getPartnerId())) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.REGISTER_PARTNER_FAILURE, request.getPartnerId(),
					"partnerId");
			throw new PartnerServiceException(ErrorCode.PARTNER_ID_CONTAINS_SPACES.getErrorCode(),
					ErrorCode.PARTNER_ID_CONTAINS_SPACES.getErrorMessage() + partnerIdMaxLength);
		}		
		if (!validatePartnerIdLength(request.getPartnerId())) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.REGISTER_PARTNER_FAILURE, request.getPartnerId(),
					"partnerId");
			throw new PartnerServiceException(ErrorCode.PARTNER_ID_LENGTH_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_ID_LENGTH_EXCEPTION.getErrorMessage() + partnerIdMaxLength);
		}

		if (!validatePartnerByEmail(request.getEmailId())) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.REGISTER_PARTNER_FAILURE, request.getPartnerId(),
					"partnerId");
			throw new PartnerServiceException(ErrorCode.EMAIL_ALREADY_EXISTS_EXCEPTION.getErrorCode(),
					ErrorCode.EMAIL_ALREADY_EXISTS_EXCEPTION.getErrorMessage());
		}
		if (request.getLangCode() != null && !getSystemSupportedLanguageCodes().contains(request.getLangCode())) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.REGISTER_PARTNER_FAILURE, request.getPartnerId(),
					"partnerId");
			throw new PartnerServiceException(ErrorCode.PARTNER_LANG_CODE_NOT_SUPPORTED.getErrorCode(),
					ErrorCode.PARTNER_LANG_CODE_NOT_SUPPORTED.getErrorMessage());
		}else {
			request.setLangCode(getSystemSupportedLanguageCodes().get(0));
		}
		if (!validatePartnerId(request.getPartnerId())) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.REGISTER_PARTNER_FAILURE, request.getPartnerId(),
					"partnerId");
			throw new PartnerServiceException(ErrorCode.PARTNER_ALREADY_REGISTERED_WITH_ID_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_ALREADY_REGISTERED_WITH_ID_EXCEPTION.getErrorMessage());
		}
		
		if(request.getAdditionalInfo() != null) {
			isJSONValid(request.getAdditionalInfo().toString());
		}
		
		PartnerType partnerType = validateAndGetPartnerType(request.getPartnerType());
		PolicyGroup policyGroup = null;
		if (partnerType.getIsPolicyRequired() &&  (request.getPolicyGroup()==null||request.getPolicyGroup().isEmpty())) {
			LOGGER.error("Policy Group is mandatory for "+partnerType.getCode());
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.REGISTER_PARTNER_FAILURE);
			throw new PartnerServiceException(ErrorCode.POLICY_GROUP_IS_MANDATORY.getErrorCode(),
					String.format(ErrorCode.POLICY_GROUP_IS_MANDATORY.getErrorMessage(),partnerType.getCode()));
		}
		if(request.getPolicyGroup()!=null && !request.getPolicyGroup().isEmpty()) {
			policyGroup = validateAndGetPolicyGroupByName(request.getPolicyGroup());
		}
		Partner partner = mapPartnerFromRequest(request, policyGroup, partnerType.getCode());		
		partner.setPartnerTypeCode(partnerType.getCode());
		partnerRepository.save(partner);
		saveToPartnerH(partner);
		PartnerResponse partnerResponse = new PartnerResponse();
		partnerResponse.setPartnerId(partner.getId());
		partnerResponse.setStatus(partner.getApprovalStatus());
		sendNotifications(EventType.PARTNER_REGISTRED, partner);
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.REGISTER_PARTNER_SUCCESS, request.getPartnerId(),
				"partnerId");
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
		partnerHistory.setLogoUrl(partner.getLogoUrl());
		partnerHistory.setAdditionalInfo(partner.getAdditionalInfo());
		partnerHRepository.save(partnerHistory);
	}

	private Partner mapPartnerFromRequest(PartnerRequestDto request, PolicyGroup policyGroup, String partnerType) {
		Partner partner = new Partner();
		partner.setId(request.getPartnerId());
		partner.setPolicyGroupId(policyGroup != null ? policyGroup.getId() : null);
		partner.setName(request.getOrganizationName());
		partner.setAddress(request.getAddress());
		partner.setContactNo(request.getContactNumber());
		partner.setPartnerTypeCode(partnerType);
		partner.setEmailId(request.getEmailId());
		partner.setPartnerTypeCode(partnerType);
		partner.setIsActive(false);
		partner.setIsDeleted(false);
		partner.setLangCode(request.getLangCode());
		partner.setUserId(request.getPartnerId());
		partner.setCrBy(getLoggedInUserId());
		partner.setApprovalStatus(PartnerConstants.IN_PROGRESS);
		partner.setLogoUrl(request.getLogoUrl());
		partner.setAdditionalInfo(request.getAdditionalInfo()== null ? "[]" : request.getAdditionalInfo().toString());
		partner.setCrDtimes(Timestamp.valueOf(LocalDateTime.now()));
		return partner;
	}

	private PolicyGroup validateAndGetPolicyGroupByName(String policyGroupName) {
		PolicyGroup policyGroupFromDb = policyGroupRepository.findByName(policyGroupName);
		if (policyGroupFromDb == null) {
			LOGGER.error(policyGroupName + " : Policy Group is not available");
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.REGISTER_PARTNER_FAILURE, policyGroupName, "policyGroupName");
			throw new PartnerServiceException(ErrorCode.POLICY_GROUP_DOES_NOT_EXIST.getErrorCode(),
					ErrorCode.POLICY_GROUP_DOES_NOT_EXIST.getErrorMessage());
		}
		if(!policyGroupFromDb.getIsActive()) {
			LOGGER.error("Policy group with name" + policyGroupName + " : is not active");
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.REGISTER_PARTNER_FAILURE, policyGroupName, "policyGroupName");
			throw new PartnerServiceException(ErrorCode.POLICY_GROUP_NOT_ACTIVE.getErrorCode(),
					ErrorCode.POLICY_GROUP_NOT_ACTIVE.getErrorMessage());			
		}
		return policyGroupFromDb;
	}

	private PartnerType validateAndGetPartnerType(String partnerType) {
		List<PartnerType> partnerTypesFromDb = getAllPartnerTypes();
		Optional<PartnerType> validPartnerType = partnerTypesFromDb.stream()
				.filter(pt -> pt.getCode().equalsIgnoreCase(partnerType)).findFirst();
		if (validPartnerType.isEmpty()) {
			LOGGER.error(partnerType + " : partnerType is not available.");
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.REGISTER_PARTNER_FAILURE, partnerType, "partnerType");
			throw new PartnerServiceException(ErrorCode.PARTNER_TYPE_DOES_NOT_EXIST.getErrorCode(),
					ErrorCode.PARTNER_TYPE_DOES_NOT_EXIST.getErrorMessage());
		}
		return validPartnerType.get();
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
		response.setStatus(partner.getIsActive() == true ? PartnerConstants.ACTIVE : PartnerConstants.DEACTIVE);
		response.setIsActive(partner.getIsActive());
		if (partner.getPolicyGroupId() != null) {
			response.setPolicyGroup(validateAndGetPolicyGroupById(partner.getPolicyGroupId()).getName());
		}
		return response;
	}

	private Partner getValidPartner(String partnerId, boolean isToRetrieve) {
		Optional<Partner> partnerById = partnerRepository.findById(partnerId);
		if (partnerById.isEmpty()) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.RETRIVE_PARTNER_FAILURE, partnerId, "partnerId");
			throw new PartnerServiceException(ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		if (!isToRetrieve) {
			if (!partnerById.get().getIsActive()) {
				auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.RETRIVE_PARTNER_FAILURE, partnerId, "partnerId");
				throw new PartnerServiceException(ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorCode(),
						ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorMessage());
			}
		}
		return partnerById.get();
	}

	private PolicyGroup validateAndGetPolicyGroupById(String policyGroupId) {
		Optional<PolicyGroup> policyGroupFromDb = policyGroupRepository.findById(policyGroupId);
		if (policyGroupFromDb.isEmpty()) {
			LOGGER.error("Policy group is not available with id {} ", policyGroupId);
			throw new PartnerServiceException(ErrorCode.POLICY_GROUP_DOES_NOT_EXIST.getErrorCode(),
					ErrorCode.POLICY_GROUP_DOES_NOT_EXIST.getErrorMessage());
		}
		return policyGroupFromDb.get();
	}

	@Override
	public PartnerResponse updatePartnerDetails(PartnerUpdateDto partnerUpdateRequest, String partnerId) {
		validateLoggedInUserAuthorization(partnerId);
		if (!validateMobileNumeber(partnerUpdateRequest.getContactNumber())) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPDATE_PARTNER_FAILURE, partnerId, "partnerId");
			throw new PartnerServiceException(ErrorCode.INVALID_MOBILE_NUMBER_EXCEPTION.getErrorCode(),
					ErrorCode.INVALID_MOBILE_NUMBER_EXCEPTION.getErrorMessage() + maxMobileNumberLength);
		};
		Partner partner = getValidPartner(partnerId, true);
		if(partnerUpdateRequest.getAdditionalInfo() != null) {
			isJSONValid(partnerUpdateRequest.getAdditionalInfo().toString());
		}
		partner.setAddress(partnerUpdateRequest.getAddress());
		partner.setContactNo(partnerUpdateRequest.getContactNumber());
		partner.setAdditionalInfo(partnerUpdateRequest.getAdditionalInfo()== null ? "[]" : partnerUpdateRequest.getAdditionalInfo().toString());
		partner.setLogoUrl(partnerUpdateRequest.getLogoUrl());
		partner.setUpdBy(getLoggedInUserId());
		partner.setUpdDtimes(Timestamp.valueOf(LocalDateTime.now()));
		partnerRepository.save(partner);
		saveToPartnerH(partner);
		PartnerResponse updateResponse = new PartnerResponse();
		updateResponse.setPartnerId(partner.getId());
		updateResponse.setStatus(partner.getApprovalStatus());
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPDATE_PARTNER_SUCCESS, partnerId, "partnerId");
		return updateResponse;
	}

	private AuthPolicy validatePolicyGroupAndPolicy(String policyGroupId, String policyName) {
		AuthPolicy authPolicyFromDb = authPolicyRepository.findByPolicyGroupAndName(policyGroupId, policyName);
		if (authPolicyFromDb == null) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.SUBMIT_API_REQUEST_FAILURE, policyName, "policyName");
			throw new PartnerServiceException(ErrorCode.POLICY_GROUP_POLICY_NOT_EXISTS.getErrorCode(),
					ErrorCode.POLICY_GROUP_POLICY_NOT_EXISTS.getErrorMessage());
		}

		if (!authPolicyFromDb.getIsActive()) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.SUBMIT_API_REQUEST_FAILURE, policyName, "policyName");
			throw new PartnerServiceException(ErrorCode.POLICY_NOT_ACTIVE_EXCEPTION.getErrorCode(),
					ErrorCode.POLICY_NOT_ACTIVE_EXCEPTION.getErrorMessage());

		}
		if (authPolicyFromDb.getValidToDate().isBefore(LocalDateTime.now())) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.SUBMIT_API_REQUEST_FAILURE, policyName, "policyName");
			throw new PartnerServiceException(ErrorCode.POLICY_EXPIRED_EXCEPTION.getErrorCode(),
					ErrorCode.POLICY_EXPIRED_EXCEPTION.getErrorMessage());

		}
		if (!authPolicyFromDb.getPolicyGroup().getIsActive()) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.SUBMIT_API_REQUEST_FAILURE, policyName, "policyName");
			throw new PartnerServiceException(ErrorCode.POLICY_GROUP_NOT_ACTIVE.getErrorCode(),
					ErrorCode.POLICY_GROUP_NOT_ACTIVE.getErrorMessage());
		}
		return authPolicyFromDb;
	}	

	/**
	 * 
	 * @param apiKey
	 * @return
	 */
	private PartnerPolicy getPartnerMappedPolicy(String apiKey) {
		return partnerPolicyRepository.findByApiKey(apiKey);
	}

	@Override
	public List<APIkeyRequests> retrieveAllApiKeyRequestsSubmittedByPartner(String partnerId) {
		List<PartnerPolicyRequest> apikeyRequestsByPartner = partnerPolicyRequestRepository.findByPartnerId(partnerId);
		if (apikeyRequestsByPartner.isEmpty()) {
			LOGGER.error("No apiKey requests exists for given partner {} ", partnerId);  			
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
		return apikeyRequests;
	}

	@Override
	public String createAndUpdateContactDetails(AddContactRequestDto request, String partnerId) {
		if (!validateEmail(request.getEmailId())) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.ADD_CONTACTS_FAILURE, partnerId, "partnerId");
			throw new PartnerServiceException(ErrorCode.INVALID_EMAIL_ID_EXCEPTION.getErrorCode(),
					ErrorCode.INVALID_EMAIL_ID_EXCEPTION.getErrorMessage());
		}

		if (!validateMobileNumeber(request.getContactNumber())) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.ADD_CONTACTS_FAILURE, partnerId, "partnerId");
			throw new PartnerServiceException(ErrorCode.INVALID_MOBILE_NUMBER_EXCEPTION.getErrorCode(),
					ErrorCode.INVALID_MOBILE_NUMBER_EXCEPTION.getErrorMessage() + maxMobileNumberLength);
		}

		PartnerContact contactsFromDb = partnerContactRepository.findByPartnerAndEmail(partnerId, request.getEmailId());
		String resultMessage;
		if (contactsFromDb != null) {
			contactsFromDb.setAddress(request.getAddress());
			contactsFromDb.setContactNo(request.getContactNumber());
			contactsFromDb.setIsActive(request.getIs_Active());
			contactsFromDb.setUpdBy(getLoggedInUserId());
			contactsFromDb.setUpdDtimes(LocalDateTime.now());
			resultMessage = "Contacts details updated successfully.";
		} else {
			Partner partnerFromDb = getValidPartner(partnerId, false);
			contactsFromDb = new PartnerContact();
			contactsFromDb.setId(PartnerUtil.createPartnerId());
			contactsFromDb.setAddress(request.getAddress());
			contactsFromDb.setContactNo(request.getContactNumber());
			contactsFromDb.setCrBy(getLoggedInUserId());
			contactsFromDb.setCrDtimes(LocalDateTime.now());
			contactsFromDb.setPartner(partnerFromDb);
			contactsFromDb.setEmailId(request.getEmailId());
			contactsFromDb.setIsActive(request.getIs_Active());
			contactsFromDb.setIsDeleted(false);
			resultMessage = "Contacts details added successfully.";
		}
		partnerContactRepository.save(contactsFromDb);
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.ADD_CONTACTS_SUCCESS, partnerId, "partnerId");
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
		validateLoggedInUserAuthorization(partnerCertRequesteDto.getPartnerId());
		Partner partner = getValidPartner(partnerCertRequesteDto.getPartnerId(), true);
		PartnerType partnerType = validateAndGetPartnerType(partner.getPartnerTypeCode());
		if (partnerType.getIsPolicyRequired() && partner.getPolicyGroupId() == null) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPLOAD_PARTNER_CERT_FAILURE, partnerCertRequesteDto.getPartnerId(), "partnerId");
			throw new PartnerServiceException(ErrorCode.POLICY_GROUP_NOT_MAPPED_PARTNER.getErrorCode(),
					ErrorCode.POLICY_GROUP_NOT_MAPPED_PARTNER.getErrorMessage());
		}
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
				auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPLOAD_PARTNER_CERT_FAILURE, partnerCertRequesteDto.getPartnerId(), "partnerId");
				throw new ApiAccessibleException(certServiceErrorList.get(0).get(PartnerConstants.ERRORCODE).toString(),
						certServiceErrorList.get(0).get(PartnerConstants.ERRORMESSAGE).toString());
			} else {
				auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPLOAD_PARTNER_CERT_FAILURE, partnerCertRequesteDto.getPartnerId(), "partnerId");
				throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
						ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage());
			}
		}
		if (responseObject == null) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPLOAD_PARTNER_CERT_FAILURE, partnerCertRequesteDto.getPartnerId(), "partnerId");
			throw new ApiAccessibleException(ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorCode(),
					ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorMessage());
		}

		String signedPartnerCert = null;
		try {
			signedPartnerCert = getPartnerCertFromChain(responseObject.getSignedCertificateData());
		} catch (Exception ex) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPLOAD_PARTNER_CERT_FAILURE, partnerCertRequesteDto.getPartnerId(), "partnerId");
			LOGGER.error("Error occured while extracting the leaf cert", ex);
			throw new PartnerServiceException(ErrorCode.P7B_CERTDATA_ERROR.getErrorCode(),
					ErrorCode.P7B_CERTDATA_ERROR.getErrorMessage());
		}
		uploadOtherDomainCertificate(signedPartnerCert, partnerCertRequesteDto.getPartnerId());
		Partner updateObject = partner;
		updateObject.setUpdBy(getLoggedInUserId());
		updateObject.setUpdDtimes(Timestamp.valueOf(LocalDateTime.now()));
		updateObject.setCertificateAlias(responseObject.getCertificateId());
		updateObject.setIsActive(true);
		updateObject.setApprovalStatus(PartnerConstants.APPROVED);
		partnerRepository.save(updateObject);
		notify(MapperUtils.mapDataToPublishDto(updateObject, signedPartnerCert), EventType.PARTNER_UPDATED);
		notify(getDataShareurl(responseObject.getSignedCertificateData()), partnerCertRequesteDto.getPartnerDomain());
		responseObject.setSignedCertificateData(signedPartnerCert);
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPLOAD_PARTNER_CERT_SUCCESS, partnerCertRequesteDto.getPartnerId(), "partnerId");
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
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPLOAD_PARTNER_CERT_FAILURE, partnerId, "partnerId");
			LOGGER.error("Error occured while mapping the response of upload other domain cert api ", e);
			throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
					ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage() + e.getMessage());
		}
		if (responseObject == null && uploadApiResponse.containsKey(PartnerConstants.ERRORS)) {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> certServiceErrorList = (List<Map<String, Object>>) uploadApiResponse
					.get(PartnerConstants.ERRORS);
			if (!certServiceErrorList.isEmpty()) {
				auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPLOAD_PARTNER_CERT_FAILURE, partnerId, "partnerId");
				throw new ApiAccessibleException(certServiceErrorList.get(0).get(PartnerConstants.ERRORCODE).toString(),
						certServiceErrorList.get(0).get(PartnerConstants.ERRORMESSAGE).toString());
			} else {
				auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPLOAD_PARTNER_CERT_FAILURE, partnerId, "partnerId");
				LOGGER.error("Error occured while mapping the response of upload other domain cert api ",
						certServiceErrorList);
				throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
						ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage());
			}
		}
		if (responseObject == null) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.UPLOAD_PARTNER_CERT_FAILURE, partnerId, "partnerId");
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
			LOGGER.error("Partner not exists with id {}", certDownloadRequestDto.getPartnerId());
			throw new PartnerServiceException(ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		if (partnerFromDb.get().getCertificateAlias() == null || partnerFromDb.get().getCertificateAlias().isEmpty()) {
			LOGGER.error("Cert is not uploaded for given partner {} ", certDownloadRequestDto.getPartnerId());
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
				LOGGER.error("Error occured while getting the cert from keymanager ");
				throw new ApiAccessibleException(certServiceErrorList.get(0).get(PartnerConstants.ERRORCODE).toString(),
						certServiceErrorList.get(0).get(PartnerConstants.ERRORMESSAGE).toString());
			} else {
				LOGGER.error("Error occurred while getting the cert {}", getApiResponse);
				throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
						ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage());
			}
		}
		if (responseObject == null) {
			LOGGER.error("Got null respone from {} ",
					environment.getProperty("pmp.partner.certificaticate.get.rest.uri"));
			throw new ApiAccessibleException(ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorCode(),
					ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorMessage());
		}
		return responseObject;

	}

	@Override
	public String addBiometricExtractors(String partnerId, String policyId, ExtractorsDto extractors) {
		if (isApprovedPolicyRequestExists(partnerId, policyId)) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.ADD_BIO_EXTRACTORS_FAILURE, partnerId, "partnerId");
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
			extractorProvider.setCrBy(getLoggedInUserId());
			extractorProvider.setCrDtimes(Timestamp.valueOf(LocalDateTime.now()));
			BiometricExtractorProvider extractorsFromDb = extractorProviderRepository
					.findByPartnerAndPolicyIdAndAttributeName(partnerId, policyId, extractor.getAttributeName());
			if (extractorsFromDb != null) {
				extractorProvider.setId(extractorsFromDb.getId());
				extractorProvider.setUpdBy(getLoggedInUserId());
				extractorProvider.setUpdDtimes(Timestamp.valueOf(LocalDateTime.now()));
				extractorProvider.setCrBy(extractorsFromDb.getCrBy());
				extractorProvider.setCrDtimes(extractorsFromDb.getCrDtimes());
			}
			extractorProvider.setIsDeleted(false);
			extractorProviderRepository.save(extractorProvider);
		}
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.ADD_BIO_EXTRACTORS_SUCCESS, partnerId, "partnerId");
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
			LOGGER.error("Bio extractors are not mapped to partner {} with policy {} ", partnerId, policyId);
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
		return response;
	}

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
		Page<Partner> page = partnerSearchHelper.search(Partner.class, dto, "id");
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
		Page<PartnerType> page = partnerSearchHelper.search(PartnerType.class, dto, null);
		if (page.getContent() != null && !page.getContent().isEmpty()) {
			partnerTypes = MapperUtils.mapAll(page.getContent(), PartnerType.class);
			pageDto = pageUtils.sortPage(partnerTypes, dto.getSort(), dto.getPagination(), page.getTotalElements());
		}
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.SEARCH_PARTNER_TYPE_SUCCESS);
		return pageDto;
	}

	@Override
	public String mapPartnerPolicyCredentialType(String credentialType, String partnerId, String policyName) {
		validateCredentialTypes(credentialType);
		Partner partner = getValidPartner(partnerId, false);
		if (!Arrays.stream(credentialTypesRequiredPartnerTypes.split(","))
				.anyMatch(partner.getPartnerTypeCode()::equalsIgnoreCase)) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.MAP_POLICY_CREDENTIAL_TYPE_FAILURE, partnerId, "partnerId");
			throw new PartnerServiceException(ErrorCode.CREDENTIAL_NOT_ALLOWED_PARTNERS.getErrorCode(),
					ErrorCode.CREDENTIAL_NOT_ALLOWED_PARTNERS.getErrorMessage() + credentialTypesRequiredPartnerTypes);
		}
		AuthPolicy validPolicy = validatePolicyGroupAndPolicy(partner.getPolicyGroupId(), policyName);
		PartnerPolicyCredentialType entity = new PartnerPolicyCredentialType();
		PartnerPolicyCredentialTypePK key = new PartnerPolicyCredentialTypePK();
		key.setCredentialType(credentialType);
		key.setPartId(partnerId);
		key.setPolicyId(validPolicy.getId());
		entity.setId(key);
		entity.setCrBy(getLoggedInUserId());
		entity.setCrDtimes(Timestamp.valueOf(LocalDateTime.now()));
		entity.setIsActive(true);
		entity.setIsDeleted(false);
		partnerCredentialTypePolicyRepo.save(entity);
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.MAP_POLICY_CREDENTIAL_TYPE_SUCCESS);
		return "Partner, policy and credentialType mapping done successfully.";
	}

	private void validateCredentialTypes(String credentialType) {
		if (!Arrays.stream(allowedCredentialTypes.split(",")).anyMatch(credentialType::equalsIgnoreCase)) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.MAP_POLICY_CREDENTIAL_TYPE_FAILURE, credentialType, "credentialType");
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
			LOGGER.error("Policy is not mapped against to credential type {} and partner {} ", credentialType,
					partnerId);
			throw new PartnerServiceException(ErrorCode.NO_DETAILS_FOUND.getErrorCode(),
					ErrorCode.NO_DETAILS_FOUND.getErrorMessage());
		}
		Optional<AuthPolicy> authPolicy = authPolicyRepository
				.findById(partnerCredentialTypePolicy.getId().getPolicyId());
		if (authPolicy.isEmpty()) {
			LOGGER.error("Policy not exists with id {} ", partnerCredentialTypePolicy.getId().getPolicyId());
			throw new PartnerServiceException(ErrorCode.POLICY_NOT_EXIST.getErrorCode(),
					ErrorCode.POLICY_NOT_EXIST.getErrorMessage());
		}		
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
		if(partnerSearchHelper.isLoggedInUserFilterRequired()) {
			SearchFilter loggedInUserFilterDto = new SearchFilter();
			loggedInUserFilterDto.setColumnName("id");
			loggedInUserFilterDto.setValue(getLoggedInUserId());
			loggedInUserFilterDto.setType("equals");
			filterValueDto.getOptionalFilters().add(loggedInUserFilterDto);
		}
		if (filterColumnValidator.validate(FilterDto.class, filterValueDto.getFilters(), Partner.class)) {
			for (FilterDto filterDto : filterValueDto.getFilters()) {
				List<FilterData> filterValues = filterHelper.filterValuesWithCode(Partner.class,
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
				List<FilterData> filterValues = filterHelper.filterValuesWithCode(PartnerPolicyRequest.class, filterDto, filterValueDto, "id");
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
		Optional<SearchFilter> partnerNameSearchFilter = Optional.empty();
		Optional<SearchFilter> partnerIdSearchFilter = Optional.empty();
		partnerNameSearchFilter = dto.getFilters().stream()
				.filter(cn -> cn.getColumnName().equalsIgnoreCase("partnerName")).findFirst();;
		dto.getFilters().removeIf(f->f.getColumnName().equalsIgnoreCase("partnerName"));
		
		partnerIdSearchFilter = dto.getFilters().stream()
				.filter(cn -> cn.getColumnName().equalsIgnoreCase("partnerId")).findFirst();
		if(partnerIdSearchFilter.isPresent()) {
			Optional<Partner> loggedInPartner = partnerRepository.findById(partnerIdSearchFilter.get().getValue());
			if (loggedInPartner.isPresent()) {
				partnerIdSearchFilter.get().setValue(loggedInPartner.get().getId());
			}
			dto.getFilters().removeIf(f -> f.getColumnName().equalsIgnoreCase("partnerId"));
		}
		
		Optional<SearchFilter> policyNameFilter = dto.getFilters().stream()
				.filter(cn -> cn.getColumnName().equalsIgnoreCase("policyName")).findFirst();;
		if (policyNameFilter.isPresent()) {
			AuthPolicy authPolicyFromDb = authPolicyRepository.findByName(policyNameFilter.get().getValue());
			SearchFilter policyIdSearchFilter = new SearchFilter();
			policyIdSearchFilter.setColumnName("policyId");
			policyIdSearchFilter.setValue(authPolicyFromDb.getId());
			policyIdSearchFilter.setType("equals");
			dto.getFilters().add(policyIdSearchFilter);
			dto.getFilters().removeIf(f -> f.getColumnName().equalsIgnoreCase("policyName"));
		}

		if(partnerSearchHelper.isLoggedInUserFilterRequired()) {
			Optional<Partner> loggedInPartner = partnerRepository.findById(getLoggedInUserId());
			if(loggedInPartner.isPresent()) {	
				SearchFilter loggedInUserSearchFilter = new SearchFilter();
				loggedInUserSearchFilter.setValue(loggedInPartner.get().getId());
				partnerIdSearchFilter = Optional.of(loggedInUserSearchFilter);
			}
		}
		Page<PartnerPolicy> page = partnerSearchHelper.search(PartnerPolicy.class, dto, null);
		if (page.getContent() != null && !page.getContent().isEmpty()) {
			if(partnerNameSearchFilter.isPresent() && partnerIdSearchFilter.isPresent()) {
				String nameValue = partnerNameSearchFilter.get().getValue();
				String idValue = partnerIdSearchFilter.get().getValue();
				partnerMappedPolicies = mapPartnerPolicies(page.getContent().stream().filter(
						f -> f.getPartner().getName().equals(nameValue) && f.getPartner().getId().equals(idValue))
						.collect(Collectors.toList()));
			}
			else if (partnerNameSearchFilter.isPresent()) {
				String value = partnerNameSearchFilter.get().getValue();
				partnerMappedPolicies = mapPartnerPolicies(page.getContent().stream()
						.filter(f -> f.getPartner().getName().equals(value)).collect(Collectors.toList()));
			} else if(partnerIdSearchFilter.isPresent()){
				String value = partnerIdSearchFilter.get().getValue();
				partnerMappedPolicies = mapPartnerPolicies(page.getContent().stream()
						.filter(f -> f.getPartner().getId().equals(value)).collect(Collectors.toList()));				
			}
			else{
				partnerMappedPolicies = mapPartnerPolicies(page.getContent());
			}
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
			searchResponse.setLabel(partnerPolicy.getLabel());
			searchResponse.setPartnerId(partnerPolicy.getPartner().getId());
			searchResponse.setPartnerName(partnerPolicy.getPartner().getName());
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
		Optional<SearchFilter> partnerNameSearchFilter = Optional.empty();
		Optional<SearchFilter> partnerIdSearchFilter = Optional.empty();
		partnerNameSearchFilter = dto.getFilters().stream()
				.filter(cn -> cn.getColumnName().equalsIgnoreCase("partnerName")).findFirst();
		dto.getFilters().removeIf(f->f.getColumnName().equalsIgnoreCase("partnerName"));
		
		Optional<SearchFilter> apikeyRequestIdFilter = dto.getFilters().stream()
				.filter(cn -> cn.getColumnName().equalsIgnoreCase("apikeyRequestId")).findFirst();
		if (apikeyRequestIdFilter.isPresent()) {
			apikeyRequestIdFilter.get().setColumnName("id");
			dto.getFilters().add(apikeyRequestIdFilter.get());
			dto.getFilters().removeIf(f -> f.getColumnName().equalsIgnoreCase("apikeyRequestId"));
		}
		
		Optional<SearchFilter> policyNameFilter = dto.getFilters().stream()
				.filter(cn -> cn.getColumnName().equalsIgnoreCase("policyName")).findFirst();
		if (policyNameFilter.isPresent()) {
			List<AuthPolicy> authPoliciesFromDb = authPolicyRepository
					.findByNameIgnoreCase(policyNameFilter.get().getValue());
			if (!authPoliciesFromDb.isEmpty()) {
				SearchFilter policyIdSearchFilter = new SearchFilter();
				policyIdSearchFilter.setColumnName("policyId");
				policyIdSearchFilter
						.setValues(authPoliciesFromDb.stream().map(AuthPolicy::getId).collect(Collectors.toList()));
				policyIdSearchFilter.setType("in");
				dto.getFilters().add(policyIdSearchFilter);
			} else {
				return new PageResponseDto<>();
			}
			dto.getFilters().removeIf(f -> f.getColumnName().equalsIgnoreCase("policyName"));
		}
			
		partnerIdSearchFilter = dto.getFilters().stream()
				.filter(cn -> cn.getColumnName().equalsIgnoreCase("partnerId")).findFirst();
		dto.getFilters().removeIf(f->f.getColumnName().equalsIgnoreCase("partnerId"));
		
		if(partnerSearchHelper.isLoggedInUserFilterRequired()) {
			Optional<Partner> loggedInPartner = partnerRepository.findById(getLoggedInUserId());
			if(loggedInPartner.isPresent()) {				
				SearchFilter loggedInUserSearchFilter = new SearchFilter();
				loggedInUserSearchFilter.setValue(loggedInPartner.get().getId());
				partnerIdSearchFilter = Optional.of(loggedInUserSearchFilter);
			}
		}
		Page<PartnerPolicyRequest> page = partnerSearchHelper.search(PartnerPolicyRequest.class, dto,
				null);
		if (page.getContent() != null && !page.getContent().isEmpty()) {
			if(partnerNameSearchFilter.isPresent() && partnerIdSearchFilter.isPresent()) {
				String nameValue = partnerNameSearchFilter.get().getValue();
				String idValue = partnerIdSearchFilter.get().getValue();
				partnerPolicyRequests = mapPolicyRequests(page.getContent().stream()
						.filter(f -> f.getPartner().getName().contains(nameValue) &&
								f.getPartner().getId().equals(idValue))
						.collect(Collectors.toList()));				
			}else if (partnerNameSearchFilter.isPresent()) {
				String value = partnerNameSearchFilter.get().getValue();
				partnerPolicyRequests = mapPolicyRequests(page.getContent().stream()
						.filter(f -> f.getPartner().getName().toLowerCase().contains(value.toLowerCase()))
						.collect(Collectors.toList()));
			}else if(partnerIdSearchFilter.isPresent()){
				String value = partnerIdSearchFilter.get().getValue();
				partnerPolicyRequests = mapPolicyRequests(page.getContent().stream()
						.filter(f -> f.getPartner().getId().equals(value))
						.collect(Collectors.toList()));				
			}
			else{
				partnerPolicyRequests = mapPolicyRequests(page.getContent());
			}
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
		List<PolicyRequestSearchResponseDto> policyRequestList = new ArrayList<>();
		if (content.size() > 0) {
			List<AuthPolicy> authPolices = authPolicyRepository.findAllByPolicyIds(
					content.stream().map(PartnerPolicyRequest::getPolicyId).collect(Collectors.toList()));
			content.forEach(policyRequest -> {
				PolicyRequestSearchResponseDto searchPolicyRequest = new PolicyRequestSearchResponseDto();
				searchPolicyRequest.setApikeyRequestId(policyRequest.getId());
				searchPolicyRequest.setPartnerId(policyRequest.getPartner().getId());
				searchPolicyRequest.setPartnerName(policyRequest.getPartner().getName());
				searchPolicyRequest.setPolicyId(policyRequest.getPolicyId());
				Optional<AuthPolicy> authPolicy = authPolices.stream()
						.filter(p -> p.getId().equals(policyRequest.getPolicyId())).findFirst();
				searchPolicyRequest.setPolicyName(authPolicy.isPresent() ? authPolicy.get().getName() : "");
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
		}
		return policyRequestList;
	}

	/**
	 * 
	 * @return
	 */
	private String getLoggedInUserId() {
		return UserDetailUtil.getLoggedInUserId();
	}
	
	private String getLoggedInUserEmail() {
		return UserDetailUtil.getLoggedInUserDetails() != null ? UserDetailUtil.getLoggedInUserDetails().getMail()
				: null;
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
		dto.setLangCode(partner.getLangCode());
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
		byte[] p7bBytes = CryptoUtil.decodeURLSafeBase64(certChain);
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

	/**
	 * This method updates the policy group for not approved partners.
	 */
	@Override
	public String updatePolicyGroup(String partnerId, String policyGroupName) {
		Partner partner = getValidPartner(partnerId, true);
		//Approved partners policy group should not be updated
		if (partner.getIsActive()) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.MAP_POLICY_GROUP_FAILURE, partnerId, "partnerId");
			throw new PartnerServiceException(ErrorCode.POLICY_GROUP_NOT_MAP_ACTIVE_PARTNER.getErrorCode(),
					ErrorCode.POLICY_GROUP_NOT_MAP_ACTIVE_PARTNER.getErrorMessage());
		}		
		PartnerType partnerType = validateAndGetPartnerType(partner.getPartnerTypeCode());		
		if (!partnerType.getIsPolicyRequired()) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.MAP_POLICY_GROUP_FAILURE, partnerId, "partnerId");
			throw new PartnerServiceException(ErrorCode.POLICY_GROUP_NOT_REQUIRED.getErrorCode(),
					ErrorCode.POLICY_GROUP_NOT_REQUIRED.getErrorMessage());
		}		
		PolicyGroup policyGroup = validateAndGetPolicyGroupByName(policyGroupName);
		partner.setPolicyGroupId(policyGroup.getId());
		partner.setUpdBy(getLoggedInUserId());
		partner.setUpdDtimes(Timestamp.valueOf(LocalDateTime.now()));
		partnerRepository.save(partner);
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.MAP_POLICY_GROUP_SUCCESS, partnerId, "partnerId");
		return "Success";
	}

	/**
	 * 
	 */
	@Override
	public EmailVerificationResponseDto isPartnerExistsWithEmail(String emailId) {
		EmailVerificationResponseDto response = new EmailVerificationResponseDto();
		if (!validateEmail(emailId)) {
			throw new PartnerServiceException(ErrorCode.INVALID_EMAIL_ID_EXCEPTION.getErrorCode(),
					ErrorCode.INVALID_EMAIL_ID_EXCEPTION.getErrorMessage());
		}
		response.setEmailExists(!validatePartnerByEmail(emailId));
		List<PartnerType> partnerTypesFromDb = getAllPartnerTypes();
		response.setPolicyRequiredPartnerTypes(partnerTypesFromDb.stream().filter(pt -> pt.getIsPolicyRequired())
				.map(p -> p.getCode().toUpperCase()).collect(Collectors.toList()));
		return response;
	}
	
	private List<PartnerType> getAllPartnerTypes(){
		return partnerTypeRepository.findAll();
	}

	@Override
	public PartnerPolicyMappingResponseDto requestForPolicyMapping(PartnerPolicyMappingRequest partnerAPIKeyRequest, String partnerId) {
		validateLoggedInUserAuthorization(partnerId);
		Partner partner = getValidPartner(partnerId, false);
		if(partner.getPolicyGroupId() == null) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.SUBMIT_API_REQUEST_FAILURE, partnerId, "partnerId");
			throw new PartnerServiceException(ErrorCode.PARTNER_NOT_MAPPED_TO_POLICY_GROUP.getErrorCode(),
					ErrorCode.PARTNER_NOT_MAPPED_TO_POLICY_GROUP.getErrorMessage());
		}
		AuthPolicy authPolicy = validatePolicyGroupAndPolicy(partner.getPolicyGroupId(),
				partnerAPIKeyRequest.getPolicyName());
		
		List<PartnerPolicyRequest> mappingRequests = partnerPolicyRequestRepository
				.findByPartnerIdAndPolicyId(partnerId, authPolicy.getId());
		
		if(mappingRequests.stream().anyMatch(r->r.getStatusCode().equalsIgnoreCase(PartnerConstants.IN_PROGRESS))) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.SUBMIT_API_REQUEST_FAILURE, partnerId, "partnerId");
			throw new PartnerServiceException(ErrorCode.PARTNER_POLICY_MAPPING_EXISTS.getErrorCode(), String
					.format(ErrorCode.PARTNER_POLICY_MAPPING_EXISTS.getErrorMessage(), PartnerConstants.IN_PROGRESS));
		}
		if(mappingRequests.stream().anyMatch(r->r.getStatusCode().equalsIgnoreCase(PartnerConstants.APPROVED))) {
			auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.SUBMIT_API_REQUEST_FAILURE, partnerId, "partnerId");
			throw new PartnerServiceException(ErrorCode.PARTNER_POLICY_MAPPING_EXISTS.getErrorCode(), String
					.format(ErrorCode.PARTNER_POLICY_MAPPING_EXISTS.getErrorMessage(), PartnerConstants.APPROVED));
		}
		PartnerPolicyMappingResponseDto response = new PartnerPolicyMappingResponseDto();
		PartnerPolicyRequest partnerPolicyRequest = new PartnerPolicyRequest();
		partnerPolicyRequest.setStatusCode(PartnerConstants.IN_PROGRESS);
		partnerPolicyRequest.setCrBy(getLoggedInUserId());
		partnerPolicyRequest.setCrDtimes(Timestamp.valueOf(LocalDateTime.now()));
		partnerPolicyRequest.setId(PartnerUtil.createPartnerPolicyRequestId());
		partnerPolicyRequest.setPartner(partner);
		partnerPolicyRequest.setPolicyId(authPolicy.getId());
		partnerPolicyRequest.setRequestDatetimes(Timestamp.valueOf(LocalDateTime.now()));
		partnerPolicyRequest.setRequestDetail(partnerAPIKeyRequest.getUseCaseDescription());
		partnerPolicyRequest.setIsDeleted(false);
		partnerPolicyRequestRepository.save(partnerPolicyRequest);
		auditUtil.setAuditRequestDto(PartnerServiceAuditEnum.SUBMIT_API_REQUEST_SUCCESS, partnerId, "partnerId");
		response.setMappingkey(partnerPolicyRequest.getId());
		response.setMessage("Policy mapping request submitted successfully.");
		return response;
	}
	
	/**
	 * validates the loggedInUser authorization
	 * @param loggedInUserId
	 */
	public void validateLoggedInUserAuthorization(String loggedInUserId) {
		if(partnerSearchHelper.isLoggedInUserFilterRequired() && !loggedInUserId.equals(getLoggedInUserId())) {
			throw new PartnerServiceException(ErrorCode.LOGGEDIN_USER_NOT_AUTHORIZED.getErrorCode(),
					ErrorCode.LOGGEDIN_USER_NOT_AUTHORIZED.getErrorMessage());
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public List<String> getSystemSupportedLanguageCodes() {		
		return List.of((mandatoryLanguges + "," + optionalLanguges).split(","));
	}
	
	/**
	 * 
	 * @param inputString
	 * @return
	 */
	private boolean isInputStringContainsSpaces(String inputString) {
		if (inputString.matches(emptySpacesRegex)) {
			return true;
		}
		return false;
	}

	@Override
	public PartnerResponse savePartner(PartnerRequest request) {
		PartnerRequestDto input = new PartnerRequestDto();
		input.setAddress(request.getAddress());
		input.setContactNumber(request.getContactNumber());
		input.setEmailId(request.getEmailId());
		input.setLangCode(request.getLangCode());
		input.setOrganizationName(request.getOrganizationName());
		input.setPartnerId(request.getPartnerId());
		input.setPartnerType(request.getPartnerType());
		input.setPolicyGroup(request.getPolicyGroup());
		return registerPartner(input);
	}
	
	/**
	 *  Validates string is valid json or not
	 * @param jsonInString
	 */
	private void isJSONValid(String jsonInString) {
		try {
			mapper.readTree(jsonInString);
		} catch (IOException e) {
			LOGGER.error("Given additionalinfo is not a valid json object ", e);
			throw new PartnerServiceException(ErrorCode.JSON_NOT_VALID.getErrorCode(),
					ErrorCode.JSON_NOT_VALID.getErrorMessage());
		}
	}

	@Override
	public PartnerResponse updatePartnerDetail(PartnerUpdateRequest partnerUpdateRequest, String partnerId) {
		PartnerUpdateDto updateRequest = new PartnerUpdateDto();
		updateRequest.setAddress(partnerUpdateRequest.getAddress());
		updateRequest.setContactNumber(partnerUpdateRequest.getContactNumber());
		updateRequest.setAdditionalInfo(null);
		updateRequest.setLogoUrl(null);	
		return updatePartnerDetails(updateRequest, partnerId);
	}
}
