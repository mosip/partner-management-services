package io.mosip.pms.policy.test.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.mosip.pms.common.entity.AuthPolicy;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.entity.PartnerPolicy;
import io.mosip.pms.common.entity.PolicyGroup;
import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.repository.AuthPolicyHRepository;
import io.mosip.pms.common.repository.AuthPolicyRepository;
import io.mosip.pms.common.repository.PartnerPolicyRepository;
import io.mosip.pms.common.repository.PolicyGroupRepository;
import io.mosip.pms.policy.dto.PolicyAttributesDto;
import io.mosip.pms.policy.dto.PolicyCreateRequestDto;
import io.mosip.pms.policy.dto.PolicyGroupCreateRequestDto;
import io.mosip.pms.policy.dto.PolicyGroupUpdateRequestDto;
import io.mosip.pms.policy.dto.PolicyStatusUpdateRequestDto;
import io.mosip.pms.policy.dto.PolicyUpdateRequestDto;
import io.mosip.pms.policy.errorMessages.PolicyManagementServiceException;
import io.mosip.pms.policy.service.PolicyManagementService;
import io.mosip.pms.policy.util.AuditUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PolicyServiceTest {

	@Autowired
	PolicyManagementService service;
	
	@Mock
	private AuthPolicyRepository authPolicyRepository;

	@Mock
	private PolicyGroupRepository policyGroupRepository;
	
	@Mock
	private AuthPolicyHRepository authPolicyHRepository;
	
	@Mock
	PartnerPolicyRepository partnerPolicyRepository;	
	
	@Mock
	private WebSubPublisher webSubPublisher;	

	@MockBean
	private AuditUtil audit;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(service, "authPolicyRepository", authPolicyRepository);
		ReflectionTestUtils.setField(service, "policyGroupRepository", policyGroupRepository);
		ReflectionTestUtils.setField(service, "authPolicyHRepository", authPolicyHRepository);
		ReflectionTestUtils.setField(service, "partnerPolicyRepository", partnerPolicyRepository);
		ReflectionTestUtils.setField(service, "webSubPublisher", webSubPublisher);		
		Mockito.doNothing().when(webSubPublisher).notify(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.doNothing().when(audit).setAuditRequestDto(Mockito.any());
	}
	
	//Success Test
	@Test
	public void createPolicyGroupTest_S001() {
		Mockito.when(policyGroupRepository.findByName("Test")).thenReturn(policyGroupData());
		service.createPolicyGroup(createPolicyGroupRequest());
	}
	
	@Test(expected = PolicyManagementServiceException.class)
	public void createPolicyGroupTest_S002() {		
		Mockito.when(policyGroupRepository.findByName("Test_Policy_Group_001")).thenReturn(policyGroupData());
		service.createPolicyGroup(createPolicyGroupRequest());
	}
	
	//success
	@Test
	public void updatePolicyGroupTest_S001() {
		Optional<PolicyGroup> policyGroupFromDb = Optional.of(policyGroupData());
		Mockito.when(policyGroupRepository.findById("1234")).thenReturn(policyGroupFromDb);
		service.updatePolicyGroup(updatePolicygroupRequest(),"1234");
	}
	
	@Test(expected = PolicyManagementServiceException.class)
	public void updatePolicyGroupTest_S002() {
		Optional<PolicyGroup> policyGroupFromDb = Optional.of(policyGroupData());
		Mockito.when(policyGroupRepository.findById("12")).thenReturn(policyGroupFromDb);
		service.updatePolicyGroup(updatePolicygroupRequest(),"1234");
	}
	
	@Test(expected = PolicyManagementServiceException.class)
	public void updatePolicyGroupTest_S003() {
		Optional<PolicyGroup> policyGroupFromDb = Optional.of(policyGroupData());
		Mockito.when(policyGroupRepository.findById("1234")).thenReturn(policyGroupFromDb);
		Mockito.when(policyGroupRepository.findByName("Test_Policy_Group_001")).thenReturn(policyGroupData());
		service.updatePolicyGroup(updatePolicygroupRequest(),"1234");
	}
	
	@Test
	public void updatePolicyGroupTest_S004() {
		PolicyGroupUpdateRequestDto updateRequest = updatePolicygroupRequest();
		updateRequest.setActive(false);
		Optional<PolicyGroup> policyGroupFromDb = Optional.of(policyGroupData());
		Mockito.when(policyGroupRepository.findById("1234")).thenReturn(policyGroupFromDb);
		service.updatePolicyGroup(updateRequest,"1234");
	}	
	
	
	@Test	
	@Ignore
	public void createPoliciesTest_S001() throws PolicyManagementServiceException, Exception {
		PolicyCreateRequestDto request = createPoliciesRequest();
		request.setVersion("0.10");
		Mockito.when(policyGroupRepository.findByName("Test_Policy_Group_001")).thenReturn(policyGroupData());
		Mockito.when(authPolicyRepository.findByName("Test")).thenReturn(getAuthPolicy());
		service.createPolicies(request);
	}
	

	@Test
	@Ignore
	public void createPoliciesTest_S002() throws PolicyManagementServiceException, Exception {
		PolicyCreateRequestDto request = createPoliciesRequest();
		request.setPolicyType("Auth");		
		Mockito.when(policyGroupRepository.findByName("Test_Policy_Group_001")).thenReturn(policyGroupData());
		Mockito.when(authPolicyRepository.findByName("Test")).thenReturn(null);
		service.createPolicies(request);
	}
	
	@Test(expected = PolicyManagementServiceException.class)
	@Ignore
	public void createPoliciesTest_S010() throws PolicyManagementServiceException, Exception {
		PolicyCreateRequestDto request = createPoliciesRequest();
		request.setPolicyType("Auth");		
		request.setPolicies(createWrongAuthPolicyInput());
		Mockito.when(policyGroupRepository.findByName("Test_Policy_Group_001")).thenReturn(policyGroupData());
		Mockito.when(authPolicyRepository.findByName("Test")).thenReturn(null);
		service.createPolicies(request);
	}
	
	@Test(expected = PolicyManagementServiceException.class)
	public void createPoliciesTest_S003() throws PolicyManagementServiceException, Exception {
		PolicyCreateRequestDto request = createPoliciesRequest();
		request.setPolicyType("Data_Share");
		Mockito.when(policyGroupRepository.findByName("Test_Policy_Group_001")).thenReturn(null);
		Mockito.when(authPolicyRepository.findByName("Test")).thenReturn(getAuthPolicy());
		service.createPolicies(request);
	}
	
	@Test(expected = PolicyManagementServiceException.class)
	public void createPoliciesTest_S004() throws PolicyManagementServiceException, Exception {
		PolicyCreateRequestDto request = createPoliciesRequest();
		request.setPolicyType("Data_Share");
		Mockito.when(policyGroupRepository.findByName("Test_Policy_Group_001")).thenReturn(policyGroupData());
		Mockito.when(authPolicyRepository.findByPolicyGroupAndName("12345","Test_001")).thenReturn(getAuthPolicy());
		service.createPolicies(request);
	}
	
	private PolicyCreateRequestDto createPoliciesRequest() {
		PolicyCreateRequestDto request= new PolicyCreateRequestDto();
		request.setDesc("Test_001");
		request.setName("Test_001");
		request.setPolicyGroupName("Test_Policy_Group_001");
		request.setPolicyType("Auth");
		request.setVersion("0.8");
		request.setPolicies(createAuthPolicyInput());
		return request;
	}

	private PolicyGroupUpdateRequestDto updatePolicygroupRequest() {
		PolicyGroupUpdateRequestDto updateRequest = new PolicyGroupUpdateRequestDto();
		updateRequest.setName("Test_Policy_Group_001");
		updateRequest.setDesc("Test_Policy_Group_001");
		return updateRequest;
	}
	private PolicyGroupCreateRequestDto createPolicyGroupRequest() {
		PolicyGroupCreateRequestDto request = new PolicyGroupCreateRequestDto();
		request.setDesc("Test_Policy_Group_001");
		request.setName("Test_Policy_Group_001");
		return request;
	}
	
	
	@Test(expected = PolicyManagementServiceException.class)
	public void updatePolicyStatus_PolicyGroupIDNotExistsTest() {
		PolicyStatusUpdateRequestDto request = createUpdatePolicyStatusRequest();
		service.updatePolicyStatus(request,"1234","3456");		
	}

	@Test(expected = PolicyManagementServiceException.class)
	public void updatePolicyStatus_PolicIDNotExistsTest() {
		PolicyStatusUpdateRequestDto request = createUpdatePolicyStatusRequest();
		Optional<PolicyGroup> policy = Optional.of(new PolicyGroup());
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policy);
		service.updatePolicyStatus(request,"12345","3456");		
	}
	
	@Test(expected = PolicyManagementServiceException.class)
	public void updatePolicyStatus_NotMappedTest() {
		PolicyStatusUpdateRequestDto request = createUpdatePolicyStatusRequest();
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("5678");
		Optional<PolicyGroup> policy = Optional.of(policyGroup);
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policy);
		Mockito.when(authPolicyRepository.findById("3456")).thenReturn(Optional.of(getAuthPolicy()));
		service.updatePolicyStatus(request,"12345","3456");
	}
	
	@Test
	public void updatePolicyStatus_AuthPoliccy_Test() {
		PolicyStatusUpdateRequestDto request = createUpdatePolicyStatusRequest();
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("12345");
		Optional<PolicyGroup> policy = Optional.of(policyGroup);
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policy);
		Mockito.when(authPolicyRepository.findById("3456")).thenReturn(Optional.of(getAuthPolicy()));
		service.updatePolicyStatus(request,"12345","3456");	
	}
	
	@Test(expected = PolicyManagementServiceException.class)
	public void updatePolicies_PolicyGroupNotExistsTest() throws PolicyManagementServiceException, Exception {
		PolicyUpdateRequestDto request = new PolicyUpdateRequestDto();
		request.setPolicyGroupName("Test");
		request.setDesc("Test");
		request.setVersion("0.8");
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("12345");
		policyGroup.setName("Test");
		Mockito.when(policyGroupRepository.findByName("Test_01")).thenReturn(policyGroup);
		service.updatePolicies(request, "12345");
	}

	@Test(expected = PolicyManagementServiceException.class)
	public void updatePolicies_AuthPolicyNotExistsTest() throws PolicyManagementServiceException, Exception {
		PolicyUpdateRequestDto request = new PolicyUpdateRequestDto();
		request.setPolicyGroupName("Test");
		request.setVersion("0.8");
		request.setDesc("Test");
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("12345");
		policyGroup.setName("Test");
		Mockito.when(policyGroupRepository.findByName("Test")).thenReturn(policyGroup);
		Mockito.when(authPolicyRepository.findById("3456")).thenReturn(Optional.of(getAuthPolicy()));
		Optional<PolicyGroup> policy = Optional.of(policyGroup);
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policy);	
		service.updatePolicies(request, "12345");
	}
	
	@Test(expected = PolicyManagementServiceException.class)
	public void updatePolicies_PolicyGroupNotMappingTest() throws PolicyManagementServiceException, Exception {
		PolicyUpdateRequestDto request = new PolicyUpdateRequestDto();
		request.setPolicyGroupName("Test");
		request.setDesc("Test");
		request.setName("Test");
		request.setVersion("0.8");
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("12345");
		policyGroup.setName("Test");
		Mockito.when(policyGroupRepository.findByName("Test")).thenReturn(policyGroup);
		AuthPolicy authPolicy = getAuthPolicy();
		authPolicy.getPolicyGroup().setId("8976");
		authPolicy.setId("12345");
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(authPolicy));
		Optional<PolicyGroup> policy = Optional.of(policyGroup);
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policy);		
		service.updatePolicies(request, "12345");
	}
	
	@Test
	@Ignore
	public void updatePoliciesTest() throws PolicyManagementServiceException, Exception {
		PolicyUpdateRequestDto request = new PolicyUpdateRequestDto();
		request.setPolicyGroupName("Test");
		request.setDesc("Test");
		request.setName("Test");
		request.setVersion("0.8");
		request.setPolicies(createAuthPolicyInput());
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("12345");
		policyGroup.setName("Test");
		Mockito.when(policyGroupRepository.findByName("Test")).thenReturn(policyGroup);
		AuthPolicy authPolicy = getAuthPolicy();
		authPolicy.getPolicyGroup().setId("12345");
		authPolicy.setName("Test");
		authPolicy.setPolicy_type("AUTH");
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(authPolicy));
		Optional<PolicyGroup> policy = Optional.of(policyGroup);
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policy);
		Mockito.when(authPolicyRepository.findByPolicyGroupAndName("12345", "Test")).thenReturn(getAuthPolicy());
		service.updatePolicies(request, "12345");
	}
	
	@Test(expected = PolicyManagementServiceException.class)
	@Ignore
	public void updatePoliciesTest_Exception() throws PolicyManagementServiceException, Exception {
		PolicyUpdateRequestDto request = new PolicyUpdateRequestDto();
		request.setPolicyGroupName("Test");
		request.setDesc("Test");
		request.setName("Test");
		request.setVersion("0.8");
		request.setPolicies(createWrongAuthPolicyInput());
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("12345");
		policyGroup.setName("Test");
		Mockito.when(policyGroupRepository.findByName("Test")).thenReturn(policyGroup);
		AuthPolicy authPolicy = getAuthPolicy();
		authPolicy.getPolicyGroup().setId("12345");
		authPolicy.setName("Test");
		authPolicy.setPolicy_type("AUTH");
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(authPolicy));
		Optional<PolicyGroup> policy = Optional.of(policyGroup);
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policy);
		Mockito.when(authPolicyRepository.findByPolicyGroupAndName("12345", "Test")).thenReturn(getAuthPolicy());
		service.updatePolicies(request, "12345");
	}
	
	@Test(expected = PolicyManagementServiceException.class)
	public void updatePolicies_PolicyNameDuplicateTest() throws PolicyManagementServiceException, Exception {
		PolicyUpdateRequestDto request = new PolicyUpdateRequestDto();
		request.setPolicyGroupName("Test");
		request.setDesc("Test");
		request.setName("Test");
		request.setPolicies(createAuthPolicyInput());
		request.setVersion("0.8");
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("12345");
		policyGroup.setName("Test");
		Mockito.when(policyGroupRepository.findByName("Test")).thenReturn(policyGroup);
		AuthPolicy authPolicy = getAuthPolicy();
		authPolicy.getPolicyGroup().setId("12345");
		authPolicy.setName("Test");
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(authPolicy));
		Optional<PolicyGroup> policy = Optional.of(policyGroup);
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policy);
		AuthPolicy authPolicyName = getAuthPolicy();
		authPolicyName.getPolicyGroup().setId("12345");
		authPolicyName.setName("Test_01");
		Mockito.when(authPolicyRepository.findByPolicyGroupAndName("12345", "Test")).thenReturn(authPolicyName);
		service.updatePolicies(request, "12345");
	}
	
	@Test(expected = PolicyManagementServiceException.class)
	public void publishPolicy_PolicyPublishedTest() throws JsonParseException, JsonMappingException, IOException {
		PolicyUpdateRequestDto request = new PolicyUpdateRequestDto();
		request.setPolicyGroupName("Test");
		request.setDesc("Test");
		request.setName("Test");
		//request.setPolicies(createAuthPolicyInput());
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("12345");
		policyGroup.setName("Test");
		Mockito.when(policyGroupRepository.findByName("Test")).thenReturn(policyGroup);
		AuthPolicy authPolicy = getAuthPolicy();
		authPolicy.getPolicyGroup().setId("12345");
		authPolicy.setName("Test");
		Mockito.when(authPolicyRepository.findByName("Test")).thenReturn(authPolicy);
		Optional<PolicyGroup> policy = Optional.of(policyGroup);
		Mockito.when(policyGroupRepository.findByName("Test")).thenReturn(policy.get());
		Mockito.when(authPolicyRepository.findByPolicyGroupAndName("12345", "Test")).thenReturn(getAuthPolicy());
		service.publishPolicy("Test", "Test");
	}
	
	@Test
	public void publishPolicyTest() throws JsonParseException, JsonMappingException, IOException {
		PolicyUpdateRequestDto request = new PolicyUpdateRequestDto();
		request.setPolicyGroupName("Test");
		request.setDesc("Test");
		request.setName("Test");
		//request.setPolicies(createAuthPolicyInput());
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("12345");
		policyGroup.setName("Test");
		Mockito.when(policyGroupRepository.findByName("Test")).thenReturn(policyGroup);
		AuthPolicy authPolicy = getAuthPolicy();
		authPolicy.getPolicyGroup().setId("12345");
		authPolicy.setName("Test");
		authPolicy.setIsActive(false);
		authPolicy.setPolicyGroup(policyGroup);
		Mockito.when(authPolicyRepository.findById("Test")).thenReturn(Optional.of(authPolicy));
		Optional<PolicyGroup> policy = Optional.of(policyGroup);
		Mockito.when(policyGroupRepository.findById("Test")).thenReturn(policy);
		Mockito.when(authPolicyRepository.findByPolicyGroupAndName("12345", "Test")).thenReturn(getAuthPolicy());
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policy);
		service.publishPolicy("Test", "Test");
	}
	
	@Test(expected = PolicyManagementServiceException.class)
	public void findPolicy_policyIdNotExistsTest() throws FileNotFoundException, IOException, ParseException {
		service.findPolicy("1234");
	}
	
	@Test(expected = PolicyManagementServiceException.class)
	public void findPolicy_policyGroupIdNotExistsTest() throws FileNotFoundException, IOException, ParseException {		
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(Optional.empty());
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(getAuthPolicies().get(0)));
		service.findPolicy("12345");
	}
	
	@Test
	public void findPolicyTest_001() throws FileNotFoundException, IOException, ParseException {
		Optional<PolicyGroup> policy = Optional.of(new PolicyGroup());
		policy.get().setId("12345");
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policy);
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(getAuthPolicies().get(0)));
		service.findPolicy("12345");
	}	
		
	@Test
	public void findAllPoliciesTest_001() throws FileNotFoundException, IOException, ParseException {
		service.findAllPolicies();
	}
	
	@Test
	public void findAllPoliciesTest_002() throws FileNotFoundException, IOException, ParseException {
		Optional<PolicyGroup> policy = Optional.of(new PolicyGroup());
		policy.get().setId("12345");
		Mockito.when(authPolicyRepository.findAll()).thenReturn(getAuthPolicies());
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policy);
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(getAuthPolicies().get(0)));
		service.findAllPolicies();
	}
	
	@Test(expected = PolicyManagementServiceException.class)
	public void getPartnerMappedPolicy_NoPolicyTest() throws JsonParseException, JsonMappingException, IOException {
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndPolicyIdAndIsActiveTrue("2345","12345")).thenReturn(null);		
		service.getPartnerMappedPolicy("12345", "12345");
	}
	
	@Test(expected = PolicyManagementServiceException.class)
	public void getPartnerMappedPolicy_NoAuthPolicyTest() throws JsonParseException, JsonMappingException, IOException {
		PartnerPolicy policy = new PartnerPolicy();
		List<PartnerPolicy> partnerPolicy = new ArrayList<>();
		Partner partner = new Partner();
		partner.setAddress("Test");
		partner.setName("Test");
		policy.setPartner(partner);
		policy.getPartner().setId("12345");
		policy.setPolicyId("12345");
		policy.setPolicyApiKey("12345");
		partnerPolicy.add(policy);
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndPolicyIdAndIsActiveTrue("12345","12345")).thenReturn(partnerPolicy);		
		service.getPartnerMappedPolicy("12345", "12345");
	}
	
	@Test
	public void getPartnerMappedPolicyTest_001() throws JsonParseException, JsonMappingException, IOException {
		PartnerPolicy policy = new PartnerPolicy();
		List<PartnerPolicy> partnerPolicy = new ArrayList<>();
		Partner partner = new Partner();
		partner.setAddress("Test");
		partner.setName("Test");
		policy.setPartner(partner);
		policy.getPartner().setId("12345");
		policy.setPolicyId("12345");
		policy.setPolicyApiKey("12345");
		partnerPolicy.add(policy);
		Optional<PolicyGroup> policyGroup = Optional.of(new PolicyGroup());
		policyGroup.get().setId("12345");		
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policyGroup);
		Mockito.when(partnerPolicyRepository.findByPartnerIdAndPolicyIdAndIsActiveTrue("12345","12345")).thenReturn(partnerPolicy);
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(getAuthPolicies().get(0)));
		service.getPartnerMappedPolicy("12345", "12345");
	}
	
	@Test
	public void getPolicyGroupPolicyTest() throws JsonParseException, JsonMappingException, IOException {
		List<PolicyGroup> policyGroups = new ArrayList<PolicyGroup>();
		Optional<PolicyGroup> policy = Optional.of(new PolicyGroup());
		policy.get().setId("12345");
		policyGroups.add(policy.get());		
		Optional<PolicyGroup> policyGroup = Optional.of(new PolicyGroup());
		policyGroup.get().setId("12345");		
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policyGroup);
		Mockito.when(authPolicyRepository.findByPolicyGroupId("12345")).thenReturn(getAuthPolicies());
		service.getPolicyGroupPolicy("12345");
	}
	
	@Test
	public void getPolicyGroupTest() throws JsonParseException, JsonMappingException, IOException {
		List<PolicyGroup> policyGroups = new ArrayList<PolicyGroup>();
		Optional<PolicyGroup> policyGroup = Optional.of(new PolicyGroup());
		policyGroup.get().setId("12345");		
		policyGroups.add(policyGroup.get());
		Mockito.when(policyGroupRepository.findAll()).thenReturn(policyGroups);
		Mockito.when(authPolicyRepository.findByPolicyGroupId("12345")).thenReturn(getAuthPolicies());
		service.getPolicyGroup();
	}
	
	@Test(expected = PolicyManagementServiceException.class)
	public void findPolicyAgainstApiKey_S001() throws FileNotFoundException, IOException, ParseException {
		Mockito.when(authPolicyRepository.findByPolicyGroupId("12345")).thenReturn(getAuthPolicies());
		Mockito.when(partnerPolicyRepository.findByApiKey("2345")).thenReturn(getPartnerPolicy());
		service.getAuthPolicyWithApiKey("2345");
	}
	
	@Test
	public void findPolicyAgainstApiKey_S002() throws FileNotFoundException, IOException, ParseException {
		Mockito.when(authPolicyRepository.findById("234")).thenReturn(Optional.of(getAuthPolicy()));		
		Mockito.when(partnerPolicyRepository.findByApiKey("2345")).thenReturn(getPartnerPolicy());
		Optional<PolicyGroup> policy = Optional.of(policyGroupData());
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policy);
		service.getAuthPolicyWithApiKey("2345");
	}
	
	@Test(expected = PolicyManagementServiceException.class)
	public void findPolicyAgainstApiKey_S003() throws FileNotFoundException, IOException, ParseException {
		Mockito.when(authPolicyRepository.findById("234")).thenReturn(Optional.of(getAuthPolicy()));		
		Mockito.when(partnerPolicyRepository.findByApiKey("2345")).thenReturn(null);
		Optional<PolicyGroup> policy = Optional.of(policyGroupData());
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policy);
		service.getAuthPolicyWithApiKey("2345");
	}
	
	private PartnerPolicy getPartnerPolicy() {
		PartnerPolicy partnerPolicy = new PartnerPolicy();
		partnerPolicy.setPolicyApiKey("2345");
		partnerPolicy.setIsActive(true);
		partnerPolicy.setPolicyId("234");
		return partnerPolicy;
	}
	
	private List<AuthPolicy> getAuthPolicies(){
		AuthPolicy policy = new AuthPolicy();
		List<AuthPolicy> policies = new ArrayList<AuthPolicy>();
		policy.setName("Test");
		policy.setDescr("Policy Desc");
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("12345");
		policyGroup.setName("Test");
		policy.setPolicyGroup(policyGroup);
		policy.getPolicyGroup().setId("12345");
		policy.setId("12345");
		policy.setIsActive(true);
		policy.setPolicyFileId("{\"allowedAuthTypes\":[{\"authType\":\"otp\",\"authSubType\":null,\"mandatory\":true},{\"authType\":\"bio\",\"authSubType\":\"FINGER\",\"mandatory\":true}],\"shareableAttributes\":[{\"encrypted\":false,\"format\":null,\"attributeName\":\"fullName\"},{\"encrypted\":false,\"format\":\"yyyy\",\"attributeName\":\"dateOfBirth\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"gender\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"phone\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"email\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"addressLine1\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"addressLine2\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"addressLine3\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"location1\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"location2\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"location3\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"postalCode\"},{\"encrypted\":false,\"format\":\"extraction\",\"attributeName\":\"face\"},{\"encrypted\":false,\"format\":\"extraction\",\"attributeName\":\"finger\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"iris\"}],\"dataSharePolicies\":{\"transactionsAllowed\":\"2\",\"shareDomain\":\"mosip.io\",\"encryptionType\":\"partnerBased\",\"validForInMinutes\":\"30\",\"typeOfShare\":\"dataShare\"}}");
		policies.add(policy);
		return policies;
		
	}
	
	private AuthPolicy getAuthPolicy(){
		AuthPolicy policy = new AuthPolicy();		
		policy.setName("Test");
		policy.setDescr("Policy Desc");
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("12345");
		policyGroup.setName("Test");
		policy.setPolicyGroup(policyGroup);
		policy.getPolicyGroup().setId("12345");
		policy.setIsActive(true);
		policy.setPolicyFileId("{\"allowedAuthTypes\":[{\"authType\":\"otp\",\"authSubType\":null,\"mandatory\":true},{\"authType\":\"bio\",\"authSubType\":\"FINGER\",\"mandatory\":true}],\"shareableAttributes\":[{\"encrypted\":false,\"format\":null,\"attributeName\":\"fullName\"},{\"encrypted\":false,\"format\":\"yyyy\",\"attributeName\":\"dateOfBirth\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"gender\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"phone\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"email\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"addressLine1\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"addressLine2\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"addressLine3\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"location1\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"location2\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"location3\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"postalCode\"},{\"encrypted\":false,\"format\":\"extraction\",\"attributeName\":\"face\"},{\"encrypted\":false,\"format\":\"extraction\",\"attributeName\":\"finger\"},{\"encrypted\":false,\"format\":null,\"attributeName\":\"iris\"}],\"dataSharePolicies\":{\"transactionsAllowed\":\"2\",\"shareDomain\":\"mosip.io\",\"encryptionType\":\"partnerBased\",\"validForInMinutes\":\"30\",\"typeOfShare\":\"dataShare\"}}");		
		return policy;
		
	}
	private PolicyStatusUpdateRequestDto createUpdatePolicyStatusRequest() {
		PolicyStatusUpdateRequestDto req = new PolicyStatusUpdateRequestDto();
		req.setStatus("Active");
		return req;
	}
	
	private PolicyGroup policyGroupData() {
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("12345");
		policyGroup.setName("Policy");
		policyGroup.setIsActive(true);
		return policyGroup;
	}
	
	private JSONObject createAuthPolicyInput() {
		PolicyAttributesDto policy = new PolicyAttributesDto();		
		policy.setAuthTokenType("partner");
		String obj = "{\"authTokenType\":\"partner\",\"allowedKycAttributes\":[{\"attributeName\":\"fullName\"},{\"attributeName\":\"dateOfBirth\"}],\"allowedAuthTypes\":[{\"authType\":\"bio\",\"authSubType\":\"FINGER\",\"mandatory\":false},{\"authType\":\"bio\",\"authSubType\":\"IRIS\",\"mandatory\":false},{\"authType\":\"bio\",\"authSubType\":\"FACE\",\"mandatory\":false},{\"authType\":\"otp\",\"authSubType\":\"\",\"mandatory\":false},{\"authType\":\"otp-request\",\"authSubType\":\"\",\"mandatory\":false},{\"authType\":\"kyc\",\"authSubType\":\"\",\"mandatory\":false},{\"authType\":\"demo\",\"authSubType\":\"\",\"mandatory\":false}]}";
		JSONParser parser = new JSONParser(); 
		JSONObject json = null;
		try {
			json = (JSONObject) parser.parse(obj);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return json;
	}
	
	private JSONObject createWrongAuthPolicyInput() {
		PolicyAttributesDto policy = new PolicyAttributesDto();		
		policy.setAuthTokenType("partner");
		String obj = "{\"allowedKycAttributes\":[{\"attributeName\":\"fullName\"},{\"attributeName\":\"dateOfBirth\"}],\"allowedAuthTypes\":[{\"authType\":\"bio\",\"authSubType\":\"FINGER\",\"mandatory\":false},{\"authType\":\"bio\",\"authSubType\":\"IRIS\",\"mandatory\":false},{\"authType\":\"bio\",\"authSubType\":\"FACE\",\"mandatory\":false},{\"authType\":\"otp\",\"authSubType\":\"\",\"mandatory\":false},{\"authType\":\"otp-request\",\"authSubType\":\"\",\"mandatory\":false},{\"authType\":\"kyc\",\"authSubType\":\"\",\"mandatory\":false},{\"authType\":\"demo\",\"authSubType\":\"\",\"mandatory\":false}]}";
		JSONParser parser = new JSONParser(); 
		JSONObject json = null;
		try {
			json = (JSONObject) parser.parse(obj);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return json;
	}
}
