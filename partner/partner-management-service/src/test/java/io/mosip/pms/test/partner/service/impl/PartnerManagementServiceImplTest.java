package io.mosip.pms.test.partner.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import io.mosip.pms.common.entity.AuthPolicy;
import io.mosip.pms.common.entity.MISPEntity;
import io.mosip.pms.common.entity.MISPLicenseEntity;
import io.mosip.pms.common.entity.MISPLicenseKey;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.entity.PartnerPolicy;
import io.mosip.pms.common.entity.PartnerPolicyRequest;
import io.mosip.pms.common.entity.PolicyGroup;
import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.repository.AuthPolicyRepository;
import io.mosip.pms.common.repository.MispLicenseKeyRepository;
import io.mosip.pms.common.repository.MispServiceRepository;
import io.mosip.pms.common.repository.PartnerPolicyRepository;
import io.mosip.pms.common.repository.PartnerPolicyRequestRepository;
import io.mosip.pms.common.repository.PartnerRepository;
import io.mosip.pms.common.repository.PolicyGroupRepository;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.partner.manager.dto.ActivateDeactivatePartnerRequest;
import io.mosip.pms.partner.manager.dto.PartnersPolicyMappingRequest;
import io.mosip.pms.partner.manager.exception.PartnerManagerServiceException;
import io.mosip.pms.partner.manager.service.impl.PartnerManagementServiceImpl;
import io.mosip.pms.test.PartnerManagementServiceTest;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = { PartnerManagementServiceTest.class })
@AutoConfigureMockMvc
@EnableWebMvc
@Ignore
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
	MispServiceRepository mispRepository;
	
	@Mock
	private WebSubPublisher webSubPublisher;
	

	@MockBean
	private AuditUtil audit;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(partnerManagementImpl, "policyGroupRepository", policyGroupRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "partnerRepository", partnerRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "authPolicyRepository", authPolicyRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "partnerPolicyRequestRepository", partnerPolicyRequestRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "partnerPolicyRepository", partnerPolicyRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "misplKeyRepository", misplKeyRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "mispRepository", mispRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "webSubPublisher", webSubPublisher);		
		Mockito.doNothing().when(webSubPublisher).notify(Mockito.any(),Mockito.any(),Mockito.any());
		Mockito.doNothing().when(audit).setAuditRequestDto(Mockito.any());
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
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
		req.setStatus("De-Active");
		String partnerId = "12345";
		partnerManagementImpl.activateDeactivateAuthEKYCPartner(partnerId, req);
	}
	@Test
	public void activateDeactivateAuthEKYCPartnerTest_S2() {
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
		req.setStatus("De-Active");
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		partnerManagementImpl.activateDeactivateAuthEKYCPartner(partnerId, req);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void activateDeactivateAuthEKYCPartnerTest_S3() {
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
		req.setStatus("ctive");
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		partnerManagementImpl.activateDeactivateAuthEKYCPartner(partnerId, req);
	}
	
	@Test
	public void activateDeactivateAuthEKYCPartnerTest_S4() {
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
		req.setStatus("Active");
		String partnerId = "12345";
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById(partnerId)).thenReturn(partner);
		partnerManagementImpl.activateDeactivateAuthEKYCPartner(partnerId, req);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void activateDeactivatePartnerAPIKeyGivenPartnerTest_S1() {
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
		req.setStatus("Active");
		String partnerId = "12345";
		String apiKey ="2345";
		partnerManagementImpl.activateDeactivatePartnerAPIKeyGivenPartner(partnerId, req, apiKey);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void activateDeactivatePartnerAPIKeyGivenPartnerTest_S2() {
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
		req.setStatus("Active");
		String partnerId = "12345";
		String apiKey ="2345";
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		Mockito.when(partnerPolicyRepository.findById(apiKey)).thenReturn(partnerPolicy);
		partnerManagementImpl.activateDeactivatePartnerAPIKeyGivenPartner(partnerId, req, apiKey);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void activateDeactivatePartnerAPIKeyGivenPartnerTest_S3() {
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
		req.setStatus("ctive");
		String partnerId = "123456";
		String apiKey ="2345";
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		Mockito.when(partnerPolicyRepository.findById(apiKey)).thenReturn(partnerPolicy);
		partnerManagementImpl.activateDeactivatePartnerAPIKeyGivenPartner(partnerId, req, apiKey);
	}
	
	@Test
	public void activateDeactivatePartnerAPIKeyGivenPartnerTest_S4() {
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
		req.setStatus("Active");
		String partnerId = "123456";
		String apiKey ="2345";
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndApikey(Mockito.anyString(), Mockito.anyString())).thenReturn(getPartnerPolicy());
		Mockito.when(partnerPolicyRepository.findById(apiKey)).thenReturn(partnerPolicy);
		partnerManagementImpl.activateDeactivatePartnerAPIKeyGivenPartner(partnerId, req, apiKey);
	}
	
	@Test
	public void activateDeactivatePartnerAPIKeyGivenPartnerTest_S5() {
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
		req.setStatus("De-Active");
		String partnerId = "123456";
		String apiKey ="2345";
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndApikey(Mockito.anyString(), Mockito.anyString())).thenReturn(getPartnerPolicy());
		Mockito.when(partnerPolicyRepository.findById(apiKey)).thenReturn(partnerPolicy);
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
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
		req.setStatus("De-Active");
		String partnerApiKey="2345";
		partnerManagementImpl.approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(req,partnerApiKey);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestIdTest_S2() {
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
		req.setStatus("De-Active");
		String apiKey ="2345";	
		Mockito.when(partnerPolicyRequestRepository.findById(apiKey)).thenReturn(Optional.of(getPartnerPolicyRequestData()));
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById("123456")).thenReturn(partner);
		partnerManagementImpl.approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(req,apiKey);
	}
	
	@Test
	public void approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestIdTest_S3() {
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
		req.setStatus("Approved");
		String apiKey ="2345";	
		Mockito.when(partnerPolicyRequestRepository.findById(apiKey)).thenReturn(Optional.of(getPartnerPolicyRequestData()));
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById("123456")).thenReturn(partner);
		partnerManagementImpl.approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(req,apiKey);
	}
	
	@Test
	public void approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestIdTest_S4() {
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
		req.setStatus("Rejected");
		String apiKey ="2345";	
		Mockito.when(partnerPolicyRequestRepository.findById(apiKey)).thenReturn(Optional.of(getPartnerPolicyRequestData()));
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById("123456")).thenReturn(partner);
		partnerManagementImpl.approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(req,apiKey);
	}
	
	@Test
	public void approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestIdTest_S5() {
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
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
	public void approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestIdTest_S6() {
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
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
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
		req.setStatus("Rejected");
		String apiKey ="2345";	
		Mockito.when(partnerPolicyRequestRepository.findById(apiKey)).thenReturn(Optional.of(getApprovedPartnerPolicyRequestData()));
		Optional<Partner> partner = Optional.of(getPartner());
		Mockito.when(partnerRepository.findById("123456")).thenReturn(partner);
		partnerManagementImpl.approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(req,apiKey);
	}
	
	@Test (expected = PartnerManagerServiceException.class)
	public void approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestIdTest_S8() {
		ActivateDeactivatePartnerRequest req = new ActivateDeactivatePartnerRequest();
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
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy","","",false);
	}	
	
	@Test(expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S3() {
		MISPLicenseEntity license = mispLicense();
		license.setValidToDate(LocalDateTime.now().plusDays(-10));
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy","","",false);
	}	
	
	@Test(expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S4() {
		MISPLicenseEntity license = mispLicense();
		String apiKey ="2345";	
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(false);
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"",false);
	}	
	
	@Test(expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S5() {
		MISPLicenseEntity license = mispLicense();
		String apiKey ="2345";	
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(-10)));
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"",false);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S6() {
		MISPLicenseEntity license = mispLicense();
		String apiKey ="2345";	
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);		
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.empty());
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"",false);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S7() {
		MISPLicenseEntity license = mispLicense();
		String apiKey ="2345";	
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setIsActive(false);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"",false);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S8() {
		MISPLicenseEntity license = mispLicense();
		String apiKey ="2345";	
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setIsActive(true);
		authPolicy.setPolicyGroup(null);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"",false);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S9() {
		MISPLicenseEntity license = mispLicense();
		String apiKey ="2345";	
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setIsActive(true);
		authPolicy.getPolicyGroup().setIsActive(false);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"",false);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S10() {
		MISPLicenseEntity license = mispLicense();
		String apiKey ="2345";	
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setIsActive(true);
		authPolicy.getPolicyGroup().setIsActive(true);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"",false);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S11() {
		MISPLicenseEntity license = mispLicense();
		String apiKey ="2345";	
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setPolicyFileId("{\"authPolicies\":[{\"authType\":\"otp\\\",\"authSubType\":null,\"mandatory\":true},{\"authType\":\"demo\",\"authSubType\":null,\"mandatory\":false},{\"authType\":\"bio\",\"authSubType\":\"FINGER\",\"mandatory\":true},{\"authType\":\"bio\",\"authSubType\":\"IRIS\",\"mandatory\":false},{\"authType\":\"bio\",\"authSubType\":\"FACE\",\"mandatory\":false},{\"authType\":\"kyc\",\"authSubType\":null,\"mandatory\":false}],\"allowedKycAttributes\":[{\"attributeName\":\"fullName\",\"required\":true},{\"attributeName\":\"dateOfBirth\",\"required\":true},{\"attributeName\":\"gender\",\"required\":true},{\"attributeName\":\"phone\",\"required\":true},{\"attributeName\":\"email\",\"required\":true},{\"attributeName\":\"addressLine1\",\"required\":true},{\"attributeName\":\"addressLine2\",\"required\":true},{\"attributeName\":\"addressLine3\",\"required\":true},{\"attributeName\":\"location1\",\"required\":true},{\"attributeName\":\"location2\",\"required\":true},{\"attributeName\":\"location3\",\"required\":true},{\"attributeName\":\"postalCode\",\"required\":false},{\"attributeName\":\"photo\",\"required\":true}]}");
		authPolicy.setIsActive(true);
		authPolicy.getPolicyGroup().setIsActive(true);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"",false);
	}
	
	@Test(expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S12() {
		MISPLicenseEntity license = mispLicense();
		String apiKey ="2345";	
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setPolicyFileId("{\"authPolicies\":[{\"authType\":\"otp\\\",\"authSubType\":null,\"mandatory\":true},{\"authType\":\"demo\",\"authSubType\":null,\"mandatory\":false},{\"authType\":\"bio\",\"authSubType\":\"FINGER\",\"mandatory\":true},{\"authType\":\"bio\",\"authSubType\":\"IRIS\",\"mandatory\":false},{\"authType\":\"bio\",\"authSubType\":\"FACE\",\"mandatory\":false},{\"authType\":\"kyc\",\"authSubType\":null,\"mandatory\":false}],\"allowedKycAttributes\":[{\"attributeName\":\"fullName\",\"required\":true},{\"attributeName\":\"dateOfBirth\",\"required\":true},{\"attributeName\":\"gender\",\"required\":true},{\"attributeName\":\"phone\",\"required\":true},{\"attributeName\":\"email\",\"required\":true},{\"attributeName\":\"addressLine1\",\"required\":true},{\"attributeName\":\"addressLine2\",\"required\":true},{\"attributeName\":\"addressLine3\",\"required\":true},{\"attributeName\":\"location1\",\"required\":true},{\"attributeName\":\"location2\",\"required\":true},{\"attributeName\":\"location3\",\"required\":true},{\"attributeName\":\"postalCode\",\"required\":false},{\"attributeName\":\"photo\",\"required\":true}]}");
		authPolicy.setIsActive(true);
		authPolicy.getPolicyGroup().setIsActive(true);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Optional<MISPEntity> misp = Optional.of(misp(false));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"",false);
	}
	
	
	@Test
	public void getPartnerMappedPolicyFileTest_S13() {
		MISPLicenseEntity license = mispLicense();
		license.setIsActive(true);
		license.setValidToDate(LocalDateTime.now().plusYears(1));
		String apiKey ="2345";	
		String partnerid = "123";
		Partner partner = new Partner();
		partner.setId("123");
		partner.setIsActive(true);
		partner.setCertificateAlias("Authorization");
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setPolicyApiKey(apiKey);
		policy.setPartner(partner);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setIsActive(true);
		authPolicy.getPolicyGroup().setIsActive(true);
		authPolicy.setValidToDate(LocalDateTime.now().plusDays(10));
		Optional<Partner> opt_partner = Optional.of(partner);
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndApikey(Mockito.anyString(), Mockito.anyString())).thenReturn(policy);
		Mockito.when(partnerRepository.findById(partnerid)).thenReturn(opt_partner);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"123",false);
	}
	
	@Test (expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_PartnerNotActive_S14() {
		MISPLicenseEntity license = mispLicense();
		String apiKey ="2345";	
		String partnerid = "123";
		Partner partner = new Partner();
		partner.setId("123");
		partner.setIsActive(false);
		partner.setCertificateAlias("Authorization");
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setPolicyApiKey(apiKey);
		policy.setPartner(partner);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setIsActive(true);
		authPolicy.getPolicyGroup().setIsActive(true);
		authPolicy.setValidToDate(LocalDateTime.now().plusDays(10));
		Optional<Partner> opt_partner = Optional.of(partner);
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		Mockito.when(partnerRepository.findById(partnerid)).thenReturn(opt_partner);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"123",false);
	}
	
	@Test (expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_PartnerCertNotExist_S15() {
		MISPLicenseEntity license = mispLicense();
		String apiKey ="2345";	
		String partnerid = "123";
		Partner partner = new Partner();
		partner.setId("123");
		partner.setIsActive(true);
		partner.setCertificateAlias(null);
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setPolicyApiKey(apiKey);
		policy.setPartner(partner);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setIsActive(true);
		authPolicy.getPolicyGroup().setIsActive(true);
		authPolicy.setValidToDate(LocalDateTime.now().plusDays(10));
		Optional<Partner> opt_partner = Optional.of(partner);
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		Mockito.when(partnerRepository.findById(partnerid)).thenReturn(opt_partner);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"123",true);
	}
	
	@Test (expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_PartnerNotMappedToPolicy_S16() {
		MISPLicenseEntity license = mispLicense();
		String apiKey ="2345";	
		String partnerid = "123";
		Partner partner = new Partner();
		partner.setId("123");
		partner.setIsActive(true);
		partner.setCertificateAlias("Authorization");
		PartnerPolicy policy = new PartnerPolicy();
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setIsActive(true);
		authPolicy.getPolicyGroup().setIsActive(true);
		authPolicy.setValidToDate(LocalDateTime.now().plusDays(10));
		Optional<Partner> opt_partner = Optional.of(partner);
		Optional<MISPEntity> misp = Optional.of(misp(true));
		
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Mockito.when(partnerRepository.findById(partnerid)).thenReturn(opt_partner);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"123",false);
	}
	
	@Test (expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_PartnerNotMappedToPolicy_S17() {
		MISPLicenseEntity license = mispLicense();
		String apiKey ="2345";	
		String partnerid = "123";
		Partner partner = new Partner();
		partner.setId("123");
		partner.setIsActive(true);
		partner.setCertificateAlias("Authorization");
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setPolicyApiKey(apiKey);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setIsActive(true);
		authPolicy.getPolicyGroup().setIsActive(true);
		authPolicy.setValidToDate(LocalDateTime.now().plusDays(10));
		Optional<Partner> opt_partner = Optional.of(partner);
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		Mockito.when(partnerRepository.findById(partnerid)).thenReturn(opt_partner);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"123",false);
	}
	
	@Test (expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_PartnerNotMappedToPolicy_S18() {
		MISPLicenseEntity license = mispLicense();
		String apiKey ="2345";	
		String partnerid = "123";
		Partner partner = new Partner();
		partner.setId("123");
		partner.setIsActive(true);
		partner.setCertificateAlias("Authorization");
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(false);
		policy.setPolicyApiKey(apiKey);
		policy.setPartner(partner);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setIsActive(true);
		authPolicy.getPolicyGroup().setIsActive(true);
		authPolicy.setValidToDate(LocalDateTime.now().plusDays(10));
		Optional<Partner> opt_partner = Optional.of(partner);
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		Mockito.when(partnerRepository.findById(partnerid)).thenReturn(opt_partner);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"123",false);
	}
	
	@Test (expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_PartnerPolicyExpired_S19() {
		MISPLicenseEntity license = mispLicense();
		String apiKey ="2345";	
		String partnerid = "123";
		Partner partner = new Partner();
		partner.setId("123");
		partner.setIsActive(true);
		partner.setCertificateAlias("Authorization");
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setPolicyApiKey(apiKey);
		policy.setPartner(partner);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(-10)));
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setIsActive(true);
		authPolicy.getPolicyGroup().setIsActive(true);
		authPolicy.setValidToDate(LocalDateTime.now().plusDays(10));
		Optional<Partner> opt_partner = Optional.of(partner);
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		Mockito.when(partnerRepository.findById(partnerid)).thenReturn(opt_partner);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"123",false);
	}
	
	@Test (expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_AuthPolicyNotExist_S20() {
		MISPLicenseEntity license = mispLicense();
		String apiKey ="2345";	
		String partnerid = "123";
		Partner partner = new Partner();
		partner.setId("123");
		partner.setIsActive(true);
		partner.setCertificateAlias("Authorization");
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setPolicyApiKey(apiKey);
		policy.setPartner(partner);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setIsActive(true);
		authPolicy.getPolicyGroup().setIsActive(true);
		authPolicy.setValidToDate(LocalDateTime.now().plusDays(10));
		Optional<Partner> opt_partner = Optional.of(partner);
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		Mockito.when(partnerRepository.findById(partnerid)).thenReturn(opt_partner);
		//Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"123",false);
	}
	
	@Test (expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_PartnerPolicyNotActive_S21() {
		MISPLicenseEntity license = mispLicense();
		String apiKey ="2345";	
		String partnerid = "123";
		Partner partner = new Partner();
		partner.setId("123");
		partner.setIsActive(true);
		partner.setCertificateAlias("Authorization");
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setPolicyApiKey(apiKey);
		policy.setPartner(partner);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setIsActive(false);
		authPolicy.getPolicyGroup().setIsActive(true);
		authPolicy.setValidToDate(LocalDateTime.now().plusDays(10));
		Optional<Partner> opt_partner = Optional.of(partner);
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		Mockito.when(partnerRepository.findById(partnerid)).thenReturn(opt_partner);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"123",false);
	}
	
	@Test (expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_PolicyGroupNotExist_S22() {
		MISPLicenseEntity license = mispLicense();
		String apiKey ="2345";	
		String partnerid = "123";
		Partner partner = new Partner();
		partner.setId("123");
		partner.setIsActive(true);
		partner.setCertificateAlias("Authorization");
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setPolicyApiKey(apiKey);
		policy.setPartner(partner);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setIsActive(true);
		authPolicy.setPolicyGroup(null);
		authPolicy.setValidToDate(LocalDateTime.now().plusDays(10));
		Optional<Partner> opt_partner = Optional.of(partner);
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		Mockito.when(partnerRepository.findById(partnerid)).thenReturn(opt_partner);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"123",false);
	}
	
	@Test (expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_PolicyGroupNotActive_S23() {
		MISPLicenseEntity license = mispLicense();
		String apiKey ="2345";	
		String partnerid = "123";
		Partner partner = new Partner();
		partner.setId("123");
		partner.setIsActive(true);
		partner.setCertificateAlias("Authorization");
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setPolicyApiKey(apiKey);
		policy.setPartner(partner);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setIsActive(true);
		authPolicy.getPolicyGroup().setIsActive(false);
		authPolicy.setValidToDate(LocalDateTime.now().minusYears(1));
		Optional<Partner> opt_partner = Optional.of(partner);
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		Mockito.when(partnerRepository.findById(partnerid)).thenReturn(opt_partner);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"123",false);
	}
	
	@Test (expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_PartnerPolicyExpired_S24() {
		MISPLicenseEntity license = mispLicense();
		String apiKey ="2345";	
		String partnerid = "123";
		Partner partner = new Partner();
		partner.setId("123");
		partner.setIsActive(true);
		partner.setCertificateAlias("Authorization");
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setPolicyApiKey(apiKey);
		policy.setPartner(partner);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setIsActive(true);
		authPolicy.getPolicyGroup().setIsActive(true);
		authPolicy.setValidToDate(LocalDateTime.now().minusYears(1));
		Optional<Partner> opt_partner = Optional.of(partner);
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		Mockito.when(partnerRepository.findById(partnerid)).thenReturn(opt_partner);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"123",false);
	}
	
	@Test (expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S25() {
		MISPLicenseEntity license = mispLicense();
		license.setIsActive(false);
		String apiKey ="2345";	
		String partnerid = "123";
		Partner partner = new Partner();
		partner.setId("123");
		partner.setIsActive(true);
		partner.setCertificateAlias("Authorization");
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setPolicyApiKey(apiKey);
		policy.setPartner(partner);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setIsActive(true);
		authPolicy.getPolicyGroup().setIsActive(true);
		authPolicy.setValidToDate(LocalDateTime.now().plusDays(10));
		Optional<Partner> opt_partner = Optional.of(partner);
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		Mockito.when(partnerRepository.findById(partnerid)).thenReturn(opt_partner);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"123",false);
	}
	
	@Test (expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S26() {
		MISPLicenseEntity license = mispLicense();
		license.setIsActive(false);
		String apiKey ="2345";	
		String partnerid = "123";
		Partner partner = new Partner();
		partner.setId("123");
		partner.setIsActive(true);
		partner.setCertificateAlias("Authorization");
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(false);
		policy.setPolicyApiKey(apiKey);
		policy.setPartner(partner);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setIsActive(true);
		authPolicy.getPolicyGroup().setIsActive(true);
		authPolicy.setValidToDate(LocalDateTime.now().plusDays(10));
		Optional<Partner> opt_partner = Optional.of(partner);
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndApikey(Mockito.anyString(), Mockito.anyString())).thenReturn(policy);
		Mockito.when(partnerRepository.findById(partnerid)).thenReturn(opt_partner);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"123",false);
	}
	
	@Test (expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S27() {
		MISPLicenseEntity license = mispLicense();
		license.setIsActive(false);
		String apiKey ="2345";	
		String partnerid = "123";
		Partner partner = new Partner();
		partner.setId("123");
		partner.setIsActive(true);
		partner.setCertificateAlias("Authorization");
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setPolicyApiKey(apiKey);
		policy.setPartner(partner);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(-10)));
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setIsActive(true);
		authPolicy.getPolicyGroup().setIsActive(true);
		authPolicy.setValidToDate(LocalDateTime.now().plusDays(10));
		Optional<Partner> opt_partner = Optional.of(partner);
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndApikey(Mockito.anyString(), Mockito.anyString())).thenReturn(policy);
		Mockito.when(partnerRepository.findById(partnerid)).thenReturn(opt_partner);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"123",false);
	}
	
	@Test (expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S28() {
		MISPLicenseEntity license = mispLicense();
		license.setIsActive(false);
		String apiKey ="2345";	
		String partnerid = "123";
		Partner partner = new Partner();
		partner.setId("123");
		partner.setIsActive(true);
		partner.setCertificateAlias("Authorization");
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setPolicyApiKey(apiKey);
		policy.setPartner(partner);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setIsActive(true);
		authPolicy.getPolicyGroup().setIsActive(true);
		authPolicy.setValidToDate(LocalDateTime.now().plusDays(10));
		Optional<Partner> opt_partner = Optional.of(partner);
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndApikey(Mockito.anyString(), Mockito.anyString())).thenReturn(policy);
		Mockito.when(partnerRepository.findById(partnerid)).thenReturn(opt_partner);
		//Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"123",false);
	}
	
	@Test (expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S29() {
		MISPLicenseEntity license = mispLicense();
		license.setIsActive(false);
		String apiKey ="2345";	
		String partnerid = "123";
		Partner partner = new Partner();
		partner.setId("123");
		partner.setIsActive(true);
		partner.setCertificateAlias("Authorization");
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setPolicyApiKey(apiKey);
		policy.setPartner(partner);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setIsActive(false);
		authPolicy.getPolicyGroup().setIsActive(true);
		authPolicy.setValidToDate(LocalDateTime.now().plusDays(10));
		Optional<Partner> opt_partner = Optional.of(partner);
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndApikey(Mockito.anyString(), Mockito.anyString())).thenReturn(policy);
		Mockito.when(partnerRepository.findById(partnerid)).thenReturn(opt_partner);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"123",false);
	}
	
	@Test (expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S30() {
		MISPLicenseEntity license = mispLicense();
		license.setIsActive(false);
		String apiKey ="2345";	
		String partnerid = "123";
		Partner partner = new Partner();
		partner.setId("123");
		partner.setIsActive(true);
		partner.setCertificateAlias("Authorization");
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setPolicyApiKey(apiKey);
		policy.setPartner(partner);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setIsActive(true);
		authPolicy.setPolicyGroup(null);
		authPolicy.setValidToDate(LocalDateTime.now().plusDays(10));
		Optional<Partner> opt_partner = Optional.of(partner);
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndApikey(Mockito.anyString(), Mockito.anyString())).thenReturn(policy);
		Mockito.when(partnerRepository.findById(partnerid)).thenReturn(opt_partner);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"123",false);
	}

	@Test (expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S31() {
		MISPLicenseEntity license = mispLicense();
		license.setIsActive(false);
		String apiKey ="2345";	
		String partnerid = "123";
		Partner partner = new Partner();
		partner.setId("123");
		partner.setIsActive(true);
		partner.setCertificateAlias("Authorization");
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setPolicyApiKey(apiKey);
		policy.setPartner(partner);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setIsActive(true);
		authPolicy.getPolicyGroup().setIsActive(false);
		authPolicy.setValidToDate(LocalDateTime.now().plusDays(10));
		Optional<Partner> opt_partner = Optional.of(partner);
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndApikey(Mockito.anyString(), Mockito.anyString())).thenReturn(policy);
		Mockito.when(partnerRepository.findById(partnerid)).thenReturn(opt_partner);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"123",false);
	}
	
	@Test (expected = PartnerManagerServiceException.class)
	public void getPartnerMappedPolicyFileTest_S32() {
		MISPLicenseEntity license = mispLicense();
		license.setIsActive(false);
		String apiKey ="2345";	
		String partnerid = "123";
		Partner partner = new Partner();
		partner.setId("123");
		partner.setIsActive(true);
		partner.setCertificateAlias("Authorization");
		Optional<PartnerPolicy> partnerPolicy = Optional.of(getPartnerPolicy());
		PartnerPolicy policy = partnerPolicy.get();
		policy.setIsActive(true);
		policy.setPolicyApiKey(apiKey);
		policy.setPartner(partner);
		policy.setValidToDatetime(Timestamp.valueOf(LocalDateTime.now().plusDays(10)));
		AuthPolicy authPolicy = getAuthPolicies().get(0);
		authPolicy.setIsActive(true);
		authPolicy.getPolicyGroup().setIsActive(true);
		authPolicy.setValidToDate(LocalDateTime.now().plusDays(-10));
		Optional<Partner> opt_partner = Optional.of(partner);
		Optional<MISPEntity> misp = Optional.of(misp(true));
		Mockito.when(misplKeyRepository.findByLicensekey(license.getMispLicenseUniqueKey().getLicense_key())).thenReturn(license);
		Mockito.when(partnerPolicyRepository.findByApiKey(apiKey)).thenReturn(policy);
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndApikey(Mockito.anyString(), Mockito.anyString())).thenReturn(policy);
		Mockito.when(partnerRepository.findById(partnerid)).thenReturn(opt_partner);
		Mockito.when(authPolicyRepository.findById(policy.getPolicyId())).thenReturn(Optional.of(authPolicy));
		Mockito.when(mispRepository.findById(Mockito.any())).thenReturn(misp);
		partnerManagementImpl.getPartnerMappedPolicyFile("aaaaaaabghjiuytdsdfghjiuytfdcvbhjy",apiKey,"123",false);
	}

	
	@Test(expected = PartnerManagerServiceException.class)
	public void partnerApiKeyPolicyMappingsTest_S1() {
		PartnersPolicyMappingRequest request = createRequest();
		partnerManagementImpl.updatePolicyAgainstApikey(request,"1234","543");
	}
	
	private MISPEntity misp(boolean isActive) {
		MISPEntity dto = new MISPEntity();
		dto.setAddress("Bangalore");
		dto.setContactNumber("1234567890");
		dto.setEmailId("airtel@gmail.com");
		dto.setName("Airtel");
		dto.setID("100");;
		dto.setStatus_code("approved");
		dto.setIsActive(isActive);
		return dto;
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
		policy.setPolicyFileId("{\"authPolicies\":[{\"authType\":\"otp\",\"authSubType\":null,\"mandatory\":true},{\"authType\":\"demo\",\"authSubType\":null,\"mandatory\":false},{\"authType\":\"bio\",\"authSubType\":\"FINGER\",\"mandatory\":true},{\"authType\":\"bio\",\"authSubType\":\"IRIS\",\"mandatory\":false},{\"authType\":\"bio\",\"authSubType\":\"FACE\",\"mandatory\":false},{\"authType\":\"kyc\",\"authSubType\":null,\"mandatory\":false}],\"allowedKycAttributes\":[{\"attributeName\":\"fullName\",\"required\":true},{\"attributeName\":\"dateOfBirth\",\"required\":true},{\"attributeName\":\"gender\",\"required\":true},{\"attributeName\":\"phone\",\"required\":true},{\"attributeName\":\"email\",\"required\":true},{\"attributeName\":\"addressLine1\",\"required\":true},{\"attributeName\":\"addressLine2\",\"required\":true},{\"attributeName\":\"addressLine3\",\"required\":true},{\"attributeName\":\"location1\",\"required\":true},{\"attributeName\":\"location2\",\"required\":true},{\"attributeName\":\"location3\",\"required\":true},{\"attributeName\":\"postalCode\",\"required\":false},{\"attributeName\":\"photo\",\"required\":true}]}");
		policies.add(policy);
		return policies;
		
	}
	
	private PolicyGroup policyGroupData() {
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("12345");
		policyGroup.setName("Test");
		return policyGroup;
	}
	private PartnerPolicy getPartnerPolicy() {
		PartnerPolicy partnerPolicy = new PartnerPolicy();
		partnerPolicy.setPolicyApiKey("567890");
		partnerPolicy.setPartner(getPartner());
		partnerPolicy.setIsActive(true);
		partnerPolicy.setPolicyId("567890");
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
