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

import io.mosip.pms.partner.manager.controller.PartnerManagementController;
import lombok.SneakyThrows;
import org.junit.Before;
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
import org.springframework.test.context.TestPropertySource;
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
import io.mosip.pms.partner.manager.dto.StatusRequestDto;
import io.mosip.pms.partner.manager.dto.ApikeyRequests;
import io.mosip.pms.partner.manager.dto.PartnerAPIKeyToPolicyMappingsResponse;
import io.mosip.pms.partner.manager.dto.PartnersPolicyMappingRequest;
import io.mosip.pms.partner.manager.dto.PartnersPolicyMappingResponse;
import io.mosip.pms.partner.manager.dto.RetrievePartnerDetailsResponse;
import io.mosip.pms.partner.manager.service.PartnerManagerService;
import io.mosip.pms.partner.request.dto.APIkeyStatusUpdateRequestDto;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@EnableWebMvc
@TestPropertySource("classpath:application.properties")
public class PartnerManagementControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	PartnerManagerService partnerManagementService;

	@MockBean
	PartnerManagementController partnerMangementController;

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
		
		partnerMangementController.approveRejectPolicyMappings(request, "56789");
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
		
		partnerMangementController.activateDeactivatePartnerAPIKey("1234", "456", request);
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
		partnerMangementController.activateDeactivatePartnerAPIKey("1234", "456", requestWrapper);
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

}
