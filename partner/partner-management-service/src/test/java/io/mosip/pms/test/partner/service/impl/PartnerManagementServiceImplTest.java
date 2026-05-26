package io.mosip.pms.test.partner.service.impl;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import org.springframework.test.util.AopTestUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.openid.bridge.model.MosipUserDto;
import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.dto.TrustCertTypeListResponseDto;
import io.mosip.pms.common.dto.TrustCertificateSummaryDto;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.entity.*;
import io.mosip.pms.common.repository.*;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.partner.response.dto.APIKeyGenerateResponseDto;
import io.mosip.pms.partner.dto.KeycloakUserDto;
import io.mosip.pms.partner.manager.dto.*;
import io.mosip.pms.common.dto.PartnerCertDownloadResponeDto;
import io.mosip.pms.partner.request.dto.LinkPolicyGroupRequestDto;
import io.mosip.pms.partner.util.PartnerHelper;
import io.mosip.pms.tasklets.util.KeyManagerHelper;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.common.service.NotificatonService;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.partner.manager.constant.ErrorCode;
import io.mosip.pms.partner.manager.constant.PartnerManageEnum;
import io.mosip.pms.partner.manager.exception.PartnerManagerServiceException;
import io.mosip.pms.partner.manager.service.impl.PartnerManagementServiceImpl;
import io.mosip.pms.partner.request.dto.APIKeyGenerateRequestDto;
import io.mosip.pms.partner.request.dto.APIkeyStatusUpdateRequestDto;
import io.mosip.pms.partner.request.dto.BioExtractorsDto;
import io.mosip.pms.partner.request.dto.BioExtractorsRequestDto;
import io.mosip.pms.partner.request.dto.BioextractorConfigurationDeleteRequestDto;
import io.mosip.pms.partner.request.dto.BioextractorConfigurationRequestDto;
import io.mosip.pms.partner.response.dto.BioextractorConfigurationDetailDto;
import io.mosip.pms.partner.response.dto.BioextractorConfigurationResponseDto;
import io.mosip.pms.test.config.TestSecurityConfig;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
public class PartnerManagementServiceImplTest {

	private AuthUserDetails mockAuthUserDetails(String userId, String... authorities) {
		AuthUserDetails details = org.mockito.Mockito.mock(AuthUserDetails.class);
		when(details.getUserId()).thenReturn(userId);
		Collection auths = new ArrayList<GrantedAuthority>();
		if (authorities != null) {
			for (String a : authorities) {
				((Collection<GrantedAuthority>) auths).add(new SimpleGrantedAuthority(a));
			}
		}
		when(details.getAuthorities()).thenReturn(auths);
		return details;
	}

	private io.mosip.kernel.openid.bridge.model.AuthUserDetails mockBridgeAuthUserDetails(String userId) {
		MosipUserDto mosipUserDto = getMosipUserDto();
		mosipUserDto.setUserId(userId);
		return new io.mosip.kernel.openid.bridge.model.AuthUserDetails(mosipUserDto, userId);
	}
	
	@Autowired
	private PartnerManagementServiceImpl partnerManagementImpl;
	
	@Mock
	PartnerPolicyRepository partnerPolicyRepository;

	@Mock
	PartnerRepository partnerRepository;

	@MockBean
	PartnerServiceRepository partnerServiceRepository;
	
	@Mock
	PartnerPolicyRequestRepository partnerPolicyRequestRepository;

	@MockBean
	PolicyGroupRepository policyGroupRepository;

	@Mock
	PartnerSummaryRepository partnerSummaryRepository;
	
	@Mock
	AuthPolicyRepository authPolicyRepository;
	
	@Mock	
	MispLicenseV2Repository mispLicenseV2Repository;
	
	@Mock
	BiometricExtractorProviderRepository extractorProviderRepository;

	@Mock
	PartnerPolicyCredentialTypeRepository partnerPolicyCredentialTypeRepository;

	@Mock
	BioextractorConfigurationRepository bioextractorConfigurationRepository;

	@Mock
	PartnerPolicyBioextractRequestRepository partnerPolicyBioextractRequestRepository;

	@Mock
	PartnerPolicyCredentialTypeRequestRepository partnerPolicyCredentialTypeRequestRepository;

	@Mock
	private WebSubPublisher webSubPublisher;

	@Mock
	Authentication authentication;

	@Mock
	SecurityContext securityContext;

	@Mock
	PartnerHelper partnerHelper;

	@Mock
	SearchHelper partnerSearchHelper;
	
	@Autowired
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate restTemplate;

	@MockBean
	private AuditUtil audit;
	
	@MockBean
	private NotificatonService notificationService;
	
	@MockBean
	private RestUtil restUtil;
	
	@Mock
    private ObjectMapper mapper;

	@Mock
	ApiKeyRequestSummaryRepository apiKeyRequestSummaryRepository;

	@Mock
	PartnerPolicyMappingRequestRepository partnerPolicyMappingRequestRepository;

	@Mock
	Environment environment;

	@MockBean
	KeyManagerHelper keyManagerHelper;
	
	@After
	public void tearDownSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(partnerManagementImpl, "policyGroupRepository", policyGroupRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "partnerRepository", partnerRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "authPolicyRepository", authPolicyRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "partnerPolicyRequestRepository", partnerPolicyRequestRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "partnerPolicyRepository", partnerPolicyRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "extractorProviderRepository", extractorProviderRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "partnerPolicyCredentialTypeRepository", partnerPolicyCredentialTypeRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "partnerPolicyBioextractRequestRepository", partnerPolicyBioextractRequestRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "partnerPolicyCredentialTypeRequestRepository", partnerPolicyCredentialTypeRequestRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "bioextractorConfigurationRepository", bioextractorConfigurationRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "maxRetries", 100);
		ReflectionTestUtils.setField(partnerManagementImpl, "mispLicenseV2Repository", mispLicenseV2Repository);
		ReflectionTestUtils.setField(partnerManagementImpl, "webSubPublisher", webSubPublisher);
		ReflectionTestUtils.setField(partnerManagementImpl, "restUtil", restUtil);
		ReflectionTestUtils.setField(partnerManagementImpl, "partnerHelper", partnerHelper);
		ReflectionTestUtils.setField(partnerManagementImpl, "partnerSearchHelper", partnerSearchHelper);
//		ReflectionTestUtils.setField(partnerManagementImpl, "mapper", mapper);		
		Mockito.doNothing().when(webSubPublisher).notify(Mockito.any(),Mockito.any(),Mockito.any());
		Mockito.doNothing().when(audit).setAuditRequestDto(Mockito.any(PartnerManageEnum.class));
		Mockito.doNothing().when(audit).setAuditRequestDto(Mockito.any(PartnerManageEnum.class), anyString(), anyString());
		Mockito.doNothing().when(notificationService).sendNotications(Mockito.any(), Mockito.any());
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void partnerApiKeyPolicyMappingsTest(){
		LocalDateTime now = LocalDateTime.now();
		String partnerID = "678789";
		String PolicyAPIKey = "56784567";
		String old_auth_pilicy_id = "56784569";
		
		PartnersPolicyMappingRequest partnersPolicyMappingRequest = new PartnersPolicyMappingRequest();
		partnersPolicyMappingRequest.setOldPolicyID("456789");
		partnersPolicyMappingRequest.setNewPolicyID("567890");
		
		Partner partner = new Partner();
		partner.setId("id");
		partner.setAddress("address");
		partner.setContactNo("47384384");
		partner.setEmailId("xyz@hotmail.com");
		partner.setName("name");
		partner.setPolicyGroupId("policyGroupId");
		partner.setIsActive(true);
		partner.setUpdBy("Partner Service");
		partner.setUpdDtimes(Timestamp.valueOf(now));
		
		
		PartnerPolicy part_policy = new PartnerPolicy();
		part_policy.setCrBy("Partner Manager");
		part_policy.setCrDtimes(Timestamp.valueOf(now));
		part_policy.setIsActive(true);
		part_policy.setIsDeleted(true);
		part_policy.setPartner(partner);
		
		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.setName("name");
		
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setName("Name");
		policyGroup.setId("12133");
		policyGroup.setIsActive(true);
		policyGroup.setUserId("UserId");
		policyGroup.setCrBy("CreatedBy");
		
		Optional<PolicyGroup> opt_PolicyGroup = Optional.of(policyGroup);
		
		Optional<AuthPolicy> opt_authPolicy = Optional.of(authPolicy);
		Optional<PartnerPolicy> partnerPolicy = Optional.of(part_policy);
		Mockito.when(partnerPolicyRepository.findById(PolicyAPIKey)).thenReturn(partnerPolicy);
		Mockito.when(authPolicyRepository.findById(old_auth_pilicy_id)).thenReturn(opt_authPolicy);
		Mockito.when(policyGroupRepository.findById(partnersPolicyMappingRequest.getNewPolicyID())).thenReturn(opt_PolicyGroup);
		partnerManagementImpl.updatePolicyAgainstApikey(partnersPolicyMappingRequest, partnerID, PolicyAPIKey);
	}
	
	@Test
	public void partnerApiKeyPolicyMappingsTest01(){
		LocalDateTime now = LocalDateTime.now();
		String partnerID = "56784567";
		String PolicyAPIKey = "56784567";
		
		
		PartnersPolicyMappingRequest partnersPolicyMappingRequest = new PartnersPolicyMappingRequest();
		partnersPolicyMappingRequest.setOldPolicyID("456789");
		partnersPolicyMappingRequest.setNewPolicyID("567890");
		
		Partner partner = new Partner();
		partner.setId("56784567");
		partner.setAddress("address");
		partner.setContactNo("47384384");
		partner.setEmailId("xyz@hotmail.com");
		partner.setName("name");
		partner.setPolicyGroupId("567890");
		partner.setIsActive(true);
		partner.setUpdBy("Partner Service");
		partner.setUpdDtimes(Timestamp.valueOf(now));
	
		PartnerPolicy part_policy = new PartnerPolicy();
		part_policy.setCrBy("Partner Manager");
		part_policy.setCrDtimes(Timestamp.valueOf(now));
		part_policy.setIsActive(true);
		part_policy.setIsDeleted(true);
		part_policy.setPartner(partner);
		part_policy.setPolicyId("567890");
		part_policy.setValidFromDatetime(Timestamp.valueOf(LocalDateTime.now().minusDays(5)));
		part_policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(5)));
		
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setName("Name");
		policyGroup.setId("567890");
		policyGroup.setIsActive(true);
		policyGroup.setUserId("UserId");
		policyGroup.setCrBy("CreatedBy");
		
		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.setName("name");
		authPolicy.setId("567890");
		authPolicy.setCrBy("admin");
		authPolicy.setIsActive(true);
		authPolicy.setCrDtimes(Timestamp.valueOf(now));
		authPolicy.setDelDtimes(LocalDateTime.now());
		authPolicy.setDescr("authPolicy");
		authPolicy.setPolicyGroup(policyGroup);
		
		
		Optional<PolicyGroup> opt_PolicyGroup = Optional.of(policyGroup);
		Optional<AuthPolicy> opt_authPolicy = Optional.of(authPolicy);
		Optional<PartnerPolicy> partnerPolicy = Optional.of(part_policy);
		Mockito.when(partnerPolicyRepository.findById(PolicyAPIKey)).thenReturn(partnerPolicy);
		Mockito.when(authPolicyRepository.findById(partnersPolicyMappingRequest.getNewPolicyID())).thenReturn(opt_authPolicy);
		Mockito.when(authPolicyRepository.findByPolicyGroupAndId(Mockito.anyString(), Mockito.anyString())).thenReturn(authPolicy);
		Mockito.when(policyGroupRepository.findById(partnersPolicyMappingRequest.getNewPolicyID())).thenReturn(opt_PolicyGroup);
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndPolicyIdAndApikey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(part_policy);
		partnerManagementImpl.updatePolicyAgainstApikey(partnersPolicyMappingRequest, partnerID, PolicyAPIKey);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void partnerApiKeyPolicyMappingsTest06(){
		LocalDateTime now = LocalDateTime.now();
		String partnerID = "56784567";
		String PolicyAPIKey = "56784567";
		
		
		PartnersPolicyMappingRequest partnersPolicyMappingRequest = new PartnersPolicyMappingRequest();
		partnersPolicyMappingRequest.setOldPolicyID("456789");
		partnersPolicyMappingRequest.setNewPolicyID("567890");
		
		Partner partner = new Partner();
		partner.setId("56784567");
		partner.setAddress("address");
		partner.setContactNo("47384384");
		partner.setEmailId("xyz@hotmail.com");
		partner.setName("name");
		partner.setPolicyGroupId("567890");
		partner.setIsActive(true);
		partner.setUpdBy("Partner Service");
		partner.setUpdDtimes(Timestamp.valueOf(now));
	
		PartnerPolicy part_policy = new PartnerPolicy();
		part_policy.setCrBy("Partner Manager");
		part_policy.setCrDtimes(Timestamp.valueOf(now));
		part_policy.setIsActive(true);
		part_policy.setIsDeleted(true);
		part_policy.setPartner(partner);
		part_policy.setPolicyId("567890");
		part_policy.setValidFromDatetime(Timestamp.valueOf(LocalDateTime.now().minusDays(5)));
		part_policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(5)));
		
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setName("Name");
		policyGroup.setId("567890");
		policyGroup.setIsActive(false);
		policyGroup.setUserId("UserId");
		policyGroup.setCrBy("CreatedBy");
		
		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.setName("name");
		authPolicy.setId("567890");
		authPolicy.setCrBy("admin");
		authPolicy.setIsActive(true);
		authPolicy.setCrDtimes(Timestamp.valueOf(now));
		authPolicy.setDelDtimes(LocalDateTime.now());
		authPolicy.setDescr("authPolicy");
		authPolicy.setPolicyGroup(policyGroup);
		
		
		Optional<PolicyGroup> opt_PolicyGroup = Optional.of(policyGroup);
		Optional<AuthPolicy> opt_authPolicy = Optional.of(authPolicy);
		Optional<PartnerPolicy> partnerPolicy = Optional.of(part_policy);
		Mockito.when(partnerPolicyRepository.findById(PolicyAPIKey)).thenReturn(partnerPolicy);
		Mockito.when(authPolicyRepository.findById(partnersPolicyMappingRequest.getNewPolicyID())).thenReturn(opt_authPolicy);
		Mockito.when(authPolicyRepository.findByPolicyGroupAndId(Mockito.anyString(), Mockito.anyString())).thenReturn(authPolicy);
		Mockito.when(policyGroupRepository.findById(partnersPolicyMappingRequest.getNewPolicyID())).thenReturn(opt_PolicyGroup);
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndPolicyIdAndApikey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(part_policy);
		partnerManagementImpl.updatePolicyAgainstApikey(partnersPolicyMappingRequest, partnerID, PolicyAPIKey);
	}

	@Test
	public void partnerApiKeyPolicyMappings_whenPolicyInactive_throwsPolicyNotActive() {
		LocalDateTime now = LocalDateTime.now();
		String partnerID = "56784567";
		String PolicyAPIKey = "56784567";

		PartnersPolicyMappingRequest partnersPolicyMappingRequest = new PartnersPolicyMappingRequest();
		partnersPolicyMappingRequest.setOldPolicyID("456789");
		partnersPolicyMappingRequest.setNewPolicyID("567890");

		Partner partner = new Partner();
		partner.setId(partnerID);
		partner.setPolicyGroupId("pg-1");
		partner.setIsActive(true);
		partner.setUpdBy("Partner Service");
		partner.setUpdDtimes(Timestamp.valueOf(now));

		PartnerPolicy part_policy = new PartnerPolicy();
		part_policy.setCrBy("Partner Manager");
		part_policy.setCrDtimes(Timestamp.valueOf(now));
		part_policy.setIsActive(true);
		part_policy.setPartner(partner);
		part_policy.setPolicyId(partnersPolicyMappingRequest.getOldPolicyID());
		part_policy.setValidFromDatetime(Timestamp.valueOf(LocalDateTime.now().minusDays(5)));
		part_policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(5)));

		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("pg-1");
		policyGroup.setIsActive(true);

		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.setId(partnersPolicyMappingRequest.getNewPolicyID());
		authPolicy.setIsActive(false);
		authPolicy.setPolicyGroup(policyGroup);

		Mockito.when(partnerPolicyRepository.findByPartnerIdAndPolicyIdAndApikey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
				.thenReturn(part_policy);
		Mockito.when(authPolicyRepository.findByPolicyGroupAndId(Mockito.anyString(), Mockito.anyString())).thenReturn(authPolicy);

		try {
			partnerManagementImpl.updatePolicyAgainstApikey(partnersPolicyMappingRequest, partnerID, PolicyAPIKey);
			fail("expected PartnerManagerServiceException");
		} catch (PartnerManagerServiceException e) {
			assertEquals(ErrorCode.POLICY_NOT_ACTIVE_EXCEPTION.getErrorCode(), e.getErrorCode());
		}
	}

	@Test(expected = PartnerManagerServiceException.class)
	public void partnerApiKeyPolicyMappingsTest02(){
		LocalDateTime now = LocalDateTime.now();
		String partnerID = "56784567";
		String PolicyAPIKey = "56784567";
		String old_auth_pilicy_id = "56784569";
		
		PartnersPolicyMappingRequest partnersPolicyMappingRequest = new PartnersPolicyMappingRequest();
		partnersPolicyMappingRequest.setOldPolicyID("456789");
		partnersPolicyMappingRequest.setNewPolicyID("567890");
		
		Partner partner = new Partner();
		partner.setId("56784567");
		partner.setAddress("address");
		partner.setContactNo("47384384");
		partner.setEmailId("xyz@hotmail.com");
		partner.setName("name");
		partner.setPolicyGroupId("456789");
		partner.setIsActive(true);
		partner.setUpdBy("Partner Service");
		partner.setUpdDtimes(Timestamp.valueOf(now));
	
		PartnerPolicy part_policy = new PartnerPolicy();
		part_policy.setCrBy("Partner Manager");
		part_policy.setCrDtimes(Timestamp.valueOf(now));
		part_policy.setIsActive(true);
		part_policy.setIsDeleted(true);
		part_policy.setPartner(partner);
		part_policy.setPolicyId("456789");
		
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setName("Name");
		policyGroup.setId("456789");
		policyGroup.setIsActive(true);
		policyGroup.setUserId("UserId");
		policyGroup.setCrBy("CreatedBy");
		
		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.setName("name");
		authPolicy.setId("567890");
		authPolicy.setPolicyGroup(policyGroup);
		
		
		Optional<PolicyGroup> opt_PolicyGroup = Optional.of(policyGroup);
		Optional<AuthPolicy> opt_authPolicy = Optional.of(authPolicy);
		Optional<PartnerPolicy> partnerPolicy = Optional.of(part_policy);
		Mockito.when(partnerPolicyRepository.findById(PolicyAPIKey)).thenReturn(partnerPolicy);
		Mockito.when(authPolicyRepository.findById(old_auth_pilicy_id)).thenReturn(opt_authPolicy);
		Mockito.when(policyGroupRepository.findById(partnersPolicyMappingRequest.getNewPolicyID())).thenReturn(opt_PolicyGroup);
		
		partnerManagementImpl.updatePolicyAgainstApikey(partnersPolicyMappingRequest, partnerID, PolicyAPIKey);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void partnerApiKeyPolicyMappingsTest03(){
		LocalDateTime now = LocalDateTime.now();
		String partnerID = "56784567";
		String PolicyAPIKey = "56784567";
		
		
		PartnersPolicyMappingRequest partnersPolicyMappingRequest = new PartnersPolicyMappingRequest();
		partnersPolicyMappingRequest.setOldPolicyID("456789");
		partnersPolicyMappingRequest.setNewPolicyID("567890");
		
		Partner partner = new Partner();
		partner.setId("56784567");
		partner.setAddress("address");
		partner.setContactNo("47384384");
		partner.setEmailId("xyz@hotmail.com");
		partner.setName("name");
		partner.setPolicyGroupId("567890");
		partner.setIsActive(true);
		partner.setUpdBy("Partner Service");
		partner.setUpdDtimes(Timestamp.valueOf(now));
	
		PartnerPolicy part_policy = new PartnerPolicy();
		part_policy.setCrBy("Partner Manager");
		part_policy.setCrDtimes(Timestamp.valueOf(now));
		part_policy.setIsActive(true);
		part_policy.setIsDeleted(true);
		part_policy.setPartner(partner);
		part_policy.setPolicyId("567890");
		
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setName("Name");
		policyGroup.setId("567890");
		policyGroup.setIsActive(true);
		policyGroup.setUserId("UserId");
		policyGroup.setCrBy("CreatedBy");
		
		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.setName("name");
		authPolicy.setId("567890");
		authPolicy.setCrBy("admin");
		authPolicy.setIsActive(true);
		authPolicy.setCrDtimes(Timestamp.valueOf(now));
		authPolicy.setDelDtimes(LocalDateTime.now());
		authPolicy.setDescr("authPolicy");
		authPolicy.setPolicyGroup(policyGroup);
		
		
		Optional<PolicyGroup> opt_PolicyGroup = Optional.of(policyGroup);
		Optional<AuthPolicy> opt_authPolicy = Optional.of(authPolicy);
		Optional<PartnerPolicy> partnerPolicy = Optional.of(part_policy);
		Mockito.when(partnerPolicyRepository.findById(PolicyAPIKey)).thenReturn(partnerPolicy);
		Mockito.when(authPolicyRepository.findById(partnersPolicyMappingRequest.getNewPolicyID())).thenReturn(opt_authPolicy);
		//Mockito.when(authPolicyRepository.findByPolicyGroupAndId(Mockito.anyString(), Mockito.anyString())).thenReturn(authPolicy);
		Mockito.when(policyGroupRepository.findById(partnersPolicyMappingRequest.getNewPolicyID())).thenReturn(opt_PolicyGroup);
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndPolicyIdAndApikey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(part_policy);
		partnerManagementImpl.updatePolicyAgainstApikey(partnersPolicyMappingRequest, partnerID, PolicyAPIKey);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void partnerApiKeyPolicyMappingsTest04(){
		LocalDateTime now = LocalDateTime.now();
		String partnerID = "56784567";
		String PolicyAPIKey = "56784567";
		
		
		PartnersPolicyMappingRequest partnersPolicyMappingRequest = new PartnersPolicyMappingRequest();
		partnersPolicyMappingRequest.setOldPolicyID("456789");
		partnersPolicyMappingRequest.setNewPolicyID("567890");
		
		Partner partner = new Partner();
		partner.setId("56784567");
		partner.setAddress("address");
		partner.setContactNo("47384384");
		partner.setEmailId("xyz@hotmail.com");
		partner.setName("name");
		partner.setPolicyGroupId("567890");
		partner.setIsActive(true);
		partner.setUpdBy("Partner Service");
		partner.setUpdDtimes(Timestamp.valueOf(now));
	
		PartnerPolicy part_policy = new PartnerPolicy();
		part_policy.setCrBy("Partner Manager");
		part_policy.setCrDtimes(Timestamp.valueOf(now));
		part_policy.setIsActive(true);
		part_policy.setIsDeleted(true);
		part_policy.setPartner(partner);
		part_policy.setPolicyId("567890");
		
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setName("Name");
		policyGroup.setId("567890");
		policyGroup.setIsActive(true);
		policyGroup.setUserId("UserId");
		policyGroup.setCrBy("CreatedBy");
		
		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.setName("name");
		authPolicy.setId("567890");
		authPolicy.setCrBy("admin");
		authPolicy.setIsActive(false);
		authPolicy.setCrDtimes(Timestamp.valueOf(now));
		authPolicy.setDelDtimes(LocalDateTime.now());
		authPolicy.setDescr("authPolicy");
		authPolicy.setPolicyGroup(policyGroup);
		
		
		Optional<PolicyGroup> opt_PolicyGroup = Optional.of(policyGroup);
		Optional<AuthPolicy> opt_authPolicy = Optional.of(authPolicy);
		Optional<PartnerPolicy> partnerPolicy = Optional.of(part_policy);
		Mockito.when(partnerPolicyRepository.findById(PolicyAPIKey)).thenReturn(partnerPolicy);
		Mockito.when(authPolicyRepository.findById(partnersPolicyMappingRequest.getNewPolicyID())).thenReturn(opt_authPolicy);
		Mockito.when(authPolicyRepository.findByPolicyGroupAndId(Mockito.anyString(), Mockito.anyString())).thenReturn(authPolicy);
		Mockito.when(policyGroupRepository.findById(partnersPolicyMappingRequest.getNewPolicyID())).thenReturn(opt_PolicyGroup);
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndPolicyIdAndApikey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(part_policy);
		partnerManagementImpl.updatePolicyAgainstApikey(partnersPolicyMappingRequest, partnerID, PolicyAPIKey);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void partnerApiKeyPolicyMappingsTest05(){
		LocalDateTime now = LocalDateTime.now();
		String partnerID = "56784567";
		String PolicyAPIKey = "56784567";
		
		
		PartnersPolicyMappingRequest partnersPolicyMappingRequest = new PartnersPolicyMappingRequest();
		partnersPolicyMappingRequest.setOldPolicyID("456789");
		partnersPolicyMappingRequest.setNewPolicyID("567890");
		
		Partner partner = new Partner();
		partner.setId("56784567");
		partner.setAddress("address");
		partner.setContactNo("47384384");
		partner.setEmailId("xyz@hotmail.com");
		partner.setName("name");
		partner.setPolicyGroupId("567890");
		partner.setIsActive(true);
		partner.setUpdBy("Partner Service");
		partner.setUpdDtimes(Timestamp.valueOf(now));
	
		PartnerPolicy part_policy = new PartnerPolicy();
		part_policy.setCrBy("Partner Manager");
		part_policy.setCrDtimes(Timestamp.valueOf(now));
		part_policy.setIsActive(false);
		part_policy.setIsDeleted(true);
		part_policy.setPartner(partner);
		part_policy.setPolicyId("567890");
		
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setName("Name");
		policyGroup.setId("567890");
		policyGroup.setIsActive(true);
		policyGroup.setUserId("UserId");
		policyGroup.setCrBy("CreatedBy");
		
		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.setName("name");
		authPolicy.setId("567890");
		authPolicy.setCrBy("admin");
		authPolicy.setIsActive(true);
		authPolicy.setCrDtimes(Timestamp.valueOf(now));
		authPolicy.setDelDtimes(LocalDateTime.now());
		authPolicy.setDescr("authPolicy");
		authPolicy.setPolicyGroup(policyGroup);
		
		
		Optional<PolicyGroup> opt_PolicyGroup = Optional.of(policyGroup);
		Optional<AuthPolicy> opt_authPolicy = Optional.of(authPolicy);
		Optional<PartnerPolicy> partnerPolicy = Optional.of(part_policy);
		Mockito.when(partnerPolicyRepository.findById(PolicyAPIKey)).thenReturn(partnerPolicy);
		Mockito.when(authPolicyRepository.findById(partnersPolicyMappingRequest.getNewPolicyID())).thenReturn(opt_authPolicy);
		Mockito.when(authPolicyRepository.findByPolicyGroupAndId(Mockito.anyString(), Mockito.anyString())).thenReturn(authPolicy);
		Mockito.when(policyGroupRepository.findById(partnersPolicyMappingRequest.getNewPolicyID())).thenReturn(opt_PolicyGroup);
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndPolicyIdAndApikey(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(part_policy);
		partnerManagementImpl.updatePolicyAgainstApikey(partnersPolicyMappingRequest, partnerID, PolicyAPIKey);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void activateDeactivateAuthEKYCPartnerTest_S1() {
		StatusRequestDto req = new StatusRequestDto();
		req.setStatus("De-Active");
		String partnerId = "12345";
		partnerManagementImpl.activateDeactivateAuthEKYCPartner(partnerId, req);
	}	
	
	@Test(expected = PartnerManagerServiceException.class)
	public void activateDeactivateAuthEKYCPartnerTest_S3() {
		StatusRequestDto req = new StatusRequestDto();
		req.setStatus("ctive");
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		partnerManagementImpl.activateDeactivateAuthEKYCPartner(partnerId, req);
	}
	
	@Test
	public void activateDeactivateAuthEKYCPartnerTest_S4() {
		StatusRequestDto req = new StatusRequestDto();
		req.setStatus("Active");
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		partnerManagementImpl.activateDeactivateAuthEKYCPartner(partnerId, req);
		req.setStatus("De-Active");
		partner.get().setPartnerTypeCode("MISP_Partner");
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		MISPLicenseEntityV2 license = new MISPLicenseEntityV2();
		MISPLicenseEntityPK id = new MISPLicenseEntityPK();
		id.setLicenseKey("qwertyhgfdsdfghb");
		license.setIsActive(true);
		id.setMispId("12345");
		license.setId(id);
		license.setValidFromDate(LocalDateTime.now().minusDays(1));
		license.setValidToDate(LocalDateTime.now().plusDays(5));
		Mockito.when(mispLicenseV2Repository.findActiveLicenseKeyByPartnerId("123456")).thenReturn(List.of(license));
		Map<String, Object> response = new HashMap<>();
		response.put("response", getCertResponse());
		response.put("id",null);
		response.put("version", null);
		Mockito.when(keyManagerHelper.encryptData(any())).thenReturn("encrypted-data");
		Mockito.when(keyManagerHelper.decryptData(any())).thenReturn("decrypted-data");
		Mockito.when(restUtil.getApi(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(response);
		partnerManagementImpl.activateDeactivateAuthEKYCPartner(partnerId, req);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void activateDeactivateAuthEKYCPartnerTest_S5() {
		StatusRequestDto req = new StatusRequestDto();
		req.setStatus("Active");
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(getPartner());
		partner.get().setCertificateAlias(null);
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		partnerManagementImpl.activateDeactivateAuthEKYCPartner(partnerId, req);
	}
	
	@Test
	public void activateDeactivateAuthEKYCPartnerTest_S6() {
		StatusRequestDto req = new StatusRequestDto();
		Map<String, Object> response = new HashMap<>();
		response.put("response", getCertResponse());
		response.put("id",null);
		response.put("version", null);
		req.setStatus("Active");
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(getPartner());
		partner.get().setApprovalStatus("approved");
		partner.get().setIsActive(true);
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		Mockito.when(restUtil.getApi(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(response);
		partnerManagementImpl.activateDeactivateAuthEKYCPartner(partnerId, req);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void activateDeactivateAuthEKYCPartnerTest_S8() {
		StatusRequestDto req = new StatusRequestDto();
		Map<String, Object> response = new HashMap<>();
		response.put("response", getCertResponse());
		response.put("id",null);
		response.put("version", null);
		req.setStatus("NOTActive");
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(getPartner());
		partner.get().setIsActive(false);
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		Mockito.when(restUtil.getApi(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(response);
		partnerManagementImpl.activateDeactivateAuthEKYCPartner(partnerId, req);
	}
	
	@Test
	public void activateDeactivateAuthEKYCPartnerTest_S7() {
		StatusRequestDto req = new StatusRequestDto();
		req.setStatus("De-active");
		String partnerId = "12345";
		Map<String, Object> response = new HashMap<>();
		response.put("response", getCertResponse());
		response.put("id",null);
		response.put("version", null);
		req.setStatus("De-active");
		Optional<Partner> partner = Optional.of(getPartner());
		partner.get().setIsActive(true);
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		Mockito.when(keyManagerHelper.encryptData(any())).thenReturn("encrypted-data");
		Mockito.when(keyManagerHelper.decryptData(any())).thenReturn("decrypted-data");
		Mockito.when(restUtil.getApi(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(response);
		partnerManagementImpl.activateDeactivateAuthEKYCPartner(partnerId, req);
	}	
	
	@Test(expected = PartnerManagerServiceException.class)
	public void getAllAuthEKYCPartnersForThePolicyGroupTest_S1() {
		partnerManagementImpl.getAllAuthEKYCPartnersForThePolicyGroup(Optional.empty());
	}
	
	@Test
	public void getAllAuthEKYCPartnersForThePolicyGroupTest_S2() {		
		List<Partner> partners = new ArrayList<Partner>();
		partners.add(getPartner());
		Mockito.when(keyManagerHelper.encryptData(any())).thenReturn("encrypted-data");
		Mockito.when(keyManagerHelper.decryptData(any())).thenReturn("decrypted-data");
		Mockito.when(partnerRepository.findAll()).thenReturn(partners);
		partnerManagementImpl.getAllAuthEKYCPartnersForThePolicyGroup(Optional.empty());
	}
	
	@Test
	public void getAllAuthEKYCPartnersForThePolicyGroupTest_S3() {		
		List<Partner> partners = new ArrayList<Partner>();
		Optional<Partner> partner = Optional.of(getPartner());
		Partner part = partner.get();
		part.setIsActive(false);
		partners.add(part);
		Mockito.when(keyManagerHelper.encryptData(any())).thenReturn("encrypted-data");
		Mockito.when(keyManagerHelper.decryptData(any())).thenReturn("decrypted-data");
		Mockito.when(partnerRepository.findAll()).thenReturn(partners);
		partnerManagementImpl.getAllAuthEKYCPartnersForThePolicyGroup(Optional.empty());
	}
	
	@Test
	public void getAllAuthEKYCPartnersForThePolicyGroupTest_S4() {		
		List<Partner> partners = new ArrayList<Partner>();
		Optional<Partner> partner = Optional.of(getPartner());
		Partner part = partner.get();
		part.setIsActive(false);
		partners.add(part);
		Mockito.when(partnerRepository.findAll()).thenReturn(partners);
		Mockito.when(partnerRepository.findByPartnerType(Mockito.anyString())).thenReturn(partners);
		Mockito.when(keyManagerHelper.encryptData(any())).thenReturn("encrypted-data");
		Mockito.when(keyManagerHelper.decryptData(any())).thenReturn("decrypted-data");
		partnerManagementImpl.getAllAuthEKYCPartnersForThePolicyGroup(Optional.of("Auth_Partner"));
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void getPartnerAPIKeyToPolicyMappingTest_S1() {
		String partnerId = "123456";
		String apiKey ="2345";
		partnerManagementImpl.getPartnerAPIKeyToPolicyMapping(partnerId, apiKey);
	}
	
	@Test
	public void getPartnerAPIKeyToPolicyMappingTest_S2() {
		String partnerId = "123456";
		String apiKey ="2345";
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		Mockito.when(partnerPolicyRepository.findById(apiKey)).thenReturn(partnerPolicy);
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndApikey(Mockito.anyString(), Mockito.anyString())).thenReturn(getPartnerPolicy());
		partnerManagementImpl.getPartnerAPIKeyToPolicyMapping(partnerId, apiKey);
	}
	
	@Test
	public void getPartnerAPIKeyToPolicyMappingTest_S3() {
		String partnerId = "123456";
		String apiKey ="2345";
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndApikey(Mockito.anyString(), Mockito.anyString())).thenReturn(getPartnerPolicy());
		Mockito.when(partnerPolicyRepository.findById(apiKey)).thenReturn(partnerPolicy);
		Mockito.when(authPolicyRepository.findById("234")).thenReturn(Optional.of(getAuthPolicies().get(0)));
		partnerManagementImpl.getPartnerAPIKeyToPolicyMapping(partnerId, apiKey);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void getPartnerAPIKeyToPolicyMappingTest_S4() {
		String partnerId = "asddsasd";
		String apiKey ="2345";
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		Mockito.when(partnerPolicyRepository.findById(apiKey)).thenReturn(partnerPolicy);
		Mockito.when(authPolicyRepository.findById("234")).thenReturn(Optional.of(getAuthPolicies().get(0)));
		partnerManagementImpl.getPartnerAPIKeyToPolicyMapping(partnerId, apiKey);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagersTest_S1() {
		
		partnerManagementImpl.getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagers();
	}
	
	@Test
	public void getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagersTest_S2() {
		List<PartnerPolicyRequest> partnerPolicyRequests = new ArrayList<PartnerPolicyRequest>();
		partnerPolicyRequests.add(getPartnerPolicyRequestData());
		Mockito.when(partnerPolicyRequestRepository.findAll()).thenReturn(partnerPolicyRequests);		
		partnerManagementImpl.getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagers();
	}
	
	@Test
	public void getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagersTest_S3() {
		List<PartnerPolicyRequest> partnerPolicyRequests = new ArrayList<PartnerPolicyRequest>();
		partnerPolicyRequests.add(getPartnerPolicyRequestData());
		Mockito.when(partnerPolicyRequestRepository.findAll()).thenReturn(partnerPolicyRequests);	
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById("123456")).thenReturn(partner);
		partnerManagementImpl.getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagers();
	}
	
	@Test
	public void getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagersTest_S4() {
		List<PartnerPolicyRequest> partnerPolicyRequests = new ArrayList<PartnerPolicyRequest>();
		partnerPolicyRequests.add(getPartnerPolicyRequestData());
		Mockito.when(partnerPolicyRequestRepository.findAll()).thenReturn(partnerPolicyRequests);	
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById("123456")).thenReturn(partner);
		Optional<PolicyGroup> policyGroup = Optional.of(getPolicyGroupData());
		Mockito.when(policyGroupRepository.findById("234")).thenReturn(policyGroup);
		partnerManagementImpl.getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagers();
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestIdTest_S1() {
		partnerManagementImpl.getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestId("12345");
	}
	
	@Test
	public void getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestIdTest_S2() {
		String apiKey ="2345";	
		Mockito.when(partnerPolicyRequestRepository.findById(apiKey)).thenReturn(Optional.of(getPartnerPolicyRequestData()));			
		partnerManagementImpl.getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestId(apiKey);
	}
	
	@Test
	public void getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestIdTest_S3() {
		String apiKey ="2345";	
		Mockito.when(partnerPolicyRequestRepository.findById(apiKey)).thenReturn(Optional.of(getPartnerPolicyRequestData()));
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById("123456")).thenReturn(partner);
		partnerManagementImpl.getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestId(apiKey);
	}
	
	@Test
	public void getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestIdTest_S4() {
		String apiKey ="2345";	
		Mockito.when(partnerPolicyRequestRepository.findById(apiKey)).thenReturn(Optional.of(getPartnerPolicyRequestData()));
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById("123456")).thenReturn(partner);	
		Optional<PolicyGroup> policyGroup = Optional.of(getPolicyGroupData());
		Mockito.when(policyGroupRepository.findById("2345")).thenReturn(policyGroup);
		partnerManagementImpl.getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestId(apiKey);
	}
	
	@Test
	public void updateAPIKeyStatusTest01() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		APIkeyStatusUpdateRequestDto statusDto = new APIkeyStatusUpdateRequestDto();
		statusDto.setLabel("456");
		statusDto.setStatus("De-Activate");
		Mockito.when(partnerPolicyRepository.findByPartnerIdPolicyIdAndLabel(Mockito.any(), Mockito.any(),
				Mockito.any())).thenReturn(getPartnerPolicy());

		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "PARTNER_ADMIN");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		try {
		partnerManagementImpl.updateAPIKeyStatus("1234", "456",statusDto);
		}catch (PartnerManagerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.INVALID_STATUS_CODE_ACTIVE_DEACTIVE.getErrorCode()));
		}
	}
	
	@Test
	public void updateAPIKeyStatusTest02() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		APIkeyStatusUpdateRequestDto statusDto = new APIkeyStatusUpdateRequestDto();
		statusDto.setLabel("456");
		statusDto.setStatus("De-Active");
		Mockito.when(keyManagerHelper.encryptData(any())).thenReturn("encrypted-data");
		Mockito.when(keyManagerHelper.decryptData(any())).thenReturn("decrypted-data");
		Mockito.when(partnerPolicyRepository.findByPartnerIdPolicyIdAndLabel(Mockito.any(), Mockito.any(),
				Mockito.any())).thenReturn(getPartnerPolicy());
		Mockito.when(authPolicyRepository.findById(Mockito.any())).thenReturn(Optional.of(getAuthPolicies().get(0)));

		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "PARTNER_ADMIN");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		partnerManagementImpl.updateAPIKeyStatus("1234", "456", statusDto);
	}
	
	@Test
	public void updateAPIKeyStatusTest03() {
		APIkeyStatusUpdateRequestDto statusDto = new APIkeyStatusUpdateRequestDto();
		statusDto.setLabel("456");
		statusDto.setStatus("De-Active");
		Mockito.when(partnerPolicyRepository.findByPartnerIdPolicyIdAndLabel(Mockito.any(), Mockito.any(),
				Mockito.any())).thenReturn(null);
		Mockito.when(authPolicyRepository.findById(Mockito.any())).thenReturn(Optional.of(getAuthPolicies().get(0)));
		try {
		partnerManagementImpl.updateAPIKeyStatus("1234", "456", statusDto);
		}catch (PartnerManagerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.PARTNER_POLICY_LABEL_NOT_EXISTS.getErrorCode()));
		}
	}
	
	@Test
	public void updateAPIKeyStatusTest04() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		APIkeyStatusUpdateRequestDto statusDto = new APIkeyStatusUpdateRequestDto();
		statusDto.setLabel("456");
		statusDto.setStatus("Active");
		Mockito.when(keyManagerHelper.encryptData(any())).thenReturn("encrypted-data");
		Mockito.when(keyManagerHelper.decryptData(any())).thenReturn("decrypted-data");
		Mockito.when(partnerPolicyRepository.findByPartnerIdPolicyIdAndLabel(Mockito.any(), Mockito.any(),
				Mockito.any())).thenReturn(getPartnerPolicy());
		Mockito.when(authPolicyRepository.findById(Mockito.any())).thenReturn(Optional.of(getAuthPolicies().get(0)));

		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "PARTNER_ADMIN");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		partnerManagementImpl.updateAPIKeyStatus("1234", "456", statusDto);
	}

	@Test
	public void updateAPIKeyStatusTest05() throws Exception {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "Auth_Partner");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		APIkeyStatusUpdateRequestDto statusDto = new APIkeyStatusUpdateRequestDto();
		statusDto.setLabel("456");
		statusDto.setStatus("De-Activate");

		PartnerPolicy partnerPolicy = getPartnerPolicy();
		Partner partner = getPartner();
		partner.setIsActive(false);
		partnerPolicy.setPartner(partner);
		Mockito.when(partnerPolicyRepository.findByPartnerIdPolicyIdAndLabel(Mockito.any(), Mockito.any(),
				Mockito.any())).thenReturn(partnerPolicy);
		try {
			partnerManagementImpl.updateAPIKeyStatus("1234", "456", statusDto);
		}catch (PartnerManagerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorCode()));
		}
	}

	@Test
	public void updateAPIKeyStatusTest06() throws Exception {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "Auth_Partner");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		APIkeyStatusUpdateRequestDto statusDto = new APIkeyStatusUpdateRequestDto();
		statusDto.setLabel("456");
		statusDto.setStatus("De-active");

		PartnerPolicy partnerPolicy = getPartnerPolicy();
		partnerPolicy.setIsActive(false);
		Mockito.when(partnerPolicyRepository.findByPartnerIdPolicyIdAndLabel(Mockito.any(), Mockito.any(),
				Mockito.any())).thenReturn(partnerPolicy);
		try {
			partnerManagementImpl.updateAPIKeyStatus("1234", "456", statusDto);
		}catch (PartnerManagerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.PARTNER_APIKEY_NOT_ACTIVE_EXCEPTION.getErrorCode()));
		}
	}
	
	@Test
	public void approveRejectPartnerPolicyMappingTest01() {
		StatusRequestDto request = new StatusRequestDto();
		request.setStatus("Approved");
		Mockito.when(partnerPolicyRequestRepository.findById(Mockito.any())).thenReturn(Optional.of(getPartnerPolicyRequestData()));
		Mockito.when(authPolicyRepository.findById(Mockito.any())).thenReturn(Optional.of(getAuthPolicies().get(0)));
		Mockito.when(partnerPolicyBioextractRequestRepository.findByPartnerPolicyRequestIdAndStatusCode(Mockito.any(), Mockito.any())).thenReturn(List.of(new PartnerPolicyBioextractRequest()));
		Mockito.when(partnerPolicyCredentialTypeRequestRepository.findByPartnerPolicyRequestIdAndStatusCode(Mockito.any(), Mockito.any())).thenReturn(List.of(new PartnerPolicyCredentialTypeRequest()));
		partnerManagementImpl.approveRejectPartnerPolicyMapping("1234", request);
	}
	
	@Test
	public void approveRejectPartnerPolicyMappingTest02() {
		StatusRequestDto request = new StatusRequestDto();
		request.setStatus("Rejected");
		Mockito.when(partnerPolicyRequestRepository.findById(Mockito.any())).thenReturn(Optional.of(getPartnerPolicyRequestData()));
		Mockito.when(authPolicyRepository.findById(Mockito.any())).thenReturn(Optional.of(getAuthPolicies().get(0)));
		partnerManagementImpl.approveRejectPartnerPolicyMapping("1234", request);
	}
	
	@Test
	public void approveRejectPartnerPolicyMappingTest03() {
		StatusRequestDto request = new StatusRequestDto();
		request.setStatus("Rejeted");
		Mockito.when(partnerPolicyRequestRepository.findById(Mockito.any())).thenReturn(Optional.of(getPartnerPolicyRequestData()));
		Mockito.when(authPolicyRepository.findById(Mockito.any())).thenReturn(Optional.of(getAuthPolicies().get(0)));
		try {
			partnerManagementImpl.approveRejectPartnerPolicyMapping("1234", request);
		}catch (PartnerManagerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.INVALID_STATUS_CODE.getErrorCode()));
		}
	}
	
	@Test
	public void approveRejectPartnerPolicyMappingTest04() {
		StatusRequestDto request = new StatusRequestDto();
		request.setStatus("Rejected");
		Mockito.when(partnerPolicyRequestRepository.findById(Mockito.any())).thenReturn(Optional.empty());
		Mockito.when(authPolicyRepository.findById(Mockito.any())).thenReturn(Optional.of(getAuthPolicies().get(0)));
		try {
			partnerManagementImpl.approveRejectPartnerPolicyMapping("1234", request);
		}catch (PartnerManagerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.PARTNER_API_DOES_NOT_EXIST_EXCEPTION.getErrorCode()));
		}
		PartnerPolicyRequest partnerPolicyRequestFromDb = getPartnerPolicyRequestData();
		partnerPolicyRequestFromDb.setStatusCode("approved");
		Mockito.when(partnerPolicyRequestRepository.findById(Mockito.any())).thenReturn(Optional.of(partnerPolicyRequestFromDb));
		try {
			partnerManagementImpl.approveRejectPartnerPolicyMapping("1234", request);
		}catch (PartnerManagerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.POLICY_REQUEST_ALREADY_APPROVED.getErrorCode()));
		}			
		partnerPolicyRequestFromDb.setStatusCode("rejected");
		Mockito.when(partnerPolicyRequestRepository.findById(Mockito.any())).thenReturn(Optional.of(partnerPolicyRequestFromDb));
		try {
			partnerManagementImpl.approveRejectPartnerPolicyMapping("1234", request);
		}catch (PartnerManagerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.POLICY_REQUEST_ALREADY_REJECTED.getErrorCode()));
		}		

		PartnerPolicyRequest partnerPolicyRequestFromDb1 = getPartnerPolicyRequestData();
		partnerPolicyRequestFromDb1.getPartner().setPartnerTypeCode("Credential_Partner");
		Mockito.when(partnerPolicyRequestRepository.findById(Mockito.any())).thenReturn(Optional.of(partnerPolicyRequestFromDb1));
		Mockito.when(partnerPolicyBioextractRequestRepository.findByPartnerPolicyRequestIdAndStatusCode(Mockito.any(),Mockito.any())).thenReturn(Collections.emptyList());
		try {
			partnerManagementImpl.approveRejectPartnerPolicyMapping("1234", request);
		}catch (PartnerManagerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.EXTRACTORS_NOT_PRESENT.getErrorCode()));
		}
	}
	
	@Test
	public void approveRejectPartnerPolicyMappingTest05() {
		StatusRequestDto request = new StatusRequestDto();
		request.setStatus("Approved");
		Mockito.when(partnerPolicyRequestRepository.findById(Mockito.any())).thenReturn(Optional.of(getPartnerPolicyRequestData()));
		Mockito.when(partnerPolicyBioextractRequestRepository.findByPartnerPolicyRequestIdAndStatusCode(Mockito.any(), Mockito.any())).thenReturn(List.of(new PartnerPolicyBioextractRequest()));
		Mockito.when(partnerPolicyCredentialTypeRequestRepository.findByPartnerPolicyRequestIdAndStatusCode(Mockito.any(), Mockito.any())).thenReturn(List.of(new PartnerPolicyCredentialTypeRequest()));
		Mockito.when(authPolicyRepository.findById(Mockito.any())).thenReturn(Optional.empty());
		try {
			partnerManagementImpl.approveRejectPartnerPolicyMapping("1234", request);
		}catch (PartnerManagerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.PARTNER_NOT_MAPPED_TO_POLICY_EXCEPTION.getErrorCode()));
		}
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setIsActive(false);
		Mockito.when(authPolicyRepository.findById(Mockito.any())).thenReturn(Optional.of(authPolicy));
		try {
			partnerManagementImpl.approveRejectPartnerPolicyMapping("1234", request);
		}catch (PartnerManagerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.PARTNER_POLICY_NOT_ACTIVE_EXCEPTION.getErrorCode()));
		}
		
		authPolicy = getAuthPolicies().get(0);
		authPolicy.setPolicyGroup(null);
		Mockito.when(authPolicyRepository.findById(Mockito.any())).thenReturn(Optional.of(authPolicy));
		try {
			partnerManagementImpl.approveRejectPartnerPolicyMapping("1234", request);
		}catch (PartnerManagerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.POLICY_GROUP_NOT_EXISTS.getErrorCode()));
		}
		
		authPolicy = getAuthPolicies().get(0);
		authPolicy.getPolicyGroup().setIsActive(false);
		Mockito.when(authPolicyRepository.findById(Mockito.any())).thenReturn(Optional.of(authPolicy));
		try {
			partnerManagementImpl.approveRejectPartnerPolicyMapping("1234", request);
		}catch (PartnerManagerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.POLICY_GROUP_NOT_ACTIVE.getErrorCode()));
		}
		
		authPolicy = getAuthPolicies().get(0);
		authPolicy.setValidToDate(LocalDateTime.now().minusDays(1));
		Mockito.when(authPolicyRepository.findById(Mockito.any())).thenReturn(Optional.of(authPolicy));
		try {
			partnerManagementImpl.approveRejectPartnerPolicyMapping("1234", request);
		}catch (PartnerManagerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.PARTNER_POLICY_EXPIRED_EXCEPTION.getErrorCode()));
		}
	}
	
	@Test
	@WithUserDetails("partner")
	public void generateAPIKeyTest() {
		APIKeyGenerateRequestDto request = new APIKeyGenerateRequestDto();
		request.setLabel("unique");
		request.setPolicyName("policyName");
		Optional<Partner> newPartner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById("partner")).thenReturn(newPartner);
		Mockito.when(authPolicyRepository.findByPolicyGroupIdAndName(newPartner.get().getPolicyGroupId(),request.getPolicyName())).thenReturn(getAuthPolicies().get(0));
		Mockito.when((partnerPolicyRequestRepository.findByPartnerIdAndPolicyIdAndStatusCode(Mockito.any(),Mockito.any(),Mockito.any()))).thenReturn(List.of(getPartnerPolicyRequestData()));
		Mockito.when(partnerPolicyRepository.findByPartnerIdPolicyIdAndLabel("partner","234","unique")).thenReturn(null);
		Map<String, Object> response = new HashMap<>();
		response.put("response", getCertResponse());
		response.put("id",null);
		response.put("version", null);
		Mockito.when(restUtil.getApi(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(response);
		partnerManagementImpl.generateAPIKey("partner", request);		
		try {
			partnerManagementImpl.generateAPIKey("InvalidPartner", request);
		}catch (PartnerManagerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.LOGGEDIN_USER_NOT_AUTHORIZED.getErrorCode()));
		}
		Mockito.when(partnerRepository.findById("partner")).thenReturn(Optional.empty());
		try {
			partnerManagementImpl.generateAPIKey("partner", request);
		}catch (PartnerManagerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.PARTNER_ID_DOES_NOT_EXIST_EXCEPTION.getErrorCode()));
		}
		Optional<Partner> partner = Optional.of(getPartner());
		partner.get().setIsActive(false);
		Mockito.when(partnerRepository.findById("partner")).thenReturn(partner);
		try {
			partnerManagementImpl.generateAPIKey("partner", request);
		}catch (PartnerManagerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.PARTNER_NOT_ACTIVE_EXCEPTION.getErrorCode()));
		}
		Mockito.when(partnerRepository.findById("partner")).thenReturn(newPartner);
		Mockito.when(authPolicyRepository.findByPolicyGroupIdAndName(request.getPolicyName(),newPartner.get().getPolicyGroupId())).thenReturn(null);
		try {
			partnerManagementImpl.generateAPIKey("partner", request);
		}catch (PartnerManagerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.POLICY_NOT_EXIST_EXCEPTION.getErrorCode()));
		}
		Mockito.when(partnerRepository.findById("partner")).thenReturn(newPartner);
		Mockito.when(authPolicyRepository.findByPolicyGroupIdAndName(newPartner.get().getPolicyGroupId(),request.getPolicyName())).thenReturn(getAuthPolicies().get(0));
		Mockito.when((partnerPolicyRequestRepository.findByPartnerIdAndPolicyIdAndStatusCode(Mockito.any(),Mockito.any(),Mockito.any()))).thenReturn(Collections.emptyList());
		try {
			partnerManagementImpl.generateAPIKey("partner", request);
		}catch (PartnerManagerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.PARTNER_POLICY_MAPPING_NOT_EXISTS.getErrorCode()));
		}
		Mockito.when(authPolicyRepository.findByPolicyGroupIdAndName(request.getPolicyName(),newPartner.get().getPolicyGroupId())).thenReturn(getAuthPolicies().get(0));
		Mockito.when((partnerPolicyRequestRepository.findByPartnerIdAndPolicyIdAndStatusCode(Mockito.any(),Mockito.any(),Mockito.any()))).thenReturn(List.of(getPartnerPolicyRequestData()));
		Mockito.when(partnerRepository.findById("partner")).thenReturn(Optional.of(getPartner()));
		Mockito.when(partnerPolicyRepository.findByPartnerIdPolicyIdAndLabel("123456","234","unique")).thenReturn(getPartnerPolicy());
		try {
			partnerManagementImpl.generateAPIKey("partner", request);
		}catch (PartnerManagerServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorCode.PARTNER_POLICY_LABEL_EXISTS.getErrorCode()));
		}
	}

	private JSONObject getCertResponse() {
		JSONObject obj=new JSONObject();		
		obj.put("certificateData", "I6RNkys7tjbmOQhJkgY1HhRpvts8LZPioJD4I82wsMHDtGj");
		obj.put("timestamp", "2021-09-02T07:43:15.577329");		
		return obj;
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void partnerApiKeyPolicyMappingsTest_S1() {
		PartnersPolicyMappingRequest request = createRequest();
		partnerManagementImpl.updatePolicyAgainstApikey(request,"1234","543");
	}
	
	private PartnersPolicyMappingRequest createRequest() {
		PartnersPolicyMappingRequest req = new PartnersPolicyMappingRequest();
		req.setOldPolicyID("1234");
		req.setNewPolicyID("456");
		return req;
	}
	
	private PolicyGroup getPolicyGroupData() {
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setName("Name");
		policyGroup.setId("12133");
		policyGroup.setIsActive(true);
		policyGroup.setUserId("UserId");
		policyGroup.setCrBy("CreatedBy");
		return policyGroup;
	}

	private PartnerPolicyRequest getPartnerPolicyRequestData() {
		PartnerPolicyRequest partnerPolicyRequest = new PartnerPolicyRequest();
		partnerPolicyRequest.setId("1234456");
		partnerPolicyRequest.setPartner(getPartner());
		partnerPolicyRequest.setPolicyId("2345");
		partnerPolicyRequest.setStatusCode("in-progress");
		return partnerPolicyRequest;
	}
	
	private List<AuthPolicy> getAuthPolicies(){
		AuthPolicy policy = new AuthPolicy();
		List<AuthPolicy> policies = new ArrayList<AuthPolicy>();
		policy.setPolicyGroup(policyGroupData());
		policy.setId("234");
		policy.setName("Test");
		policy.setDescr("Policy Desc");
		policy.setIsActive(true);
		policy.setValidToDate(LocalDateTime.now().plusDays(5));
		policy.setPolicyFileId("{\"authPolicies\":[{\"authType\":\"otp\",\"authSubType\":null,\"mandatory\":true},{\"authType\":\"demo\",\"authSubType\":null,\"mandatory\":false},{\"authType\":\"bio\",\"authSubType\":\"FINGER\",\"mandatory\":true},{\"authType\":\"bio\",\"authSubType\":\"IRIS\",\"mandatory\":false},{\"authType\":\"bio\",\"authSubType\":\"FACE\",\"mandatory\":false},{\"authType\":\"kyc\",\"authSubType\":null,\"mandatory\":false}],\"allowedKycAttributes\":[{\"attributeName\":\"fullName\",\"required\":true},{\"attributeName\":\"dateOfBirth\",\"required\":true},{\"attributeName\":\"gender\",\"required\":true},{\"attributeName\":\"phone\",\"required\":true},{\"attributeName\":\"email\",\"required\":true},{\"attributeName\":\"addressLine1\",\"required\":true},{\"attributeName\":\"addressLine2\",\"required\":true},{\"attributeName\":\"addressLine3\",\"required\":true},{\"attributeName\":\"location1\",\"required\":true},{\"attributeName\":\"location2\",\"required\":true},{\"attributeName\":\"location3\",\"required\":true},{\"attributeName\":\"postalCode\",\"required\":false},{\"attributeName\":\"photo\",\"required\":true}]}");
		policies.add(policy);
		return policies;		
	}
	
	private PolicyGroup policyGroupData() {
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("12345");
		policyGroup.setName("Test");
		policyGroup.setIsActive(true);
		return policyGroup;
	}
	private PartnerPolicy getPartnerPolicy() {
		PartnerPolicy partnerPolicy = new PartnerPolicy();
		partnerPolicy.setPolicyApiKey("567890");
		partnerPolicy.setPartner(getPartner());
		partnerPolicy.setIsActive(true);
		partnerPolicy.setPolicyId("567890");
		partnerPolicy.setValidFromDatetime(Timestamp.valueOf(LocalDateTime.now().minusDays(5)));
		partnerPolicy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(5)));
		return partnerPolicy;
	}
	private Partner getPartner() {
		Partner part = new Partner();
		part.setAddress("Bane");
		part.setContactNo("0123456789");
		part.setIsActive(true);
		part.setId("123456");
		part.setCertificateAlias("abcd");
		part.setPolicyGroupId("234");
		part.setPartnerTypeCode("Auth");
		part.setApprovalStatus("approved");
		return part;
	}

	@Test
	public void getPartnerDetailsTest01() throws Exception {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "PARTNER_ADMIN");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("FTM_Provider");
		partner.setApprovalStatus("approved");
		partner.setIsActive(true);
		partner.setCertificateAlias("abs");
		partner.setPolicyGroupId("121");
		partner.setCrDtimes(Timestamp.valueOf(LocalDateTime.now()));
		partner.setName("abc");
		partner.setEmailId("abc@gmail.com");
		partner.setContactNo("1234567890");
		partnerList.add(partner);
		when(partnerServiceRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(partnerServiceRepository.findById(any())).thenReturn(Optional.of(partner));

		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setName("policygroup123");
		policyGroup.setDesc("descr");
		when(policyGroupRepository.findPolicyGroupById(anyString())).thenReturn(policyGroup);

		PartnerCertDownloadResponeDto partnerCertDownloadResponeDto = new PartnerCertDownloadResponeDto();
		String certificate = "-----BEGIN CERTIFICATE-----\n" +
				"MIIFfTCCA2WgAwIBAgIUOVZNyD46U0OAEhaGC/Y7NXbu+OkwDQYJKoZIhvcNAQEL\n" +
				"BQAwTjELMAkGA1UEBhMCSU4xCzAJBgNVBAgMAk1IMQswCQYDVQQHDAJQTjELMAkG\n" +
				"A1UECgwCQ0ExCzAJBgNVBAsMAkNBMQswCQYDVQQDDAJDQTAeFw0yNDA1MDkwNzI1\n" +
				"MDJaFw0yOTA1MDkwNzI1MDJaME4xCzAJBgNVBAYTAklOMQswCQYDVQQIDAJNSDEL\n" +
				"MAkGA1UEBwwCUE4xCzAJBgNVBAoMAkNBMQswCQYDVQQLDAJDQTELMAkGA1UEAwwC\n" +
				"Q0EwggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAwggIKAoICAQCzdWD2DvhSnmLqU3fX\n" +
				"RT3z8ikS6qHxn5Hu/a2ijkuZxAZj0UCUJ83kM20NwocJDHT1qx6+yjdl+BECsgoI\n" +
				"ro9MXgFOsHCphyR5KiP4mY95qRlE03h7WBfr4wDn/6f5tCbqCcBqdXMAQxUp34D+\n" +
				"Pro0EwkXNulHNMTvz5hpoCEiGyfXUP48I4q2nb8rMXaplhqz+vAYgA4rsK6K9IUh\n" +
				"uJDxtZRHdIfxnvbfjxDbuPkN0ehOQ1uQrDVY6ENCIUxdgR/p94kZ+CNsD21c57gJ\n" +
				"2wYg+BceQn1rVSGnfpqMoogZCMUWFvaE4i91419VXxDLgeC/4Qw8n5onBY+dVHjW\n" +
				"04OolR2DqotFyaPlZiVdpUys6+KZ7fS9mwWEY0kqtLzcBeb4g4nPvObfKnqSmVMZ\n" +
				"DHRuAx6MG3oFZrnNuS6oIYGwLpoko6iqEiGohHsSxMulT43XOxoNgDq9noQc9SYv\n" +
				"tzdzijBRLAxNBDTB0rgZra27tLIFlqP1TpqZtM3ThOmPJQn6JG8WeiVWnmUkpmXX\n" +
				"6opGqhLWMM/u1n4fdf716h7340RbCPJoOpTPphYo/WedFQskqZvhTU6HMIj4JQAj\n" +
				"OVVwgtrDOdx051ps2hhiSU5tL4LmjLHIsfyoCSuHkzBhVMZ/jKFm8C4Or2RRG85A\n" +
				"wtzEANSxVZRjw6S1hsHsI+8m2QIDAQABo1MwUTAdBgNVHQ4EFgQUjDli1GMiclHK\n" +
				"igNm2kuKh48AON8wHwYDVR0jBBgwFoAUjDli1GMiclHKigNm2kuKh48AON8wDwYD\n" +
				"VR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAAOCAgEAk6IWcDdBc1tngCaPNLhU\n" +
				"c3pXRdTjDuLHMxHRiP/7Vi3V2xcKRak5ZMzYAJK6YThp3Z04V9d5jJoi/CDhMuPK\n" +
				"RV1GmbdA7b24Jic2fQHWOJkgafT2Gx4yHmLo5ctSuDHPfSvzUgeghG0k3eNJgCai\n" +
				"Ctr+wvCRZGvvbl2JnJUcWiHBxH/PaWJ4Jd1T4UKmhlFhTw26TXQGHuW/UJwgh8OR\n" +
				"V8A+WeMXxKFsh38b8RnWVa6XdajIq9UAZvvd4Q16zjdnMWx/7zcIK5D1MDb/KmSJ\n" +
				"yho1LKRZx5YtSeI4FWs8dzZ0nCCiTe7TrnnhlXThJ6rXeo5AshtM4fGrvizaf4n3\n" +
				"7I9mJkqiccp1ml+2EcgsdX7HbnGE/R8VVbh3jUhWHuysLCiVSMbjnktCLWoXjSb9\n" +
				"JqOYF3yo6JQslQB0fQMyKmvsn/FplQBbU0PUrg9vpAg9nZlZf3UHO5z072pXD6ky\n" +
				"5pKjh+q0JOk00Eln9AoU6YuIyPBQ9mI3X8iYB5UhUBbgAPeg1pwWCWhdt40f0D5t\n" +
				"JkVnICy+Gh1ps8QPA6coEaajbIq14Uh6eYEwxFHPsxlbn7pzjoCJG2v7N8VwgfuL\n" +
				"DdGs4hFikdUAfBT/Diug/n9/ZgfdN6Ctf4U/SM65vZvfRqtLIoTIs4PcF3YtKK04\n" +
				"m0UA3Sxxre0vVWYO4GmmZUY=\n" +
				"-----END CERTIFICATE-----";
		partnerCertDownloadResponeDto.setCertificateData(certificate);
		Map<String, Object> apiResponse = new HashMap<>();
		apiResponse.put("response", partnerCertDownloadResponeDto);

		when(environment.getProperty("pmp.partner.certificaticate.get.rest.uri")).thenReturn("uri");
		when(restUtil.getApi(anyString(), any(), eq(Map.class))).thenReturn(apiResponse);
		when(partnerHelper.getCertificate(any(),any(),any())).thenReturn(partnerCertDownloadResponeDto);
		partnerManagementImpl.getPartnerDetails("123");

		Map<String, Object> apiResponse1 = new HashMap<>();
		apiResponse1.put("response", null);

		when(environment.getProperty("pmp.partner.certificaticate.get.rest.uri")).thenReturn("uri");
		when(restUtil.getApi(anyString(), any(), eq(Map.class))).thenReturn(apiResponse1);
		partnerManagementImpl.getPartnerDetails("123");

		when(partnerServiceRepository.findById(any())).thenReturn(Optional.empty());
		partnerManagementImpl.getPartnerDetails("123");

		partner.setCertificateAlias(null);
		when(partnerServiceRepository.findById(any())).thenReturn(Optional.of(partner));
		partnerManagementImpl.getPartnerDetails("123");

		when(policyGroupRepository.findPolicyGroupById(anyString())).thenReturn(null);
		partnerManagementImpl.getPartnerDetails("123");

		partner.setPartnerTypeCode("Auth_Partner");
		partner.setPolicyGroupId(null);
		when(partnerServiceRepository.findById(any())).thenReturn(Optional.of(partner));
		partnerManagementImpl.getPartnerDetails("123");

		partnerManagementImpl.getPartnerDetails(null);

		partnerList = new ArrayList<>();
		when(partnerServiceRepository.findByUserId(anyString())).thenReturn(partnerList);
		partnerManagementImpl.getPartnerDetails("123");
	}

	@Test
	public void getPartnerDetailsTest02() throws Exception {
		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("FTM_Provider");
		partner.setApprovalStatus("approved");
		partner.setIsActive(true);
		partner.setCertificateAlias("abs");
		partner.setPolicyGroupId("121");
		partner.setCrDtimes(Timestamp.valueOf(LocalDateTime.now()));
		partner.setName("abc");
		partner.setEmailId("abc@gmail.com");
		partner.setContactNo("1234567890");
		partnerList.add(partner);
		when(partnerServiceRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(partnerServiceRepository.findById(any())).thenReturn(Optional.of(partner));

		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setName("policygroup123");
		policyGroup.setDesc("descr");
		when(policyGroupRepository.findPolicyGroupById(anyString())).thenReturn(policyGroup);

		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		io.mosip.kernel.openid.bridge.model.AuthUserDetails authUserDetails =
				new io.mosip.kernel.openid.bridge.model.AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		partnerManagementImpl.getPartnerDetails("123");
	}

	@Test
	public void getAdminPartnersTest01() throws Exception {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "PARTNER_ADMIN");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		String sortFieldName = "createdDateTime";
		String sortType = "desc";
		Integer pageNo = 0;
		Integer pageSize = 8;
		PartnerFilterDto partnerFilterDto = new PartnerFilterDto();
		partnerFilterDto.setPartnerId("abc");
		partnerFilterDto.setPartnerTypeCode("Auth_Partner");
		partnerFilterDto.setOrganizationName("ABC");
		partnerFilterDto.setEmailAddress("abc");
		partnerFilterDto.setCertificateUploadStatus("not_uploaded");
		partnerFilterDto.setPolicyGroupName("default");
		partnerFilterDto.setIsActive(false);
		ResponseWrapperV2<PageResponseV2Dto<PartnerSummaryDto>> responseWrapper = new ResponseWrapperV2<>();
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		PartnerSummaryEntity partnerSummaryEntity = new PartnerSummaryEntity();
		partnerSummaryEntity.setPartnerId("123");
		Page<PartnerSummaryEntity> page = new PageImpl<>(List.of(partnerSummaryEntity), pageable, 1);
		when(partnerSummaryRepository.getSummaryOfAllPartners(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any(), any(), any(), any())).thenReturn(page);
		partnerManagementImpl.getAdminPartners(sortFieldName, sortType, pageNo, pageSize, partnerFilterDto);
	}

	@Test
	public void getAdminPartnersTest02() throws Exception {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "PARTNER_ADMIN");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		String sortFieldName = "certificateUploadStatus";
		String sortType = "desc";
		Integer pageNo = 0;
		Integer pageSize = 8;
		ResponseWrapperV2<PageResponseV2Dto<PartnerSummaryDto>> responseWrapper = new ResponseWrapperV2<>();
		Page<PartnerSummaryEntity> page = null;
		when(partnerSummaryRepository.getSummaryOfAllPartners(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any(), any(), any(), any())).thenReturn(page);
		partnerManagementImpl.getAdminPartners(sortFieldName, sortType, pageNo, pageSize, null);
	}

	@Test
	public void getAllApiKeyRequestsTest01() throws Exception {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "Auth_Partner");
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
		ApiKeyFilterDto apiKeyFilterDto = new ApiKeyFilterDto();
		apiKeyFilterDto.setPartnerId("123");
		apiKeyFilterDto.setPolicyName("policy");
		apiKeyFilterDto.setOrgName("ABC");
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		ApiKeyRequestsSummaryEntity apiKeyRequestsSummaryEntity = new ApiKeyRequestsSummaryEntity();
		apiKeyRequestsSummaryEntity.setApiKeyId("12345");
		Page<ApiKeyRequestsSummaryEntity> page = new PageImpl<>(List.of(apiKeyRequestsSummaryEntity), pageable, 1);

		when(apiKeyRequestSummaryRepository.getSummaryOfAllApiKeyRequests(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyList(), anyBoolean(), any(), any(), any(), any())).thenReturn(page);
		partnerManagementImpl.getAllApiKeyRequests(sortFieldName, sortType, pageNo, pageSize, apiKeyFilterDto);
	}

	@Test
	public void getAllApiKeyRequestsV2Test01() throws Exception {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "Auth_Partner");
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
		ApiKeyFilterDto apiKeyFilterDto = new ApiKeyFilterDto();
		apiKeyFilterDto.setPartnerId("123");
		apiKeyFilterDto.setPolicyName("policy");
		apiKeyFilterDto.setOrgName("ABC");
		apiKeyFilterDto.setExpiryPeriod(30);
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		ApiKeyRequestsSummaryEntity apiKeyRequestsSummaryEntity = new ApiKeyRequestsSummaryEntity();
		apiKeyRequestsSummaryEntity.setApiKeyId("12345");
		Page<ApiKeyRequestsSummaryEntity> page = new PageImpl<>(List.of(apiKeyRequestsSummaryEntity), pageable, 1);

		when(apiKeyRequestSummaryRepository.getSummaryOfAllApiKeyRequests(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyList(), anyBoolean(), any(), any(), any(), any())).thenReturn(page);
		partnerManagementImpl.getAllApiKeyRequestsV2(sortFieldName, sortType, pageNo, pageSize, apiKeyFilterDto);
	}

	@Test
	public void getAllApiKeyRequestsTest02() throws Exception {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "Auth_Partner");
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

		String sortFieldName = "status";
		String sortType = "desc";
		Integer pageNo = 0;
		Integer pageSize = 8;
		ApiKeyFilterDto apiKeyFilterDto = new ApiKeyFilterDto();
		apiKeyFilterDto.setPartnerId("123");
		apiKeyFilterDto.setPolicyName("policy");
		apiKeyFilterDto.setOrgName("ABC");
		partnerManagementImpl.getAllApiKeyRequests(sortFieldName, sortType, pageNo, pageSize, null);

		partnerList = new ArrayList<>();
		when(partnerServiceRepository.findByUserId(anyString())).thenReturn(partnerList);
		partnerManagementImpl.getAllApiKeyRequests(sortFieldName, sortType, pageNo, pageSize, apiKeyFilterDto);
	}

	@Test
	public void getAllPartnerPolicyRequestsTest01() throws Exception {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "Auth_Partner");
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
		PartnerPolicyRequestFilterDto partnerPolicyRequestFilterDto = new PartnerPolicyRequestFilterDto();
		partnerPolicyRequestFilterDto.setPartnerId("abc");
		partnerPolicyRequestFilterDto.setPolicyName("policy");
		partnerPolicyRequestFilterDto.setOrganizationName("ABC");
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		PartnerPolicyRequestSummaryEntity entity = new PartnerPolicyRequestSummaryEntity();
		entity.setId("12345");
		Page<PartnerPolicyRequestSummaryEntity> page = new PageImpl<>(List.of(entity), pageable, 1 );
		when(partnerPolicyMappingRequestRepository.getSummaryOfAllPartnerPolicyRequests(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyList(), anyBoolean(), any())).thenReturn(page);
		partnerManagementImpl.getAllPartnerPolicyRequests(sortFieldName, sortType, pageNo, pageSize, partnerPolicyRequestFilterDto);
	}

	@Test
	public void getAllPartnerPolicyRequestsTest02() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		io.mosip.kernel.openid.bridge.model.AuthUserDetails authUserDetails =
				new io.mosip.kernel.openid.bridge.model.AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("Auth_Partner")
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
		PartnerPolicyRequestFilterDto partnerPolicyRequestFilterDto = new PartnerPolicyRequestFilterDto();
		partnerPolicyRequestFilterDto.setPartnerId("abc");
		partnerPolicyRequestFilterDto.setPolicyName("policy");
		partnerPolicyRequestFilterDto.setOrganizationName("ABC");
		partnerManagementImpl.getAllPartnerPolicyRequests(sortFieldName, sortType, pageNo, pageSize, null);

		partnerList = new ArrayList<>();
		when(partnerServiceRepository.findByUserId(anyString())).thenReturn(partnerList);
		partnerManagementImpl.getAllPartnerPolicyRequests(sortFieldName, sortType, pageNo, pageSize, partnerPolicyRequestFilterDto);
	}

	@Test
	public void getTrustCertificatesTest01() throws Exception {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "Auth_Partner");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		String sortFieldName = "partnerDomain";
		String sortType = "desc";
		Integer pageNo = 0;
		Integer pageSize = 8;
		TrustCertificateFilterDto filterDto = new TrustCertificateFilterDto();
		filterDto.setCertificateId("abc");
		filterDto.setCaCertificateType("root");
		filterDto.setPartnerDomain("Auth");
		filterDto.setExpiryPeriod(30);

		TrustCertificateSummaryDto trustCertificateSummaryDto = new TrustCertificateSummaryDto();
		trustCertificateSummaryDto.setCaCertificateType("ROOT");
		trustCertificateSummaryDto.setCertId("abc");
		trustCertificateSummaryDto.setPartnerDomain("AUTH");
		List<TrustCertificateSummaryDto> trustCertificateSummaryDtoList = new ArrayList<>();
		trustCertificateSummaryDtoList.add(trustCertificateSummaryDto);

		TrustCertTypeListResponseDto trustCertTypeListResponseDto = new TrustCertTypeListResponseDto();
		trustCertTypeListResponseDto.setPageNumber(1);
		trustCertTypeListResponseDto.setPageSize(8);
		trustCertTypeListResponseDto.setTotalRecords(10);
		trustCertTypeListResponseDto.setAllPartnerCertificates(trustCertificateSummaryDtoList);

		Map<String, Object> apiResponse = new HashMap<>();
		apiResponse.put("response", trustCertTypeListResponseDto);

		when(environment.getProperty("pmp.trust.certificates.post.rest.uri")).thenReturn("uri");
		when(restUtil.postApi(anyString(), any(), eq(""), eq(""),
				eq(MediaType.APPLICATION_JSON), any(), eq(Map.class))).thenReturn(apiResponse);

		when(mapper.writeValueAsString(any())).thenReturn(new ObjectMapper().writeValueAsString(trustCertTypeListResponseDto));
		when(mapper.readValue(anyString(), eq(TrustCertTypeListResponseDto.class))).thenReturn(trustCertTypeListResponseDto);

		ResponseWrapperV2<PageResponseV2Dto<TrustCertificateSummaryDto>> responseWrapper =
				partnerManagementImpl.getTrustCertificates(sortFieldName, sortType, pageNo, pageSize, filterDto);
	}

	@Test
	public void getTrustCertificatesTest02() throws Exception {
		MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		String sortFieldName = "partnerDomain";
		String sortType = "desc";
		int pageNo = 0;
		Integer pageSize = 8;
		partnerManagementImpl.getTrustCertificates(sortFieldName, sortType, pageNo, pageSize, null);
	}

	@Test
	public void getTrustCertificatesTest03() throws Exception {
		MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		String sortFieldName = "partnerDomain";
		String sortType = "desc";
		Integer pageNo = 0;
		Integer pageSize = 8;
		TrustCertificateFilterDto filterDto = new TrustCertificateFilterDto();
		filterDto.setCertificateId("abc");
		filterDto.setCaCertificateType("root");
		filterDto.setPartnerDomain("Auth");

		List<Map<String, Object>> errorList = new ArrayList<>();
		Map<String, Object> error = new HashMap<>();
		error.put("errorCode", "001");
		error.put("message", "error occured");
		errorList.add(error);
		Map<String, Object> apiResponse = new HashMap<>();
		apiResponse.put("response", null);
		apiResponse.put("errors", errorList);

		when(environment.getProperty("pmp.trust.certificates.post.rest.uri")).thenReturn("uri");
		when(restUtil.postApi(anyString(), any(), eq(""), eq(""),
				eq(MediaType.APPLICATION_JSON), any(), eq(Map.class))).thenReturn(apiResponse);
		partnerManagementImpl.getTrustCertificates(sortFieldName, sortType, pageNo, pageSize, filterDto);
	}

	@Test
	public void getTrustCertificatesTest04() throws Exception {
		MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		String sortFieldName = "partnerDomain";
		String sortType = "desc";
		Integer pageNo = 0;
		Integer pageSize = 8;
		TrustCertificateFilterDto filterDto = new TrustCertificateFilterDto();
		filterDto.setCertificateId("abc");
		filterDto.setCaCertificateType("root");
		filterDto.setPartnerDomain("Auth");

		List<Map<String, Object>> errorList = new ArrayList<>();
		Map<String, Object> apiResponse = new HashMap<>();
		apiResponse.put("response", null);
		apiResponse.put("errors", errorList);

		when(environment.getProperty("pmp.trust.certificates.post.rest.uri")).thenReturn("uri");
		when(restUtil.postApi(anyString(), any(), eq(""), eq(""),
				eq(MediaType.APPLICATION_JSON), any(), eq(Map.class))).thenReturn(apiResponse);
		partnerManagementImpl.getTrustCertificates(sortFieldName, sortType, pageNo, pageSize, filterDto);
	}

	@Test
	public void getTrustCertificatesTest05() throws Exception {
		MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		String sortFieldName = "partnerDomain";
		String sortType = "desc";
		Integer pageNo = 0;
		Integer pageSize = 8;
		TrustCertificateFilterDto filterDto = new TrustCertificateFilterDto();
		filterDto.setCertificateId("abc");
		filterDto.setCaCertificateType("root");
		filterDto.setPartnerDomain("Auth");

		Map<String, Object> apiResponse = new HashMap<>();
		apiResponse.put("response", null);

		when(environment.getProperty("pmp.trust.certificates.post.rest.uri")).thenReturn("uri");
		when(restUtil.postApi(anyString(), any(), eq(""), eq(""),
				eq(MediaType.APPLICATION_JSON), any(), eq(Map.class))).thenReturn(apiResponse);
		partnerManagementImpl.getTrustCertificates(sortFieldName, sortType, pageNo, pageSize, filterDto);
	}

	@Test
	public void getTrustCertificatesTest06() throws Exception {
		MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		String sortFieldName = "partnerDomain";
		Integer pageNo = 0;
		Integer pageSize = 8;
		TrustCertificateFilterDto filterDto = new TrustCertificateFilterDto();
		filterDto.setCertificateId("abc");
		filterDto.setCaCertificateType("root");
		filterDto.setPartnerDomain("Auth");
		partnerManagementImpl.getTrustCertificates(sortFieldName, null, pageNo, pageSize, filterDto);
	}

	@Test
	public void downloadTrustCertificatesTest01() throws Exception {
		MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		Map<String, Object> apiResponse = new HashMap<>();
		Map<String, Object> response = new HashMap<>();
		response.put("p7bFile", "-----BEGIN CERTIFICATE-----\n" +
				"MIIFfTCCA2WgAwIBAgIUOVZNyD46U0OAEhaGC/Y7NXbu+OkwDQYJKoZIhvcNAQEL\n" +
				"BQAwTjELMAkGA1UEBhMCSU4xCzAJBgNVBAgMAk1IMQswCQYDVQQHDAJQTjELMAkG\n" +
				"A1UECgwCQ0ExCzAJBgNVBAsMAkNBMQswCQYDVQQDDAJDQTAeFw0yNDA1MDkwNzI1\n" +
				"MDJaFw0yOTA1MDkwNzI1MDJaME4xCzAJBgNVBAYTAklOMQswCQYDVQQIDAJNSDEL\n" +
				"MAkGA1UEBwwCUE4xCzAJBgNVBAoMAkNBMQswCQYDVQQLDAJDQTELMAkGA1UEAwwC\n" +
				"Q0EwggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAwggIKAoICAQCzdWD2DvhSnmLqU3fX\n" +
				"RT3z8ikS6qHxn5Hu/a2ijkuZxAZj0UCUJ83kM20NwocJDHT1qx6+yjdl+BECsgoI\n" +
				"ro9MXgFOsHCphyR5KiP4mY95qRlE03h7WBfr4wDn/6f5tCbqCcBqdXMAQxUp34D+\n" +
				"Pro0EwkXNulHNMTvz5hpoCEiGyfXUP48I4q2nb8rMXaplhqz+vAYgA4rsK6K9IUh\n" +
				"uJDxtZRHdIfxnvbfjxDbuPkN0ehOQ1uQrDVY6ENCIUxdgR/p94kZ+CNsD21c57gJ\n" +
				"2wYg+BceQn1rVSGnfpqMoogZCMUWFvaE4i91419VXxDLgeC/4Qw8n5onBY+dVHjW\n" +
				"04OolR2DqotFyaPlZiVdpUys6+KZ7fS9mwWEY0kqtLzcBeb4g4nPvObfKnqSmVMZ\n" +
				"DHRuAx6MG3oFZrnNuS6oIYGwLpoko6iqEiGohHsSxMulT43XOxoNgDq9noQc9SYv\n" +
				"tzdzijBRLAxNBDTB0rgZra27tLIFlqP1TpqZtM3ThOmPJQn6JG8WeiVWnmUkpmXX\n" +
				"6opGqhLWMM/u1n4fdf716h7340RbCPJoOpTPphYo/WedFQskqZvhTU6HMIj4JQAj\n" +
				"OVVwgtrDOdx051ps2hhiSU5tL4LmjLHIsfyoCSuHkzBhVMZ/jKFm8C4Or2RRG85A\n" +
				"wtzEANSxVZRjw6S1hsHsI+8m2QIDAQABo1MwUTAdBgNVHQ4EFgQUjDli1GMiclHK\n" +
				"igNm2kuKh48AON8wHwYDVR0jBBgwFoAUjDli1GMiclHKigNm2kuKh48AON8wDwYD\n" +
				"VR0TAQH/BAUwAwEB/zANBgkqhkiG9w0BAQsFAAOCAgEAk6IWcDdBc1tngCaPNLhU\n" +
				"c3pXRdTjDuLHMxHRiP/7Vi3V2xcKRak5ZMzYAJK6YThp3Z04V9d5jJoi/CDhMuPK\n" +
				"RV1GmbdA7b24Jic2fQHWOJkgafT2Gx4yHmLo5ctSuDHPfSvzUgeghG0k3eNJgCai\n" +
				"Ctr+wvCRZGvvbl2JnJUcWiHBxH/PaWJ4Jd1T4UKmhlFhTw26TXQGHuW/UJwgh8OR\n" +
				"V8A+WeMXxKFsh38b8RnWVa6XdajIq9UAZvvd4Q16zjdnMWx/7zcIK5D1MDb/KmSJ\n" +
				"yho1LKRZx5YtSeI4FWs8dzZ0nCCiTe7TrnnhlXThJ6rXeo5AshtM4fGrvizaf4n3\n" +
				"7I9mJkqiccp1ml+2EcgsdX7HbnGE/R8VVbh3jUhWHuysLCiVSMbjnktCLWoXjSb9\n" +
				"JqOYF3yo6JQslQB0fQMyKmvsn/FplQBbU0PUrg9vpAg9nZlZf3UHO5z072pXD6ky\n" +
				"5pKjh+q0JOk00Eln9AoU6YuIyPBQ9mI3X8iYB5UhUBbgAPeg1pwWCWhdt40f0D5t\n" +
				"JkVnICy+Gh1ps8QPA6coEaajbIq14Uh6eYEwxFHPsxlbn7pzjoCJG2v7N8VwgfuL\n" +
				"DdGs4hFikdUAfBT/Diug/n9/ZgfdN6Ctf4U/SM65vZvfRqtLIoTIs4PcF3YtKK04\n" +
				"m0UA3Sxxre0vVWYO4GmmZUY=\n" +
				"-----END CERTIFICATE-----");
		apiResponse.put("response", response);

		when(environment.getProperty("pmp.download.trust.certificates.get.rest.uri")).thenReturn("uri");
		when(restUtil.getApi(anyString(), any(), eq(Map.class))).thenReturn(apiResponse);
		partnerManagementImpl.downloadTrustCertificates("123");
	}

	@Test
	public void downloadTrustCertificatesTest02() throws Exception {
		MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		partnerManagementImpl.downloadTrustCertificates("");
	}

	@Test
	public void downloadTrustCertificatesTest03() throws Exception {
		MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		List<Map<String, Object>> errorList = new ArrayList<>();
		Map<String, Object> error = new HashMap<>();
		error.put("errorCode", "001");
		error.put("message", "error occured");
		errorList.add(error);
		Map<String, Object> apiResponse = new HashMap<>();
		apiResponse.put("response", null);
		apiResponse.put("errors", errorList);

		when(environment.getProperty("pmp.download.trust.certificates.get.rest.uri")).thenReturn("uri");
		when(restUtil.getApi(anyString(), any(), eq(Map.class))).thenReturn(apiResponse);
		partnerManagementImpl.downloadTrustCertificates("123");
	}

	@Test
	public void downloadTrustCertificatesTest04() throws Exception {
		MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		List<Map<String, Object>> errorList = new ArrayList<>();
		Map<String, Object> apiResponse = new HashMap<>();
		apiResponse.put("response", null);
		apiResponse.put("errors", errorList);

		when(environment.getProperty("pmp.download.trust.certificates.get.rest.uri")).thenReturn("uri");
		when(restUtil.getApi(anyString(), any(), eq(Map.class))).thenReturn(apiResponse);
		partnerManagementImpl.downloadTrustCertificates("123");
	}

	@Test
	public void downloadTrustCertificatesTest05() throws Exception {
		MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		List<Map<String, Object>> errorList = new ArrayList<>();
		Map<String, Object> error = new HashMap<>();
		error.put("errorCodes", "001");
		error.put("messages", "error occured");
		errorList.add(error);
		Map<String, Object> apiResponse = new HashMap<>();
		apiResponse.put("response", null);
		apiResponse.put("errors", errorList);

		when(environment.getProperty("pmp.download.trust.certificates.get.rest.uri")).thenReturn("uri");
		when(restUtil.getApi(anyString(), any(), eq(Map.class))).thenReturn(apiResponse);
		partnerManagementImpl.downloadTrustCertificates("123");
	}

	@Test
	public void downloadTrustCertificatesTest06() throws Exception {
		MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		Map<String, Object> apiResponse = new HashMap<>();
		apiResponse.put("response", null);

		when(environment.getProperty("pmp.download.trust.certificates.get.rest.uri")).thenReturn("uri");
		when(restUtil.getApi(anyString(), any(), eq(Map.class))).thenReturn(apiResponse);
		partnerManagementImpl.downloadTrustCertificates("123");
	}

	private LinkPolicyGroupRequestDto getLinkPolicyGroupRequestDto() {
		LinkPolicyGroupRequestDto requestDto = new LinkPolicyGroupRequestDto();
		requestDto.setPolicyGroupId("group1");
		return requestDto;
	}

	private Optional<Partner> getPartnerForLinkPolicyGroup() {
		Partner partner = new Partner();
		partner.setId("partner1");
		partner.setIsActive(true);
		partner.setApprovalStatus("approved");
		partner.setPartnerTypeCode("MISP_Partner");
		return Optional.of(partner);
	}

	private PolicyGroup getPolicyGroup() {
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("group1");
		policyGroup.setName("Group 1");
		policyGroup.setDesc("description");
		policyGroup.setIsActive(true);
		return policyGroup;
	}

	@Test
	public void linkPolicyGroupTest_WithValidRequests() {
		MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		when(partnerServiceRepository.findById(anyString())).thenReturn(getPartnerForLinkPolicyGroup());
		when(policyGroupRepository.findPolicyGroupById(anyString())).thenReturn(getPolicyGroup());

		Partner partner = getPartnerForLinkPolicyGroup().get();
		partner.setPolicyGroupId(getPolicyGroup().getId());
		when(partnerRepository.save(any())).thenReturn(partner);
		partnerManagementImpl.linkPolicyGroup("partner1", getLinkPolicyGroupRequestDto());
	}

	@Test
	public void linkPolicyGroupTest_WithNullPointerException() {
		when(partnerServiceRepository.findById(anyString())).thenReturn(getPartnerForLinkPolicyGroup());
		when(policyGroupRepository.findPolicyGroupById(anyString())).thenReturn(getPolicyGroup());
		partnerManagementImpl.linkPolicyGroup("partner1", getLinkPolicyGroupRequestDto());
	}

	@Test
	public void linkPolicyGroupTest_WithPolicygroupNotActive() {
		when(partnerServiceRepository.findById(anyString())).thenReturn(getPartnerForLinkPolicyGroup());

		PolicyGroup policyGroup = getPolicyGroup();
		policyGroup.setIsActive(false);
		when(policyGroupRepository.findPolicyGroupById(anyString())).thenReturn(policyGroup);
		partnerManagementImpl.linkPolicyGroup("partner1", getLinkPolicyGroupRequestDto());
	}

	@Test
	public void linkPolicyGroupTest_WithPolicygroupNotExist() {
		when(partnerServiceRepository.findById(anyString())).thenReturn(getPartnerForLinkPolicyGroup());
		when(policyGroupRepository.findPolicyGroupById(anyString())).thenReturn(null);
		partnerManagementImpl.linkPolicyGroup("partner1", getLinkPolicyGroupRequestDto());
	}

	@Test
	public void linkPolicyGroupTest_WithPolicygroupAlreadyLinked() {
		Partner partner = getPartnerForLinkPolicyGroup().get();
		partner.setPolicyGroupId("group2");
		when(partnerServiceRepository.findById(anyString())).thenReturn(Optional.of(partner));
		partnerManagementImpl.linkPolicyGroup("partner1", getLinkPolicyGroupRequestDto());
	}

	@Test
	public void linkPolicyGroupTest_WithPartnerNotActive() {
		Partner partner = getPartnerForLinkPolicyGroup().get();
		partner.setIsActive(false);
		when(partnerServiceRepository.findById(anyString())).thenReturn(Optional.of(partner));
		partnerManagementImpl.linkPolicyGroup("partner1", getLinkPolicyGroupRequestDto());
	}

	@Test
	public void linkPolicyGroupTest_WithNotMISPPartner() {
		Partner partner = getPartnerForLinkPolicyGroup().get();
		partner.setPartnerTypeCode("Auth");
		when(partnerServiceRepository.findById(anyString())).thenReturn(Optional.of(partner));
		partnerManagementImpl.linkPolicyGroup("partner1", getLinkPolicyGroupRequestDto());
	}

	@Test
	public void linkPolicyGroupTest_WithPartnerNotExist() {
		when(partnerServiceRepository.findById(anyString())).thenReturn(Optional.empty());
		partnerManagementImpl.linkPolicyGroup("partner1", getLinkPolicyGroupRequestDto());
	}

	@Test
	public void linkPolicyGroupTest_WithInvalidRequests() {
		// null policy group id
		LinkPolicyGroupRequestDto requestDto = new LinkPolicyGroupRequestDto();
		requestDto.setPolicyGroupId(null);
		partnerManagementImpl.linkPolicyGroup("partner1", requestDto);

		// null partner id
		partnerManagementImpl.linkPolicyGroup(null, requestDto);
	}

	private io.mosip.kernel.openid.bridge.model.MosipUserDto getMosipUserDto() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = new io.mosip.kernel.openid.bridge.model.MosipUserDto();
		mosipUserDto.setUserId("123");
		mosipUserDto.setMail("abc@gmail.com");
		return mosipUserDto;
	}

	@Test
	public void updateAPIKeyTest_SuccessWithExpiryDate() throws Exception {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "PARTNER_ADMIN");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		String partnerId = "partner123";
		String policyId = "policy456";
		String apiKeyName = "apiKeyName123";
		io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto requestDto = new io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto();
		requestDto.setExpiryDateTime(java.time.OffsetDateTime.now().plusDays(30));

		PartnerPolicy partnerPolicy = getPartnerPolicy();
		partnerPolicy.setIsActive(true);
		partnerPolicy.setLabel(apiKeyName);
		Partner partner = getPartner();
		partner.setId(partnerId);
		partner.setIsActive(true);
		partnerPolicy.setPartner(partner);

		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.setId(policyId);
		authPolicy.setIsActive(true);
		Mockito.when(partnerServiceRepository.findById(partnerId)).thenReturn(Optional.of(partner));
		Mockito.when(authPolicyRepository.findById(policyId)).thenReturn(Optional.of(authPolicy));
		Mockito.when(partnerPolicyRepository.findByPartnerIdPolicyIdAndLabel(partnerId, policyId, apiKeyName))
				.thenReturn(partnerPolicy);
		Mockito.when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(true);
		Mockito.when(partnerPolicyRepository.save(any(PartnerPolicy.class))).thenReturn(partnerPolicy);
		Mockito.doNothing().when(webSubPublisher).notify(any(), any(), any());
		Mockito.doNothing().when(notificationService).sendNotications(any(), any());

		ResponseWrapperV2<io.mosip.pms.partner.response.dto.APIKeyUpdateResponseDto> response =
				partnerManagementImpl.updateAPIKey(partnerId, policyId, apiKeyName, requestDto);
		assertNotNull(response);
		assertTrue(response.getErrors() == null || response.getErrors().isEmpty());
	}

	@Test
	public void updateAPIKeyTest_SuccessWithDeactivateStatus() throws Exception {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "PARTNER_ADMIN");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		String partnerId = "partner123";
		String policyId = "policy456";
		String apiKeyName = "apiKeyName123";
		io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto requestDto = new io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto();
		requestDto.setStatus("De-active");

		PartnerPolicy partnerPolicy = getPartnerPolicy();
		partnerPolicy.setIsActive(true);
		partnerPolicy.setLabel(apiKeyName);
		Partner partner = getPartner();
		partner.setId(partnerId);
		partner.setIsActive(true);
		partnerPolicy.setPartner(partner);

		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.setId(policyId);
		authPolicy.setIsActive(true);
		Mockito.when(partnerServiceRepository.findById(partnerId)).thenReturn(Optional.of(partner));
		Mockito.when(authPolicyRepository.findById(policyId)).thenReturn(Optional.of(authPolicy));
		Mockito.when(partnerPolicyRepository.findByPartnerIdPolicyIdAndLabel(partnerId, policyId, apiKeyName))
				.thenReturn(partnerPolicy);
		Mockito.when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(true);
		Mockito.when(partnerPolicyRepository.save(any(PartnerPolicy.class))).thenReturn(partnerPolicy);
		Mockito.doNothing().when(webSubPublisher).notify(any(), any(), any());
		Mockito.doNothing().when(notificationService).sendNotications(any(), any());

		ResponseWrapperV2<io.mosip.pms.partner.response.dto.APIKeyUpdateResponseDto> response =
				partnerManagementImpl.updateAPIKey(partnerId, policyId, apiKeyName, requestDto);
		assertNotNull(response);
		assertTrue(response.getErrors() == null || response.getErrors().isEmpty());
	}

	@Test
	public void updateAPIKeyTest_SuccessWithBothFields() throws Exception {
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

		String partnerId = "partner123";
		String policyId = "policy456";
		String apiKeyName = "apiKeyName123";
		io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto requestDto = new io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto();
		requestDto.setStatus("De-active");
		requestDto.setExpiryDateTime(java.time.OffsetDateTime.now().plusDays(30));

		PartnerPolicy partnerPolicy = getPartnerPolicy();
		partnerPolicy.setIsActive(true);
		partnerPolicy.setLabel(apiKeyName);
		Partner partner = getPartner();
		partner.setId(partnerId);
		partner.setIsActive(true);
		partnerPolicy.setPartner(partner);

		AuthPolicy authPolicy = new AuthPolicy();
		authPolicy.setId(policyId);
		authPolicy.setIsActive(true);
		Mockito.when(partnerServiceRepository.findById(partnerId)).thenReturn(Optional.of(partner));
		Mockito.when(authPolicyRepository.findById(policyId)).thenReturn(Optional.of(authPolicy));
		Mockito.when(partnerPolicyRepository.findByPartnerIdPolicyIdAndLabel(partnerId, policyId, apiKeyName))
				.thenReturn(partnerPolicy);
		Mockito.when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(true);
		Mockito.when(partnerPolicyRepository.save(any(PartnerPolicy.class))).thenReturn(partnerPolicy);
		Mockito.doNothing().when(webSubPublisher).notify(any(), any(), any());
		Mockito.doNothing().when(notificationService).sendNotications(any(), any());

		ResponseWrapperV2<io.mosip.pms.partner.response.dto.APIKeyUpdateResponseDto> response =
				partnerManagementImpl.updateAPIKey(partnerId, policyId, apiKeyName, requestDto);
		assertNotNull(response);
	}

	@Test
	public void updateAPIKeyTest_NullPartnerId() throws Exception {
		io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto requestDto = new io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto();
		requestDto.setStatus("De-active");

		ResponseWrapperV2<io.mosip.pms.partner.response.dto.APIKeyUpdateResponseDto> response =
				partnerManagementImpl.updateAPIKey(null, "policy456", "apiKeyName123", requestDto);
		assertNotNull(response);
		assertNotNull(response.getErrors());
	}

	@Test
	public void updateAPIKeyTest_EmptyPartnerId() throws Exception {
		io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto requestDto = new io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto();
		requestDto.setStatus("De-active");

		ResponseWrapperV2<io.mosip.pms.partner.response.dto.APIKeyUpdateResponseDto> response =
				partnerManagementImpl.updateAPIKey("", "policy456", "apiKeyName123", requestDto);
		assertNotNull(response);
		assertNotNull(response.getErrors());
	}

	@Test
	public void updateAPIKeyTest_NullPolicyId() throws Exception {
		io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto requestDto = new io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto();
		requestDto.setStatus("De-active");

		ResponseWrapperV2<io.mosip.pms.partner.response.dto.APIKeyUpdateResponseDto> response =
				partnerManagementImpl.updateAPIKey("partner123", null, "apiKeyName123", requestDto);
		assertNotNull(response);
		assertNotNull(response.getErrors());
	}

	@Test
	public void updateAPIKeyTest_NullApiKeyName() throws Exception {
		io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto requestDto = new io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto();
		requestDto.setStatus("De-active");

		ResponseWrapperV2<io.mosip.pms.partner.response.dto.APIKeyUpdateResponseDto> response =
				partnerManagementImpl.updateAPIKey("partner123", "policy456", null, requestDto);
		assertNotNull(response);
		assertNotNull(response.getErrors());
	}

	@Test
	public void updateAPIKeyTest_NoFieldsProvided() throws Exception {
		io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto requestDto = new io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto();

		ResponseWrapperV2<io.mosip.pms.partner.response.dto.APIKeyUpdateResponseDto> response =
				partnerManagementImpl.updateAPIKey("partner123", "policy456", "apiKeyName123", requestDto);
		assertNotNull(response);
		assertNotNull(response.getErrors());
	}

	@Test
	public void updateAPIKeyTest_PartnerPolicyNotFound() throws Exception {
		io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto requestDto = new io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto();
		requestDto.setStatus("De-active");

		Mockito.when(partnerPolicyRepository.findByPartnerIdPolicyIdAndLabel(anyString(), anyString(), anyString()))
				.thenReturn(null);

		ResponseWrapperV2<io.mosip.pms.partner.response.dto.APIKeyUpdateResponseDto> response =
				partnerManagementImpl.updateAPIKey("partner123", "policy456", "apiKeyName123", requestDto);
		assertNotNull(response);
		assertNotNull(response.getErrors());
	}

	@Test
	public void updateAPIKeyTest_PartnerNotActive() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("Auth_Partner")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		String partnerId = "partner123";
		String policyId = "policy456";
		String apiKeyName = "apiKeyName123";
		io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto requestDto = new io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto();
		requestDto.setStatus("De-active");

		PartnerPolicy partnerPolicy = getPartnerPolicy();
		partnerPolicy.setIsActive(true);
		partnerPolicy.setLabel(apiKeyName);
		Partner partner = getPartner();
		partner.setId(partnerId);
		partner.setIsActive(false);
		partnerPolicy.setPartner(partner);

		Mockito.when(partnerPolicyRepository.findByPartnerIdPolicyIdAndLabel(partnerId, policyId, apiKeyName))
				.thenReturn(partnerPolicy);
		Mockito.when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(false);

		ResponseWrapperV2<io.mosip.pms.partner.response.dto.APIKeyUpdateResponseDto> response =
				partnerManagementImpl.updateAPIKey(partnerId, policyId, apiKeyName, requestDto);
		assertNotNull(response);
		assertNotNull(response.getErrors());
	}

	@Test
	public void updateAPIKeyTest_ApiKeyNotActive() throws Exception {
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

		String partnerId = "partner123";
		String policyId = "policy456";
		String apiKeyName = "apiKeyName123";
		io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto requestDto = new io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto();
		requestDto.setStatus("De-active");

		PartnerPolicy partnerPolicy = getPartnerPolicy();
		partnerPolicy.setIsActive(false);
		partnerPolicy.setLabel(apiKeyName);
		Partner partner = getPartner();
		partner.setId(partnerId);
		partner.setIsActive(true);
		partnerPolicy.setPartner(partner);

		Mockito.when(partnerPolicyRepository.findByPartnerIdPolicyIdAndLabel(partnerId, policyId, apiKeyName))
				.thenReturn(partnerPolicy);
		Mockito.when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(true);

		ResponseWrapperV2<io.mosip.pms.partner.response.dto.APIKeyUpdateResponseDto> response =
				partnerManagementImpl.updateAPIKey(partnerId, policyId, apiKeyName, requestDto);
		assertNotNull(response);
		assertNotNull(response.getErrors());
	}

	@Test
	public void updateAPIKeyTest_NonAdminCannotUpdateExpiry() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(
				new SimpleGrantedAuthority("Auth_Partner")
		);
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		String partnerId = "partner123";
		String policyId = "policy456";
		String apiKeyName = "apiKeyName123";
		io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto requestDto = new io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto();
		requestDto.setExpiryDateTime(java.time.OffsetDateTime.now().plusDays(30));

		PartnerPolicy partnerPolicy = getPartnerPolicy();
		partnerPolicy.setIsActive(true);
		partnerPolicy.setLabel(apiKeyName);
		Partner partner = getPartner();
		partner.setId(partnerId);
		partner.setIsActive(true);
		partnerPolicy.setPartner(partner);

		Mockito.when(partnerPolicyRepository.findByPartnerIdPolicyIdAndLabel(partnerId, policyId, apiKeyName))
				.thenReturn(partnerPolicy);
		Mockito.when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(false);

		ResponseWrapperV2<io.mosip.pms.partner.response.dto.APIKeyUpdateResponseDto> response =
				partnerManagementImpl.updateAPIKey(partnerId, policyId, apiKeyName, requestDto);
		assertNotNull(response);
		assertNotNull(response.getErrors());
	}

	@Test
	public void updateAPIKeyTest_ExpiryDateInPast() throws Exception {
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

		String partnerId = "partner123";
		String policyId = "policy456";
		String apiKeyName = "apiKeyName123";
		io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto requestDto = new io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto();
		requestDto.setExpiryDateTime(java.time.OffsetDateTime.now().minusDays(1));

		PartnerPolicy partnerPolicy = getPartnerPolicy();
		partnerPolicy.setIsActive(true);
		partnerPolicy.setLabel(apiKeyName);
		Partner partner = getPartner();
		partner.setId(partnerId);
		partner.setIsActive(true);
		partnerPolicy.setPartner(partner);

		Mockito.when(partnerPolicyRepository.findByPartnerIdPolicyIdAndLabel(partnerId, policyId, apiKeyName))
				.thenReturn(partnerPolicy);
		Mockito.when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(true);

		ResponseWrapperV2<io.mosip.pms.partner.response.dto.APIKeyUpdateResponseDto> response =
				partnerManagementImpl.updateAPIKey(partnerId, policyId, apiKeyName, requestDto);
		assertNotNull(response);
		assertNotNull(response.getErrors());
	}

	@Test
	public void updateAPIKeyTest_InvalidStatus() throws Exception {
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

		String partnerId = "partner123";
		String policyId = "policy456";
		String apiKeyName = "apiKeyName123";
		io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto requestDto = new io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto();
		requestDto.setStatus("Active");

		PartnerPolicy partnerPolicy = getPartnerPolicy();
		partnerPolicy.setIsActive(true);
		partnerPolicy.setLabel(apiKeyName);
		Partner partner = getPartner();
		partner.setId(partnerId);
		partner.setIsActive(true);
		partnerPolicy.setPartner(partner);

		Mockito.when(partnerPolicyRepository.findByPartnerIdPolicyIdAndLabel(partnerId, policyId, apiKeyName))
				.thenReturn(partnerPolicy);
		Mockito.when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(true);

		ResponseWrapperV2<io.mosip.pms.partner.response.dto.APIKeyUpdateResponseDto> response =
				partnerManagementImpl.updateAPIKey(partnerId, policyId, apiKeyName, requestDto);
		assertNotNull(response);
		assertNotNull(response.getErrors());
	}

	@Test
	public void updateAPIKeyTest_ExceptionHandling() throws Exception {
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

		String partnerId = "partner123";
		String policyId = "policy456";
		String apiKeyName = "apiKeyName123";
		io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto requestDto = new io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto();
		requestDto.setStatus("De-active");

		PartnerPolicy partnerPolicy = getPartnerPolicy();
		partnerPolicy.setIsActive(true);
		partnerPolicy.setLabel(apiKeyName);
		Partner partner = getPartner();
		partner.setId(partnerId);
		partner.setIsActive(true);
		partnerPolicy.setPartner(partner);

		Mockito.when(partnerPolicyRepository.findByPartnerIdPolicyIdAndLabel(partnerId, policyId, apiKeyName))
				.thenReturn(partnerPolicy);
		Mockito.when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(true);
		Mockito.when(partnerPolicyRepository.save(any(PartnerPolicy.class))).thenThrow(new RuntimeException("Database error"));

		ResponseWrapperV2<io.mosip.pms.partner.response.dto.APIKeyUpdateResponseDto> response =
				partnerManagementImpl.updateAPIKey(partnerId, policyId, apiKeyName, requestDto);
		assertNotNull(response);
		assertNotNull(response.getErrors());
	}

	@Test
	public void createBioextractorConfigurationSuccess() throws Exception {
		setupSecurityContextForBioextractor();
		BioextractorConfigurationRequestDto req = buildBioextractorRequest();
		when(bioextractorConfigurationRepository.existsByConfigNameIgnoreCaseAndIsDeletedFalse(anyString())).thenReturn(false);
		when(bioextractorConfigurationRepository.existsById(anyString())).thenReturn(false);
		when(bioextractorConfigurationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		ResponseWrapperV2<BioextractorConfigurationResponseDto> resp =
				partnerManagementImpl.createBioextractorConfiguration(req);

		assertNotNull(resp);
		assertNotNull(resp.getResponse());
		assertEquals("SUCCESS", resp.getResponse().getStatus());
		assertNotNull(resp.getResponse().getId());
	}

	@Test
	public void createBioextractorConfigurationNullRequest() {
		ResponseWrapperV2<BioextractorConfigurationResponseDto> resp =
				partnerManagementImpl.createBioextractorConfiguration(null);

		assertNotNull(resp);
		assertFalse(resp.getErrors().isEmpty());
		assertEquals(io.mosip.pms.partner.constant.ErrorCode.INVALID_REQUEST_PARAM.getErrorCode(),
				resp.getErrors().get(0).getErrorCode());
	}

	@Test
	public void createBioextractorConfigurationBlankConfigName() {
		BioextractorConfigurationRequestDto req = new BioextractorConfigurationRequestDto();
		req.setConfigName("   ");
		req.setBioextractorProviderName("ProviderA");
		req.setBioModality("face");

		ResponseWrapperV2<BioextractorConfigurationResponseDto> resp =
				partnerManagementImpl.createBioextractorConfiguration(req);

		assertNotNull(resp);
		assertFalse(resp.getErrors().isEmpty());
		assertEquals(io.mosip.pms.partner.constant.ErrorCode.MISSING_PARTNER_INPUT_PARAMETER.getErrorCode(),
				resp.getErrors().get(0).getErrorCode());
	}

	@Test
	public void createBioextractorConfigurationNullProviderName() {
		BioextractorConfigurationRequestDto req = new BioextractorConfigurationRequestDto();
		req.setConfigName("cfg1");
		req.setBioextractorProviderName(null);
		req.setBioModality("face");

		ResponseWrapperV2<BioextractorConfigurationResponseDto> resp =
				partnerManagementImpl.createBioextractorConfiguration(req);

		assertNotNull(resp);
		assertFalse(resp.getErrors().isEmpty());
		assertEquals(io.mosip.pms.partner.constant.ErrorCode.MISSING_PARTNER_INPUT_PARAMETER.getErrorCode(),
				resp.getErrors().get(0).getErrorCode());
	}

	@Test
	public void createBioextractorConfigurationNullModality() {
		BioextractorConfigurationRequestDto req = new BioextractorConfigurationRequestDto();
		req.setConfigName("cfg1");
		req.setBioextractorProviderName("ProviderA");
		req.setBioModality(null);

		ResponseWrapperV2<BioextractorConfigurationResponseDto> resp =
				partnerManagementImpl.createBioextractorConfiguration(req);

		assertNotNull(resp);
		assertFalse(resp.getErrors().isEmpty());
		assertEquals(io.mosip.pms.partner.constant.ErrorCode.MISSING_PARTNER_INPUT_PARAMETER.getErrorCode(),
				resp.getErrors().get(0).getErrorCode());
	}

	@Test
	public void createBioextractorConfiguration_DuplicateConfigName() {
		BioextractorConfigurationRequestDto req = buildBioextractorRequest();
		when(bioextractorConfigurationRepository.existsByConfigNameIgnoreCaseAndIsDeletedFalse(anyString())).thenReturn(true);

		ResponseWrapperV2<BioextractorConfigurationResponseDto> resp =
				partnerManagementImpl.createBioextractorConfiguration(req);

		assertNotNull(resp);
		assertFalse(resp.getErrors().isEmpty());
		assertEquals(io.mosip.pms.partner.constant.ErrorCode.DUPLICATE_BIOEXTRACTOR_CONFIG_NAME.getErrorCode(),
				resp.getErrors().get(0).getErrorCode());
	}

	@Test
	public void createBioextractorConfiguration_IdCollisionRetry() throws Exception {
		setupSecurityContextForBioextractor();
		BioextractorConfigurationRequestDto req = buildBioextractorRequest();
		when(bioextractorConfigurationRepository.existsByConfigNameIgnoreCaseAndIsDeletedFalse(anyString())).thenReturn(false);
		when(bioextractorConfigurationRepository.existsById(anyString())).thenReturn(true).thenReturn(false);
		when(bioextractorConfigurationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		ResponseWrapperV2<BioextractorConfigurationResponseDto> resp =
				partnerManagementImpl.createBioextractorConfiguration(req);

		assertNotNull(resp);
		assertNotNull(resp.getResponse());
		assertEquals("SUCCESS", resp.getResponse().getStatus());
	}

	@Test
	public void createBioextractorConfiguration_MaxRetriesExceeded() {
		ReflectionTestUtils.setField(partnerManagementImpl, "maxRetries", 0);
		BioextractorConfigurationRequestDto req = buildBioextractorRequest();
		when(bioextractorConfigurationRepository.existsByConfigNameIgnoreCaseAndIsDeletedFalse(anyString())).thenReturn(false);
		when(bioextractorConfigurationRepository.existsById(anyString())).thenReturn(true);

		ResponseWrapperV2<BioextractorConfigurationResponseDto> resp =
				partnerManagementImpl.createBioextractorConfiguration(req);

		assertNotNull(resp);
		assertFalse(resp.getErrors().isEmpty());
		assertEquals(io.mosip.pms.partner.constant.ErrorCode.UNABLE_TO_GENERATE_UNIQUE_ID.getErrorCode(),
				resp.getErrors().get(0).getErrorCode());
	}

	@Test
	public void createBioextractorConfiguration_SaveException() throws Exception {
		setupSecurityContextForBioextractor();
		BioextractorConfigurationRequestDto req = buildBioextractorRequest();
		when(bioextractorConfigurationRepository.existsByConfigNameIgnoreCaseAndIsDeletedFalse(anyString())).thenReturn(false);
		when(bioextractorConfigurationRepository.existsById(anyString())).thenReturn(false);
		when(bioextractorConfigurationRepository.save(any())).thenThrow(new RuntimeException("DB error"));

		ResponseWrapperV2<BioextractorConfigurationResponseDto> resp =
				partnerManagementImpl.createBioextractorConfiguration(req);

		assertNotNull(resp);
		assertFalse(resp.getErrors().isEmpty());
		assertEquals(io.mosip.pms.partner.constant.ErrorCode.CREATE_BIOEXTRACTOR_CONFIG_ERROR.getErrorCode(),
				resp.getErrors().get(0).getErrorCode());
	}

	@Test
	public void getBioextractorConfigurationsSuccess() {
		ReflectionTestUtils.setField(partnerManagementImpl, "partnerHelper", new PartnerHelper());
		ReflectionTestUtils.setField(partnerManagementImpl, "getBioextractorConfigurationsId",
				"mosip.pms.bioextractor.configurations.get");
		BioextractorConfiguration config = new BioextractorConfiguration();
		config.setId("cfg-id-1");
		config.setConfigName("config-one");
		config.setBioextractorProviderName("provider-a");
		config.setBioextractorProviderVersion("1.0");
		config.setBioModality("face");
		config.setCrDtimes(Timestamp.valueOf(LocalDateTime.of(2026, 1, 1, 10, 30)));
		Page<BioextractorConfiguration> page = new PageImpl<>(
				Collections.singletonList(config),
				PageRequest.of(0, 8),
				1
		);
		when(bioextractorConfigurationRepository.getAllBioextractorConfigurations(
				any(), any(), any(), any(), any(Pageable.class)))
				.thenReturn(page);

		ResponseWrapperV2<PageResponseV2Dto<BioextractorConfigurationDetailDto>> resp =
				partnerManagementImpl.getBioextractorConfigurations(
						"createdDateTime", "desc", 0, 8,
						new BioextractorConfigurationFilterDto());

		assertNotNull(resp);
		assertNotNull(resp.getResponse());
		assertEquals(1, resp.getResponse().getData().size());
		assertEquals("cfg-id-1", resp.getResponse().getData().get(0).getId());
		assertEquals("config-one", resp.getResponse().getData().get(0).getConfigName());
		assertEquals(LocalDateTime.of(2026, 1, 1, 10, 30), resp.getResponse().getData().get(0).getCreatedDateTime());
		assertEquals("mosip.pms.bioextractor.configurations.get", resp.getId());
	}

	@Test
	public void getBioextractorConfigurationsEmptyList() {
		ReflectionTestUtils.setField(partnerManagementImpl, "partnerHelper", new PartnerHelper());
		ReflectionTestUtils.setField(partnerManagementImpl, "getBioextractorConfigurationsId",
				"mosip.pms.bioextractor.configurations.get");
		Page<BioextractorConfiguration> page = new PageImpl<>(
				Collections.emptyList(),
				PageRequest.of(0, 8),
				0
		);
		when(bioextractorConfigurationRepository.getAllBioextractorConfigurations(
				any(), any(), any(), any(), any(Pageable.class)))
				.thenReturn(page);

		ResponseWrapperV2<PageResponseV2Dto<BioextractorConfigurationDetailDto>> resp =
				partnerManagementImpl.getBioextractorConfigurations(
						"createdDateTime", "desc", 0, 8,
						new BioextractorConfigurationFilterDto());

		assertNotNull(resp);
		assertNotNull(resp.getResponse());
		assertTrue(resp.getResponse().getData().isEmpty());
		assertTrue(resp.getErrors() == null || resp.getErrors().isEmpty());
	}

	@Test
	public void getBioextractorConfigurationsRepositoryException() {
		ReflectionTestUtils.setField(partnerManagementImpl, "getBioextractorConfigurationsId",
				"mosip.pms.bioextractor.configurations.get");
		when(bioextractorConfigurationRepository.getAllBioextractorConfigurations(
				any(), any(), any(), any(), any(Pageable.class)))
				.thenThrow(new RuntimeException("DB error"));

		ResponseWrapperV2<PageResponseV2Dto<BioextractorConfigurationDetailDto>> resp =
				partnerManagementImpl.getBioextractorConfigurations(
						null, null, 0, 8,
						new BioextractorConfigurationFilterDto());

		assertNotNull(resp);
		assertNotNull(resp.getErrors());
		assertFalse(resp.getErrors().isEmpty());
		assertEquals(io.mosip.pms.partner.constant.ErrorCode.FETCH_BIOEXTRACTOR_CONFIGS_ERROR.getErrorCode(),
				resp.getErrors().get(0).getErrorCode());
	}

	@Test
	public void getBioextractorConfigurations_InvalidBioModalityFilter() {
		ReflectionTestUtils.setField(partnerManagementImpl, "partnerHelper", new PartnerHelper());
		ReflectionTestUtils.setField(partnerManagementImpl, "getBioextractorConfigurationsId",
				"mosip.pms.bioextractor.configurations.get");

		Object originalEnv = ReflectionTestUtils.getField(partnerManagementImpl, "environment");
		try {
			Environment env = org.mockito.Mockito.mock(Environment.class);
			when(env.getProperty(eq("mosip.pms.bioextractor.allowed.modalities.attribute.name.map"), anyString()))
					.thenReturn("face:photo,iris:iris:finger:fingerprint");
			ReflectionTestUtils.setField(partnerManagementImpl, "environment", env);

			BioextractorConfigurationFilterDto filterDto = new BioextractorConfigurationFilterDto();
			filterDto.setBioModality("Invalid");

			ResponseWrapperV2<PageResponseV2Dto<BioextractorConfigurationDetailDto>> resp =
					partnerManagementImpl.getBioextractorConfigurations(
							"createdDateTime", "desc", 0, 8, filterDto);

			assertNotNull(resp);
			assertNotNull(resp.getErrors());
			assertFalse(resp.getErrors().isEmpty());
			assertEquals(io.mosip.pms.partner.constant.ErrorCode.INVALID_INPUT_FORMAT.getErrorCode(),
					resp.getErrors().get(0).getErrorCode());
			assertTrue(resp.getErrors().get(0).getMessage().contains("Valid values are: face, iris, finger"));
		} finally {
			ReflectionTestUtils.setField(partnerManagementImpl, "environment", originalEnv);
		}
	}

	@Test
	public void getBioextractorConfigurations_ValidBioModalityFilter() {
		ReflectionTestUtils.setField(partnerManagementImpl, "partnerHelper", new PartnerHelper());
		ReflectionTestUtils.setField(partnerManagementImpl, "getBioextractorConfigurationsId",
				"mosip.pms.bioextractor.configurations.get");

		Object originalEnv = ReflectionTestUtils.getField(partnerManagementImpl, "environment");
		try {
			Environment env = org.mockito.Mockito.mock(Environment.class);
			when(env.getProperty(eq("mosip.pms.bioextractor.allowed.modalities.attribute.name.map"), anyString()))
					.thenReturn("face:photo,iris:iris:finger:fingerprint");
			ReflectionTestUtils.setField(partnerManagementImpl, "environment", env);

			Page<BioextractorConfiguration> page = new PageImpl<>(
					Collections.emptyList(),
					PageRequest.of(0, 8),
					0
			);
			when(bioextractorConfigurationRepository.getAllBioextractorConfigurations(
					any(), any(), any(), any(), any(Pageable.class)))
					.thenReturn(page);

			BioextractorConfigurationFilterDto filterDto = new BioextractorConfigurationFilterDto();
			filterDto.setBioModality("Face");

			ResponseWrapperV2<PageResponseV2Dto<BioextractorConfigurationDetailDto>> resp =
					partnerManagementImpl.getBioextractorConfigurations(
							"createdDateTime", "desc", 0, 8, filterDto);

			assertNotNull(resp);
			assertNotNull(resp.getResponse());
			assertTrue(resp.getErrors() == null || resp.getErrors().isEmpty());
		} finally {
			ReflectionTestUtils.setField(partnerManagementImpl, "environment", originalEnv);
		}
	}

	@Test
	public void getBioextractorConfigurations_ConfigEmpty_SkipsBioModalityValidation() {
		ReflectionTestUtils.setField(partnerManagementImpl, "partnerHelper", new PartnerHelper());
		ReflectionTestUtils.setField(partnerManagementImpl, "getBioextractorConfigurationsId",
				"mosip.pms.bioextractor.configurations.get");

		Object originalEnv = ReflectionTestUtils.getField(partnerManagementImpl, "environment");
		try {
			Environment env = org.mockito.Mockito.mock(Environment.class);
			when(env.getProperty(eq("mosip.pms.bioextractor.allowed.modalities.attribute.name.map"), anyString()))
					.thenReturn("");
			ReflectionTestUtils.setField(partnerManagementImpl, "environment", env);

			Page<BioextractorConfiguration> page = new PageImpl<>(
					Collections.emptyList(),
					PageRequest.of(0, 8),
					0
			);
			when(bioextractorConfigurationRepository.getAllBioextractorConfigurations(
					any(), any(), any(), any(), any(Pageable.class)))
					.thenReturn(page);

			BioextractorConfigurationFilterDto filterDto = new BioextractorConfigurationFilterDto();
			filterDto.setBioModality("not-configured-value");

			ResponseWrapperV2<PageResponseV2Dto<BioextractorConfigurationDetailDto>> resp =
					partnerManagementImpl.getBioextractorConfigurations(
							"createdDateTime", "desc", 0, 8, filterDto);

			assertNotNull(resp);
			assertNotNull(resp.getResponse());
			assertTrue(resp.getErrors() == null || resp.getErrors().isEmpty());
		} finally {
			ReflectionTestUtils.setField(partnerManagementImpl, "environment", originalEnv);
		}
	}

	@Test
	public void createBioextractorConfiguration_InvalidBioModality_ReturnsSameValidationError() {
		BioextractorConfigurationRequestDto req = buildBioextractorRequest();
		req.setBioModality("invalid");

		Object originalEnv = ReflectionTestUtils.getField(partnerManagementImpl, "environment");
		try {
			Environment env = org.mockito.Mockito.mock(Environment.class);
			when(env.getProperty(eq("mosip.pms.bioextractor.allowed.modalities.attribute.name.map"), anyString()))
					.thenReturn("face:photo,iris:iris:finger:fingerprint");
			ReflectionTestUtils.setField(partnerManagementImpl, "environment", env);

			ResponseWrapperV2<BioextractorConfigurationResponseDto> resp =
					partnerManagementImpl.createBioextractorConfiguration(req);

			assertNotNull(resp);
			assertNotNull(resp.getErrors());
			assertFalse(resp.getErrors().isEmpty());
			assertEquals(io.mosip.pms.partner.constant.ErrorCode.INVALID_INPUT_FORMAT.getErrorCode(),
					resp.getErrors().get(0).getErrorCode());
			assertTrue(resp.getErrors().get(0).getMessage().contains("Valid values are: face, iris, finger"));
		} finally {
			ReflectionTestUtils.setField(partnerManagementImpl, "environment", originalEnv);
		}
	}

	@Test
	public void getBioextractorConfigurationByIdSuccess() {
		ReflectionTestUtils.setField(partnerManagementImpl, "getBioextractorConfigurationDetailsId",
				"mosip.pms.bioextractor.configuration.details.get");
		BioextractorConfiguration config = new BioextractorConfiguration();
		config.setId("cfg-id-1");
		config.setConfigName("config-one");
		config.setBioextractorProviderName("provider-a");
		config.setBioextractorProviderVersion("1.0");
		config.setBioModality("face");
		config.setCrDtimes(Timestamp.valueOf(LocalDateTime.of(2026, 1, 1, 10, 30)));
		when(bioextractorConfigurationRepository.findByIdAndIsDeletedFalse("cfg-id-1")).thenReturn(Optional.of(config));

		ResponseWrapperV2<BioextractorConfigurationDetailDto> resp =
				partnerManagementImpl.getBioextractorConfigurationById("cfg-id-1");

		assertNotNull(resp);
		assertNotNull(resp.getResponse());
		assertEquals("cfg-id-1", resp.getResponse().getId());
		assertEquals("config-one", resp.getResponse().getConfigName());
		assertEquals("mosip.pms.bioextractor.configuration.details.get", resp.getId());
		assertTrue(resp.getErrors() == null || resp.getErrors().isEmpty());
	}

	@Test
	public void getBioextractorConfigurationByIdNullId() {
		ReflectionTestUtils.setField(partnerManagementImpl, "getBioextractorConfigurationDetailsId",
				"mosip.pms.bioextractor.configuration.details.get");

		ResponseWrapperV2<BioextractorConfigurationDetailDto> resp =
				partnerManagementImpl.getBioextractorConfigurationById(null);

		assertNotNull(resp);
		assertNotNull(resp.getErrors());
		assertFalse(resp.getErrors().isEmpty());
		assertEquals(io.mosip.pms.partner.constant.ErrorCode.INVALID_REQUEST_PARAM.getErrorCode(),
				resp.getErrors().get(0).getErrorCode());
	}

	@Test
	public void getBioextractorConfigurationByIdBlankId() {
		ReflectionTestUtils.setField(partnerManagementImpl, "getBioextractorConfigurationDetailsId",
				"mosip.pms.bioextractor.configuration.details.get");

		ResponseWrapperV2<BioextractorConfigurationDetailDto> resp =
				partnerManagementImpl.getBioextractorConfigurationById("   ");

		assertNotNull(resp);
		assertNotNull(resp.getErrors());
		assertFalse(resp.getErrors().isEmpty());
		assertEquals(io.mosip.pms.partner.constant.ErrorCode.INVALID_REQUEST_PARAM.getErrorCode(),
				resp.getErrors().get(0).getErrorCode());
	}

	@Test
	public void getBioextractorConfigurationByIdNotFound() {
		ReflectionTestUtils.setField(partnerManagementImpl, "getBioextractorConfigurationDetailsId",
				"mosip.pms.bioextractor.configuration.details.get");
		when(bioextractorConfigurationRepository.findByIdAndIsDeletedFalse("missing-id")).thenReturn(Optional.empty());

		ResponseWrapperV2<BioextractorConfigurationDetailDto> resp =
				partnerManagementImpl.getBioextractorConfigurationById("missing-id");

		assertNotNull(resp);
		assertNotNull(resp.getErrors());
		assertFalse(resp.getErrors().isEmpty());
		assertEquals(io.mosip.pms.partner.constant.ErrorCode.BIOEXTRACTOR_CONFIGURATION_NOT_FOUND.getErrorCode(),
				resp.getErrors().get(0).getErrorCode());
	}

	@Test
	public void getBioextractorConfigurationByIdRepositoryException() {
		ReflectionTestUtils.setField(partnerManagementImpl, "getBioextractorConfigurationDetailsId",
				"mosip.pms.bioextractor.configuration.details.get");
		when(bioextractorConfigurationRepository.findByIdAndIsDeletedFalse("cfg-id-1"))
				.thenThrow(new RuntimeException("DB error"));

		ResponseWrapperV2<BioextractorConfigurationDetailDto> resp =
				partnerManagementImpl.getBioextractorConfigurationById("cfg-id-1");

		assertNotNull(resp);
		assertNotNull(resp.getErrors());
		assertFalse(resp.getErrors().isEmpty());
		assertEquals(io.mosip.pms.partner.constant.ErrorCode.FETCH_BIOEXTRACTOR_CONFIG_BY_ID_ERROR.getErrorCode(),
				resp.getErrors().get(0).getErrorCode());
	}
	
	@Test
	public void deleteBioextractorConfigurationSuccess() {
		ReflectionTestUtils.setField(partnerManagementImpl, "patchDeleteBioextractorConfigurationId",
				"mosip.pms.bioextractor.configuration.delete.patch");
		
		BioextractorConfigurationDeleteRequestDto req = new BioextractorConfigurationDeleteRequestDto();
		req.setStatus("DELETED");
		
		BioextractorConfiguration config = new BioextractorConfiguration();
		config.setId("cfg-id-1");
		config.setConfigName("config-one");
		when(bioextractorConfigurationRepository.findByIdAndIsDeletedFalse("cfg-id-1"))
				.thenReturn(Optional.of(config));
		when(bioextractorConfigurationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
		
		ResponseWrapperV2<BioextractorConfigurationResponseDto> resp =
				partnerManagementImpl.deleteBioextractorConfiguration("cfg-id-1", req);
		
		assertNotNull(resp);
		assertNotNull(resp.getResponse());
		assertEquals("cfg-id-1", resp.getResponse().getId());
		assertEquals("Bio Extractor configuration deleted successfully.", resp.getResponse().getStatus());
		assertEquals("mosip.pms.bioextractor.configuration.delete.patch", resp.getId());
		assertTrue(resp.getErrors() == null || resp.getErrors().isEmpty());
		
		ArgumentCaptor<BioextractorConfiguration> captor = ArgumentCaptor.forClass(BioextractorConfiguration.class);
		Mockito.verify(bioextractorConfigurationRepository).save(captor.capture());
		assertTrue(captor.getValue().isDeleted());
	}
	
	@Test
	public void deleteBioextractorConfigurationNotFound() {
		ReflectionTestUtils.setField(partnerManagementImpl, "patchDeleteBioextractorConfigurationId",
				"mosip.pms.bioextractor.configuration.delete.patch");
		
		BioextractorConfigurationDeleteRequestDto req = new BioextractorConfigurationDeleteRequestDto();
		req.setStatus("DELETED");
		
		when(bioextractorConfigurationRepository.findByIdAndIsDeletedFalse("missing-id"))
				.thenReturn(Optional.empty());
		
		ResponseWrapperV2<BioextractorConfigurationResponseDto> resp =
				partnerManagementImpl.deleteBioextractorConfiguration("missing-id", req);
		
		assertNotNull(resp);
		assertNotNull(resp.getErrors());
		assertFalse(resp.getErrors().isEmpty());
		assertEquals(io.mosip.pms.partner.constant.ErrorCode.BIOEXTRACTOR_CONFIGURATION_NOT_FOUND.getErrorCode(),
				resp.getErrors().get(0).getErrorCode());
	}
	
	@Test
	public void deleteBioextractorConfigurationInvalidStatus_returnsInvalidInputFormat() {
		ReflectionTestUtils.setField(partnerManagementImpl, "patchDeleteBioextractorConfigurationId",
				"mosip.pms.bioextractor.configuration.delete.patch");
		
		BioextractorConfigurationDeleteRequestDto req = new BioextractorConfigurationDeleteRequestDto();
		req.setStatus("string");
		
		ResponseWrapperV2<BioextractorConfigurationResponseDto> resp =
				partnerManagementImpl.deleteBioextractorConfiguration("cfg-id-1", req);
		
		assertNotNull(resp);
		assertNotNull(resp.getErrors());
		assertFalse(resp.getErrors().isEmpty());
		assertEquals(io.mosip.pms.partner.constant.ErrorCode.INVALID_INPUT_FORMAT.getErrorCode(),
				resp.getErrors().get(0).getErrorCode());
	}
	
	@Test
	public void deleteBioextractorConfigurationNullStatus_returnsInvalidRequestParam() {
		ReflectionTestUtils.setField(partnerManagementImpl, "patchDeleteBioextractorConfigurationId",
				"mosip.pms.bioextractor.configuration.delete.patch");
		
		BioextractorConfigurationDeleteRequestDto req = new BioextractorConfigurationDeleteRequestDto();
		req.setStatus(null);
		
		ResponseWrapperV2<BioextractorConfigurationResponseDto> resp =
				partnerManagementImpl.deleteBioextractorConfiguration("cfg-id-1", req);
		
		assertNotNull(resp);
		assertNotNull(resp.getErrors());
		assertFalse(resp.getErrors().isEmpty());
		assertEquals(io.mosip.pms.partner.constant.ErrorCode.INVALID_REQUEST_PARAM.getErrorCode(),
				resp.getErrors().get(0).getErrorCode());
	}

	private void setupSecurityContextForBioextractor() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		Collection<GrantedAuthority> newAuthorities = List.of(new SimpleGrantedAuthority("PARTNER_ADMIN"));
		Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
		addAuthoritiesMethod.setAccessible(true);
		addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
	}

	private BioextractorConfigurationRequestDto buildBioextractorRequest() {
		BioextractorConfigurationRequestDto req = new BioextractorConfigurationRequestDto();
		req.setConfigName(" Config One ");
		req.setBioextractorProviderName("ProviderA");
		req.setBioextractorProviderVersion("1.0");
		req.setBioModality("face");
		return req;
	}

	@Test
	public void getPartnerPolicyRequestBioExtractors_nullRequestId_setsInvalidRequestError() {
		io.mosip.pms.partner.response.dto.BioExtractorsResponseWrapperV2 resp =
				partnerManagementImpl.getPartnerPolicyRequestBioExtractors(null);
		assertNotNull(resp);
		assertNotNull(resp.getErrors());
		assertFalse(resp.getErrors().isEmpty());
		assertEquals(io.mosip.pms.partner.constant.ErrorCode.INVALID_REQUEST_PARAM.getErrorCode(),
				resp.getErrors().get(0).getErrorCode());
	}

	@Test
	public void getPartnerPolicyRequestBioExtractors_success_mapsRows() {
		Mockito.when(partnerSearchHelper.isLoggedInUserFilterRequired()).thenReturn(false);

		Partner partner = new Partner();
		partner.setId("partner-1");
		PartnerPolicyRequest parent = new PartnerPolicyRequest();
		parent.setId("mapping-1");
		parent.setStatusCode("InProgress");
		parent.setPartner(partner);

		PartnerPolicyBioextractRequest row = new PartnerPolicyBioextractRequest();
		row.setAttributeName("attr");
		row.setBiometricModality("face");
		row.setBiometricSubTypes("left");
		row.setExtractorProvider("prov");
		row.setExtractorProviderVersion("1.0");

		when(partnerPolicyRequestRepository.findByReqId("req-1")).thenReturn(parent);
		when(partnerPolicyBioextractRequestRepository
				.findByPartnerPolicyRequestIdOrderByCrDtimesAsc("mapping-1"))
				.thenReturn(List.of(row));

		io.mosip.pms.partner.response.dto.BioExtractorsResponseWrapperV2 resp =
				partnerManagementImpl.getPartnerPolicyRequestBioExtractors("req-1");

		assertNotNull(resp);
		assertEquals("mapping-1", resp.getPartnerPolicyRequestId());
		assertEquals("InProgress", resp.getStatusCode());
		assertNotNull(resp.getResponse());
		assertNotNull(resp.getResponse().getExtractors());
		assertEquals(1, resp.getResponse().getExtractors().size());
		assertTrue(resp.getErrors() == null || resp.getErrors().isEmpty());
	}

	@Test
	public void getPartnerPolicyRequestCredentialTypes_success_trimsValue() {
		Mockito.when(partnerSearchHelper.isLoggedInUserFilterRequired()).thenReturn(false);

		Partner partner = new Partner();
		partner.setId("partner-1");
		PartnerPolicyRequest parent = new PartnerPolicyRequest();
		parent.setId("mapping-1");
		parent.setStatusCode("InProgress");
		parent.setPartner(partner);

		PartnerPolicyCredentialTypeRequest row = new PartnerPolicyCredentialTypeRequest();
		row.setCredentialType(" euin ");

		when(partnerPolicyRequestRepository.findByReqId("req-1")).thenReturn(parent);
		when(partnerPolicyCredentialTypeRequestRepository
				.findFirstByPartnerPolicyRequestIdOrderByCrDtimesAsc("mapping-1"))
				.thenReturn(Optional.of(row));

		io.mosip.pms.partner.response.dto.CredentialTypesResponseWrapperV2 resp =
				partnerManagementImpl.getPartnerPolicyRequestCredentialTypes("req-1");

		assertNotNull(resp);
		assertEquals("mapping-1", resp.getPartnerPolicyRequestId());
		assertEquals("InProgress", resp.getStatusCode());
		assertNotNull(resp.getResponse());
		assertEquals("euin", resp.getResponse().getCredentialType());
		assertTrue(resp.getErrors() == null || resp.getErrors().isEmpty());
	}

	@Test(expected = PartnerManagerServiceException.class)
	public void getPartners_emptyResult_throwsPartnerManagerServiceException() {
		when(partnerRepository.findAll()).thenReturn(Collections.emptyList());
		partnerManagementImpl.getPartners(Optional.empty());
	}

	@Test
	public void getPartners_findAll_mapsPartnersAndLogoUrl() {
		Partner p = buildPartnerForGetPartners(false);
		when(partnerRepository.findAll()).thenReturn(Collections.singletonList(p));

		PartnerDetailsResponse resp = partnerManagementImpl.getPartners(Optional.empty());

		assertNotNull(resp);
		assertNotNull(resp.getPartners());
		assertEquals(1, resp.getPartners().size());
		assertEquals("p1", resp.getPartners().get(0).getPartnerID());
		assertEquals("http://logo", resp.getPartners().get(0).getLogoUrl());
		Mockito.verify(partnerRepository).findAll();
	}

	@Test
	public void getPartners_blankPartnerTypeUsesFindAll() {
		Partner p = buildPartnerForGetPartners(false);
		when(partnerRepository.findAll()).thenReturn(Collections.singletonList(p));

		PartnerDetailsResponse resp = partnerManagementImpl.getPartners(Optional.of("   "));

		assertNotNull(resp);
		assertEquals(1, resp.getPartners().size());
		Mockito.verify(partnerRepository).findAll();
		Mockito.verify(partnerRepository, Mockito.never()).findByPartnerType(anyString());
	}

	@Test
	public void getPartners_findByPartnerType_whenTypeProvided() {
		Partner p = buildPartnerForGetPartners(false);
		when(partnerRepository.findByPartnerType("Auth_Partner")).thenReturn(Collections.singletonList(p));

		PartnerDetailsResponse resp = partnerManagementImpl.getPartners(Optional.of("Auth_Partner"));

		assertNotNull(resp);
		assertEquals(1, resp.getPartners().size());
		Mockito.verify(partnerRepository).findByPartnerType("Auth_Partner");
	}

	@Test
	public void getPartners_decryptsWhenEmailIdHashPresent() {
		Partner p = buildPartnerForGetPartners(true);
		when(partnerRepository.findAll()).thenReturn(Collections.singletonList(p));
		when(keyManagerHelper.decryptData(anyString())).thenReturn("decrypted-value");

		PartnerDetailsResponse resp = partnerManagementImpl.getPartners(Optional.empty());

		assertEquals("decrypted-value", resp.getPartners().get(0).getEmailId());
		assertEquals("decrypted-value", resp.getPartners().get(0).getContactNumber());
		assertEquals("decrypted-value", resp.getPartners().get(0).getAddress());
		Mockito.verify(keyManagerHelper, Mockito.times(3)).decryptData(anyString());
	}

	@Test
	public void getPartners_validAdditionalInfo_parsesJson() {
		Partner p = buildPartnerForGetPartners(false);
		p.setAdditionalInfo("{\"k\":\"v\"}");
		when(partnerRepository.findAll()).thenReturn(Collections.singletonList(p));

		PartnerDetailsResponse resp = partnerManagementImpl.getPartners(Optional.empty());

		assertNotNull(resp.getPartners().get(0).getAdditionalInfo());
	}

	@Test(expected = PartnerManagerServiceException.class)
	public void getPartners_invalidAdditionalInfo_throws() {
		Partner p = buildPartnerForGetPartners(false);
		p.setAdditionalInfo("not-json{{{");
		when(partnerRepository.findAll()).thenReturn(Collections.singletonList(p));
		partnerManagementImpl.getPartners(Optional.empty());
	}

	private Partner buildPartnerForGetPartners(boolean encrypted) {
		Partner p = new Partner();
		p.setId("p1");
		p.setIsActive(true);
		p.setName("Org");
		p.setContactNo("999");
		p.setEmailId("mail@test.com");
		p.setAddress("addr line");
		p.setPartnerTypeCode("Auth_Partner");
		p.setLogoUrl("http://logo");
		if (encrypted) {
			p.setEmailIdHash("hash");
		} else {
			p.setEmailIdHash(null);
		}
		p.setAdditionalInfo(null);
		return p;
	}

	@Test
	public void getSortColumn_returnsMappedValueOrAlias() {
		Map<String, String> map = new HashMap<>();
		map.put("status", "status_col");
		assertEquals("status_col", partnerManagementImpl.getSortColumn(map, "status"));
		assertEquals("unknownAlias", partnerManagementImpl.getSortColumn(map, "unknownAlias"));
	}

	@Test
	public void updateAPIKey_partnerIdNotFound_returnsError() {
		io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto requestDto =
				new io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto();
		requestDto.setStatus("De-active");
		when(partnerServiceRepository.findById("missing-partner")).thenReturn(Optional.empty());

		ResponseWrapperV2<io.mosip.pms.partner.response.dto.APIKeyUpdateResponseDto> response =
				partnerManagementImpl.updateAPIKey("missing-partner", "policy456", "apiKeyName", requestDto);

		assertNotNull(response.getErrors());
		assertFalse(response.getErrors().isEmpty());
		assertEquals(ErrorCode.PARTNER_ID_DOES_NOT_EXIST_EXCEPTION.getErrorCode(),
				response.getErrors().get(0).getErrorCode());
	}

	@Test
	public void updateAPIKey_policyIdNotFound_returnsError() {
		io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto requestDto =
				new io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto();
		requestDto.setStatus("De-active");
		Partner partner = new Partner();
		partner.setId("partner123");
		when(partnerServiceRepository.findById("partner123")).thenReturn(Optional.of(partner));
		when(authPolicyRepository.findById("missing-policy")).thenReturn(Optional.empty());

		ResponseWrapperV2<io.mosip.pms.partner.response.dto.APIKeyUpdateResponseDto> response =
				partnerManagementImpl.updateAPIKey("partner123", "missing-policy", "apiKeyName", requestDto);

		assertNotNull(response.getErrors());
		assertFalse(response.getErrors().isEmpty());
		assertEquals(ErrorCode.POLICY_NOT_EXIST_EXCEPTION.getErrorCode(),
				response.getErrors().get(0).getErrorCode());
	}

	@Test
	public void getPartnerPolicyRequestBioExtractors_loggedInFilterRequired_matchingUser_success() throws Exception {
		Mockito.when(partnerSearchHelper.isLoggedInUserFilterRequired()).thenReturn(true);
		io.mosip.kernel.openid.bridge.model.AuthUserDetails authUserDetails = mockBridgeAuthUserDetails("123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		Partner partner = new Partner();
		partner.setId("123");
		PartnerPolicyRequest parent = new PartnerPolicyRequest();
		parent.setId("mapping-1");
		parent.setStatusCode("InProgress");
		parent.setPartner(partner);

		when(partnerPolicyRequestRepository.findByReqId("req-1")).thenReturn(parent);
		when(partnerPolicyBioextractRequestRepository
				.findByPartnerPolicyRequestIdOrderByCrDtimesAsc("mapping-1"))
				.thenReturn(Collections.emptyList());

		io.mosip.pms.partner.response.dto.BioExtractorsResponseWrapperV2 resp =
				partnerManagementImpl.getPartnerPolicyRequestBioExtractors("req-1");

		assertNotNull(resp);
		assertTrue(resp.getErrors() == null || resp.getErrors().isEmpty());
	}

	@Test
	public void getPartnerPolicyRequestBioExtractors_loggedInFilterRequired_userMismatch_returnsError() {
		Mockito.when(partnerSearchHelper.isLoggedInUserFilterRequired()).thenReturn(true);
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "Auth_Partner");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		Partner partner = new Partner();
		partner.setId("other-partner");
		PartnerPolicyRequest parent = new PartnerPolicyRequest();
		parent.setId("mapping-1");
		parent.setStatusCode("InProgress");
		parent.setPartner(partner);

		when(partnerPolicyRequestRepository.findByReqId("req-1")).thenReturn(parent);

		io.mosip.pms.partner.response.dto.BioExtractorsResponseWrapperV2 resp =
				partnerManagementImpl.getPartnerPolicyRequestBioExtractors("req-1");

		assertNotNull(resp.getErrors());
		assertFalse(resp.getErrors().isEmpty());
		assertEquals(io.mosip.pms.partner.constant.ErrorCode.LOGGEDIN_USER_NOT_AUTHORIZED.getErrorCode(),
				resp.getErrors().get(0).getErrorCode());
	}

	@Test
	public void getPartnerPolicyRequestBioExtractors_nullRows_treatedAsEmptyList() {
		Mockito.when(partnerSearchHelper.isLoggedInUserFilterRequired()).thenReturn(false);

		Partner partner = new Partner();
		partner.setId("partner-1");
		PartnerPolicyRequest parent = new PartnerPolicyRequest();
		parent.setId("mapping-1");
		parent.setStatusCode("InProgress");
		parent.setPartner(partner);

		when(partnerPolicyRequestRepository.findByReqId("req-1")).thenReturn(parent);
		when(partnerPolicyBioextractRequestRepository
				.findByPartnerPolicyRequestIdOrderByCrDtimesAsc("mapping-1"))
				.thenReturn(null);

		io.mosip.pms.partner.response.dto.BioExtractorsResponseWrapperV2 resp =
				partnerManagementImpl.getPartnerPolicyRequestBioExtractors("req-1");

		assertNotNull(resp.getResponse());
		assertNotNull(resp.getResponse().getExtractors());
		assertTrue(resp.getResponse().getExtractors().isEmpty());
	}

	@Test
	public void getPartnerPolicyRequestCredentialTypes_optionalRowEmpty() {
		Mockito.when(partnerSearchHelper.isLoggedInUserFilterRequired()).thenReturn(false);

		Partner partner = new Partner();
		partner.setId("partner-1");
		PartnerPolicyRequest parent = new PartnerPolicyRequest();
		parent.setId("mapping-1");
		parent.setStatusCode("InProgress");
		parent.setPartner(partner);

		when(partnerPolicyRequestRepository.findByReqId("req-1")).thenReturn(parent);
		when(partnerPolicyCredentialTypeRequestRepository
				.findFirstByPartnerPolicyRequestIdOrderByCrDtimesAsc("mapping-1"))
				.thenReturn(Optional.empty());

		io.mosip.pms.partner.response.dto.CredentialTypesResponseWrapperV2 resp =
				partnerManagementImpl.getPartnerPolicyRequestCredentialTypes("req-1");

		assertNotNull(resp.getResponse());
		assertTrue(resp.getErrors() == null || resp.getErrors().isEmpty());
		org.junit.Assert.assertNull(resp.getResponse().getCredentialType());
	}

	@Test
	public void submitBioExtractorsRequest_whenChildRowsExist_returnsAlreadyExistsError() {
		String partnerId = "p1";
		String policyId = "pol-1";
		String requestId = "req-1";

		Partner partner = new Partner();
		partner.setId(partnerId);
		partner.setIsActive(true);
		partner.setApprovalStatus(PartnerConstants.APPROVED);

		PartnerPolicyRequest parent = new PartnerPolicyRequest();
		parent.setId(requestId);
		parent.setPartner(partner);
		parent.setPolicyId(policyId);
		parent.setStatusCode(PartnerConstants.IN_PROGRESS);
		parent.setIsDeleted(false);

		Mockito.when(partnerSearchHelper.isLoggedInUserFilterRequired()).thenReturn(false);
		when(partnerPolicyRequestRepository.findByReqId(requestId)).thenReturn(parent);
		when(partnerRepository.findById(partnerId)).thenReturn(Optional.of(partner));
		when(partnerPolicyBioextractRequestRepository.existsByPartnerPolicyRequestId(requestId)).thenReturn(true);

		BioExtractorsRequestDto req = new BioExtractorsRequestDto();
		BioExtractorsDto extractor = new BioExtractorsDto();
		extractor.setAttributeName("face");
		extractor.setBiometric("face");
		extractor.setExtractorProvider("prov");
		extractor.setExtractorProviderVersion("1.0");
		req.setExtractors(List.of(extractor));

		try {
			partnerManagementImpl.submitBioExtractorsRequest(requestId, req);
			fail("Expected PartnerServiceException");
		} catch (io.mosip.pms.partner.exception.PartnerServiceException ex) {
			assertEquals(io.mosip.pms.partner.constant.ErrorCode.BIOEXTRACT_REQUEST_ALREADY_EXISTS.getErrorCode(), ex.getErrorCode());
		}
	}

	@Test
	public void submitBioExtractorsRequest_validateExtractor_invalid_throws() {
		PartnerManagementServiceImpl target = AopTestUtils.getTargetObject(partnerManagementImpl);
		io.mosip.pms.partner.exception.PartnerServiceException ex = assertThrows(
				io.mosip.pms.partner.exception.PartnerServiceException.class,
				() -> ReflectionTestUtils.invokeMethod(target, "validateExtractorForBioExtractRequest", "p1", (BioExtractorsDto) null));
		assertNotNull(ex);
	}

	@Test
	public void submitBioExtractorsRequest_validateExtractor_valid_noThrow() {
		PartnerManagementServiceImpl target = AopTestUtils.getTargetObject(partnerManagementImpl);
		Object originalEnv = ReflectionTestUtils.getField(target, "environment");
		try {
			Environment env = org.mockito.Mockito.mock(Environment.class);
			when(env.getProperty(eq("mosip.pms.bioextractor.allowed.modalities.attribute.name.map"), anyString()))
					.thenReturn("");
			ReflectionTestUtils.setField(target, "environment", env);
			BioExtractorsDto extractor = new BioExtractorsDto();
			extractor.setAttributeName("attr");
			extractor.setBiometric("face");
			extractor.setExtractorProvider("prov");
			extractor.setExtractorProviderVersion("1.0");
			ReflectionTestUtils.invokeMethod(target, "validateExtractorForBioExtractRequest", "p1", extractor);
		} finally {
			ReflectionTestUtils.setField(target, "environment", originalEnv);
		}
	}

	@Test
	public void getPartnerPolicyRequestCredentialTypes_loggedInFilterRequired_matchingUser() throws Exception {
		Mockito.when(partnerSearchHelper.isLoggedInUserFilterRequired()).thenReturn(true);
		io.mosip.kernel.openid.bridge.model.AuthUserDetails authUserDetails = mockBridgeAuthUserDetails("123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		Partner partner = new Partner();
		partner.setId("123");
		PartnerPolicyRequest parent = new PartnerPolicyRequest();
		parent.setId("mapping-1");
		parent.setStatusCode("InProgress");
		parent.setPartner(partner);

		PartnerPolicyCredentialTypeRequest row = new PartnerPolicyCredentialTypeRequest();
		row.setCredentialType("   ");

		when(partnerPolicyRequestRepository.findByReqId("req-1")).thenReturn(parent);
		when(partnerPolicyCredentialTypeRequestRepository
				.findFirstByPartnerPolicyRequestIdOrderByCrDtimesAsc("mapping-1"))
				.thenReturn(Optional.of(row));

		io.mosip.pms.partner.response.dto.CredentialTypesResponseWrapperV2 resp =
				partnerManagementImpl.getPartnerPolicyRequestCredentialTypes("req-1");

		assertTrue(resp.getErrors() == null || resp.getErrors().isEmpty());
		org.junit.Assert.assertNull(resp.getResponse().getCredentialType());
	}
}
