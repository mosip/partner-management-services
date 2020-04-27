package io.mosip.pmp.policy.test.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.simple.parser.ParseException;
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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.mosip.pmp.policy.dto.AllowedKycDto;
import io.mosip.pmp.policy.dto.AuthPolicyDto;
import io.mosip.pmp.policy.dto.PolicyCreateRequestDto;
import io.mosip.pmp.policy.dto.PolicyDto;
import io.mosip.pmp.policy.dto.PolicyStatusUpdateRequestDto;
import io.mosip.pmp.policy.dto.PolicyUpdateRequestDto;
import io.mosip.pmp.policy.entity.AuthPolicy;
import io.mosip.pmp.policy.entity.PolicyGroup;
import io.mosip.pmp.policy.errorMessages.PolicyManagementServiceException;
import io.mosip.pmp.policy.repository.AuthPolicyRepository;
import io.mosip.pmp.policy.repository.PolicyGroupRepository;
import io.mosip.pmp.policy.service.PolicyManagementService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PolicyServiceTest.class)
@AutoConfigureMockMvc
@EnableWebMvc
public class PolicyServiceTest {

	PolicyManagementService service;
	
	@Mock
	private AuthPolicyRepository authPolicyRepository;

	@Mock
	private PolicyGroupRepository policyGroupRepository;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		service = new PolicyManagementService();
		ReflectionTestUtils.setField(service, "authPolicyRepository", authPolicyRepository);
		ReflectionTestUtils.setField(service, "policyGroupRepository", policyGroupRepository);
	}
	
	@Test
	public void createPolicyTest() throws PolicyManagementServiceException, Exception {
		PolicyCreateRequestDto request = createPolicyGroupRequest();
		 service.createPolicyGroup(request);
	}
	
	@Test(expected = PolicyManagementServiceException.class)
	public void createPolicy_NameExistsException() throws PolicyManagementServiceException, Exception {
		PolicyCreateRequestDto request = createPolicyGroupRequest();
		Mockito.when(policyGroupRepository.findByName("Test")).thenReturn(policyGroupData());
		 service.createPolicyGroup(request);
	}
	
	@Test(expected = PolicyManagementServiceException.class)
	public void createPolicy_AuthNotDefined() throws PolicyManagementServiceException, Exception {
		PolicyCreateRequestDto request = createPolicyGroupRequest();
		request.setPolicies(null);
		Mockito.when(policyGroupRepository.findByName("Test")).thenReturn(policyGroupData());
		 service.createPolicyGroup(request);
	}
	
	@Test//(expected = PolicyManagementServiceException.class)
	public void updatePolicy_IDNotExistsTest() throws Exception {
		Optional<PolicyGroup> policy = Optional.of(new PolicyGroup());
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policy);
		PolicyUpdateRequestDto request = createPolicyUpdateRequestDto();
		service.update(request);
	}
	
	@Test(expected = PolicyManagementServiceException.class)
	public void updatePolicy_Test() throws Exception {
		PolicyGroup policy = policyGroupData();
		policy.setName("Policy");
		policy.setId("2345");
		Mockito.when(policyGroupRepository.findByName("Policy")).thenReturn(policy);
		PolicyUpdateRequestDto request = createPolicyUpdateRequestDto();
		service.update(request);
	}
	
	@Test(expected = PolicyManagementServiceException.class)
	public void updatePolicyStatus_PolicyIDNotExistsTest() {
		PolicyStatusUpdateRequestDto request = createUpdatePolicyStatusRequest();
		service.updatePolicyStatus(request);
		
	}
	
	@Test//(expected = PolicyManagementServiceException.class)
	public void updatePolicyStatusTest() {
		PolicyStatusUpdateRequestDto request = createUpdatePolicyStatusRequest();
		Optional<PolicyGroup> policy = Optional.of(new PolicyGroup());
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policy);
		service.updatePolicyStatus(request);
		
	}
	
	@Test
	public void updatePolicyStatus_AuthPoliccy_Test() {
		PolicyStatusUpdateRequestDto request = createUpdatePolicyStatusRequest();
		Optional<PolicyGroup> policy = Optional.of(new PolicyGroup());
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policy);
		Mockito.when(authPolicyRepository.findByPolicyId("12345")).thenReturn(getAuthPolicies());
		service.updatePolicyStatus(request);
		
	}

	@Test(expected = PolicyManagementServiceException.class)
	public void findPolicyTest_PolicyId_NotExists() throws FileNotFoundException, IOException, ParseException {
		service.findPolicy("12345");
	}
	
	@Test//(expected = PolicyManagementServiceException.class)
	public void findPolicyTest () throws FileNotFoundException, IOException, ParseException {
		Optional<PolicyGroup> policy = Optional.of(new PolicyGroup());
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policy);
		Mockito.when(authPolicyRepository.findByPolicyId("12345")).thenReturn(getAuthPolicies());
		service.findPolicy("12345");
	}
	
	@Test
	public void findAllPolicies() throws JsonParseException, JsonMappingException, IOException {
		Optional<PolicyGroup> policy = Optional.of(new PolicyGroup());
		List<PolicyGroup> policyGroups = new ArrayList<PolicyGroup>();
		policyGroups.add(policy.get());
		Mockito.when(policyGroupRepository.findAll()).thenReturn(policyGroups);
		service.findAllPolicies();
	}
	
	@Test(expected = PolicyManagementServiceException.class)
	public void findAllPolicies_NoRecords() throws JsonParseException, JsonMappingException, IOException {
		List<PolicyGroup> policyGroups = new ArrayList<PolicyGroup>();
		//policyGroups.add(policy.get());
		Mockito.when(authPolicyRepository.findByPolicyId("12345")).thenReturn(getAuthPolicies());
		Mockito.when(policyGroupRepository.findAll()).thenReturn(policyGroups);
		service.findAllPolicies();
	}
	
	@Test
	public void findAllPoliciesTest() throws JsonParseException, JsonMappingException, IOException {
		List<PolicyGroup> policyGroups = new ArrayList<PolicyGroup>();
		Optional<PolicyGroup> policy = Optional.of(new PolicyGroup());
		policy.get().setId("12345");
		policyGroups.add(policy.get());
		Mockito.when(authPolicyRepository.findByPolicyId("12345")).thenReturn(getAuthPolicies());
		Mockito.when(policyGroupRepository.findAll()).thenReturn(policyGroups);
		service.findAllPolicies();
	}
	
	private List<AuthPolicy> getAuthPolicies(){
		AuthPolicy policy = new AuthPolicy();
		List<AuthPolicy> policies = new ArrayList<AuthPolicy>();
		policy.setName("Test");
		policy.setDescr("Policy Desc");
		policy.setIsActive(true);
		policy.setPolicyFileId("{\"authPolicies\":[{\"authType\":\"otp\",\"authSubType\":null,\"mandatory\":true},{\"authType\":\"demo\",\"authSubType\":null,\"mandatory\":false},{\"authType\":\"bio\",\"authSubType\":\"FINGER\",\"mandatory\":true},{\"authType\":\"bio\",\"authSubType\":\"IRIS\",\"mandatory\":false},{\"authType\":\"bio\",\"authSubType\":\"FACE\",\"mandatory\":false},{\"authType\":\"kyc\",\"authSubType\":null,\"mandatory\":false}],\"allowedKycAttributes\":[{\"attributeName\":\"fullName\",\"required\":true},{\"attributeName\":\"dateOfBirth\",\"required\":true},{\"attributeName\":\"gender\",\"required\":true},{\"attributeName\":\"phone\",\"required\":true},{\"attributeName\":\"email\",\"required\":true},{\"attributeName\":\"addressLine1\",\"required\":true},{\"attributeName\":\"addressLine2\",\"required\":true},{\"attributeName\":\"addressLine3\",\"required\":true},{\"attributeName\":\"location1\",\"required\":true},{\"attributeName\":\"location2\",\"required\":true},{\"attributeName\":\"location3\",\"required\":true},{\"attributeName\":\"postalCode\",\"required\":false},{\"attributeName\":\"photo\",\"required\":true}]}");
		policies.add(policy);
		return policies;
		
	}
	private PolicyStatusUpdateRequestDto createUpdatePolicyStatusRequest() {
		PolicyStatusUpdateRequestDto req = new PolicyStatusUpdateRequestDto();
		req.setId("12345");
		req.setStatus("Active");
		return req;
	}

	private PolicyUpdateRequestDto createPolicyUpdateRequestDto() {
		PolicyUpdateRequestDto request = new PolicyUpdateRequestDto();
		request.setDesc("Policy Test");
		request.setName("Policy");
		request.setId("12345");
		request.setPolicies(createAuthPolicyInput());
		return request;
	}

	private PolicyCreateRequestDto createPolicyGroupRequest() {
		PolicyCreateRequestDto request = new PolicyCreateRequestDto();
		request.setDesc("Test Policy");
		request.setName("Test");
		request.setPolicies(createAuthPolicyInput());
		return request;
	}
	
	private PolicyGroup policyGroupData() {
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("12345");
		policyGroup.setName("Test");
		return policyGroup;
	}
	
	private PolicyDto createAuthPolicyInput() {
		PolicyDto policy = new PolicyDto();
		policy.setAllowedKycAttributes(getAllowedKycAttributes());
		policy.setAuthPolicies(getAuthPoliciesDto());
		return policy;
	}

	private List<AuthPolicyDto> getAuthPoliciesDto() {
		List<AuthPolicyDto> authPolicies = new ArrayList<AuthPolicyDto>();
		AuthPolicyDto dto = new AuthPolicyDto();		
		dto.setAuthSubType("otp");
		dto.setAuthSubType("none");
		dto.setMandatory(true);
		authPolicies.add(dto);		
		return authPolicies;
	}

	private List<AllowedKycDto> getAllowedKycAttributes() {
		List<AllowedKycDto> allowedKycList = new ArrayList<AllowedKycDto>();
		AllowedKycDto dto =  new AllowedKycDto();
		dto.setAttributeName("Name");
		dto.setRequired(true);
		allowedKycList.add(dto);
		return allowedKycList;
	}
}
