package io.mosip.pms.test.partner.service.impl;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.pms.common.entity.AuthPolicy;
import io.mosip.pms.common.entity.MISPLicenseEntity;
import io.mosip.pms.common.entity.MISPLicenseKey;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.entity.PartnerPolicy;
import io.mosip.pms.common.entity.PartnerPolicyRequest;
import io.mosip.pms.common.entity.PolicyGroup;
import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.repository.AuthPolicyRepository;
import io.mosip.pms.common.repository.MispLicenseKeyRepository;
import io.mosip.pms.common.repository.PartnerPolicyRepository;
import io.mosip.pms.common.repository.PartnerPolicyRequestRepository;
import io.mosip.pms.common.repository.PartnerRepository;
import io.mosip.pms.common.repository.PolicyGroupRepository;
import io.mosip.pms.common.service.NotificatonService;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.partner.manager.constant.PartnerManageEnum;
import io.mosip.pms.partner.manager.dto.StatusRequestDto;
import io.mosip.pms.partner.manager.dto.PartnersPolicyMappingRequest;
import io.mosip.pms.partner.manager.exception.PartnerManagerServiceException;
import io.mosip.pms.partner.manager.service.impl.PartnerManagementServiceImpl;
import io.mosip.pms.partner.response.dto.PartnerCertDownloadResponeDto;


@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional("pmsPlatformTransactionManager")
public class PartnerManagementServiceImplTest {
	
	@Autowired
	private PartnerManagementServiceImpl partnerManagementImpl;
	
	@Mock
	PartnerPolicyRepository partnerPolicyRepository;

	@Mock
	PartnerRepository partnerRepository;
	
	@Mock
	PartnerPolicyRequestRepository partnerPolicyRequestRepository;
	
	@Mock
	PolicyGroupRepository policyGroupRepository;
	
	@Mock
	AuthPolicyRepository authPolicyRepository;
	
	@Mock
	private MispLicenseKeyRepository misplKeyRepository;
	
	@Mock
	private WebSubPublisher webSubPublisher;
	

	@MockBean
	private AuditUtil audit;
	
	@MockBean
	private NotificatonService notificationService;
	
	@MockBean
	private RestUtil restUtil;
	
	@Mock
    private ObjectMapper mapper;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(partnerManagementImpl, "policyGroupRepository", policyGroupRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "partnerRepository", partnerRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "authPolicyRepository", authPolicyRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "partnerPolicyRequestRepository", partnerPolicyRequestRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "partnerPolicyRepository", partnerPolicyRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "misplKeyRepository", misplKeyRepository);
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
	@Test
	@Ignore
	public void activateDeactivateAuthEKYCPartnerTest_S2() {
		StatusRequestDto req = new StatusRequestDto();
		req.setStatus("De-Active");
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
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
		partner.get().setIsActive(false);
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
	public void activateDeactivatePartnerAPIKeyGivenPartnerTest_S1() {
		StatusRequestDto req = new StatusRequestDto();
		req.setStatus("Active");
		String partnerId = "12345";
		String apiKey ="2345";
		partnerManagementImpl.activateDeactivatePartnerAPIKeyGivenPartner(partnerId, req, apiKey);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void activateDeactivatePartnerAPIKeyGivenPartnerTest_S2() {
		StatusRequestDto req = new StatusRequestDto();
		req.setStatus("Active");
		String partnerId = "12345";
		String apiKey ="2345";
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		Mockito.when(partnerPolicyRepository.findById(apiKey)).thenReturn(partnerPolicy);
		partnerManagementImpl.activateDeactivatePartnerAPIKeyGivenPartner(partnerId, req, apiKey);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void activateDeactivatePartnerAPIKeyGivenPartnerTest_S3() {
		StatusRequestDto req = new StatusRequestDto();
		req.setStatus("ctive");
		String partnerId = "123456";
		String apiKey ="2345";
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		Mockito.when(partnerPolicyRepository.findById(apiKey)).thenReturn(partnerPolicy);
		partnerManagementImpl.activateDeactivatePartnerAPIKeyGivenPartner(partnerId, req, apiKey);
	}
	
	@Test
	public void activateDeactivatePartnerAPIKeyGivenPartnerTest_S4() {
		StatusRequestDto req = new StatusRequestDto();
		req.setStatus("Active");
		String partnerId = "123456";
		String apiKey ="2345";
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndApikey(Mockito.anyString(), Mockito.anyString())).thenReturn(getPartnerPolicy());
		Mockito.when(partnerPolicyRepository.findById(apiKey)).thenReturn(partnerPolicy);
		Mockito.when(authPolicyRepository.findById(partnerPolicy.get().getPolicyId())).thenReturn(Optional.of(getAuthPolicies().get(0)));
		partnerManagementImpl.activateDeactivatePartnerAPIKeyGivenPartner(partnerId, req, apiKey);
	}
	
	@Test
	public void activateDeactivatePartnerAPIKeyGivenPartnerTest_S5() {
		StatusRequestDto req = new StatusRequestDto();
		req.setStatus("De-Active");
		String partnerId = "123456";
		String apiKey ="2345";
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndApikey(Mockito.anyString(), Mockito.anyString())).thenReturn(getPartnerPolicy());
		Mockito.when(partnerPolicyRepository.findById(apiKey)).thenReturn(partnerPolicy);
		Mockito.when(authPolicyRepository.findById(partnerPolicy.get().getPolicyId())).thenReturn(Optional.of(getAuthPolicies().get(0)));
		partnerManagementImpl.activateDeactivatePartnerAPIKeyGivenPartner(partnerId, req, apiKey);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void activateDeactivatePartnerAPIKeyGivenPartnerTest_S6() {
		StatusRequestDto req = new StatusRequestDto();
		req.setStatus("NOTActive");
		String partnerId = "123456";
		String apiKey ="2345";
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndApikey(Mockito.anyString(), Mockito.anyString())).thenReturn(getPartnerPolicy());
		Mockito.when(partnerPolicyRepository.findById(apiKey)).thenReturn(partnerPolicy);
		Mockito.when(authPolicyRepository.findById(partnerPolicy.get().getPolicyId())).thenReturn(Optional.of(getAuthPolicies().get(0)));
		partnerManagementImpl.activateDeactivatePartnerAPIKeyGivenPartner(partnerId, req, apiKey);
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
	
	
	@Test(expected = PartnerManagerServiceException.class)
	public void approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestIdTest_S1() {
		StatusRequestDto req = new StatusRequestDto();
		req.setStatus("De-Active");
		String partnerApiKey="2345";
		partnerManagementImpl.approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(req,partnerApiKey);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestIdTest_S2() {
		StatusRequestDto req = new StatusRequestDto();
		req.setStatus("De-Active");
		String apiKey ="2345";	
		Mockito.when(partnerPolicyRequestRepository.findById(apiKey)).thenReturn(Optional.of(getPartnerPolicyRequestData()));
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById("123456")).thenReturn(partner);
		partnerManagementImpl.approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(req,apiKey);
	}
	
	@Test
	public void approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestIdTest_S3() throws IOException {
		StatusRequestDto req = new StatusRequestDto();
		Map<String, Object> response = new HashMap<>();
		response.put("response", getCertResponse());
		response.put("id",null);
		response.put("version", null);
		req.setStatus("Approved");
		String apiKey ="2345";
		PartnerCertDownloadResponeDto certDto = new PartnerCertDownloadResponeDto();
		certDto.setCertificateData("sdfghjjhgffghjijhgrffghjhbvfghgftgbvgbghbvghjkjhb");
		certDto.setTimestamp(LocalDateTime.now());
		Mockito.when(partnerPolicyRequestRepository.findById(apiKey)).thenReturn(Optional.of(getPartnerPolicyRequestData()));
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById("123456")).thenReturn(partner);
		Mockito.when(authPolicyRepository.findById(Optional.of(getPartnerPolicyRequestData()).get().getPolicyId())).thenReturn(Optional.of(getAuthPolicies().get(0)));
		Mockito.when(restUtil.getApi(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(response);
//		Mockito.when(
//				mapper.readValue(Mockito.anyString(), Mockito.eq(PartnerCertDownloadResponeDto.class)))
//				.thenReturn(certDto);
		partnerManagementImpl.approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(req,apiKey);
	}

	@Test
	public void approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestIdTest_S10() throws IOException {
		StatusRequestDto req = new StatusRequestDto();
		Map<String, Object> response = new HashMap<>();
		response.put("response", getCertResponse());
		response.put("id",null);
		response.put("version", null);
		req.setStatus("rejected");
		String apiKey ="2345";	
		Mockito.when(partnerPolicyRequestRepository.findById(apiKey)).thenReturn(Optional.of(getPartnerPolicyRequestData()));
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById("123456")).thenReturn(partner);
		Mockito.when(authPolicyRepository.findById(Optional.of(getPartnerPolicyRequestData()).get().getPolicyId())).thenReturn(Optional.of(getAuthPolicies().get(0)));
		Mockito.when(restUtil.getApi(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(response);
		partnerManagementImpl.approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(req,apiKey);
	}
	@Test(expected = PartnerManagerServiceException.class)
	public void approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestIdTest_S11() throws IOException {
		StatusRequestDto req = new StatusRequestDto();
		Map<String, Object> response = new HashMap<>();
		response.put("response", getCertResponse());
		response.put("id",null);
		response.put("version", null);
		req.setStatus("notrejected");
		String apiKey ="2345";	
		Mockito.when(partnerPolicyRequestRepository.findById(apiKey)).thenReturn(Optional.of(getPartnerPolicyRequestData()));
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById("123456")).thenReturn(partner);
		Mockito.when(authPolicyRepository.findById(Optional.of(getPartnerPolicyRequestData()).get().getPolicyId())).thenReturn(Optional.of(getAuthPolicies().get(0)));
		Mockito.when(restUtil.getApi(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(response);
		partnerManagementImpl.approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(req,apiKey);
	}

	@SuppressWarnings("unchecked")
	private JSONObject getCertResponse() {
		JSONObject obj=new JSONObject();		
		obj.put("certificateData", "I6RNkys7tjbmOQhJkgY1HhRpvts8LZPioJD4I82wsMHDtGj");
		obj.put("timestamp", "2021-09-02T07:43:15.577329");		
		return obj;
	}
	
	@Test
	@Ignore
	public void approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestIdTest_S4() {
		StatusRequestDto req = new StatusRequestDto();
		req.setStatus("Rejected");
		String apiKey ="2345";	
		Mockito.when(partnerPolicyRequestRepository.findById(apiKey)).thenReturn(Optional.of(getPartnerPolicyRequestData()));
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById("123456")).thenReturn(partner);
		partnerManagementImpl.approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(req,apiKey);
	}
	
	@Test
	@Ignore
	public void approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestIdTest_S5() {
		StatusRequestDto req = new StatusRequestDto();
		req.setStatus("Approved");
		String apiKey ="2345";	
		Mockito.when(partnerPolicyRequestRepository.findById(apiKey)).thenReturn(Optional.of(getPartnerPolicyRequestData()));
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById("123456")).thenReturn(partner);
		String partnerId = "123456";
		PartnerPolicy partnerFromDB = Optional.of(getPartnerPolicy()).get();
		Mockito.when(partnerPolicyRepository.findByPartnerId(partnerId)).thenReturn(partnerFromDB);
		partnerManagementImpl.approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(req,apiKey);
	}	
	
	@Test
	@Ignore
	public void approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestIdTest_S6() {
		StatusRequestDto req = new StatusRequestDto();
		req.setStatus("Approved");
		String apiKey ="2345";	
		Mockito.when(partnerPolicyRequestRepository.findById(apiKey)).thenReturn(Optional.of(getPartnerPolicyRequestData()));
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById("123456")).thenReturn(partner);	
		Mockito.when(partnerPolicyRepository.findByPartnerId("7267132")).thenReturn(null);
		Optional<PartnerPolicy> partnerFromDB1 = Optional.of(getPartnerPolicy());
		Mockito.when(partnerPolicyRepository.findById("30636009")).thenReturn(partnerFromDB1);
		Mockito.when(authPolicyRepository.findById("234")).thenReturn(Optional.of(getAuthPolicies().get(0)));
		partnerManagementImpl.approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(req,apiKey);
	}	
	
	@Test (expected = PartnerManagerServiceException.class)
	public void approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestIdTest_S7() {
		StatusRequestDto req = new StatusRequestDto();
		req.setStatus("Rejected");
		String apiKey ="2345";	
		Mockito.when(partnerPolicyRequestRepository.findById(apiKey)).thenReturn(Optional.of(getApprovedPartnerPolicyRequestData()));
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById("123456")).thenReturn(partner);
		partnerManagementImpl.approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(req,apiKey);
	}
	
	@Test (expected = PartnerManagerServiceException.class)
	public void approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestIdTest_S8() {
		StatusRequestDto req = new StatusRequestDto();
		req.setStatus("Rejected");
		String apiKey ="2345";	
		Mockito.when(partnerPolicyRequestRepository.findById(apiKey)).thenReturn(Optional.of(getRejectedPartnerPolicyRequestData()));
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById("123456")).thenReturn(partner);
		partnerManagementImpl.approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(req,apiKey);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S1() {
		partnerManagementImpl.getPartnerMappedPolicyFile("","","",false);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S2() {		
		MISPLicenseEntity license = mispLicense();
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(Optional.of(getPartner()));
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy","","",false);
	}	
	
	@Test(expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S3() {
		MISPLicenseEntity license = mispLicense();
		license.setValidToDate(LocalDateTime.now().plusDays(-10));
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(Optional.of(getPartner()));
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy","","",false);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S34() {
		MISPLicenseEntity license = mispLicense();
		license.setValidToDate(LocalDateTime.now().plusDays(-10));
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(null);
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(Optional.of(getPartner()));
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy","","",false);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S35() {
		MISPLicenseEntity license = mispLicense();
		license.setIsActive(false);
		license.setValidToDate(LocalDateTime.now().plusDays(10));
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(Optional.of(getPartner()));
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy","","",false);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S40() {
		MISPLicenseEntity license = mispLicense();
		license.setIsActive(true);
		license.setValidToDate(LocalDateTime.now().plusDays(10));
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(Optional.of(getPartner()));
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndApikey(Mockito.anyString(),Mockito.anyString())).thenReturn(null);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy","","",false);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S41() {
		Mockito.when(misplKeyRepository.findByLicensekey(Mockito.anyString())).thenReturn(mispLicense());
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(Optional.of(getPartner()));
		PartnerPolicy partnerPolicy = getPartnerPolicy();
		partnerPolicy.setIsActive(false);
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndApikey(Mockito.anyString(),Mockito.anyString())).thenReturn(partnerPolicy);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy","","",false);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S42() {
		Mockito.when(misplKeyRepository.findByLicensekey(Mockito.anyString())).thenReturn(mispLicense());
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(Optional.of(getPartner()));
		PartnerPolicy partnerPolicy = getPartnerPolicy();
		partnerPolicy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().minusDays(10)));
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndApikey(Mockito.anyString(),Mockito.anyString())).thenReturn(partnerPolicy);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy","","",false);
	}
	

	@Test(expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S51() {
		Mockito.when(misplKeyRepository.findByLicensekey(Mockito.anyString())).thenReturn(mispLicense());
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(Optional.of(getPartner()));
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndApikey(Mockito.anyString(),Mockito.anyString())).thenReturn(getPartnerPolicy());		
		Mockito.when(authPolicyRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy","","",false);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S52() {
		Mockito.when(misplKeyRepository.findByLicensekey(Mockito.anyString())).thenReturn(mispLicense());		
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(Optional.of(getPartner()));
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndApikey(Mockito.anyString(),Mockito.anyString())).thenReturn(getPartnerPolicy());
		Optional<AuthPolicy> authPolicy = Optional.of(getAuthPolicies().get(0));
		authPolicy.get().setIsActive(false);
		Mockito.when(authPolicyRepository.findById(Mockito.anyString())).thenReturn(authPolicy);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy","","",false);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S53() {
		Mockito.when(misplKeyRepository.findByLicensekey(Mockito.anyString())).thenReturn(mispLicense());
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(Optional.of(getPartner()));
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndApikey(Mockito.anyString(),Mockito.anyString())).thenReturn(getPartnerPolicy());
		Optional<AuthPolicy> authPolicy = Optional.of(getAuthPolicies().get(0));
		authPolicy.get().setPolicyGroup(null);
		Mockito.when(authPolicyRepository.findById(Mockito.anyString())).thenReturn(authPolicy);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy","","",false);
	}

	@Test(expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S54() {
		Mockito.when(misplKeyRepository.findByLicensekey(Mockito.anyString())).thenReturn(mispLicense());
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(Optional.of(getPartner()));
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndApikey(Mockito.anyString(),Mockito.anyString())).thenReturn(getPartnerPolicy());
		Optional<AuthPolicy> authPolicy = Optional.of(getAuthPolicies().get(0));
		authPolicy.get().getPolicyGroup().setIsActive(false);
		Mockito.when(authPolicyRepository.findById(Mockito.anyString())).thenReturn(authPolicy);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy","","",false);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S55() {
		Mockito.when(misplKeyRepository.findByLicensekey(Mockito.anyString())).thenReturn(mispLicense());
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(Optional.of(getPartner()));
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndApikey(Mockito.anyString(),Mockito.anyString())).thenReturn(getPartnerPolicy());
		Optional<AuthPolicy> authPolicy = Optional.of(getAuthPolicies().get(0));
		authPolicy.get().setValidToDate(LocalDateTime.now().minusDays(10));
		Mockito.when(authPolicyRepository.findById(Mockito.anyString())).thenReturn(authPolicy);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy","","",false);
	}
	
	@Test
	public void getPartnerMappedPolicyFileTest_S61() {
		Mockito.when(misplKeyRepository.findByLicensekey(Mockito.anyString())).thenReturn(mispLicense());
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(Optional.of(getPartner()));
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndApikey(Mockito.anyString(),Mockito.anyString())).thenReturn(getPartnerPolicy());
		Optional<AuthPolicy> authPolicy = Optional.of(getAuthPolicies().get(0));
		Mockito.when(authPolicyRepository.findById(Mockito.anyString())).thenReturn(authPolicy);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy","","",false);
	}
	
	@Test
	public void getPartnerMappedPolicyFileTest_S62() {
		Map<String, Object> response = new HashMap<>();
		response.put("response", getCertResponse());
		response.put("id",null);
		response.put("version", null);
		Mockito.when(restUtil.getApi(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(response);
		Mockito.when(misplKeyRepository.findByLicensekey(Mockito.anyString())).thenReturn(mispLicense());
		Mockito.when(partnerRepository.findById(Mockito.anyString())).thenReturn(Optional.of(getPartner()));
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndApikey(Mockito.anyString(),Mockito.anyString())).thenReturn(getPartnerPolicy());
		Optional<AuthPolicy> authPolicy = Optional.of(getAuthPolicies().get(0));
		Mockito.when(authPolicyRepository.findById(Mockito.anyString())).thenReturn(authPolicy);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy","","",true);
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


	private MISPLicenseEntity mispLicense() {
		MISPLicenseEntity mispLices = new MISPLicenseEntity();
		mispLices.setMispLicenseUniqueKey(mispLUniqueKey());
		mispLices.setValidFromDate(LocalDateTime.now());
		mispLices.setValidToDate(LocalDateTime.now().plusDays(90));
		mispLices.setIsActive(true);
		return mispLices;
	}
	
	private MISPLicenseKey mispLUniqueKey() {
		MISPLicenseKey unique = new MISPLicenseKey();
		unique.setMisp_id("100");
		unique.setLicense_key("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy");
		return unique;
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
	
	private PartnerPolicyRequest getApprovedPartnerPolicyRequestData() {
		PartnerPolicyRequest partnerPolicyRequest = new PartnerPolicyRequest();
		partnerPolicyRequest.setId("1234456");
		partnerPolicyRequest.setPartner(getPartner());
		partnerPolicyRequest.setPolicyId("2345");
		partnerPolicyRequest.setStatusCode("APPROVED");
		return partnerPolicyRequest;
	}
	
	private PartnerPolicyRequest getRejectedPartnerPolicyRequestData() {
		PartnerPolicyRequest partnerPolicyRequest = new PartnerPolicyRequest();
		partnerPolicyRequest.setId("1234456");
		partnerPolicyRequest.setPartner(getPartner());
		partnerPolicyRequest.setPolicyId("2345");
		partnerPolicyRequest.setStatusCode("REJECTED");
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
		return part;
	}
}
