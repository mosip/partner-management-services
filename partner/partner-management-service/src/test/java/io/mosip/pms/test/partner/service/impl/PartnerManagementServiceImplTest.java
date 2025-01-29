package io.mosip.pms.test.partner.service.impl;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import io.mosip.kernel.openid.bridge.model.AuthUserDetails;
import io.mosip.kernel.openid.bridge.model.MosipUserDto;
import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.entity.*;
import io.mosip.pms.common.repository.*;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.partner.dto.KeycloakUserDto;
import io.mosip.pms.partner.manager.dto.*;
import io.mosip.pms.partner.response.dto.PartnerCertDownloadResponeDto;
import io.mosip.pms.partner.util.PartnerHelper;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import io.mosip.pms.common.service.NotificatonService;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.partner.manager.constant.ErrorCode;
import io.mosip.pms.partner.manager.constant.PartnerManageEnum;
import io.mosip.pms.partner.manager.exception.PartnerManagerServiceException;
import io.mosip.pms.partner.manager.service.impl.PartnerManagementServiceImpl;
import io.mosip.pms.partner.request.dto.APIKeyGenerateRequestDto;
import io.mosip.pms.partner.request.dto.APIkeyStatusUpdateRequestDto;
import io.mosip.pms.test.config.TestSecurityConfig;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
public class PartnerManagementServiceImplTest {
	
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
	MispLicenseRepository mispLicenseRepository;
	
	@Mock
	BiometricExtractorProviderRepository extractorProviderRepository;
	
	@Mock
	private WebSubPublisher webSubPublisher;

	@Mock
	Authentication authentication;

	@Mock
	SecurityContext securityContext;

	@Mock
	PartnerHelper partnerHelper;
	
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
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(partnerManagementImpl, "policyGroupRepository", policyGroupRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "partnerRepository", partnerRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "authPolicyRepository", authPolicyRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "partnerPolicyRequestRepository", partnerPolicyRequestRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "partnerPolicyRepository", partnerPolicyRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "extractorProviderRepository", extractorProviderRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "mispLicenseRepository", mispLicenseRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "webSubPublisher", webSubPublisher);
		ReflectionTestUtils.setField(partnerManagementImpl, "restUtil", restUtil);		
//		ReflectionTestUtils.setField(partnerManagementImpl, "mapper", mapper);		
		Mockito.doNothing().when(webSubPublisher).notify(Mockito.any(),Mockito.any(),Mockito.any());
		Mockito.doNothing().when(audit).setAuditRequestDto(Mockito.any(PartnerManageEnum.class));
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
		MISPLicenseEntity license = new MISPLicenseEntity();
		license.setLicenseKey("qwertyhgfdsdfghb");
		license.setIsActive(true);
		license.setMispId("12345");
		license.setValidFromDate(LocalDateTime.now().minusDays(1));
		license.setValidToDate(LocalDateTime.now().plusDays(5));
		Mockito.when(mispLicenseRepository.findByMispIdAndIsActive("123456")).thenReturn(List.of(license));
		Map<String, Object> response = new HashMap<>();
		response.put("response", getCertResponse());
		response.put("id",null);
		response.put("version", null);
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
		Mockito.when(partnerPolicyRepository.findByPartnerIdPolicyIdAndLabel(Mockito.any(), Mockito.any(),
				Mockito.any())).thenReturn(getPartnerPolicy());
		Mockito.when(authPolicyRepository.findById(Mockito.any())).thenReturn(Optional.of(getAuthPolicies().get(0)));

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
		Mockito.when(partnerPolicyRepository.findByPartnerIdPolicyIdAndLabel(Mockito.any(), Mockito.any(),
				Mockito.any())).thenReturn(getPartnerPolicy());
		Mockito.when(authPolicyRepository.findById(Mockito.any())).thenReturn(Optional.of(getAuthPolicies().get(0)));

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

		partnerManagementImpl.updateAPIKeyStatus("1234", "456", statusDto);
	}

	@Test
	public void updateAPIKeyStatusTest05() throws Exception {
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
		Mockito.when(extractorProviderRepository.findByPartnerAndPolicyId(Mockito.any(),Mockito.any())).thenReturn(List.of());
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
		Mockito.when(partnerPolicyRepository.findByPartnerIdPolicyIdAndLabel("partner","234","unique")).thenReturn(getPartnerPolicy());
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
	
	
	

	@SuppressWarnings("unchecked")
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
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
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
				"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\n" +
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
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		partnerManagementImpl.getPartnerDetails("123");
	}

	@Test
	public void getAdminPartnersTest01() throws Exception {
		MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
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
		when(partnerSummaryRepository.getSummaryOfAllPartners(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any(), any())).thenReturn(page);
		partnerManagementImpl.getAdminPartners(sortFieldName, sortType, pageNo, pageSize, partnerFilterDto);
	}

	@Test
	public void getAdminPartnersTest02() throws Exception {
		MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		String sortFieldName = "certificateUploadStatus";
		String sortType = "desc";
		Integer pageNo = 0;
		Integer pageSize = 8;
		ResponseWrapperV2<PageResponseV2Dto<PartnerSummaryDto>> responseWrapper = new ResponseWrapperV2<>();
		Page<PartnerSummaryEntity> page = null;
		when(partnerSummaryRepository.getSummaryOfAllPartners(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), any(), any())).thenReturn(page);
		partnerManagementImpl.getAdminPartners(sortFieldName, sortType, pageNo, pageSize, null);
	}

	@Test
	public void getAllApiKeyRequestsTest01() throws Exception {
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

		when(apiKeyRequestSummaryRepository.getSummaryOfAllApiKeyRequests(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyList(), anyBoolean(), any())).thenReturn(page);
		partnerManagementImpl.getAllApiKeyRequests(sortFieldName, sortType, pageNo, pageSize, apiKeyFilterDto);
	}

	@Test
	public void getAllApiKeyRequestsTest02() throws Exception {
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
		when(partnerPolicyMappingRequestRepository.getSummaryOfAllPartnerPolicyRequests(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyList(), anyBoolean(), any())).thenReturn(page);
		partnerManagementImpl.getAllPartnerPolicyRequests(sortFieldName, sortType, pageNo, pageSize, partnerPolicyRequestFilterDto);
	}

	@Test
	public void getAllPartnerPolicyRequestsTest02() throws Exception {
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
	public void downloadTrustCertificatesTest01() throws Exception {
		MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		Map<String, Object> apiResponse = new HashMap<>();
		Map<String, Object> response = new HashMap<>();
		response.put("p7bFile", "-----BEGIN CERTIFICATE-----\n" +
				"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\n" +
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

	private io.mosip.kernel.openid.bridge.model.MosipUserDto getMosipUserDto() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = new io.mosip.kernel.openid.bridge.model.MosipUserDto();
		mosipUserDto.setUserId("123");
		mosipUserDto.setMail("abc@gmail.com");
		return mosipUserDto;
	}
}
