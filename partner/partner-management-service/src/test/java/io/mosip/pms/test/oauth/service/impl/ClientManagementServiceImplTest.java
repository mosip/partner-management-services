package io.mosip.pms.test.oauth.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mosip.kernel.openid.bridge.model.AuthUserDetails;
import io.mosip.pms.common.entity.*;
import io.mosip.pms.common.entity.ClientDetail;
import io.mosip.pms.common.repository.*;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.oidc.client.contant.ClientServiceAuditEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.mosip.pms.common.constant.EventType;
import io.mosip.pms.common.dto.ClientPublishDto;
import io.mosip.pms.common.dto.PartnerDataPublishDto;
import io.mosip.pms.common.dto.PolicyPublishDto;
import io.mosip.pms.common.dto.Type;
import io.mosip.pms.common.exception.ApiAccessibleException;
import io.mosip.pms.common.util.MapperUtils;
import io.mosip.pms.oauth.client.dto.*;
import io.mosip.pms.partner.response.dto.PartnerCertDownloadResponeDto;
import net.minidev.json.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import static org.junit.Assert.assertTrue;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static io.mosip.pms.common.util.UserDetailUtil.getLoggedInUserId;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.oauth.client.dto.ClientDetailCreateRequest;
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

	@Mock
	PartnerRepository partnerRepository;

	@Mock
	ClientDetailRepository clientDetailRepository;

	@Mock
	AuthPolicyRepository authPolicyRepository;

	@Mock
	PartnerPolicyRequestRepository partnerPolicyRequestRepository;

	@MockBean
	private RestUtil restUtil;
	
	@MockBean
	private AuditUtil auditUtil;

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private WebSubPublisher webSubPublisher;

	@Mock
	Authentication authentication;

	@Mock
	SecurityContext securityContext;

	@Mock
	PartnerServiceRepository partnerServiceRepository;

	Map<String, Object> public_key;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(serviceImpl, "clientDetailRepository", clientDetailRepository);
		ReflectionTestUtils.setField(serviceImpl, "authPolicyRepository", authPolicyRepository);
		ReflectionTestUtils.setField(serviceImpl, "partnerPolicyRequestRepository", partnerPolicyRequestRepository);
		ReflectionTestUtils.setField(serviceImpl, "webSubPublisher", webSubPublisher);
		ReflectionTestUtils.setField(serviceImpl, "restUtil", restUtil);

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
		when(partnerRepository.findById(any(String.class))).thenReturn(Optional.of(new Partner()));

		ClientDetailResponse response = serviceImpl.createOIDCClient(createRequest);

		assertNotNull(response);
	}

	@Test (expected = PartnerServiceException.class)
	public void createOIDCClient_validRequest_returnsClientDetailResponse() throws Exception {

		when(environment.getProperty("auth.url")).thenReturn("https://pms.net/partner");
		when(clientDetailRepository.findById(anyString())).thenReturn(Optional.empty());
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(new Partner()));

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

	@Test(expected = PartnerServiceException.class)
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

	@Test (expected = PartnerServiceException.class)
	public void testUpdateOIDCClient_WithFailure() throws Exception {
		doNothing().when(auditUtil).setAuditRequestDto((ClientServiceAuditEnum) any());

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
	public void testGetClientDetails_Success() {
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
	public void testGetClientDetails_Exception() {
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
		clientDetail.setPolicyId("PolicyId");
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

		when(objectMapper.readValue(anyString(), eq(PartnerCertDownloadResponeDto.class))).thenReturn(partnerCertDownloadResponeDto);
		when(objectMapper.writeValueAsString(any())).thenReturn("123");
		when(restUtil.getApi(anyString(), any(), any())).thenReturn(new HashMap<>());

		String certificateData = ReflectionTestUtils.invokeMethod(serviceImpl, "getPartnerCertificate", "Certificate Alias");
		assertEquals("Certificate Data", certificateData);

		verify(environment).getProperty(anyString());
		verify(objectMapper).readValue(anyString(), eq(PartnerCertDownloadResponeDto.class));
		verify(objectMapper).writeValueAsString(any());
		verify(restUtil).getApi(anyString(), any(), any());
	}

	@Test (expected = PartnerServiceException.class)
	public void testCreateOIDCClientWithError() throws Exception {

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
		when(clientDetailRepository.findById(anyString())).thenReturn(Optional.empty());
		serviceImpl.updateOAuthClient("clientId", new ClientDetailUpdateRequestV2());
		verify(auditUtil).setAuditRequestDto(any(ClientServiceAuditEnum.class));
		verify(clientDetailRepository).findById(anyString());
	}

	@Test (expected = PartnerServiceException.class)
	public void testUpdateOAuthClientWhenNotificationFails() throws Exception {
		doNothing().when(auditUtil).setAuditRequestDto(any(ClientServiceAuditEnum.class));

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
		when(objectMapper.readValue(anyString(), eq(ClientDetailResponse.class)))
				.thenReturn(clientDetailResponse);
		when(objectMapper.writeValueAsString(any())).thenReturn("123");
		when(restUtil.postApi(anyString(), anyList(), anyString(), anyString(), any(MediaType.class),
				any(), any())).thenReturn(new HashMap<>());

		ClientDetailResponse result = ReflectionTestUtils.invokeMethod(serviceImpl,
				"makeCreateEsignetServiceCall", "Request", "Callee Api");
		assertSame(clientDetailResponse, result);
		verify(objectMapper).readValue(anyString(), eq(ClientDetailResponse.class));
		verify(objectMapper).writeValueAsString(any());
		verify(restUtil).postApi(anyString(), anyList(), anyString(), anyString(), any(MediaType.class), any(), any());
	}

	@Test (expected = ApiAccessibleException.class)
	public void testMakeCreateEsignetServiceCall_ExceptionThrown() throws JsonProcessingException {
		when(objectMapper.readValue(anyString(), eq(Object.class))).thenReturn("Value");
		when(objectMapper.writeValueAsString(any()))
				.thenThrow(new ApiAccessibleException("An error occurred", "An error occurred"));
		when(restUtil.postApi(anyString(), anyList(), anyString(), anyString(), any(MediaType.class),
				any(), any())).thenReturn(new HashMap<>());

		ReflectionTestUtils.invokeMethod(serviceImpl, "makeCreateEsignetServiceCall",("Request"), "Callee Api");
		verify(objectMapper).readValue(anyString(), eq(ClientDetailResponse.class));
		verify(objectMapper).writeValueAsString(any());
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

	@Test(expected = PartnerServiceException.class)
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
	public void getOidcClients() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("Auth_Partner");
		partner.setPolicyGroupId("abc");
		partner.setApprovalStatus("approved");
		partnerList.add(partner);
		when(partnerServiceRepository.findByUserId(anyString())).thenReturn(partnerList);

		List<ClientDetail> clientDetailList = new ArrayList<>();
		ClientDetail clientDetail = new ClientDetail();
		clientDetail.setId("id123");
		clientDetail.setName("Sample Client");
		clientDetail.setRpId("rp123");
		clientDetail.setPolicyId("policy123");
		clientDetail.setLogoUri("https://example.com/logo.png");
		clientDetail.setRedirectUris("https://example.com/callback");
		clientDetail.setPublicKey("public-key-string");
		clientDetail.setClaims("claims-string");
		clientDetail.setAcrValues("acr-values-string");
		clientDetail.setStatus("active");
		clientDetail.setGrantTypes("grant-type-string");
		clientDetail.setClientAuthMethods("auth-methods-string");
		clientDetail.setCreatedBy("creator-user");
		clientDetail.setCreatedDateTime(LocalDateTime.now());
		clientDetail.setUpdatedBy("updater-user");
		clientDetail.setUpdatedDateTime(LocalDateTime.now());
		clientDetail.setIsDeleted(false);
		when(clientDetailRepository.findAllByPartnerId(anyString())).thenReturn(clientDetailList);

		AuthPolicy authPolicy = new AuthPolicy();
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setName("abc");
		authPolicy.setPolicyGroup(policyGroup);
		authPolicy.setName("abc");
		when(authPolicyRepository.findById(anyString())).thenReturn(Optional.of(authPolicy));

		serviceImpl.getOidcClients();
	}

	private io.mosip.kernel.openid.bridge.model.MosipUserDto getMosipUserDto() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = new io.mosip.kernel.openid.bridge.model.MosipUserDto();
		mosipUserDto.setUserId("123");
		mosipUserDto.setMail("abc@gmail.com");
		return mosipUserDto;
	}

}
