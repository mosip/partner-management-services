package io.mosip.pms.partner.manager.service.impl;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.transaction.Transactional;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.pms.common.constant.ApiAccessibleExceptionConstant;
import io.mosip.pms.common.constant.EventType;
import io.mosip.pms.common.dto.APIKeyDataPublishDto;
import io.mosip.pms.common.dto.PartnerDataPublishDto;
import io.mosip.pms.common.dto.PolicyPublishDto;
import io.mosip.pms.common.dto.Type;
import io.mosip.pms.common.entity.AuthPolicy;
import io.mosip.pms.common.entity.BiometricExtractorProvider;
import io.mosip.pms.common.entity.MISPEntity;
import io.mosip.pms.common.entity.MISPLicenseEntity;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.entity.PartnerPolicy;
import io.mosip.pms.common.entity.PartnerPolicyRequest;
import io.mosip.pms.common.exception.ApiAccessibleException;
import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.repository.AuthPolicyRepository;
import io.mosip.pms.common.repository.BiometricExtractorProviderRepository;
import io.mosip.pms.common.repository.MispLicenseKeyRepository;
import io.mosip.pms.common.repository.MispServiceRepository;
import io.mosip.pms.common.repository.PartnerPolicyRepository;
import io.mosip.pms.common.repository.PartnerPolicyRequestRepository;
import io.mosip.pms.common.repository.PartnerRepository;
import io.mosip.pms.common.repository.PolicyGroupRepository;
import io.mosip.pms.common.response.dto.NotificationDto;
import io.mosip.pms.common.service.NotificatonService;
import io.mosip.pms.common.util.MapperUtils;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.partner.constant.PartnerConstants;
import io.mosip.pms.partner.manager.constant.ErrorCode;
import io.mosip.pms.partner.manager.constant.PartnerManageEnum;
import io.mosip.pms.partner.manager.dto.ActivateDeactivatePartnerRequest;
import io.mosip.pms.partner.manager.dto.ApikeyRequests;
import io.mosip.pms.partner.manager.dto.PartnerAPIKeyToPolicyMappingsResponse;
import io.mosip.pms.partner.manager.dto.PartnerPolicyResponse;
import io.mosip.pms.partner.manager.dto.PartnersPolicyMappingRequest;
import io.mosip.pms.partner.manager.dto.PartnersPolicyMappingResponse;
import io.mosip.pms.partner.manager.dto.RetrievePartnerDetailsResponse;
import io.mosip.pms.partner.manager.dto.RetrievePartnersDetails;
import io.mosip.pms.partner.manager.exception.PartnerManagerServiceException;
import io.mosip.pms.partner.manager.service.PartnerManagerService;
import io.mosip.pms.partner.response.dto.PartnerCertDownloadResponeDto;

@Service
@Transactional
public class PartnerManagementServiceImpl implements PartnerManagerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PartnerManagementServiceImpl.class);

	@Autowired
	private MispLicenseKeyRepository misplKeyRepository;

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

	@Value("${pmp.bioextractors.required.partner.types}")
	private String biometricExtractorsRequiredPartnerTypes;

	@Value("${mosip.pmp.partner.policy.expiry.period.indays}")
	private int partnerPolicyExpiryInDays;

	@Override
	public PartnersPolicyMappingResponse updatePolicyAgainstApikey(PartnersPolicyMappingRequest request,
			String partnerId, String partnerApikey) {
		PartnerPolicy partnerPolicyFromDb = partnerPolicyRepository.findByPartnerIdAndPolicyIdAndApikey(partnerId,
				request.getOldPolicyID(), partnerApikey);
		if (partnerPolicyFromDb == null) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.API_KEY_MAPPING_FAILURE);
			throw new PartnerManagerServiceException(ErrorCode.PARTNER_APIKEY_POLICY_MAPPING_NOTEXISTS.getErrorCode(),
					ErrorCode.PARTNER_APIKEY_POLICY_MAPPING_NOTEXISTS.getErrorMessage());

		}
		if (!partnerPolicyFromDb.getIsActive()) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.API_KEY_MAPPING_FAILURE);
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
		auditUtil.setAuditRequestDto(PartnerManageEnum.API_KEY_MAPPING_SUCCESS);
		return partnersPolicyMappingResponse;
	}

	private AuthPolicy validateAndGetPolicyOfPolicyGroup(String policyGroupId, String policyId) {
		AuthPolicy authPolicy = authPolicyRepository.findByPolicyGroupAndId(policyGroupId, policyId);
		if (authPolicy == null) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.API_KEY_MAPPING_FAILURE);
			throw new PartnerManagerServiceException(ErrorCode.POLICY_NOT_EXIST_EXCEPTION.getErrorCode(),
					ErrorCode.POLICY_NOT_EXIST_EXCEPTION.getErrorMessage() + policyId);

		}
		if (!authPolicy.getIsActive()) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.API_KEY_MAPPING_FAILURE);
			throw new PartnerManagerServiceException(ErrorCode.POLICY_NOT_ACTIVE_EXCEPTION.getErrorCode(),
					ErrorCode.POLICY_NOT_ACTIVE_EXCEPTION.getErrorMessage() + policyId);

		}
		if (!authPolicy.getPolicyGroup().getIsActive()) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.API_KEY_MAPPING_FAILURE);
			throw new PartnerManagerServiceException(ErrorCode.POLICY_GROUP_NOT_ACTIVE.getErrorCode(),
					ErrorCode.POLICY_GROUP_NOT_ACTIVE.getErrorMessage() + policyId);
		}
		return authPolicy;
	}

	@Override
	public PartnersPolicyMappingResponse activateDeactivateAuthEKYCPartner(String partnerId,
			ActivateDeactivatePartnerRequest request) {
		Optional<Partner> partnerFromDb = partnerRepository.findById(partnerId);
		if (partnerFromDb.isEmpty()) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.ACTIVATE_DEACTIVATE_KYC_PARTNERS_FAILURE);
			auditUtil.setAuditRequestDto(PartnerManageEnum.ACTIVATE_DEACTIVATE_KYC_PARTNERS_FAILURE);
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
			updatePartnerObject.setIsActive(true);			
			partnerRepository.save(updatePartnerObject);
			response.setMessage("Partner activated successfully");			
			auditUtil.setAuditRequestDto(PartnerManageEnum.ACTIVATE_DEACTIVATE_KYC_PARTNERS_SUCCESS);
			notify(MapperUtils.mapDataToPublishDto(updatePartnerObject,getPartnerCertificate(updatePartnerObject.getCertificateAlias())), null, null, EventType.PARTNER_UPDATED);
			sendNotifications(EventType.PARTNER_UPDATED, updatePartnerObject);
			return response;
		}
		if (request.getStatus().equalsIgnoreCase(PartnerConstants.DEACTIVE)) {
			updatePartnerObject.setIsActive(false);
			partnerRepository.save(updatePartnerObject);
			response.setMessage("Partner de-activated successfully");
			auditUtil.setAuditRequestDto(PartnerManageEnum.ACTIVATE_DEACTIVATE_KYC_PARTNERS_SUCCESS);
			notify(MapperUtils.mapDataToPublishDto(updatePartnerObject,getPartnerCertificate(updatePartnerObject.getCertificateAlias())), null, null, EventType.PARTNER_UPDATED);
			sendNotifications(EventType.PARTNER_UPDATED, updatePartnerObject);
			return response;
		}
		auditUtil.setAuditRequestDto(PartnerManageEnum.ACTIVATE_DEACTIVATE_KYC_PARTNERS_FAILURE);
		LOGGER.info(request.getStatus() + " : is Invalid Input Parameter, it should be (Active/De-Active)");
		throw new PartnerManagerServiceException(ErrorCode.INVALID_STATUS_CODE_ACTIVE_DEACTIVE.getErrorCode(),
				ErrorCode.INVALID_STATUS_CODE_ACTIVE_DEACTIVE.getErrorMessage());
	}

	@Override
	public PartnersPolicyMappingResponse activateDeactivatePartnerAPIKeyGivenPartner(String partnerId,
			ActivateDeactivatePartnerRequest request, String partnerAPIKey) {
		PartnerPolicy partnerPolicyFromDb = partnerPolicyRepository.findByPartnerIdAndApikey(partnerId, partnerAPIKey);
		if (partnerPolicyFromDb == null) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.ACTIVATE_DEACTIVATE_API_PARTNERS_FAILED);
			throw new PartnerManagerServiceException(ErrorCode.PARTNER_API_KEY_NOT_MAPPED.getErrorCode(),
					ErrorCode.PARTNER_API_KEY_NOT_MAPPED.getErrorMessage());
		}
		partnerPolicyFromDb.setUpdBy(getUser());
		partnerPolicyFromDb.setUpdDtimes(Timestamp.valueOf(LocalDateTime.now()));
		PartnersPolicyMappingResponse response = new PartnersPolicyMappingResponse();
		if (request.getStatus().equalsIgnoreCase(PartnerConstants.ACTIVE)) {
			partnerPolicyFromDb.setIsActive(true);
			notify(null, null, MapperUtils.mapKeyDataToPublishDto(partnerPolicyFromDb), EventType.APIKEY_UPDATED);
			partnerPolicyRepository.save(partnerPolicyFromDb);
			response.setMessage("Partner apikey activated successfully.");
			auditUtil.setAuditRequestDto(PartnerManageEnum.ACTIVATE_DEACTIVATE_API_PARTNERS_SUCCESS);
			sendNotifications(EventType.APIKEY_STATUS_UPDATED, partnerPolicyFromDb.getPartner(), partnerPolicyFromDb);
			return response;
		}
		if (request.getStatus().equalsIgnoreCase(PartnerConstants.DEACTIVE)) {
			partnerPolicyFromDb.setIsActive(false);
			notify(null, null, MapperUtils.mapKeyDataToPublishDto(partnerPolicyFromDb), EventType.APIKEY_UPDATED);
			partnerPolicyRepository.save(partnerPolicyFromDb);
			response.setMessage("Partner apikey de-activated successfully.");
			auditUtil.setAuditRequestDto(PartnerManageEnum.ACTIVATE_DEACTIVATE_API_PARTNERS_SUCCESS);
			sendNotifications(EventType.APIKEY_STATUS_UPDATED, partnerPolicyFromDb.getPartner(), partnerPolicyFromDb);
			return response;
		}
		auditUtil.setAuditRequestDto(PartnerManageEnum.ACTIVATE_DEACTIVATE_API_PARTNERS_FAILED);
		LOGGER.info(request.getStatus() + " : is Invalid Input Parameter, it should be (Active/De-Active)");
		throw new PartnerManagerServiceException(ErrorCode.INVALID_STATUS_CODE_ACTIVE_DEACTIVE.getErrorCode(),
				ErrorCode.INVALID_STATUS_CODE_ACTIVE_DEACTIVE.getErrorMessage());
	}

	@Override
	public RetrievePartnerDetailsResponse getAllAuthEKYCPartnersForThePolicyGroup(Optional<String> partnerType) {
		RetrievePartnerDetailsResponse partnersResponse = new RetrievePartnerDetailsResponse();
		List<RetrievePartnersDetails> partners = new ArrayList<RetrievePartnersDetails>();
		List<Partner> partnersFromDb = null;
		if (partnerType.isPresent() && !partnerType.get().trim().isEmpty()) {
			partnersFromDb = partnerRepository.findByPartnerType(partnerType.get());
		} else {
			partnersFromDb = partnerRepository.findAll();
		}
		Partner partner = null;
		if (partnersFromDb.isEmpty()) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_POLICY_GROUP_FAILURE);
			throw new PartnerManagerServiceException(ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		Iterator<Partner> partnerIterat = partnersFromDb.iterator();
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
		auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_POLICY_GROUP_SUCCESS);
		return partnersResponse;
	}

	@Override
	public PartnerAPIKeyToPolicyMappingsResponse getPartnerAPIKeyToPolicyMapping(String partnerId,
			String partnerAPIKey) {
		PartnerPolicy partnerPolicyFromDb = partnerPolicyRepository.findByPartnerIdAndApikey(partnerId, partnerAPIKey);
		if (partnerPolicyFromDb == null) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.API_KEY_MAPPING_FAILURE);
			throw new PartnerManagerServiceException(ErrorCode.PARTNER_API_KEY_NOT_MAPPED.getErrorCode(),
					ErrorCode.PARTNER_API_KEY_NOT_MAPPED.getErrorMessage());
		}
		PartnerAPIKeyToPolicyMappingsResponse response = new PartnerAPIKeyToPolicyMappingsResponse();
		response.setPartnerID(partnerId);
		response.setPolicyId(partnerPolicyFromDb.getPolicyId());
		auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_SUCCESS);
		return response;
	}

	@Override
	public List<ApikeyRequests> getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagers() {
		List<PartnerPolicyRequest> apikeyRequestsFromDb = partnerPolicyRequestRepository.findAll();
		if (apikeyRequestsFromDb.isEmpty()) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_FAILURE);
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
		auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_SUCCESS);
		return response;
	}

	@Override
	public ApikeyRequests getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestId(String apiKeyReqId) {
		Optional<PartnerPolicyRequest> apikeyRequestsFromDb = partnerPolicyRequestRepository.findById(apiKeyReqId);
		if (apikeyRequestsFromDb.isEmpty()) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_FAILURE);
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
		auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_SUCCESS);
		return apikeyRequest;
	}

	@Override
	public PartnersPolicyMappingResponse approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(
			ActivateDeactivatePartnerRequest request, String requestedApikey) {
		PartnersPolicyMappingResponse response = new PartnersPolicyMappingResponse();
		PartnerPolicyRequest updateObject = getValidApikeyRequestForStatusUpdate(requestedApikey);
		AuthPolicy validPolicy = validatePolicy(updateObject.getPolicyId());		
		if ((request.getStatus().equalsIgnoreCase(PartnerConstants.APPROVED))) {
			updateObject.setUpdBy(getUser());
			updateObject.setUpdDtimes(Timestamp.valueOf(LocalDateTime.now()));
			updateObject.setStatusCode(PartnerConstants.APPROVED);
			PartnerPolicy partnerPolicy = new PartnerPolicy();
			partnerPolicy.setPolicyApiKey(requestedApikey);
			partnerPolicy.setPartner(updateObject.getPartner());
			partnerPolicy.setPolicyId(updateObject.getPolicyId());
			partnerPolicy.setIsActive(true);
			partnerPolicy.setIsDeleted(false);
			partnerPolicy.setValidFromDatetime(Timestamp.valueOf(LocalDateTime.now()));
			partnerPolicy
					.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(partnerPolicyExpiryInDays)));
			partnerPolicy.setCrBy(getUser());
			partnerPolicy.setCrDtimes(Timestamp.valueOf(LocalDateTime.now()));
			partnerPolicyRequestRepository.save(updateObject);
			partnerPolicyRepository.save(partnerPolicy);
			response.setMessage("Apikey request approved successfully.");
			auditUtil.setAuditRequestDto(PartnerManageEnum.APPROVE_REJECT_PARTNER_API_SUCCESS);
			notify(MapperUtils.mapDataToPublishDto(updateObject.getPartner(),getPartnerCertificate(updateObject.getPartner().getCertificateAlias())),
					MapperUtils.mapPolicyToPublishDto(validPolicy, getPolicyObject(validPolicy.getPolicyFileId())),
					MapperUtils.mapKeyDataToPublishDto(partnerPolicy), EventType.APIKEY_APPROVED);
			return response;
		}
		if ((request.getStatus().equalsIgnoreCase(PartnerConstants.REJECTED))) {
			updateObject.setUpdBy(getUser());
			updateObject.setUpdDtimes(Timestamp.valueOf(LocalDateTime.now()));
			updateObject.setStatusCode(PartnerConstants.REJECTED);
			partnerPolicyRequestRepository.save(updateObject);
			response.setMessage("Apikey request rejected successfully.");
			auditUtil.setAuditRequestDto(PartnerManageEnum.APPROVE_REJECT_PARTNER_API_SUCCESS);
			return response;
		}
		auditUtil.setAuditRequestDto(PartnerManageEnum.APPROVE_REJECT_PARTNER_API_FAILURE);
		LOGGER.info(request.getStatus() + " : Invalid Input Parameter (status should be Approved/Rejected)");
		throw new PartnerManagerServiceException(ErrorCode.INVALID_STATUS_CODE.getErrorCode(),
				ErrorCode.INVALID_STATUS_CODE.getErrorMessage());
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

	@Override
	public PartnerPolicyResponse getPartnerMappedPolicyFile(String mispLicenseKey, String policy_api_key,
			String partnerId, boolean needPartnerCert) {
		Partner validPartner = validateAndGetPartner(partnerId);
		MISPLicenseEntity validLicense = validateAndGetMispLicense(mispLicenseKey);
		PartnerPolicy partnerPolicy = validateAndGetApikey(partnerId, policy_api_key);
		AuthPolicy policy = validatePolicy(partnerPolicy.getPolicyId());
		PartnerPolicyResponse response = new PartnerPolicyResponse();
		if (needPartnerCert) {
			response.setCertificateData(getPartnerCertificate(validPartner.getCertificateAlias()));
		}
		response.setPolicyId(policy.getId());
		response.setPolicyDescription(policy.getPolicyGroup().getDesc());
		response.setPolicy(getPolicyObject(policy.getPolicyFileId()));
		response.setPolicyStatus(policy.getIsActive());
		response.setPartnerId(validPartner.getId());
		response.setPartnerName(validPartner.getName());
		response.setPolicyName(policy.getName());
		response.setMispExpiresOn(toISOFormat(validLicense.getValidToDate()));
		response.setPolicyExpiresOn(toISOFormat(policy.getValidToDate()));
		response.setApiKeyExpiresOn(toISOFormat(partnerPolicy.getValidToDatetime().toLocalDateTime()));
		auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_POLICY_MAPPING_SUCCESS);
		return response;
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

	private static LocalDateTime toISOFormat(LocalDateTime localDateTime) {
		ZonedDateTime zonedtime = localDateTime.atZone(ZoneId.systemDefault());
		ZonedDateTime converted = zonedtime.withZoneSameInstant(ZoneOffset.UTC);
		return converted.toLocalDateTime();
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

	private PartnerPolicy validateAndGetApikey(String partnerId, String policy_api_key) {
		PartnerPolicy partnerPolicyFromDb = partnerPolicyRepository.findByPartnerIdAndApikey(partnerId, policy_api_key);
		if (partnerPolicyFromDb == null) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.APPROVE_REJECT_PARTNER_API_FAILURE);
			throw new PartnerManagerServiceException(ErrorCode.PARTNER_NOT_MAPPED_TO_POLICY_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_NOT_MAPPED_TO_POLICY_EXCEPTION.getErrorMessage());
		}
		if (!partnerPolicyFromDb.getIsActive()) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.APPROVE_REJECT_PARTNER_API_FAILURE);
			throw new PartnerManagerServiceException(ErrorCode.PARTNER_POLICY_NOT_ACTIVE_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_POLICY_NOT_ACTIVE_EXCEPTION.getErrorMessage());
		}
		if (partnerPolicyFromDb.getValidToDatetime().before(Timestamp.valueOf(LocalDateTime.now()))) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.APPROVE_REJECT_PARTNER_API_FAILURE);
			throw new PartnerManagerServiceException(ErrorCode.PARTNER_POLICY_EXPIRED_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_POLICY_EXPIRED_EXCEPTION.getErrorMessage());
		}
		return partnerPolicyFromDb;
	}

	private AuthPolicy validatePolicy(String policyId) {
		Optional<AuthPolicy> authPolicy = authPolicyRepository.findById(policyId);
		if (authPolicy.isEmpty()) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_POLICY_MAPPING_FAILURE);
			throw new PartnerManagerServiceException(ErrorCode.PARTNER_NOT_MAPPED_TO_POLICY_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_NOT_MAPPED_TO_POLICY_EXCEPTION.getErrorMessage());
		}
		if (!authPolicy.get().getIsActive()) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_POLICY_MAPPING_FAILURE);
			throw new PartnerManagerServiceException(ErrorCode.PARTNER_POLICY_NOT_ACTIVE_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_POLICY_NOT_ACTIVE_EXCEPTION.getErrorMessage());
		}
		if (authPolicy.get().getPolicyGroup() == null) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_POLICY_MAPPING_FAILURE);
			throw new PartnerManagerServiceException(ErrorCode.POLICY_GROUP_NOT_EXISTS.getErrorCode(),
					ErrorCode.POLICY_GROUP_NOT_EXISTS.getErrorMessage());
		}
		if (!authPolicy.get().getPolicyGroup().getIsActive()) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_POLICY_MAPPING_FAILURE);
			throw new PartnerManagerServiceException(ErrorCode.POLICY_GROUP_NOT_ACTIVE.getErrorCode(),
					ErrorCode.POLICY_GROUP_NOT_ACTIVE.getErrorMessage());
		}
		if (authPolicy.get().getValidToDate().isBefore(LocalDateTime.now())) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_POLICY_MAPPING_FAILURE);
			throw new PartnerManagerServiceException(ErrorCode.PARTNER_POLICY_EXPIRED_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_POLICY_EXPIRED_EXCEPTION.getErrorMessage());
		}
		return authPolicy.get();
	}

	private MISPLicenseEntity validateAndGetMispLicense(String mispLicenseKey) {
		MISPLicenseEntity mispLicense = misplKeyRepository.findByLicensekey(mispLicenseKey);
		if (mispLicense == null) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_POLICY_MAPPING_FAILURE);
			throw new PartnerManagerServiceException(ErrorCode.MISP_LICENSE_KEY_NOT_EXISTS.getErrorCode(),
					ErrorCode.MISP_LICENSE_KEY_NOT_EXISTS.getErrorMessage());
		}
		if (!mispLicense.getIsActive()) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_POLICY_MAPPING_FAILURE);
			throw new PartnerManagerServiceException(ErrorCode.MISP_IS_BLOCKED.getErrorCode(),
					ErrorCode.MISP_IS_BLOCKED.getErrorMessage());
		}
		if (mispLicense.getValidToDate().isBefore(LocalDateTime.now())) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_POLICY_MAPPING_FAILURE);
			throw new PartnerManagerServiceException(ErrorCode.MISP_LICENSE_KEY_EXPIRED.getErrorCode(),
					ErrorCode.MISP_LICENSE_KEY_EXPIRED.getErrorMessage());
		}
		validateMispProvider(mispLicense.getMispLicenseUniqueKey().getMisp_id());
		return mispLicense;
	}

	private void validateMispProvider(String misp_id) {
		Optional<MISPEntity> mispFromDb = mispRepository.findById(misp_id);
		if (mispFromDb.isEmpty()) {
			validateAndGetPartner(misp_id);
		} else {
			if (!mispFromDb.get().getIsActive()) {
				auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_POLICY_MAPPING_FAILURE);
				throw new PartnerManagerServiceException(ErrorCode.MISP_IS_BLOCKED.getErrorCode(),
						ErrorCode.MISP_IS_BLOCKED.getErrorMessage());
			}
		}
	}

	private Partner validateAndGetPartner(String partnerId) {
		Optional<Partner> partner = partnerRepository.findById(partnerId);
		if (partner.isEmpty()) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_POLICY_MAPPING_FAILURE);
			throw new PartnerManagerServiceException(ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_DOES_NOT_EXIST_EXCEPTION.getErrorMessage());
		}
		if (!partner.get().getIsActive()) {
			auditUtil.setAuditRequestDto(PartnerManageEnum.GET_PARTNER_POLICY_MAPPING_FAILURE);
			throw new PartnerManagerServiceException(ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorMessage());
		}
		return partner.get();
	}

	public String getUser() {
		if (Objects.nonNull(SecurityContextHolder.getContext())
				&& Objects.nonNull(SecurityContextHolder.getContext().getAuthentication())
				&& Objects.nonNull(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
				&& SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof AuthUserDetails) {
			return ((AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
					.getUserId();
		} else {
			return null;
		}
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
		dto.setPartnerStatus(partner.getIsActive() == true ? PartnerConstants.ACTIVE : PartnerConstants.DEACTIVE);
		dto.setApiKey(partnerPolicyFromDb.getPolicyApiKey());
		dto.setApiKeyExpiryDate(partnerPolicyFromDb.getValidToDatetime().toLocalDateTime());
		dto.setApiKeyStatus(partnerPolicyFromDb.getIsActive() == true ? PartnerConstants.ACTIVE : PartnerConstants.DEACTIVE);
		dto.setPolicyId(partnerPolicyFromDb.getPolicyId());
		dto.setPolicyName(authPolicy.get().getName());
		dto.setPolicyExpiryDateTime(authPolicy.get().getValidToDate());
		dto.setPolicyStatus(authPolicy.get().getIsActive() == true ? PartnerConstants.ACTIVE : PartnerConstants.DEACTIVE);
		notificationDtos.add(dto);
		try {
			notificationService.sendNotications(eventType, notificationDtos);
		} catch (Exception e) {
			LOGGER.error("Error occured while sending the apikey notifications.", e.getLocalizedMessage(),
					e.getMessage());
		}
	}
}

