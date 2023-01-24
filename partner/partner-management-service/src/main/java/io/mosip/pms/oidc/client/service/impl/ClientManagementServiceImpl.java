package io.mosip.pms.oidc.client.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.HMACUtils2;
import io.mosip.pms.common.constant.ApiAccessibleExceptionConstant;
import io.mosip.pms.common.constant.CommonConstant;
import io.mosip.pms.common.constant.EventType;
import io.mosip.pms.common.dto.ClientPublishDto;
import io.mosip.pms.common.dto.PartnerDataPublishDto;
import io.mosip.pms.common.dto.PolicyPublishDto;
import io.mosip.pms.common.dto.Type;
import io.mosip.pms.common.entity.AuthPolicy;
import io.mosip.pms.common.entity.ClientDetail;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.entity.PartnerPolicyRequest;
import io.mosip.pms.common.exception.ApiAccessibleException;
import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.repository.AuthPolicyRepository;
import io.mosip.pms.common.repository.ClientDetailRepository;
import io.mosip.pms.common.repository.PartnerPolicyRequestRepository;
import io.mosip.pms.common.repository.PartnerRepository;
import io.mosip.pms.common.util.AuthenticationContextRefUtil;
import io.mosip.pms.common.util.MapperUtils;
import io.mosip.pms.common.util.PMSLogger;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.common.util.UserDetailUtil;
import io.mosip.pms.oidc.client.dto.ClientDetailCreateRequest;
import io.mosip.pms.oidc.client.dto.ClientDetailResponse;
import io.mosip.pms.oidc.client.dto.ClientDetailUpdateRequest;
import io.mosip.pms.oidc.client.dto.CreateClientRequestDto;
import io.mosip.pms.oidc.client.dto.RequestWrapper;
import io.mosip.pms.oidc.client.dto.UpdateClientRequestDto;
import io.mosip.pms.oidc.client.service.ClientManagementService;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.constant.PartnerConstants;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.response.dto.PartnerCertDownloadResponeDto;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.lang.JoseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	ClientDetailRepository clientDetailRepository;

	@Autowired
	AuthPolicyRepository authPolicyRepository;
	
	@Autowired
	PartnerRepository partnerRepository;

	@Autowired
	PartnerPolicyRequestRepository partnerPolicyRequestRepository;

	@Autowired
	RestUtil restUtil;

	@Autowired
	private Environment environment;

	@Autowired
	private WebSubPublisher webSubPublisher;
	
	@Autowired
	private AuthenticationContextRefUtil authenticationContextClassRefUtil;

	@Override
	public ClientDetailResponse createOIDCClient(ClientDetailCreateRequest createRequest) throws Exception {
		String publicKey = getJWKString(createRequest.getPublicKey());
		String clientId = CryptoUtil.encodeToURLSafeBase64(HMACUtils2.generateHash(publicKey.getBytes()));
		Optional<ClientDetail> result = clientDetailRepository.findById(clientId);
		if (result.isPresent()) {
			LOGGER.error("createOIDCClient::Client with name {} already exists", createRequest.getName());
			throw new PartnerServiceException(ErrorCode.DUPLICATE_CLIENT.getErrorCode(),
					ErrorCode.DUPLICATE_CLIENT.getErrorMessage());
		}
		Optional<Partner> partner = partnerRepository.findById(createRequest.getAuthPartnerId());
		if(partner.isEmpty()) {
			LOGGER.error("createOIDCClient::AuthPartner with Id {} doesn't exists", createRequest.getAuthPartnerId());
			throw new PartnerServiceException(ErrorCode.INVALID_PARTNERID.getErrorCode(), String
					.format(ErrorCode.INVALID_PARTNERID.getErrorMessage(), createRequest.getAuthPartnerId()));
		}
		if(!partner.get().getPartnerTypeCode().equalsIgnoreCase(AUTH_PARTNER_TYPE)) {
			LOGGER.error("createOIDCClient::{} cannot create OIDC Client", partner.get().getPartnerTypeCode());
			throw new PartnerServiceException(ErrorCode.INVALID_PARTNER_TYPE.getErrorCode(), String
					.format(ErrorCode.INVALID_PARTNER_TYPE.getErrorMessage(), partner.get().getPartnerTypeCode()));
		}
		Optional<AuthPolicy> policyFromDb = authPolicyRepository.findById(createRequest.getPolicyId());
		if (!policyFromDb.isPresent()) {
			LOGGER.error("createOIDCClient::Policy with Id {} not exists", createRequest.getPolicyId());
			throw new PartnerServiceException(ErrorCode.POLICY_NOT_EXIST.getErrorCode(),
					ErrorCode.POLICY_NOT_EXIST.getErrorMessage());
		}
		AuthPolicy policy = policyFromDb.get();
		if(!policy.getPolicy_type().equals(AUTH_POLICY_TYPE)) {
			LOGGER.error("createOIDCClient::Policy Type Mismatch. {} policy cannot be used to create OIDC Client",policy.getPolicy_type());
			throw new PartnerServiceException(ErrorCode.PARTNER_POLICY_TYPE_MISMATCH.getErrorCode(), String
					.format(ErrorCode.PARTNER_POLICY_TYPE_MISMATCH.getErrorMessage()));
		}
		List<PartnerPolicyRequest> policyMappingReqFromDb = partnerPolicyRequestRepository
				.findByPartnerIdAndPolicyId(createRequest.getAuthPartnerId(), policyFromDb.get().getId());
		if (policyMappingReqFromDb.isEmpty()) {
			LOGGER.error("createOIDCClient::Policy and partner mapping not exists for policy {} and partner {}",
					createRequest.getPolicyId(), createRequest.getAuthPartnerId());
			throw new PartnerServiceException(ErrorCode.PARTNER_POLICY_MAPPING_NOT_EXISTS.getErrorCode(),
					ErrorCode.PARTNER_POLICY_MAPPING_NOT_EXISTS.getErrorMessage());
		}

		if (!policyMappingReqFromDb.get(0).getStatusCode().equalsIgnoreCase(CommonConstant.APPROVED)) {
			LOGGER.error(
					"createOIDCClient::Policy and partner mapping is not approved for policy {} and partner {} and status {}",
					createRequest.getPolicyId(), createRequest.getAuthPartnerId(),
					policyMappingReqFromDb.get(0).getStatusCode().equalsIgnoreCase("approved"));
			throw new PartnerServiceException(ErrorCode.PARTNER_POLICY_NOT_APPROVED.getErrorCode(),
					ErrorCode.PARTNER_POLICY_NOT_APPROVED.getErrorMessage());
		}

		ClientDetail clientDetail = new ClientDetail();
		clientDetail.setPublicKey(publicKey);
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
			throw new PartnerServiceException(ErrorCode.PARTNER_HAVING_NO_CLAIMS.getErrorCode(),
					ErrorCode.PARTNER_HAVING_NO_CLAIMS.getErrorMessage());
		}
		clientDetail.setClaims(String.join(",",claims));
		Set<String> acrValues = authenticationContextClassRefUtil.getAuthFactors(getReqAttributeFromPolicyJson(
				getPolicyObject(policyFromDb.get().getPolicyFileId()), ALLOWED_AUTH_TYPES, AUTH_TYPE, MANDATORY));
		if (acrValues.isEmpty()) {
			LOGGER.error(
					"createOIDCClient::Partner has no User Claims");
			throw new PartnerServiceException(ErrorCode.PARTNER_HAVING_NO_ACRVALUES.getErrorCode(),
					ErrorCode.PARTNER_HAVING_NO_ACRVALUES.getErrorMessage());
		}
		clientDetail.setAcrValues( String.join(",",acrValues));		
		clientDetail.setStatus("ACTIVE");
		clientDetail.setGrantTypes(String.join(",", createRequest.getGrantTypes()));
		clientDetail.setClientAuthMethods(String.join(",", createRequest.getClientAuthMethods()));
		clientDetail.setCreatedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
		clientDetail.setCreatedBy(getLoggedInUserId());
		callIdpService(clientDetail, environment.getProperty("mosip.pms.esignet.oidc-client-create-url"), true);
		publishClientData(policyMappingReqFromDb.get(0).getPartner(), policyFromDb.get(), clientDetail);
		clientDetailRepository.save(clientDetail);
		var response = new ClientDetailResponse();
		response.setClientId(clientDetail.getId());
		response.setStatus(clientDetail.getStatus());		
		return response;
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
	private static Set<String> getReqAttributeFromPolicyJson(JSONObject policyObject, String parentAttribute,
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
	@SuppressWarnings("unchecked")
	private ClientDetailResponse callIdpService(ClientDetail request, String calleeApi, boolean isItForCreate) throws Exception {
		RequestWrapper<CreateClientRequestDto> createRequestwrapper = new RequestWrapper<>();
		createRequestwrapper.setRequestTime(DateUtils.getUTCCurrentDateTimeString(CommonConstant.DATE_FORMAT));
		CreateClientRequestDto dto = new CreateClientRequestDto();
		dto.setClientId(request.getId());
		dto.setClientName(request.getName());
		dto.setRelyingPartyId(request.getRpId());
		dto.setLogoUri(request.getLogoUri());
		dto.setPublicKey(objectMapper.readValue(request.getPublicKey(), Map.class));
		dto.setUserClaims(convertStringToList(request.getClaims()));
		dto.setAuthContextRefs(convertStringToList(request.getAcrValues()));
		dto.setRedirectUris(convertStringToList(request.getRedirectUris()));
		dto.setGrantTypes(convertStringToList(request.getGrantTypes()));
		dto.setClientAuthMethods(convertStringToList(request.getClientAuthMethods()));
		createRequestwrapper.setRequest(dto);
		return makeCreateIDPServiceCall(createRequestwrapper, calleeApi);

	}
	
	private void makeUpdateIDPServiceCall(ClientDetail request, String calleeApi) {
		RequestWrapper<UpdateClientRequestDto> updateRequestwrapper = new RequestWrapper<>();
		updateRequestwrapper.setRequestTime(DateUtils.getUTCCurrentDateTimeString(CommonConstant.DATE_FORMAT));
		UpdateClientRequestDto updateRequest = new UpdateClientRequestDto();
		updateRequest.setClientAuthMethods(convertStringToList(request.getClientAuthMethods()));
		updateRequest.setClientName(request.getName());
		updateRequest.setGrantTypes(convertStringToList(request.getGrantTypes()));
		updateRequest.setLogoUri(request.getLogoUri());
		updateRequest.setRedirectUris(convertStringToList(request.getRedirectUris()));
		updateRequest.setStatus(request.getStatus());
		updateRequest.setUserClaims(convertStringToList(request.getClaims()));
		updateRequest.setAuthContextRefs(convertStringToList(request.getAcrValues()));
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
	
	/**
	 * 
	 * @param request
	 * @param calleeApi
	 * @return
	 */
	private ClientDetailResponse makeCreateIDPServiceCall(Object request, String calleeApi) {
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
		Optional<ClientDetail> result = clientDetailRepository.findById(clientId);
		if (!result.isPresent()) {
			LOGGER.error("updateOIDCClient::Client not exists with id {}", clientId);
			throw new PartnerServiceException(ErrorCode.CLIENT_NOT_EXISTS.getErrorCode(),
					ErrorCode.CLIENT_NOT_EXISTS.getErrorMessage());
		}
		ClientDetail clientDetail = result.get();
		clientDetail.setName(updateRequest.getClientName());
		clientDetail.setLogoUri(updateRequest.getLogoUri());
		clientDetail.setRedirectUris(String.join(",", updateRequest.getRedirectUris()));
		clientDetail.setGrantTypes(String.join(",", updateRequest.getGrantTypes()));
		clientDetail.setClientAuthMethods(String.join(",", updateRequest.getClientAuthMethods()));
		clientDetail.setStatus(updateRequest.getStatus());
		clientDetail.setUpdatedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
		clientDetail.setUpdatedBy(getLoggedInUserId());
		makeUpdateIDPServiceCall(clientDetail, environment.getProperty("mosip.pms.esignet.oidc-client-update-url"));
		clientDetail = clientDetailRepository.save(clientDetail);
		var response = new ClientDetailResponse();
		response.setClientId(clientDetail.getId());
		response.setStatus(clientDetail.getStatus());
		notify(MapperUtils.mapClientDataToPublishDto(clientDetail), EventType.OIDC_CLIENT_UPDATED);
		return response;
	}	
	
	/**
	 * 
	 * @param jwk
	 * @return
	 * @throws Exception
	 */
	private String getJWKString(Map<String, Object> jwk) throws Exception {
		try {
			RsaJsonWebKey jsonWebKey = new RsaJsonWebKey(jwk);
			return jsonWebKey.toJson();
		} catch (JoseException e) {
			throw new Exception();
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
	public io.mosip.pms.oidc.client.dto.ClientDetail getClientDetails(String clientId) {
		Optional<ClientDetail> result = clientDetailRepository.findById(clientId);
		if (!result.isPresent()) {
			LOGGER.error("getClientDetails::Client not exists with id {}", clientId);
			throw new PartnerServiceException(ErrorCode.CLIENT_NOT_EXISTS.getErrorCode(),
					ErrorCode.CLIENT_NOT_EXISTS.getErrorMessage());
		}

		io.mosip.pms.oidc.client.dto.ClientDetail dto = new io.mosip.pms.oidc.client.dto.ClientDetail();
		Optional<AuthPolicy> policyFromDb = authPolicyRepository.findById(result.get().getPolicyId());
		dto.setId(result.get().getId());
		dto.setName(result.get().getName());
		dto.setPolicyId(result.get().getPolicyId());
		dto.setPolicyName(policyFromDb.isEmpty() ? "" : policyFromDb.get().getName());
		dto.setRelyingPartyId(result.get().getRpId());
		dto.setLogoUri(result.get().getLogoUri());
		dto.setStatus(result.get().getStatus());
		dto.setPublicKey(result.get().getPublicKey());
		dto.setClaims(convertStringToList(result.get().getClaims()));
		dto.setAcrValues(convertStringToList(result.get().getAcrValues()));
		dto.setRedirectUris(convertStringToList(result.get().getRedirectUris()));
		dto.setGrantTypes(convertStringToList(result.get().getGrantTypes()));
		dto.setClientAuthMethods(convertStringToList(result.get().getClientAuthMethods()));
		return dto;
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