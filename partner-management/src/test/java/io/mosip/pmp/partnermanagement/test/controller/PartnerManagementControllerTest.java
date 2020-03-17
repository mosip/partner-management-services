package io.mosip.pmp.partnermanagement.test.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pmp.partnermanagement.core.RequestWrapper;
import io.mosip.pmp.partnermanagement.dto.ActivateDeactivatePartnerRequest;
import io.mosip.pmp.partnermanagement.dto.ApikeyRequests;
import io.mosip.pmp.partnermanagement.dto.PartnerAPIKeyToPolicyMappingsResponse;
import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingRequest;
import io.mosip.pmp.partnermanagement.dto.PartnersPolicyMappingResponse;
import io.mosip.pmp.partnermanagement.dto.PolicyIDResponse;
import io.mosip.pmp.partnermanagement.dto.RetrievePartnerDetailsResponse;
import io.mosip.pmp.partnermanagement.dto.RetrievePartnerManagers;
import io.mosip.pmp.partnermanagement.dto.RetrievePartnersDetails;
import io.mosip.pmp.partnermanagement.service.PartnerManagementService;
import io.mosip.pmp.partnermanagement.test.PartnermanagementApplicationTest;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PartnermanagementApplicationTest.class)
@AutoConfigureMockMvc
@EnableWebMvc
public class PartnerManagementControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	PartnerManagementService partnerManagementService;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void test_Test() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/pmpartners/test"))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
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
		Mockito.when(partnerManagementService.partnerApiKeyPolicyMappings(partnersPolicyMappingRequest, partnerID,
				partnerAPIKey)).thenReturn(partnersPolicyMappingResponse);

		mockMvc.perform(post("/pmpartners/partnerID/partnerAPIKey").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
	}

	@Test
	public void activateDeactivateAuthEKYCPartnersTest() throws Exception {
		String partnerID = "67899";
		PartnersPolicyMappingResponse partnersPolicyMappingResponse = new PartnersPolicyMappingResponse();
		ActivateDeactivatePartnerRequest activateDeactivatePartnerRequest = new ActivateDeactivatePartnerRequest();
		activateDeactivatePartnerRequest.setStatus("Active");
		Mockito.when(
				partnerManagementService.activateDeactivateAuthEKYCPartner(partnerID, activateDeactivatePartnerRequest))
				.thenReturn(partnersPolicyMappingResponse);
		RequestWrapper<ActivateDeactivatePartnerRequest> request = new RequestWrapper<ActivateDeactivatePartnerRequest>();
		request.setRequest(activateDeactivatePartnerRequest);
		request.setId("mosip.partnermanagement.partners.policy.mapping");
		request.setVersion("1.0");
		request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
		request.setMetadata("{}");

		mockMvc.perform(put("/pmpartners/updateStatus/partnerID").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void activateDeactivatePartnerAPIKeyGivenPartnerTest() throws Exception {
		String PartnerAPIKey = "45678";
		String partnerID = "67899";
		PartnersPolicyMappingResponse partnersPolicyMappingResponse = new PartnersPolicyMappingResponse();
		ActivateDeactivatePartnerRequest activateDeactivatePartnerRequest = new ActivateDeactivatePartnerRequest();
		activateDeactivatePartnerRequest.setStatus("Active");
		Mockito.when(partnerManagementService.activateDeactivatePartnerAPIKeyGivenPartner(partnerID,
				activateDeactivatePartnerRequest, PartnerAPIKey)).thenReturn(partnersPolicyMappingResponse);

		RequestWrapper<ActivateDeactivatePartnerRequest> request = new RequestWrapper<ActivateDeactivatePartnerRequest>();
		request.setRequest(activateDeactivatePartnerRequest);
		request.setId("mosip.partnermanagement.partners.policy.mapping");
		request.setVersion("1.0");
		request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
		request.setMetadata("{}");

		mockMvc.perform(put("/pmpartners/partnerID/PartnerAPIKey").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestIdTest() throws Exception {
		String partnerAPIKey = "45678";
		PartnersPolicyMappingResponse partnersPolicyMappingResponse = new PartnersPolicyMappingResponse();
		ActivateDeactivatePartnerRequest activateDeactivatePartnerRequest = new ActivateDeactivatePartnerRequest();
		activateDeactivatePartnerRequest.setStatus("Active");
		Mockito.when(partnerManagementService.approveRejectPartnerAPIKeyRequestsBasedOnAPIKeyRequestId(
				activateDeactivatePartnerRequest, partnerAPIKey)).thenReturn(partnersPolicyMappingResponse);

		RequestWrapper<ActivateDeactivatePartnerRequest> request = new RequestWrapper<ActivateDeactivatePartnerRequest>();
		request.setRequest(activateDeactivatePartnerRequest);
		request.setId("mosip.partnermanagement.partners.policy.mapping");
		request.setVersion("1.0");
		request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
		request.setMetadata("{}");

		mockMvc.perform(put("/pmpartners/PartnerAPIKeyRequests/APIKeyReqID")
				.contentType(MediaType.APPLICATION_JSON_VALUE).content(objectMapper.writeValueAsString(request)))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void getAllAuthEKYCPartnersForThePolicyGroupTest() throws Exception {
		RetrievePartnerDetailsResponse retrievePartnerDetailsResponse = new RetrievePartnerDetailsResponse();
		Mockito.when(partnerManagementService.getAllAuthEKYCPartnersForThePolicyGroup())
				.thenReturn(retrievePartnerDetailsResponse);
		mockMvc.perform(MockMvcRequestBuilders.get("/pmpartners/")).andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void getparticularAuthEKYCPartnerDetailsForGivenPartnerIdTest() throws Exception {
		String partnerID = "67899";
		RetrievePartnersDetails retrievePartnersDetails = new RetrievePartnersDetails();
		Mockito.when(partnerManagementService.getparticularAuthEKYCPartnerDetailsForGivenPartnerId(partnerID))
				.thenReturn(retrievePartnersDetails);
		mockMvc.perform(MockMvcRequestBuilders.get("/pmpartners/partnerID"))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	public void getPartnerManagerTest() throws Exception {
		RetrievePartnerManagers retrievePartnerManagers = new RetrievePartnerManagers();
		Mockito.when(partnerManagementService.getPartnerManager()).thenReturn(retrievePartnerManagers);
		mockMvc.perform(MockMvcRequestBuilders.get("/pmpartners/getManager"))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	public void getPolicyIDTest()  throws Exception{
		String policyName = "Banking";
		PolicyIDResponse policyIDResponse = new PolicyIDResponse();
		Mockito.when(partnerManagementService.getPartnerPolicyID(Mockito.anyString())).thenReturn(policyIDResponse);
		mockMvc.perform(MockMvcRequestBuilders.get("/pmpartners/policyname/" + policyName))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void getPartnerAPIKeyToPolicyMappingsTest() throws Exception {
		String partnerID = "67899";
		String PartnerAPIKey = "56789";
		PartnerAPIKeyToPolicyMappingsResponse partnerAPIKeyToPolicyMappingsResponse = new PartnerAPIKeyToPolicyMappingsResponse();
		Mockito.when(partnerManagementService.getPartnerAPIKeyToPolicyMapping(partnerID, PartnerAPIKey))
				.thenReturn(partnerAPIKeyToPolicyMappingsResponse);
		mockMvc.perform(MockMvcRequestBuilders.get("/pmpartners/partnerID/PartnerAPIKey"))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	public void getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagerTest() throws Exception {
		List<ApikeyRequests> apikeyRequests = new ArrayList<ApikeyRequests>();
		Mockito.when(partnerManagementService.getAllPartnerAPIKeyRequestsAsReceivedByPartnerManagers())
				.thenReturn(apikeyRequests);
		mockMvc.perform(MockMvcRequestBuilders.get("/pmpartners/PartnerAPIKeyRequests"))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	public void getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestIdTest() throws Exception {
		String APIKeyReqID = "56789";
		ApikeyRequests apikeyRequests = new ApikeyRequests();
		Mockito.when(partnerManagementService.getTheRequestForPartnerAPIKeyToPolicyMappingsForGivenRequestId(APIKeyReqID))
				.thenReturn(apikeyRequests);
		mockMvc.perform(MockMvcRequestBuilders.get("/pmpartners/PartnerAPIKeyRequests/APIKeyReqID"))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}
}
