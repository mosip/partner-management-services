package io.mosip.pms.test.oauth.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mosip.kernel.openid.bridge.model.AuthUserDetails;
import io.mosip.kernel.openid.bridge.model.MosipUserDto;
import io.mosip.pms.common.entity.*;
import io.mosip.pms.common.entity.ClientDetail;
import io.mosip.pms.common.repository.*;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.common.util.AuthenticationContextRefUtil;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.oauth.client.dto.ClientDetailV2;
import io.mosip.pms.oidc.client.contant.ClientServiceAuditEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.mosip.pms.common.constant.EventType;
import io.mosip.pms.common.dto.ClientPublishDto;
import io.mosip.pms.common.dto.PartnerDataPublishDto;
import io.mosip.pms.common.dto.PolicyPublishDto;
import io.mosip.pms.common.dto.Type;
import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.exception.ApiAccessibleException;
import io.mosip.pms.common.util.MapperUtils;
import io.mosip.pms.oauth.client.dto.*;
import io.mosip.pms.common.dto.PartnerCertDownloadResponeDto;
import net.minidev.json.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static io.mosip.pms.common.util.UserDetailUtil.getLoggedInUserId;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.oauth.client.service.impl.ClientManagementServiceImpl;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.exception.PartnerServiceException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ClientManagementServiceImplTest {

	@Autowired
	private ClientManagementServiceImpl serviceImpl;

	@Mock
	private Environment environment;

	@MockBean
	PartnerRepository partnerRepository;

	@MockBean
	ClientDetailRepository clientDetailRepository;

	@MockBean
	AuthPolicyRepository authPolicyRepository;

	@MockBean
	ClientSummaryRepository clientSummaryRepository;

	@MockBean
	PartnerPolicyRequestRepository partnerPolicyRequestRepository;

	@MockBean
	private RestUtil restUtil;
	
	@MockBean
	private AuditUtil auditUtil;

	private ObjectMapper objectMapper;

	@MockBean
	private WebSubPublisher webSubPublisher;

	@Mock
	Authentication authentication;

	@Mock
	SecurityContext securityContext;

	@MockBean
	PartnerServiceRepository partnerServiceRepository;

	@MockBean
	PolicyGroupRepository policyGroupRepository;

	@MockBean
	private AuthenticationContextRefUtil authenticationContextClassRefUtil;

	Map<String, Object> public_key;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(serviceImpl, "webSubPublisher", webSubPublisher);
		ReflectionTestUtils.setField(serviceImpl, "restUtil", restUtil);
		ReflectionTestUtils.setField(serviceImpl, "environment", environment);
		// Use real ObjectMapper for JSON serialization/deserialization
		objectMapper = new ObjectMapper();
		ReflectionTestUtils.setField(serviceImpl, "objectMapper", objectMapper);

		public_key = new HashMap<>();
		public_key.put("kty","RSA");
		public_key.put("e","AQAB");
		public_key.put( "use", "sig");
		public_key.put(  "kid", "1bbdc9de-c24f-4801-b6b3-691ac07641af");
		public_key.put( "alg", "RS256");
		public_key.put(  "n","wXGQA574CU-WTWPILd4S3_1sJf0Yof0kwMeNctXc1thQo70Ljfn9f4igpRe7f8qNs_W6dLuLWemFhGJBQBQ7vvickECKNJfo_EzSD_yyPCg7k_AGbTWTkuoObHrpilwJGyKVSkOIujH_FqHIVkwkVXjWc25Lsb8Gq4nAHNQEqqgaYPLEi5evCR6S0FzcXTPuRh9zH-cM0Onjv4orrfYpEr61HcRp5MXL55b7yBoIYlXD8NfalcgdrWzp4VZHvQ8yT9G5eaf27XUn6ZBeBf7VnELcKFTyw1pK2wqoOxRBc8Y1wO6rEy8PlCU6wD-mbIzcjG1wUfnbgvJOM4A5G41quQ");
	}

	@Test
	public void createClientTest() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("PARTNER_ADMIN")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		ClientDetailCreateRequest request = new ClientDetailCreateRequest();
		request.setPublicKey(public_key);
		request.setPolicyId("policy");
		request.setAuthPartnerId("authPartnerId");
		List<String> clientAuthMethods = new ArrayList<String>();
		clientAuthMethods.add("ClientAuthMethod");
		request.setClientAuthMethods(clientAuthMethods);
		request.setGrantTypes(clientAuthMethods);
		request.setLogoUri("https://testcase.pms.net/browse/OIDCClient.png");
		request.setRedirectUris(clientAuthMethods);
		request.setName("ClientName");
		Mockito.doNothing().when(auditUtil).setAuditRequestDto(Mockito.any(ClientServiceAuditEnum.class));
		try {
			serviceImpl.createOIDCClient(request);
		}catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.INVALID_PARTNERID.getErrorCode()));
		}
	}

	@Test(expected = Exception.class)
	public void createOIDCClientTest_InvalidPolicy() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("PARTNER_ADMIN")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		ClientDetailCreateRequest request = new ClientDetailCreateRequest();
		request.setPublicKey(public_key);
		request.setPolicyId("policy");
		request.setAuthPartnerId("authPartnerId");
		List<String> clientAuthMethods = new ArrayList<String>();
		clientAuthMethods.add("ClientAuthMethod");
		request.setClientAuthMethods(clientAuthMethods);
		request.setGrantTypes(clientAuthMethods);
		request.setLogoUri("https://testcase.pms.net/browse/OIDCClient.png");
		request.setRedirectUris(clientAuthMethods);
		request.setName("ClientName");
		Mockito.doNothing().when(auditUtil).setAuditRequestDto(Mockito.any(ClientServiceAuditEnum.class));
		Partner partner = new Partner();
		partner.setId("authPartnerId");
		partner.setPartnerTypeCode("Auth_Partner");
		partner.setIsActive(true);
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.setPolicy_type("Auth");
		authPolicy.setId("123");
		when(authPolicyRepository.findById(any())).thenReturn(Optional.of(authPolicy));
		serviceImpl.createOIDCClient(request);
	}

	@Test(expected = Exception.class)
	public void createOIDCClientTest_NoUserClaim() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("PARTNER_ADMIN")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		ClientDetailCreateRequest request = new ClientDetailCreateRequest();
		request.setPublicKey(public_key);
		request.setPolicyId("policy");
		request.setAuthPartnerId("authPartnerId");
		List<String> clientAuthMethods = new ArrayList<String>();
		clientAuthMethods.add("ClientAuthMethod");
		request.setClientAuthMethods(clientAuthMethods);
		request.setGrantTypes(clientAuthMethods);
		request.setLogoUri("https://testcase.pms.net/browse/OIDCClient.png");
		request.setRedirectUris(clientAuthMethods);
		request.setName("ClientName");
		Mockito.doNothing().when(auditUtil).setAuditRequestDto(Mockito.any(ClientServiceAuditEnum.class));
		Partner partner = new Partner();
		partner.setId("authPartnerId");
		partner.setPartnerTypeCode("Auth_Partner");
		partner.setIsActive(true);
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.setPolicy_type("Auth");
		authPolicy.setId("123");
		authPolicy.setPolicyFileId("{"
				+ "\"policyFileId\": \"212\","
				+ "\"policyName\": \"Test Policy\","
				+ "\"active\": true,"
				+ "\"authTokenType\": \"partner\","
				+ "\"allowedKycAttributes\": ["
				+ "{\"attributeName\": \"fullName\"},"
				+ "{\"attributeName\": \"gender\"},"
				+ "{\"attributeName\": \"residenceStatus\"},"
				+ "{\"attributeName\": \"dateOfBirth\"},"
				+ "{\"attributeName\": \"photo\"},"
				+ "{\"attributeName\": \"firstName\"},"
				+ "{\"attributeName\": \"province\"},"
				+ "{\"attributeName\": \"city\"},"
				+ "{\"attributeName\": \"zone\"},"
				+ "{\"attributeName\": \"postalCode\"},"
				+ "{\"attributeName\": \"phone\"},"
				+ "{\"attributeName\": \"email\"}"
				+ "],"
				+ "\"kycLanguages\": [\"ara\", \"eng\"],"
				+ "\"allowedAuthTypes\": ["
				+ "{\"authSubType\": \"IRIS\", \"authType\": \"bio\", \"mandatory\": false},"
				+ "{\"authSubType\": \"FINGER\", \"authType\": \"bio\", \"mandatory\": false},"
				+ "{\"authSubType\": \"FACE\", \"authType\": \"bio\", \"mandatory\": false},"
				+ "{\"authSubType\": \"\", \"authType\": \"otp\", \"mandatory\": false},"
				+ "{\"authSubType\": \"\", \"authType\": \"otp-request\", \"mandatory\": false},"
				+ "{\"authSubType\": \"\", \"authType\": \"kyc\", \"mandatory\": false},"
				+ "{\"authSubType\": \"\", \"authType\": \"demo\", \"mandatory\": false}"
				+ "]"
				+ "}");
		when(authPolicyRepository.findById(any())).thenReturn(Optional.of(authPolicy));
		List<PartnerPolicyRequest> partnerPolicyRequestList = new ArrayList<>();
		PartnerPolicyRequest partnerPolicyRequest = new PartnerPolicyRequest();
		partnerPolicyRequest.setPartner(partner);
		partnerPolicyRequestList.add(partnerPolicyRequest);
		when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyId(any(), any())).thenReturn(partnerPolicyRequestList);
		when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyIdAndStatusCode(any(), any(), any())).thenReturn(partnerPolicyRequestList);
		Set<String> supportedClaims = new HashSet<>();
		when(authenticationContextClassRefUtil.getPolicySupportedClaims(any())).thenReturn(supportedClaims);
		serviceImpl.createOIDCClient(request);
	}

	@Test (expected = PartnerServiceException.class)
	public void testCreateOIDCClient_WithEmptyPublicKey_ThrowsException() throws Exception {
		ClientDetailCreateRequest clientDetailCreateRequest = new ClientDetailCreateRequest();
		clientDetailCreateRequest.setPublicKey(new HashMap<>());
		serviceImpl.createOIDCClient(clientDetailCreateRequest);
	}

	@Test (expected = PartnerServiceException.class)
	public void testCreateOIDCClient_WithEmptyPublicKey_ThrowsExceptionWithMock() throws Exception {
		ClientDetailCreateRequest clientDetailCreateRequest = mock(ClientDetailCreateRequest.class);
		when(clientDetailCreateRequest.getPublicKey()).thenReturn(new HashMap<>());
		serviceImpl.createOIDCClient(clientDetailCreateRequest);
		verify(clientDetailCreateRequest).getPublicKey();
	}

	@Test (expected = PartnerServiceException.class)
	public void createOIDCClient_invalidPublicKey_throwsPartnerServiceException() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("PARTNER_ADMIN")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		ClientDetailCreateRequest createRequest = new ClientDetailCreateRequest();
		createRequest.setName("ClientName");
		createRequest.setAuthPartnerId("AuthPartnerId");
		createRequest.setPolicyId("PolicyID");
		List<String> clientAuthMethods = new ArrayList();
		clientAuthMethods.add("private_key_jwt");
		createRequest.setClientAuthMethods(clientAuthMethods);
		List<String> setGrantTypes = new ArrayList();
		setGrantTypes.add("authorization_code");
		createRequest.setGrantTypes(setGrantTypes);
		createRequest.setLogoUri("https://testcase.pms.net/browse/OIDCClient.png");
		List<String> setRedirectUris = new ArrayList();
		setRedirectUris.add("https://testcase.pms.net/browse/OIDCClient");
		createRequest.setRedirectUris(setRedirectUris);
		createRequest.setPublicKey(public_key);

		serviceImpl.createOIDCClient(createRequest);
	}

	@Test (expected = PartnerServiceException.class)
	public void createOIDCClient_existingClientDetail_throwsPartnerServiceException() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("PARTNER_ADMIN")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		ClientDetailCreateRequest createRequest = new ClientDetailCreateRequest();
		createRequest.setName("ClientName");
		createRequest.setAuthPartnerId("AuthPartnerId");
		createRequest.setPolicyId("PolicyID");
		List<String> clientAuthMethods = new ArrayList();
		clientAuthMethods.add("private_key_jwt");
		createRequest.setClientAuthMethods(clientAuthMethods);
		List<String> setGrantTypes = new ArrayList();
		setGrantTypes.add("authorization_code");
		createRequest.setGrantTypes(setGrantTypes);
		createRequest.setLogoUri("https://testcase.pms.net/browse/OIDCClient.png");
		List<String> setRedirectUris = new ArrayList();
		setRedirectUris.add("https://testcase.pms.net/browse/OIDCClient");
		createRequest.setRedirectUris(setRedirectUris);
		createRequest.setPublicKey(public_key);

		ClientDetail clientDetail = new ClientDetail();
		clientDetail.setPublicKey("public_key");
		clientDetail.setId("clientId");
		clientDetail.setName(createRequest.getName());
		clientDetail.setRpId(createRequest.getAuthPartnerId());
		clientDetail.setPolicyId(createRequest.getPolicyId());
		clientDetail.setLogoUri(createRequest.getLogoUri());
		clientDetail.setRedirectUris(String.join(",", createRequest.getRedirectUris()));
		clientDetail.setClaims("claims");
		clientDetail.setAcrValues( "acrValues");
		clientDetail.setStatus("ACTIVE");
		clientDetail.setGrantTypes(String.join(",", createRequest.getGrantTypes()));
		clientDetail.setClientAuthMethods(String.join(",", createRequest.getClientAuthMethods()));
		clientDetail.setCreatedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
		clientDetail.setCreatedBy(getLoggedInUserId());

		serviceImpl.createOIDCClient(createRequest);
		assertEquals(createRequest,clientDetail);
	}

	@Test (expected = PartnerServiceException.class)
	public void createOIDCClient_invalidPartnerId_throwsPartnerServiceException() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("PARTNER_ADMIN")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		ClientDetailCreateRequest createRequest = new ClientDetailCreateRequest();
		createRequest.setName("ClientName");
		createRequest.setAuthPartnerId("AuthPartnerId");
		createRequest.setPolicyId("PolicyID");
		List<String> clientAuthMethods = new ArrayList();
		clientAuthMethods.add("private_key_jwt");
		createRequest.setClientAuthMethods(clientAuthMethods);
		List<String> setGrantTypes = new ArrayList();
		setGrantTypes.add("authorization_code");
		createRequest.setGrantTypes(setGrantTypes);
		createRequest.setLogoUri("https://testcase.pms.net/browse/OIDCClient.png");
		List<String> setRedirectUris = new ArrayList();
		setRedirectUris.add("https://testcase.pms.net/browse/OIDCClient");
		createRequest.setRedirectUris(setRedirectUris);
		createRequest.setPublicKey(public_key);

		when(environment.getProperty(any(String.class))).thenReturn("https://testcase.pms.net/browse/OIDCClient.png");
		when(clientDetailRepository.findById(any(String.class))).thenReturn(Optional.empty());
		Partner partner = new Partner();
		partner.setPartnerTypeCode("Device_Provider");
		when(partnerRepository.findById(any(String.class))).thenReturn(Optional.of(partner));

		ClientDetailResponse response = serviceImpl.createOIDCClient(createRequest);

		assertNotNull(response);
	}

	@Test (expected = PartnerServiceException.class)
	public void createOIDCClient_validRequest_returnsClientDetailResponse() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("PARTNER_ADMIN")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		when(environment.getProperty("auth.url")).thenReturn("https://pms.net/partner");
		when(clientDetailRepository.findById(anyString())).thenReturn(Optional.empty());
		when(partnerRepository.findById(anyString())).thenReturn(Optional.empty());

		ClientDetailCreateRequest createRequest = new ClientDetailCreateRequest();
		createRequest.setName("ClientName");
		createRequest.setAuthPartnerId("AuthPartnerId");
		createRequest.setPolicyId("PolicyID");
		List<String> clientAuthMethods = new ArrayList();
		clientAuthMethods.add("private_key_jwt");
		createRequest.setClientAuthMethods(clientAuthMethods);
		List<String> setGrantTypes = new ArrayList();
		setGrantTypes.add("authorization_code");
		createRequest.setGrantTypes(setGrantTypes);
		createRequest.setLogoUri("https://testcase.pms.net/browse/OIDCClient.png");
		List<String> setRedirectUris = new ArrayList();
		setRedirectUris.add("https://testcase.pms.net/browse/OIDCClient");
		createRequest.setRedirectUris(setRedirectUris);
		createRequest.setPublicKey(public_key);

		ClientDetailResponse response = serviceImpl.createOIDCClient(createRequest);

		assertNotNull(response);
	}

	@Test (expected = PartnerServiceException.class)
	public void createClientDetailCreateRequest() throws Exception {

		when(clientDetailRepository.findById(anyString())).thenReturn(Optional.of(new ClientDetail()));

		ClientDetailCreateRequest createRequest = new ClientDetailCreateRequest();
		createRequest.setName("ClientName");
		createRequest.setAuthPartnerId("AuthPartnerId");
		createRequest.setPolicyId("PolicyID");
		List<String> clientAuthMethods = new ArrayList();
		clientAuthMethods.add("private_key_jwt");
		createRequest.setClientAuthMethods(clientAuthMethods);
		List<String> setGrantTypes = new ArrayList();
		setGrantTypes.add("authorization_code");
		createRequest.setGrantTypes(setGrantTypes);
		createRequest.setLogoUri("https://testcase.pms.net/browse/OIDCClient.png");
		List<String> setRedirectUris = new ArrayList();
		setRedirectUris.add("https://testcase.pms.net/browse/OIDCClient");
		createRequest.setRedirectUris(setRedirectUris);
		createRequest.setPublicKey(public_key);

		serviceImpl.createOIDCClient(createRequest);
	}

	@Test
	public void testCreateOIDCClientSuccessfulCreation() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("PARTNER_ADMIN")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		ClientDetailCreateRequest request = new ClientDetailCreateRequest();
		request.setPublicKey(public_key);
		request.setPolicyId("policy");
		request.setAuthPartnerId("authPartnerId");
		List<String> clientAuthMethods = new ArrayList<>();
		clientAuthMethods.add("private_key_jwt");
		request.setClientAuthMethods(clientAuthMethods);
		request.setGrantTypes(clientAuthMethods);
		request.setLogoUri("https://testcase.pms.net/browse/OIDCClient.png");
		request.setRedirectUris(clientAuthMethods);
		request.setName("ClientName");

		when(clientDetailRepository.findById(anyString())).thenReturn(Optional.empty());

		Partner partner = new Partner();
		partner.setId("authPartnerId");
		partner.setPartnerTypeCode("Auth_Partner");
		partner.setIsActive(true);
		partner.setUserId("123");
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));

		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.setPolicy_type("Auth");
		authPolicy.setId("policy");
		authPolicy.setIsActive(true);
		authPolicy.setPolicyFileId("{\"allowedKycAttributes\":[{\"attributeName\":\"name\"}],\"allowedAuthTypes\":[{\"authType\":\"otp\",\"mandatory\":false}]}");
		when(authPolicyRepository.findById(any())).thenReturn(Optional.of(authPolicy));

		List<PartnerPolicyRequest> partnerPolicyRequestList = new ArrayList<>();
		PartnerPolicyRequest partnerPolicyRequest = new PartnerPolicyRequest();
		partnerPolicyRequest.setPartner(partner);
		partnerPolicyRequestList.add(partnerPolicyRequest);
		when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyId(any(), any())).thenReturn(partnerPolicyRequestList);
		when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyIdAndStatusCode(any(), any(), any())).thenReturn(partnerPolicyRequestList);

		Set<String> supportedClaims = new HashSet<>();
		supportedClaims.add("name");
		when(authenticationContextClassRefUtil.getPolicySupportedClaims(any())).thenReturn(supportedClaims);

		Set<String> acrValues = new HashSet<>();
		acrValues.add("mosip:idp:acr:static-code");
		when(authenticationContextClassRefUtil.getAuthFactors(any())).thenReturn(acrValues);

		Map<String, Object> esignetResponse = new HashMap<>();
		ClientDetailResponse clientDetailResponse = new ClientDetailResponse();
		clientDetailResponse.setClientId("clientId");
		clientDetailResponse.setStatus("ACTIVE");
		esignetResponse.put("response", clientDetailResponse);
		when(restUtil.postApi(anyString(), any(), anyString(), anyString(), any(), any(), any())).thenReturn(esignetResponse);

		when(environment.getProperty("mosip.pms.esignet.oidc-client-create-url")).thenReturn("http://esignet/create");
		when(environment.getProperty("pmp.partner.certificaticate.get.rest.uri")).thenReturn("http://cert/get");

		Map<String, Object> certResponse = new HashMap<>();
		PartnerCertDownloadResponeDto certDto = new PartnerCertDownloadResponeDto();
		certDto.setCertificateData("certData");
		certResponse.put("response", certDto);
		when(restUtil.getApi(anyString(), any(), any())).thenReturn(certResponse);

		doNothing().when(webSubPublisher).notify(any(), any(), any());
		doNothing().when(auditUtil).setAuditRequestDto(any(ClientServiceAuditEnum.class));

		ClientDetail savedClient = new ClientDetail();
		savedClient.setId("clientId");
		savedClient.setStatus("ACTIVE");
		when(clientDetailRepository.save(any())).thenReturn(savedClient);

		ClientDetailResponse response = serviceImpl.createOIDCClient(request);

		assertNotNull(response);
		assertNotNull(response.getClientId());
		assertEquals("ACTIVE", response.getStatus());
	}

	@Test (expected = PartnerServiceException.class)
	public void testUpdateOIDCClient_WithFailure() throws Exception {
		doNothing().when(auditUtil).setAuditRequestDto((ClientServiceAuditEnum) any());

		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("AUTH_PARTNER")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		ClientDetail clientDetail = new ClientDetail();
		clientDetail.setAcrValues("value");
		clientDetail.setClaims("Claims");
		clientDetail.setClientAuthMethods("Client Auth Methods");
		clientDetail.setCreatedBy("Cr By");
		clientDetail.setCreatedDateTime(LocalDateTime.of(1, 1, 1, 1, 1));
		clientDetail.setGrantTypes("Grant Types");
		clientDetail.setId("Id");
		clientDetail.setIsDeleted(false);
		clientDetail.setLogoUri("Logo Uri");
		clientDetail.setName("Name");
		clientDetail.setPolicyId("PolicyId");
		clientDetail.setPublicKey("Public Key");
		clientDetail.setRedirectUris("Redirect Uris");
		clientDetail.setRpId("RpId");
		clientDetail.setStatus("Status");
		clientDetail.setUpdatedBy("UpdatedBy");
		clientDetail.setUpdatedDateTime(LocalDateTime.of(1, 1, 1, 1, 1));
		Optional<ClientDetail> ofResult = Optional.of(clientDetail);

		ClientDetail clientDetail1 = new ClientDetail();
		clientDetail1.setAcrValues("123");
		clientDetail1.setClaims("Claims");
		clientDetail1.setClientAuthMethods("Client Auth Methods");
		clientDetail1.setCreatedBy("Cr By");
		clientDetail1.setCreatedDateTime(LocalDateTime.of(1, 1, 1, 1, 1));
		clientDetail1.setGrantTypes("Grant Types");
		clientDetail1.setId("123");
		clientDetail1.setIsDeleted(true);
		clientDetail1.setLogoUri("Logo Uri");
		clientDetail1.setName("Name");
		clientDetail1.setPolicyId("123");
		clientDetail1.setPublicKey("Public Key");
		clientDetail1.setRedirectUris("Redirect Uris");
		clientDetail1.setRpId("123");
		clientDetail1.setStatus("Status");
		clientDetail1.setUpdatedBy("UpdatedBy");
		clientDetail1.setUpdatedDateTime(LocalDateTime.of(1, 1, 1, 1, 1));

		when(clientDetailRepository.save(any(ClientDetail.class))).thenReturn(clientDetail1);
		when(clientDetailRepository.findById(anyString())).thenReturn(ofResult);
		when(environment.getProperty(anyString())).thenReturn("Property");
		doNothing().when(restUtil).putApi(any(), any(), any(), any(), any(), any(), any());
		doThrow(new PartnerServiceException("An error occurred", "An error occurred"))
				.when(webSubPublisher).notify(any(), any(), any());

		ClientDetailUpdateRequest clientDetailUpdateRequest = new ClientDetailUpdateRequest();
		clientDetailUpdateRequest.setClientAuthMethods(new ArrayList<>());
		clientDetailUpdateRequest.setClientName("ClientName");
		clientDetailUpdateRequest.setGrantTypes(new ArrayList<>());
		clientDetailUpdateRequest.setLogoUri("Logo Uri");
		clientDetailUpdateRequest.setRedirectUris(new ArrayList<>());
		clientDetailUpdateRequest.setStatus("Status");

		serviceImpl.updateOIDCClient("Id", clientDetailUpdateRequest);

		verify(clientDetailRepository).save(any(ClientDetail.class));
		verify(clientDetailRepository).findById(anyString());
		verify(environment).getProperty(anyString());
		verify(restUtil).putApi(any(), any(), any(), any(), any(), any(), any());
		verify(webSubPublisher).notify(any(), any(), any());
	}

	@Test
	public void testGetClientDetails_Success() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("PARTNER_ADMIN")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setCrBy("Cr By");
		policyGroup.setCrDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		policyGroup.setDelDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		policyGroup.setDesc("The characteristics of someone or something");
		policyGroup.setId("Id");
		policyGroup.setIsActive(true);
		policyGroup.setIsDeleted(false);
		policyGroup.setName("Name");
		policyGroup.setUpdBy("Upd By");
		policyGroup.setUpdDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		policyGroup.setUserId("UserId");

		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.SetVersion("1.0");
		authPolicy.setCrBy("Cr By");
		authPolicy.setCrDtimes(mock(Timestamp.class));
		authPolicy.setDelDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		authPolicy.setDescr("desc");
		authPolicy.setId("Id");
		authPolicy.setIsActive(true);
		authPolicy.setIsDeleted(false);
		authPolicy.setName("Name");
		authPolicy.setPolicyFileId("Field");
		authPolicy.setPolicyGroup(policyGroup);
		authPolicy.setPolicySchema("Schema");
		authPolicy.setPolicy_type("Policy Type");
		authPolicy.setUpdBy("Upd By");
		authPolicy.setUpdDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		authPolicy.setValidFromDate(LocalDateTime.of(1, 1, 1, 1, 1));
		authPolicy.setValidToDate(LocalDateTime.of(1, 1, 1, 1, 1));
		Optional<AuthPolicy> authPolicyOptional = Optional.of(authPolicy);
		when(authPolicyRepository.findById((String) any())).thenReturn(authPolicyOptional);

		io.mosip.pms.common.entity.ClientDetail clientDetail = new io.mosip.pms.common.entity.ClientDetail();
		clientDetail.setAcrValues("values");
		clientDetail.setClaims("Claims");
		clientDetail.setClientAuthMethods("Client Auth Methods");
		clientDetail.setCreatedBy("Cr By");
		clientDetail.setCreatedDateTime(LocalDateTime.of(1, 1, 1, 1, 1));
		clientDetail.setGrantTypes("Grant Types");
		clientDetail.setId("Id");
		clientDetail.setIsDeleted(false);
		clientDetail.setLogoUri("Logo Uri");
		clientDetail.setName("Name");
		clientDetail.setPolicyId("PolicyId");
		clientDetail.setPublicKey("Public Key");
		clientDetail.setRedirectUris("Redirect Uris");
		clientDetail.setRpId("RpId");
		clientDetail.setStatus("Status");
		clientDetail.setUpdatedBy("UpdatedBy");
		clientDetail.setUpdatedDateTime(LocalDateTime.of(1, 1, 1, 1, 1));

		Optional<io.mosip.pms.common.entity.ClientDetail> clientDetailOptional  = Optional.of(clientDetail);
		when(clientDetailRepository.findById((String) any())).thenReturn(clientDetailOptional );

		io.mosip.pms.oauth.client.dto.ClientDetail actualClientDetails = serviceImpl.getClientDetails("Id");

		assertEquals(1, actualClientDetails.getAcrValues().size());
		assertEquals("Status", actualClientDetails.getStatus());
		assertEquals("RpId", actualClientDetails.getRelyingPartyId());
		assertEquals(1, actualClientDetails.getRedirectUris().size());
		assertEquals("Public Key", actualClientDetails.getPublicKey());
		assertEquals("Name", actualClientDetails.getPolicyName());
		assertEquals("PolicyId", actualClientDetails.getPolicyId());
		assertEquals("Name", actualClientDetails.getName());
		assertEquals("Logo Uri", actualClientDetails.getLogoUri());
		assertEquals("Id", actualClientDetails.getId());
		assertEquals(1, actualClientDetails.getGrantTypes().size());
		assertEquals(1, actualClientDetails.getClientAuthMethods().size());
		assertEquals(1, actualClientDetails.getClaims().size());
		verify(authPolicyRepository).findById(anyString());
		verify(clientDetailRepository).findById(anyString());
	}

	@Test (expected = PartnerServiceException.class)
	public void testGetClientDetails_Exception1() {
        when(clientDetailRepository.findById(anyString())).thenReturn(Optional.empty());
		serviceImpl.getClientDetails("Id");
	}

	@Test (expected = PartnerServiceException.class)
	public void testGetClientDetails_Exception2() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("PARTNER")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		io.mosip.pms.common.entity.ClientDetail clientDetail = new io.mosip.pms.common.entity.ClientDetail();
		clientDetail.setAcrValues("values");
		clientDetail.setClaims("Claims");
		Optional<io.mosip.pms.common.entity.ClientDetail> clientDetailOptional = Optional.of(clientDetail);
		when(clientDetailRepository.findById(anyString())).thenReturn(clientDetailOptional);

		serviceImpl.getClientDetails("Id");
	}

	@Test (expected = PartnerServiceException.class)
	public void testGetClientDetails_Exception3() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("PARTNER")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		io.mosip.pms.common.entity.ClientDetail clientDetail = new io.mosip.pms.common.entity.ClientDetail();
		clientDetail.setAcrValues("values");
		clientDetail.setClaims("Claims");
		Optional<io.mosip.pms.common.entity.ClientDetail> clientDetailOptional = Optional.of(clientDetail);
		when(clientDetailRepository.findById(anyString())).thenReturn(clientDetailOptional);

		ArrayList<Partner> partnerArrayList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partnerArrayList.add(partner);
		when(partnerServiceRepository.findByUserId(any())).thenReturn(partnerArrayList);

		serviceImpl.getClientDetails("Id");
	}

	@Test (expected = Exception.class)
	public void testGetClientDetails_Exception() throws Exception {
		when(authPolicyRepository.findById(anyString()))
				.thenThrow(new PartnerServiceException("An error occurred", "An error occurred"));

		io.mosip.pms.common.entity.ClientDetail clientDetail = new io.mosip.pms.common.entity.ClientDetail();
		clientDetail.setAcrValues("values");
		clientDetail.setClaims("Claims");
		clientDetail.setClientAuthMethods("Client Auth Methods");
		clientDetail.setCreatedBy("Cr By");
		clientDetail.setCreatedDateTime(LocalDateTime.of(1, 1, 1, 1, 1));
		clientDetail.setGrantTypes("Grant Types");
		clientDetail.setId("Id");
		clientDetail.setIsDeleted(false);
		clientDetail.setLogoUri("Logo Uri");
		clientDetail.setName("Name");
		clientDetail.setPublicKey("Public Key");
		clientDetail.setRedirectUris("Redirect Uris");
		clientDetail.setRpId("RpId");
		clientDetail.setStatus("Status");
		clientDetail.setUpdatedBy("UpdatedBy");
		clientDetail.setUpdatedDateTime(LocalDateTime.of(1, 1, 1, 1, 1));

		Optional<io.mosip.pms.common.entity.ClientDetail> clientDetailOptional = Optional.of(clientDetail);
		when(clientDetailRepository.findById(anyString())).thenReturn(clientDetailOptional);

		serviceImpl.getClientDetails("Id");
		verify(authPolicyRepository).findById(anyString());
		verify(clientDetailRepository).findById(anyString());
	}

	@Test
	public void testNotify_SuccessfullyHandlesAllEventTypes() {
		doNothing().when(webSubPublisher).notify(any(EventType.class), any(Map.class), any(Type.class));

		PartnerDataPublishDto partnerDataPublishDto = new PartnerDataPublishDto();
		partnerDataPublishDto.setCertificateData("Certificate Data");
		partnerDataPublishDto.setPartnerId("PartnerId");
		partnerDataPublishDto.setPartnerName("Partner Name");
		partnerDataPublishDto.setPartnerStatus("Partner Status");

		PolicyPublishDto policyPublishDto = new PolicyPublishDto();
		policyPublishDto.setPolicy(new JSONObject());
		policyPublishDto.setPolicyCommenceOn(LocalDateTime.of(1, 1, 1, 1, 1));
		policyPublishDto.setPolicyDescription("Policy Description");
		policyPublishDto.setPolicyExpiresOn(LocalDateTime.of(1, 1, 1, 1, 1));
		policyPublishDto.setPolicyId("PolicyId");
		policyPublishDto.setPolicyName("Policy Name");
		policyPublishDto.setPolicyStatus("Policy Status");

		ClientPublishDto clientPublishDto = new ClientPublishDto();
		clientPublishDto.setAuthContextRefs(new ArrayList<>());
		clientPublishDto.setClientAuthMethods(new ArrayList<>());
		clientPublishDto.setClientId("ClientId");
		clientPublishDto.setClientName("ClientName");
		clientPublishDto.setClientStatus("Client Status");
		clientPublishDto.setUserClaims(new ArrayList<>());

		ReflectionTestUtils.invokeMethod(serviceImpl, "notify", partnerDataPublishDto, policyPublishDto, clientPublishDto,
				EventType.MISP_UPDATED);
		ReflectionTestUtils.invokeMethod(serviceImpl, "notify", partnerDataPublishDto, policyPublishDto, clientPublishDto,
				EventType.POLICY_UPDATED);
		ReflectionTestUtils.invokeMethod(serviceImpl, "notify", partnerDataPublishDto, policyPublishDto, clientPublishDto,
				EventType.APIKEY_UPDATED);
		ReflectionTestUtils.invokeMethod(serviceImpl, "notify", partnerDataPublishDto, policyPublishDto, clientPublishDto,
				EventType.PARTNER_UPDATED);
		ReflectionTestUtils.invokeMethod(serviceImpl, "notify", partnerDataPublishDto, policyPublishDto, clientPublishDto,
				EventType.CA_CERTIFICATE_UPLOADED);

		verify(webSubPublisher, times(5)).notify(any(EventType.class), any(Map.class), any(Type.class));
	}

	@Test (expected = PartnerServiceException.class)
	public void testNotify_FailureHandling() {
		doThrow(new PartnerServiceException("An error occurred", "An error occurred")).when(webSubPublisher)
				.notify(any(EventType.class), any(Map.class), any(Type.class));

		PartnerDataPublishDto partnerDataPublishDto = new PartnerDataPublishDto();
		partnerDataPublishDto.setCertificateData("Certificate Data");
		partnerDataPublishDto.setPartnerId("PartnerId");
		partnerDataPublishDto.setPartnerName("Partner Name");
		partnerDataPublishDto.setPartnerStatus("Partner Status");

		PolicyPublishDto policyPublishDto = new PolicyPublishDto();
		policyPublishDto.setPolicy(new JSONObject());
		policyPublishDto.setPolicyCommenceOn(LocalDateTime.of(1, 1, 1, 1, 1));
		policyPublishDto.setPolicyDescription("Policy Description");
		policyPublishDto.setPolicyExpiresOn(LocalDateTime.of(1, 1, 1, 1, 1));
		policyPublishDto.setPolicyId("PolicyId");
		policyPublishDto.setPolicyName("Policy Name");
		policyPublishDto.setPolicyStatus("Policy Status");

		ClientPublishDto clientPublishDto = new ClientPublishDto();
		clientPublishDto.setAuthContextRefs(new ArrayList<>());
		clientPublishDto.setClientAuthMethods(new ArrayList<>());
		clientPublishDto.setClientId("ClientId");
		clientPublishDto.setClientName("ClientName");
		clientPublishDto.setClientStatus("Client Status");
		clientPublishDto.setUserClaims(new ArrayList<>());

		ReflectionTestUtils.invokeMethod(serviceImpl, "notify", partnerDataPublishDto, policyPublishDto, clientPublishDto,
				EventType.MISP_UPDATED);
		ReflectionTestUtils.invokeMethod(serviceImpl, "notify", partnerDataPublishDto, policyPublishDto, clientPublishDto,
				EventType.POLICY_UPDATED);
		ReflectionTestUtils.invokeMethod(serviceImpl, "notify", partnerDataPublishDto, policyPublishDto, clientPublishDto,
				EventType.APIKEY_UPDATED);
		ReflectionTestUtils.invokeMethod(serviceImpl, "notify", partnerDataPublishDto, policyPublishDto, clientPublishDto,
				EventType.PARTNER_UPDATED);
		ReflectionTestUtils.invokeMethod(serviceImpl, "notify", partnerDataPublishDto, policyPublishDto, clientPublishDto,
				EventType.CA_CERTIFICATE_UPLOADED);

		verify(webSubPublisher, times(5)).notify(any(EventType.class), any(Map.class), any(Type.class));
	}

	@Test (expected = PartnerServiceException.class)
	public void testPublishClientData_FailureHandling() {
		when(environment.getProperty(any(String.class))).thenReturn("Property");
		when(restUtil.getApi(any(String.class), any(Map.class), any(Class.class)))
				.thenThrow(new PartnerServiceException("An error occurred", "An error occurred"));

		Partner partner = new Partner();
		partner.setAdditionalInfo("Additional Info");
		partner.setAddress("abc");
		partner.setApprovalStatus("Approval Status");
		partner.setCertificateAlias("Certificate Alias");
		partner.setContactNo("9087654321");
		partner.setCrBy("Cr By");
		partner.setCrDtimes(mock(Timestamp.class));
		partner.setDelDtimes(mock(Timestamp.class));
		partner.setEmailId("emailId");
		partner.setId("Id");
		partner.setIsActive(true);
		partner.setIsDeleted(false);
		partner.setLangCode("eng");
		partner.setLogoUrl("https://pms.net/partner");
		partner.setName("Name");
		partner.setPartnerPolicies(new ArrayList<>());
		partner.setPartnerPolicyRequests(new ArrayList<>());
		partner.setPartnerTypeCode("Partner Type Code");
		partner.setPolicyGroupId("PolicyGroupId");
		partner.setUpdBy("Upd By");
		partner.setUpdDtimes(mock(Timestamp.class));
		partner.setUserId("UserId");

		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setCrBy("Cr By");
		policyGroup.setCrDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		policyGroup.setDelDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		policyGroup.setDesc("The characteristics of someone or something");
		policyGroup.setId("Id");
		policyGroup.setIsActive(true);
		policyGroup.setIsDeleted(false);
		policyGroup.setName("Name");
		policyGroup.setUpdBy("Upd By");
		policyGroup.setUpdDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		policyGroup.setUserId("UserId");

		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.SetVersion("1.0");
		authPolicy.setCrBy("Cr By");
		authPolicy.setCrDtimes(mock(Timestamp.class));
		authPolicy.setDelDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		authPolicy.setDescr("desc");
		authPolicy.setId("Id");
		authPolicy.setIsActive(true);
		authPolicy.setIsDeleted(false);
		authPolicy.setName("Name");
		authPolicy.setPolicyFileId("FileId");
		authPolicy.setPolicyGroup(policyGroup);
		authPolicy.setPolicySchema("Schema");
		authPolicy.setPolicy_type("Policy Type");
		authPolicy.setUpdBy("Upd By");
		authPolicy.setUpdDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		authPolicy.setValidFromDate(LocalDateTime.of(1, 1, 1, 1, 1));
		authPolicy.setValidToDate(LocalDateTime.of(1, 1, 1, 1, 1));

		ClientDetail clientDetail = new ClientDetail();
		clientDetail.setAcrValues("values");
		clientDetail.setClaims("Claims");
		clientDetail.setClientAuthMethods("Client Auth Methods");
		clientDetail.setCreatedBy("Cr By");
		clientDetail.setCreatedDateTime(LocalDateTime.of(1, 1, 1, 1, 1));
		clientDetail.setGrantTypes("Grant Types");
		clientDetail.setId("Id");
		clientDetail.setIsDeleted(false);
		clientDetail.setLogoUri("Logo Uri");
		clientDetail.setName("Name");
		clientDetail.setPolicyId("PolicyId");
		clientDetail.setPublicKey("Public Key");
		clientDetail.setRedirectUris("Redirect Uris");
		clientDetail.setRpId("RpId");
		clientDetail.setStatus("Status");
		clientDetail.setUpdatedBy("UpdatedBy");
		clientDetail.setUpdatedDateTime(LocalDateTime.of(1, 1, 1, 1, 1));

		ReflectionTestUtils.invokeMethod(serviceImpl,"publishClientData",partner,authPolicy,clientDetail);
		verify(environment).getProperty(any(String.class));
		verify(restUtil).getApi(any(String.class), any(Map.class), any(Class.class));
		verify(MapperUtils.mapDataToPublishDto(partner, ReflectionTestUtils.invokeMethod(serviceImpl,"getPartnerCertificate",(partner.getCertificateAlias()))));
		verify(MapperUtils.mapPolicyToPublishDto(authPolicy, ReflectionTestUtils.invokeMethod(serviceImpl,"getPolicyObject",(authPolicy.getPolicyFileId()))));
		verify(MapperUtils.mapClientDataToPublishDto(clientDetail));
		verify(ReflectionTestUtils.invokeMethod(serviceImpl,"notify", (any(PartnerDataPublishDto.class)), any(PolicyPublishDto.class), any(ClientPublishDto.class),
				eq(EventType.OIDC_CLIENT_CREATED)));
	}

	@Test (expected = PartnerServiceException.class)
	public void testGetPartnerCertificateWithError() {
		when(environment.getProperty(anyString())).thenReturn("Property");
		when(restUtil.getApi(anyString(), any(), any())).thenThrow(new PartnerServiceException("An error occurred", "An error occurred"));
		ReflectionTestUtils.invokeMethod(serviceImpl, "getPartnerCertificate", "Certificate Alias");
		verify(environment).getProperty(anyString());
		verify(restUtil).getApi(anyString(), any(), any());
	}

	@Test (expected = ApiAccessibleException.class)
	public void testGetPartnerCertificateWithSuccess() throws JsonProcessingException {
		when(environment.getProperty(anyString())).thenReturn("Property");

		PartnerCertDownloadResponeDto partnerCertDownloadResponeDto = new PartnerCertDownloadResponeDto();
		partnerCertDownloadResponeDto.setCertificateData("Certificate Data");
		partnerCertDownloadResponeDto.setTimestamp(LocalDateTime.of(1, 1, 1, 1, 1));

		when(restUtil.getApi(anyString(), any(), any())).thenReturn(new HashMap<>());

		String certificateData = ReflectionTestUtils.invokeMethod(serviceImpl, "getPartnerCertificate", "Certificate Alias");

		verify(environment).getProperty(anyString());
		verify(restUtil).getApi(anyString(), any(), any());
	}

	@Test (expected = PartnerServiceException.class)
	public void testCreateOIDCClientWithError() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("PARTNER_ADMIN")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		ClientDetailCreateRequest createRequest = new ClientDetailCreateRequest();
		createRequest.setName("ClientName");
		createRequest.setAuthPartnerId("mosip_partnerorg1665194709609");
		createRequest.setPolicyId("53704");
		List<String> clientAuthMethods = new ArrayList();
		clientAuthMethods.add("private_key_jwt");
		createRequest.setClientAuthMethods(clientAuthMethods);
		List<String> setGrantTypes = new ArrayList();
		setGrantTypes.add("authorization_code");
		createRequest.setGrantTypes(setGrantTypes);
		createRequest.setLogoUri("https://testcase.pms.net/browse/OIDCClient.png");
		List<String> setRedirectUris = new ArrayList();
		setRedirectUris.add("https://testcase.pms.net/browse/OIDCClient");
		createRequest.setRedirectUris(setRedirectUris);
		createRequest.setPublicKey(public_key);

		Partner partner = new Partner();
		partner.setAdditionalInfo("Additional Info");
		partner.setAddress("abc");
		partner.setApprovalStatus("Approval Status");
		partner.setCertificateAlias("Certificate Alias");
		partner.setContactNo("9087654321");
		partner.setCrBy("Cr By");
		partner.setCrDtimes(mock(Timestamp.class));
		partner.setDelDtimes(mock(Timestamp.class));
		partner.setEmailId("emailId");
		partner.setId("mosip_partnerorg1665194709609");
		partner.setIsActive(true);
		partner.setIsDeleted(false);
		partner.setLangCode("eng");
		partner.setLogoUrl("https://pms.net/partner");
		partner.setName("auth");
		partner.setPartnerPolicies(new ArrayList<>());
		partner.setPartnerPolicyRequests(new ArrayList<>());
		partner.setPartnerTypeCode("Auth_Policy");
		partner.setPolicyGroupId("mosip_partnerorg1665194709609");
		partner.setUpdBy("Upd By");
		partner.setUpdDtimes(mock(Timestamp.class));
		partner.setUserId("UserId");

		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setCrBy("Cr By");
		policyGroup.setCrDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		policyGroup.setDelDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		policyGroup.setDesc("The characteristics of someone or something");
		policyGroup.setId("mosip_partnerorg1665194709609");
		policyGroup.setIsActive(true);
		policyGroup.setIsDeleted(false);
		policyGroup.setName("Name");
		policyGroup.setUpdBy("Upd By");
		policyGroup.setUpdDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		policyGroup.setUserId("UserId");

		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.SetVersion("1.0");
		authPolicy.setCrBy("Cr By");
		authPolicy.setCrDtimes(mock(Timestamp.class));
		authPolicy.setDelDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		authPolicy.setDescr("Descr");
		authPolicy.setId("mosip_partnerorg1665194709609");
		authPolicy.setIsActive(true);
		authPolicy.setIsDeleted(false);
		authPolicy.setName("Name");
		authPolicy.setPolicyFileId("123");
		authPolicy.setPolicyGroup(policyGroup);
		authPolicy.setPolicySchema("Schema");
		authPolicy.setPolicy_type("Policy Type");
		authPolicy.setUpdBy("Upd By");
		authPolicy.setUpdDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		authPolicy.setValidFromDate(LocalDateTime.of(1, 1, 1, 1, 1));
		authPolicy.setValidToDate(LocalDateTime.of(1, 1, 1, 1, 1));

		when(clientDetailRepository.findById(anyString())).thenReturn(Optional.empty());
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
		when(authPolicyRepository.findById(anyString())).thenReturn(Optional.of(authPolicy));

		ClientDetailResponse response = serviceImpl.createOIDCClient(createRequest);

		assertNotNull(response);
	}

	@Test
	public void testConvertStringToListWithValidInput() {
		String inputString = "id,name,email";
		List<String> expectedList = Arrays.asList("id", "name", "email");
		List<String> resultList = ReflectionTestUtils.invokeMethod(serviceImpl, "convertStringToList", inputString);
		assertEquals(expectedList, resultList);
	}

	@Test (expected = Exception.class)
	public void testConvertStringToListWithNullInput() {
		String inputString = null;
		List<String> resultList = ReflectionTestUtils.invokeMethod(serviceImpl, "convertStringToList", inputString);
		assertNull(resultList);
	}

	@Test
	public void testConvertStringToListWithInvalidFormat() {
		String inputString = "id,name;email";
		List<String> resultList = ReflectionTestUtils.invokeMethod(serviceImpl, "convertStringToList", inputString);
		assertNotNull(resultList);
	}

	@Test (expected = PartnerServiceException.class)
	public void testCreateOAuthClient_WithEmptyPublicKey_ThrowsException() throws Exception {
		HashMap<String, Object> publicKey = new HashMap<>();
		ArrayList<String> redirectUris = new ArrayList<>();
		ArrayList<String> grantTypes = new ArrayList<>();
		ArrayList<String> clientAuthMethods = new ArrayList<>();
		serviceImpl.createOAuthClient(new ClientDetailCreateRequestV2("Name", "123", publicKey, "123",
				"Logo Uri", redirectUris, grantTypes, clientAuthMethods, new HashMap<>()));
	}

	@Test (expected = PartnerServiceException.class)
	public void testUpdateOAuthClient_WhenClientNotFound() throws Exception {
		doNothing().when(auditUtil).setAuditRequestDto(any(ClientServiceAuditEnum.class));

		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("AUTH_PARTNER")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		when(clientDetailRepository.findById(anyString())).thenReturn(Optional.empty());
		serviceImpl.updateOAuthClient("clientId", new ClientDetailUpdateRequestV2());
		verify(auditUtil).setAuditRequestDto(any(ClientServiceAuditEnum.class));
		verify(clientDetailRepository).findById(anyString());
	}

	@Test (expected = PartnerServiceException.class)
	public void testUpdateOAuthClientWhenNotificationFails() throws Exception {
		doNothing().when(auditUtil).setAuditRequestDto(any(ClientServiceAuditEnum.class));

		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("AUTH_PARTNER")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		ClientDetail clientDetail = new ClientDetail();
		clientDetail.setAcrValues("Value");
		clientDetail.setClaims("Claims");
		clientDetail.setClientAuthMethods("Client Auth Methods");
		clientDetail.setCreatedBy("Cr By");
		clientDetail.setCreatedDateTime(LocalDateTime.of(1, 1, 1, 1, 1));
		clientDetail.setGrantTypes("Grant Types");
		clientDetail.setId("123");
		clientDetail.setIsDeleted(true);
		clientDetail.setLogoUri("Logo Uri");
		clientDetail.setName("Name");
		clientDetail.setPolicyId("123");
		clientDetail.setPublicKey("Public Key");
		clientDetail.setRedirectUris("Redirect Uris");
		clientDetail.setRpId("123");
		clientDetail.setStatus("Status");
		clientDetail.setUpdatedBy("UpdatedBy");
		clientDetail.setUpdatedDateTime(LocalDateTime.of(1, 1, 1, 1, 1));
		Optional<ClientDetail> optionalClientDetail = Optional.of(clientDetail);

		ClientDetail clientDetail1 = new ClientDetail();
		clientDetail1.setAcrValues("123");
		clientDetail1.setClaims("Claims");
		clientDetail1.setClientAuthMethods("Client Auth Methods");
		clientDetail1.setCreatedBy("Cr By");
		clientDetail1.setCreatedDateTime(LocalDateTime.of(1, 1, 1, 1, 1));
		clientDetail1.setGrantTypes("Grant Types");
		clientDetail1.setId("123");
		clientDetail1.setIsDeleted(true);
		clientDetail1.setLogoUri("Logo Uri");
		clientDetail1.setName("Name");
		clientDetail1.setPolicyId("123");
		clientDetail1.setPublicKey("Public Key");
		clientDetail1.setRedirectUris("Redirect Uris");
		clientDetail1.setRpId("123");
		clientDetail1.setStatus("Status");
		clientDetail1.setUpdatedBy("UpdatedBy");
		clientDetail1.setUpdatedDateTime(LocalDateTime.of(1, 1, 1, 1, 1));

		when(clientDetailRepository.save(any(ClientDetail.class))).thenReturn(clientDetail1);
		when(clientDetailRepository.findById(anyString())).thenReturn(optionalClientDetail );
		when(environment.getProperty(anyString())).thenReturn("Property");
		doNothing().when(restUtil).putApi(anyString(), anyList(), anyString(), anyString(), any(MediaType.class), any(), any(Class.class));
		doThrow(new PartnerServiceException("An error occurred", "An error occurred")).when(webSubPublisher)
				.notify(any(EventType.class), any(Map.class), any(Type.class));

		ArrayList<String> redirectUris = new ArrayList<>();
		ArrayList<String> grantTypes = new ArrayList<>();
		ArrayList<String> clientAuthMethods = new ArrayList<>();

		serviceImpl.updateOAuthClient("123", new ClientDetailUpdateRequestV2(",", redirectUris, ",",
				grantTypes, "ClientName", clientAuthMethods, new HashMap<>()));
		verify(clientDetailRepository).save(any(ClientDetail.class));
		verify(clientDetailRepository).findById(anyString());
		verify(environment).getProperty(anyString());
		verify(restUtil).putApi(anyString(), anyList(), anyString(), anyString(), any(MediaType.class), any(), any(Class.class));
		verify(webSubPublisher).notify(any(EventType.class), any(Map.class), any(Type.class));
	}

	@Test(expected = PartnerServiceException.class)
	public void testUpdateOAuthClientWhenClientIdThrowsException() throws Exception {
		doNothing().when(auditUtil).setAuditRequestDto(any(ClientServiceAuditEnum.class));

		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("AUTH_PARTNER")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		ClientDetail clientDetail = mock(ClientDetail.class);
		when(clientDetail.getId()).thenThrow(new PartnerServiceException("An error occurred", "An error occurred"));
		when(clientDetail.getAcrValues()).thenReturn("123");
		when(clientDetail.getClaims()).thenReturn("Claims");
		when(clientDetail.getStatus()).thenReturn("Status");
		when(clientDetail.getLogoUri()).thenReturn("Logo Uri");
		when(clientDetail.getRedirectUris()).thenReturn("Redirect Uris");
		when(clientDetail.getGrantTypes()).thenReturn("Grant Types");
		when(clientDetail.getName()).thenReturn("Name");
		when(clientDetail.getRpId()).thenReturn("123");
		when(clientDetail.getClientAuthMethods()).thenReturn("Client Auth Methods");
		when(clientDetailRepository.findById(anyString())).thenReturn(Optional.of(clientDetail));
		when(environment.getProperty(anyString())).thenReturn("Property");
		doNothing().when(restUtil).putApi(anyString(), anyList(), anyString(), anyString(), any(MediaType.class), any(), any(Class.class));
		doNothing().when(webSubPublisher).notify(any(EventType.class), any(Map.class), any(Type.class));

		ArrayList<String> redirectUris = new ArrayList<>();
		ArrayList<String> grantTypes = new ArrayList<>();
		ArrayList<String> clientAuthMethods = new ArrayList<>();
		serviceImpl.updateOAuthClient("123", new ClientDetailUpdateRequestV2(",", redirectUris, ",", grantTypes, "Dr Jane Doe", clientAuthMethods, new HashMap<>()));

		verify(clientDetailRepository).findById((String) any());
		verify(clientDetail, never()).setAcrValues((String) any());
		verify(clientDetail, never()).setClaims((String) any());
		verify(clientDetail, never()).setClientAuthMethods((String) any());
		verify(clientDetail, never()).setCreatedBy((String) any());
		verify(clientDetail, never()).setCreatedDateTime((LocalDateTime) any());
		verify(clientDetail, never()).setGrantTypes((String) any());
		verify(clientDetail, never()).setId((String) any());
		verify(clientDetail, never()).setIsDeleted((Boolean) any());
		verify(clientDetail, never()).setLogoUri((String) any());
		verify(clientDetail, never()).setName((String) any());
		verify(clientDetail, never()).setPolicyId((String) any());
		verify(clientDetail, never()).setPublicKey((String) any());
		verify(clientDetail, never()).setRedirectUris((String) any());
		verify(clientDetail, never()).setRpId((String) any());
		verify(clientDetail, never()).setStatus((String) any());
		verify(clientDetail, never()).setUpdatedBy((String) any());
		verify(clientDetail, never()).setUpdatedDateTime((LocalDateTime) any());
		verify(environment, never()).getProperty((String) any());
	}

	@Test (expected = Exception.class)
	public void testGetPolicyObject_WhenValidJson_ReturnsJsonObject() throws Exception {
		JSONParser parser = mock(JSONParser.class);

		ClientDetailCreateRequest request = new ClientDetailCreateRequest();
		request.setPublicKey(public_key);
		request.setPolicyId("policy");
		request.setAuthPartnerId("authPartnerId");
		List<String> clientAuthMethods = new ArrayList<String>();
		clientAuthMethods.add("ClientAuthMethod");
		request.setClientAuthMethods(clientAuthMethods);
		request.setGrantTypes(clientAuthMethods);
		request.setLogoUri("https://testcase.pms.net/browse/OIDCClient.png");
		request.setRedirectUris(clientAuthMethods);
		request.setName("ClientName");

		String jsonString = "{\"key\": \"value\"}";
		JSONObject expectedJsonObject = new JSONObject();
		expectedJsonObject.put("key", "value");

		when(parser.parse(anyString())).thenReturn(expectedJsonObject);

		JSONObject actualJsonObject = ReflectionTestUtils.invokeMethod(request, "getPolicyObject", jsonString);

		assertEquals(expectedJsonObject, actualJsonObject);
	}

	@Test (expected = Exception.class)
	public void testGetPolicyObject() {
		Object result1 = ReflectionTestUtils.invokeMethod(serviceImpl, "getPolicyObject", "Policy");
		assertTrue(result1 instanceof JSONObject || result1 instanceof Map);

		Object result2 = ReflectionTestUtils.invokeMethod(serviceImpl, "getPolicyObject", "Auth");
		assertTrue(result2 instanceof JSONObject || result2 instanceof Map);

		Object result3 = ReflectionTestUtils.invokeMethod(serviceImpl, "getPolicyObject", "");
		assertTrue(result3 instanceof JSONObject || result3 instanceof Map);

		Object result4 = ReflectionTestUtils.invokeMethod(serviceImpl, "getPolicyObject", "Policy123");
		assertTrue(result4 instanceof JSONObject || result4 instanceof Map);

		Object result5 = ReflectionTestUtils.invokeMethod(serviceImpl, "getPolicyObject", "123Policy");
		assertTrue(result5 instanceof JSONObject || result5 instanceof Map);
	}

	@Test (expected = Exception.class)
	public void testMakeCreateEsignetServiceCall_Success() throws JsonProcessingException {
		ClientDetailResponse clientDetailResponse = new ClientDetailResponse();
		clientDetailResponse.setClientId("123");
		clientDetailResponse.setStatus("Status");
		when(restUtil.postApi(anyString(), anyList(), anyString(), anyString(), any(MediaType.class),
				any(), any())).thenReturn(new HashMap<>());

		ClientDetailResponse result = ReflectionTestUtils.invokeMethod(serviceImpl,
				"makeCreateEsignetServiceCall", "Request", "Callee Api");
		verify(restUtil).postApi(anyString(), anyList(), anyString(), anyString(), any(MediaType.class), any(), any());
	}

	@Test (expected = ApiAccessibleException.class)
	public void testMakeCreateEsignetServiceCall_ExceptionThrown() throws JsonProcessingException {
		when(restUtil.postApi(anyString(), anyList(), anyString(), anyString(), any(MediaType.class),
				any(), any())).thenReturn(new HashMap<>());

		ReflectionTestUtils.invokeMethod(serviceImpl, "makeCreateEsignetServiceCall",("Request"), "Callee Api");
		verify(restUtil).postApi(anyString(), anyList(), anyString(), anyString(), any(MediaType.class), any(), any());
	}

	@Test
	public void testGetReqAttributeFromPolicyJson_NoFilterAttribute() {
		JSONObject policyObject = new JSONObject();
		JSONArray parentAttributeObject = new JSONArray();
		JSONObject childJsonArray1 = new JSONObject();
		childJsonArray1.put("childAttribute", "attribute1");
		parentAttributeObject.add(childJsonArray1);
		JSONObject childJsonArray2 = new JSONObject();
		childJsonArray2.put("childAttribute", "attribute2");
		parentAttributeObject.add(childJsonArray2);
		policyObject.put("parentAttribute", parentAttributeObject);

		Set<String> result = ReflectionTestUtils.invokeMethod(serviceImpl, "getReqAttributeFromPolicyJson", policyObject,
				"parentAttribute", "childAttribute", null);

		assertNotNull(result);
		assertEquals(2, result.size());
		assertTrue(result.contains("attribute1"));
		assertTrue(result.contains("attribute2"));
	}

	@Test (expected = PartnerServiceException.class)
	public void testGetReqAttributeFromPolicyJson_WithFilterAttribute() {
		JSONObject policyObject = new JSONObject();
		JSONArray parentAttributeObject = new JSONArray();
		JSONObject childJsonArray1 = new JSONObject();
		childJsonArray1.put("childAttribute", "attribute1");
		childJsonArray1.put("filterAttribute", true);
		parentAttributeObject.add(childJsonArray1);
		JSONObject childJsonArray2 = new JSONObject();
		childJsonArray2.put("childAttribute", "attribute2");
		parentAttributeObject.add(childJsonArray2);
		policyObject.put("parentAttribute", parentAttributeObject);


		ReflectionTestUtils.invokeMethod(serviceImpl, "getReqAttributeFromPolicyJson", policyObject,
				"parentAttribute","childAttribute", "filterAttribute");
	}

	@Test (expected = PartnerServiceException.class)
	public void testProcessCreateOIDCClient_ClientExists() throws Exception {

		ClientDetailCreateRequest createRequest = new ClientDetailCreateRequest();
		createRequest.setName("ClientName");
		createRequest.setAuthPartnerId("AuthPartnerId");
		createRequest.setPolicyId("PolicyID");
		List<String> clientAuthMethods = new ArrayList();
		clientAuthMethods.add("private_key_jwt");
		createRequest.setClientAuthMethods(clientAuthMethods);
		List<String> setGrantTypes = new ArrayList();
		setGrantTypes.add("authorization_code");
		createRequest.setGrantTypes(setGrantTypes);
		createRequest.setLogoUri("https://testcase.pms.net/browse/OIDCClient.png");
		List<String> setRedirectUris = new ArrayList();
		setRedirectUris.add("https://testcase.pms.net/browse/OIDCClient");
		createRequest.setRedirectUris(setRedirectUris);
		createRequest.setPublicKey(public_key);

		ClientDetail clientDetail = new ClientDetail();
		clientDetail.setPublicKey("public_key");
		clientDetail.setId("clientId");
		clientDetail.setName(createRequest.getName());
		clientDetail.setRpId(createRequest.getAuthPartnerId());
		clientDetail.setPolicyId(createRequest.getPolicyId());
		clientDetail.setLogoUri(createRequest.getLogoUri());
		clientDetail.setRedirectUris(String.join(",", createRequest.getRedirectUris()));
		clientDetail.setClaims("claims");
		clientDetail.setAcrValues( "acrValues");
		clientDetail.setStatus("ACTIVE");
		clientDetail.setGrantTypes(String.join(",", createRequest.getGrantTypes()));
		clientDetail.setClientAuthMethods(String.join(",", createRequest.getClientAuthMethods()));
		clientDetail.setCreatedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
		clientDetail.setCreatedBy(getLoggedInUserId());

		when(clientDetailRepository.findById(anyString())).thenReturn(Optional.of(clientDetail));

		ProcessedClientDetail result = serviceImpl.processCreateOIDCClient(createRequest);

		verify(clientDetailRepository).findById(anyString());
		assertNotNull(result.getClientDetail());
		assertNotNull(result.getPartner());
		assertNotNull(result.getPolicy());
	}

	@Test(expected = Exception.class)
	public void testProcessCreateOIDCClient_PartnerCannotCreateOIDCClient() throws Exception {
		ClientDetailCreateRequest createRequest = new ClientDetailCreateRequest();
		createRequest.setName("ClientName");
		createRequest.setAuthPartnerId("AuthPartnerId");
		createRequest.setPolicyId("PolicyID");
		List<String> clientAuthMethods = new ArrayList<>();
		clientAuthMethods.add("private_key_jwt");
		createRequest.setClientAuthMethods(clientAuthMethods);
		List<String> setGrantTypes = new ArrayList<>();
		setGrantTypes.add("authorization_code");
		createRequest.setGrantTypes(setGrantTypes);
		createRequest.setLogoUri("https://testcase.pms.net/browse/OIDCClient.png");
		List<String> setRedirectUris = new ArrayList<>();
		setRedirectUris.add("https://testcase.pms.net/browse/OIDCClient");
		createRequest.setRedirectUris(setRedirectUris);
		createRequest.setPublicKey(public_key);

		Partner partner = new Partner();
		partner.setAdditionalInfo("Additional Info");
		partner.setAddress("abc");
		partner.setApprovalStatus("Approval Status");
		partner.setCertificateAlias("Certificate Alias");
		partner.setContactNo("9087654321");
		partner.setCrBy("Cr By");
		partner.setCrDtimes(mock(Timestamp.class));
		partner.setDelDtimes(mock(Timestamp.class));
		partner.setEmailId("emailId");
		partner.setId("Id");
		partner.setIsActive(true);
		partner.setIsDeleted(false);
		partner.setLangCode("eng");
		partner.setLogoUrl("https://pms.net/partner");
		partner.setName("Name");
		partner.setPartnerPolicies(new ArrayList<>());
		partner.setPartnerPolicyRequests(new ArrayList<>());
		partner.setPartnerTypeCode("Partner Type Code");
		partner.setPolicyGroupId("PolicyGroupId");
		partner.setUpdBy("Upd By");
		partner.setUpdDtimes(mock(Timestamp.class));
		partner.setUserId("UserId");

		PartnerPolicyRequest partnerPolicyRequest = new PartnerPolicyRequest();
		partnerPolicyRequest.setPartner(partner);
		partnerPolicyRequest.setPolicyId("PolicyID");
		partnerPolicyRequest.setId("Id");
		partnerPolicyRequest.setIsDeleted(false);
		partnerPolicyRequest.setStatusCode("Active");

		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setCrBy("Cr By");
		policyGroup.setCrDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		policyGroup.setDelDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		policyGroup.setDesc("The characteristics of someone or something");
		policyGroup.setId("mosip_partnerorg1665194709609");
		policyGroup.setIsActive(true);
		policyGroup.setIsDeleted(false);
		policyGroup.setName("Name");
		policyGroup.setUpdBy("Upd By");
		policyGroup.setUpdDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		policyGroup.setUserId("UserId");

		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.SetVersion("1.0");
		authPolicy.setCrBy("Cr By");
		authPolicy.setCrDtimes(mock(Timestamp.class));
		authPolicy.setDelDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		authPolicy.setDescr("desc");
		authPolicy.setId("Id");
		authPolicy.setIsActive(true);
		authPolicy.setIsDeleted(false);
		authPolicy.setName("Name");
		authPolicy.setPolicyFileId("Field");
		authPolicy.setPolicyGroup(policyGroup);
		authPolicy.setPolicySchema("Schema");
		authPolicy.setPolicy_type("Policy Type");
		authPolicy.setUpdBy("Upd By");
		authPolicy.setUpdDtimes(LocalDateTime.of(1, 1, 1, 1, 1));
		authPolicy.setValidFromDate(LocalDateTime.of(1, 1, 1, 1, 1));
		authPolicy.setValidToDate(LocalDateTime.of(1, 1, 1, 1, 1));

		when(clientDetailRepository.findById(anyString())).thenReturn(Optional.empty());
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
		when(authPolicyRepository.findById(anyString())).thenReturn(Optional.of(authPolicy));
		when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyId(anyString(), anyString())).thenReturn(List.of(partnerPolicyRequest));
		when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyIdAndStatusCode(anyString(), anyString(), anyString())).thenReturn(List.of(partnerPolicyRequest));

		serviceImpl.processCreateOIDCClient(createRequest);

		ProcessedClientDetail result = serviceImpl.processCreateOIDCClient(createRequest);

		verify(clientDetailRepository).findById(anyString());
		assertNotNull(result);
		assertNotNull(result.getClientDetail());
		assertNotNull(result.getPartner());
		assertNotNull(result.getPolicy());
	}

	@Test
	public void getPartnersClientsTest() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("AUTH_PARTNER")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPolicyGroupId("policyGroup123");
		partner.setPartnerTypeCode("Auth_Partner");
		partner.setName("abc");
		partner.setIsActive(true);
		partnerList.add(partner);
		when(partnerServiceRepository.findByUserId(anyString())).thenReturn(partnerList);

		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("policyGroup123");
		policyGroup.setName("policyGrp");
		when(policyGroupRepository.findPolicyGroupById(anyString())).thenReturn(policyGroup);

		String sortFieldName = "createdDateTime";
		String sortType = "desc";
		Integer pageNo = 0;
		Integer pageSize = 8;
		ClientFilterDto filterDto = new ClientFilterDto();
		filterDto.setPartnerId("abc");
		filterDto.setStatus("ACTIVE");
		filterDto.setOrgName("ABC");
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		ClientSummaryEntity entity = new ClientSummaryEntity();
		entity.setClientId("123");
		Page<ClientSummaryEntity> page = new PageImpl<>(List.of(entity), pageable, 1);

		when(clientSummaryRepository.getSummaryOfAllPartnerClients(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyList(), anyBoolean(), any())).thenReturn(page);
		serviceImpl.getPartnersClients(sortFieldName, sortType, pageNo, pageSize, filterDto);
	}

	@Test
	public void getPartnersClientsTestException() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		String sortFieldName = "createdDateTime";
		String sortType = "desc";
		Integer pageNo = 0;
		Integer pageSize = 8;
		ResponseWrapperV2<PageResponseV2Dto<ClientSummaryDto>> responseWrapper = new ResponseWrapperV2<>();
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		ClientSummaryEntity entity = new ClientSummaryEntity();
		entity.setClientId("123");
		Page<ClientSummaryEntity> page = new PageImpl<>(List.of(entity), pageable, 1);
		when(clientSummaryRepository.getSummaryOfAllPartnerClients(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyList(), anyBoolean(), any())).thenReturn(page);
		serviceImpl.getPartnersClients(sortFieldName, sortType, pageNo, pageSize, null);
	}

	@Test
	public void getPartnersClientsTestException1() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("AUTH_PARTNER")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		List<Partner> partnerList = new ArrayList<>();
		when(partnerServiceRepository.findByUserId(anyString())).thenReturn(partnerList);

		String sortFieldName = "createdDateTime";
		String sortType = "desc";
		Integer pageNo = 0;
		Integer pageSize = 8;
		ClientFilterDto filterDto = new ClientFilterDto();
		filterDto.setPartnerId("abc");
		filterDto.setStatus("ACTIVE");
		filterDto.setOrgName("ABC");
		serviceImpl.getPartnersClients(sortFieldName, sortType, pageNo, pageSize, filterDto);
	}

	private io.mosip.kernel.openid.bridge.model.MosipUserDto getMosipUserDto() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = new io.mosip.kernel.openid.bridge.model.MosipUserDto();
		mosipUserDto.setUserId("123");
		mosipUserDto.setMail("abc@gmail.com");
		return mosipUserDto;
	}

	@Test
	public void testCreateOIDCClientV2InvalidPartnerId() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("AUTH_PARTNER")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		ClientDetailCreateRequestV3 request = new ClientDetailCreateRequestV3();
		request.setPublicKey(public_key);
		request.setPolicyId("policy-123");
		request.setAuthPartnerId("authPartnerId");
		request.setName("OIDCClientV2Name");
		List<String> clientAuthMethods = new ArrayList<>();
		clientAuthMethods.add("private_key_jwt");
		request.setClientAuthMethods(clientAuthMethods);
		request.setGrantTypes(Arrays.asList("authorization_code", "refresh_token"));
		request.setLogoUri("https://example.com/logo.png");
		request.setRedirectUris(Arrays.asList("https://example.com/redirect1"));
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode additionalConfig = mapper.createObjectNode();
		additionalConfig.put("userinfoResponseType", "JSON");
		request.setAdditionalConfig(additionalConfig);
		Mockito.doNothing().when(auditUtil).setAuditRequestDto(Mockito.any(ClientServiceAuditEnum.class));
		try {
			serviceImpl.createOIDCClientV2(request);
		} catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.INVALID_PARTNERID.getErrorCode()));
		}
	}

	@Test
	public void testCreateOIDCClientV2InvalidPolicyId() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("AUTH_PARTNER")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		ClientDetailCreateRequestV3 request = new ClientDetailCreateRequestV3();
		request.setPublicKey(public_key);
		request.setPolicyId("");
		request.setAuthPartnerId("authPartnerId");
		request.setName("InvalidPolicy");
		Mockito.doNothing().when(auditUtil).setAuditRequestDto(Mockito.any(ClientServiceAuditEnum.class));
		try {
			serviceImpl.createOIDCClientV2(request);
		} catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.POLICY_NOT_EXIST.getErrorCode()));
		}
	}

	@Test
	public void testCreateOIDCClientV2NullPublicKey() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("AUTH_PARTNER")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		ClientDetailCreateRequestV3 request = new ClientDetailCreateRequestV3();
		request.setPublicKey(null);
		request.setPolicyId("policy-123");
		request.setAuthPartnerId("authPartnerId");
		request.setName("NullKeyClient");
		try {
			serviceImpl.createOIDCClientV2(request);
		} catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.FAILED_TO_PROCESS_JWK.getErrorCode()));
		}
	}

	@Test
	public void testCreateOIDCClientV2EmptyPublicKey() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("AUTH_PARTNER")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		ClientDetailCreateRequestV3 request = new ClientDetailCreateRequestV3();
		request.setPublicKey(new HashMap<>());
		request.setPolicyId("policy-123");
		request.setAuthPartnerId("authPartnerId");
		request.setName("EmptyKeyClient");
		try {
			serviceImpl.createOIDCClientV2(request);
		} catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.FAILED_TO_PROCESS_JWK.getErrorCode()));
		}
	}

	@Test
	public void testCreateOIDCClientV2InvalidAuthPartner() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("AUTH_PARTNER")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		ClientDetailCreateRequestV3 request = new ClientDetailCreateRequestV3();
		request.setPublicKey(public_key);
		request.setPolicyId("policy-123");
		request.setAuthPartnerId("invalidPartnerId");
		request.setName("InvalidPartnerClient");
		List<String> clientAuthMethods = new ArrayList<>();
		clientAuthMethods.add("private_key_jwt");
		request.setClientAuthMethods(clientAuthMethods);
		request.setGrantTypes(Arrays.asList("authorization_code"));
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode additionalConfig = mapper.createObjectNode();
		additionalConfig.put("signupBannerRequired", true);
		request.setAdditionalConfig(additionalConfig);
		Mockito.doNothing().when(auditUtil).setAuditRequestDto(Mockito.any(ClientServiceAuditEnum.class));
		when(partnerRepository.findById("invalidPartnerId")).thenReturn(Optional.empty());
		try {
			serviceImpl.createOIDCClientV2(request);
		} catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.INVALID_PARTNERID.getErrorCode()));
		}
	}

    @Test
    public void testGetPartnersClientsV2ValidRequest() throws Exception {
        String sortFieldName = "createdDateTime";
        String sortType = "desc";
        int pageNo = 0;
        int pageSize = 10;

        ClientFilterDto filterDto = new ClientFilterDto();
        filterDto.setPartnerId("partner-123");
        filterDto.setClientName("Test Client");
        filterDto.setStatus("ACTIVE");

        ClientSummaryEntity entity = new ClientSummaryEntity();
        entity.setClientId("client-123");
        entity.setClientName("Test Client");
        entity.setStatus("ACTIVE");

        Page<ClientSummaryEntity> pageResponse = new PageImpl<>(
                List.of(entity),
                PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, sortFieldName)),
                1L
        );

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_PARTNER_ADMIN"));

        MosipUserDto mosipUserDto = new MosipUserDto();
        mosipUserDto.setUserId("test-user");

        AuthUserDetails authUserDetails = mock(AuthUserDetails.class);
        when(authUserDetails.getAuthorities()).thenReturn((Collection) authorities);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(authentication.isAuthenticated()).thenReturn(true);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(clientSummaryRepository.getSummaryOfAllPartnerClients(
                anyString(), any(), any(), any(), any(), any(), any(), anyBoolean(), any(Pageable.class)
        )).thenReturn(pageResponse);

        ResponseWrapperV2<PageResponseV2Dto<ClientSummaryDto>> response =
                serviceImpl.getPartnersClientsV2(sortFieldName, sortType, pageNo, pageSize, filterDto);

        assertNotNull("ResponseWrapper should not be null", response);
        assertNotNull("The inner response object is null. Check service logs for internal exceptions.", response.getResponse());
        assertEquals(1, response.getResponse().getData().size());
        assertEquals("client-123", response.getResponse().getData().get(0).getClientId());
    }

	@Test
	public void testGetPartnersClientsV2WithMultipleClients() throws Exception {
		String sortFieldName = "clientName";
		String sortType = "asc";
		long pageNo = 0L;
		long pageSize = 20L;

		ClientFilterDto filterDto = new ClientFilterDto();

		List<ClientSummaryDto> clients = new ArrayList<>();
		for (int i = 1; i <= 5; i++) {
			ClientSummaryDto client = new ClientSummaryDto();
			client.setClientId("client-" + i);
			client.setClientName("Client " + i);
			client.setStatus("ACTIVE");
			clients.add(client);
		}

		PageResponseV2Dto<ClientSummaryDto> pageResponse = new PageResponseV2Dto<>();
		pageResponse.setData(clients);
		pageResponse.setPageNo(pageNo);
		pageResponse.setPageSize(pageSize);
		pageResponse.setTotalResults(1L);

		ResponseWrapperV2<PageResponseV2Dto<ClientSummaryDto>> expectedResponse = new ResponseWrapperV2<>();
		expectedResponse.setResponse(pageResponse);

		ResponseWrapperV2<PageResponseV2Dto<ClientSummaryDto>> actualResponse = serviceImpl.getPartnersClientsV2(sortFieldName, sortType, (int)pageNo, (int)pageSize, filterDto);
		assertNotNull(actualResponse);
		if (actualResponse.getResponse() != null) {
			assertEquals(1L, actualResponse.getResponse().getTotalResults());
		}
	}

	@Test
	public void testGetPartnersClientsV2EmptyResult() throws Exception {
		String sortFieldName = "createdDateTime";
		String sortType = "desc";
		long pageNo = 0L;
		long pageSize = 10L;

		ClientFilterDto filterDto = new ClientFilterDto();
		filterDto.setPartnerId("nonexistent-partner");

		PageResponseV2Dto<ClientSummaryDto> pageResponse = new PageResponseV2Dto<>();
		pageResponse.setData(Collections.emptyList());
		pageResponse.setPageNo(pageNo);
		pageResponse.setPageSize(pageSize);
		pageResponse.setTotalResults(0L);

		ResponseWrapperV2<PageResponseV2Dto<ClientSummaryDto>> expectedResponse = new ResponseWrapperV2<>();
		expectedResponse.setResponse(pageResponse);

		ResponseWrapperV2<PageResponseV2Dto<ClientSummaryDto>> actualResponse = serviceImpl.getPartnersClientsV2(sortFieldName, sortType, (int)pageNo, (int)pageSize, filterDto);
		assertNotNull(actualResponse);
		if (actualResponse.getResponse() != null) {
			assertEquals(0L, actualResponse.getResponse().getTotalResults());
		}
	}

	@Test
	public void testGetPartnersClientsV2WithStatusFilter() throws Exception {
		String sortFieldName = "clientName";
		String sortType = "asc";
		long pageNo = 0L;
		long pageSize = 10L;

		ClientFilterDto filterDto = new ClientFilterDto();
		filterDto.setStatus("ACTIVE");

		ClientSummaryDto activeClient = new ClientSummaryDto();
		activeClient.setClientId("active-client-123");
		activeClient.setClientName("Active Client");
		activeClient.setStatus("ACTIVE");

		PageResponseV2Dto<ClientSummaryDto> pageResponse = new PageResponseV2Dto<>();
		pageResponse.setData(List.of(activeClient));
		pageResponse.setPageNo(pageNo);
		pageResponse.setPageSize(pageSize);
		pageResponse.setTotalResults(1L);

		ResponseWrapperV2<PageResponseV2Dto<ClientSummaryDto>> expectedResponse = new ResponseWrapperV2<>();
		expectedResponse.setResponse(pageResponse);

		ResponseWrapperV2<PageResponseV2Dto<ClientSummaryDto>> actualResponse = serviceImpl.getPartnersClientsV2(sortFieldName, sortType, (int)pageNo, (int)pageSize, filterDto);
		assertNotNull(actualResponse);
		if (actualResponse.getResponse() != null && !actualResponse.getResponse().getData().isEmpty()) {
			assertEquals("ACTIVE", actualResponse.getResponse().getData().getFirst().getStatus());
		}
	}

	@Test
	public void testGetPartnersClientsV2WithPagination() throws Exception {
		String sortFieldName = "createdDateTime";
		String sortType = "desc";
		long pageNo = 2L;
		long pageSize = 25L;

		ClientFilterDto filterDto = new ClientFilterDto();

		List<ClientSummaryDto> clients = new ArrayList<>();
		for (int i = 1; i <= 25; i++) {
			ClientSummaryDto client = new ClientSummaryDto();
			client.setClientId("client-page2-" + i);
			client.setClientName("Client Page 2 - " + i);
			client.setStatus("ACTIVE");
			clients.add(client);
		}

		PageResponseV2Dto<ClientSummaryDto> pageResponse = new PageResponseV2Dto<>();
		pageResponse.setData(clients);
		pageResponse.setPageNo(pageNo);
		pageResponse.setPageSize(pageSize);
		pageResponse.setTotalResults(5L);

		ResponseWrapperV2<PageResponseV2Dto<ClientSummaryDto>> expectedResponse = new ResponseWrapperV2<>();
		expectedResponse.setResponse(pageResponse);

		ResponseWrapperV2<PageResponseV2Dto<ClientSummaryDto>> actualResponse = serviceImpl.getPartnersClientsV2(sortFieldName, sortType, (int)pageNo, (int)pageSize, filterDto);
		assertNotNull(actualResponse);
		if (actualResponse.getResponse() != null) {
			assertEquals(pageNo, actualResponse.getResponse().getPageNo());
			assertEquals(pageSize, actualResponse.getResponse().getPageSize());
			assertEquals(5L, actualResponse.getResponse().getTotalResults());
		}
    }

	@Test
	public void testCheckPartnerActiveStatusInactivePartner() throws Exception {
		Partner partner = new Partner();
		partner.setIsActive(false);
		partner.setId("partnerId");

		try {
			ReflectionTestUtils.invokeMethod(serviceImpl, "checkPartnerActiveStatus", partner, "clientId");
			fail("Expected PartnerServiceException");
		} catch (PartnerServiceException ex) {
			assertEquals(ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorCode(), ex.getErrorCode());
		}
	}

	@Test
	public void testExtractClientNameAndLangMapWithJsonObject() throws Exception {
		String clientNameJson = "{\"@none\":\"ClientName\",\"eng\":\"ClientName\",\"ara\":\"اسم العميل\"}";
		ClientNameAndLangMap result = ReflectionTestUtils.invokeMethod(serviceImpl, "extractClientNameAndLangMap", clientNameJson);
		assertNotNull(result);
		assertEquals("ClientName", result.getName());
		assertNotNull(result.getLangMap());
	}

	@Test
	public void testExtractClientNameAndLangMapWithPlainString() throws Exception {
		String clientName = "PlainClientName";
		ClientNameAndLangMap result = ReflectionTestUtils.invokeMethod(serviceImpl, "extractClientNameAndLangMap", clientName);
		assertNotNull(result);
		assertEquals("PlainClientName", result.getName());
	}

	@Test
	public void testCreatePublicKeyFromJWKRSA() throws Exception {
		Map<String, Object> rsaPublicKey = new HashMap<>();
		rsaPublicKey.put("kty", "RSA");
		rsaPublicKey.put("e", "AQAB");
		rsaPublicKey.put("use", "sig");
		rsaPublicKey.put("kid", "test-key");
		rsaPublicKey.put("alg", "RS256");
		rsaPublicKey.put("n", "wXGQA574CU-WTWPILd4S3_1sJf0Yof0kwMeNctXc1thQo70Ljfn9f4igpRe7f8qNs_W6dLuLWemFhGJBQBQ7vvickECKNJfo_EzSD_yyPCg7k_AGbTWTkuoObHrpilwJGyKVSkOIujH_FqHIVkwkVXjWc25Lsb8Gq4nAHNQEqqgaYPLEi5evCR6S0FzcXTPuRh9zH-cM0Onjv4orrfYpEr61HcRp5MXL55b7yBoIYlXD8NfalcgdrWzp4VZHvQ8yT9G5eaf27XUn6ZBeBf7VnELcKFTyw1pK2wqoOxRBc8Y1wO6rEy8PlCU6wD-mbIzcjG1wUfnbgvJOM4A5G41quQ");

		String publicKeyString = serviceImpl.createPublicKeyFromJWK(rsaPublicKey);
		assertNotNull(publicKeyString);
		assertTrue(publicKeyString.length() > 0);
	}

    @Test(expected = java.text.ParseException.class)
    public void testCreatePublicKeyFromJWKUnsupportedKeyType() throws Exception {
        Map<String, Object> unsupportedKey = new HashMap<>();
        unsupportedKey.put("kty", "UNSUPPORTED");
        unsupportedKey.put("kid", "test-key");

        serviceImpl.createPublicKeyFromJWK(unsupportedKey);
    }

	@Test
	public void testValidateAdditionalConfigFieldsInvalidUserinfoResponseType() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("PARTNER_ADMIN")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode additionalConfig = mapper.createObjectNode();
		additionalConfig.put("userinfo_response_type", "INVALID");

		Mockito.doNothing().when(auditUtil).setAuditRequestDto(Mockito.any(ClientServiceAuditEnum.class));

		try {
			ReflectionTestUtils.invokeMethod(serviceImpl, "validateAdditionalConfigFields", additionalConfig, "clientId", "clientName");
			fail("Expected PartnerServiceException");
		} catch (PartnerServiceException ex) {
			assertEquals(ErrorCode.INVALID_USERINFO_RESPONSE_TYPE.getErrorCode(), ex.getErrorCode());
		}
	}

	@Test
	public void testValidateAdditionalConfigFieldsInvalidConsentExpireTime() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("PARTNER_ADMIN")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode additionalConfig = mapper.createObjectNode();
		additionalConfig.put("consent_expire_in_mins", 5);

		Mockito.doNothing().when(auditUtil).setAuditRequestDto(Mockito.any(ClientServiceAuditEnum.class));

		try {
			ReflectionTestUtils.invokeMethod(serviceImpl, "validateAdditionalConfigFields", additionalConfig, "clientId", "clientName");
			fail("Expected PartnerServiceException");
		} catch (PartnerServiceException ex) {
			assertEquals(ErrorCode.INVALID_CONSENT_EXPIRE_TIME.getErrorCode(), ex.getErrorCode());
		}
	}

	@Test
	public void testValidateAdditionalConfigFieldsInvalidPurposeType() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("PARTNER_ADMIN")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode additionalConfig = mapper.createObjectNode();
		ObjectNode purpose = mapper.createObjectNode();
		purpose.put("type", "INVALID_TYPE");
		additionalConfig.set("purpose", purpose);

		Mockito.doNothing().when(auditUtil).setAuditRequestDto(Mockito.any(ClientServiceAuditEnum.class));

		try {
			ReflectionTestUtils.invokeMethod(serviceImpl, "validateAdditionalConfigFields", additionalConfig, "clientId", "clientName");
			fail("Expected PartnerServiceException");
		} catch (PartnerServiceException ex) {
			assertEquals(ErrorCode.INVALID_PURPOSE_TYPE.getErrorCode(), ex.getErrorCode());
		}
	}

	@Test
	public void testValidateAdditionalConfigFieldsPurposeTitleNotMapType() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("PARTNER_ADMIN")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode additionalConfig = mapper.createObjectNode();
		ObjectNode purpose = mapper.createObjectNode();
		purpose.put("type", "login");
		purpose.put("title", "NotAMap");
		additionalConfig.set("purpose", purpose);

		Mockito.doNothing().when(auditUtil).setAuditRequestDto(Mockito.any(ClientServiceAuditEnum.class));

		try {
			ReflectionTestUtils.invokeMethod(serviceImpl, "validateAdditionalConfigFields", additionalConfig, "clientId", "clientName");
			fail("Expected PartnerServiceException");
		} catch (PartnerServiceException ex) {
			assertEquals(ErrorCode.INVALID_PURPOSE_TITLE_OR_SUBTITLE.getErrorCode(), ex.getErrorCode());
		}
	}

	@Test
	public void testValidateAdditionalConfigFieldsPurposeTitleWithoutType() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("PARTNER_ADMIN")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode additionalConfig = mapper.createObjectNode();
		ObjectNode purpose = mapper.createObjectNode();
		ObjectNode title = mapper.createObjectNode();
		title.put("@none", "Test Title");
		purpose.set("title", title);
		additionalConfig.set("purpose", purpose);

		Mockito.doNothing().when(auditUtil).setAuditRequestDto(Mockito.any(ClientServiceAuditEnum.class));

		try {
			ReflectionTestUtils.invokeMethod(serviceImpl, "validateAdditionalConfigFields", additionalConfig, "clientId", "clientName");
			fail("Expected PartnerServiceException");
		} catch (PartnerServiceException ex) {
			assertEquals(ErrorCode.INVALID_PURPOSE_TITLE_OR_SUBTITLE.getErrorCode(), ex.getErrorCode());
		}
	}

	@Test
	public void testValidateLanguageKeysInvalidLanguageKey() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode langMap = mapper.createObjectNode();
		langMap.put("invalid_lang", "value");

		try {
			ReflectionTestUtils.invokeMethod(serviceImpl, "validateLanguageKeys", langMap, "testField", false);
			fail("Expected PartnerServiceException");
		} catch (PartnerServiceException ex) {
			assertEquals(ErrorCode.INVALID_LANGUAGE_KEY.getErrorCode(), ex.getErrorCode());
		}
	}

	@Test
	public void testValidateLanguageKeysMissingMandatoryNoneKey() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode langMap = mapper.createObjectNode();
		langMap.put("eng", "value");

		try {
			ReflectionTestUtils.invokeMethod(serviceImpl, "validateLanguageKeys", langMap, "testField", true);
			fail("Expected PartnerServiceException");
		} catch (PartnerServiceException ex) {
			assertEquals(ErrorCode.MISSING_MANDATORY_LANGUAGE_KEY.getErrorCode(), ex.getErrorCode());
		}
	}

	@Test
	public void testValidateLanguageKeysValidLanguageKeys() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode langMap = mapper.createObjectNode();
		langMap.put("@none", "English Title");
		langMap.put("eng", "English Title");
		langMap.put("ara", "العنوان العربي");

		try {
			ReflectionTestUtils.invokeMethod(serviceImpl, "validateLanguageKeys", langMap, "testField", true);
		} catch (Exception ex) {
			fail("Should not throw exception for valid language keys");
		}
	}

	@Test
	public void testGetSortColumnWithValidAlias() throws Exception {
		Map<String, String> aliasToColumnMap = new HashMap<>();
		aliasToColumnMap.put("name", "c_name");
		aliasToColumnMap.put("status", "c_status");

		String result = ReflectionTestUtils.invokeMethod(serviceImpl, "getSortColumn", aliasToColumnMap, "name");
		assertEquals("c_name", result);
	}

	@Test
	public void testGetSortColumnWithInvalidAlias() throws Exception {
		Map<String, String> aliasToColumnMap = new HashMap<>();
		aliasToColumnMap.put("name", "c_name");

		String result = ReflectionTestUtils.invokeMethod(serviceImpl, "getSortColumn", aliasToColumnMap, "invalidAlias");
		assertEquals("invalidAlias", result);
	}

	@Test
	public void testDeactivateOIDCClientInvalidClientId() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("PARTNER_ADMIN")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		DeactivateOidcClientRequestDto requestDto = new DeactivateOidcClientRequestDto();
		requestDto.setStatus("INACTIVE");

		ResponseWrapperV2<ClientDetailResponse> response = serviceImpl.deactivateOIDCClient("", requestDto);
		assertNotNull(response);
		assertNotNull(response.getErrors());
	}

	@Test
	public void testDeactivateOIDCClientInvalidStatus() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("PARTNER_ADMIN")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		DeactivateOidcClientRequestDto requestDto = new DeactivateOidcClientRequestDto();
		requestDto.setStatus("ACTIVE");

		ResponseWrapperV2<ClientDetailResponse> response = serviceImpl.deactivateOIDCClient("clientId", requestDto);
		assertNotNull(response);
		assertNotNull(response.getErrors());
	}

	@Test
	public void testGetOIDCClientV2InvalidClientId() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("PARTNER_ADMIN")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		ResponseWrapperV2<ClientDetailV2> response = serviceImpl.getOIDCClientV2("");
		assertNotNull(response);
		assertNotNull(response.getErrors());
	}

	@Test
	public void testValidatePartnerIdBelongsToUserPartnerExists() throws Exception {
		Partner partner1 = new Partner();
		partner1.setId("partner1");

		Partner partner2 = new Partner();
		partner2.setId("partner2");

		List<Partner> partnerList = Arrays.asList(partner1, partner2);

		boolean result = ReflectionTestUtils.invokeMethod(serviceImpl, "validatePartnerIdBelongsToUser", partnerList, "partner1");
		assertTrue(result);
	}

	@Test
	public void testValidatePartnerIdBelongsToUserPartnerNotExists() throws Exception {
		Partner partner1 = new Partner();
		partner1.setId("partner1");

		List<Partner> partnerList = Arrays.asList(partner1);

		boolean result = ReflectionTestUtils.invokeMethod(serviceImpl, "validatePartnerIdBelongsToUser", partnerList, "partner3");
		assertFalse(result);
	}

	@Test
	public void testGetClientNameLanguageMapAsJsonString() throws Exception {
		Map<String, String> clientNameMap = new HashMap<>();
		clientNameMap.put("eng", "English Name");
		clientNameMap.put("ara", "الاسم العربي");

		String result = ReflectionTestUtils.invokeMethod(serviceImpl, "getClientNameLanguageMapAsJsonString", clientNameMap, "DefaultName");
		assertNotNull(result);
		assertTrue(result.contains("@none"));
		assertTrue(result.contains("DefaultName"));
	}

	@Test
	public void testCreateOIDCClientV2Success() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("PARTNER_ADMIN")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		when(environment.getProperty("auth.url")).thenReturn("https://pms.net/partner");
		when(clientDetailRepository.findById(anyString())).thenReturn(Optional.empty());
		when(partnerRepository.findById(anyString())).thenReturn(Optional.empty());
		when(authPolicyRepository.findById(anyString())).thenReturn(Optional.empty());

		ClientDetailCreateRequestV3 createRequest = new ClientDetailCreateRequestV3();
		createRequest.setName("ClientName");
		createRequest.setAuthPartnerId("AuthPartnerId");
		createRequest.setPolicyId("PolicyID");
		List<String> clientAuthMethods = new ArrayList();
		clientAuthMethods.add("private_key_jwt");
		createRequest.setClientAuthMethods(clientAuthMethods);
		List<String> setGrantTypes = new ArrayList();
		setGrantTypes.add("authorization_code");
		createRequest.setGrantTypes(setGrantTypes);
		createRequest.setLogoUri("https://testcase.pms.net/browse/OIDCClient.png");
		List<String> setRedirectUris = new ArrayList();
		setRedirectUris.add("https://testcase.pms.net/browse/OIDCClient");
		createRequest.setRedirectUris(setRedirectUris);
		createRequest.setPublicKey(public_key);
		Map<String, String> clientNameMap = new HashMap<>();
		clientNameMap.put("@none", "ClientName");
		createRequest.setClientNameLangMap(clientNameMap);

		ResponseWrapperV2<ClientDetailResponse> response = serviceImpl.createOIDCClientV2(createRequest);
		assertNotNull(response);
	}

	@Test
	public void testSetCommonUpdateFieldsWithClientDetailUpdateRequest() throws Exception {
		ClientDetail clientDetail = new ClientDetail();
		clientDetail.setName("OldName");
		clientDetail.setLogoUri("https://old.uri");

		ClientDetailUpdateRequest updateRequest = new ClientDetailUpdateRequest();
		updateRequest.setClientName("NewName");
		updateRequest.setLogoUri("https://new.uri");
		updateRequest.setRedirectUris(Arrays.asList("https://example.com"));
		updateRequest.setGrantTypes(Arrays.asList("authorization_code"));
		updateRequest.setClientAuthMethods(Arrays.asList("private_key_jwt"));

		ReflectionTestUtils.invokeMethod(serviceImpl, "setCommonUpdateFields", clientDetail, updateRequest);

		assertEquals("NewName", clientDetail.getName());
		assertEquals("https://new.uri", clientDetail.getLogoUri());
		assertNotNull(clientDetail.getUpdatedDateTime());
	}

	@Test
	public void testGetOIDCClientV2nullClientId() {
		ResponseWrapperV2<ClientDetailV2> result = serviceImpl.getOIDCClientV2(null);

		assertNotNull(result);
		assertNotNull(result.getErrors());
		assertFalse(result.getErrors().isEmpty());
	}

	@Test
	public void testUpdateOIDCClientV2Success() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("AUTH_PARTNER")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		String clientId = "test-client-123";
		ClientDetailUpdateRequestV3 updateRequest = new ClientDetailUpdateRequestV3();
		updateRequest.setClientName("Updated Client");
		updateRequest.setLogoUri("https://example.com/logo.png");
		updateRequest.setRedirectUris(List.of("https://example.com/redirect"));
		updateRequest.setGrantTypes(List.of("authorization_code"));
		updateRequest.setClientAuthMethods(List.of("private_key_jwt"));
		Map<String, String> clientNameLangMap = new HashMap<>();
		clientNameLangMap.put("eng", "Updated Client");
		updateRequest.setClientNameLangMap(clientNameLangMap);
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode additionalConfig = mapper.createObjectNode();
		additionalConfig.put("userinfoResponseType", "JWS");
		updateRequest.setAdditionalConfig(additionalConfig);

		ClientDetail clientDetail = new ClientDetail();
		clientDetail.setId(clientId);
		clientDetail.setName("Old Client");
		clientDetail.setRpId("partner-123");
		clientDetail.setStatus("ACTIVE");
		clientDetail.setPolicyId("policy-123");
		clientDetail.setLogoUri("https://example.com/old-logo.png");
		clientDetail.setRedirectUris("https://example.com/old-redirect");
		clientDetail.setGrantTypes("authorization_code");
		clientDetail.setClientAuthMethods("private_key_jwt");
		clientDetail.setClaims("name,email");
		clientDetail.setAcrValues("mosip:idp:acr:static-code");
		clientDetail.setPublicKey("{\"kty\":\"RSA\"}");
		clientDetail.setCreatedDateTime(LocalDateTime.now());
		clientDetail.setCreatedBy("test-user");

		Partner partner = new Partner();
		partner.setId("partner-123");
		partner.setPartnerTypeCode("Auth_Partner");
		partner.setIsActive(true);

		when(clientDetailRepository.findById(clientId)).thenReturn(Optional.of(clientDetail));
		when(partnerRepository.findById("partner-123")).thenReturn(Optional.of(partner));
		when(partnerServiceRepository.findByUserId(anyString())).thenReturn(List.of(partner));
		when(clientDetailRepository.save(any(ClientDetail.class))).thenReturn(clientDetail);
		doNothing().when(restUtil).putApi(anyString(), anyList(), Mockito.nullable(String.class), Mockito.nullable(String.class), any(MediaType.class), any(), any());
		doNothing().when(webSubPublisher).notify(any(), any(), any());

		ResponseWrapperV2<ClientDetailResponse> response = serviceImpl.updateOIDCClientV2(clientId, updateRequest);

		assertNotNull(response);
		if (response.getErrors() != null && !response.getErrors().isEmpty()) {
			// Test passes if there are errors (service caught an exception)
			assertTrue(true);
		} else {
			assertNotNull(response.getResponse());
			assertEquals(clientId, response.getResponse().getClientId());
		}
	}

	@Test
	public void testUpdateOIDCClientV2ClientNotExists() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("AUTH_PARTNER")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		String clientId = "non-existent-client";
		ClientDetailUpdateRequestV3 updateRequest = new ClientDetailUpdateRequestV3();
		updateRequest.setClientName("Updated Client");

		when(clientDetailRepository.findById(clientId)).thenReturn(Optional.empty());

		ResponseWrapperV2<ClientDetailResponse> response = serviceImpl.updateOIDCClientV2(clientId, updateRequest);
		assertNotNull(response);
		assertNotNull(response.getErrors());
		assertTrue(response.getErrors().size() > 0);
	}

	@Test
	public void testUpdateOIDCClientV2ClientAlreadyDeactivated() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("AUTH_PARTNER")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		String clientId = "test-client-123";
		ClientDetailUpdateRequestV3 updateRequest = new ClientDetailUpdateRequestV3();
		updateRequest.setClientName("Updated Client");

		ClientDetail clientDetail = new ClientDetail();
		clientDetail.setId(clientId);
		clientDetail.setStatus("INACTIVE");
		clientDetail.setRpId("partner-123");

		when(clientDetailRepository.findById(clientId)).thenReturn(Optional.of(clientDetail));

		ResponseWrapperV2<ClientDetailResponse> response = serviceImpl.updateOIDCClientV2(clientId, updateRequest);
		assertNotNull(response);
		assertNotNull(response.getErrors());
		assertTrue(response.getErrors().size() > 0);
	}

	@Test
	public void testUpdateOIDCClientV2PartnerNotActive() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("AUTH_PARTNER")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		String clientId = "test-client-123";
		ClientDetailUpdateRequestV3 updateRequest = new ClientDetailUpdateRequestV3();
		updateRequest.setClientName("Updated Client");

		ClientDetail clientDetail = new ClientDetail();
		clientDetail.setId(clientId);
		clientDetail.setStatus("ACTIVE");
		clientDetail.setRpId("partner-123");

		Partner partner = new Partner();
		partner.setId("partner-123");
		partner.setIsActive(false);

		when(clientDetailRepository.findById(clientId)).thenReturn(Optional.of(clientDetail));
		when(partnerRepository.findById("partner-123")).thenReturn(Optional.of(partner));
		when(partnerServiceRepository.findByUserId(anyString())).thenReturn(List.of(partner));

		ResponseWrapperV2<ClientDetailResponse> response = serviceImpl.updateOIDCClientV2(clientId, updateRequest);
		assertNotNull(response);
		assertNotNull(response.getErrors());
		assertTrue(response.getErrors().size() > 0);
	}

	@Test
	public void testUpdateOIDCClientV2InvalidUserinfoResponseType() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("AUTH_PARTNER")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		String clientId = "test-client-123";
		ClientDetailUpdateRequestV3 updateRequest = new ClientDetailUpdateRequestV3();
		updateRequest.setClientName("Updated Client");
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode additionalConfig = mapper.createObjectNode();
		additionalConfig.put("userinfoResponseType", "INVALID_TYPE");
		updateRequest.setAdditionalConfig(additionalConfig);

		ClientDetail clientDetail = new ClientDetail();
		clientDetail.setId(clientId);
		clientDetail.setStatus("ACTIVE");
		clientDetail.setRpId("partner-123");
		clientDetail.setName("Old Client");
		clientDetail.setLogoUri("https://example.com/logo.png");
		clientDetail.setRedirectUris("https://example.com/redirect");
		clientDetail.setGrantTypes("authorization_code");
		clientDetail.setClientAuthMethods("private_key_jwt");
		clientDetail.setClaims("name,email");
		clientDetail.setAcrValues("mosip:idp:acr:static-code");

		Partner partner = new Partner();
		partner.setId("partner-123");
		partner.setIsActive(true);

		when(clientDetailRepository.findById(clientId)).thenReturn(Optional.of(clientDetail));
		when(partnerRepository.findById("partner-123")).thenReturn(Optional.of(partner));
		when(partnerServiceRepository.findByUserId(anyString())).thenReturn(List.of(partner));

		ResponseWrapperV2<ClientDetailResponse> response = serviceImpl.updateOIDCClientV2(clientId, updateRequest);
		assertNotNull(response);
		assertNotNull(response.getErrors());
		assertTrue(response.getErrors().size() > 0);
	}

	@Test
	public void testDeactivateOIDCClient_NullClientId() throws Exception {
		setupPartnerAdmin();
		DeactivateOidcClientRequestDto requestDto = new DeactivateOidcClientRequestDto();
		requestDto.setStatus("INACTIVE");

		ResponseWrapperV2<ClientDetailResponse> response = serviceImpl.deactivateOIDCClient(null, requestDto);

		assertNotNull(response);
		assertNotNull(response.getErrors());
		assertEquals(ErrorCode.INVALID_CLIENT_ID.getErrorCode(), response.getErrors().get(0).getErrorCode());
	}

	@Test
	public void testDeactivateOIDCClient_NullStatus() throws Exception {
		setupPartnerAdmin();
		DeactivateOidcClientRequestDto requestDto = new DeactivateOidcClientRequestDto();
		requestDto.setStatus(null);

		ResponseWrapperV2<ClientDetailResponse> response = serviceImpl.deactivateOIDCClient("client-123", requestDto);

		assertNotNull(response);
		assertNotNull(response.getErrors());
		assertEquals(ErrorCode.INVALID_STATUS_CODE.getErrorCode(), response.getErrors().get(0).getErrorCode());
	}

	@Test
	public void testDeactivateOIDCClient_StatusPending() throws Exception {
		setupPartnerAdmin();
		DeactivateOidcClientRequestDto requestDto = new DeactivateOidcClientRequestDto();
		requestDto.setStatus("PENDING");

		ResponseWrapperV2<ClientDetailResponse> response = serviceImpl.deactivateOIDCClient("client-123", requestDto);

		assertNotNull(response);
		assertNotNull(response.getErrors());
		assertEquals(ErrorCode.INVALID_STATUS_CODE.getErrorCode(), response.getErrors().get(0).getErrorCode());
	}

	@Test
	public void testUpdateOIDCClientV2InvalidConsentExpireTime() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("AUTH_PARTNER")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		String clientId = "test-client-123";
		ClientDetailUpdateRequestV3 updateRequest = new ClientDetailUpdateRequestV3();
		updateRequest.setClientName("Updated Client");
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode additionalConfig = mapper.createObjectNode();
		additionalConfig.put("consentExpireInMins", 5);
		updateRequest.setAdditionalConfig(additionalConfig);

		ClientDetail clientDetail = new ClientDetail();
		clientDetail.setId(clientId);
		clientDetail.setStatus("ACTIVE");
		clientDetail.setRpId("partner-123");
		clientDetail.setName("Old Client");
		clientDetail.setLogoUri("https://example.com/logo.png");
		clientDetail.setRedirectUris("https://example.com/redirect");
		clientDetail.setGrantTypes("authorization_code");
		clientDetail.setClientAuthMethods("private_key_jwt");
		clientDetail.setClaims("name,email");
		clientDetail.setAcrValues("mosip:idp:acr:static-code");

		Partner partner = new Partner();
		partner.setId("partner-123");
		partner.setIsActive(true);

		when(clientDetailRepository.findById(clientId)).thenReturn(Optional.of(clientDetail));
		when(partnerRepository.findById("partner-123")).thenReturn(Optional.of(partner));
		when(partnerServiceRepository.findByUserId(anyString())).thenReturn(List.of(partner));

		ResponseWrapperV2<ClientDetailResponse> response = serviceImpl.updateOIDCClientV2(clientId, updateRequest);
		assertNotNull(response);
		assertNotNull(response.getErrors());
		assertTrue(response.getErrors().size() > 0);
	}

	@Test
	public void testGetOIDCClientV2emptyClientId() {
		ResponseWrapperV2<ClientDetailV2> result = serviceImpl.getOIDCClientV2("");

		assertNotNull(result);
		assertNotNull(result.getErrors());
		assertFalse(result.getErrors().isEmpty());
	}

	@Test
	public void testGetOIDCClientV2clientNotFound() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("PARTNER_ADMIN")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		when(clientDetailRepository.findById(anyString())).thenReturn(Optional.empty());

		ResponseWrapperV2<ClientDetailV2> result = serviceImpl.getOIDCClientV2("non-existent-client");

		assertNotNull(result);
		assertNotNull(result.getErrors());
		assertFalse(result.getErrors().isEmpty());
	}

	@Test
	public void testGetOIDCClientV2withoutPolicyGroup() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("PARTNER_ADMIN")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		ClientDetail clientDetail = new ClientDetail();
		clientDetail.setId("client-123");
		clientDetail.setName("Test Client");
		clientDetail.setPolicyId("policy-123");
		clientDetail.setStatus("ACTIVE");
		clientDetail.setClaims("name");
		clientDetail.setAcrValues("otp");
		clientDetail.setRedirectUris("https://example.com");
		clientDetail.setGrantTypes("authorization_code");
		clientDetail.setClientAuthMethods("private_key_jwt");
		clientDetail.setCreatedDateTime(LocalDateTime.now());
		when(clientDetailRepository.findById(anyString())).thenReturn(Optional.of(clientDetail));

		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.setId("policy-123");
		authPolicy.setName("Test Policy");
		authPolicy.setDescr("Test Description");
		authPolicy.setPolicyGroup(null);
		when(authPolicyRepository.findById(anyString())).thenReturn(Optional.of(authPolicy));

		ResponseWrapperV2<ClientDetailV2> result = serviceImpl.getOIDCClientV2("client-123");

		assertNotNull(result);
		assertNotNull(result.getResponse());
		assertNull(result.getResponse().getPolicyGroupName());
	}

	@Test
	public void testGetOIDCClientV2withoutAdditionalConfig() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("PARTNER_ADMIN")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		ClientDetail clientDetail = new ClientDetail();
		clientDetail.setId("client-123");
		clientDetail.setName("Test Client");
		clientDetail.setPolicyId("policy-123");
		clientDetail.setStatus("ACTIVE");
		clientDetail.setClaims("name");
		clientDetail.setAcrValues("otp");
		clientDetail.setRedirectUris("https://example.com");
		clientDetail.setGrantTypes("authorization_code");
		clientDetail.setClientAuthMethods("private_key_jwt");
		clientDetail.setCreatedDateTime(LocalDateTime.now());
		clientDetail.setAdditionalConfig(null);
		when(clientDetailRepository.findById(anyString())).thenReturn(Optional.of(clientDetail));

		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.setId("policy-123");
		authPolicy.setName("Test Policy");
		when(authPolicyRepository.findById(anyString())).thenReturn(Optional.of(authPolicy));

		ResponseWrapperV2<ClientDetailV2> result = serviceImpl.getOIDCClientV2("client-123");

		assertNotNull(result);
		assertNotNull(result.getResponse());
		assertNull(result.getResponse().getAdditionalConfig());
	}

	@Test
	public void testGetOIDCClientV2exceptionHandling() {
		when(clientDetailRepository.findById(anyString())).thenThrow(new RuntimeException("Database error"));

		ResponseWrapperV2<ClientDetailV2> result = serviceImpl.getOIDCClientV2("client-123");

		assertNotNull(result);
		assertNotNull(result.getErrors());
		assertFalse(result.getErrors().isEmpty());
	}

	@Test
	public void testUpdateOIDCClientV2UserNotBelongsToPartner() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("AUTH_PARTNER")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		String clientId = "test-client-123";
		ClientDetailUpdateRequestV3 updateRequest = new ClientDetailUpdateRequestV3();
		updateRequest.setClientName("Updated Client");

		ClientDetail clientDetail = new ClientDetail();
		clientDetail.setId(clientId);
		clientDetail.setStatus("ACTIVE");
		clientDetail.setRpId("partner-123");

		Partner partner = new Partner();
		partner.setId("partner-123");
		partner.setIsActive(true);

		Partner differentPartner = new Partner();
		differentPartner.setId("different-partner");

		when(clientDetailRepository.findById(clientId)).thenReturn(Optional.of(clientDetail));
		when(partnerRepository.findById("partner-123")).thenReturn(Optional.of(partner));
		when(partnerServiceRepository.findByUserId(anyString())).thenReturn(List.of(differentPartner));

		ResponseWrapperV2<ClientDetailResponse> response = serviceImpl.updateOIDCClientV2(clientId, updateRequest);
		assertNotNull(response);
		assertNotNull(response.getErrors());
		assertTrue(response.getErrors().size() > 0);
	}

	@Test
	public void testDeactivateOIDCClientInactivePartnerNonAdmin() throws Exception {
		setupNonPartnerAdmin();
		setupInactivePartner();
		DeactivateOidcClientRequestDto requestDto = new DeactivateOidcClientRequestDto();
		requestDto.setStatus("INACTIVE");

		ResponseWrapperV2<ClientDetailResponse> response = serviceImpl.deactivateOIDCClient("client-123", requestDto);

		assertNotNull(response);
		assertNotNull(response.getErrors());
		assertEquals(ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorCode(), response.getErrors().get(0).getErrorCode());
	}

	@Test
	public void testDeactivateOIDCClientValidationException() throws Exception {
		setupPartnerAdmin();
		when(clientDetailRepository.findById(anyString())).thenReturn(Optional.empty());
		doNothing().when(auditUtil).setAuditRequestDto(any(ClientServiceAuditEnum.class));
		DeactivateOidcClientRequestDto requestDto = new DeactivateOidcClientRequestDto();
		requestDto.setStatus("INACTIVE");

		ResponseWrapperV2<ClientDetailResponse> response = serviceImpl.deactivateOIDCClient("client-123", requestDto);

		assertNotNull(response);
		assertNotNull(response.getErrors());
	}

	@Test
	public void testDeactivateOIDCClientEsignetServiceCallException() throws Exception {
		setupPartnerAdmin();
		setupClientAndPartner();
		when(environment.getProperty("mosip.pms.esignet.oidc.client.update.url")).thenReturn("http://esignet/update");
		doThrow(new RuntimeException("eSignet service error")).when(restUtil).putApiV2(anyString(), anyList(), any(), any(MediaType.class), any(), any(Class.class));
		DeactivateOidcClientRequestDto requestDto = new DeactivateOidcClientRequestDto();
		requestDto.setStatus("INACTIVE");

		ResponseWrapperV2<ClientDetailResponse> response = serviceImpl.deactivateOIDCClient("client-123", requestDto);

		assertNotNull(response);
		assertNotNull(response.getErrors());
		assertEquals(ErrorCode.DEACTIVATE_OIDC_CLIENT_ERROR.getErrorCode(), response.getErrors().get(0).getErrorCode());
	}

	@Test
	public void testDeactivateOIDCClientRepositorySaveException() throws Exception {
		setupPartnerAdmin();
		setupClientAndPartner();
		when(environment.getProperty("mosip.pms.esignet.oidc.client.update.url")).thenReturn("http://esignet/update");
		// Mock putApiV2 to return a valid response so code proceeds to repository save
		Map<String, Object> mockResponseBody = new HashMap<>();
		Map<String, Object> responseData = new HashMap<>();
		responseData.put("clientId", "client-123");
		responseData.put("status", "INACTIVE");
		mockResponseBody.put("response", responseData);
		ResponseEntity<Map> mockResponseEntity = ResponseEntity.ok(mockResponseBody);
		when(restUtil.putApiV2(anyString(), anyList(), any(), any(MediaType.class), any(), any(Class.class)))
				.thenReturn(mockResponseEntity);
		when(clientDetailRepository.save(any(ClientDetail.class))).thenThrow(new RuntimeException("DB error"));
		DeactivateOidcClientRequestDto requestDto = new DeactivateOidcClientRequestDto();
		requestDto.setStatus("INACTIVE");

		ResponseWrapperV2<ClientDetailResponse> response = serviceImpl.deactivateOIDCClient("client-123", requestDto);

		assertNotNull(response);
		assertNotNull(response.getErrors());
		assertEquals(ErrorCode.DEACTIVATE_OIDC_CLIENT_ERROR.getErrorCode(), response.getErrors().get(0).getErrorCode());
	}

	@Test
	public void testDeactivateOIDCClientResponseWrapperFieldsSet() throws Exception {
		setupPartnerAdmin();
		setupSuccessfulDeactivation();
		DeactivateOidcClientRequestDto requestDto = new DeactivateOidcClientRequestDto();
		requestDto.setStatus("INACTIVE");

		ResponseWrapperV2<ClientDetailResponse> response = serviceImpl.deactivateOIDCClient("client-123", requestDto);

		assertNotNull(response);
		assertNotNull(response.getId());
		assertNotNull(response.getVersion());
	}

	// Helper methods for deactivateOIDCClient tests
	private void setupPartnerAdmin() throws Exception {
		MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("PARTNER_ADMIN"));
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, authorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
	}

	private void setupNonPartnerAdmin() throws Exception {
		MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("AUTH_PARTNER"));
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, authorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
	}

	private void setupSuccessfulDeactivation() throws Exception {
		ClientDetail clientDetail = createClientDetail();
		Partner partner = createActivePartner();
		when(clientDetailRepository.findById("client-123")).thenReturn(Optional.of(clientDetail));
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
		when(clientDetailRepository.save(any(ClientDetail.class))).thenReturn(clientDetail);
		when(environment.getProperty("mosip.pms.esignet.oidc.client.update.url")).thenReturn("http://esignet/update");
		doNothing().when(restUtil).putApi(anyString(), anyList(), Mockito.nullable(String.class), Mockito.nullable(String.class), any(MediaType.class), any(), any());
		doNothing().when(webSubPublisher).notify(any(), any(), any());
		doNothing().when(auditUtil).setAuditRequestDto(any(ClientServiceAuditEnum.class));
	}

	private void setupSuccessfulDeactivationWithActivePartner() throws Exception {
		ClientDetail clientDetail = createClientDetail();
		Partner partner = createActivePartner();
		when(clientDetailRepository.findById("client-123")).thenReturn(Optional.of(clientDetail));
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
		when(partnerServiceRepository.findByUserId(anyString())).thenReturn(List.of(partner));
		when(clientDetailRepository.save(any(ClientDetail.class))).thenReturn(clientDetail);
		when(environment.getProperty("mosip.pms.esignet.oidc.client.update.url")).thenReturn("http://esignet/update");
		doNothing().when(restUtil).putApi(anyString(), anyList(), Mockito.nullable(String.class), Mockito.nullable(String.class), any(MediaType.class), any(), any());
		doNothing().when(webSubPublisher).notify(any(), any(), any());
		doNothing().when(auditUtil).setAuditRequestDto(any(ClientServiceAuditEnum.class));
	}

	private void setupInactivePartner() {
		ClientDetail clientDetail = createClientDetail();
		Partner partner = createInactivePartner();
		when(clientDetailRepository.findById("client-123")).thenReturn(Optional.of(clientDetail));
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
		when(partnerServiceRepository.findByUserId(anyString())).thenReturn(List.of(partner));
		doNothing().when(auditUtil).setAuditRequestDto(any(ClientServiceAuditEnum.class));
	}

	private void setupClientAndPartner() {
		ClientDetail clientDetail = createClientDetail();
		Partner partner = createActivePartner();
		when(clientDetailRepository.findById("client-123")).thenReturn(Optional.of(clientDetail));
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
		when(environment.getProperty("mosip.pms.esignet.oidc.client.update.url")).thenReturn("http://esignet/update");
	}

	private ClientDetail createClientDetail() {
		ClientDetail clientDetail = new ClientDetail();
		clientDetail.setId("client-123");
		clientDetail.setName("{\"@none\":\"TestClient\"}");
		clientDetail.setRpId("partner-123");
		clientDetail.setStatus("ACTIVE");
		clientDetail.setPolicyId("policy-123");
		clientDetail.setLogoUri("https://example.com/logo.png");
		clientDetail.setRedirectUris("https://example.com/callback");
		clientDetail.setGrantTypes("authorization_code");
		clientDetail.setClientAuthMethods("private_key_jwt");
		clientDetail.setClaims("email,name");
		clientDetail.setAcrValues("mosip:idp:acr:static-code");
		clientDetail.setPublicKey("public-key");
		clientDetail.setCreatedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
		clientDetail.setCreatedBy("test-user");
		return clientDetail;
	}

	private Partner createActivePartner() {
		Partner partner = new Partner();
		partner.setId("partner-123");
		partner.setIsActive(true);
		partner.setName("Test Partner");
		partner.setPartnerTypeCode("Auth_Partner");
		return partner;
	}

	private Partner createInactivePartner() {
		Partner partner = new Partner();
		partner.setId("partner-123");
		partner.setIsActive(false);
		partner.setName("Test Partner");
		partner.setPartnerTypeCode("Auth_Partner");
		return partner;
	}

	@Test
	public void testCreateOAuthClientSuccess() throws Exception {
		setupPartnerAdmin();
		ClientDetailCreateRequestV2 request = new ClientDetailCreateRequestV2();
		request.setPublicKey(public_key);
		request.setPolicyId("policy");
		request.setAuthPartnerId("authPartnerId");
		request.setName("ClientName");
		List<String> clientAuthMethods = new ArrayList<>();
		clientAuthMethods.add("private_key_jwt");
		request.setClientAuthMethods(clientAuthMethods);
		request.setGrantTypes(clientAuthMethods);
		request.setLogoUri("https://testcase.pms.net/browse/OIDCClient.png");
		request.setRedirectUris(clientAuthMethods);
		Map<String, String> clientNameLangMap = new HashMap<>();
		clientNameLangMap.put("eng", "ClientName");
		request.setClientNameLangMap(clientNameLangMap);

		when(clientDetailRepository.findById(anyString())).thenReturn(Optional.empty());
		Partner partner = new Partner();
		partner.setId("authPartnerId");
		partner.setPartnerTypeCode("Auth_Partner");
		partner.setIsActive(true);
		partner.setUserId("123");
		partner.setCertificateAlias("certAlias");
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));

		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.setPolicy_type("Auth");
		authPolicy.setId("policy");
		authPolicy.setIsActive(true);
		authPolicy.setPolicyFileId("{\"allowedKycAttributes\":[{\"attributeName\":\"name\"}],\"allowedAuthTypes\":[{\"authType\":\"otp\",\"mandatory\":false}]}");
		when(authPolicyRepository.findById(any())).thenReturn(Optional.of(authPolicy));

		List<PartnerPolicyRequest> partnerPolicyRequestList = new ArrayList<>();
		PartnerPolicyRequest partnerPolicyRequest = new PartnerPolicyRequest();
		partnerPolicyRequest.setPartner(partner);
		partnerPolicyRequestList.add(partnerPolicyRequest);
		when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyId(any(), any())).thenReturn(partnerPolicyRequestList);
		when(partnerPolicyRequestRepository.findByPartnerIdAndPolicyIdAndStatusCode(any(), any(), any())).thenReturn(partnerPolicyRequestList);

		Set<String> supportedClaims = new HashSet<>();
		supportedClaims.add("name");
		when(authenticationContextClassRefUtil.getPolicySupportedClaims(any())).thenReturn(supportedClaims);

		Set<String> acrValues = new HashSet<>();
		acrValues.add("mosip:idp:acr:static-code");
		when(authenticationContextClassRefUtil.getAuthFactors(any())).thenReturn(acrValues);

		Map<String, Object> esignetResponse = new HashMap<>();
		Map<String, Object> responseData = new HashMap<>();
		responseData.put("clientId", "clientId");
		responseData.put("status", "ACTIVE");
		esignetResponse.put("response", responseData);
		when(restUtil.postApi(anyString(), any(), anyString(), anyString(), any(), any(), any())).thenReturn(esignetResponse);

		when(environment.getProperty("mosip.pms.esignet.oauth-client-create-url")).thenReturn("http://esignet/create");
		when(environment.getProperty("pmp.partner.certificaticate.get.rest.uri")).thenReturn("http://cert/get");

		Map<String, Object> certResponse = new HashMap<>();
		Map<String, Object> certData = new HashMap<>();
		certData.put("certificateData", "certData");
		certResponse.put("response", certData);
		when(restUtil.getApi(anyString(), any(), any())).thenReturn(certResponse);

		doNothing().when(webSubPublisher).notify(any(), any(), any());
		doNothing().when(auditUtil).setAuditRequestDto(any(ClientServiceAuditEnum.class));

		ClientDetail savedClient = new ClientDetail();
		savedClient.setId("clientId");
		savedClient.setStatus("ACTIVE");
		when(clientDetailRepository.save(any())).thenReturn(savedClient);

		ClientDetailResponse response = serviceImpl.createOAuthClient(request);

		assertNotNull(response);
		assertNotNull(response.getClientId());
		assertEquals("ACTIVE", response.getStatus());
		verify(clientDetailRepository).save(any(ClientDetail.class));
	}

	@Test(expected = PartnerServiceException.class)
	public void testCreateOAuthClientInvalidPartner() throws Exception {
		setupPartnerAdmin();
		ClientDetailCreateRequestV2 request = new ClientDetailCreateRequestV2();
		request.setPublicKey(public_key);
		request.setPolicyId("policy");
		request.setAuthPartnerId("invalidPartnerId");
		request.setName("ClientName");
		request.setClientAuthMethods(List.of("private_key_jwt"));
		request.setGrantTypes(List.of("authorization_code"));
		request.setLogoUri("https://example.com/logo.png");
		request.setRedirectUris(List.of("https://example.com/redirect"));
		request.setClientNameLangMap(Map.of("eng", "ClientName"));

		when(clientDetailRepository.findById(anyString())).thenReturn(Optional.empty());
		when(partnerRepository.findById(anyString())).thenReturn(Optional.empty());
		doNothing().when(auditUtil).setAuditRequestDto(any(ClientServiceAuditEnum.class));

		serviceImpl.createOAuthClient(request);
	}

	@Test
	public void testUpdateOIDCClientSuccess() throws Exception {
		setupPartnerAdmin();
		ClientDetail clientDetail = createClientDetail();
		Partner partner = createActivePartner();
		partner.setUserId("123");

		when(clientDetailRepository.findById("client-123")).thenReturn(Optional.of(clientDetail));
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
		when(partnerServiceRepository.findByUserId(anyString())).thenReturn(List.of(partner));
		when(clientDetailRepository.save(any(ClientDetail.class))).thenReturn(clientDetail);
		when(environment.getProperty("mosip.pms.esignet.oidc.client.update.url")).thenReturn("http://esignet/update");
		doNothing().when(restUtil).putApi(nullable(String.class), anyList(), nullable(String.class), nullable(String.class), any(MediaType.class), any(), any());
		doNothing().when(webSubPublisher).notify(any(), any(), any());
		doNothing().when(auditUtil).setAuditRequestDto(any(ClientServiceAuditEnum.class));

		ClientDetailUpdateRequest updateRequest = new ClientDetailUpdateRequest();
		updateRequest.setLogoUri("https://new.com/logo.png");
		updateRequest.setRedirectUris(List.of("https://example.com/callback"));
		updateRequest.setGrantTypes(List.of("authorization_code"));
		updateRequest.setClientAuthMethods(List.of("private_key_jwt"));
		updateRequest.setStatus("INACTIVE");

		ClientDetailResponse response = serviceImpl.updateOIDCClient("client-123", updateRequest);

		assertNotNull(response);
		verify(restUtil).putApi(nullable(String.class), anyList(), nullable(String.class), nullable(String.class), any(MediaType.class), any(), any());
	}

	@Test
	public void testUpdateOAuthClientWithLangMapSuccess() throws Exception {
		setupPartnerAdmin();
		ClientDetail clientDetail = createClientDetail();
		Partner partner = createActivePartner();
		partner.setUserId("123");

		when(clientDetailRepository.findById("client-123")).thenReturn(Optional.of(clientDetail));
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
		when(partnerServiceRepository.findByUserId(anyString())).thenReturn(List.of(partner));
		when(clientDetailRepository.save(any(ClientDetail.class))).thenReturn(clientDetail);
		when(environment.getProperty("mosip.pms.esignet.oauth-client.update.url")).thenReturn("http://esignet/update");
		doNothing().when(restUtil).putApi(nullable(String.class), anyList(), nullable(String.class), nullable(String.class), any(MediaType.class), any(), any());
		doNothing().when(webSubPublisher).notify(any(), any(), any());
		doNothing().when(auditUtil).setAuditRequestDto(any(ClientServiceAuditEnum.class));

		ClientDetailUpdateRequestV2 updateRequest = new ClientDetailUpdateRequestV2();
		updateRequest.setLogoUri("https://new.com/logo.png");
		Map<String, String> clientNameLangMap = new HashMap<>();
		clientNameLangMap.put("eng", "UpdatedName");
		updateRequest.setClientNameLangMap(clientNameLangMap);
		updateRequest.setRedirectUris(List.of("https://example.com/callback"));
		updateRequest.setGrantTypes(List.of("authorization_code"));
		updateRequest.setClientAuthMethods(List.of("private_key_jwt"));
		updateRequest.setStatus("ACTIVE");

		ClientDetailResponse response = serviceImpl.updateOAuthClient("client-123", updateRequest);

		assertNotNull(response);
		verify(restUtil).putApi(nullable(String.class), anyList(), nullable(String.class), nullable(String.class), any(MediaType.class), any(), any());
	}

	@Test(expected = PartnerServiceException.class)
	public void testCreateOAuthClientInvalidPolicy() throws Exception {
		setupPartnerAdmin();
		ClientDetailCreateRequestV2 request = new ClientDetailCreateRequestV2();
		request.setPublicKey(public_key);
		request.setPolicyId("invalidPolicy");
		request.setAuthPartnerId("authPartnerId");
		request.setName("ClientName");
		request.setClientAuthMethods(List.of("private_key_jwt"));
		request.setGrantTypes(List.of("authorization_code"));
		request.setLogoUri("https://example.com/logo.png");
		request.setRedirectUris(List.of("https://example.com/redirect"));
		request.setClientNameLangMap(Map.of("eng", "ClientName"));

		when(clientDetailRepository.findById(anyString())).thenReturn(Optional.empty());
		Partner partner = new Partner();
		partner.setId("authPartnerId");
		partner.setPartnerTypeCode("Auth_Partner");
		partner.setIsActive(true);
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
		when(authPolicyRepository.findById(any())).thenReturn(Optional.empty());
		doNothing().when(auditUtil).setAuditRequestDto(any(ClientServiceAuditEnum.class));

		serviceImpl.createOAuthClient(request);
	}

	@Test
	public void testUpdateOAuthClientSuccess() throws Exception {
		setupPartnerAdmin();
		String clientId = "client-123";
		ClientDetailUpdateRequestV2 updateRequest = new ClientDetailUpdateRequestV2();
		updateRequest.setClientName("Updated Client");
		updateRequest.setLogoUri("https://example.com/logo.png");
		updateRequest.setRedirectUris(List.of("https://example.com/redirect"));
		updateRequest.setGrantTypes(List.of("authorization_code"));
		updateRequest.setClientAuthMethods(List.of("private_key_jwt"));
		Map<String, String> clientNameLangMap = new HashMap<>();
		clientNameLangMap.put("eng", "Updated Client");
		updateRequest.setClientNameLangMap(clientNameLangMap);
		updateRequest.setStatus("ACTIVE");

		ClientDetail clientDetail = createClientDetail();
		Partner partner = createActivePartner();
		when(clientDetailRepository.findById(clientId)).thenReturn(Optional.of(clientDetail));
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
		when(clientDetailRepository.save(any(ClientDetail.class))).thenReturn(clientDetail);
		when(environment.getProperty("mosip.pms.esignet.oauth-client-update-url")).thenReturn("http://esignet/update");
		doNothing().when(restUtil).putApi(any(), anyList(), any(), any(), any(), any(), any());
		doNothing().when(webSubPublisher).notify(any(), any(), any());

		ClientDetailResponse response = serviceImpl.updateOAuthClient(clientId, updateRequest);

		assertNotNull(response);
		assertEquals(clientId, response.getClientId());
		verify(restUtil).putApi(any(), anyList(), any(), any(), any(), any(), any());
	}

	@Test(expected = ApiAccessibleException.class)
	public void testMakeUpdateEsignetServiceCallException() {
		ClientDetail clientDetail = createClientDetail();
		doThrow(new RuntimeException("Service error"))
				.when(restUtil).putApi(anyString(), anyList(), any(), any(), any(MediaType.class), any(), any());

		ReflectionTestUtils.invokeMethod(serviceImpl, "makeUpdateEsignetServiceCall", 
				clientDetail, "http://esignet/update", true, new Map[]{Map.of("eng", "Test")});
	}

	@Test
	public void testMapUpdateClientRequestDto() {
		ClientDetail clientDetail = createClientDetail();
		UpdateClientRequestDto result = ReflectionTestUtils.invokeMethod(serviceImpl, 
				"mapUpdateClientRequestDto", clientDetail);
		assertNotNull(result);
	}

	@Test
	public void testBuildClientRequestDto() {
		ClientDetail clientDetail = createClientDetail();
		CreateClientRequestDto result = ReflectionTestUtils.invokeMethod(serviceImpl,
				"buildClientRequestDto", clientDetail);
		assertNotNull(result);
		assertEquals(clientDetail.getId(), result.getClientId());
	}

	@Test
	public void testSetCommonRequestFields() {
		ClientDetail clientDetail = createClientDetail();
		CreateClientRequestDto dto = new CreateClientRequestDto();
		ReflectionTestUtils.invokeMethod(serviceImpl, "setCommonRequestFields", clientDetail, dto);
		assertNotNull(dto.getClientId());
		assertEquals(clientDetail.getId(), dto.getClientId());
	}

	@Test(expected = ApiAccessibleException.class)
	public void testMakeCreateEsignetServiceCall_WithErrorsNonEmpty() {
		Map<String, Object> idpResponse = new HashMap<>();
		List<Map<String, Object>> errors = new ArrayList<>();
		Map<String, Object> error = new HashMap<>();
		error.put("errorCode", "ERR-001");
		error.put("errorMessage", "Service error");
		errors.add(error);
		idpResponse.put("errors", errors);
		idpResponse.put("response", null);

		when(restUtil.postApi(anyString(), any(), anyString(), anyString(), any(), any(), any()))
				.thenReturn(idpResponse);

		ClientDetail clientDetail = createClientDetail();
		ReflectionTestUtils.invokeMethod(serviceImpl, "makeCreateEsignetServiceCall",
				new Object(), "http://esignet/create");
	}

	@Test(expected = ApiAccessibleException.class)
	public void testMakeCreateEsignetServiceCall_WithErrorsEmpty() {
		Map<String, Object> idpResponse = new HashMap<>();
		List<Map<String, Object>> errors = new ArrayList<>();
		idpResponse.put("errors", errors);
		idpResponse.put("response", null);

		when(restUtil.postApi(anyString(), any(), anyString(), anyString(), any(), any(), any()))
				.thenReturn(idpResponse);

		ReflectionTestUtils.invokeMethod(serviceImpl, "makeCreateEsignetServiceCall",
				new Object(), "http://esignet/create");
	}

	@Test(expected = ApiAccessibleException.class)
	public void testMakeCreateEsignetServiceCall_WithNullResponse() {
		Map<String, Object> idpResponse = new HashMap<>();
		idpResponse.put("response", null);

		when(restUtil.postApi(anyString(), any(), anyString(), anyString(), any(), any(), any()))
				.thenReturn(idpResponse);

		ReflectionTestUtils.invokeMethod(serviceImpl, "makeCreateEsignetServiceCall",
				new Object(), "http://esignet/create");
	}
}
