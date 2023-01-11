package io.mosip.pms.test.oidc.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.repository.AuthPolicyRepository;
import io.mosip.pms.common.repository.ClientDetailRepository;
import io.mosip.pms.common.repository.PartnerPolicyRequestRepository;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.oidc.client.dto.ClientDetailCreateRequest;
import io.mosip.pms.oidc.client.service.impl.ClientManagementServiceImpl;
import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.exception.PartnerServiceException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ClientManagementServiceImplTest {

	@Autowired
	private ClientManagementServiceImpl serviceImpl;

	@Mock
	ClientDetailRepository clientDetailRepository;

	@Mock
	AuthPolicyRepository authPolicyRepository;

	@Mock
	PartnerPolicyRequestRepository partnerPolicyRequestRepository;

	@MockBean
	private RestUtil restUtil;

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
		request.setClientAuthMethods(clientAuthMethods);;
		request.setGrantTypes(clientAuthMethods);
		request.setLogoUri("https://testcase.pms.net/browse/OIDCClient.png");
		request.setRedirectUris(clientAuthMethods);
		request.setName("ClientName");
		try {
			serviceImpl.createOIDCClient(request);
		}catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.INVALID_PARTNERID.getErrorCode()));
		}
	}

}
