package io.mosip.pmp.partner.test.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pmp.partner.core.RequestWrapper;
import io.mosip.pmp.partner.dto.DownloadPartnerAPIkeyResponse;
import io.mosip.pmp.partner.dto.PartnerAPIKeyRequest;
import io.mosip.pmp.partner.dto.PartnerAPIKeyResponse;
import io.mosip.pmp.partner.dto.PartnerRequest;
import io.mosip.pmp.partner.dto.PartnerResponse;
import io.mosip.pmp.partner.dto.PartnerUpdateRequest;
import io.mosip.pmp.partner.dto.RetrievePartnerDetailsResponse;
import io.mosip.pmp.partner.service.PartnerService;
import io.mosip.pmp.partner.test.PartnerserviceApplicationTest;
 

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PartnerserviceApplicationTest.class)
@AutoConfigureMockMvc
@EnableWebMvc
public class PartnerServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PartnerService partnerService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void partnerSelfRegistrationTest() throws Exception {
        PartnerResponse response = new PartnerResponse();
        Mockito.when(partnerService.savePartner(Mockito.any())).thenReturn(response);
        RequestWrapper<PartnerRequest> request = createRequest();

        mockMvc.perform(post("/partners/partnerReg").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated());
    }

    @Test
    public void retrievePartnerDetailsTest() throws Exception {
        RetrievePartnerDetailsResponse response = new RetrievePartnerDetailsResponse();
        Mockito.when(partnerService.getPartnerDetails("12345")).thenReturn(response);
        mockMvc.perform(MockMvcRequestBuilders.get("/partners/12345")).andExpect(MockMvcResultMatchers.status().isOk());
    }
    
    @Test
    public void updatePartnerDetailsTest() throws Exception{
    	String partnerId = "12345";
    	PartnerResponse response = new PartnerResponse();
    	response.setPartnerID(partnerId);
    	response.setStatus("true");
    	
    	Mockito.when(partnerService.updatePartnerDetail(Mockito.any(), Mockito.any())).thenReturn(response);
    	RequestWrapper<PartnerUpdateRequest> request = updateRequest();
    	
    	mockMvc.perform(put("/partners/partnerId").contentType(MediaType.APPLICATION_JSON_VALUE)
    			.content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk())
    	        .andExpect(jsonPath("$.response.status", is("true")));
    }

    @Test
    public void submitPartnerApiKeyRequestTest() throws JsonProcessingException, Exception {
    	String apiRequestId = "873276828663";
    	PartnerAPIKeyResponse response = new PartnerAPIKeyResponse();
    	response.setApiRequestId(apiRequestId);
    	response.setMessage("partnerAPIKeyRequest successfully created");
    	
    	Mockito.when(partnerService.submitPartnerApiKeyReq(Mockito.any(), Mockito.any())).thenReturn(response);
    	RequestWrapper<PartnerAPIKeyRequest> request = createSubmitPartnerApiKeyRequest();
    	
    	mockMvc.perform(post("/partners/12345/partnerAPIKeyRequests").contentType(MediaType.APPLICATION_JSON_VALUE)
                 .content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated());
    }
    
    @Test
    public void downloadPartnerAPIkeyTest() throws Exception{
    	DownloadPartnerAPIkeyResponse response = new DownloadPartnerAPIkeyResponse();
    	String partnerAPIKey = "fa604-affcd-33201-04770";
    	response.setPartnerAPIKey(partnerAPIKey);
    	
    	Mockito.when(partnerService.downloadPartnerAPIkey(Mockito.any(), Mockito.any())).thenReturn(response);
    	
    	mockMvc.perform(post("/partners/12345/partnerAPIKeyRequests/partnerAPIKey").contentType(MediaType.APPLICATION_JSON_VALUE))
                 .andExpect(status().isOk());
    	
    }
    
    private RequestWrapper<PartnerAPIKeyRequest> createSubmitPartnerApiKeyRequest(){
    	RequestWrapper<PartnerAPIKeyRequest> request = new RequestWrapper<PartnerAPIKeyRequest>();
    	request.setId("mosip.partnermanagement.partnerAPIKeyRequest.create");
    	request.setMetadata("{}");
    	request.setRequest(createPartnerAPIKeyRequest());
    	request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
    	request.setVersion("1.0");
    	return request;
    }
    
    private PartnerAPIKeyRequest createPartnerAPIKeyRequest() {
    	PartnerAPIKeyRequest partnerAPIKeyRequest = new PartnerAPIKeyRequest();
    	partnerAPIKeyRequest.setPolicyName("airtelIndPolicy");
    	partnerAPIKeyRequest.setUseCaseDescription("Need to submit the payment");
    	return partnerAPIKeyRequest;
    }
    
    private RequestWrapper<PartnerRequest> createRequest() {
        RequestWrapper<PartnerRequest> request = new RequestWrapper<PartnerRequest>();
        request.setRequest(createPartnerRequest());
        request.setId("mosip.partnermanagement.partners.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
    }
    
    private PartnerRequest createPartnerRequest() {
        PartnerRequest partnerRequest = new PartnerRequest();
        partnerRequest.setAddress("address");
        partnerRequest.setContactNumber("898989898");
        partnerRequest.setEmailId("xyz@gmail.com");
        partnerRequest.setOrganizationName("airtel India");
        partnerRequest.setPolicyGroup("telecom");
        return partnerRequest;
    }
     
    private RequestWrapper<PartnerUpdateRequest> updateRequest(){
    	RequestWrapper<PartnerUpdateRequest> request = new RequestWrapper<PartnerUpdateRequest>();
    	request.setRequest(partnerUpdateRequest());
    	request.setId("mosip.partnermanagement.partners.update");
    	request.setVersion("1.0");
    	request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
    	return request;
    }
    
    private PartnerUpdateRequest partnerUpdateRequest() {
    	PartnerUpdateRequest partnerUpdateRequest = new PartnerUpdateRequest();
    	partnerUpdateRequest.setAddress("Bangalore,INDIA");
    	partnerUpdateRequest.setContactNumber("9886779980");
    	partnerUpdateRequest.setEmailId("airtelInd@gmail.com");
    	partnerUpdateRequest.setOrganizationName("airtelInd");
    	return partnerUpdateRequest;
    }
}