package io.mosip.pms.policy.test.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.mosip.pms.common.repository.AuthPolicyRepository;
import io.mosip.pms.policy.controller.PolicyManagementController;
import io.mosip.pms.policy.dto.*;
import io.mosip.pms.policy.errorMessages.ServiceError;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pms.common.dto.FilterDto;
import io.mosip.pms.common.dto.FilterValueDto;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.common.dto.Pagination;
import io.mosip.pms.common.dto.PolicyFilterValueDto;
import io.mosip.pms.common.dto.PolicySearchDto;
import io.mosip.pms.common.dto.SearchAuthPolicy;
import io.mosip.pms.common.dto.SearchDto;
import io.mosip.pms.common.dto.SearchFilter;
import io.mosip.pms.common.dto.SearchSort;
import io.mosip.pms.common.entity.PolicyGroup;
import io.mosip.pms.common.validator.FilterColumnValidator;
import io.mosip.pms.policy.errorMessages.PolicyManagementServiceException;
import io.mosip.pms.policy.service.PolicyManagementService;
import io.mosip.pms.policy.test.PolicyServiceTest;
import io.mosip.pms.policy.util.AuditUtil;

/**
 * @author Nagarjuna Kuchi
 *
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PolicyServiceTest.class)
@AutoConfigureMockMvc
@EnableWebMvc
@TestPropertySource("classpath:application.properties")
public class PolicyManagementControllerTest {
	
	@Autowired
    private MockMvc mockMvc;
	
	@Mock
	private PolicyManagementService policyManagementService;
	
	@Autowired
	private ObjectMapper objectMapper;	
	
	@Mock
	FilterColumnValidator filterColumnValidator;
	
	@Mock
	private AuditUtil audit;
	@InjectMocks
	PolicyManagementController policyManagementController;

	@Mock
	AuthPolicyRepository authPolicyRepository;
	
	FilterDto filterDto = new FilterDto();
	SearchFilter searchFilter = new SearchFilter();
	FilterValueDto filterValueDto = new FilterValueDto();
	PolicyFilterValueDto policyFilterValueDto = new PolicyFilterValueDto();
	PolicySearchDto policySearchDto = new PolicySearchDto();
	Pagination pagination = new Pagination();
	SearchSort searchSort = new SearchSort();
	SearchDto searchDto = new SearchDto();
	
	@Before
	public void setUp() {
		//ReflectionTestUtils.setField(policyManagementService, "filterColumnValidator", filterColumnValidator);
		Mockito.doNothing().when(audit).setAuditRequestDto(Mockito.any());
		filterDto.setColumnName("name");
		filterDto.setText("");
		filterDto.setType("ALL");
		searchFilter.setColumnName("name");
		searchFilter.setFromValue("");
		searchFilter.setToValue("");
		searchFilter.setType("ALL");
		searchFilter.setValue("m");
		List<FilterDto> filterDtos = new ArrayList<FilterDto>();
    	filterDtos.add(filterDto);
    	List<SearchFilter> searchDtos = new ArrayList<SearchFilter>();
    	searchDtos.add(searchFilter);
    	filterValueDto.setFilters(filterDtos);
    	policyFilterValueDto.setPolicyType("");
    	policyFilterValueDto.setFilters(filterDtos);
    	policySearchDto.setPolicyType("");
    	searchDto.setFilters(searchDtos);
    	searchSort.setSortField("model");
    	searchSort.setSortType("asc");
    	List<SearchSort> searchDtos1 = new ArrayList<SearchSort>();
    	searchDtos1.add(searchSort);
    	searchDto.setSort(searchDtos1);
    	pagination.setPageFetch(10);
    	pagination.setPageStart(0);
    	
	}
	
	@Test
	@WithMockUser(roles = {"POLICYMANAGER"})
	public void getValueForKeyTest() throws PolicyManagementServiceException, Exception{
		mockMvc.perform(MockMvcRequestBuilders.get("/policies/config/12345")).
		andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	@WithMockUser(roles = {"POLICYMANAGER"})
	public void getPolicyGroups() throws PolicyManagementServiceException, Exception{
		mockMvc.perform(MockMvcRequestBuilders.get("/policies/group/all")).
		andExpect(MockMvcResultMatchers.status().isOk());
	}
		
	@Test
	@WithMockUser(roles = {"POLICYMANAGER"})
	public void getPolicyGroupTest() throws Exception{
		PolicyWithAuthPolicyDto response = new PolicyWithAuthPolicyDto();
		Mockito.when(policyManagementService.getPolicyGroupPolicy(Mockito.any())).thenReturn(response);
		mockMvc.perform(MockMvcRequestBuilders.get("/policies/group/12345")).
		andExpect(MockMvcResultMatchers.status().isOk());		
	}
	
	@Test
	@WithMockUser(roles = {"POLICYMANAGER"})
	public void getPartnersPolicyTest() throws Exception{
		PolicyResponseDto response = new PolicyResponseDto();
		Mockito.when(policyManagementService.getPartnerMappedPolicy(Mockito.any(),Mockito.any())).thenReturn(response);
		mockMvc.perform(MockMvcRequestBuilders.get("/policies/12345/partner/12345")).
		andExpect(MockMvcResultMatchers.status().isOk());		
	}
	
	@Test
	@WithMockUser(roles = {"POLICYMANAGER"})
	public void publishPolicyTest() throws Exception{
		PolicyResponseDto response = new PolicyResponseDto();
		Mockito.when(policyManagementService.publishPolicy(Mockito.any(),Mockito.any())).thenReturn(response);
		mockMvc.perform(MockMvcRequestBuilders.post("/policies/12345/group/12345/publish")).
		andExpect(MockMvcResultMatchers.status().isOk());		
	}
	
	@Test
	@WithMockUser(roles = {"POLICYMANAGER"})
	public void searchPolicy() throws PolicyManagementServiceException, Exception{
		PageResponseDto<SearchAuthPolicy> response = new PageResponseDto<SearchAuthPolicy>();
		 Mockito.when(policyManagementService.searchPolicy(Mockito.any())).thenReturn(response);
		 RequestWrapper<PolicySearchDto> policySearchDto = createPolicySearchhRequest();
		 mockMvc.perform(post("/policies/search").contentType(MediaType.APPLICATION_JSON_VALUE)
	                .content(objectMapper.writeValueAsString(policySearchDto))).andExpect(status().isOk());
	}
	
	@Test
	@WithMockUser(roles = {"POLICYMANAGER"})
	public void searchPolicyGroup() throws PolicyManagementServiceException, Exception{
		PageResponseDto<PolicyGroup> response = new PageResponseDto<PolicyGroup>();
		 Mockito.when(policyManagementService.searchPolicyGroup(Mockito.any())).thenReturn(response);
		 RequestWrapper<SearchDto> policyGroupSearchDto = createPolicyGroupSearchhRequest();
		 mockMvc.perform(post("/policies/group/search").contentType(MediaType.APPLICATION_JSON_VALUE)
	                .content(objectMapper.writeValueAsString(policyGroupSearchDto))).andExpect(status().isOk());
	}
	
	@Test
	@WithMockUser(roles = {"POLICYMANAGER"})
	public void policyGroupFilterValuesTest() throws PolicyManagementServiceException, Exception{
		FilterResponseCodeDto response = new FilterResponseCodeDto();
		Mockito.when(policyManagementService.policyGroupFilterValues(Mockito.any())).thenReturn(response);
		RequestWrapper<FilterValueDto> policyGroupFilterValueDto = createPolicyGFilterRequest();
		 mockMvc.perform(post("/policies/group/filtervalues").contentType(MediaType.APPLICATION_JSON_VALUE)
	                .content(objectMapper.writeValueAsString(policyGroupFilterValueDto))).andExpect(status().isOk());
		
	}
	
	@Test
	@WithMockUser(roles = {"POLICYMANAGER"})
	public void policyFilterValuesTest() throws PolicyManagementServiceException, Exception{
		FilterResponseCodeDto response = new FilterResponseCodeDto();
		Mockito.when(policyManagementService.policyFilterValues(Mockito.any())).thenReturn(response);
		RequestWrapper<PolicyFilterValueDto> policyFilterValueDto = createPolicyFilterrRequest();
		 mockMvc.perform(post("/policies/filtervalues").contentType(MediaType.APPLICATION_JSON_VALUE)
	                .content(objectMapper.writeValueAsString(policyFilterValueDto))).andExpect(status().isOk());
	}
	
	@Test
	@WithMockUser(roles = {"POLICYMANAGER"})
	public void policyGroupCreationTest() throws PolicyManagementServiceException, Exception{
		PolicyGroupCreateResponseDto response = new PolicyGroupCreateResponseDto();
		Mockito.when(policyManagementService.createPolicyGroup(Mockito.any())).thenReturn(response);
		RequestWrapper<PolicyGroupCreateRequestDto> request = createPolicyGroupRequest();
		mockMvc.perform(post("/policies/group/new").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	@WithMockUser(roles = {"POLICYMANAGER"})
	public void policyCreationTest() throws PolicyManagementServiceException, Exception{
		PolicyCreateResponseDto response = new PolicyCreateResponseDto();
		Mockito.when(policyManagementService.createPolicies(Mockito.any())).thenReturn(response);
		RequestWrapper<PolicyCreateRequestDto> request = createPolicyRequest();
		mockMvc.perform(post("/policies").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	@WithMockUser(roles = {"POLICYMANAGER"})
	public void updatePolicyGroupTest() throws Exception{
		PolicyGroupCreateResponseDto response = new PolicyGroupCreateResponseDto();
		Mockito.when(policyManagementService.updatePolicyGroup(Mockito.any(),Mockito.any())).thenReturn(response);
		RequestWrapper<PolicyGroupUpdateRequestDto> request = createPolicyGroupUpdateRequest();
		
		mockMvc.perform(put("/policies/group/12345").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	@WithMockUser(roles = {"POLICYMANAGER"})
	public void updatePolicyTest() throws Exception{
		PolicyCreateResponseDto response = new PolicyCreateResponseDto();
		Mockito.when(policyManagementService.updatePolicies(Mockito.any(),Mockito.any())).thenReturn(response);
		RequestWrapper<PolicyUpdateRequestDto> request = createPolicyUpdateRequest();
		
		mockMvc.perform(put("/policies/12345").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	@WithMockUser(roles = {"POLICYMANAGER"})
	public void updatePolicyStatus() throws JsonProcessingException, Exception{
		ResponseWrapper<PolicyStatusUpdateResponseDto> response = new ResponseWrapper<>();
		Mockito.when(policyManagementService.updatePolicyStatus(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(response);
		RequestWrapper<PolicyStatusUpdateRequestDto> request = createPolicyStatusUpateRequest();
		
		mockMvc.perform(MockMvcRequestBuilders.patch("/policies/12345/group/12345").contentType(MediaType.APPLICATION_JSON_VALUE)
    			.content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	@WithMockUser(roles = {"POLICYMANAGER"})
	public void getPoliciesTest() throws Exception{
		mockMvc.perform(MockMvcRequestBuilders.get("/policies")).
		andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	@WithMockUser(roles = {"POLICYMANAGER"})
	public void getPolicyTest() throws Exception{
		PolicyResponseDto response = new PolicyResponseDto();
		Mockito.when(policyManagementService.findPolicy(Mockito.any())).thenReturn(response);
		
		mockMvc.perform(MockMvcRequestBuilders.get("/policies/12345")).
		andExpect(MockMvcResultMatchers.status().isOk());		
	}
	
	@Test
	@WithMockUser(roles = {"POLICYMANAGER"})
	public void getPoliciesByGroupName() throws Exception{
		List<PolicyDetailsDto> response = new ArrayList<>();
		Mockito.when(policyManagementService.getActivePolicyDetailsByGroupName(Mockito.any())).thenReturn(response);		
		mockMvc.perform(MockMvcRequestBuilders.get("/policies/active/group/12345")).
		andExpect(MockMvcResultMatchers.status().isOk());		
	}
	
	private RequestWrapper<PolicyStatusUpdateRequestDto> createPolicyStatusUpateRequest() {
		RequestWrapper<PolicyStatusUpdateRequestDto> request = new RequestWrapper<PolicyStatusUpdateRequestDto>();
		request.setRequest(createPolicyStatusUpdateRequest());
        request.setId("mosip.partnermanagement.policies.policy.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;

	}

	private PolicyStatusUpdateRequestDto createPolicyStatusUpdateRequest() {
		PolicyStatusUpdateRequestDto request = new PolicyStatusUpdateRequestDto();
		request.setStatus("De-Active");
		return request;
	}

	private RequestWrapper<PolicyUpdateRequestDto> createPolicyUpdateRequest() {
		RequestWrapper<PolicyUpdateRequestDto> request = new RequestWrapper<PolicyUpdateRequestDto>();
		PolicyUpdateRequestDto updateRequest = new PolicyUpdateRequestDto();
		updateRequest.setDesc("Update Policy");
		updateRequest.setName("Update Name");
		updateRequest.setPolicyGroupName("Test");
		updateRequest.setVersion("1.0");
		request.setRequest(updateRequest);
        request.setId("mosip.partnermanagement.policies.authPolicies.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
	}
	
	private RequestWrapper<PolicyGroupUpdateRequestDto> createPolicyGroupUpdateRequest() {
		RequestWrapper<PolicyGroupUpdateRequestDto> request = new RequestWrapper<PolicyGroupUpdateRequestDto>();
		PolicyGroupUpdateRequestDto updateRequest = new PolicyGroupUpdateRequestDto();
		updateRequest.setDesc("Update Policy");
		updateRequest.setName("Update Name");
		updateRequest.setIsActive(true);
		request.setRequest(updateRequest);
        request.setId("mosip.partnermanagement.policies.authPolicies.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
	}

	private RequestWrapper<PolicyGroupCreateRequestDto> createPolicyGroupRequest() {
		RequestWrapper<PolicyGroupCreateRequestDto> request = new RequestWrapper<PolicyGroupCreateRequestDto>();
		request.setRequest(createPolicyGRequest());
        request.setId("mosip.partnermanagement.policies.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
	}
	
	private PolicyGroupCreateRequestDto createPolicyGRequest() {
		PolicyGroupCreateRequestDto requestDto = new PolicyGroupCreateRequestDto();
		requestDto.setName("PolicyName");
		requestDto.setDesc("PolicyDesc");
		return requestDto;
	}
	
	private RequestWrapper<PolicyCreateRequestDto> createPolicyRequest() {
		RequestWrapper<PolicyCreateRequestDto> request = new RequestWrapper<PolicyCreateRequestDto>();
		request.setRequest(createPolicyyRequest());
        request.setId("mosip.partnermanagement.policies.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
	}
	
	private PolicyCreateRequestDto createPolicyyRequest() {
		JSONObject policies = null;
		PolicyCreateRequestDto requestDto = new PolicyCreateRequestDto();
		requestDto.setPolicyGroupName("mpolicygroup-default-auth");
		requestDto.setPolicyId("mosip.partnermanagement.policies.create");
		requestDto.setPolicyType("AUTH");
		requestDto.setVersion("1.0");
		requestDto.setName("PolicyName");
		requestDto.setDesc("PolicyDesc");
		requestDto.setPolicies(policies);
		return requestDto;
	}
	
	
	private RequestWrapper<PolicySearchDto> createPolicySearchhRequest() {
		RequestWrapper<PolicySearchDto> request = new RequestWrapper<PolicySearchDto>();
		request.setRequest(createPolicySearchRequest());
        request.setId("mosip.partnermanagement.policies.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
	}
	private PolicySearchDto createPolicySearchRequest() {
		List<SearchSort> searchDtos1 = new ArrayList<SearchSort>();
		List<SearchFilter> searchDtos = new ArrayList<SearchFilter>();
		PolicySearchDto requestDto = new PolicySearchDto();
		Pagination pagination = new Pagination();
		SearchSort searchSort = new SearchSort();
		SearchDto searchDto = new SearchDto();
		SearchFilter searchFilter = new SearchFilter();
		searchFilter.setColumnName("name");
		searchFilter.setFromValue("");
		searchFilter.setToValue("");
		searchFilter.setType("ALL");
		searchFilter.setValue("m");
    	searchDtos.add(searchFilter);
		searchDto.setFilters(searchDtos);
    	searchSort.setSortField("model");
    	searchSort.setSortType("asc");
    	searchDtos1.add(searchSort);
    	searchDto.setSort(searchDtos1);
    	pagination.setPageFetch(10);
    	pagination.setPageStart(0);
    	requestDto.setPolicyType("AUTH");
    	requestDto.setFilters(searchDtos);
    	requestDto.setSort(searchDtos1);
    	requestDto.setPagination(pagination);
		return requestDto;
	}
	
	private RequestWrapper<SearchDto> createPolicyGroupSearchhRequest() {
		RequestWrapper<SearchDto> request = new RequestWrapper<SearchDto>();
		request.setRequest(createPolicyGroupSearchRequest());
        request.setId("mosip.partnermanagement.policies.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
	}
	private SearchDto createPolicyGroupSearchRequest() {
		List<SearchSort> searchDtos1 = new ArrayList<SearchSort>();
		List<SearchFilter> searchDtos = new ArrayList<SearchFilter>();
		SearchDto requestDto = new SearchDto();
		Pagination pagination = new Pagination();
		SearchSort searchSort = new SearchSort();
		SearchFilter searchFilter = new SearchFilter();
		searchFilter.setColumnName("name");
		searchFilter.setFromValue("");
		searchFilter.setToValue("");
		searchFilter.setType("ALL");
		searchFilter.setValue("m");
		searchSort.setSortField("model");
    	searchSort.setSortType("asc");
    	searchDtos.add(searchFilter);
    	searchDtos1.add(searchSort);
    	pagination.setPageFetch(10);
    	pagination.setPageStart(0);
    	requestDto.setFilters(searchDtos);
    	requestDto.setSort(searchDtos1);
    	requestDto.setPagination(pagination);
		return requestDto;
	}
	
	private RequestWrapper<FilterValueDto> createPolicyGFilterRequest() {
		RequestWrapper<FilterValueDto> request = new RequestWrapper<FilterValueDto>();
		request.setRequest(createPolicyGroupFilterRequest());
        request.setId("mosip.partnermanagement.policies.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
	}
	
	/**
	 * 
	 * @return
	 */
	private FilterValueDto createPolicyGroupFilterRequest() {
		List<FilterDto> filterDtos = new ArrayList<FilterDto>();
		List<SearchFilter> searchDtos = new ArrayList<SearchFilter>();
		FilterDto filterDto = new FilterDto();
		SearchFilter searchFilter = new SearchFilter();
		FilterValueDto filterValueDto = new FilterValueDto();
		PolicyFilterValueDto policyFilterValueDto = new PolicyFilterValueDto();
		policyFilterValueDto.setPolicyType("");
    	policyFilterValueDto.setFilters(filterDtos);
		filterDto.setColumnName("name");
		filterDto.setText("");
		filterDto.setType("ALL");
		searchFilter.setColumnName("name");
		searchFilter.setFromValue("");
		searchFilter.setToValue("");
		searchFilter.setType("ALL");
		searchFilter.setValue("m");
    	filterDtos.add(filterDto);
    	searchDtos.add(searchFilter);
    	filterValueDto.setFilters(filterDtos);
    	return filterValueDto;
	}
	
	/**
	 * 
	 * @return
	 */
	private RequestWrapper<PolicyFilterValueDto> createPolicyFilterrRequest() {
		RequestWrapper<PolicyFilterValueDto> request = new RequestWrapper<PolicyFilterValueDto>();
		request.setRequest(createPolicyFilterRequest());
        request.setId("mosip.partnermanagement.policies.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
	}
	
	/**
	 * 
	 * @return
	 */
	private PolicyFilterValueDto createPolicyFilterRequest() {
		List<FilterDto> filterDtos = new ArrayList<FilterDto>();
		List<SearchFilter> searchDtos = new ArrayList<SearchFilter>();
		FilterDto filterDto = new FilterDto();
		SearchFilter searchFilter = new SearchFilter();
		PolicyFilterValueDto policyFilterValueDto = new PolicyFilterValueDto();
		filterDto.setColumnName("name");
		filterDto.setText("");
		filterDto.setType("ALL");
		searchFilter.setColumnName("name");
		searchFilter.setFromValue("");
		searchFilter.setToValue("");
		searchFilter.setType("ALL");
		searchFilter.setValue("m");
    	filterDtos.add(filterDto);
    	searchDtos.add(searchFilter);
    	policyFilterValueDto.setPolicyType("AUTH");
    	policyFilterValueDto.setFilters(filterDtos);
    	return policyFilterValueDto;
	}
	@Test
	public void definePolicyGroupTest001(){
		PolicyGroupCreateRequestDto createRequestDto = new PolicyGroupCreateRequestDto();
		createRequestDto.setName("Name");
		createRequestDto.setDesc("desc");
		RequestWrapper<PolicyGroupCreateRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setId("id");
		requestWrapper.setVersion("1.2.0");
		requestWrapper.setMetadata("metadata");
		requestWrapper.setRequesttime(LocalDateTime.now());
		requestWrapper.setRequest(createRequestDto);
		PolicyGroupCreateResponseDto mockedResponseDto = new PolicyGroupCreateResponseDto();
		Mockito.when(policyManagementService.createPolicyGroup(createRequestDto)).thenReturn(mockedResponseDto);
		ResponseWrapper<PolicyGroupCreateResponseDto> response = policyManagementController.definePolicyGroup(requestWrapper);
		assertNotNull(response);
		assertNotNull(response.getResponse());
		assertEquals(mockedResponseDto, response.getResponse());
		verify(audit).setAuditRequestDto(PolicyManageEnum.CREATE_POLICY_GROUP, createRequestDto.getName(), "policyGroupName");
	}
	@Test
	public void testSearchPolicyGroup() {
		SearchDto searchDto = new SearchDto();
		List<SearchFilter> filters=new ArrayList<>();
		SearchFilter searchFilter1=new SearchFilter();
		searchFilter1.setFromValue("fvalue");
		searchFilter1.setValue("value");
		searchFilter1.setColumnName("cname");
		searchFilter1.setToValue("to value");
		searchFilter1.setFromValue("from value");
		List<String> values=new ArrayList<>();
		String s="Values";
		values.add(s);
		searchFilter1.setValues(values);
		searchFilter1.setType("type");
		filters.add(searchFilter1);
		searchDto.setFilters(filters);
		List<SearchSort> sort=new ArrayList<>();
		SearchSort searchSort1=new SearchSort();
		searchSort1.setSortField("sortfield");
		searchSort1.setSortType("stype");
		sort.add(searchSort1);
		searchDto.setSort(sort);
		searchDto.setPagination(pagination);
		RequestWrapper<SearchDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setVersion("1.2.1");
		requestWrapper.setId("id");
		requestWrapper.setMetadata("metadata");
		requestWrapper.setRequest(searchDto);
		PageResponseDto<PolicyGroup> mockedPageResponseDto = new PageResponseDto<>();
		Mockito.when(policyManagementService.searchPolicyGroup(searchDto)).thenReturn(mockedPageResponseDto);
		ResponseWrapper<PageResponseDto<PolicyGroup>> response = policyManagementController.searchPolicyGroup(requestWrapper);
		assertNotNull(response);
		assertNotNull(response.getResponse());
		assertEquals(mockedPageResponseDto, response.getResponse());
		verify(audit).setAuditRequestDto(PolicyManageEnum.SEARCH_POLICY_GROUP);
	}

	@Test
	public void testUpdatePolicyGroup() {
		String policyGroupId = "policyGroupId";
		PolicyGroupUpdateRequestDto updateRequestDto = new PolicyGroupUpdateRequestDto();
		updateRequestDto.setDesc("desc");
		updateRequestDto.setName("name");
		updateRequestDto.setIsActive(true);
		RequestWrapper<PolicyGroupUpdateRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setId("id");
		requestWrapper.setVersion("1.2.0");
		requestWrapper.setMetadata("metadata");
		requestWrapper.setRequest(updateRequestDto);
		PolicyGroupCreateResponseDto mockedResponseDto = new PolicyGroupCreateResponseDto();
		Mockito.when(policyManagementService.updatePolicyGroup(updateRequestDto, policyGroupId)).thenReturn(mockedResponseDto);
		ResponseWrapper<PolicyGroupCreateResponseDto> response = policyManagementController.updatePolicyGroup(policyGroupId, requestWrapper);
		assertNotNull(response);
		assertNotNull(response.getResponse());
		assertEquals(mockedResponseDto, response.getResponse());
		verify(audit).setAuditRequestDto(PolicyManageEnum.UPDATE_POLICY_GROUP, updateRequestDto.getName(), "policyGroupName");
	}
	@Test
	public void testDefinePolicy() throws Exception {
		PolicyCreateRequestDto createRequestDto = new PolicyCreateRequestDto();
		createRequestDto.setPolicyId("id");
		createRequestDto.setDesc("desc");
		createRequestDto.setPolicyType("ptype");
		createRequestDto.setPolicyGroupName("pgn");
		createRequestDto.setVersion("1.2.0");
		createRequestDto.setName("name");
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name", "John Wick");
		jsonObject.put("age", 58);
		jsonObject.put("email", "xyz@gmail.com");
		createRequestDto.setPolicies(jsonObject);
		RequestWrapper<PolicyCreateRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setMetadata("metadata");
		requestWrapper.setId("id");
		requestWrapper.setVersion("1.2.0");
		requestWrapper.setRequest(createRequestDto);
		PolicyCreateResponseDto mockedResponseDto = new PolicyCreateResponseDto();
		Mockito.when(policyManagementService.createPolicies(createRequestDto)).thenReturn(mockedResponseDto);
		ResponseWrapper<PolicyCreateResponseDto> response = policyManagementController.definePolicy(requestWrapper);
		assertNotNull(response);
		assertNotNull(response.getResponse());
		assertEquals(mockedResponseDto, response.getResponse());
		verify(audit).setAuditRequestDto(PolicyManageEnum.CREATE_POLICY, createRequestDto.getName(), "policyName");
	}
	@Test
	public void testPublishPolicy() throws JsonParseException, JsonMappingException, IOException {
		String policygroupId = "policyGroupId";
		String policyId = "policyId";
		PolicyResponseDto mockedResponseDto = new PolicyResponseDto();
		RequestWrapper<PolicyGroupUpdateRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setVersion("1.2.0");
		requestWrapper.setId("id");
		requestWrapper.setMetadata("metadata");
		Mockito.when(policyManagementService.publishPolicy(policygroupId, policyId)).thenReturn(mockedResponseDto);
		ResponseWrapper<PolicyResponseDto> response = policyManagementController.publishPolicy(policygroupId, policyId);
		assertNotNull(response);
		assertNotNull(response.getResponse());
		assertEquals(mockedResponseDto, response.getResponse());
		verify(audit).setAuditRequestDto(PolicyManageEnum.PUBLISH_POLICY, policyId, "policyId");
	}
	@Test
	public void testUpdatePolicyDetails() throws Exception {
		String policyId = "policyId";
		PolicyUpdateRequestDto updateRequestDto = new PolicyUpdateRequestDto();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name", "Joey Tribbiani");
		jsonObject.put("age", 28);
		jsonObject.put("email", "Joey@gmail.com");
		updateRequestDto.setPolicies(jsonObject);
		updateRequestDto.setVersion("1.12.2");
		updateRequestDto.setPolicyGroupName("pgname");
		updateRequestDto.setDesc("desc");
		updateRequestDto.setName("name");
		RequestWrapper<PolicyUpdateRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setMetadata("metadata");
		requestWrapper.setVersion("1.12.2");
		requestWrapper.setId("id");
		requestWrapper.setRequest(updateRequestDto);
		ResponseWrapper<PolicyCreateResponseDto> response = policyManagementController.updatePolicyDetails(requestWrapper, policyId);
		assertNotNull(response);
	}
	@Test
	public void testUpdatePolicyStatus() throws Exception {
		PolicyStatusUpdateRequestDto statusUpdateRequest = new PolicyStatusUpdateRequestDto();
		statusUpdateRequest.setStatus("active");
		RequestWrapper<PolicyStatusUpdateRequestDto> requestDto = new RequestWrapper<>();
		requestDto.setId("id");
		requestDto.setVersion("1.2.3");
		requestDto.setMetadata("metadata");
		requestDto.setRequest(statusUpdateRequest);
	}
	@Test
	public void testGetPolicies() throws FileNotFoundException, IOException, ParseException, ParseException, org.json.simple.parser.ParseException {
		List<PolicyResponseDto> mockedResponseList = new ArrayList<>();
		Mockito.when(policyManagementService.findAllPolicies()).thenReturn(mockedResponseList);
		ResponseWrapper<List<PolicyResponseDto>> response = policyManagementController.getPolicies();
		assertNotNull(response);
		assertNotNull(response.getResponse());
		assertEquals(mockedResponseList, response.getResponse());
	}
	@Test
	public void testGetPolicy() throws Exception {
		String policyId = "policyId";
		PolicyResponseDto mockedResponseDto = new PolicyResponseDto();
		Mockito.when(policyManagementService.findPolicy(policyId)).thenReturn(mockedResponseDto);
		ResponseWrapper<PolicyResponseDto> response = policyManagementController.getPolicy(policyId);
		response.setVersion("version");
		response.setId("id");
		List<ServiceError> errors = new ArrayList<>();
		response.setErrors(errors);
		assertNotNull(response);
		assertNotNull(response.getResponse());
		assertEquals(mockedResponseDto, response.getResponse());
	}
	@Test
	public void testGetPolicyGroup001() throws JsonParseException, JsonMappingException, IOException {
		String policygroupId = "policygroupId";
		PolicyWithAuthPolicyDto mockedResponseDto = new PolicyWithAuthPolicyDto();
		Mockito.when(policyManagementService.getPolicyGroupPolicy(policygroupId)).thenReturn(mockedResponseDto);
		ResponseWrapper<PolicyWithAuthPolicyDto> response = policyManagementController.getPolicyGroup(policygroupId);
		assertNotNull(response);
		assertNotNull(response.getResponse());
		assertEquals(mockedResponseDto, response.getResponse());
	}
	@Test
	public void testGetPolicyGroup() throws JsonParseException, JsonMappingException, IOException {
		List<PolicyWithAuthPolicyDto> mockedResponseList = new ArrayList<>();
		Mockito.when(policyManagementService.getPolicyGroup()).thenReturn(mockedResponseList);
		ResponseWrapper<List<PolicyWithAuthPolicyDto>> response = policyManagementController.getPolicyGroup();
		assertNotNull(response);
		assertNotNull(response.getResponse());
		assertEquals(mockedResponseList, response.getResponse());
	}
	@Test
	public void testSearchPolicyGroup001() {
		SearchDto searchDto = new SearchDto();
		RequestWrapper<SearchDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequest(searchDto);
		PageResponseDto<PolicyGroup> mockedResponse = new PageResponseDto<>();
		Mockito.when(policyManagementService.searchPolicyGroup(searchDto)).thenReturn(mockedResponse);
		ResponseWrapper<PageResponseDto<PolicyGroup>> response = policyManagementController.searchPolicyGroup(requestWrapper);
		assertNotNull(response);
		assertNotNull(response.getResponse());
		assertEquals(mockedResponse, response.getResponse());
		verify(audit).setAuditRequestDto(PolicyManageEnum.SEARCH_POLICY_GROUP);
	}
	@Test
	public void testGetPartnersPolicy() throws JsonParseException, JsonMappingException, IOException {
		String partnerId = "partnerId";
		String policyId = "policyId";
		PolicyResponseDto mockedResponseDto = new PolicyResponseDto();
		Mockito.when(policyManagementService.getPartnerMappedPolicy(partnerId, policyId)).thenReturn(mockedResponseDto);
		ResponseWrapper<PolicyResponseDto> response = policyManagementController.getPartnersPolicy(partnerId, policyId);
		assertNotNull(response);
		assertNotNull(response.getResponse());
		assertEquals(mockedResponseDto, response.getResponse());
	}
	@Test
	public void testSearchPolicy() {
		PolicySearchDto policySearchDto = new PolicySearchDto();
		RequestWrapper<PolicySearchDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequest(policySearchDto);
		PageResponseDto<SearchAuthPolicy> mockedResponse = new PageResponseDto<>();
		Mockito.when(policyManagementService.searchPolicy(policySearchDto)).thenReturn(mockedResponse);
		ResponseWrapper<PageResponseDto<SearchAuthPolicy>> response = policyManagementController.searchPolicy(requestWrapper);
		assertNotNull(response);
		assertNotNull(response.getResponse());
		assertEquals(mockedResponse, response.getResponse());
		verify(audit).setAuditRequestDto(PolicyManageEnum.SEARCH_POLICY);
	}
	@Test
	public void testGetValueForKey() {
		String key = "configKey";
		KeyValuePair<String, Object> mockedResponse = new KeyValuePair<>("aaa",122);
		Mockito.when(policyManagementService.getValueForKey(key)).thenReturn(mockedResponse);
		ResponseWrapper<KeyValuePair<String, Object>> response = policyManagementController.getValueForKey(key);
		assertNotNull(response);
		assertNotNull(response.getResponse());
		assertEquals(mockedResponse, response.getResponse());
	}
	@Test
	public void testPolicyGroupFilterValues() {
		FilterValueDto filterValueDto = new FilterValueDto();
		List<SearchFilter> optionalFilters=new ArrayList<>();
		filterValueDto.setOptionalFilters(optionalFilters);
		List <FilterDto> filters=new ArrayList<>();
		filterValueDto.setFilters(filters);
		RequestWrapper<FilterValueDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setRequest(filterValueDto);
		FilterResponseCodeDto mockedResponse = new FilterResponseCodeDto();
		Mockito.when(policyManagementService.policyGroupFilterValues(filterValueDto)).thenReturn(mockedResponse);
		ResponseWrapper<FilterResponseCodeDto> response = policyManagementController.policyGroupFilterValues(requestWrapper);
		assertNotNull(response);
		assertNotNull(response.getResponse());
		assertEquals(mockedResponse, response.getResponse());
		verify(audit).setAuditRequestDto(PolicyManageEnum.FILTERVALUES_POLICY_GROUP);
	}
	@Test
	public void testPolicyFilterValues() {
		PolicyFilterValueDto policyFilterValueDto = new PolicyFilterValueDto();
		policyFilterValueDto.setPolicyType("ptype");
		List <FilterDto> filters=new ArrayList<>();
		policyFilterValueDto.setFilters(filters);
		List<SearchFilter> optionalFilters=new ArrayList<>();
		policyFilterValueDto.setOptionalFilters(optionalFilters);
		RequestWrapper<PolicyFilterValueDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setMetadata("metadata");
		requestWrapper.setId("id");
		requestWrapper.setVersion("1.2.2");
		requestWrapper.setRequest(policyFilterValueDto);
		FilterResponseCodeDto mockedResponse = new FilterResponseCodeDto();
		Mockito.when(policyManagementService.policyFilterValues(policyFilterValueDto)).thenReturn(mockedResponse);
		ResponseWrapper<FilterResponseCodeDto> response = policyManagementController.policyFilterValues(requestWrapper);
		assertNotNull(response);
		assertNotNull(response.getResponse());
		assertEquals(mockedResponse, response.getResponse());
		verify(audit).setAuditRequestDto(PolicyManageEnum.FILTERVALUES_POLICY);
	}
	@Test
	public void testGetPoliciesByGroupName() {
		String groupName = "GroupName";
		List<PolicyDetailsDto> mockedResponse = new ArrayList<>();
		Mockito.when(policyManagementService.getActivePolicyDetailsByGroupName(groupName)).thenReturn(mockedResponse);
		ResponseWrapper<List<PolicyDetailsDto>> response = policyManagementController.getPoliciesByGroupName(groupName);
		assertNotNull(response);
		assertNotNull(response.getResponse());
		assertEquals(mockedResponse, response.getResponse());
	}
}
