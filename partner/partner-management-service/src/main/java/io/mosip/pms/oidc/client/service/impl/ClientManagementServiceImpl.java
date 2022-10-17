package io.mosip.pms.oidc.client.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.pms.common.constant.ApiAccessibleExceptionConstant;
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

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	ClientDetailRepository clientDetailRepository;

	@Autowired
	AuthPolicyRepository authPolicyRepository;

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
		
		Optional<ClientDetail> result = clientDetailRepository.findByName(createRequest.getName());
		if (result.isPresent()) {
			LOGGER.error("createOIDCClient::Clinet with name {} already exists", createRequest.getName());
			throw new PartnerServiceException(ErrorCode.DUPLICATE_CLIENT_NAME.getErrorCode(),
					ErrorCode.DUPLICATE_CLIENT_NAME.getErrorMessage());
		}
		AuthPolicy policyFromDb = authPolicyRepository.findByName(createRequest.getPolicyName());
		if (policyFromDb == null) {
			LOGGER.error("createOIDCClient::Policy with name {} not exists", createRequest.getPolicyName());
			throw new PartnerServiceException(ErrorCode.POLICY_NOT_EXIST.getErrorCode(),
					ErrorCode.POLICY_NOT_EXIST.getErrorMessage());
		}

		List<PartnerPolicyRequest> policyMappingReqFromDb = partnerPolicyRequestRepository
				.findByPartnerIdAndPolicyId(createRequest.getRelayingPartyId(), policyFromDb.getId());
		if (policyMappingReqFromDb.isEmpty()) {
			LOGGER.error("createOIDCClient::Policy and partner mapping not exists for policy {} and partner {}",
					createRequest.getPolicyName(), createRequest.getRelayingPartyId());
			throw new PartnerServiceException(ErrorCode.PARTNER_POLICY_MAPPING_NOT_EXISTS.getErrorCode(),
					ErrorCode.PARTNER_POLICY_MAPPING_NOT_EXISTS.getErrorMessage());
		}

		if (!policyMappingReqFromDb.get(0).getStatusCode().equalsIgnoreCase("approved")) {
			LOGGER.error(
					"createOIDCClient::Policy and partner mapping is not approved for policy {} and partner {} and status {}",
					createRequest.getPolicyName(), createRequest.getRelayingPartyId(),
					policyMappingReqFromDb.get(0).getStatusCode().equalsIgnoreCase("approved"));
			throw new PartnerServiceException(ErrorCode.PARTNER_POLICY_NOT_APPROVED.getErrorCode(),
					ErrorCode.PARTNER_POLICY_NOT_APPROVED.getErrorMessage());
		}

		ClientDetail clientDetail = new ClientDetail();
		clientDetail.setPublicKey(getJWKString(createRequest.getPublicKey()));
		clientDetail.setId(UUID.randomUUID().toString());
		clientDetail.setName(createRequest.getName());
		clientDetail.setRpId(createRequest.getRelayingPartyId());
		clientDetail.setPolicyId(policyFromDb.getId());
		clientDetail.setLogoUri(createRequest.getLogoUri());
		clientDetail.setRedirectUris(String.join(",", createRequest.getRedirectUris()));
		clientDetail.setClaims(String.join(",", authenticationContextClassRefUtil.getPolicySupportedClaims(getReqAttributeFromPolicyJson(
				getPolicyObject(policyFromDb.getPolicyFileId()), ALLOWED_KYC_ATTRIBUTES, ATTRIBUTE_NAME, null))));
		clientDetail.setAcrValues(String.join(",", authenticationContextClassRefUtil.getAuthFactors(getReqAttributeFromPolicyJson(
				getPolicyObject(policyFromDb.getPolicyFileId()), ALLOWED_AUTH_TYPES, AUTH_TYPE, MANDATORY))));		
		clientDetail.setStatus("ACTIVE");
		clientDetail.setGrantTypes(String.join(",", createRequest.getGrantTypes()));
		clientDetail.setClientAuthMethods(String.join(",", createRequest.getClientAuthMethods()));
		clientDetail.setCreatedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
		clientDetail.setCreatedBy(getLoggedInUserId());
		authenticationContextClassRefUtil.getPolicySupportedClaims(Arrays.asList(clientDetail.getClaims()));
		callIdpService(clientDetail, environment.getProperty("pmp-idp.oidc.client.create.rest.uri"), true);
		publishClientData(policyMappingReqFromDb.get(0).getPartner(), policyFromDb, clientDetail);
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
	private static List<String> getReqAttributeFromPolicyJson(JSONObject policyObject, String parentAttribute,
			String childAttribute, String childAttribute1) throws Exception {
		List<String> attributes = new ArrayList<>();
		JSONArray parentAttributeObject = (JSONArray) policyObject.get(parentAttribute);
		for (int i = 0; i < parentAttributeObject.size(); i++) {
			JSONObject childJsonArray = (JSONObject) parentAttributeObject.get(i);
			String key = (String) childJsonArray.get(childAttribute);
			if (childAttribute1 != null) {
				if ((boolean) childJsonArray.get(childAttribute1)) {
					LOGGER.error("createOIDCClient::Policy is having mandatory auth. So client cannot be created.");
					throw new PartnerServiceException(ErrorCode.POLICY_HAVING_MANDATORY_AUTHS.getErrorCode(),
							ErrorCode.POLICY_HAVING_MANDATORY_AUTHS.getErrorMessage());
				}
			}
			attributes.add(key);
		}
		return attributes;
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private ClientDetailResponse callIdpService(ClientDetail request, String calleeApi, boolean isItForCreate) {
		RequestWrapper<CreateClientRequestDto> createRequestwrapper = new RequestWrapper<>();
		createRequestwrapper.setRequestTime(DateUtils.getUTCCurrentDateTimeString("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
		CreateClientRequestDto dto = new CreateClientRequestDto();
		dto.setClientId(request.getId());
		dto.setClientName(request.getName());
		dto.setRelyingPartyId(request.getRpId());
		dto.setLogoUri(request.getLogoUri());
		try {
			dto.setPublicKey(objectMapper.readValue(request.getPublicKey(), Map.class));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		updateRequestwrapper.setRequestTime(DateUtils.getUTCCurrentDateTimeString());
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
				LOGGER.error(("IDAServiceResponse:: Idp service response contains errors."));
				throw new ApiAccessibleException(idpServiceErrorList.get(0).get(PartnerConstants.ERRORCODE).toString(),
						idpServiceErrorList.get(0).get(PartnerConstants.ERRORMESSAGE).toString());
			} else {
				LOGGER.error(("IDAServiceResponse:: Idp service response contains errors."));
				throw new ApiAccessibleException(ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorCode(),
						ApiAccessibleExceptionConstant.UNABLE_TO_PROCESS.getErrorMessage());
			}
		}
		if (response == null) {
			LOGGER.error(("IDAServiceResponse:: Idp service response is null"));
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
	private void publishClientData(Partner partnerData, AuthPolicy policyData, ClientDetail clinetData) {
		notify(MapperUtils.mapDataToPublishDto(partnerData, getPartnerCertificate(partnerData.getCertificateAlias())),
				MapperUtils.mapPolicyToPublishDto(policyData, getPolicyObject(policyData.getPolicyFileId())),
				MapperUtils.mapClientDataToPublishDto(clinetData), EventType.OIDC_CLIENT_CREATED);
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
		makeUpdateIDPServiceCall(clientDetail, environment.getProperty("pmp-idp.oidc.client.update.rest.uri"));
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
		dto.setRelayingPartyId(result.get().getRpId());
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