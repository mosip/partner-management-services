/*package io.mosip.pmp.partnermanagement.test.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import io.mosip.pmp.partnermanagement.PartnermanagementApplication;
import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingRequest;
import io.mosip.pmp.partnermanagement.entity.AuthPolicy;
import io.mosip.pmp.partnermanagement.entity.Partner;
import io.mosip.pmp.partnermanagement.entity.PartnerPolicy;
import io.mosip.pmp.partnermanagement.entity.PolicyGroup;
import io.mosip.pmp.partnermanagement.exception.PartnerDoesNotExistException;
import io.mosip.pmp.partnermanagement.repository.AuthPolicyRepository;
import io.mosip.pmp.partnermanagement.repository.PartnerPolicyRepository;
import io.mosip.pmp.partnermanagement.repository.PartnerPolicyRequestRepository;
import io.mosip.pmp.partnermanagement.repository.PartnerRepository;
import io.mosip.pmp.partnermanagement.repository.PolicyGroupRepository;
import io.mosip.pmp.partnermanagement.service.impl.PartnerManagementServiceImpl;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = { PartnermanagementApplication.class })
@AutoConfigureMockMvc
@EnableWebMvc
public class PartnerManagementServiceImplTest {
	
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
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		partnerManagementImpl = new PartnerManagementServiceImpl();
		ReflectionTestUtils.setField(partnerManagementImpl, "policyGroupRepository", policyGroupRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "partnerRepository", partnerRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "authPolicyRepository", authPolicyRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "partnerPolicyRequestRepository", partnerPolicyRequestRepository);
		ReflectionTestUtils.setField(partnerManagementImpl, "partnerPolicyRepository", partnerPolicyRepository);
	}
	
	@Test(expected = PartnerDoesNotExistException.class)
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
		partnerManagementImpl.partnerApiKeyPolicyMappings(partnersPolicyMappingRequest, partnerID, PolicyAPIKey);
	}
}
*/