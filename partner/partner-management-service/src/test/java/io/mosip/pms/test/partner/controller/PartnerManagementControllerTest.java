package io.mosip.pms.test.partner.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.partner.manager.controller.PartnerManagementController;
import io.mosip.pms.partner.manager.dto.*;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


import io.mosip.pms.common.repository.AuthPolicyRepository;
import io.mosip.pms.common.repository.MispLicenseKeyRepository;
import io.mosip.pms.common.repository.PartnerPolicyRepository;
import io.mosip.pms.common.repository.PartnerPolicyRequestRepository;
import io.mosip.pms.common.repository.PartnerRepository;
import io.mosip.pms.common.repository.PolicyGroupRepository;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.partner.manager.constant.PartnerManageEnum;
import io.mosip.pms.partner.manager.service.PartnerManagerService;
import io.mosip.pms.partner.request.dto.APIkeyStatusUpdateRequestDto;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@EnableWebMvc
public class PartnerManagementControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	PartnerManagerService partnerManagementService;

	@Mock
	private MispLicenseKeyRepository misplKeyRepository;	
	
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
	
	@Autowired
	private ObjectMapper objectMapper;	
	
	@Autowired
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate restTemplate;
	
	@MockBean
	private AuditUtil audit;
	
	@Before
	public void setUp() {
		Mockito.doNothing().when(audit).setAuditRequestDto(Mockito.any(PartnerManageEnum.class));
	}
	
	@Test
	@WithMockUser(roles = {"PARTNERMANAGER"})
	public void partnerApiKeyToPolicyMappingsTest() throws Exception {
		String partnerID = "67899";
		String partnerAPIKey = "45678";

		PartnersPolicyMappingRequest partnersPolicyMappingRequest = new PartnersPolicyMappingRequest();
		partnersPolicyMappingRequest.setNewPolicyID("456789");
		partnersPolicyMappingRequest.setOldPolicyID("456789");
		RequestWrapper<PartnersPolicyMappingRequest> request = new RequestWrapper<PartnersPolicyMappingRequest>();
		request.setRequest(partnersPolicyMappingRequest);
		request.setId("mosip.partnermanagement.partners.policy.mapping");
		request.setVersion("1.0");
		request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
		request.setMetadata("{}");

		PartnersPolicyMappingResponse partnersPolicyMappingResponse = new PartnersPolicyMappingResponse();
		Mockito.when(partnerManagementService.updatePolicyAgainstApikey(partnersPolicyMappingRequest,partnerID,
				partnerAPIKey)).thenReturn(partnersPolicyMappingResponse);
		
		mockMvc.perform(put("/partners/67899/apikey/45678/policies").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
	}
	
	
	@Test
	@WithMockUser(roles = {"PARTNERMANAGER"})
	public void activateDeactivateAuthEKYCPartnersTest() throws Exception {
		String partnerID = "67899";
		PartnersPolicyMappingResponse partnersPolicyMappingResponse = new PartnersPolicyMappingResponse();
		StatusRequestDto activateDeactivatePartnerRequest = new StatusRequestDto();
		activateDeactivatePartnerRequest.setStatus("Active");
		Mockito.when(
				partnerManagementService.activateDeactivateAuthEKYCPartner(partnerID, activateDeactivatePartnerRequest))
				.thenReturn(partnersPolicyMappingResponse);
		RequestWrapper<StatusRequestDto> request = new RequestWrapper<StatusRequestDto>();
		request.setRequest(activateDeactivatePartnerRequest);
		request.setId("mosip.partnermanagement.partners.policy.mapping");
		request.setVersion("1.0");
		request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
		request.setMetadata("{}");

		mockMvc.perform(MockMvcRequestBuilders.patch("/partners/12345").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNERMANAGER"})
	public void getAllAuthEKYCPartnersForThePolicyGroupTest() throws Exception {
		RetrievePartnerDetailsResponse retrievePartnerDetailsResponse = new RetrievePartnerDetailsResponse();
		Mockito.when(partnerManagementService.getAllAuthEKYCPartnersForThePolicyGroup(Optional.empty()))
				.thenReturn(retrievePartnerDetailsResponse);
		mockMvc.perform(MockMvcRequestBuilders.get("/partners")).andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNERMANAGER"})
	public void getValidatPartnerAPIKeyPatternAndExpiryTest() throws Exception {
		String partnerID = "67899";
		String PartnerAPIKey = "56789";
		PartnerAPIKeyToPolicyMappingsResponse partnerAPIKeyToPolicyMappingsResponse = new PartnerAPIKeyToPolicyMappingsResponse();
		Mockito.when(partnerManagementService.getPartnerAPIKeyToPolicyMapping(partnerID, PartnerAPIKey))
				.thenReturn(partnerAPIKeyToPolicyMappingsResponse);
		mockMvc.perform(MockMvcRequestBuilders.get("/partners/67899/apikey/56789"))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	@WithMockUser(roles = {"PARTNERMANAGER"})
	public void getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagerTest() throws Exception {
		List<ApikeyRequests> apikeyRequests = new ArrayList<ApikeyRequests>();
		Mockito.when(partnerManagementService.getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagers())
				.thenReturn(apikeyRequests);
		mockMvc.perform(MockMvcRequestBuilders.get("/partners/apikey"))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	@WithMockUser(roles = {"PARTNERMANAGER"})
	public void getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestIdTest() throws Exception {
		String APIKeyReqID = "56789";
		ApikeyRequests apikeyRequests = new ApikeyRequests();
		Mockito.when(partnerManagementService.getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestId(APIKeyReqID))
				.thenReturn(apikeyRequests);
		mockMvc.perform(MockMvcRequestBuilders.get("/partners/apikey/56789"))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	@WithMockUser(roles = {"PARTNERMANAGER"})
	public void approveRejectPolicyMappings() throws JsonProcessingException, Exception{
		String mappingKey = "56789";
		StatusRequestDto requestDto = new StatusRequestDto();
		RequestWrapper<StatusRequestDto> request = new RequestWrapper<StatusRequestDto>();
		request.setRequest(requestDto);
		request.setId("mosip.partnermanagement.partners.policy.mapping");
		request.setVersion("1.0");
		request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
		request.setMetadata("{}");
		Mockito.when(partnerManagementService.approveRejectPartnerPolicyMapping(mappingKey,requestDto))
				.thenReturn("Success");
		
		mockMvc.perform(MockMvcRequestBuilders.put("/partners/policy/56789").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
	}
	
	@Test
	@WithMockUser(roles = {"PARTNERMANAGER"})
	public void activateDeactivatePartnerAPIKey() throws Exception {
		APIkeyStatusUpdateRequestDto requestDto = new APIkeyStatusUpdateRequestDto();
		RequestWrapper<APIkeyStatusUpdateRequestDto> request = new RequestWrapper<APIkeyStatusUpdateRequestDto>();
		request.setRequest(requestDto);
		request.setId("mosip.partnermanagement.partners.policy.mapping");
		request.setVersion("1.0");
		request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
		request.setMetadata("{}");
		Mockito.when(partnerManagementService.updateAPIKeyStatus("1234","456",requestDto))
				.thenReturn("Success");
		
		mockMvc.perform(MockMvcRequestBuilders.patch("/partners/1234/policy/456/apiKey/status").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test (expected = Exception.class)
	public void testGetPartnersDetails() {
		PartnerManagementController partnerManagementController = new PartnerManagementController();
		partnerManagementController.getPartnersDeatils(Optional.of("1234"));
	}

	@Test (expected = Exception.class)
	public void testApproveRejectPolicyMappings() {

		PartnerManagementController partnerManagementController = new PartnerManagementController();

		StatusRequestDto statusRequestDto = new StatusRequestDto();
		statusRequestDto.setStatus("Status");

		RequestWrapper<StatusRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setId("mosip.partnermanagement.partners.policy.mapping");
		requestWrapper.setMetadata("{}");
		requestWrapper.setRequest(statusRequestDto);
		requestWrapper.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
		requestWrapper.setVersion("1.0");
		partnerManagementController.approveRejectPolicyMappings(requestWrapper, "MappingKey");
	}

	@Test (expected = Exception.class)
	public void testApproveRejectPolicyMappings2() {

		PartnerManagementController partnerManagementController = new PartnerManagementController();

		StatusRequestDto statusRequestDto = new StatusRequestDto();
		statusRequestDto.setStatus("Status");
		RequestWrapper<StatusRequestDto> requestWrapper = (RequestWrapper<StatusRequestDto>) mock(RequestWrapper.class);
		when(requestWrapper.getId()).thenReturn("mosip.partnermanagement.partners.policy.mapping");
		when(requestWrapper.getVersion()).thenReturn("1.0");
		doNothing().when(requestWrapper).setId((String) any());
		doNothing().when(requestWrapper).setMetadata((Object) any());
		doNothing().when(requestWrapper).setRequest((StatusRequestDto) any());
		doNothing().when(requestWrapper).setRequesttime((LocalDateTime) any());
		doNothing().when(requestWrapper).setVersion((String) any());
		requestWrapper.setId("mosip.partnermanagement.partners.policy.mapping");
		requestWrapper.setMetadata("{}");
		requestWrapper.setRequest(statusRequestDto);
		requestWrapper.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
		requestWrapper.setVersion("1.0");
		partnerManagementController.approveRejectPolicyMappings(requestWrapper, "MappingKey");
	}

	@SneakyThrows
	@Test
	public void testActivateDeactivatePartnerAPIKey() {
		PartnerManagementController partnerManagementController = new PartnerManagementController();

		APIkeyStatusUpdateRequestDto apIkeyStatusUpdateRequestDto = new APIkeyStatusUpdateRequestDto();
		apIkeyStatusUpdateRequestDto.setLabel("Label");
		apIkeyStatusUpdateRequestDto.setStatus("Status");

		RequestWrapper<APIkeyStatusUpdateRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setId("mosip.partnermanagement.partners.policy.mapping");
		requestWrapper.setMetadata("{}");
		requestWrapper.setRequest(apIkeyStatusUpdateRequestDto);
		requestWrapper.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
		requestWrapper.setVersion("1.0");
		Mockito.when(partnerManagementService.updateAPIKeyStatus("1234","456",apIkeyStatusUpdateRequestDto))
				.thenReturn("Success");
		mockMvc.perform(MockMvcRequestBuilders.patch("/partners/1234/policy/456/apiKey/status").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(requestWrapper))).andExpect(MockMvcResultMatchers.status().isUnauthorized());
	}

	@SneakyThrows
	@Test (expected = Exception.class)
	public void testActivateDeactivatePartnerAPIKey2() {

		PartnerManagementController partnerManagementController = new PartnerManagementController();

		APIkeyStatusUpdateRequestDto apIkeyStatusUpdateRequestDto = new APIkeyStatusUpdateRequestDto();
		apIkeyStatusUpdateRequestDto.setLabel("Label");
		apIkeyStatusUpdateRequestDto.setStatus("Status");
		RequestWrapper<APIkeyStatusUpdateRequestDto> requestWrapper = (RequestWrapper<APIkeyStatusUpdateRequestDto>) mock(RequestWrapper.class);
		when(requestWrapper.getId()).thenReturn("id");
		when(requestWrapper.getVersion()).thenReturn("1.0");
		doNothing().when(requestWrapper).setId((String) any());
		doNothing().when(requestWrapper).setMetadata((Object) any());
		doNothing().when(requestWrapper).setRequest((APIkeyStatusUpdateRequestDto) any());
		doNothing().when(requestWrapper).setRequesttime((LocalDateTime) any());
		doNothing().when(requestWrapper).setVersion((String) any());
		requestWrapper.setId("mosip.partnermanagement.partners.policy.mapping");
		requestWrapper.setMetadata("{}");
		requestWrapper.setRequest(apIkeyStatusUpdateRequestDto);
		requestWrapper.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
		requestWrapper.setVersion("1.0");
		Mockito.when(partnerManagementService.updateAPIKeyStatus("1234","456",apIkeyStatusUpdateRequestDto))
				.thenReturn("Success");

		partnerManagementController.activateDeactivatePartnerAPIKey("1234", "456", requestWrapper);

		mockMvc.perform(MockMvcRequestBuilders.patch("/partners/1234/policy/456/apiKey/status").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(requestWrapper))).andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test (expected = Exception.class)
	public void testActivateDeactivatePartnerAPIKey3() {

		PartnerManagementController partnerManagementController = new PartnerManagementController();

		APIkeyStatusUpdateRequestDto apIkeyStatusUpdateRequestDto = new APIkeyStatusUpdateRequestDto();
		apIkeyStatusUpdateRequestDto.setLabel("Label");
		apIkeyStatusUpdateRequestDto.setStatus("Status");

		RequestWrapper<APIkeyStatusUpdateRequestDto> requestWrapper = new RequestWrapper<>();
		requestWrapper.setId("mosip.partnermanagement.partners.policy.mapping");
		requestWrapper.setMetadata("{}");
		requestWrapper.setRequest(apIkeyStatusUpdateRequestDto);
		requestWrapper.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
		requestWrapper.setVersion("1.0");
		partnerManagementController.activateDeactivatePartnerAPIKey("123", "456", requestWrapper);

	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void getPartnerDetailsTest() throws Exception {
		ResponseWrapperV2<PartnerDetailsV3Dto> responseWrapper = new ResponseWrapperV2<>();

		Mockito.when(partnerManagementService.getPartnerDetails(anyString()))
				.thenReturn(responseWrapper);

		mockMvc.perform(MockMvcRequestBuilders.get("/partners/{partnerId}/v2", "samplePartnerId")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void getAllPartnersTest() throws Exception {
		String sortFieldName = "createdDateTime";
		String sortType = "desc";
		int pageNo = 0;
		int pageSize = 8;
		FilterDto filterDto = new FilterDto();
		filterDto.setPartnerId("abc");
		filterDto.setPartnerTypeCode("Auth_Partner");
		filterDto.setOrganizationName("ABC");
		filterDto.setEmailAddress("abc");
		filterDto.setCertificateUploadStatus("not_uploaded");
		filterDto.setPolicyGroupName("default");
		filterDto.setIsActive(false);
		ResponseWrapperV2<PageResponseV2Dto<PartnerSummaryDto>> responseWrapper = new ResponseWrapperV2<>();

		Mockito.when(partnerManagementService.getAllPartners(sortFieldName, sortType, pageNo, pageSize, filterDto))
				.thenReturn(responseWrapper);
		mockMvc.perform(MockMvcRequestBuilders.get("/partners/v3?sortFieldName=createdDateTime&sortType=desc&pageSize=8&pageNo=0&" +
						"partnerId=abc&partnerType=Auth_Partner&orgName=ABC&emailAddress=abc&certificateUploadStatus=not_uploaded&policyGroupName=default&isActive=false"))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

}
