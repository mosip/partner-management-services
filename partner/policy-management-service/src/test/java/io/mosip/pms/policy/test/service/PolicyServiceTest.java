package io.mosip.pms.policy.test.service;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pms.common.dto.FilterData;
import io.mosip.pms.common.dto.FilterDto;
import io.mosip.pms.common.dto.FilterValueDto;
import io.mosip.pms.common.dto.Pagination;
import io.mosip.pms.common.dto.PolicyFilterValueDto;
import io.mosip.pms.common.dto.PolicySearchDto;
import io.mosip.pms.common.dto.SearchDto;
import io.mosip.pms.common.dto.SearchFilter;
import io.mosip.pms.common.dto.SearchSort;
import io.mosip.pms.common.entity.AuthPolicy;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.entity.PartnerPolicy;
import io.mosip.pms.common.entity.PolicyGroup;
import io.mosip.pms.common.helper.FilterHelper;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.common.helper.WebSubPublisher;
import io.mosip.pms.common.repository.AuthPolicyHRepository;
import io.mosip.pms.common.repository.AuthPolicyRepository;
import io.mosip.pms.common.repository.PartnerPolicyRepository;
import io.mosip.pms.common.repository.PolicyGroupRepository;
import io.mosip.pms.common.util.PageUtils;
import io.mosip.pms.common.validator.FilterColumnValidator;
import io.mosip.pms.policy.dto.PolicyAttributesDto;
import io.mosip.pms.policy.dto.PolicyCreateRequestDto;
import io.mosip.pms.policy.dto.PolicyGroupCreateRequestDto;
import io.mosip.pms.policy.dto.PolicyGroupUpdateRequestDto;
import io.mosip.pms.policy.dto.PolicyStatusUpdateRequestDto;
import io.mosip.pms.policy.dto.PolicyUpdateRequestDto;
import io.mosip.pms.policy.errorMessages.ErrorMessages;
import io.mosip.pms.policy.errorMessages.PolicyManagementServiceException;
import io.mosip.pms.policy.service.PolicyManagementService;
import io.mosip.pms.policy.util.AuditUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application.properties")
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
	
	@Mock
	FilterColumnValidator filterColumnValidator;

	@Mock
	SearchHelper searchHelper;
	
	@Mock
	FilterHelper filterHelper;
	
	@Mock
    private ObjectMapper mapper;
	
	@Mock
	PageUtils pageUtils;
	
	private String authPolicySchema = "{\r\n" + 
			"	\"$schema\": \"http://json-schema.org/draft-04/schema#\",\r\n" + 
			"	\"type\": \"object\",\r\n" + 
			"	\"properties\": {\r\n" + 
			"		\"allowedKycAttributes\":{\r\n" + 
			"			\"type\":\"array\",\r\n" + 
			"			\"additionalItems\": false,\r\n" + 
			"			\"items\":\r\n" + 
			"			{\r\n" + 
			"				\"type\":\"object\",\r\n" + 
			"				\"properties\":{\r\n" + 
			"					\"attributeName\":{\r\n" + 
			"						\"type\":\"string\"\r\n" + 
			"					}\r\n" + 
			"				},\r\n" + 
			"				\"required\":[\r\n" + 
			"					\"attributeName\"\r\n" + 
			"				],\r\n" + 
			"				\"additionalProperties\": false				\r\n" + 
			"			}\r\n" + 
			"			\r\n" + 
			"		},\r\n" + 
			"		\"allowedAuthTypes\":{\r\n" + 
			"			\"type\":\"array\",\r\n" + 
			"			\"additionalItems\": false,\r\n" + 
			"			\"items\":\r\n" + 
			"			{\r\n" + 
			"				\"type\":\"object\",\r\n" + 
			"				\"properties\":{\r\n" + 
			"					\"authType\":{\r\n" + 
			"						\"type\":\"string\"\r\n" + 
			"					},\r\n" + 
			"					\"authSubType\":{\r\n" + 
			"						\"type\":\"string\"\r\n" + 
			"					},\r\n" + 
			"					\"mandatory\":{\r\n" + 
			"						\"type\":\"boolean\"\r\n" + 
			"					}\r\n" + 
			"				},\r\n" + 
			"				\"required\":[\r\n" + 
			"					\"authType\",\r\n" + 
			"					\"mandatory\"\r\n" + 
			"				],\r\n" + 
			"				\"additionalProperties\": false\r\n" + 
			"			}\r\n" + 
			"			\r\n" + 
			"		},\r\n" + 
			"		\"authTokenType\":{\r\n" + 
			"			\"type\":\"string\",\r\n" + 
			"			\"enum\":[\"random\",\"partner\",\"policy\"]\r\n" + 
			"		},\r\n" + 
			"		\"kycLanguages\":{\r\n" + 
			"			\"type\":\"array\",\r\n" + 
			"			 \"items\":{\r\n" + 
			"				 \"type\":\"string\"\r\n" + 
			"			 }\r\n" + 
			"		}\r\n" + 
			"	},\r\n" + 
			"	\"required\":[\r\n" + 
			"		\"authTokenType\",\r\n" + 
			"		\"allowedAuthTypes\",\r\n" + 
			"		\"allowedKycAttributes\"\r\n" + 
			"	],\r\n" + 
			"	\"additionalProperties\": false	\r\n" + 
			"}";
	FilterDto filterDto = new FilterDto();
	SearchFilter searchDto = new SearchFilter();
	Pagination pagination = new Pagination();
	SearchSort searchSort = new SearchSort();
	SearchFilter searchFilter = new SearchFilter();
	PolicySearchDto policySearchDto = new PolicySearchDto();
	SearchDto search = new SearchDto();
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(service, "authPolicyRepository", authPolicyRepository);
		ReflectionTestUtils.setField(service, "policyGroupRepository", policyGroupRepository);
		ReflectionTestUtils.setField(service, "authPolicyHRepository", authPolicyHRepository);
		ReflectionTestUtils.setField(service, "partnerPolicyRepository", partnerPolicyRepository);
		ReflectionTestUtils.setField(service, "webSubPublisher", webSubPublisher);		
		ReflectionTestUtils.setField(service, "filterColumnValidator", filterColumnValidator);
		ReflectionTestUtils.setField(service, "searchHelper", searchHelper);
		ReflectionTestUtils.setField(service, "pageUtils", pageUtils);
		ReflectionTestUtils.setField(service, "filterHelper", filterHelper);
		ReflectionTestUtils.setField(service, "mapper", mapper);

		Mockito.doNothing().when(webSubPublisher).notify(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.doNothing().when(audit).setAuditRequestDto(Mockito.any());
		
		// Search
		searchSort.setSortField("name");
		searchSort.setSortType("asc");
		searchFilter.setColumnName("name");
		searchFilter.setFromValue("");
		searchFilter.setToValue("");
		searchFilter.setType("STARTSWITH");
		searchFilter.setValue("b");
		List<SearchSort> searchDtos1 = new ArrayList<SearchSort>();
		searchDtos1.add(searchSort);
		List<SearchFilter> searchfilterDtos = new ArrayList<SearchFilter>();
		searchfilterDtos.add(searchFilter);
		policySearchDto.setFilters(searchfilterDtos);
		policySearchDto.setPagination(pagination);
		policySearchDto.setSort(searchDtos1);
		policySearchDto.setPolicyType("AUTH");
		
		search.setFilters(searchfilterDtos);
		search.setPagination(pagination);
		search.setSort(searchDtos1);
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
		updateRequest.setIsActive(false);
		Optional<PolicyGroup> policyGroupFromDb = Optional.of(policyGroupData());
		Mockito.when(policyGroupRepository.findById("1234")).thenReturn(policyGroupFromDb);
		Mockito.when(authPolicyRepository.findByPolicyGroupId(policyGroupFromDb.get().getId())).thenReturn(getAuthPolicies());
		service.updatePolicyGroup(updateRequest,"1234");
	}	
	
	@Test(expected = PolicyManagementServiceException.class)
	public void updatePolicyGroupTest_S005() {		
		Mockito.when(policyGroupRepository.findById("1234")).thenReturn(Optional.empty());
		service.updatePolicyGroup(updatePolicygroupRequest(),"1234");
	}
	
	@Test
	public void updatePolicyGroupTest_S006() {
		PolicyGroupUpdateRequestDto updateRequest = updatePolicygroupRequest();
		updateRequest.setIsActive(false);
		Optional<PolicyGroup> policyGroupFromDb = Optional.of(policyGroupData());
		Mockito.when(policyGroupRepository.findById("1234")).thenReturn(policyGroupFromDb);		
		Mockito.when(authPolicyRepository.findActivePoliciesByPolicyGroupId("1234")).thenReturn(getAuthPolicies());
		try {
			service.updatePolicyGroup(updateRequest,"1234");
		}catch (PolicyManagementServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorMessages.ACTIVE_POLICY_EXISTS_UNDER_POLICY_GROUP.getErrorCode()));
		}
	}

	
	
	@Test	
	public void createPoliciesTest_S001() throws PolicyManagementServiceException, Exception {
		PolicyCreateRequestDto request = createPoliciesRequest();
		request.setVersion("0.10");
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode actualObj = objectMapper.readTree(authPolicySchema);
		Mockito.when(mapper.readValue(new URL("http://localhost:8999/schema"), JsonNode.class)).thenReturn(actualObj);
		Mockito.when(policyGroupRepository.findByName("Test_Policy_Group_001")).thenReturn(policyGroupData());
		Mockito.when(authPolicyRepository.findByName("Test")).thenReturn(getAuthPolicy());
		service.createPolicies(request);
	}
	

	@Test	
	public void createPoliciesTest_S002() throws PolicyManagementServiceException, Exception {
		PolicyCreateRequestDto request = createPoliciesRequest();
		request.setPolicyType("Auth");		
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode actualObj = objectMapper.readTree(authPolicySchema);
		Mockito.when(mapper.readValue(new URL("http://localhost:8999/schema"), JsonNode.class)).thenReturn(actualObj);
		Mockito.when(policyGroupRepository.findByName("Test_Policy_Group_001")).thenReturn(policyGroupData());
		Mockito.when(authPolicyRepository.findByName("Test")).thenReturn(null);
		service.createPolicies(request);
	}
	
	@Test(expected = PolicyManagementServiceException.class)
	public void createPoliciesTest_S010() throws PolicyManagementServiceException, Exception {
		PolicyCreateRequestDto request = createPoliciesRequest();
		request.setPolicyType("Auth");		
		request.setPolicies(createWrongAuthPolicyInput());
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode actualObj = objectMapper.readTree(authPolicySchema);
		Mockito.when(mapper.readValue(new URL("http://localhost:8999/schema"), JsonNode.class)).thenReturn(actualObj);
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
		Mockito.when(authPolicyRepository.findByPolicyGroupIdAndName("12345","Test_001")).thenReturn(getAuthPolicy());
		service.createPolicies(request);
	}
	
	@Test(expected = PolicyManagementServiceException.class)	
	public void createPoliciesTest_S005() throws PolicyManagementServiceException, Exception {
		PolicyCreateRequestDto request = createPoliciesRequest();
		request.setPolicyType("Auth");		
		Mockito.when(mapper.readValue(new URL("http://localhost:8999/schema"), String.class)).thenReturn(authPolicySchema);
		PolicyGroup policyGroup = policyGroupData();
		policyGroup.setIsActive(false);
		Mockito.when(policyGroupRepository.findByName("Test_Policy_Group_001")).thenReturn(policyGroup);
		Mockito.when(authPolicyRepository.findByName("Test")).thenReturn(null);
		service.createPolicies(request);
	}
	
	@Test(expected = PolicyManagementServiceException.class)	
	public void createPoliciesTest_S006() throws PolicyManagementServiceException, Exception {
		PolicyCreateRequestDto request = createPoliciesRequest();
		request.setPolicyType("Auth");		
		Mockito.when(mapper.readValue(new URL("http://localhost:8999/schema"), String.class)).thenReturn(authPolicySchema);
		PolicyGroup policyGroup = policyGroupData();
		policyGroup.setIsActive(false);
		Mockito.when(policyGroupRepository.findByName("Test_Policy_Group_001")).thenReturn(policyGroup);
		Mockito.when(authPolicyRepository.findByName("Test")).thenReturn(null);
		service.createPolicies(request);
	}
	
	@Test	
	public void createPoliciesTest_S007() throws PolicyManagementServiceException, Exception {
		PolicyCreateRequestDto request = createPoliciesRequest();
		request.setName("Test");
		request.setVersion("0.10");
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode actualObj = objectMapper.readTree(authPolicySchema);
		Mockito.when(mapper.readValue(new URL("http://localhost:8999/schema"), JsonNode.class)).thenReturn(actualObj);
		Mockito.when(policyGroupRepository.findByName("Test_Policy_Group_001")).thenReturn(policyGroupData());
		Mockito.when(authPolicyRepository.findByName("Test")).thenReturn(getAuthPolicy());
		Mockito.when(authPolicyRepository.findByPolicyGroupIdAndName("12345","Test")).thenReturn(getAuthPolicy());
		try {
			service.createPolicies(request);
		}catch (PolicyManagementServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorMessages.AUTH_POLICY_NAME_DUPLICATE_EXCEPTION.getErrorCode()));
		}
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
		updateRequest.setIsActive(true);
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
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("12345");
		policyGroup.setName("Test");
		policyGroup.setIsActive(true);
		Optional<PolicyGroup> policy = Optional.of(policyGroup);
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policy);
		service.updatePolicyStatus(request,"12345","3456");		
	}
	
	@Test(expected = PolicyManagementServiceException.class)
	public void updatePolicyStatus_NotMappedTest() {
		PolicyStatusUpdateRequestDto request = createUpdatePolicyStatusRequest();
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("5678");
		policyGroup.setIsActive(true);
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
		policyGroup.setIsActive(true);
		Optional<PolicyGroup> policy = Optional.of(policyGroup);
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policy);
		AuthPolicy authPolicy = getAuthPolicy();
		authPolicy.setIsActive(false);
		request.setStatus("De-Active");
		//authPolicy.setPolicySchema("publishedUrl");
		Mockito.when(authPolicyRepository.findById("3456")).thenReturn(Optional.of(authPolicy));
		service.updatePolicyStatus(request,"12345","3456");	
	}
	
	@Test
	public void updatePolicyStatus_Test() {
		PolicyStatusUpdateRequestDto request = createUpdatePolicyStatusRequest();
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("12345");
		policyGroup.setIsActive(true);
		Optional<PolicyGroup> policy = Optional.of(policyGroup);
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policy);
		AuthPolicy authPolicy = getAuthPolicy();
		authPolicy.setIsActive(false);
		request.setStatus("De-Active");
		authPolicy.setPolicySchema("publishedUrl");
		Mockito.when(authPolicyRepository.findById("3456")).thenReturn(Optional.of(authPolicy));
		try {
			service.updatePolicyStatus(request,"12345","3456");
		}catch (PolicyManagementServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorMessages.PUBLISHED_POLICY_STATUS_UPDATE.getErrorCode()));
		}
	}
	
	@Test
	public void updatePolicyStatus_Test_01() {
		PolicyStatusUpdateRequestDto request = createUpdatePolicyStatusRequest();
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("12345");
		policyGroup.setIsActive(true);
		Optional<PolicyGroup> policy = Optional.of(policyGroup);
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policy);
		AuthPolicy authPolicy = getAuthPolicy();
		authPolicy.setIsActive(false);
		request.setStatus("De-Active");
		//authPolicy.setPolicySchema("publishedUrl");
		Mockito.when(authPolicyRepository.findById("3456")).thenReturn(Optional.of(authPolicy));
		Mockito.when(partnerPolicyRepository.findByPolicyIdAndIsActiveTrue("3456")).thenReturn(List.of(new PartnerPolicy()));
		try {
			service.updatePolicyStatus(request,"12345","3456");
		}catch (PolicyManagementServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorMessages.ACTIVE_APIKEY_EXISTS_UNDER_POLICY.getErrorCode()));
		}
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
		policyGroup.setIsActive(true);
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
		policyGroup.setIsActive(true);
		Mockito.when(policyGroupRepository.findByName("Test")).thenReturn(policyGroup);
		AuthPolicy authPolicy = getAuthPolicy();
		authPolicy.getPolicyGroup().setId("8976");
		authPolicy.setId("12345");
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(authPolicy));
		Optional<PolicyGroup> policy = Optional.of(policyGroup);
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policy);		
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
		policyGroup.setIsActive(true);
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
		Mockito.when(authPolicyRepository.findByPolicyGroupIdAndName("12345", "Test")).thenReturn(authPolicyName);
		service.updatePolicies(request, "12345");
	}

	@Test(expected = PolicyManagementServiceException.class)
	public void updatePolicies_Test_01() throws PolicyManagementServiceException, Exception {
		PolicyUpdateRequestDto request = new PolicyUpdateRequestDto();
		request.setPolicyGroupName("Test");
		request.setDesc("Test");
		request.setName("Test");
		request.setPolicies(createAuthPolicyInput());
		request.setVersion("0.8");
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("12345");
		policyGroup.setName("Test");
		policyGroup.setIsActive(false);
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
		Mockito.when(authPolicyRepository.findByPolicyGroupIdAndName("12345", "Test")).thenReturn(authPolicyName);
		service.updatePolicies(request, "12345");
	}
	
	@Test(expected = PolicyManagementServiceException.class)
	public void updatePolicies_Test_02() throws PolicyManagementServiceException, Exception {
		PolicyUpdateRequestDto request = new PolicyUpdateRequestDto();
		request.setPolicyGroupName("Test");
		request.setDesc("Test");
		request.setName("Test");
		request.setPolicies(createAuthPolicyInput());
		request.setVersion("0.8");
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("12345");
		policyGroup.setName("Test");
		policyGroup.setIsActive(true);
		Mockito.when(policyGroupRepository.findByName("Test")).thenReturn(policyGroup);
		AuthPolicy authPolicy = getAuthPolicy();
		authPolicy.getPolicyGroup().setId("12345");
		authPolicy.setName("Test");
		authPolicy.setPolicySchema("localhost");
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(authPolicy));
		Optional<PolicyGroup> policy = Optional.of(policyGroup);
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policy);
		AuthPolicy authPolicyName = getAuthPolicy();
		authPolicyName.setPolicySchema("localhost");
		authPolicyName.getPolicyGroup().setId("12345");
		authPolicyName.setName("Test_01");
		Mockito.when(authPolicyRepository.findByPolicyGroupIdAndName("12345", "Test")).thenReturn(authPolicyName);
		service.updatePolicies(request, "12345");
	}
	
	@Test
	public void updatePolicies_Test_03() throws PolicyManagementServiceException, Exception {
		PolicyUpdateRequestDto request = new PolicyUpdateRequestDto();
		request.setPolicyGroupName("Test");
		request.setDesc("Test");
		request.setName("Test");
		request.setPolicies(createAuthPolicyInput());
		request.setVersion("0.8");
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("12345");
		policyGroup.setName("Test_01");
		policyGroup.setIsActive(true);
		Mockito.when(policyGroupRepository.findByName("Test")).thenReturn(policyGroup);
		AuthPolicy authPolicy = getAuthPolicy();
		authPolicy.getPolicyGroup().setId("12345");
		authPolicy.setName("Test_01");
		authPolicy.setPolicy_type("Auth");
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(authPolicy));
		Optional<PolicyGroup> policy = Optional.of(policyGroup);
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policy);
		AuthPolicy authPolicyName = getAuthPolicy();
		authPolicyName.setPolicySchema("localhost");
		authPolicyName.getPolicyGroup().setId("12345");
		authPolicyName.setName("Test_01");
		authPolicyName.setPolicy_type("Auth");
		Mockito.when(authPolicyRepository.findByPolicyGroupIdAndName("12345", "Test")).thenReturn(authPolicyName);
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode actualObj = objectMapper.readTree(authPolicySchema);
		Mockito.when(mapper.readValue(new URL("http://localhost:8999/schema"), JsonNode.class)).thenReturn(actualObj);
		service.updatePolicies(request, "12345");
	}
	
	@Test
	public void updatePolicies_Test_04() throws PolicyManagementServiceException, Exception {
		PolicyUpdateRequestDto request = new PolicyUpdateRequestDto();
		request.setPolicyGroupName("Test");
		request.setDesc("Test");
		request.setName("Test");
		request.setPolicies(createAuthPolicyInput());
		request.setVersion("0.8");
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("12345");
		policyGroup.setName("Test_01");
		policyGroup.setIsActive(true);
		Mockito.when(policyGroupRepository.findByName("Test")).thenReturn(policyGroup);
		AuthPolicy authPolicy = getAuthPolicy();
		authPolicy.getPolicyGroup().setId("12345");
		authPolicy.setName("Test_01");
		authPolicy.setPolicy_type("Auth");
		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(authPolicy));
		Optional<PolicyGroup> policy = Optional.of(policyGroup);
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policy);
		AuthPolicy authPolicyName = getAuthPolicy();
		authPolicyName.setPolicySchema("localhost");
		authPolicyName.getPolicyGroup().setId("12345");
		authPolicyName.setName("Test_01");
		authPolicyName.setPolicy_type("Auth");
		Mockito.when(authPolicyRepository.findByPolicyGroupIdAndName("12345", "Test_01")).thenReturn(authPolicyName);
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode actualObj = objectMapper.readTree(authPolicySchema);
		Mockito.when(mapper.readValue(new URL("http://localhost:8999/schema"), JsonNode.class)).thenReturn(actualObj);
		service.updatePolicies(request, "12345");
	}

	@Test(expected = PolicyManagementServiceException.class)
	public void updatePoliciesStatus_01() {
		PolicyStatusUpdateRequestDto request = createUpdatePolicyStatusRequest();
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("12345");
		policyGroup.setIsActive(true);
		Optional<PolicyGroup> policy = Optional.of(policyGroup);
		Mockito.when(policyGroupRepository.findById(Mockito.anyString())).thenReturn(policy);
		AuthPolicy authPolicy = getAuthPolicy();
		authPolicy.setIsActive(true);		
		Mockito.when(authPolicyRepository.findById("3456")).thenReturn(Optional.of(authPolicy));		
		service.updatePolicyStatus(request,"1234","3456");	
	}
	
	@Test(expected = PolicyManagementServiceException.class)
	public void updatePoliciesStatus_02() {
		PolicyStatusUpdateRequestDto request = createUpdatePolicyStatusRequest();
		request.setStatus("NotActive");
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("12345");
		Optional<PolicyGroup> policy = Optional.of(policyGroup);
		Mockito.when(policyGroupRepository.findById(Mockito.anyString())).thenReturn(policy);
		AuthPolicy authPolicy = getAuthPolicy();
		authPolicy.setIsActive(true);		
		Mockito.when(authPolicyRepository.findById("3456")).thenReturn(Optional.of(authPolicy));		
		service.updatePolicyStatus(request,"1234","3456");	
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
		Mockito.when(authPolicyRepository.findByPolicyGroupIdAndName("12345", "Test")).thenReturn(getAuthPolicy());
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
		policyGroup.setIsActive(true);
		Mockito.when(policyGroupRepository.findByName("Test")).thenReturn(policyGroup);
		AuthPolicy authPolicy = getAuthPolicy();
		authPolicy.getPolicyGroup().setId("12345");
		authPolicy.setName("Test");
		authPolicy.setIsActive(false);
		authPolicy.setPolicyGroup(policyGroup);
		Mockito.when(authPolicyRepository.findById("Test")).thenReturn(Optional.of(authPolicy));
		Optional<PolicyGroup> policy = Optional.of(policyGroup);
		Mockito.when(policyGroupRepository.findById("Test")).thenReturn(policy);
		Mockito.when(authPolicyRepository.findByPolicyGroupIdAndName("12345", "Test")).thenReturn(getAuthPolicy());
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policy);
		service.publishPolicy("Test", "Test");
	}
	
	@Test
	public void publishPolicyTest_02() throws JsonParseException, JsonMappingException, IOException {
		PolicyUpdateRequestDto request = new PolicyUpdateRequestDto();
		request.setPolicyGroupName("Test");
		request.setDesc("Test");
		request.setName("Test");
		//request.setPolicies(createAuthPolicyInput());
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("12345");
		policyGroup.setName("Test");
		policyGroup.setIsActive(true);
		Mockito.when(policyGroupRepository.findByName("Test")).thenReturn(policyGroup);
		AuthPolicy authPolicy = getAuthPolicy();
		authPolicy.getPolicyGroup().setId("12345");
		authPolicy.setName("Test");
		authPolicy.setIsActive(false);
		authPolicy.setPolicyGroup(policyGroup);
		authPolicy.setPolicySchema("publishedUrl");
		Mockito.when(authPolicyRepository.findById("Test")).thenReturn(Optional.of(authPolicy));
		Optional<PolicyGroup> policy = Optional.of(policyGroup);
		Mockito.when(policyGroupRepository.findById("Test")).thenReturn(policy);
		Mockito.when(authPolicyRepository.findByPolicyGroupIdAndName("12345", "Test")).thenReturn(getAuthPolicy());
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policy);
		try {
		service.publishPolicy("Test", "Test");
		}catch (PolicyManagementServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorMessages.POLICY_PUBLISHED.getErrorCode()));
		}
	}

	@Test(expected = PolicyManagementServiceException.class)
	public void publishPolicyTest_01() throws JsonParseException, JsonMappingException, IOException {
		PolicyUpdateRequestDto request = new PolicyUpdateRequestDto();
		request.setPolicyGroupName("Test");
		request.setDesc("Test");
		request.setName("Test");
		//request.setPolicies(createAuthPolicyInput());
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setId("12345");
		policyGroup.setName("Test");
		policyGroup.setIsActive(false);
		Mockito.when(policyGroupRepository.findByName("Test")).thenReturn(policyGroup);
		AuthPolicy authPolicy = getAuthPolicy();
		authPolicy.getPolicyGroup().setId("12345");
		authPolicy.setName("Test");
		authPolicy.setIsActive(false);
		authPolicy.setPolicySchema("localhost");
		authPolicy.setPolicyGroup(policyGroup);
		Mockito.when(authPolicyRepository.findById("Test")).thenReturn(Optional.of(authPolicy));
		Optional<PolicyGroup> policy = Optional.of(policyGroup);
		Mockito.when(policyGroupRepository.findById("Test")).thenReturn(policy);
		AuthPolicy authPolicy01 = getAuthPolicy();
		authPolicy01.setIsActive(false);
		authPolicy01.setPolicySchema("localhost");
		Mockito.when(authPolicyRepository.findByPolicyGroupIdAndName("12345", "Test")).thenReturn(authPolicy01);
		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(policy);
		service.publishPolicy("Test", "Test");
	}

	@Test(expected = PolicyManagementServiceException.class)
	public void findPolicy_policyIdNotExistsTest() throws FileNotFoundException, IOException, ParseException {
		service.findPolicy("1234");
	}
	
//	@Test(expected = PolicyManagementServiceException.class)
//	public void findPolicy_policyGroupIdNotExistsTest() throws FileNotFoundException, IOException, ParseException {		
//		Mockito.when(policyGroupRepository.findById("12345")).thenReturn(Optional.empty());
//		List<AuthPolicy> authpolicies= getAuthPolicies();
//		authpolicies.get(0).setPolicyGroup(null);
//		Mockito.when(authPolicyRepository.findById("12345")).thenReturn(Optional.of(authpolicies.get(0)));
//		service.findPolicy("12345");
//	}
	
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
	
	@Test
	public void policyFilterValuesTest() {
		Mockito.doReturn(true).when(filterColumnValidator).validate(Mockito.any(), Mockito.any(), Mockito.any());
		PolicyFilterValueDto filterValueDto = new PolicyFilterValueDto(); 
		FilterDto filterDto = new FilterDto();
		filterDto.setColumnName("name");
		filterDto.setText("");
		filterDto.setType("all");
		List<FilterDto> filterDtos = new ArrayList<FilterDto>();
		filterDtos.add(filterDto);
		SearchFilter searchDto = new SearchFilter();
		searchDto.setColumnName("name");
		searchDto.setFromValue("");
		searchDto.setToValue("");
		searchDto.setType("all");
		searchDto.setValue("b");
		filterValueDto.setFilters(filterDtos);
		List<SearchFilter> searchDtos = new ArrayList<SearchFilter>();
		searchDtos.add(searchDto);
		filterValueDto.setPolicyType("AUTH");	
		filterValueDto.setOptionalFilters(searchDtos);
		service.policyFilterValues(filterValueDto);
	}

	@Test
	public void policyFilterValuesTest_01() {
		Mockito.doReturn(true).when(filterColumnValidator).validate(Mockito.any(), Mockito.any(), Mockito.any());
		PolicyFilterValueDto filterValueDto = new PolicyFilterValueDto(); 
		FilterDto filterDto = new FilterDto();
		filterDto.setColumnName("name");
		filterDto.setText("");
		filterDto.setType("all");
		List<FilterDto> filterDtos = new ArrayList<FilterDto>();
		filterDtos.add(filterDto);
		SearchFilter searchDto = new SearchFilter();
		searchDto.setColumnName("name");
		searchDto.setFromValue("");
		searchDto.setToValue("");
		searchDto.setType("all");
		searchDto.setValue("b");
		filterValueDto.setFilters(filterDtos);
		List<SearchFilter> searchDtos = new ArrayList<SearchFilter>();
		searchDtos.add(searchDto);
		filterValueDto.setPolicyType("AUTH");	
		filterValueDto.setOptionalFilters(searchDtos);
		FilterData filterData = new FilterData("test","test");
		List<FilterData> filterDataList = new ArrayList<>();
		filterDataList.add(filterData);
		Mockito.when(filterHelper.filterValuesWithCode(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(filterDataList);
		service.policyFilterValues(filterValueDto);
	}

	@Test
	public void policyGroupFilterValuesTest() {
		Mockito.doReturn(true).when(filterColumnValidator).validate(Mockito.any(), Mockito.any(), Mockito.any());
		FilterValueDto filterValueDto = new FilterValueDto(); 
		FilterDto filterDto = new FilterDto();
		filterDto.setColumnName("name");
		filterDto.setText("");
		filterDto.setType("all");
		List<FilterDto> filterDtos = new ArrayList<FilterDto>();
		filterDtos.add(filterDto);
		SearchFilter searchDto = new SearchFilter();
		searchDto.setColumnName("name");
		searchDto.setFromValue("");
		searchDto.setToValue("");
		searchDto.setType("all");
		searchDto.setValue("b");
		filterValueDto.setFilters(filterDtos);
		List<SearchFilter> searchDtos = new ArrayList<SearchFilter>();
		searchDtos.add(searchDto);			
		filterValueDto.setOptionalFilters(searchDtos);
		service.policyGroupFilterValues(filterValueDto);
	}

	@Test
	public void policyGroupFilterValuesTest_01() {
		Mockito.doReturn(true).when(filterColumnValidator).validate(Mockito.any(), Mockito.any(), Mockito.any());
		FilterValueDto filterValueDto = new FilterValueDto(); 
		FilterDto filterDto = new FilterDto();
		filterDto.setColumnName("name");
		filterDto.setText("");
		filterDto.setType("all");
		List<FilterDto> filterDtos = new ArrayList<FilterDto>();
		filterDtos.add(filterDto);
		SearchFilter searchDto = new SearchFilter();
		searchDto.setColumnName("name");
		searchDto.setFromValue("");
		searchDto.setToValue("");
		searchDto.setType("all");
		searchDto.setValue("b");
		filterValueDto.setFilters(filterDtos);
		List<SearchFilter> searchDtos = new ArrayList<SearchFilter>();
		searchDtos.add(searchDto);			
		filterValueDto.setOptionalFilters(searchDtos);
		FilterData filterData = new FilterData("test","test");
		List<FilterData> filterDataList = new ArrayList<>();
		filterDataList.add(filterData);
		Mockito.when(filterHelper.filterValuesWithCode(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(filterDataList);
		service.policyGroupFilterValues(filterValueDto);
	}

	@Test
	public void getValueForKeyTest() {
		service.getValueForKey("pmp.policy.expiry.period.indays");
	}
	
	@Test
	public void getValueForKeyTest_01() {
		service.getValueForKey("pmp.policy.expiry01.period.indays");
	}
	
	@Test
	public void getValueForKeyTest_02() {
		service.getValueForKey("pmp.auth.policy.schema");
	}
	
	@Test
	public void getValueForKeyTest_03() {
		service.getValueForKey("hibernate.dialect");
	}
	
	@Test
	public void getValueForKeyTest_04() {
		service.getValueForKey("pmp.test.auth.policy.schema");
	}
	
	@Test
	public void searchPolicyTest_01() {
		AuthPolicy authPolicy = getAuthPolicy();
		Mockito.doReturn(new PageImpl<>(Arrays.asList(authPolicy))).when(searchHelper).search(Mockito.any(), Mockito.any());
		service.searchPolicy(policySearchDto);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void searchPolicyTest_02() {
		Mockito.doReturn(new PageImpl<>(Collections.EMPTY_LIST)).when(searchHelper).search(Mockito.any(), Mockito.any());
		service.searchPolicy(policySearchDto);
	}
	
	@Test
	public void searchPolicyTest_03() {
		AuthPolicy authPolicy = getAuthPolicy();
		Mockito.doReturn(new PageImpl<>(Arrays.asList(authPolicy))).when(searchHelper).search(Mockito.any(), Mockito.any());
		policySearchDto.setPolicyType("all");
		service.searchPolicy(policySearchDto);
	}

	@Test
	public void searchPolicyGroup_01() {
		PolicyGroup policyGroup = new PolicyGroup();
		policyGroup.setName("test");
		policyGroup.setDesc("test");
		Mockito.doReturn(new PageImpl<>(Arrays.asList(policyGroup))).when(searchHelper).search(Mockito.any(), Mockito.any());
		service.searchPolicyGroup(policySearchDto);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void searchPolicyGroup_02() {
		Mockito.doReturn(new PageImpl<>(Collections.EMPTY_LIST)).when(searchHelper).search(Mockito.any(), Mockito.any());
		service.searchPolicyGroup(policySearchDto);
	}
	
	@Test
	public void searchPartnerPolicyTest_01() {
		PartnerPolicy partnerPolicy = getPartnerPolicy();
		Mockito.doReturn(new PageImpl<>(Arrays.asList(partnerPolicy))).when(searchHelper).search(Mockito.any(), Mockito.any());
		service.searchPartnerPolicy(search);
	}
	
	@Test
	public void getActivePolicyDetailsByGroupName01() {
		Mockito.when(policyGroupRepository.findByName(Mockito.any())).thenReturn(null);
		try {
			service.getActivePolicyDetailsByGroupName("groupName");
		}catch (PolicyManagementServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorMessages.POLICY_GROUP_NAME_NOT_EXISTS.getErrorCode()));
		}
	}
	
	@Test
	public void getActivePolicyDetailsByGroupName02() {
		PolicyGroup validPolicyGroup = new PolicyGroup();
		validPolicyGroup.setCrBy("system");
		validPolicyGroup.setCrDtimes(LocalDateTime.now());
		validPolicyGroup.setIsActive(false);
		validPolicyGroup.setName("policyGroupName");
		validPolicyGroup.setDesc("policyGroupDesc");
		Mockito.when(policyGroupRepository.findByName(Mockito.any())).thenReturn(validPolicyGroup);
		try {
			service.getActivePolicyDetailsByGroupName("groupName");
		}catch (PolicyManagementServiceException e) {
			assertTrue(e.getErrorCode().equals(ErrorMessages.POLICY_GROUP_NOT_ACTIVE.getErrorCode()));
		}
	}
	
	@Test
	public void getActivePolicyDetailsByGroupName03() {
		PolicyGroup validPolicyGroup = new PolicyGroup();
		validPolicyGroup.setCrBy("system");
		validPolicyGroup.setCrDtimes(LocalDateTime.now());
		validPolicyGroup.setIsActive(true);
		validPolicyGroup.setName("policyGroupName");
		validPolicyGroup.setDesc("policyGroupDesc");
		Mockito.when(policyGroupRepository.findByName(Mockito.any())).thenReturn(validPolicyGroup);
		service.getActivePolicyDetailsByGroupName("groupName");
	}
	
	@Test
	public void getActivePolicyDetailsByGroupName04() {
		PolicyGroup validPolicyGroup = new PolicyGroup();
		validPolicyGroup.setCrBy("system");
		validPolicyGroup.setCrDtimes(LocalDateTime.now());
		validPolicyGroup.setIsActive(true);
		validPolicyGroup.setId("groupid");
		validPolicyGroup.setName("policyGroupName");
		validPolicyGroup.setDesc("policyGroupDesc");
		AuthPolicy validAuthPolicy = new AuthPolicy();
		validAuthPolicy.setIsActive(true);
		validAuthPolicy.setId("policyId");
		validAuthPolicy.setName("authpolicy");
		validAuthPolicy.setDescr("validPolicyDesc");
		validAuthPolicy.setPolicy_type("auth");
		validAuthPolicy.setPolicyGroup(validPolicyGroup);
		List<AuthPolicy> listOfAuthPolicies = new ArrayList<>();
		listOfAuthPolicies.add(validAuthPolicy);
		Mockito.when(policyGroupRepository.findByName(Mockito.any())).thenReturn(validPolicyGroup);
		Mockito.when(authPolicyRepository.findActivePoliciesByPolicyGroupId("groupid")).thenReturn(listOfAuthPolicies);
		service.getActivePolicyDetailsByGroupName("groupName");
	}
	
//	List<AuthPolicy> authPoliciesByGroupName = authPolicyRepository
//			.findActivePoliciesByPolicyGroupId(policy_group_by_name.getId());

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
