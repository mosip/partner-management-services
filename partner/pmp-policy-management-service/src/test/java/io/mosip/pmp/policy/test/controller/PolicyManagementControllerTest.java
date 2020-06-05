package io.mosip.pmp.policy.test.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pmp.policy.dto.AllowedKycDto;
import io.mosip.pmp.policy.dto.AuthPolicyDto;
import io.mosip.pmp.policy.dto.PolicyCreateRequestDto;
import io.mosip.pmp.policy.dto.PolicyCreateResponseDto;
import io.mosip.pmp.policy.dto.PolicyDto;
import io.mosip.pmp.policy.dto.PolicyStatusUpdateRequestDto;
import io.mosip.pmp.policy.dto.PolicyStatusUpdateResponseDto;
import io.mosip.pmp.policy.dto.PolicyUpdateRequestDto;
import io.mosip.pmp.policy.dto.PolicyUpdateResponseDto;
import io.mosip.pmp.policy.dto.PolicyWithAuthPolicyDto;
import io.mosip.pmp.policy.dto.RequestWrapper;
import io.mosip.pmp.policy.dto.ResponseWrapper;
import io.mosip.pmp.policy.errorMessages.PolicyManagementServiceException;
import io.mosip.pmp.policy.service.PolicyManagementService;
import io.mosip.pmp.policy.test.PolicyServiceTest;


/**
 * @author Nagarjuna Kuchi
 *
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PolicyServiceTest.class)
@AutoConfigureMockMvc
@EnableWebMvc
public class PolicyManagementControllerTest {
	
	@Autowired
    private MockMvc mockMvc;
	
	@MockBean
	private PolicyManagementService policyManagementService;
	
	@Autowired
	private ObjectMapper objectMapper;	
	
	@Test
	@WithMockUser(roles = {"POLICYMANAGER"})
	public void policyGroupCreationTest() throws PolicyManagementServiceException, Exception{
		ResponseWrapper<PolicyCreateResponseDto> response = new ResponseWrapper<PolicyCreateResponseDto>();
		Mockito.when(policyManagementService.createPolicyGroup(Mockito.any())).thenReturn(response);
		RequestWrapper<PolicyCreateRequestDto> request = createRequest();
		
		mockMvc.perform(post("/policies").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	@WithMockUser(roles = {"POLICYMANAGER"})
	public void updatePolicyTest() throws Exception{
		ResponseWrapper<PolicyUpdateResponseDto> response = new ResponseWrapper<PolicyUpdateResponseDto>();
		Mockito.when(policyManagementService.update(Mockito.any())).thenReturn(response);
		RequestWrapper<PolicyUpdateRequestDto> request = createPolicyUpdateRequest();
		
		mockMvc.perform(put("/policies/12345").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	@WithMockUser(roles = {"POLICYMANAGER"})
	public void updatePolicyStatus() throws JsonProcessingException, Exception{
		ResponseWrapper<PolicyStatusUpdateResponseDto> response = new ResponseWrapper<>();
		Mockito.when(policyManagementService.updatePolicyStatus(Mockito.any())).thenReturn(response);
		RequestWrapper<PolicyStatusUpdateRequestDto> request = createPolicyStatusUpateRequest();
		
		mockMvc.perform(MockMvcRequestBuilders.patch("/policies/12345").contentType(MediaType.APPLICATION_JSON_VALUE)
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
		PolicyWithAuthPolicyDto response = new PolicyWithAuthPolicyDto();
		Mockito.when(policyManagementService.findPolicy(Mockito.any())).thenReturn(response);
		
		mockMvc.perform(MockMvcRequestBuilders.get("/policies/policyId/12345")).
		andExpect(MockMvcResultMatchers.status().isOk());		
	}
	
	@Test
	@WithMockUser(roles = {"POLICYMANAGER"})
	public void getPolicyWithApiKeyTest() throws Exception{
		PolicyWithAuthPolicyDto response = new PolicyWithAuthPolicyDto();
		Mockito.when(policyManagementService.findPolicy(Mockito.any())).thenReturn(response);		
		mockMvc.perform(MockMvcRequestBuilders.get("/policies/partnerApiKey/12345")).
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
		request.setId("12345");
		request.setStatus("De-Active");
		return request;
	}

	private RequestWrapper<PolicyUpdateRequestDto> createPolicyUpdateRequest() {
		RequestWrapper<PolicyUpdateRequestDto> request = new RequestWrapper<PolicyUpdateRequestDto>();
		request.setRequest(createUpdatePolicyRequest());
        request.setId("mosip.partnermanagement.policies.authPolicies.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
	}

	private PolicyUpdateRequestDto createUpdatePolicyRequest() {
		PolicyUpdateRequestDto request = new PolicyUpdateRequestDto();
		request.setPolicies(createAuthPolicyInput());
		request.setDesc("Policy desc Updated.");
		request.setName("Updated Policy Name");
		request.setId("12345");		
		return request;
	}

	private PolicyDto createAuthPolicyInput() {
		PolicyDto policy = new PolicyDto();
		policy.setAllowedKycAttributes(getAllowedKycAttributes());
		policy.setAuthPolicies(getAuthPolicies());
		return policy;
	}

	private List<AuthPolicyDto> getAuthPolicies() {
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

	private RequestWrapper<PolicyCreateRequestDto> createRequest() {
		RequestWrapper<PolicyCreateRequestDto> request = new RequestWrapper<PolicyCreateRequestDto>();
		request.setRequest(createPolicyRequest());
        request.setId("mosip.partnermanagement.policies.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
	}

	private PolicyCreateRequestDto createPolicyRequest() {
		PolicyCreateRequestDto requestDto = new PolicyCreateRequestDto();
		requestDto.setName("PolicyName");
		requestDto.setDesc("PolicyDesc");
		return requestDto;
	}

}
