package io.mosip.pms.test.oidc.service.impl;

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
import io.mosip.pms.test.config.TestUtil;

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
		public_key = TestUtil.generateJWK_RSA().toJSONObject();
	}

	@Test
	public void createClientTest() throws Exception {
		ClientDetailCreateRequest request = new ClientDetailCreateRequest();
		System.out.println(public_key);
		try {
			serviceImpl.createOIDCClient(request);
		}catch (PartnerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.POLICY_NOT_EXIST.getErrorCode()));
		}
	}

}
