package io.mosip.pms.test.partner.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.dto.TrustCertificateSummaryDto;
import io.mosip.pms.common.request.dto.RequestWrapperV2;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.common.util.RequestValidator;
import io.mosip.pms.partner.manager.controller.PartnerManagementController;
import io.mosip.pms.partner.manager.dto.*;
import io.mosip.pms.partner.manager.service.impl.PartnerManagementServiceImpl;
import io.mosip.pms.partner.request.dto.BioextractorConfigurationRequestDto;
import io.mosip.pms.partner.request.dto.LinkPolicyGroupRequestDto;
import io.mosip.pms.partner.request.dto.LinkPolicyGroupResponseDto;
import io.mosip.pms.partner.response.dto.BioextractorConfigurationDetailDto;
import io.mosip.pms.partner.response.dto.BioextractorConfigurationResponseDto;
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

	@MockBean
	RequestValidator requestValidator;
	
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
		requestDto.setStatus("status");
		RequestWrapper<StatusRequestDto> request = new RequestWrapper<StatusRequestDto>();
		request.setRequest(requestDto);
		request.setId("mosip.partnermanagement.partners.policy.mapping");
		request.setVersion("1.0");
		request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
		request.setMetadata("{}");
		Mockito.when(partnerManagementService.approveRejectPartnerPolicyMapping(any(), any()))
				.thenReturn("Success");
		
		mockMvc.perform(MockMvcRequestBuilders.put("/partners/policy/56789").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
	}
	
	@Test
	@WithMockUser(roles = {"PARTNERMANAGER"})
	public void activateDeactivatePartnerAPIKey() throws Exception {
		APIkeyStatusUpdateRequestDto requestDto = new APIkeyStatusUpdateRequestDto();
		requestDto.setLabel("label");
		requestDto.setStatus("status");
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

	@Test
	public void testActivateDeactivatePartnerAPIKey() throws Exception {

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
				.content(objectMapper.writeValueAsString(requestWrapper))).andExpect(MockMvcResultMatchers.status().isOk());
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

		mockMvc.perform(MockMvcRequestBuilders.get("/admin-partners/{partnerId}", "samplePartnerId")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void getAllPartnersTest() throws Exception {
		String sortFieldName = "createdDateTime";
		String sortType = "desc";
		Integer pageNo = 0;
		Integer pageSize = 8;
		PartnerFilterDto partnerFilterDto = new PartnerFilterDto();
		partnerFilterDto.setPartnerId("abc");
		partnerFilterDto.setPartnerTypeCode("Auth_Partner");
		partnerFilterDto.setOrganizationName("ABC");
		partnerFilterDto.setEmailAddress("abc");
		partnerFilterDto.setCertificateUploadStatus("not_uploaded");
		partnerFilterDto.setPolicyGroupName("default");
		partnerFilterDto.setIsActive(false);
		ResponseWrapperV2<PageResponseV2Dto<PartnerSummaryDto>> responseWrapper = new ResponseWrapperV2<>();

		Mockito.when(partnerManagementService.getAdminPartners(sortFieldName, sortType, pageNo, pageSize, partnerFilterDto))
				.thenReturn(responseWrapper);
		mockMvc.perform(MockMvcRequestBuilders.get("/admin-partners?sortFieldName=createdDateTime&sortType=desc&pageSize=8&pageNo=0&" +
						"partnerId=abc&partnerType=Auth_Partner&orgName=ABC&emailAddress=abc&certificateUploadStatus=not_uploaded&policyGroupName=default&isActive=false"))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void getAllPartners_NoFiltersTest() throws Exception {
		String sortFieldName = "createdDateTime";
		String sortType = "desc";
		Integer pageNo = 0;
		Integer pageSize = 8;
		PartnerFilterDto partnerFilterDto = new PartnerFilterDto();
		partnerFilterDto.setPartnerId("abc");
		partnerFilterDto.setPartnerTypeCode("Auth_Partner");
		partnerFilterDto.setOrganizationName("ABC");
		partnerFilterDto.setEmailAddress("abc");
		partnerFilterDto.setCertificateUploadStatus("not_uploaded");
		partnerFilterDto.setPolicyGroupName("default");
		partnerFilterDto.setIsActive(false);
		ResponseWrapperV2<PageResponseV2Dto<PartnerSummaryDto>> responseWrapper = new ResponseWrapperV2<>();

		Mockito.when(partnerManagementService.getAdminPartners(sortFieldName, sortType, pageNo, pageSize, partnerFilterDto))
				.thenReturn(responseWrapper);
		mockMvc.perform(MockMvcRequestBuilders.get("/admin-partners?sortFieldName=createdDateTime&sortType=desc&pageSize=8&pageNo=0&"))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void getAllApiKeyRequestsTest() throws Exception {
		String sortFieldName = "createdDateTime";
		String sortType = "desc";
		Integer pageNo = 0;
		Integer pageSize = 8;
		ApiKeyFilterDto apiKeyFilterDto = new ApiKeyFilterDto();
		ResponseWrapperV2<PageResponseV2Dto<ApiKeyRequestSummaryDto>> responseWrapper = new ResponseWrapperV2<>();

		Mockito.when(partnerManagementService.getAllApiKeyRequests(sortFieldName, sortType, pageNo, pageSize, apiKeyFilterDto))
				.thenReturn(responseWrapper);
		mockMvc.perform(MockMvcRequestBuilders.get("/partner-api-keys")
						.param("sortFieldName", sortFieldName)
						.param("sortType", sortType)
						.param("pageNo", String.valueOf(pageNo))
						.param("pageSize", String.valueOf(pageSize))
						.param("partnerId", "123")
						.param("apiKeyLabel", "label")
						.param("orgName", "ABC")
						.param("status", "approved")
						.param("policyName", "policy name")
						.param("policyGroupName", "policy group"))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void getAllApiKeyRequests_NoFiltersTest() throws Exception {
		String sortFieldName = "createdDateTime";
		String sortType = "desc";
		Integer pageNo = 0;
		Integer pageSize = 8;
		ApiKeyFilterDto apiKeyFilterDto = new ApiKeyFilterDto();
		ResponseWrapperV2<PageResponseV2Dto<ApiKeyRequestSummaryDto>> responseWrapper = new ResponseWrapperV2<>();

		Mockito.when(partnerManagementService.getAllApiKeyRequests(sortFieldName, sortType, pageNo, pageSize, apiKeyFilterDto))
				.thenReturn(responseWrapper);
		mockMvc.perform(MockMvcRequestBuilders.get("/partner-api-keys")
						.param("sortFieldName", sortFieldName)
						.param("sortType", sortType)
						.param("pageNo", String.valueOf(pageNo))
						.param("pageSize", String.valueOf(pageSize)))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void getAllApiKeyRequestsV2Test() throws Exception {
		String sortFieldName = "createdDateTime";
		String sortType = "desc";
		Integer pageNo = 0;
		Integer pageSize = 8;
		ApiKeyFilterDto apiKeyFilterDto = new ApiKeyFilterDto();
		ResponseWrapperV2<PageResponseV2Dto<ApiKeyRequestSummaryV2Dto>> responseWrapper = new ResponseWrapperV2<>();

		Mockito.when(partnerManagementService.getAllApiKeyRequestsV2(sortFieldName, sortType, pageNo, pageSize, apiKeyFilterDto))
				.thenReturn(responseWrapper);
		mockMvc.perform(MockMvcRequestBuilders.get("/partner-api-keys/v2")
						.param("sortFieldName", sortFieldName)
						.param("sortType", sortType)
						.param("pageNo", String.valueOf(pageNo))
						.param("pageSize", String.valueOf(pageSize))
						.param("partnerId", "123")
						.param("apiKeyLabel", "label")
						.param("orgName", "ABC")
						.param("status", "approved")
						.param("policyName", "policy name")
						.param("policyGroupName", "policy group")
						.param("partnerType", "Auth_Partner"))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void getAllApiKeyRequestsV2_NoFiltersTest() throws Exception {
		String sortFieldName = "createdDateTime";
		String sortType = "desc";
		Integer pageNo = 0;
		Integer pageSize = 8;
		ApiKeyFilterDto apiKeyFilterDto = new ApiKeyFilterDto();
		ResponseWrapperV2<PageResponseV2Dto<ApiKeyRequestSummaryV2Dto>> responseWrapper = new ResponseWrapperV2<>();

		Mockito.when(partnerManagementService.getAllApiKeyRequestsV2(sortFieldName, sortType, pageNo, pageSize, apiKeyFilterDto))
				.thenReturn(responseWrapper);
		mockMvc.perform(MockMvcRequestBuilders.get("/partner-api-keys/v2")
						.param("sortFieldName", sortFieldName)
						.param("sortType", sortType)
						.param("pageNo", String.valueOf(pageNo))
						.param("pageSize", String.valueOf(pageSize)))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}


	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void getAllPartnerPolicyRequestsTest() throws Exception {
		String sortFieldName = "createdDateTime";
		String sortType = "desc";
		Integer pageNo = 0;
		Integer pageSize = 8;
		String partnerId = "123";
		String partnerComment = "Request details";
		String orgName = "ABC";
		String status = "approved";
		String policyId = "policy-123";
		String policyName = "Sample Policy";
		String policyGroupName = "Default Group";
		String partnerTypeCode = "Auth_Partner";
		String partnerType = "partnerType";
		PartnerPolicyRequestFilterDto partnerPolicyRequestFilterDto = new PartnerPolicyRequestFilterDto();
		ResponseWrapperV2<PageResponseV2Dto<PartnerPolicyRequestSummaryDto>> responseWrapper = new ResponseWrapperV2<>();

		Mockito.when(partnerManagementService.getAllPartnerPolicyRequests(sortFieldName, sortType, pageNo, pageSize, partnerPolicyRequestFilterDto))
				.thenReturn(responseWrapper);
		mockMvc.perform(MockMvcRequestBuilders.get("/partner-policy-requests")
						.param("sortFieldName", sortFieldName)
						.param("sortType", sortType)
						.param("pageNo", String.valueOf(pageNo))
						.param("pageSize", String.valueOf(pageSize))
						.param("partnerId", partnerId)
						.param("partnerComment", partnerComment)
						.param("orgName", orgName)
						.param("status", status)
						.param("policyId", policyId)
						.param("policyName", policyName)
						.param("policyGroupName", policyGroupName)
						.param("partnerType", partnerType)
						.param("partnerTypeCode", partnerTypeCode))
				.andExpect(MockMvcResultMatchers.status().isOk());

		Mockito.when(partnerManagementService.getAllPartnerPolicyRequests(sortFieldName, sortType, pageNo, pageSize, partnerPolicyRequestFilterDto))
				.thenReturn(responseWrapper);
		mockMvc.perform(MockMvcRequestBuilders.get("/partner-policy-requests")
						.param("sortFieldName", sortFieldName)
						.param("sortType", sortType)
						.param("pageNo", String.valueOf(pageNo))
						.param("pageSize", String.valueOf(pageSize))
						.param("partnerId", partnerId)
						.param("partnerIdSearchType", "equals"))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void getAllPartnerPolicyRequests_NoFiltersTest() throws Exception {
		String sortFieldName = "createdDateTime";
		String sortType = "desc";
		Integer pageNo = 0;
		Integer pageSize = 8;
		String partnerId = "123";
		String partnerComment = "Request details";
		String orgName = "ABC";
		String status = "approved";
		String policyId = "policy-123";
		String policyName = "Sample Policy";
		String policyGroupName = "Default Group";
		String partnerTypeCode = "Auth_Partner";
		PartnerPolicyRequestFilterDto partnerPolicyRequestFilterDto = new PartnerPolicyRequestFilterDto();
		ResponseWrapperV2<PageResponseV2Dto<PartnerPolicyRequestSummaryDto>> responseWrapper = new ResponseWrapperV2<>();

		Mockito.when(partnerManagementService.getAllPartnerPolicyRequests(sortFieldName, sortType, pageNo, pageSize, partnerPolicyRequestFilterDto))
				.thenReturn(responseWrapper);
		mockMvc.perform(MockMvcRequestBuilders.get("/partner-policy-requests")
						.param("sortFieldName", sortFieldName)
						.param("sortType", sortType)
						.param("pageNo", String.valueOf(pageNo))
						.param("pageSize", String.valueOf(pageSize)))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void getCaCertificatesTest() throws Exception {
		String sortFieldName = "caCertificateType";
		String sortType = "desc";
		Integer pageNo = 0;
		Integer pageSize = 8;
		TrustCertificateFilterDto filterDto = new TrustCertificateFilterDto();
		ResponseWrapperV2<PageResponseV2Dto<TrustCertificateSummaryDto>> responseWrapper = new ResponseWrapperV2<>();

		Mockito.when(partnerManagementService.getTrustCertificates(sortFieldName, sortType, pageNo, pageSize, filterDto))
				.thenReturn(responseWrapper);
		mockMvc.perform(MockMvcRequestBuilders.get("/trust-chain-certificates")
						.param("sortFieldName", sortFieldName)
						.param("sortType", sortType)
						.param("pageNo", String.valueOf(pageNo))
						.param("pageSize", String.valueOf(pageSize))
						.param("caCertificateType", "root")
						.param("certificateId", "123")
						.param("partnerDomain", "FTM")
						.param("expiryPeriod", "30")
						.param("issuedTo", "CA")
						.param("issuedBy", "CA"))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void getCaCertificates_NoFiltersTest() throws Exception {
		String sortFieldName = "caCertificateType";
		String sortType = "desc";
		Integer pageNo = 0;
		Integer pageSize = 8;
		TrustCertificateFilterDto filterDto = new TrustCertificateFilterDto();
		ResponseWrapperV2<PageResponseV2Dto<TrustCertificateSummaryDto>> responseWrapper = new ResponseWrapperV2<>();

		Mockito.when(partnerManagementService.getTrustCertificates(sortFieldName, sortType, pageNo, pageSize, filterDto))
				.thenReturn(responseWrapper);
		mockMvc.perform(MockMvcRequestBuilders.get("/trust-chain-certificates")
						.param("sortFieldName", sortFieldName)
						.param("sortType", sortType)
						.param("pageNo", String.valueOf(pageNo))
						.param("pageSize", String.valueOf(pageSize)))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void downloadRootCertificateTest() throws Exception {
		ResponseWrapperV2<TrustCertificateResponseDto> responseWrapper = new ResponseWrapperV2<>();
		Mockito.when(partnerManagementService.downloadTrustCertificates(anyString())).thenReturn(responseWrapper);
		mockMvc.perform(MockMvcRequestBuilders.get("/trust-chain-certificates/{certificateId}/certificateFile", "123")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void downloadRootCertificateTest_InvalidCertificateId() throws Exception {
		ResponseWrapperV2<TrustCertificateResponseDto> responseWrapper = new ResponseWrapperV2<>();
		Mockito.when(partnerManagementService.downloadTrustCertificates(anyString())).thenReturn(responseWrapper);
		mockMvc.perform(MockMvcRequestBuilders.get("/trust-chain-certificates/{certificateId}/certificateFile", "123_invalid")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	private LinkPolicyGroupRequestDto getLinkPolicyGroupRequestDto() {
		LinkPolicyGroupRequestDto requestDto = new LinkPolicyGroupRequestDto();
		requestDto.setPolicyGroupId("group1");
		return requestDto;
	}

	private RequestWrapperV2<LinkPolicyGroupRequestDto> getRequestWrapper() {
		RequestWrapperV2<LinkPolicyGroupRequestDto> request = new RequestWrapperV2<>();
		request.setId("mosip.pms.link.policy.group.post");
		request.setVersion("1.0");
		request.setRequest(getLinkPolicyGroupRequestDto());
		return request;
	}

	private ResponseWrapperV2<LinkPolicyGroupResponseDto> getResponseWrapper() {
		ResponseWrapperV2<LinkPolicyGroupResponseDto> response = new ResponseWrapperV2<>();
		response.setId("mosip.pms.link.policy.group.post");
		response.setVersion("1.0");
		LinkPolicyGroupResponseDto responseDto = new LinkPolicyGroupResponseDto();
		responseDto.setPartnerId("123");
		responseDto.setPolicyGroupId("group1");
		responseDto.setPolicyGroupName("Default Group");
		response.setResponse(responseDto);
		return response;
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void linkPolicyGroupTest() throws Exception {
		ResponseWrapperV2<Object> errorResponseWrapper = new ResponseWrapperV2<>();
		Mockito.when(requestValidator.validate(any(), any())).thenReturn(Optional.of(errorResponseWrapper));
		Mockito.when(partnerManagementService.linkPolicyGroup(anyString(), any())).thenReturn(getResponseWrapper());
		mockMvc.perform(MockMvcRequestBuilders.post("/123/policy-group").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(getRequestWrapper()))).andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void linkPolicyGroupTest_InvalidRequest() throws Exception {
		Mockito.when(requestValidator.validate(any(), any())).thenReturn(Optional.empty());
		Mockito.when(partnerManagementService.linkPolicyGroup(anyString(), any())).thenReturn(getResponseWrapper());
		mockMvc.perform(MockMvcRequestBuilders.post("/123/policy-group").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(getRequestWrapper()))).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void updateAPIKeyExpiryTestValidationPresent() throws Exception {
		RequestWrapperV2<io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto> requestWrapper = new RequestWrapperV2<>();
		io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto requestDto = new io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto();
		requestDto.setExpiryDateTime(java.time.OffsetDateTime.now().plusDays(30));
		requestWrapper.setRequest(requestDto);

		ResponseWrapperV2<io.mosip.pms.partner.response.dto.APIKeyUpdateResponseDto> errorResponse = new ResponseWrapperV2<>();
		Mockito.doReturn(Optional.of(errorResponse)).when(requestValidator).validate(anyString(), any());

		mockMvc.perform(MockMvcRequestBuilders.patch("/partners/partner123/policies/policy456/api-keys/apiKeyName123")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(requestWrapper)))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void updateAPIKeyExpiryTestSuccess() throws Exception {
		RequestWrapperV2<io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto> requestWrapper = new RequestWrapperV2<>();
		io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto requestDto = new io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto();
		requestDto.setExpiryDateTime(java.time.OffsetDateTime.now().plusDays(30));
		requestWrapper.setRequest(requestDto);

		ResponseWrapperV2<io.mosip.pms.partner.response.dto.APIKeyUpdateResponseDto> response = new ResponseWrapperV2<>();
		Mockito.doReturn(Optional.empty()).when(requestValidator).validate(anyString(), any());
		Mockito.when(partnerManagementService.updateAPIKey(anyString(), anyString(), anyString(), any())).thenReturn(response);

		mockMvc.perform(MockMvcRequestBuilders.patch("/partners/partner123/policies/policy456/api-keys/apiKeyName123")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(requestWrapper)))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void updateAPIKeyDeactivateTestSuccess() throws Exception {
		RequestWrapperV2<io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto> requestWrapper = new RequestWrapperV2<>();
		io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto requestDto = new io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto();
		requestDto.setStatus("De-active");
		requestWrapper.setRequest(requestDto);
		requestWrapper.setId("mosip.pms.update.api.key.patch");
		requestWrapper.setVersion("1.0");

		ResponseWrapperV2<io.mosip.pms.partner.response.dto.APIKeyUpdateResponseDto> response = new ResponseWrapperV2<>();
		Mockito.doReturn(Optional.empty()).when(requestValidator).validate(anyString(), any());
		Mockito.when(partnerManagementService.updateAPIKey(anyString(), anyString(), anyString(), any())).thenReturn(response);

		mockMvc.perform(MockMvcRequestBuilders.patch("/partners/partner123/policies/policy456/api-keys/apiKeyName123")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(requestWrapper)))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void updateAPIKeyBothFieldsTestSuccess() throws Exception {
		RequestWrapperV2<io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto> requestWrapper = new RequestWrapperV2<>();
		io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto requestDto = new io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto();
		requestDto.setStatus("De-active");
		requestDto.setExpiryDateTime(java.time.OffsetDateTime.now().plusDays(30));
		requestWrapper.setRequest(requestDto);

		ResponseWrapperV2<io.mosip.pms.partner.response.dto.APIKeyUpdateResponseDto> response = new ResponseWrapperV2<>();
		Mockito.doReturn(Optional.empty()).when(requestValidator).validate(anyString(), any());
		Mockito.when(partnerManagementService.updateAPIKey(anyString(), anyString(), anyString(), any())).thenReturn(response);

		mockMvc.perform(MockMvcRequestBuilders.patch("/partners/partner123/policies/policy456/api-keys/apiKeyName123")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(requestWrapper)))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = {"AUTH_PARTNER"})
	public void updateAPIKeyWithAuthPartnerRoleTest() throws Exception {
		RequestWrapperV2<io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto> requestWrapper = new RequestWrapperV2<>();
		io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto requestDto = new io.mosip.pms.partner.request.dto.APIKeyUpdateRequestDto();
		requestDto.setStatus("De-active");
		requestWrapper.setRequest(requestDto);

		ResponseWrapperV2<io.mosip.pms.partner.response.dto.APIKeyUpdateResponseDto> response = new ResponseWrapperV2<>();
		Mockito.doReturn(Optional.empty()).when(requestValidator).validate(anyString(), any());
		Mockito.when(partnerManagementService.updateAPIKey(anyString(), anyString(), anyString(), any())).thenReturn(response);

		mockMvc.perform(MockMvcRequestBuilders.patch("/partners/partner123/policies/policy456/api-keys/apiKeyName123")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(requestWrapper)))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNERMANAGER"})
	public void getPartnersDeatilsTest() throws Exception {
		PartnerDetailsResponse partnerDetailsResponse = new PartnerDetailsResponse();
		Mockito.when(partnerManagementService.getPartners(any())).thenReturn(partnerDetailsResponse);
		mockMvc.perform(MockMvcRequestBuilders.get("/partners/v2")
				.param("partnerType", "Auth_Partner"))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNERMANAGER"})
	public void getPartnersDeatilsTestWithoutPartnerType() throws Exception {
		PartnerDetailsResponse partnerDetailsResponse = new PartnerDetailsResponse();
		Mockito.when(partnerManagementService.getPartners(Optional.empty())).thenReturn(partnerDetailsResponse);
		mockMvc.perform(MockMvcRequestBuilders.get("/partners/v2"))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNERMANAGER"})
	public void getPartnersDeatilsTestResponseValidation() throws Exception {
		PartnerDetailsResponse partnerDetailsResponse = new PartnerDetailsResponse();
		Mockito.when(partnerManagementService.getPartners(Optional.of("Auth_Partner"))).thenReturn(partnerDetailsResponse);
		mockMvc.perform(MockMvcRequestBuilders.get("/partners/v2")
				.param("partnerType", "Auth_Partner"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.id").value("mosip.partnermanagement.partners.retrieve"))
				.andExpect(MockMvcResultMatchers.jsonPath("$.version").value("1.0"));
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void createBioextractorConfigurationSuccessTest() throws Exception {
		ResponseWrapperV2<BioextractorConfigurationResponseDto> responseWrapper = new ResponseWrapperV2<>();
		BioextractorConfigurationResponseDto responseDto = new BioextractorConfigurationResponseDto();
		responseDto.setId("123456");
		responseDto.setStatus("SUCCESS");
		responseWrapper.setResponse(responseDto);

		Mockito.doReturn(Optional.empty()).when(requestValidator).validate(anyString(), any());
		Mockito.when(partnerManagementService.createBioextractorConfiguration(any())).thenReturn(responseWrapper);

		mockMvc.perform(post("/bio-extractor-configurations")
						.contentType(MediaType.APPLICATION_JSON_VALUE)
						.content(objectMapper.writeValueAsString(buildBioextractorConfigRequestWrapper())))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void createBioextractorConfigurationValidationFailTest() throws Exception {
		ResponseWrapperV2<BioextractorConfigurationResponseDto> errorWrapper = new ResponseWrapperV2<>();
		Mockito.doReturn(Optional.of(errorWrapper)).when(requestValidator).validate(anyString(), any());

		mockMvc.perform(post("/bio-extractor-configurations")
						.contentType(MediaType.APPLICATION_JSON_VALUE)
						.content(objectMapper.writeValueAsString(buildBioextractorConfigRequestWrapper())))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void getBioextractorConfigurationsSuccessTest() throws Exception {
		ResponseWrapperV2<PageResponseV2Dto<BioextractorConfigurationDetailDto>> responseWrapper = new ResponseWrapperV2<>();
		BioextractorConfigurationDetailDto dto = new BioextractorConfigurationDetailDto();
		dto.setId("cfg-id-1");
		dto.setConfigName("config-one");
		dto.setBioextractorProviderName("provider-a");
		dto.setBioextractorProviderVersion("1.0");
		dto.setBioModality("face");
		PageResponseV2Dto<BioextractorConfigurationDetailDto> pageResponse = new PageResponseV2Dto<>();
		pageResponse.setData(Collections.singletonList(dto));
		pageResponse.setPageNo(0);
		pageResponse.setPageSize(10);
		pageResponse.setTotalResults(1L);
		responseWrapper.setResponse(pageResponse);

		Mockito.when(partnerManagementService.getBioextractorConfigurations())
				.thenReturn(responseWrapper);

		mockMvc.perform(MockMvcRequestBuilders.get("/bio-extractor-configurations")
						.contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
		verify(partnerManagementService, times(1))
				.getBioextractorConfigurations();
	}

	private RequestWrapperV2<BioextractorConfigurationRequestDto> buildBioextractorConfigRequestWrapper() {
		RequestWrapperV2<BioextractorConfigurationRequestDto> wrapper = new RequestWrapperV2<>();
		wrapper.setId("mosip.pms.bioextractor.configurations.post");
		wrapper.setVersion("1.0");
		wrapper.setRequestTime(LocalDateTime.now());
		BioextractorConfigurationRequestDto req = new BioextractorConfigurationRequestDto();
		req.setConfigName("testConfig");
		req.setBioextractorProviderName("ProviderA");
		req.setBioextractorProviderVersion("1.0");
		req.setBioModality("face");
		wrapper.setRequest(req);
		return wrapper;
	}
}
