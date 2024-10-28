package io.mosip.pms.partner.manager.service.impl;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;
import javax.transaction.Transactional;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.pms.common.dto.*;
import io.mosip.pms.common.entity.*;
import io.mosip.pms.common.repository.*;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.manager.dto.*;
import io.mosip.pms.partner.manager.dto.FilterDto;
import io.mosip.pms.partner.request.dto.PartnerCertDownloadRequestDto;
import io.mosip.pms.partner.response.dto.OriginalCertDownloadResponseDto;
import io.mosip.pms.partner.util.MultiPartnerUtil;
import io.mosip.pms.partner.util.PartnerHelper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.pms.common.constant.ApiAccessibleExceptionConstant;
import io.mosip.pms.common.constant.ConfigKeyConstants;
import io.mosip.pms.common.constant.EventType;
import io.mosip.pms.common.exception.ApiAccessibleException;
import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.response.dto.NotificationDto;
import io.mosip.pms.common.service.NotificatonService;
import io.mosip.pms.common.util.MapperUtils;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.common.util.UserDetailUtil;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.partner.constant.PartnerConstants;
import io.mosip.pms.partner.manager.constant.ErrorCode;
import io.mosip.pms.partner.manager.constant.PartnerManageEnum;
import io.mosip.pms.partner.manager.exception.PartnerManagerServiceException;
import io.mosip.pms.partner.manager.service.PartnerManagerService;
import io.mosip.pms.partner.request.dto.APIKeyGenerateRequestDto;
import io.mosip.pms.partner.request.dto.APIkeyStatusUpdateRequestDto;
import io.mosip.pms.partner.response.dto.APIKeyGenerateResponseDto;
import io.mosip.pms.partner.response.dto.PartnerCertDownloadResponeDto;
import io.mosip.pms.partner.util.PartnerUtil;

@Service
@Transactional
public class PartnerManagementServiceImpl implements PartnerManagerService {

	private static final Logger LOGGER = PMSLogger.getLogger(PartnerManagementServiceImpl.class);
	public static final String DEVICE_PROVIDER = "Device_Provider";
	public static final String FTM_PROVIDER = "FTM_Provider";

	@Value("${mosip.pms.api.id.all.partners.get}")
	private String getAllPartnersId;

	@Autowired
	PartnerSummaryRepository partnerSummaryRepository;

	@Autowired
	PartnerPolicyRepository partnerPolicyRepository;

	@Autowired
	PartnerRepository partnerRepository;

	@Autowired
	PartnerPolicyRequestRepository partnerPolicyRequestRepository;

	@Autowired
	PolicyGroupRepository policyGroupRepository;

	@Autowired
	BiometricExtractorProviderRepository extractorProviderRepository;

	@Autowired
	AuthPolicyRepository authPolicyRepository;

	@Autowired
	MispServiceRepository mispRepository;
	
	@Autowired
	MispLicenseRepository mispLicenseRepository;

	@Autowired
	private WebSubPublisher webSubPublisher;
	
	@Autowired
	private NotificatonService notificationService;

	@Autowired
	RestUtil restUtil;

	@Autowired
	AuditUtil auditUtil;

	@Autowired
	private Environment environment;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	PartnerHelper partnerHelper;

	@Autowired
	PartnerServiceRepository partnerServiceRepository;

	@Value("${pmp.bioextractors.required.partner.types}")
	private String biometricExtractorsRequiredPartnerTypes;

	@Value("${mosip.pmp.partner.policy.expiry.period.indays}")
	private int partnerPolicyExpiryInDays;

	@Value("${mosip.pms.api.id.partner.details.get}")
	private String getPartnerDetailsId;

	public static final String VERSION = "1.0";

	@Override
	public PartnersPolicyMappingResponse updatePolicyAgainstApikey(PartnersPolicyMappingRequest request,
			String partnerId, String partnerApikey) {
		PartnerPolicy partnerPolicyFromDb = partnerPolicyRepository.findByPartnerIdAndPolicyIdAndApikey(partnerId,
				request.getOldPolicyID(), partnerApikey);
		if (partnerPolicyFromDb == null) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.API_KEY_MAPPING_FAILURE, partnerId, "partnerId");
			throw new PartnerManagerServiceException(ErrorCode.PARTNER_APIKEY_POLICY_MAPPING_NOTEXISTS.getErrorCode(),
					ErrorCode.PARTNER_APIKEY_POLICY_MAPPING_NOTEXISTS.getErrorMessage());

		}
		if (!partnerPolicyFromDb.getIsActive()) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.API_KEY_MAPPING_FAILURE, partnerId, "partnerId");
			throw new PartnerManagerServiceException(ErrorCode.PARTNER_APIKEY_NOT_ACTIVE_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_APIKEY_NOT_ACTIVE_EXCEPTION.getErrorMessage());
		}
		AuthPolicy requestedPolicy = validateAndGetPolicyOfPolicyGroup(
				partnerPolicyFromDb.getPartner().getPolicyGroupId(), request.getNewPolicyID());
		PartnerPolicy updateObject = partnerPolicyFromDb;
		updateObject.setUpdBy(getUser());
		updateObject.setUpdDtimes(Timestamp.valueOf(LocalDateTime.now()));
		updateObject.setPolicyId(requestedPolicy.getId());
		partnerPolicyRepository.save(updateObject);
		PartnersPolicyMappingResponse partnersPolicyMappingResponse = new PartnersPolicyMappingResponse();
		partnersPolicyMappingResponse.setMessage("Given apikey updated with policy successfully. ");
		notify(null, null, MapperUtils.mapKeyDataToPublishDto(updateObject), EventType.APIKEY_UPDATED);
		auditUtil.setAuditRequestDto(PartnerManageEnum.API_KEY_MAPPING_SUCCESS, partnerId, "partnerId");
		return partnersPolicyMappingResponse;
	}

	private AuthPolicy validateAndGetPolicyOfPolicyGroup(String policyGroupId, String policyId) {
		AuthPolicy authPolicy = authPolicyRepository.findByPolicyGroupAndId(policyGroupId, policyId);
		if (authPolicy == null) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.API_KEY_MAPPING_FAILURE, policyId, "policyId");
			throw new PartnerManagerServiceException(ErrorCode.POLICY_NOT_EXIST_EXCEPTION.getErrorCode(),
					ErrorCode.POLICY_NOT_EXIST_EXCEPTION.getErrorMessage() + policyId);

		}
		if (!authPolicy.getIsActive()) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.API_KEY_MAPPING_FAILURE, policyId, "policyId");
			throw new PartnerManagerServiceException(ErrorCode.POLICY_NOT_ACTIVE_EXCEPTION.getErrorCode(),
					ErrorCode.POLICY_NOT_ACTIVE_EXCEPTION.getErrorMessage() + policyId);

		}
		if (!authPolicy.getPolicyGroup().getIsActive()) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.API_KEY_MAPPING_FAILURE, policyId, "policyId");
			throw new PartnerManagerServiceException(ErrorCode.POLICY_GROUP_NOT_ACTIVE.getErrorCode(),
					ErrorCode.POLICY_GROUP_NOT_ACTIVE.getErrorMessage() + policyId);
		}
		return authPolicy;
	}

	@Override
	public PartnersPolicyMappingResponse activateDeactivateAuthEKYCPartner(String partnerId,
			StatusRequestDto request) {
		Optional<Partner> partnerFromDb = partnerRepository.findById(partnerId);
		if (partnerFromDb.isEmpty()) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.ACTIVATE_DEACTIVATE_KYC_PARTNERS_FAILURE, partnerId, "partnerId");			
			throw new PartnerManagerServiceException(ErrorCode.PARTNER_ID_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_ID_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		Partner updatePartnerObject = partnerFromDb.get();
		updatePartnerObject.setUpdBy(getUser());
		updatePartnerObject.setUpdDtimes(Timestamp.valueOf(LocalDateTime.now()));
		PartnersPolicyMappingResponse response = new PartnersPolicyMappingResponse();		
		if (request.getStatus().equalsIgnoreCase(PartnerConstants.ACTIVE)) {
			if(updatePartnerObject.getCertificateAlias() == null) {
				throw new PartnerManagerServiceException(ErrorCode.CERTIFICATE_NOT_UPLOADED_EXCEPTION.getErrorCode(),
						ErrorCode.CERTIFICATE_NOT_UPLOADED_EXCEPTION.getErrorMessage());				
			}
			//if partner is not active, then only make partner active
			if(!updatePartnerObject.getIsActive()) {
				updatePartnerObject.setIsActive(true);			
				partnerRepository.save(updatePartnerObject);
				notify(MapperUtils.mapDataToPublishDto(updatePartnerObject,getPartnerCertificate(updatePartnerObject.getCertificateAlias())), null, null, EventType.PARTNER_UPDATED);
				sendNotifications(EventType.PARTNER_UPDATED, updatePartnerObject);
			}
			response.setMessage("Partner activated successfully");			
			auditUtil.setAuditRequestDto(PartnerManageEnum.ACTIVATE_DEACTIVATE_KYC_PARTNERS_SUCCESS, partnerId, "partnerId");
			return response;
		}
		if (request.getStatus().equalsIgnoreCase(PartnerConstants.DEACTIVE)) {
			// if partner is active, then only make partner in-active
			if(updatePartnerObject.getIsActive()) {
				updatePartnerObject.setIsActive(false);
				partnerRepository.save(updatePartnerObject);
				// if partner is misp, then de-activate all licenses.
				if(updatePartnerObject.getPartnerTypeCode().equalsIgnoreCase(environment.getProperty(ConfigKeyConstants.MISP_PARTNER_TYPE, "MISP_Partner"))) {
					List<MISPLicenseEntity> activeLicenses = mispLicenseRepository.findByMispIdAndIsActive(updatePartnerObject.getId());
					// assuming one misp partner will have only one active license key at any giving point of time
					for(MISPLicenseEntity license : activeLicenses) {
						license.setIsActive(false);
						license.setUpdatedBy(getUser());
						license.setUpdatedDateTime(LocalDateTime.now());
						mispLicenseRepository.save(license);
						Map<String, Object> data = new HashMap<>();
						data.put("mispLicenseData", MapperUtils.mapDataToPublishDto(license));
						notify(data, EventType.MISP_LICENSE_UPDATED);
					}
				}
				notify(MapperUtils.mapDataToPublishDto(updatePartnerObject,getPartnerCertificate(updatePartnerObject.getCertificateAlias())), null, null, EventType.PARTNER_UPDATED);
				sendNotifications(EventType.PARTNER_UPDATED, updatePartnerObject);
			}
			response.setMessage("Partner de-activated successfully");
			auditUtil.setAuditRequestDto(PartnerManageEnum.ACTIVATE_DEACTIVATE_KYC_PARTNERS_SUCCESS, partnerId, "partnerId");
			return response;
		}
		auditUtil.setAuditRequestDto(PartnerManageEnum.ACTIVATE_DEACTIVATE_KYC_PARTNERS_FAILURE, partnerId, "partnerId");
		LOGGER.info(request.getStatus() + " : is Invalid Input Parameter, it should be (Active/De-Active)");
		throw new PartnerManagerServiceException(ErrorCode.INVALID_STATUS_CODE_ACTIVE_DEACTIVE.getErrorCode(),
				ErrorCode.INVALID_STATUS_CODE_ACTIVE_DEACTIVE.getErrorMessage());
	}
	

	@Override
	public RetrievePartnerDetailsResponse getAllAuthEKYCPartnersForThePolicyGroup(Optional<String> partnerType) {
		RetrievePartnerDetailsResponse partnersResponse = new RetrievePartnerDetailsResponse();
		List<RetrievePartnersDetails> partners = new ArrayList<RetrievePartnersDetails>();		
		Iterator<Partner> partnerIterat = getPartnersByPartnerType(partnerType).iterator();
		Partner partner = null;
		while (partnerIterat.hasNext()) {
			RetrievePartnersDetails retrievePartnersDetails = new RetrievePartnersDetails();
			partner = partnerIterat.next();
			retrievePartnersDetails.setPartnerID(partner.getId());
			retrievePartnersDetails
			.setStatus(partner.getIsActive() == true ? PartnerConstants.ACTIVE : PartnerConstants.DEACTIVE);
			retrievePartnersDetails.setOrganizationName(partner.getName());
			retrievePartnersDetails.setContactNumber(partner.getContactNo());
			retrievePartnersDetails.setEmailId(partner.getEmailId());
			retrievePartnersDetails.setAddress(partner.getAddress());
			retrievePartnersDetails.setPartnerType(partner.getPartnerTypeCode());
			partners.add(retrievePartnersDetails);
		}
		partnersResponse.setPartners(partners);
		return partnersResponse;
	}

	/**
	 * 
	 * @param partnerType
	 * @return
	 */
	@Override
	public PartnerDetailsResponse getPartners(Optional<String> partnerType) {
		PartnerDetailsResponse partnersResponse = new PartnerDetailsResponse();
		List<PartnerDetailsDto> partners = new ArrayList<PartnerDetailsDto>();		
		Iterator<Partner> partnerIterat = getPartnersByPartnerType(partnerType).iterator();
		Partner partner = null;
		while (partnerIterat.hasNext()) {
			PartnerDetailsDto retrievePartnersDetails = new PartnerDetailsDto();
			partner = partnerIterat.next();
			retrievePartnersDetails.setPartnerID(partner.getId());
			retrievePartnersDetails
			.setStatus(partner.getIsActive() == true ? PartnerConstants.ACTIVE : PartnerConstants.DEACTIVE);
			retrievePartnersDetails.setOrganizationName(partner.getName());
			retrievePartnersDetails.setContactNumber(partner.getContactNo());
			retrievePartnersDetails.setEmailId(partner.getEmailId());
			retrievePartnersDetails.setAddress(partner.getAddress());
			retrievePartnersDetails.setPartnerType(partner.getPartnerTypeCode());
			retrievePartnersDetails.setLogoUrl(partner.getLogoUrl());
			retrievePartnersDetails.setAdditionalInfo(
					partner.getAdditionalInfo() == null ? null : getValidJson(partner.getAdditionalInfo()));
			partners.add(retrievePartnersDetails);
		}
		partnersResponse.setPartners(partners);
		return partnersResponse;

	}
	
	private JsonNode getValidJson(String jsonInString) {
		try {
			return mapper.readTree(jsonInString);
		} catch (IOException e) {
			LOGGER.error("Given addtional info is not a valid json object ", e);			
			throw new PartnerManagerServiceException(ErrorCode.JSON_NOT_VALID.getErrorCode(),
					ErrorCode.JSON_NOT_VALID.getErrorMessage());
		}
	}
	/**
	 * 
	 * @param partnerType
	 * @return
	 */
	private List<Partner> getPartnersByPartnerType(Optional<String> partnerType) {
		List<Partner> partnersFromDb = null;
		if (partnerType.isPresent() && !partnerType.get().trim().isEmpty()) {
			partnersFromDb = partnerRepository.findByPartnerType(partnerType.get());
		} else {
			partnersFromDb = partnerRepository.findAll();
		}		
		if (partnersFromDb.isEmpty()) {
			LOGGER.error("Partners not exists in database");
			throw new PartnerManagerServiceException(ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		return partnersFromDb;
	}

	@Override
	public PartnerAPIKeyToPolicyMappingsResponse getPartnerAPIKeyToPolicyMapping(String partnerId,
			String partnerAPIKey) {
		PartnerPolicy partnerPolicyFromDb = partnerPolicyRepository.findByPartnerIdAndApikey(partnerId, partnerAPIKey);
		if (partnerPolicyFromDb == null) {
			LOGGER.error("Given apikey {} not mapped to partner {}", partnerAPIKey, partnerId);
			throw new PartnerManagerServiceException(ErrorCode.PARTNER_API_KEY_NOT_MAPPED.getErrorCode(),
					ErrorCode.PARTNER_API_KEY_NOT_MAPPED.getErrorMessage());
		}
		PartnerAPIKeyToPolicyMappingsResponse response = new PartnerAPIKeyToPolicyMappingsResponse();
		response.setPartnerID(partnerId);
		response.setPolicyId(partnerPolicyFromDb.getPolicyId());
		return response;
	}

	@Override
	public List<ApikeyRequests> getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagers() {
		List<PartnerPolicyRequest> apikeyRequestsFromDb = partnerPolicyRequestRepository.findAll();
		if (apikeyRequestsFromDb.isEmpty()) {
			LOGGER.error("No apikey requests exists");
			throw new PartnerManagerServiceException(ErrorCode.NO_PARTNER_API_KEY_REQUEST_EXCEPTION.getErrorCode(),
					ErrorCode.NO_PARTNER_API_KEY_REQUEST_EXCEPTION.getErrorMessage());
		}
		List<ApikeyRequests> response = new ArrayList<ApikeyRequests>();
		for (PartnerPolicyRequest partnerPolicyRequest : apikeyRequestsFromDb) {
			ApikeyRequests apikeyRequests = new ApikeyRequests();
			apikeyRequests.setApiKeyReqNo(partnerPolicyRequest.getId());
			apikeyRequests.setOrganizationName(partnerPolicyRequest.getPartner().getName());
			apikeyRequests.setPartnerID(partnerPolicyRequest.getPartner().getId());
			apikeyRequests.setPolicyDesc(partnerPolicyRequest.getRequestDetail());
			apikeyRequests.setPolicyId(partnerPolicyRequest.getPolicyId());
			apikeyRequests.setStatus(partnerPolicyRequest.getStatusCode());
			response.add(apikeyRequests);
		}		
		return response;
	}

	@Override
	public ApikeyRequests getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestId(String apiKeyReqId) {
		Optional<PartnerPolicyRequest> apikeyRequestsFromDb = partnerPolicyRequestRepository.findById(apiKeyReqId);
		if (apikeyRequestsFromDb.isEmpty()) {
			LOGGER.error("APIKey request is not exists with id {}", apiKeyReqId);
			throw new PartnerManagerServiceException(ErrorCode.NO_PARTNER_API_KEY_REQUEST_EXCEPTION.getErrorCode(),
					ErrorCode.NO_PARTNER_API_KEY_REQUEST_EXCEPTION.getErrorMessage());
		}
		ApikeyRequests apikeyRequest = new ApikeyRequests();
		apikeyRequest.setApiKeyReqNo(apikeyRequestsFromDb.get().getId());
		apikeyRequest.setOrganizationName(apikeyRequestsFromDb.get().getPartner().getName());
		apikeyRequest.setPartnerID(apikeyRequestsFromDb.get().getPartner().getId());
		apikeyRequest.setPolicyDesc(apikeyRequestsFromDb.get().getRequestDetail());
		apikeyRequest.setPolicyId(apikeyRequestsFromDb.get().getPolicyId());
		apikeyRequest.setStatus(apikeyRequestsFromDb.get().getStatusCode());
		return apikeyRequest;
	}

	private PartnerPolicyRequest getValidApikeyRequestForStatusUpdate(String requestedApikey) {
		Optional<PartnerPolicyRequest> partnerPolicyRequestFromDb = partnerPolicyRequestRepository
				.findById(requestedApikey);
		if (partnerPolicyRequestFromDb.isEmpty()) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.APPROVE_REJECT_PARTNER_API_FAILURE);
			throw new PartnerManagerServiceException(ErrorCode.PARTNER_API_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_API_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		if (partnerPolicyRequestFromDb.get().getStatusCode().equalsIgnoreCase(PartnerConstants.APPROVED)) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.APPROVE_REJECT_PARTNER_API_FAILURE);
			throw new PartnerManagerServiceException(ErrorCode.POLICY_REQUEST_ALREADY_APPROVED.getErrorCode(),
					ErrorCode.POLICY_REQUEST_ALREADY_APPROVED.getErrorMessage());
		}
		if (partnerPolicyRequestFromDb.get().getStatusCode().equalsIgnoreCase(PartnerConstants.REJECTED)) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.APPROVE_REJECT_PARTNER_API_FAILURE);
			throw new PartnerManagerServiceException(ErrorCode.POLICY_REQUEST_ALREADY_REJECTED.getErrorCode(),
					ErrorCode.POLICY_REQUEST_ALREADY_REJECTED.getErrorMessage());
		}
		if (Arrays.stream(biometricExtractorsRequiredPartnerTypes.split(","))
				.anyMatch(partnerPolicyRequestFromDb.get().getPartner().getPartnerTypeCode()::equalsIgnoreCase)) {
			List<BiometricExtractorProvider> extractorsFromDb = extractorProviderRepository.findByPartnerAndPolicyId(
					partnerPolicyRequestFromDb.get().getPartner().getId(),
					partnerPolicyRequestFromDb.get().getPolicyId());
			if (extractorsFromDb.isEmpty()) {
				auditUtil.setAuditRequestDto(PartnerManageEnum.APPROVE_REJECT_PARTNER_API_FAILURE);
				throw new PartnerManagerServiceException(ErrorCode.EXTRACTORS_NOT_PRESENT.getErrorCode(),
						ErrorCode.EXTRACTORS_NOT_PRESENT.getErrorMessage());
			}
		}		

		return partnerPolicyRequestFromDb.get();
	}

	private JSONObject getPolicyObject(String policy) {
		JSONParser parser = new JSONParser();
		String error = null;
		try {
			return ((JSONObject) parser.parse(policy));
		} catch (ParseException e) {
			error = e.getMessage();
		}
		auditUtil.setAuditRequestDto(PartnerManageEnum.GET_POLICY_FAILURE);
		throw new PartnerManagerServiceException(ErrorCode.POLICY_PARSING_ERROR.getErrorCode(),
				ErrorCode.POLICY_PARSING_ERROR.getErrorMessage() + error);
	}


	@SuppressWarnings("unchecked")
	private String getPartnerCertificate(String certificateAlias) {
		Map<String, String> pathsegments = new HashMap<>();
		pathsegments.put("partnerCertId", certificateAlias);
		Map<String, Object> getApiResponse = restUtil
				.getApi(environment.getProperty("pmp.partner.certificaticate.get.rest.uri"), pathsegments, Map.class);
		PartnerCertDownloadResponeDto responseObject = null;
		try {			
			responseObject = mapper.readValue(mapper.writeValueAsString(getApiResponse.get("response")),
					PartnerCertDownloadResponeDto.class);
		} catch (IOException e) {
			LOGGER.error("Error occured while parsing the response ", e);
			throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
					ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage());
		}
		if (responseObject == null && getApiResponse.containsKey(PartnerConstants.ERRORS)) {
			List<Map<String, Object>> certServiceErrorList = (List<Map<String, Object>>) getApiResponse
					.get(PartnerConstants.ERRORS);
			if (!certServiceErrorList.isEmpty()) {
				throw new ApiAccessibleException(certServiceErrorList.get(0).get(PartnerConstants.ERRORCODE).toString(),
						certServiceErrorList.get(0).get(PartnerConstants.ERRORMESSAGE).toString());
			} else {
				throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
						ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage());
			}
		}
		if (responseObject == null) {
			throw new ApiAccessibleException(ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorCode(),
					ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorMessage());
		}

		return responseObject.getCertificateData();
	}

	private AuthPolicy validatePolicy(String policyId) {
		Optional<AuthPolicy> authPolicy = authPolicyRepository.findById(policyId);
		if (authPolicy.isEmpty()) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_POLICY_MAPPING_FAILURE, policyId, "policyId");
			throw new PartnerManagerServiceException(ErrorCode.PARTNER_NOT_MAPPED_TO_POLICY_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_NOT_MAPPED_TO_POLICY_EXCEPTION.getErrorMessage());
		}
		if (!authPolicy.get().getIsActive()) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_POLICY_MAPPING_FAILURE, policyId, "policyId");
			throw new PartnerManagerServiceException(ErrorCode.PARTNER_POLICY_NOT_ACTIVE_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_POLICY_NOT_ACTIVE_EXCEPTION.getErrorMessage());
		}
		if (authPolicy.get().getPolicyGroup() == null) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_POLICY_MAPPING_FAILURE, policyId, "policyId");
			throw new PartnerManagerServiceException(ErrorCode.POLICY_GROUP_NOT_EXISTS.getErrorCode(),
					ErrorCode.POLICY_GROUP_NOT_EXISTS.getErrorMessage());
		}
		if (!authPolicy.get().getPolicyGroup().getIsActive()) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_POLICY_MAPPING_FAILURE, policyId, "policyId");
			throw new PartnerManagerServiceException(ErrorCode.POLICY_GROUP_NOT_ACTIVE.getErrorCode(),
					ErrorCode.POLICY_GROUP_NOT_ACTIVE.getErrorMessage());
		}
		if (authPolicy.get().getValidToDate().isBefore(LocalDateTime.now())) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_POLICY_MAPPING_FAILURE, policyId, "policyId");
			throw new PartnerManagerServiceException(ErrorCode.PARTNER_POLICY_EXPIRED_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_POLICY_EXPIRED_EXCEPTION.getErrorMessage());
		}
		return authPolicy.get();
	}

	public String getUser() {
		return UserDetailUtil.getLoggedInUser();
	}
	
	/**
	 * 
	 * @param partnerDataToPublish
	 * @param policyDataToPublish
	 * @param apiKeyDataToPublish
	 * @param eventType
	 */
	private void notify(PartnerDataPublishDto partnerDataToPublish, PolicyPublishDto policyDataToPublish,
			APIKeyDataPublishDto apiKeyDataToPublish, EventType eventType) {
		Map<String, Object> data = new HashMap<>();
		if (partnerDataToPublish != null) {
			data.put(PartnerConstants.PARTNER_DATA, partnerDataToPublish);
		}
		if (policyDataToPublish != null) {
			data.put(PartnerConstants.POLICY_DATA, policyDataToPublish);
		}
		if(apiKeyDataToPublish != null) {
			data.put(PartnerConstants.APIKEY_DATA, apiKeyDataToPublish);
		}
		notify(data, eventType);
	}
	
	/**
	 * 
	 * @param data
	 * @param eventType
	 */
	private void notify(Map<String, Object> data,EventType eventType) {
		Type type = new Type();
		type.setName("PartnerManagementServiceImpl");
		type.setNamespace("io.mosip.pmp.partner.manager.service.impl.PartnerManagementServiceImpl");
		webSubPublisher.notify(eventType, data, type);
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
			LOGGER.error("Error occured while sending the partner update notifications.", e.getLocalizedMessage(),
					e.getMessage());
		}
	}
	
	private void sendNotifications(EventType eventType, Partner partner,PartnerPolicy partnerPolicyFromDb) {
		List<NotificationDto> notificationDtos = new ArrayList<NotificationDto>();
		Optional<AuthPolicy> authPolicy = authPolicyRepository.findById(partnerPolicyFromDb.getPolicyId());
		NotificationDto dto = new NotificationDto();
		dto.setPartnerId(partner.getId());
		dto.setPartnerName(partner.getName());
		dto.setEmailId(partner.getEmailId());
		dto.setLangCode(partner.getLangCode());
		dto.setPartnerStatus(partner.getIsActive() == true ? PartnerConstants.ACTIVE : PartnerConstants.DEACTIVE);
		dto.setApiKey(partnerPolicyFromDb.getPolicyApiKey());
		dto.setApiKeyExpiryDate(partnerPolicyFromDb.getValidToDatetime().toLocalDateTime());
		dto.setApiKeyStatus(partnerPolicyFromDb.getIsActive() == true ? PartnerConstants.ACTIVE : PartnerConstants.DEACTIVE);
		dto.setPolicyId(partnerPolicyFromDb.getPolicyId());
		dto.setPolicyName(authPolicy.isPresent() ? authPolicy.get().getName() : null);
		dto.setPolicyExpiryDateTime(authPolicy.isPresent() ? authPolicy.get().getValidToDate() : null);
		dto.setPolicyStatus((authPolicy.isPresent() && authPolicy.get().getIsActive() == true) ? PartnerConstants.ACTIVE : PartnerConstants.DEACTIVE);
		notificationDtos.add(dto);
		try {
			notificationService.sendNotications(eventType, notificationDtos);
		} catch (Exception e) {
			LOGGER.error("Error occured while sending the apikey notifications.", e.getLocalizedMessage(),
					e.getMessage());
		}
	}

	@Override
	public APIKeyGenerateResponseDto generateAPIKey(String partnerId, APIKeyGenerateRequestDto requestDto) {
		if(!getUser().equals(partnerId)) {
			throw new PartnerManagerServiceException(ErrorCode.LOGGEDIN_USER_NOT_AUTHORIZED.getErrorCode(),
					ErrorCode.LOGGEDIN_USER_NOT_AUTHORIZED.getErrorMessage());
		}
		Optional<Partner> partnerFromDb = partnerRepository.findById(partnerId);
		if (partnerFromDb.isEmpty()) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.GENERATE_API_KEY_FAILURE, partnerId, "partnerId");
			throw new PartnerManagerServiceException(ErrorCode.PARTNER_ID_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_ID_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		if (!partnerFromDb.get().getIsActive()) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.GENERATE_API_KEY_FAILURE, partnerId, "partnerId");
			throw new PartnerManagerServiceException(ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorMessage());
		}
		AuthPolicy validPolicy = authPolicyRepository.findByPolicyGroupIdAndName(partnerFromDb.get().getPolicyGroupId(),requestDto.getPolicyName());
		if(validPolicy == null) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.GENERATE_API_KEY_FAILURE, partnerId, "partnerId");
			throw new PartnerManagerServiceException(ErrorCode.POLICY_NOT_EXIST_EXCEPTION.getErrorCode(),
					ErrorCode.POLICY_NOT_EXIST_EXCEPTION.getErrorMessage());			
		}		
		List<PartnerPolicyRequest> approvedMappedPolicy = partnerPolicyRequestRepository
				.findByPartnerIdAndPolicyIdAndStatusCode(partnerId, validPolicy.getId(),
						PartnerConstants.APPROVED);
		if (approvedMappedPolicy.isEmpty()) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.GENERATE_API_KEY_FAILURE, partnerId, "partnerId");
			throw new PartnerManagerServiceException(ErrorCode.PARTNER_POLICY_MAPPING_NOT_EXISTS.getErrorCode(),
					ErrorCode.PARTNER_POLICY_MAPPING_NOT_EXISTS.getErrorMessage());
		}		
		PartnerPolicy policyByLabel = partnerPolicyRepository.findByPartnerIdPolicyIdAndLabel(
				partnerFromDb.get().getId(), validPolicy.getId(), PartnerUtil.trimAndReplace(requestDto.getLabel()));
		if(policyByLabel != null) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.GENERATE_API_KEY_FAILURE, partnerId, "partnerId");
			throw new PartnerManagerServiceException(ErrorCode.PARTNER_POLICY_LABEL_EXISTS.getErrorCode(),
					ErrorCode.PARTNER_POLICY_LABEL_EXISTS.getErrorMessage());
		}
		APIKeyGenerateResponseDto response = new APIKeyGenerateResponseDto();
		PartnerPolicy partnerPolicy = new PartnerPolicy();
		partnerPolicy.setPolicyApiKey(PartnerUtil.createPartnerApiKey());
		partnerPolicy.setPartner(approvedMappedPolicy.get(0).getPartner());
		partnerPolicy.setPolicyId(approvedMappedPolicy.get(0).getPolicyId());
		partnerPolicy.setIsActive(true);
		partnerPolicy.setIsDeleted(false);
		partnerPolicy.setLabel(requestDto.getLabel());
		partnerPolicy.setValidFromDatetime(Timestamp.valueOf(LocalDateTime.now()));
		partnerPolicy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(partnerPolicyExpiryInDays)));
		partnerPolicy.setCrBy(getUser());
		partnerPolicy.setCrDtimes(Timestamp.valueOf(LocalDateTime.now()));
		partnerPolicyRepository.save(partnerPolicy);		
		notify(MapperUtils.mapDataToPublishDto(approvedMappedPolicy.get(0).getPartner(),
				getPartnerCertificate(approvedMappedPolicy.get(0).getPartner().getCertificateAlias())),
				MapperUtils.mapPolicyToPublishDto(validPolicy, getPolicyObject(validPolicy.getPolicyFileId())),
				MapperUtils.mapKeyDataToPublishDto(partnerPolicy), EventType.APIKEY_APPROVED);
		response.setApiKey(partnerPolicy.getPolicyApiKey());
		response.setLabel(partnerPolicy.getLabel());
		response.setPartnerId(partnerId);
		response.setPolicyId(approvedMappedPolicy.get(0).getPolicyId());
		auditUtil.setAuditRequestDto(PartnerManageEnum.GENERATE_API_KEY_SUCCESS, partnerId, "partnerId");
		return response;	
	}

	@Override
	public String approveRejectPartnerPolicyMapping(String mappingkey, StatusRequestDto statusRequest) {		
		PartnerPolicyRequest updateObject = getValidApikeyRequestForStatusUpdate(mappingkey);
		validatePolicy(updateObject.getPolicyId());		
		if ((statusRequest.getStatus().equalsIgnoreCase(PartnerConstants.APPROVED))) {
			updateObject.setUpdBy(getUser());
			updateObject.setUpdDtimes(Timestamp.valueOf(LocalDateTime.now()));
			updateObject.setStatusCode(PartnerConstants.APPROVED);
			partnerPolicyRequestRepository.save(updateObject);			
			auditUtil.setAuditRequestDto(PartnerManageEnum.APPROVE_REJECT_PARTNER_API_SUCCESS, mappingkey, "mappingKey");
			return "Policy mapping approved successfully";
		}
		if ((statusRequest.getStatus().equalsIgnoreCase(PartnerConstants.REJECTED))) {
			updateObject.setUpdBy(getUser());
			updateObject.setUpdDtimes(Timestamp.valueOf(LocalDateTime.now()));
			updateObject.setStatusCode(PartnerConstants.REJECTED);
			partnerPolicyRequestRepository.save(updateObject);			
			auditUtil.setAuditRequestDto(PartnerManageEnum.APPROVE_REJECT_PARTNER_API_SUCCESS, mappingkey, "mappingKey");
			return "Policy mapping rejected successfully";
		}
		auditUtil.setAuditRequestDto(PartnerManageEnum.APPROVE_REJECT_PARTNER_API_FAILURE, mappingkey, "mappingKey");
		LOGGER.info(statusRequest.getStatus() + " : Invalid Input Parameter (status should be Approved/Rejected)");
		throw new PartnerManagerServiceException(ErrorCode.INVALID_STATUS_CODE.getErrorCode(),
				ErrorCode.INVALID_STATUS_CODE.getErrorMessage());
	}

	@Override
	public String updateAPIKeyStatus(String partnerId, String policyId, APIkeyStatusUpdateRequestDto request) {
		PartnerPolicy policyByLabel = partnerPolicyRepository.findByPartnerIdPolicyIdAndLabel(partnerId, policyId,
				request.getLabel());
		if(policyByLabel == null) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.ACTIVATE_DEACTIVATE_API_PARTNERS_FAILED, partnerId, "partnerId");
			throw new PartnerManagerServiceException(ErrorCode.PARTNER_POLICY_LABEL_NOT_EXISTS.getErrorCode(),
					ErrorCode.PARTNER_POLICY_LABEL_NOT_EXISTS.getErrorMessage());
		}
		/*
		// check if Partner is Active or not
		if (policyByLabel.getPartner() != null && !policyByLabel.getPartner().getIsActive()) {
			LOGGER.error("Partner is not Active, hence status of API key cannot be updated, for partner: " + partnerId);
			auditUtil.setAuditRequestDto(PartnerManageEnum.ACTIVATE_DEACTIVATE_API_PARTNERS_FAILED, partnerId,
					"partnerId");
			throw new PartnerManagerServiceException(ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorMessage());
		}
		*/
		// check if API key has been already deactivated
		if (!policyByLabel.getIsActive() && request.getStatus().equalsIgnoreCase(PartnerConstants.DEACTIVE)) {
			LOGGER.error(
					"API key is already deactivated hence it cannot be deactivated again, for partner: " + partnerId);
			auditUtil.setAuditRequestDto(PartnerManageEnum.ACTIVATE_DEACTIVATE_API_PARTNERS_FAILED, partnerId,
					"partnerId");
			throw new PartnerManagerServiceException(ErrorCode.PARTNER_APIKEY_NOT_ACTIVE_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_APIKEY_NOT_ACTIVE_EXCEPTION.getErrorMessage());
		}
		policyByLabel.setUpdBy(getUser());
		policyByLabel.setUpdDtimes(Timestamp.valueOf(LocalDateTime.now()));		
		if (request.getStatus().equalsIgnoreCase(PartnerConstants.ACTIVE)) {
			policyByLabel.setIsActive(true);
			notify(null, null, MapperUtils.mapKeyDataToPublishDto(policyByLabel), EventType.APIKEY_UPDATED);
			partnerPolicyRepository.save(policyByLabel);			
			auditUtil.setAuditRequestDto(PartnerManageEnum.ACTIVATE_DEACTIVATE_API_PARTNERS_SUCCESS, partnerId, "partnerId");
			sendNotifications(EventType.APIKEY_STATUS_UPDATED, policyByLabel.getPartner(), policyByLabel);
			return "Partner apikey activated successfully.";
		}
		if (request.getStatus().equalsIgnoreCase(PartnerConstants.DEACTIVE)) {
			policyByLabel.setIsActive(false);
			notify(null, null, MapperUtils.mapKeyDataToPublishDto(policyByLabel), EventType.APIKEY_UPDATED);
			partnerPolicyRepository.save(policyByLabel);			
			auditUtil.setAuditRequestDto(PartnerManageEnum.ACTIVATE_DEACTIVATE_API_PARTNERS_SUCCESS, partnerId, "partnerId");
			sendNotifications(EventType.APIKEY_STATUS_UPDATED, policyByLabel.getPartner(), policyByLabel);
			return "Partner apikey de-activated successfully.";
		}
		auditUtil.setAuditRequestDto(PartnerManageEnum.ACTIVATE_DEACTIVATE_API_PARTNERS_FAILED, partnerId, "partnerId");
		LOGGER.info(request.getStatus() + " : is Invalid Input Parameter, it should be (Active/De-Active)");
		throw new PartnerManagerServiceException(ErrorCode.INVALID_STATUS_CODE_ACTIVE_DEACTIVE.getErrorCode(),
				ErrorCode.INVALID_STATUS_CODE_ACTIVE_DEACTIVE.getErrorMessage());
	}

	@Override
	public ResponseWrapperV2<PartnerDetailsV3Dto> getPartnerDetails(String partnerId) {
		ResponseWrapperV2<PartnerDetailsV3Dto> responseWrapper = new ResponseWrapperV2<>();
		try {
			String userId = getUserId();
			List<Partner> partnerList = partnerServiceRepository.findByUserId(userId);
			if (partnerList.isEmpty()) {
				LOGGER.info("sessionId", "idType", "id", "User id does not exists.");
				throw new PartnerServiceException(io.mosip.pms.partner.constant.ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
						io.mosip.pms.partner.constant.ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
			}
			if (Objects.isNull(partnerId) || partnerId.isEmpty()) {
				throw new PartnerServiceException(
						io.mosip.pms.partner.constant.ErrorCode.INVALID_REQUEST_PARAM.getErrorCode(),
						io.mosip.pms.partner.constant.ErrorCode.INVALID_REQUEST_PARAM.getErrorMessage()
				);
			}
			Optional<Partner> optionalPartner = partnerServiceRepository.findById(partnerId);
			if (optionalPartner.isEmpty()) {
				throw new PartnerServiceException(
						io.mosip.pms.partner.constant.ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
						io.mosip.pms.partner.constant.ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage()
				);
			}
			PartnerDetailsV3Dto partnerDetailsV3Dto = new PartnerDetailsV3Dto();
			Partner partner = optionalPartner.get();
			partnerDetailsV3Dto.setPartnerId(partner.getId());
			partnerDetailsV3Dto.setApprovalStatus(partner.getApprovalStatus());
			partnerDetailsV3Dto.setIsActive(partner.getIsActive());
			partnerDetailsV3Dto.setCreatedDateTime(partner.getCrDtimes().toLocalDateTime());
			partnerDetailsV3Dto.setPartnerType(partner.getPartnerTypeCode());
			partnerDetailsV3Dto.setOrganizationName(partner.getName());
			partnerDetailsV3Dto.setEmailId(partner.getEmailId());
			partnerDetailsV3Dto.setContactNumber(partner.getContactNo());
			if ((!partner.getPartnerTypeCode().equals(FTM_PROVIDER) &&
					!partner.getPartnerTypeCode().equals(DEVICE_PROVIDER) &&
					(Objects.isNull(partner.getPolicyGroupId()) || partner.getPolicyGroupId().isEmpty()))) {
				LOGGER.info("sessionId", "idType", "id",
						"Policy Group Id is empty for partner Id -" + partner.getId());
				throw new PartnerServiceException(
						io.mosip.pms.partner.constant.ErrorCode.POLICY_GROUP_ID_NOT_EXISTS.getErrorCode(),
						io.mosip.pms.partner.constant.ErrorCode.POLICY_GROUP_ID_NOT_EXISTS.getErrorMessage()
				);
			}
			if (Objects.nonNull(partner.getPolicyGroupId())) {
				PolicyGroup policyGroup = policyGroupRepository.findPolicyGroupById(partner.getPolicyGroupId());
				if (Objects.isNull(policyGroup)) {
					throw new PartnerServiceException(
							io.mosip.pms.partner.constant.ErrorCode.MATCHING_POLICY_GROUP_NOT_EXISTS.getErrorCode(),
							io.mosip.pms.partner.constant.ErrorCode.MATCHING_POLICY_GROUP_NOT_EXISTS.getErrorMessage()
					);
				}
				partnerDetailsV3Dto.setPolicyGroupName(policyGroup.getName());
			}
			if (Objects.isNull(partner.getCertificateAlias())){
				partnerDetailsV3Dto.setIsCertificateAvailable(false);
			} else {
				PartnerCertDownloadRequestDto requestDto = new PartnerCertDownloadRequestDto();
				requestDto.setPartnerId(partner.getId());

				PartnerCertDownloadResponeDto partnerCertDownloadResponeDto = partnerHelper.getCertificate(partner.getCertificateAlias(),
						"pmp.partner.certificaticate.get.rest.uri", PartnerCertDownloadResponeDto.class);
				X509Certificate cert = MultiPartnerUtil.decodeCertificateData(partnerCertDownloadResponeDto.getCertificateData());
				partnerDetailsV3Dto.setCertificateIssuedTo(PartnerUtil.getCertificateName(cert.getSubjectDN().getName()));
				partnerDetailsV3Dto.setCertificateUploadDateTime(cert.getNotBefore());
				partnerDetailsV3Dto.setCertificateExpiryDateTime(cert.getNotAfter());
				partnerDetailsV3Dto.setIsCertificateAvailable(true);
			}
			responseWrapper.setResponse(partnerDetailsV3Dto);
		} catch (PartnerServiceException ex) {
			LOGGER.info("sessionId", "idType", "id",
					"In getPartnerDetails method of PartnerManagementServiceImpl - " + ex.getMessage());
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (Exception ex) {
			LOGGER.error("sessionId", "idType", "id",
					"Error in getPartnerDetails method of PartnerManagementServiceImpl - " + ex.getMessage());
			// Error when keyalias is present and certificate is missing in Keymanager
			if (ex instanceof ApiAccessibleException && "KER-PCM-012".equals(((ApiAccessibleException) ex).getErrorCode())) {
				String errorCode = ErrorCode.CERTIFICATE_NOT_AVAILABLE_IN_KM.getErrorCode();
				String errorMessage = ErrorCode.CERTIFICATE_NOT_AVAILABLE_IN_KM.getErrorMessage();
				responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
			} else {
				String errorCode = ErrorCode.FETCH_PARTNER_DETAILS_ERROR.getErrorCode();
				String errorMessage = ErrorCode.FETCH_PARTNER_DETAILS_ERROR.getErrorMessage();
				responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
			}
		}
		responseWrapper.setId(getPartnerDetailsId);
		responseWrapper.setVersion(VERSION);
		return responseWrapper;
	}

	@Override
	public ResponseWrapperV2<PageResponseV2Dto<PartnerSummaryDto>> getAllPartners(String sortFieldName, String sortType, int pageNo, int pageSize, FilterDto filterDto) {
		ResponseWrapperV2<PageResponseV2Dto<PartnerSummaryDto>> responseWrapper = new ResponseWrapperV2<>();
		try {
			PageResponseV2Dto<PartnerSummaryDto> pageResponseV2Dto = new PageResponseV2Dto<>();
			// Pagination
			Pageable pageable = PageRequest.of(pageNo, pageSize);

			//Sorting
			if (Objects.nonNull(sortFieldName) && Objects.nonNull(sortType)) {
				if (sortFieldName.equalsIgnoreCase("certificateUploadStatus") || sortFieldName.equalsIgnoreCase("isActive")) {
					sortType = sortType.equalsIgnoreCase(PartnerConstants.ASC) ? PartnerConstants.DESC : PartnerConstants.ASC;
				}
				Sort sort = partnerHelper.getSortingRequest(getSortColumn(sortFieldName), sortType);
				pageable = PageRequest.of(pageNo, pageSize, sort);
			}

			Page<PartnerSummaryEntity> page = partnerSummaryRepository.
					getSummaryOfAllPartners(filterDto.getPartnerId(), filterDto.getPartnerTypeCode(),
							filterDto.getOrganizationName(), filterDto.getPolicyGroupName(),
							filterDto.getCertificateUploadStatus(), filterDto.getEmailAddress(),
							filterDto.getIsActive(), pageable);
			if (Objects.nonNull(page) && !page.getContent().isEmpty()) {
				List<PartnerSummaryDto> partnerSummaryDtoList = MapperUtils.mapAll(page.getContent(), PartnerSummaryDto.class);
				pageResponseV2Dto.setPageNo(pageNo);
				pageResponseV2Dto.setPageSize(pageSize);
				pageResponseV2Dto.setTotalResults(page.getTotalElements());
				pageResponseV2Dto.setData(partnerSummaryDtoList);
			}
			responseWrapper.setResponse(pageResponseV2Dto);
		} catch (PartnerServiceException ex) {
			LOGGER.info("sessionId", "idType", "id", "In getAllPartners method of PartnerManagementServiceImpl - " + ex.getMessage());
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (Exception ex) {
			LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
			LOGGER.error("sessionId", "idType", "id",
					"In getAllPartners method of PartnerManagementServiceImpl - " + ex.getMessage());
			String errorCode = ErrorCode.FETCH_ALL_PARTNER_DETAILS_ERROR.getErrorCode();
			String errorMessage = ErrorCode.FETCH_ALL_PARTNER_DETAILS_ERROR.getErrorMessage();
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
		}
		responseWrapper.setId(getAllPartnersId);
		responseWrapper.setVersion(VERSION);
		return responseWrapper;
	}

	public String getSortColumn(String alias) {
		return partnerHelper.aliasToColumnMap.getOrDefault(alias, alias); // Return alias if no match found
	}

	private AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	private String getUserId() {
		String userId = authUserDetails().getUserId();
		return userId;
	}
}

