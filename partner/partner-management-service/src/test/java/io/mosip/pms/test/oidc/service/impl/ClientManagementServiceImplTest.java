package io.mosip.pms.test.oidc.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.Ref;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;


import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.oidc.client.contant.ClientServiceAuditEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.mosip.pms.common.constant.EventType;
import io.mosip.pms.common.dto.ClientPublishDto;
import io.mosip.pms.common.dto.PartnerDataPublishDto;
import io.mosip.pms.common.dto.PolicyPublishDto;
import io.mosip.pms.common.dto.Type;
import io.mosip.pms.common.entity.AuthPolicy;
import io.mosip.pms.common.entity.ClientDetail;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.entity.PolicyGroup;
import io.mosip.pms.common.exception.ApiAccessibleException;
import io.mosip.pms.common.repository.PartnerRepository;
import io.mosip.pms.common.util.MapperUtils;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.oidc.client.contant.ClientServiceAuditEnum;
import io.mosip.pms.oidc.client.dto.*;
import io.mosip.pms.partner.response.dto.PartnerCertDownloadResponeDto;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static io.mosip.pms.common.util.UserDetailUtil.getLoggedInUserId;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.repository.AuthPolicyRepository;
import io.mosip.pms.common.repository.ClientDetailRepository;
import io.mosip.pms.common.repository.PartnerPolicyRequestRepository;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.oidc.client.service.impl.ClientManagementServiceImpl;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.exception.PartnerServiceException;
import org.springframework.web.multipart.MultipartFile;

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

	@Test (expected = Exception.class)
	public void testCreateOIDCClient02() throws Exception {
		ClientDetailCreateRequest clientDetailCreateRequest = new ClientDetailCreateRequest();
		clientDetailCreateRequest.setPublicKey(new HashMap<>());
		serviceImpl.createOIDCClient(clientDetailCreateRequest);
	}

	@Test (expected = Exception.class)
	public void testCreateOIDCClient03() throws Exception {
		ClientDetailCreateRequest clientDetailCreateRequest = mock(ClientDetailCreateRequest.class);
		when(clientDetailCreateRequest.getPublicKey()).thenReturn(new HashMap<>());
		serviceImpl.createOIDCClient(clientDetailCreateRequest);
		verify(clientDetailCreateRequest).getPublicKey();
	}

	@Test (expected = PartnerServiceException.class)
	public void testCreateOIDCClient04() throws Exception {
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
	public void testCreateOIDCClient05() throws Exception {
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
	public void testCreateOIDCClient06() throws Exception {

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
	public void testCreateOIDCClient07() throws Exception {

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
	public void testCreateOIDCClient08() throws Exception {

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
	public void testUpdateOIDCClient() throws Exception {
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
		clientDetail1.setAcrValues("value");
		clientDetail1.setClaims("Claims");
		clientDetail1.setClientAuthMethods("Client Auth Methods");
		clientDetail1.setCreatedBy("Cr By");
		clientDetail1.setCreatedDateTime(LocalDateTime.of(1, 1, 1, 1, 1));
		clientDetail1.setGrantTypes("Grant Types");
		clientDetail1.setId("Id");
		clientDetail1.setIsDeleted(false);
		clientDetail1.setLogoUri("Logo Uri");
		clientDetail1.setName("Name");
		clientDetail1.setPolicyId("PolicyId");
		clientDetail1.setPublicKey("Public Key");
		clientDetail1.setRedirectUris("Redirect Uris");
		clientDetail1.setRpId("RpId");
		clientDetail1.setStatus("Status");
		clientDetail1.setUpdatedBy("UpdatedBy");
		clientDetail1.setUpdatedDateTime(LocalDateTime.of(1, 1, 1, 1, 1));
		when(environment.getProperty(any(String.class))).thenReturn("Property");
		when(clientDetailRepository.save((ClientDetail) any())).thenReturn(clientDetail1);
		when(clientDetailRepository.findById((String) any())).thenReturn(ofResult);
		doNothing().when(restUtil)
				.putApi((String) any(), (List<String>) any(), (String) any(), (String) any(), (MediaType) any(), (Object) any(),
						(Class<Object>) any());
		doNothing().when(webSubPublisher).notify((EventType) any(), (Map<String, Object>) any(), (Type) any());

		ClientDetailUpdateRequest clientDetailUpdateRequest = new ClientDetailUpdateRequest();
		clientDetailUpdateRequest.setClientAuthMethods(new ArrayList<>());
		clientDetailUpdateRequest.setClientName("ClientName");
		clientDetailUpdateRequest.setGrantTypes(new ArrayList<>());
		clientDetailUpdateRequest.setLogoUri("Logo Uri");
		clientDetailUpdateRequest.setRedirectUris(new ArrayList<>());
		clientDetailUpdateRequest.setStatus("Status");
		ClientDetailResponse actualUpdateOIDCClientResult = serviceImpl.updateOIDCClient("Id",
				clientDetailUpdateRequest);
		assertEquals("Id", actualUpdateOIDCClientResult.getClientId());
		assertEquals("Status", actualUpdateOIDCClientResult.getStatus());
		verify(auditUtil).setAuditRequestDto((ClientServiceAuditEnum) any());
		verify(clientDetailRepository).save((ClientDetail) any());
		verify(clientDetailRepository).findById((String) any());
		verify(restUtil).putApi((String) any(), (List<String>) any(), (String) any(), (String) any(), (MediaType) any(),
				(Object) any(), (Class<Object>) any());
		verify(webSubPublisher).notify((EventType) any(), (Map<String, Object>) any(), (Type) any());
	}

	@Test (expected = PartnerServiceException.class)
	public void testUpdateOIDCClient2() throws Exception {
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
		clientDetail1.setAcrValues("value");
		clientDetail1.setClaims("Claims");
		clientDetail1.setClientAuthMethods("Client Auth Methods");
		clientDetail1.setCreatedBy("Cr By");
		clientDetail1.setCreatedDateTime(LocalDateTime.of(1, 1, 1, 1, 1));
		clientDetail1.setGrantTypes("Grant Types");
		clientDetail1.setId("Id");
		clientDetail1.setIsDeleted(false);
		clientDetail1.setLogoUri("Logo Uri");
		clientDetail1.setName("Name");
		clientDetail1.setPolicyId("PolicyId");
		clientDetail1.setPublicKey("Public Key");
		clientDetail1.setRedirectUris("Redirect Uris");
		clientDetail1.setRpId("RpId");
		clientDetail1.setStatus("Status");
		clientDetail1.setUpdatedBy("UpdatedBy");
		clientDetail1.setUpdatedDateTime(LocalDateTime.of(1, 1, 1, 1, 1));
		when(clientDetailRepository.save((ClientDetail) any())).thenReturn(clientDetail1);
		when(clientDetailRepository.findById((String) any())).thenReturn(ofResult);
		when(environment.getProperty((String) any())).thenReturn("Property");
		doNothing().when(restUtil)
				.putApi((String) any(), (List<String>) any(), (String) any(), (String) any(), (MediaType) any(),
						(Object) any(), (Class<Object>) any());
		doThrow(new PartnerServiceException("An error occurred", "An error occurred")).when(webSubPublisher)
				.notify((EventType) any(), (Map<String, Object>) any(), (Type) any());

		ClientDetailUpdateRequest clientDetailUpdateRequest = new ClientDetailUpdateRequest();
		clientDetailUpdateRequest.setClientAuthMethods(new ArrayList<>());
		clientDetailUpdateRequest.setClientName("ClientName");
		clientDetailUpdateRequest.setGrantTypes(new ArrayList<>());
		clientDetailUpdateRequest.setLogoUri("Logo Uri");
		clientDetailUpdateRequest.setRedirectUris(new ArrayList<>());
		clientDetailUpdateRequest.setStatus("Status");
		serviceImpl.updateOIDCClient("Id", clientDetailUpdateRequest);
		verify(clientDetailRepository).save((ClientDetail) any());
		verify(clientDetailRepository).findById((String) any());
		verify(environment).getProperty((String) any());
		verify(restUtil).putApi((String) any(), (List<String>) any(), (String) any(), (String) any(), (MediaType) any(),
				(Object) any(), (Class<Object>) any());
		verify(webSubPublisher).notify((EventType) any(), (Map<String, Object>) any(), (Type) any());
	}

	@Test
	public void testGetClientDetails() {
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
		authPolicy.setDescr("Descr");
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
		Optional<AuthPolicy> ofResult = Optional.of(authPolicy);
		when(authPolicyRepository.findById((String) any())).thenReturn(ofResult);

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
		Optional<io.mosip.pms.common.entity.ClientDetail> ofResult1 = Optional.of(clientDetail);
		when(clientDetailRepository.findById((String) any())).thenReturn(ofResult1);
		io.mosip.pms.oidc.client.dto.ClientDetail actualClientDetails = serviceImpl
				.getClientDetails("Id");
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
		verify(authPolicyRepository).findById((String) any());
		verify(clientDetailRepository).findById((String) any());
	}

	@Test (expected = PartnerServiceException.class)
	public void testGetClientDetails2() {
		when(authPolicyRepository.findById((String) any()))
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
		Optional<io.mosip.pms.common.entity.ClientDetail> ofResult = Optional.of(clientDetail);
		when(clientDetailRepository.findById((String) any())).thenReturn(ofResult);
		serviceImpl.getClientDetails("Id");
		verify(authPolicyRepository).findById((String) any());
		verify(clientDetailRepository).findById((String) any());
	}

	@Test
	public void testNotify() {
		doNothing().when(webSubPublisher).notify((EventType) any(), (Map<String, Object>) any(), (Type) any());

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
		verify(webSubPublisher).notify((EventType) any(), (Map<String, Object>) any(), (Type) any());
	}

	@Test
	public void testNotify2() {
		doNothing().when(webSubPublisher).notify((EventType) any(), (Map<String, Object>) any(), (Type) any());

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
				EventType.POLICY_UPDATED);
		verify(webSubPublisher).notify((EventType) any(), (Map<String, Object>) any(), (Type) any());
	}

	@Test
	public void testNotify3() {
		doNothing().when(webSubPublisher).notify((EventType) any(), (Map<String, Object>) any(), (Type) any());

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
				EventType.APIKEY_UPDATED);
		verify(webSubPublisher).notify((EventType) any(), (Map<String, Object>) any(), (Type) any());
	}

	@Test
	public void testNotify4() {
		doNothing().when(webSubPublisher).notify((EventType) any(), (Map<String, Object>) any(), (Type) any());

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
				EventType.PARTNER_UPDATED);
		verify(webSubPublisher).notify((EventType) any(), (Map<String, Object>) any(), (Type) any());
	}

	@Test
	public void testNotify5() {
		doNothing().when(webSubPublisher).notify((EventType) any(), (Map<String, Object>) any(), (Type) any());

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
				EventType.CA_CERTIFICATE_UPLOADED);
		verify(webSubPublisher).notify((EventType) any(), (Map<String, Object>) any(), (Type) any());
	}

	@Test (expected = PartnerServiceException.class)
	public void testNotify6() {
		doThrow(new PartnerServiceException("An error occurred", "An error occurred")).when(webSubPublisher)
				.notify((EventType) any(), (Map<String, Object>) any(), (Type) any());

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
		verify(webSubPublisher).notify((EventType) any(), (Map<String, Object>) any(), (Type) any());
	}

	@Test
	public void testGetPolicyObject() {
		ReflectionTestUtils.invokeMethod(serviceImpl,"getPolicyObject","Policy");
		ReflectionTestUtils.invokeMethod(serviceImpl,"getPolicyObject","Auth");
		ReflectionTestUtils.invokeMethod(serviceImpl,"getPolicyObject","");
		ReflectionTestUtils.invokeMethod(serviceImpl,"getPolicyObject","Policy123");
		ReflectionTestUtils.invokeMethod(serviceImpl,"getPolicyObject","123Policy");
	}

	@Test (expected = PartnerServiceException.class)
	public void testPublishClientData() {
		when(environment.getProperty((String) any())).thenReturn("Property");
		when(restUtil.getApi((String) any(), (Map<String, String>) any(), (Class<Object>) any()))
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
		authPolicy.setDescr("Descr");
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
		verify(environment).getProperty((String) any());
		verify(restUtil).getApi((String) any(), (Map<String, String>) any(), (Class<Object>) any());
		verify(MapperUtils.mapDataToPublishDto(partner, ReflectionTestUtils.invokeMethod(serviceImpl,"getPartnerCertificate",(partner.getCertificateAlias()))));
		verify(MapperUtils.mapPolicyToPublishDto(authPolicy, ReflectionTestUtils.invokeMethod(serviceImpl,"getPolicyObject",(authPolicy.getPolicyFileId()))));
		verify(MapperUtils.mapClientDataToPublishDto(clientDetail));
		verify(ReflectionTestUtils.invokeMethod(serviceImpl,"notify", (any(PartnerDataPublishDto.class)), any(PolicyPublishDto.class), any(ClientPublishDto.class),
				eq(EventType.OIDC_CLIENT_CREATED)));
	}

	@Test (expected = UndeclaredThrowableException.class)
	public void testCallIdpService01() throws Exception {
		when(objectMapper.readValue((String) any(), (Class<Map<Object, Object>>) any())).thenReturn(new HashMap<>());
		when(restUtil.postApi((String) any(), (List<String>) any(), (String) any(), (String) any(), (MediaType) any(),
				(Object) any(), (Class<Object>) any()))
				.thenThrow(new PartnerServiceException("An error occurred", "An error occurred"));

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

		CreateClientRequestDto dto = new CreateClientRequestDto();
		dto.setClientId(clientDetail.getId());
		dto.setClientName(clientDetail.getName());
		dto.setRelyingPartyId(clientDetail.getRpId());
		dto.setLogoUri(clientDetail.getLogoUri());
		dto.setPublicKey(objectMapper.readValue(clientDetail.getPublicKey(), Map.class));
		dto.setUserClaims(Collections.singletonList((clientDetail.getClaims())));
		dto.setAuthContextRefs(Collections.singletonList(clientDetail.getAcrValues()));
		dto.setRedirectUris(Collections.singletonList(clientDetail.getRedirectUris()));
		dto.setGrantTypes(Collections.singletonList(clientDetail.getGrantTypes()));
		dto.setClientAuthMethods(Collections.singletonList(clientDetail.getClientAuthMethods()));
		ReflectionTestUtils.invokeMethod(serviceImpl,"callIdpService",clientDetail,"calleeApi",true);
		verify(objectMapper).readValue((String) any(), (Class<Map<Object, Object>>) any());
		verify(restUtil).postApi((String) any(), (List<String>) any(), (String) any(), (String) any(), (MediaType) any(),
				(Object) any(), (Class<Object>) any());
	}

	@Test (expected = NullPointerException.class)
	public void testGetReqAttributeFromPolicyJson() {

		JSONObject policyObject = new JSONObject();
		policyObject.put("id","001");
		policyObject.put("name","Policy");

		ReflectionTestUtils.invokeMethod(serviceImpl,"getReqAttributeFromPolicyJson",policyObject,"Parent Attribute", "Child Attribute",
				"Filter Attribute");
	}

	@Test (expected = ApiAccessibleException.class)
	public void testMakeCreateIDPServiceCall01() throws JsonProcessingException {
		ClientDetailResponse clientDetailResponse = new ClientDetailResponse();
		clientDetailResponse.setClientId("ClientId");
		clientDetailResponse.setStatus("Status");
		when(objectMapper.readValue((String) any(), (Class<ClientDetailResponse>) any()))
				.thenReturn(clientDetailResponse);
		when(objectMapper.writeValueAsString((Object) any())).thenReturn("123");
		when(restUtil.postApi((String) any(), (List<String>) any(), (String) any(), (String) any(), (MediaType) any(),
				(Object) any(), (Class<Object>) any())).thenReturn(new HashMap<>());
		assertSame(clientDetailResponse, ReflectionTestUtils.invokeMethod(serviceImpl,"makeCreateIDPServiceCall","Request","Callee Api"));
		verify(objectMapper).readValue((String) any(), (Class<ClientDetailResponse>) any());
		verify(objectMapper).writeValueAsString((Object) any());
		verify(restUtil).postApi((String) any(), (List<String>) any(), (String) any(), (String) any(), (MediaType) any(),
				(Object) any(), (Class<Object>) any());
	}

	@Test (expected = PartnerServiceException.class)
	public void testGetPartnerCertificate01() {
		when(environment.getProperty((String) any())).thenReturn("Property");
		when(restUtil.getApi((String) any(), (Map<String, String>) any(), (Class<Object>) any()))
				.thenThrow(new PartnerServiceException("An error occurred", "An error occurred"));
		ReflectionTestUtils.invokeMethod(serviceImpl,"getPartnerCertificate","Certificate Alias");
		verify(environment).getProperty((String) any());
		verify(restUtil).getApi((String) any(), (Map<String, String>) any(), (Class<Object>) any());
	}

	@Test (expected = ApiAccessibleException.class)
	public void testGetPartnerCertificate02() throws JsonProcessingException {
		when(environment.getProperty((String) any())).thenReturn("Property");

		PartnerCertDownloadResponeDto partnerCertDownloadResponeDto = new PartnerCertDownloadResponeDto();
		partnerCertDownloadResponeDto.setCertificateData("Certificate Data");
		partnerCertDownloadResponeDto.setTimestamp(LocalDateTime.of(1, 1, 1, 1, 1));
		when(objectMapper.readValue((String) any(), (Class<PartnerCertDownloadResponeDto>) any()))
				.thenReturn(partnerCertDownloadResponeDto);
		when(objectMapper.writeValueAsString((Object) any())).thenReturn("123");
		when(restUtil.getApi((String) any(), (Map<String, String>) any(), (Class<Object>) any()))
				.thenReturn(new HashMap<>());
		assertEquals("Certificate Data", ReflectionTestUtils.invokeMethod(serviceImpl,"getPartnerCertificate","Certificate Alias"));
		verify(environment).getProperty((String) any());
		verify(objectMapper).readValue((String) any(), (Class<PartnerCertDownloadResponeDto>) any());
		verify(objectMapper).writeValueAsString((Object) any());
		verify(restUtil).getApi((String) any(), (Map<String, String>) any(), (Class<Object>) any());
	}

	@Test (expected = PartnerServiceException.class)
	public void testCreateOIDCClient01() throws Exception {

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
		authPolicy.setPolicyFileId("42");
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
	public void testConvertStringToList01() {
		String inputString = "id,name,email";
		List<String> expectedList = Arrays.asList("id", "name", "email");
		List<String> resultList = ReflectionTestUtils.invokeMethod(serviceImpl, "convertStringToList", inputString);
		assertEquals(expectedList, resultList);
	}

}
