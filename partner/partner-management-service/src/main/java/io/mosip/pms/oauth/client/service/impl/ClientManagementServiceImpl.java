package io.mosip.pms.oauth.client.service.impl;

import java.security.PublicKey;
import java.util.*;

import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.RSAKey;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.pms.common.dto.*;
import io.mosip.pms.common.entity.*;
import io.mosip.pms.common.entity.ClientDetail;
import io.mosip.pms.common.repository.*;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.oauth.client.dto.*;
import io.mosip.pms.oauth.client.dto.ClientDetailV2;
import io.mosip.pms.oidc.client.contant.ClientServiceAuditEnum;
import io.mosip.pms.partner.util.MultiPartnerUtil;
import io.mosip.pms.partner.util.PartnerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.HMACUtils2;
import io.mosip.pms.common.constant.ApiAccessibleExceptionConstant;
import io.mosip.pms.common.constant.CommonConstant;
import io.mosip.pms.common.constant.EventType;
import io.mosip.pms.common.exception.ApiAccessibleException;
import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.util.AuthenticationContextRefUtil;
import io.mosip.pms.common.util.MapperUtils;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.common.util.UserDetailUtil;
import io.mosip.pms.oauth.client.service.ClientManagementService;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.partner.exception.PartnerServiceException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import jakarta.validation.constraints.NotNull;

@Service
public class ClientManagementServiceImpl implements ClientManagementService {

	private static final Logger LOGGER = PMSLogger.getLogger(ClientManagementServiceImpl.class);

	private static final String ALLOWED_KYC_ATTRIBUTES = "allowedKycAttributes";
	private static final String ATTRIBUTE_NAME = "attributeName";
	private static final String ALLOWED_AUTH_TYPES = "allowedAuthTypes";
	private static final String AUTH_TYPE = "authType";
	private static final String MANDATORY = "mandatory";
	private static final String AUTH_POLICY_TYPE = "Auth";
	private static final String AUTH_PARTNER_TYPE = "Auth_Partner";
	private static final String ERROR_MESSAGE = "errorMessage";
	public static final String ACTIVE = "ACTIVE";
	public static final String INACTIVE = "INACTIVE";
	public static final String BLANK_STRING = "";
	public static final String VERSION = "1.0";
	public static final String NONE_LANG_KEY = "@none";
	public static final String ENG_KEY = "eng";
	public static final Set<String> VALID_USER_INFO_RESPONSE_TYPES = Set.of("JWS", "JWE");
	public static final Set<String> VALID_PURPOSE_TYPES = Set.of("verify", "link", "login");

	@Value("${mosip.pms.api.id.oauth.clients.get}")
	private String getClientsId;

	@Value("${mosip.pms.api.id.oauth.partners.clients.get}")
	private String getPartnersClientsId;

	@Value("${mosip.pms.api.id.oidc.clients.get}")
	private String getPartnersClientsV2Id;

	@Value("${mosip.pms.api.id.create.oidc.client.post}")
	private String postCreateOidcClientId;

    @Value("${mosip.pms.api.id.update.oidc.client.put}")
    private String putUpdateOidcClientId;

	@Value("${mosip.pms.api.id.oidc.client.details.get}")
	private String getOidcClientDetailsId;

	@Value("${mosip.pms.api.id.deactivate.oidc.client.patch}")
	private String patchDeactivateOidcClientId;

	@Value("#{'${mosip.pms.supported.oidc.languages}'.split(',')}")
	private List<String> supportedOidcLanguages;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	ClientDetailRepository clientDetailRepository;

	@Autowired
	ClientSummaryRepository clientSummaryRepository;

	@Autowired
	AuthPolicyRepository authPolicyRepository;

	@Autowired
	PartnerRepository partnerRepository;

	@Autowired
	PartnerServiceRepository partnerServiceRepository;

	@Autowired
	PartnerPolicyRequestRepository partnerPolicyRequestRepository;

	@Autowired
	RestUtil restUtil;

	@Autowired
	AuditUtil auditUtil;

	@Autowired
	private Environment environment;

	@Autowired
	private WebSubPublisher webSubPublisher;
	
	@Autowired
	private AuthenticationContextRefUtil authenticationContextClassRefUtil;

	@Autowired
	PartnerHelper partnerHelper;

	@Override
	public ClientDetailResponse createOIDCClient(ClientDetailCreateRequest createRequest) throws Exception {
		ProcessedClientDetail processedClientDetail = processCreateOIDCClient(createRequest);
		ClientDetail clientDetail = processedClientDetail.getClientDetail();
		callEsignetService(clientDetail, environment.getProperty("mosip.pms.esignet.oidc-client-create-url"),false);
		publishClientData(processedClientDetail.getPartner(), processedClientDetail.getPolicy(), clientDetail);
		clientDetailRepository.save(clientDetail);
		var response = new ClientDetailResponse();
		response.setClientId(clientDetail.getId());
		response.setStatus(clientDetail.getStatus());		
		return response;
	}

	private List<Partner> validateUserId() {
		String userId = getUserId();
		List<Partner> partnerList = partnerServiceRepository.findByUserId(userId);
		return partnerList;
	}

	private boolean validatePartnerIdBelongsToUser(List<Partner> partnerList, String partnerId) {
		boolean isPartnerBelongsToUser = false;
		for (Partner partner : partnerList) {
			if (partner.getId().equals(partnerId)) {
				isPartnerBelongsToUser = true;
				break;
			}
		}
		return isPartnerBelongsToUser;
	}

	@Override
	public ClientDetailResponse createOAuthClient(ClientDetailCreateRequestV2 createRequest) throws Exception {
		ProcessedClientDetail processedClientDetail = processCreateOIDCClient(createRequest);
		ClientDetail clientDetail = processedClientDetail.getClientDetail();
		callEsignetService(clientDetail, environment.getProperty("mosip.pms.esignet.oauth-client-create-url"), true, createRequest.getClientNameLangMap());
		String clientName=getClientNameLanguageMapAsJsonString(
				createRequest.getClientNameLangMap(),
				createRequest.getName()
        );
		clientDetail.setName(clientName);
		publishClientData(processedClientDetail.getPartner(), processedClientDetail.getPolicy(), clientDetail);
		clientDetailRepository.save(clientDetail);
		var response = new ClientDetailResponse();
		response.setClientId(clientDetail.getId());
		response.setStatus(clientDetail.getStatus());		
		return response;
		
	}
	
	public ProcessedClientDetail processCreateOIDCClient(ClientDetailCreateRequest createRequest) throws Exception {
		//get the JWK from request
		Map<String, Object> jwkKeyFromRequest = createRequest.getPublicKey();
		//get String form of JWK
		String jwkKeyString = getJWKString(jwkKeyFromRequest);
		//Generate a new public key using the key's most significant fields from the JWK
		String generatedPublicKey = createPublicKeyFromJWK(jwkKeyFromRequest);
		//Create a Base64-encoded hash of the newly generated public key to check for duplicate keys
		String clientId = CryptoUtil.encodeToURLSafeBase64(HMACUtils2.generateHash(generatedPublicKey.getBytes()));
		Optional<ClientDetail> result = clientDetailRepository.findById(clientId);
		if (result.isPresent()) {
			LOGGER.error("createOIDCClient::Client with name {} already exists", createRequest.getName());
			auditUtil.setAuditRequestDto(ClientServiceAuditEnum.CREATE_CLIENT_FAILURE, createRequest.getName(),
					clientId);
			throw new PartnerServiceException(ErrorCode.DUPLICATE_CLIENT.getErrorCode(),
					ErrorCode.DUPLICATE_CLIENT.getErrorMessage());
		}
		boolean isAdmin = partnerHelper.isPartnerAdmin(authUserDetails().getAuthorities().toString());
		// Skip the below checks if the user is logged in as a partner_admin
		if (!isAdmin) {
			// Validate the logged-in user ID and fetch the list of partners associated to it
			List<Partner> partnerList = validateUserId();
			if (partnerList.isEmpty()) {
				LOGGER.error("sessionId", "idType", "id", "User id does not exist.");
				auditUtil.setAuditRequestDto(ClientServiceAuditEnum.CREATE_CLIENT_FAILURE, createRequest.getName(),
						clientId);
				throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
						ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
			}
			// Check if the partner ID belongs to the user.
			boolean isValidPartner = validatePartnerIdBelongsToUser(partnerList, createRequest.getAuthPartnerId());
			if (!isValidPartner) {
				LOGGER.error("sessionId", "idType", "id", "The given partner ID does not belong to the user.");
				auditUtil.setAuditRequestDto(ClientServiceAuditEnum.CREATE_CLIENT_FAILURE, createRequest.getName(),
						clientId);
				throw new PartnerServiceException(ErrorCode.PARTNER_NOT_BELONGS_TO_THE_USER_CREATE_OIDC.getErrorCode(),
						ErrorCode.PARTNER_NOT_BELONGS_TO_THE_USER_CREATE_OIDC.getErrorMessage());
			}
		}
		Optional<Partner> partner = partnerRepository.findById(createRequest.getAuthPartnerId());
		if(partner.isEmpty()) {
			LOGGER.error("createOIDCClient::AuthPartner with Id {} doesn't exists", createRequest.getAuthPartnerId());
			auditUtil.setAuditRequestDto(ClientServiceAuditEnum.CREATE_CLIENT_FAILURE, createRequest.getName(),
					clientId);
			throw new PartnerServiceException(ErrorCode.INVALID_PARTNERID.getErrorCode(), String
					.format(ErrorCode.INVALID_PARTNERID.getErrorMessage(), createRequest.getAuthPartnerId()));
		}
		if(!partner.get().getPartnerTypeCode().equalsIgnoreCase(AUTH_PARTNER_TYPE)) {
			LOGGER.error("createOIDCClient::{} cannot create OIDC Client", partner.get().getPartnerTypeCode());
			auditUtil.setAuditRequestDto(ClientServiceAuditEnum.CREATE_CLIENT_FAILURE, createRequest.getName(),
					clientId);
			throw new PartnerServiceException(ErrorCode.INVALID_PARTNER_TYPE.getErrorCode(), String
					.format(ErrorCode.INVALID_PARTNER_TYPE.getErrorMessage(), partner.get().getPartnerTypeCode()));
		}
		//check if Partner is Active or not
		if (!partner.get().getIsActive()) {
			LOGGER.error("createOIDCClient::Partner is not Active with id {}", clientId);
			auditUtil.setAuditRequestDto(ClientServiceAuditEnum.CREATE_CLIENT_FAILURE, createRequest.getName(),
					clientId);
			throw new PartnerServiceException(ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorMessage());
		}
		Optional<AuthPolicy> policyFromDb = authPolicyRepository.findById(createRequest.getPolicyId());
		if (!policyFromDb.isPresent()) {
			LOGGER.error("createOIDCClient::Policy with Id {} not exists", createRequest.getPolicyId());
			auditUtil.setAuditRequestDto(ClientServiceAuditEnum.CREATE_CLIENT_FAILURE, createRequest.getName(),
					clientId);
			throw new PartnerServiceException(ErrorCode.POLICY_NOT_EXIST.getErrorCode(),
					ErrorCode.POLICY_NOT_EXIST.getErrorMessage());
		}
		AuthPolicy policy = policyFromDb.get();
		if(!policy.getPolicy_type().equals(AUTH_POLICY_TYPE)) {
			LOGGER.error("createOIDCClient::Policy Type Mismatch. {} policy cannot be used to create OIDC Client",policy.getPolicy_type());
			auditUtil.setAuditRequestDto(ClientServiceAuditEnum.CREATE_CLIENT_FAILURE, createRequest.getName(),
					clientId);
			throw new PartnerServiceException(ErrorCode.PARTNER_POLICY_TYPE_MISMATCH.getErrorCode(), String
					.format(ErrorCode.PARTNER_POLICY_TYPE_MISMATCH.getErrorMessage()));
		}
		List<PartnerPolicyRequest> policyMappingReqFromDb = partnerPolicyRequestRepository
				.findByPartnerIdAndPolicyId(createRequest.getAuthPartnerId(), policyFromDb.get().getId());
		if (policyMappingReqFromDb.isEmpty()) {
			LOGGER.error("createOIDCClient::Policy and partner mapping not exists for policy {} and partner {}",
					createRequest.getPolicyId(), createRequest.getAuthPartnerId());
			auditUtil.setAuditRequestDto(ClientServiceAuditEnum.CREATE_CLIENT_FAILURE, createRequest.getName(),
					clientId);
			throw new PartnerServiceException(ErrorCode.PARTNER_POLICY_MAPPING_NOT_EXISTS.getErrorCode(),
					ErrorCode.PARTNER_POLICY_MAPPING_NOT_EXISTS.getErrorMessage());
		}
		List<PartnerPolicyRequest> approvedMappedPolicy = partnerPolicyRequestRepository
				.findByPartnerIdAndPolicyIdAndStatusCode(partner.get().getUserId(), policy.getId(), PartnerConstants.APPROVED);
		if (approvedMappedPolicy.isEmpty()) {
			LOGGER.error("createOIDCClient::Policy and partner mapping is not approved for policy {} and partner {} and status {}"
					, createRequest.getPolicyId(), createRequest.getAuthPartnerId());
			auditUtil.setAuditRequestDto(ClientServiceAuditEnum.CREATE_CLIENT_FAILURE, createRequest.getName(), clientId);
			throw new PartnerServiceException(ErrorCode.PARTNER_POLICY_NOT_APPROVED.getErrorCode(), ErrorCode.PARTNER_POLICY_NOT_APPROVED.getErrorMessage());
		}

		ClientDetail clientDetail = new ClientDetail();
		clientDetail.setPublicKey(jwkKeyString);
		clientDetail.setId(clientId);
		clientDetail.setName(createRequest.getName());
		clientDetail.setRpId(createRequest.getAuthPartnerId());
		clientDetail.setPolicyId(createRequest.getPolicyId());
		clientDetail.setLogoUri(createRequest.getLogoUri());
		clientDetail.setRedirectUris(String.join(",", createRequest.getRedirectUris()));
		Set<String> claims =  authenticationContextClassRefUtil.getPolicySupportedClaims(getReqAttributeFromPolicyJson(
				getPolicyObject(policyFromDb.get().getPolicyFileId()), ALLOWED_KYC_ATTRIBUTES, ATTRIBUTE_NAME, null));
		if (claims.isEmpty()) {
			LOGGER.error(
					"createOIDCClient::Partner has no User Claims");
			auditUtil.setAuditRequestDto(ClientServiceAuditEnum.CREATE_CLIENT_FAILURE);
			throw new PartnerServiceException(ErrorCode.PARTNER_HAVING_NO_CLAIMS.getErrorCode(),
					ErrorCode.PARTNER_HAVING_NO_CLAIMS.getErrorMessage());
		}
		clientDetail.setClaims(String.join(",",claims));
		Set<String> acrValues = authenticationContextClassRefUtil.getAuthFactors(getReqAttributeFromPolicyJson(
				getPolicyObject(policyFromDb.get().getPolicyFileId()), ALLOWED_AUTH_TYPES, AUTH_TYPE, MANDATORY));
		if (acrValues.isEmpty()) {
			LOGGER.error(
					"createOIDCClient::Partner has no User Claims");
			auditUtil.setAuditRequestDto(ClientServiceAuditEnum.CREATE_CLIENT_FAILURE);
			throw new PartnerServiceException(ErrorCode.PARTNER_HAVING_NO_ACRVALUES.getErrorCode(),
					ErrorCode.PARTNER_HAVING_NO_ACRVALUES.getErrorMessage());
		}
		clientDetail.setAcrValues( String.join(",",acrValues));		
		clientDetail.setStatus(ACTIVE);
		clientDetail.setGrantTypes(String.join(",", createRequest.getGrantTypes()));
		clientDetail.setClientAuthMethods(String.join(",", createRequest.getClientAuthMethods()));
		clientDetail.setCreatedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
		clientDetail.setCreatedBy(getLoggedInUserId());
		ProcessedClientDetail processedClientDetail = new ProcessedClientDetail();
		processedClientDetail.setClientDetail(clientDetail);
		processedClientDetail.setPartner(policyMappingReqFromDb.get(0).getPartner());
		processedClientDetail.setPolicy(policyFromDb.get());
		return processedClientDetail;
	}
	
	

	/**
	 * 
	 * @param policyObject
	 * @param parentAttribute
	 * @param childAttribute
	 * @param childAttribute1
	 * @return
	 * @throws Exception
	 */
	private static Set<String> getReqAttributeFromPolicyJson(@NotNull JSONObject policyObject, String parentAttribute,
															 String childAttribute, String filterAttribute) {
		Set<String> attributes = new HashSet<>();
		JSONArray parentAttributeObject = (JSONArray) policyObject.get(parentAttribute);
		for (int i = 0; i < parentAttributeObject.size(); i++) {
			JSONObject childJsonArray = (JSONObject) parentAttributeObject.get(i);
			String key = (String) childJsonArray.get(childAttribute);
			if (filterAttribute != null && (boolean) childJsonArray.get(filterAttribute)) {
				LOGGER.error("createOIDCClient::Policy is having mandatory auth. So client cannot be created.");
				throw new PartnerServiceException(ErrorCode.POLICY_HAVING_MANDATORY_AUTHS.getErrorCode(),
						ErrorCode.POLICY_HAVING_MANDATORY_AUTHS.getErrorMessage());
			}
		    attributes.add(key.toLowerCase());
		}
		return attributes;
	}

	/**
	 * 
	 * @param request
	 * @return
	 * @throws Exception  
	 */
	@SafeVarargs
	@SuppressWarnings("unchecked")
	private ClientDetailResponse callEsignetService(ClientDetail request, String calleeApi, Boolean isOAuthClient, Map<String,String>... clientNameLangMap) {
		RequestWrapper<CreateClientRequestDto> createRequestwrapper = new RequestWrapper<>();
		createRequestwrapper.setRequestTime(DateUtils.getUTCCurrentDateTimeString(CommonConstant.DATE_FORMAT));

		CreateClientRequestDto dto = buildClientRequestDto(request);

		if (Boolean.TRUE.equals(isOAuthClient) && clientNameLangMap.length > 0) {
			CreateClientRequestDtoV2 dtoV2 = new CreateClientRequestDtoV2(dto, clientNameLangMap[0]);
			createRequestwrapper.setRequest(dtoV2);
		} else {
			createRequestwrapper.setRequest(dto);
		}

		return makeCreateEsignetServiceCall(createRequestwrapper, calleeApi);
	}

	private CreateClientRequestDto buildClientRequestDto(ClientDetail request) {
		CreateClientRequestDto dto = new CreateClientRequestDto();
		setCommonRequestFields(request, dto);
		return dto;
	}

	private void setCommonRequestFields(ClientDetail request, CreateClientRequestDto dto) {
		dto.setClientId(request.getId());
		dto.setClientName(request.getName());
		dto.setRelyingPartyId(request.getRpId());
		dto.setLogoUri(request.getLogoUri());

		try {
			dto.setPublicKey(objectMapper.readValue(request.getPublicKey(), Map.class));
		} catch (JsonProcessingException e) {
			LOGGER.error("Error processing public key JSON: {}", e.getMessage());
		}

		dto.setUserClaims(convertStringToList(request.getClaims()));
		dto.setAuthContextRefs(convertStringToList(request.getAcrValues()));
		dto.setRedirectUris(convertStringToList(request.getRedirectUris()));
		dto.setGrantTypes(convertStringToList(request.getGrantTypes()));
		dto.setClientAuthMethods(convertStringToList(request.getClientAuthMethods()));
	}
	
	
	@SafeVarargs
	private void makeUpdateEsignetServiceCall(ClientDetail request, String calleeApi, Boolean isOAuthClient, Map<String,String>... clientNameLangMap) {
		RequestWrapper<UpdateClientRequestDto> updateRequestwrapper = new RequestWrapper<>();
		updateRequestwrapper.setRequestTime(DateUtils.getUTCCurrentDateTimeString(CommonConstant.DATE_FORMAT));
		UpdateClientRequestDto updateRequest = mapUpdateClientRequestDto(request);
		if(Boolean.TRUE.equals(isOAuthClient) && clientNameLangMap.length>0) {
			UpdateClientRequestDtoV2 updateRequestV2 = new UpdateClientRequestDtoV2(updateRequest, clientNameLangMap[0]);
			updateRequestwrapper.setRequest(updateRequestV2);
		}
		else updateRequestwrapper.setRequest(updateRequest);
		
		List<String> pathsegments = new ArrayList<>();
		pathsegments.add(request.getId());
		try {
			restUtil.putApi(calleeApi, pathsegments, null, null, MediaType.APPLICATION_JSON, updateRequestwrapper, Map.class);
		}catch (Exception e) {
			LOGGER.error("callIdpService::Error from idp service {} ", e.getMessage(), e);
			throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
					ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage() + e.getMessage());
		}
	}
	
	
	private UpdateClientRequestDto mapUpdateClientRequestDto(ClientDetail request) {
		UpdateClientRequestDto updateRequest = new UpdateClientRequestDto();
		setUpdateClientRequest(request, updateRequest);
		return updateRequest;
	}
	
	/**
	 * 
	 * @param request
	 * @param calleeApi
	 * @return
	 */
	private ClientDetailResponse makeCreateEsignetServiceCall(Object request, String calleeApi) {
		ClientDetailResponse response = null;		
		Map<String, Object> idpClientResponse = restUtil.postApi(calleeApi, null, "", "", MediaType.APPLICATION_JSON,
				request, Map.class);
		
		try {
			response = objectMapper.readValue(objectMapper.writeValueAsString(idpClientResponse.get("response")),
					ClientDetailResponse.class);
		} catch (Exception e) {
			LOGGER.error("callIdpService::Error from idp service {} ", e.getMessage(), e);
			throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
					ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage() + e.getMessage());
		}
		if (response == null && idpClientResponse.containsKey(PartnerConstants.ERRORS)) {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> idpServiceErrorList = (List<Map<String, Object>>) idpClientResponse
					.get(PartnerConstants.ERRORS);
			if (!idpServiceErrorList.isEmpty()) {
				LOGGER.error(("IDPServiceResponse:: Idp service response contains errors."));
				throw new ApiAccessibleException((String) idpServiceErrorList.get(0).get(PartnerConstants.ERRORCODE),
						(String) idpServiceErrorList.get(0).get(ERROR_MESSAGE));
			} else {
				LOGGER.error(("IDPServiceResponse:: Idp service response contains errors."));
				throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
						ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage());
			}
		}
		if (response == null) {
			LOGGER.error(("IDPServiceResponse:: Idp service response is null"));
			throw new ApiAccessibleException(ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorCode(),
					ApiAccessibleExceptionConstant.API_NULL_RESPONSE_EXCEPTION.getErrorMessage());
		}
		return response;
	}

	/**
	 * 
	 * @param partnerData
	 * @param policyData\
	 * 
	 * @param clinetData
	 */
	private void publishClientData(Partner partnerData, AuthPolicy policyData, ClientDetail clientData) {
		notify(MapperUtils.mapDataToPublishDto(partnerData, getPartnerCertificate(partnerData.getCertificateAlias())),
				MapperUtils.mapPolicyToPublishDto(policyData, getPolicyObject(policyData.getPolicyFileId())),
				MapperUtils.mapClientDataToPublishDto(clientData), EventType.OIDC_CLIENT_CREATED);
	}

	@SuppressWarnings("unchecked")
	private String getPartnerCertificate(String certificateAlias) {
		Map<String, String> pathsegments = new HashMap<>();
		pathsegments.put("partnerCertId", certificateAlias);
		Map<String, Object> getApiResponse = restUtil
				.getApi(environment.getProperty("pmp.partner.certificaticate.get.rest.uri"), pathsegments, Map.class);
		PartnerCertDownloadResponeDto responseObject = null;
		try {
			responseObject = objectMapper.readValue(objectMapper.writeValueAsString(getApiResponse.get("response")),
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

	@Override
	public ClientDetailResponse updateOIDCClient(String clientId, ClientDetailUpdateRequest updateRequest)
			throws Exception {
		
		ClientDetail clientDetail = processUpdateOIDCClient(clientId,updateRequest);
		makeUpdateEsignetServiceCall(clientDetail, environment.getProperty("mosip.pms.esignet.oidc-client-update-url"),false);
		clientDetail = clientDetailRepository.save(clientDetail);
		var response = new ClientDetailResponse();
		response.setClientId(clientDetail.getId());
		response.setStatus(clientDetail.getStatus());
		notify(MapperUtils.mapClientDataToPublishDto(clientDetail), EventType.OIDC_CLIENT_UPDATED);
		return response;
	}	
	
	
	@Override
	public ClientDetailResponse updateOAuthClient(String clientId, ClientDetailUpdateRequestV2 updateRequest)
			throws Exception {
		
		ClientDetail clientDetail = processUpdateOIDCClient(clientId,updateRequest);
		makeUpdateEsignetServiceCall(clientDetail, environment.getProperty("mosip.pms.esignet.oauth-client-update-url"), true, updateRequest.getClientNameLangMap());
		String clientName=getClientNameLanguageMapAsJsonString(
				updateRequest.getClientNameLangMap(),
				updateRequest.getClientName()
        );
		clientDetail.setName(clientName);
		clientDetail = clientDetailRepository.save(clientDetail);
		var response = new ClientDetailResponse();
		response.setClientId(clientDetail.getId());
		response.setStatus(clientDetail.getStatus());
		notify(MapperUtils.mapClientDataToPublishDto(clientDetail), EventType.OIDC_CLIENT_UPDATED);
		return response;
	}	
	
	
	
	public ClientDetail processUpdateOIDCClient(String clientId, ClientDetailUpdateRequest updateRequest) {
		boolean isAdmin = partnerHelper.isPartnerAdmin(authUserDetails().getAuthorities().toString());
		// validate client details and partner
		ProcessedUpdateClientDetail processedUpdateClientDetail = validateClientDetailsAndPartner(clientId, isAdmin);
		ClientDetail clientDetail = processedUpdateClientDetail.getClientDetail();
		Partner partner = processedUpdateClientDetail.getPartner();

		if ( !isAdmin || (isAdmin && clientDetail.getStatus().equalsIgnoreCase(updateRequest.getStatus()))) {
			//check if Partner is Active or not
			checkPartnerActiveStatus(partner, clientId, ClientServiceAuditEnum.UPDATE_CLIENT_FAILURE);
		}

		if (!clientDetail.getStatus().equalsIgnoreCase(updateRequest.getStatus())) {
			clientDetail.setStatus(updateRequest.getStatus().toUpperCase());
			clientDetail.setUpdatedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
			clientDetail.setUpdatedBy(getLoggedInUserId());
			return clientDetail;
		}
		setCommonUpdateFields(clientDetail, updateRequest);
		return clientDetail;
	}

	private ProcessedUpdateClientDetail validateClientDetailsAndPartner(String clientId, boolean isAdmin) {
		Optional<ClientDetail> result = clientDetailRepository.findById(clientId);
		if (!result.isPresent()) {
			LOGGER.error("updateOIDCClient::Client not exists with id {}", clientId);
			auditUtil.setAuditRequestDto(ClientServiceAuditEnum.UPDATE_CLIENT_FAILURE);
			throw new PartnerServiceException(ErrorCode.CLIENT_NOT_EXISTS.getErrorCode(),
					ErrorCode.CLIENT_NOT_EXISTS.getErrorMessage());
		}
		//Check if OIDC client is already deactivated
		if (result.get().getStatus().equalsIgnoreCase("inactive")) {
			LOGGER.error("updateOIDCClient::Client already deactivated with id {}", clientId);
			auditUtil.setAuditRequestDto(ClientServiceAuditEnum.UPDATE_CLIENT_FAILURE, clientId,
					result.get().getStatus());
			throw new PartnerServiceException(ErrorCode.CLIENT_ALREADY_DEACTIVATED.getErrorCode(),
					ErrorCode.CLIENT_ALREADY_DEACTIVATED.getErrorMessage());
		}
		Optional<Partner> partner = partnerRepository.findById(result.get().getRpId());
		String partnerId = result.get().getRpId();
		if (partner.isEmpty() || partnerId == null) {
			LOGGER.error("updateOIDCClient::Partner not exists with id {}", partnerId);
			auditUtil.setAuditRequestDto(ClientServiceAuditEnum.UPDATE_CLIENT_FAILURE);
			throw new PartnerServiceException(ErrorCode.INVALID_PARTNERID.getErrorCode(),
					String.format(ErrorCode.INVALID_PARTNERID.getErrorMessage(), partnerId));
		}
		// Skip the below checks if the user is logged in as a partner_admin
		if (!isAdmin) {
			// Validate the logged-in user ID and fetch the list of partners associated to it
			List<Partner> partnerList = validateUserId();
			if (partnerList.isEmpty()) {
				LOGGER.error("sessionId", "idType", "id", "User id does not exist.");
				auditUtil.setAuditRequestDto(ClientServiceAuditEnum.UPDATE_CLIENT_FAILURE);
				throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
						ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
			}
			// Check if the partner ID belongs to the user.
			boolean isValidPartner = validatePartnerIdBelongsToUser(partnerList, partnerId);
			if (!isValidPartner) {
				LOGGER.error("sessionId", "idType", "id", "The given partner ID does not belong to the user.");
				auditUtil.setAuditRequestDto(ClientServiceAuditEnum.UPDATE_CLIENT_FAILURE);
				throw new PartnerServiceException(ErrorCode.PARTNER_NOT_BELONGS_TO_THE_USER_UPDATE_OIDC.getErrorCode(),
						ErrorCode.PARTNER_NOT_BELONGS_TO_THE_USER_UPDATE_OIDC.getErrorMessage());
			}
		}
		ProcessedUpdateClientDetail processedUpdateClientDetail = new ProcessedUpdateClientDetail();
		processedUpdateClientDetail.setClientDetail(result.get());
		processedUpdateClientDetail.setPartner(partner.get());
		return processedUpdateClientDetail;
	}

	private void setCommonUpdateFields(ClientDetail clientDetail, ClientDetailUpdateRequest updateRequest) {
		clientDetail.setName(updateRequest.getClientName());
		clientDetail.setLogoUri(updateRequest.getLogoUri());
		clientDetail.setRedirectUris(String.join(",", updateRequest.getRedirectUris()));
		clientDetail.setGrantTypes(String.join(",", updateRequest.getGrantTypes()));
		clientDetail.setClientAuthMethods(String.join(",", updateRequest.getClientAuthMethods()));
		clientDetail.setUpdatedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
		clientDetail.setUpdatedBy(getLoggedInUserId());
	}
	
	/**
	 * 
	 * @param jwk
	 * @return
	 * @throws Exception
	 */
	private String getJWKString(Map<String, Object> jwk) {
		try {
			JWK jsonWebKey = JWK.parse(jwk);
			return objectMapper.writeValueAsString(jsonWebKey.toJSONObject());
		} catch (Exception e) {
			LOGGER.error("createOIDCClient::Failed to process Client Public Key");
			throw new PartnerServiceException(ErrorCode.FAILED_TO_PROCESS_JWK.getErrorCode(),
					ErrorCode.FAILED_TO_PROCESS_JWK.getErrorMessage());
		}
	}

	/**
	 * 
	 * @param policy
	 * @return
	 */
	private JSONObject getPolicyObject(String policy) {
		JSONParser parser = new JSONParser();
		try {
			return ((JSONObject) parser.parse(policy));
		} catch (ParseException e) {
			return (JSONObject) Collections.emptyMap();
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
			ClientPublishDto clientDataToPublish, EventType eventType) {
		Map<String, Object> data = new HashMap<>();
		if (partnerDataToPublish != null) {
			data.put(PartnerConstants.PARTNER_DATA, partnerDataToPublish);
		}
		if (policyDataToPublish != null) {
			data.put(PartnerConstants.POLICY_DATA, policyDataToPublish);
		}
		if (clientDataToPublish != null) {
			data.put(PartnerConstants.CLIENT_DATA, clientDataToPublish);
		}
		notify(data, eventType);
	}
	
	/**
	 * 
	 * @param clientDataToPublish
	 * @param eventType
	 */
	private void notify(ClientPublishDto clientDataToPublish, EventType eventType) {
		Map<String, Object> data = new HashMap<>();
		if (clientDataToPublish != null) {
			data.put(PartnerConstants.CLIENT_DATA, clientDataToPublish);
		}
		notify(data, eventType);
	}

	/**
	 * 
	 * @param data
	 * @param eventType
	 */
	private void notify(Map<String, Object> data, EventType eventType) {
		Type type = new Type();
		type.setName("PartnerManagementServiceImpl");
		type.setNamespace("io.mosip.pmp.partner.manager.service.impl.PartnerManagementServiceImpl");
		webSubPublisher.notify(eventType, data, type);
	}

	@Override
	public io.mosip.pms.oauth.client.dto.ClientDetail getClientDetails(String clientId) {
		ClientDetail client = fetchAndValidateClient(clientId);

		io.mosip.pms.oauth.client.dto.ClientDetail dto =
				new io.mosip.pms.oauth.client.dto.ClientDetail();

		populateCommonClientFields(client, dto);
		// set policy name
		Optional<AuthPolicy> policyFromDb = authPolicyRepository.findById(client.getPolicyId());
		dto.setPolicyName(policyFromDb.isEmpty() ? "" : policyFromDb.get().getName());

		// Name is plain string here
		dto.setName(client.getName());

		return dto;
	}

	private ClientDetail fetchAndValidateClient(String clientId) {
		Optional<ClientDetail> result = clientDetailRepository.findById(clientId);
		if (!result.isPresent()) {
			LOGGER.error("getClientDetails::Client not exists with id {}", clientId);
			throw new PartnerServiceException(ErrorCode.CLIENT_NOT_EXISTS.getErrorCode(),
					ErrorCode.CLIENT_NOT_EXISTS.getErrorMessage());
		}

		ClientDetail client = result.get();
		boolean isAdmin = partnerHelper.isPartnerAdmin(authUserDetails().getAuthorities().toString());
		// Skip the below checks if the user is logged in as a partner_admin
		if (!isAdmin) {
			// Validate the logged-in user ID and fetch the list of partners associated to it
			List<Partner> partnerList = validateUserId();
			if (partnerList.isEmpty()) {
				LOGGER.error("sessionId", "idType", "id", "User id does not exist.");
				auditUtil.setAuditRequestDto(ClientServiceAuditEnum.GET_CLIENT_FAILURE);
				throw new PartnerServiceException(ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
						ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
			}
			// Check if the partner ID belongs to the user.
			boolean isValidPartner = validatePartnerIdBelongsToUser(partnerList, result.get().getRpId());
			if (!isValidPartner) {
				LOGGER.error("sessionId", "idType", "id", "The given partner ID does not belong to the user.");
				auditUtil.setAuditRequestDto(ClientServiceAuditEnum.GET_CLIENT_FAILURE);
				throw new PartnerServiceException(ErrorCode.PARTNER_NOT_BELONGS_TO_THE_USER_GET_OIDC.getErrorCode(),
						ErrorCode.PARTNER_NOT_BELONGS_TO_THE_USER_GET_OIDC.getErrorMessage());
			}
		}

		return client;
	}

	private void populateCommonClientFields(ClientDetail clientDetail, io.mosip.pms.oauth.client.dto.ClientDetail dto) {
		dto.setId(clientDetail.getId());
		dto.setPolicyId(clientDetail.getPolicyId());
		dto.setRelyingPartyId(clientDetail.getRpId());
		dto.setLogoUri(clientDetail.getLogoUri());
		dto.setStatus(clientDetail.getStatus());
		dto.setPublicKey(clientDetail.getPublicKey());
		dto.setClaims(convertStringToList(clientDetail.getClaims()));
		dto.setAcrValues(convertStringToList(clientDetail.getAcrValues()));
		dto.setRedirectUris(convertStringToList(clientDetail.getRedirectUris()));
		dto.setGrantTypes(convertStringToList(clientDetail.getGrantTypes()));
		dto.setClientAuthMethods(convertStringToList(clientDetail.getClientAuthMethods()));
	}
	
    private String getClientNameLanguageMapAsJsonString(Map<String, String> clientNameMap, String clientName) {
        clientNameMap.put(NONE_LANG_KEY, clientName);
        JSONObject clientNameObject = new JSONObject(clientNameMap);
        return clientNameObject.toString();
    }

	@Override
	public ResponseWrapperV2<PageResponseV2Dto<ClientSummaryDto>> getPartnersClients(String sortFieldName, String sortType, Integer pageNo, Integer pageSize, ClientFilterDto filterDto) {
		return getClientsList(sortFieldName, sortType, pageNo, pageSize, filterDto, getPartnersClientsId);
	}

	@Override
	public ResponseWrapperV2<PageResponseV2Dto<ClientSummaryDto>> getPartnersClientsV2(String sortFieldName, String sortType, Integer pageNo, Integer pageSize, ClientFilterDto filterDto) {
		return getClientsList(sortFieldName, sortType, pageNo, pageSize, filterDto, getPartnersClientsV2Id);
	}

	private ResponseWrapperV2<PageResponseV2Dto<ClientSummaryDto>> getClientsList(String sortFieldName, String sortType, Integer pageNo, Integer pageSize, ClientFilterDto filterDto, String responseId) {
		ResponseWrapperV2<PageResponseV2Dto<ClientSummaryDto>> responseWrapper = new ResponseWrapperV2<>();
		try {
			PageResponseV2Dto<ClientSummaryDto> pageResponseV2Dto = new PageResponseV2Dto<>();
			partnerHelper.validateRequestParameters(partnerHelper.oidcClientsAliasToColumnMap, sortFieldName, sortType, pageNo, pageSize);
			boolean isPartnerAdmin = partnerHelper.isPartnerAdmin(authUserDetails().getAuthorities().toString());
			List<String> partnerIdList = null;
			if (!isPartnerAdmin) {
				String userId = getUserId();
				List<Partner> partnerList = partnerServiceRepository.findByUserId(userId);
				if (partnerList.isEmpty()) {
					LOGGER.info("sessionId", "idType", "id", "User id does not exists.");
					throw new PartnerServiceException(io.mosip.pms.partner.constant.ErrorCode.USER_ID_NOT_EXISTS.getErrorCode(),
							io.mosip.pms.partner.constant.ErrorCode.USER_ID_NOT_EXISTS.getErrorMessage());
				}
				partnerIdList = new ArrayList<>();
				for (Partner partner : partnerList) {
					partnerHelper.validatePartnerId(partner, userId);
					partnerHelper.validatePolicyGroupId(partner, userId);
					partnerHelper.validatePolicyGroup(partner);
					partnerIdList.add(partner.getId());
				}
			}

			Pageable pageable = Pageable.unpaged();

			// Pagination
			boolean isPaginationEnabled = (pageNo != null && pageSize != null);
			if (isPaginationEnabled) {
				pageable = PageRequest.of(pageNo, pageSize);
			}

			//Sorting
			if (isPaginationEnabled && Objects.nonNull(sortFieldName) && Objects.nonNull(sortType)) {
				Sort sort = partnerHelper.getSortingRequest(getSortColumn(partnerHelper.oidcClientsAliasToColumnMap, sortFieldName), sortType);
				pageable = PageRequest.of(pageNo, pageSize, sort);
			}
			Page<ClientSummaryEntity> page = clientSummaryRepository.
					getSummaryOfAllPartnerClients(filterDto.getPartnerId(), filterDto.getOrgName(),
							filterDto.getPolicyGroupName(), filterDto.getPolicyName(),
							filterDto.getClientName(), filterDto.getStatus(), partnerIdList, isPartnerAdmin, pageable);
			if (Objects.nonNull(page) && !page.getContent().isEmpty()) {
				List<ClientSummaryDto> clientSummaryDtoList = MapperUtils.mapAll(page.getContent(), ClientSummaryDto.class);
				pageResponseV2Dto.setPageNo(page.getNumber());
				pageResponseV2Dto.setPageSize(page.getSize());
				pageResponseV2Dto.setTotalResults(page.getTotalElements());
				pageResponseV2Dto.setData(clientSummaryDtoList);
			}
			responseWrapper.setResponse(pageResponseV2Dto);

		} catch (PartnerServiceException ex) {
			LOGGER.info("sessionId", "idType", "id", "In getClientsList method of ClientManagementServiceImpl - " + ex.getMessage());
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (Exception ex) {
			LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
			LOGGER.error("sessionId", "idType", "id",
					"In getClientsList method of ClientManagementServiceImpl - " + ex.getMessage());
			String errorCode = ErrorCode.OIDC_CLIENTS_FETCH_ERROR.getErrorCode();
			String errorMessage = ErrorCode.OIDC_CLIENTS_FETCH_ERROR.getErrorMessage();
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
		}
		responseWrapper.setId(responseId);
		responseWrapper.setVersion(VERSION);
		return responseWrapper;
	}

	public String getSortColumn(Map<String, String> aliasToColumnMap, String alias) {
		return aliasToColumnMap.getOrDefault(alias, alias); // Return alias if no match found
	}

	private String getUserId() {
		String userId = authUserDetails().getUserId();
		return userId;
	}

	private AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	@Override
	public ResponseWrapperV2<ClientDetailResponse> createOIDCClientV2(ClientDetailCreateRequestV3 request) {
		ResponseWrapperV2<ClientDetailResponse> responseWrapper = new ResponseWrapperV2<>();
		try {
			ProcessedClientDetail processedClientDetail = processCreateOIDCClientV2(request);
			validateLanguageKeys(request.getClientNameLangMap(), "clientNameLangMap",  false);
			ClientDetail clientDetail = processedClientDetail.getClientDetail();
			callEsignetServiceV2(clientDetail, environment.getProperty("mosip.pms.esignet.oidc.client.create.url"), true, request.getClientNameLangMap());
			String clientName=getClientNameLanguageMapAsJsonString(
					request.getClientNameLangMap(),
					request.getName()
			);
			clientDetail.setName(clientName);
			publishClientData(processedClientDetail.getPartner(), processedClientDetail.getPolicy(), clientDetail);
			clientDetailRepository.save(clientDetail);

			ClientDetailResponse response = new ClientDetailResponse();
			response.setClientId(clientDetail.getId());
			response.setStatus(clientDetail.getStatus());
			responseWrapper.setResponse(response);

		} catch (ApiAccessibleException ex) {
			LOGGER.info("sessionId", "idType", "id", "In createOIDCClientV2 method of ClientManagementServiceImpl - " + ex.getMessage());
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (PartnerServiceException ex) {
			LOGGER.info("sessionId", "idType", "id", "In createOIDCClientV2 method of ClientManagementServiceImpl - " + ex.getMessage());
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (Exception ex) {
			LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
			LOGGER.error("sessionId", "idType", "id",
					"In createOIDCClientV2 method of ClientManagementServiceImpl - " + ex.getMessage());
			String errorCode = ErrorCode.CREATE_OIDC_CLIENT_ERROR.getErrorCode();
			String errorMessage = ErrorCode.CREATE_OIDC_CLIENT_ERROR.getErrorMessage();
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
		}
		responseWrapper.setId(postCreateOidcClientId);
		responseWrapper.setVersion(VERSION);
		return responseWrapper;
	}

	private ProcessedClientDetail processCreateOIDCClientV2(ClientDetailCreateRequestV3 createRequest) throws Exception {
		ProcessedClientDetail processedClientDetail = processCreateOIDCClient(createRequest);
		if (createRequest.getAdditionalConfig() != null) {
			ClientDetail clientDetail = processedClientDetail.getClientDetail();

			// validate additional config fields 
			validateAdditionalConfigFields(createRequest.getAdditionalConfig(), clientDetail.getId(), createRequest.getName());

			// convert additional config as String and set to client detail
			ObjectMapper mapper = new ObjectMapper();
			String additionalConfig = mapper.writeValueAsString(createRequest.getAdditionalConfig());
			clientDetail.setAdditionalConfig(additionalConfig);

			processedClientDetail.setClientDetail(clientDetail);
		}
		return processedClientDetail;
	}

	private void validateAdditionalConfigFields(AdditionalConfigDto additionalConfigDto, String clientId, String clientName) {
		if(additionalConfigDto.getUserinfoResponseType() != null &&
				!VALID_USER_INFO_RESPONSE_TYPES.contains(additionalConfigDto.getUserinfoResponseType())) {
				LOGGER.error("validateAdditionalConfigFields::Invalid userinfo_response_type {}",
						additionalConfigDto.getUserinfoResponseType());
				auditUtil.setAuditRequestDto(ClientServiceAuditEnum.CREATE_CLIENT_FAILURE, clientName,
						clientId);
				throw new PartnerServiceException(ErrorCode.INVALID_USERINFO_RESPONSE_TYPE.getErrorCode(), String
						.format(ErrorCode.INVALID_USERINFO_RESPONSE_TYPE.getErrorMessage(),
								additionalConfigDto.getUserinfoResponseType()));
		}
		if(additionalConfigDto.getConsentExpireInMins() != null &&
				additionalConfigDto.getConsentExpireInMins() < 10) {
				LOGGER.error("validateAdditionalConfigFields::Invalid consent_expire_in_mins {}",
						additionalConfigDto.getConsentExpireInMins());
				auditUtil.setAuditRequestDto(ClientServiceAuditEnum.CREATE_CLIENT_FAILURE, clientName,
						clientId);
				throw new PartnerServiceException(ErrorCode.INVALID_CONSENT_EXPIRE_TIME.getErrorCode(), String
						.format(ErrorCode.INVALID_CONSENT_EXPIRE_TIME.getErrorMessage(),
								additionalConfigDto.getConsentExpireInMins()));
		}
		// PURPOSE VALIDATION
		Map<String, Object> purpose = additionalConfigDto.getPurpose();
		if (purpose != null && !purpose.isEmpty()) {

			// Extract individual fields
			String type = purpose.get("type") != null ? purpose.get("type").toString() : null;
			Map<String, Object> title = null;
			Map<String, Object> subtitle = null;

			Object titleObj = purpose.get("title");
			if (titleObj instanceof Map) {
				title = (Map<String, Object>) titleObj;
			} else if (titleObj != null) {
				throw new PartnerServiceException(
						ErrorCode.INVALID_PURPOSE_TITLE_OR_SUBTITLE.getErrorCode(),
						"purpose.title must be a map");
			}
			Object subtitleObj = purpose.get("subTitle");
			if (subtitleObj instanceof Map) {
				subtitle = (Map<String, Object>) subtitleObj;
			} else if (subtitleObj != null) {
				throw new PartnerServiceException(
						ErrorCode.INVALID_PURPOSE_TITLE_OR_SUBTITLE.getErrorCode(),
						"purpose.subTitle must be a map");
			}

			// 1. purpose.type only allow login / link / verify (case insensitive)
			if (type != null) {
				if (!VALID_PURPOSE_TYPES.contains(type.toLowerCase())) {
					throw new PartnerServiceException(
							ErrorCode.INVALID_PURPOSE_TYPE.getErrorCode(),
							String.format(ErrorCode.INVALID_PURPOSE_TYPE.getErrorMessage(), type)
					);
				}
			}

			// 2. title/subtitle allowed ONLY if type is not null
			if (type == null && ((title != null && !title.isEmpty()) ||
					(subtitle != null && !subtitle.isEmpty()))) {

				throw new PartnerServiceException(
						ErrorCode.INVALID_PURPOSE_TITLE_OR_SUBTITLE.getErrorCode(),
						ErrorCode.INVALID_PURPOSE_TITLE_OR_SUBTITLE.getErrorMessage()
				);
			}

			// 3. Validate title keys (@none mandatory)
			validateLanguageKeys(title, "purpose.title", true);

			// 4. Validate subtitle keys (@none mandatory)
			validateLanguageKeys(subtitle, "purpose.subTitle", true);
		}
	}

	private void validateLanguageKeys(Map<String, ?> langMap, String fieldName, boolean isNoneMandatory) {
		if (langMap == null || langMap.isEmpty()) {
			return;
		}

		// If @none is allowed, add it to the allowed keys
		Set<String> validKeys = new HashSet<>(supportedOidcLanguages);
		if (isNoneMandatory) {
			validKeys.add(NONE_LANG_KEY);
		}

		// Validate invalid keys
		for (String key : langMap.keySet()) {
			if (!validKeys.contains(key)) {
				throw new PartnerServiceException(
						ErrorCode.INVALID_LANGUAGE_KEY.getErrorCode(),
						String.format(
								ErrorCode.INVALID_LANGUAGE_KEY.getErrorMessage(), fieldName, key
						)
				);
			}
		}

		// Validate mandatory @none
		if (isNoneMandatory && !langMap.containsKey(NONE_LANG_KEY)) {
			throw new PartnerServiceException(
					ErrorCode.MISSING_MANDATORY_LANGUAGE_KEY.getErrorCode(),
					String.format(
							ErrorCode.MISSING_MANDATORY_LANGUAGE_KEY.getErrorMessage(), fieldName
					)
			);
		}
	}


	@SafeVarargs
	@SuppressWarnings("unchecked")
	private ClientDetailResponse callEsignetServiceV2(ClientDetail request, String calleeApi, Boolean isOAuthClient, Map<String,String>... clientNameLangMap) throws JsonProcessingException {
		RequestWrapper<CreateClientRequestDtoV3> createRequestwrapper = new RequestWrapper<>();
		createRequestwrapper.setRequestTime(DateUtils.getUTCCurrentDateTimeString(CommonConstant.DATE_FORMAT));

		CreateClientRequestDtoV3 dto = new CreateClientRequestDtoV3();
		setCommonRequestFields(request, dto);

		if (Boolean.TRUE.equals(isOAuthClient)) {
			if (clientNameLangMap.length > 0) {
				dto.setClientNameLangMap(clientNameLangMap[0]);
			}
			if (Objects.nonNull(request.getAdditionalConfig())) {
				dto.setAdditionalConfig(objectMapper.readValue(request.getAdditionalConfig(), Map.class));
			}
		}
		createRequestwrapper.setRequest(dto);

		return makeCreateEsignetServiceCall(createRequestwrapper, calleeApi);
	}

    @Override
    public ResponseWrapperV2<ClientDetailResponse> updateOIDCClientV2(String clientId, ClientDetailUpdateRequestV3 updateRequest) {
        ResponseWrapperV2<ClientDetailResponse> responseWrapper = new ResponseWrapperV2<>();
        try {
            ClientDetail clientDetail = processUpdateOIDCClientV2(clientId,updateRequest);
			validateLanguageKeys(updateRequest.getClientNameLangMap(), "clientNameLangMap",  false);
            makeUpdateEsignetServiceCallV2(clientDetail, environment.getProperty("mosip.pms.esignet.oidc.client.update.url"), updateRequest.getClientNameLangMap());
            String clientName=getClientNameLanguageMapAsJsonString(
                    updateRequest.getClientNameLangMap(),
                    updateRequest.getClientName()
            );
            clientDetail.setName(clientName);
            clientDetail = clientDetailRepository.save(clientDetail);

			responseWrapper.setResponse(getResponseDto(clientDetail));

        } catch (ApiAccessibleException ex) {
            LOGGER.info("sessionId", "idType", "id", "In updateOIDCClientV2 method of ClientManagementServiceImpl - " + ex.getMessage());
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
        } catch (PartnerServiceException ex) {
            LOGGER.info("sessionId", "idType", "id", "In updateOIDCClientV2 method of ClientManagementServiceImpl - " + ex.getMessage());
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
        } catch (Exception ex) {
            LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
            LOGGER.error("sessionId", "idType", "id",
                    "In updateOIDCClientV2 method of ClientManagementServiceImpl - " + ex.getMessage());
            String errorCode = ErrorCode.UPDATE_OIDC_CLIENT_ERROR.getErrorCode();
            String errorMessage = ErrorCode.UPDATE_OIDC_CLIENT_ERROR.getErrorMessage();
            responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
        }
        responseWrapper.setId(putUpdateOidcClientId);
        responseWrapper.setVersion(VERSION);
        return responseWrapper;
    }

    public ClientDetail processUpdateOIDCClientV2(String clientId, ClientDetailUpdateRequestV3 updateRequest) throws Exception {
		// validate client details and partner
        ProcessedUpdateClientDetail processedUpdateClientDetail = validateClientDetailsAndPartner(clientId, false);
		ClientDetail clientDetail = processedUpdateClientDetail.getClientDetail();
		Partner partner = processedUpdateClientDetail.getPartner();

        //check if Partner is Active or not
        if (!partner.getIsActive()) {
            checkPartnerActiveStatus(partner, clientId, ClientServiceAuditEnum.UPDATE_CLIENT_FAILURE);
        }
        setCommonUpdateFields(clientDetail, updateRequest);
        if (updateRequest.getAdditionalConfig() != null) {
            // validate additional config fields
            validateAdditionalConfigFields(updateRequest.getAdditionalConfig(), clientDetail.getId(), updateRequest.getClientName());

            // convert additional config as String and set to client detail
            ObjectMapper mapper = new ObjectMapper();
            String additionalConfig = mapper.writeValueAsString(updateRequest.getAdditionalConfig());
            clientDetail.setAdditionalConfig(additionalConfig);
        }
        return clientDetail;
    }

    @SafeVarargs
    private void makeUpdateEsignetServiceCallV2(ClientDetail request, String calleeApi, Map<String,String>... clientNameLangMap) throws Exception {
        RequestWrapper<UpdateClientRequestDtoV3> updateRequestwrapper = new RequestWrapper<>();
        updateRequestwrapper.setRequestTime(DateUtils.getUTCCurrentDateTimeString(CommonConstant.DATE_FORMAT));

        UpdateClientRequestDtoV3 updateRequest = new UpdateClientRequestDtoV3();
		setUpdateClientRequest(request, updateRequest);

		if (clientNameLangMap.length > 0) {
			updateRequest.setClientNameLangMap(clientNameLangMap[0]);
		}
		if (Objects.nonNull(request.getAdditionalConfig())) {
			updateRequest.setAdditionalConfig(objectMapper.readValue(request.getAdditionalConfig(), Map.class));
		}
        updateRequestwrapper.setRequest(updateRequest);

        List<String> pathsegments = new ArrayList<>();
        pathsegments.add(request.getId());
        try {
            restUtil.putApi(calleeApi, pathsegments, null, null, MediaType.APPLICATION_JSON, updateRequestwrapper, Map.class);
        }catch (Exception e) {
            LOGGER.error("callIdpService::Error from idp service {} ", e.getMessage(), e);
            throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
                    ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage() + e.getMessage());
        }
    }

	private void setUpdateClientRequest(ClientDetail clientDetail, UpdateClientRequestDto updateRequest) {
		updateRequest.setClientAuthMethods(convertStringToList(clientDetail.getClientAuthMethods()));
		updateRequest.setClientName(clientDetail.getName());
		updateRequest.setLogoUri(clientDetail.getLogoUri());
		updateRequest.setRedirectUris(convertStringToList(clientDetail.getRedirectUris()));
		updateRequest.setGrantTypes(convertStringToList(clientDetail.getGrantTypes()));
		updateRequest.setStatus(clientDetail.getStatus());
		updateRequest.setUserClaims(convertStringToList(clientDetail.getClaims()));
		updateRequest.setAuthContextRefs(convertStringToList(clientDetail.getAcrValues()));
	}

	@Override
	public ResponseWrapperV2<ClientDetailV2> getOIDCClientV2(String clientId) {
		ResponseWrapperV2<ClientDetailV2> responseWrapper = new ResponseWrapperV2<>();
		try {
			if (Objects.isNull(clientId) || clientId.isEmpty()) {
				throw new PartnerServiceException(ErrorCode.INVALID_CLIENT_ID.getErrorCode(),
						ErrorCode.INVALID_CLIENT_ID.getErrorMessage());
			}
			ClientDetail client = fetchAndValidateClient(clientId);

			ClientDetailV2 dto = new ClientDetailV2();
			populateCommonClientFields(client, dto);
			dto.setCreatedDateTime(client.getCreatedDateTime());
			// set policy name and description
			Optional<AuthPolicy> policyFromDb = authPolicyRepository.findById(client.getPolicyId());
			if (policyFromDb.isPresent()) {
				dto.setPolicyName(policyFromDb.get().getName());
				dto.setPolicyDescription(policyFromDb.get().getDescr());

				// set policy group name and description
				PolicyGroup policyGroup = policyFromDb.get().getPolicyGroup();
				if (policyGroup != null) {
					dto.setPolicyGroupName(policyGroup.getName());
					dto.setPolicyGroupDescription(policyGroup.getDesc());
				}
			}

			// set client name and client name lang map
			ClientNameAndLangMap clientNameAndLangMap = extractClientNameAndLangMap(client.getName());
			String name = clientNameAndLangMap.getName();
			Map<String, String> langMap = clientNameAndLangMap.getLangMap();
			dto.setName(name);
			dto.setClientNameLangMap(langMap);

			// set additional config
			if (client.getAdditionalConfig() != null) {
				dto.setAdditionalConfig(
						objectMapper.readValue(client.getAdditionalConfig(), AdditionalConfigDto.class)
				);
			}
			responseWrapper.setResponse(dto);
		} catch (PartnerServiceException ex) {
			LOGGER.info("sessionId", "idType", "id", "In getOIDCClientV2 method of ClientManagementServiceImpl - " + ex.getMessage());
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (Exception ex) {
			LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
			LOGGER.error("sessionId", "idType", "id",
					"In getOIDCClientV2 method of ClientManagementServiceImpl - " + ex.getMessage());
			String errorCode = ErrorCode.GET_OIDC_CLIENT_ERROR.getErrorCode();
			String errorMessage = ErrorCode.GET_OIDC_CLIENT_ERROR.getErrorMessage();
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
		}
		responseWrapper.setId(getOidcClientDetailsId);
		responseWrapper.setVersion(VERSION);
		return responseWrapper;
	}

	@Override
	public ResponseWrapperV2<ClientDetailResponse> deactivateOIDCClient(String clientId, DeactivateOidcClientRequestDto requestDto) {
		ResponseWrapperV2<ClientDetailResponse> responseWrapper = new ResponseWrapperV2<>();
		try {
			if (Objects.isNull(clientId) || clientId.isEmpty()) {
				LOGGER.error("updateOIDCClient::Invalid client id {}", clientId);
				auditUtil.setAuditRequestDto(ClientServiceAuditEnum.UPDATE_CLIENT_FAILURE);
				throw new PartnerServiceException(ErrorCode.INVALID_CLIENT_ID.getErrorCode(),
						ErrorCode.INVALID_CLIENT_ID.getErrorMessage());
			}
			String status = requestDto.getStatus();
			if (Objects.isNull(status) || !status.equalsIgnoreCase(INACTIVE)) {
				LOGGER.info(status + " : is Invalid Input Parameter, it should be (INACTIVE)");
				auditUtil.setAuditRequestDto(ClientServiceAuditEnum.UPDATE_CLIENT_FAILURE);
				throw new PartnerServiceException(ErrorCode.INVALID_STATUS_CODE.getErrorCode(),
						ErrorCode.INVALID_STATUS_CODE.getErrorMessage());
			}
			// validate client and partner details
			boolean isPartnerAdmin = partnerHelper.isPartnerAdmin(authUserDetails().getAuthorities().toString());
			ProcessedUpdateClientDetail processedUpdateClientDetail = validateClientDetailsAndPartner(clientId, isPartnerAdmin);
			ClientDetail clientDetail = processedUpdateClientDetail.getClientDetail();
			Partner partner = processedUpdateClientDetail.getPartner();

			//check if Partner is Active or not
			if (!isPartnerAdmin) {
				checkPartnerActiveStatus(partner, clientId, ClientServiceAuditEnum.UPDATE_CLIENT_FAILURE);
			}

			// populate client name and client name lang map
			ClientNameAndLangMap clientNameAndLangMap = extractClientNameAndLangMap(clientDetail.getName());
			String name = clientNameAndLangMap.getName();
			Map<String, String> langMap = clientNameAndLangMap.getLangMap();
			clientDetail.setName(name);
			clientDetail.setStatus(INACTIVE);

			makeUpdateEsignetServiceCallV2(clientDetail, environment.getProperty("mosip.pms.esignet.oidc.client.update.url"), langMap);
			String clientName=getClientNameLanguageMapAsJsonString(langMap, name);
			clientDetail.setName(clientName);
			clientDetail.setStatus(INACTIVE);
			clientDetail.setUpdatedBy(getLoggedInUserId());
			clientDetail.setUpdatedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
			clientDetail = clientDetailRepository.save(clientDetail);

			responseWrapper.setResponse(getResponseDto(clientDetail));

		} catch (PartnerServiceException ex) {
			LOGGER.info("sessionId", "idType", "id", "In deactivateOIDCClient method of ClientManagementServiceImpl - " + ex.getMessage());
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(ex.getErrorCode(), ex.getErrorText()));
		} catch (Exception ex) {
			LOGGER.debug("sessionId", "idType", "id", ex.getStackTrace());
			LOGGER.error("sessionId", "idType", "id",
					"In deactivateOIDCClient method of ClientManagementServiceImpl - " + ex.getMessage());
			String errorCode = ErrorCode.DEACTIVATE_OIDC_CLIENT_ERROR.getErrorCode();
			String errorMessage = ErrorCode.DEACTIVATE_OIDC_CLIENT_ERROR.getErrorMessage();
			responseWrapper.setErrors(MultiPartnerUtil.setErrorResponse(errorCode, errorMessage));
		}
		responseWrapper.setId(patchDeactivateOidcClientId);
		responseWrapper.setVersion(VERSION);
		return responseWrapper;
	}

	private ClientDetailResponse getResponseDto(ClientDetail clientDetail) {
		notify(MapperUtils.mapClientDataToPublishDto(clientDetail), EventType.OIDC_CLIENT_UPDATED);
		ClientDetailResponse response = new ClientDetailResponse();
		response.setClientId(clientDetail.getId());
		response.setStatus(clientDetail.getStatus());
		return response;
	}

	private void checkPartnerActiveStatus(Partner partner, String clientId, ClientServiceAuditEnum auditEnum) {
		//check if Partner is Active or not
		if (!partner.getIsActive()) {
			LOGGER.error("Partner is not Active with id {}", clientId);
			auditUtil.setAuditRequestDto(auditEnum);
			throw new PartnerServiceException(ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorCode(),
					ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorMessage());
		}
	}

	private ClientNameAndLangMap extractClientNameAndLangMap(String clientNameStr) {
		String nameValue;
		Map<String, String> clientNameLangMap = new HashMap<>();

		try {
			JSONObject json = (JSONObject) new JSONParser().parse(clientNameStr);

			String noneValue = (String) json.get("@none");
			if (noneValue != null) {
				nameValue = noneValue;

				// Add other language keys except @none
				for (Object key : json.keySet()) {
					String keyStr = (String) key;
					if (!"@none".equals(keyStr)) {
						clientNameLangMap.put(keyStr, (String) json.get(keyStr));
					}
				}

			} else {
				nameValue = clientNameStr;
			}

		} catch (Exception ex) {
			// Not JSON → fallback
			nameValue = clientNameStr;
		}

		return new ClientNameAndLangMap(nameValue, clientNameLangMap);
	}

	/**
	 * Creates a public key from the provided JWK (JSON Web Key).

	 * Supported JWK types:
	 * - RSA: Represented by "RSA" key type.
	 * - EC: Represented by "EC" key type (Elliptic Curve).
	 * - OKP: Represented by "OKP" key type (Octet Key Pair, typically used for EdDSA).
	 *
	 * @param jwk A map representing the JSON Web Key.
	 * @return A Base64 encoded string representation of the public key.
	 * @throws Exception If the JWK is invalid or an unsupported key type is provided.
	 */
	public String createPublicKeyFromJWK(Map<String, Object> jwk) throws Exception {
		// Parse the JWK
		JWK parsedJwk = JWK.parse(jwk);
		byte[] publicKeyBytes;

		// Determine the key type and create the corresponding public key
		switch (parsedJwk.getKeyType().getValue()) {
			case "RSA":
				publicKeyBytes = createRSAPublicKey((RSAKey) parsedJwk).getEncoded();
				break;

			case "EC":
				publicKeyBytes = createECCPublicKey((ECKey) parsedJwk).getEncoded();
				break;

			case "OKP":
				publicKeyBytes = createEdDSAPublicKey((OctetKeyPair) parsedJwk);
				break;

			default:
				// Throw an exception if the key type is unsupported
				throw new UnsupportedOperationException("Unsupported key type: " + parsedJwk.getKeyType());
		}
		// Return the public key as a Base64 encoded string
		return Base64.getEncoder().encodeToString(publicKeyBytes);
	}

	// Method to create an RSA PublicKey from an RSAKey JWK
	private PublicKey createRSAPublicKey(RSAKey rsaJwk) throws Exception {
		return rsaJwk.toPublicKey();
	}

	// Method to create an EC PublicKey from an ECKey JWK
	private PublicKey createECCPublicKey(ECKey ecJwk) throws Exception {
		return ecJwk.toPublicKey();
	}

	/*
	 * Note: The Nimbus library does not provide built-in support for creating
	 * PublicKey instances from "OKP" key type JWKs (EdDSA). As a workaround,
	 * the public key bytes are directly extracted from the JWK by retrieving
	 * the x-coordinate and decoding it from Base64 URL format.
	 */
	// Method to create an EdDSA PublicKey from an OctetKeyPair JWK
	public byte[] createEdDSAPublicKey(OctetKeyPair octetJwk) throws Exception {
		// Retrieve the x-coordinate from the EdDSA key and decode it from Base64 URL
		String xValue = octetJwk.getX().toString();
		return Base64.getUrlDecoder().decode(xValue);
	}

	/**
	 * 
	 * @param commaSeparatedString
	 * @return
	 */
	private List<String> convertStringToList(String commaSeparatedString){
		return Arrays.asList(commaSeparatedString.split(","));
	}
	
	/**
	 * 
	 * @return
	 */
	private String getLoggedInUserId() {
		return UserDetailUtil.getLoggedInUserId();
	}
}
