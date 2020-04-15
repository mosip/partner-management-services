package io.mosip.pmp.partner.test.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.LocalDateTime;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pmp.partner.core.RequestWrapper;
import io.mosip.pmp.partner.dto.APIkeyRequests;
import io.mosip.pmp.partner.dto.DigitalCertificateResponse;
import io.mosip.pmp.partner.dto.PartnerAPIKeyRequest;
import io.mosip.pmp.partner.dto.PartnerAPIKeyResponse;
import io.mosip.pmp.partner.dto.PartnerRequest;
import io.mosip.pmp.partner.dto.PartnerResponse;
import io.mosip.pmp.partner.dto.PartnerUpdateRequest;
import io.mosip.pmp.partner.dto.PartnersRetrieveApiKeyRequests;
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
    public void test_Test() throws Exception{
    	 mockMvc.perform(MockMvcRequestBuilders.get("/partners/test")).andExpect(MockMvcResultMatchers.status().isOk());
    }
    
    @Test
    public void partnerSelfRegistrationTest() throws Exception {
        PartnerResponse response = new PartnerResponse();
        Mockito.when(partnerService.savePartner(Mockito.any())).thenReturn(response);
        RequestWrapper<PartnerRequest> request = createRequest();

        mockMvc.perform(post("/partners").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
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
    	response.setPartnerId(partnerId);
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
    	
    	mockMvc.perform(MockMvcRequestBuilders.patch("/partners/12345/partnerAPIKeyRequests").contentType(MediaType.APPLICATION_JSON_VALUE)
                 .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }
    
    @Test
    public void retrieveAllApiKeyRequestsSubmittedByPartnerTillDateTest()throws Exception{
    	String partnerId = "345678";
    	PartnersRetrieveApiKeyRequests response = new PartnersRetrieveApiKeyRequests();
    	APIkeyRequests aPIkeyRequests = new APIkeyRequests();
    	
    	LocalDateTime now = LocalDateTime.now();
    	aPIkeyRequests.setApiKeyReqID("345678");
    	aPIkeyRequests.setApiKeyRequestStatus("Active");
    	aPIkeyRequests.setPartnerApiKey("34567895678");
    	aPIkeyRequests.setValidityTill(Timestamp.valueOf(now));
    	
    	
    	List<APIkeyRequests> list_aPIkeyRequests = new ArrayList<>();
    	list_aPIkeyRequests.add(aPIkeyRequests);
		response.setAPIkeyRequests(list_aPIkeyRequests);
		
       Mockito.when(partnerService.retrieveAllApiKeyRequestsSubmittedByPartner(partnerId)).thenReturn(list_aPIkeyRequests);
       mockMvc.perform(MockMvcRequestBuilders.get("/partners/partnerId/partnerAPIKeyRequests")).andExpect(MockMvcResultMatchers.status().isOk());
    }
    
    @Test
    public void viewApiKeyRequestStatusAndApiKey_Test() throws Exception{
    	APIkeyRequests response = new APIkeyRequests();
        Mockito.when(partnerService.viewApiKeyRequestStatusApiKey("12345","123456")).thenReturn(response);
        mockMvc.perform(MockMvcRequestBuilders.get("/partners/12345/partnerAPIKeyRequests/123456")).andExpect(MockMvcResultMatchers.status().isOk());
    }
    
   @Test
    public void validateDigitalCertificate_Test() throws JsonProcessingException, Exception{
    	DigitalCertificateResponse response = new DigitalCertificateResponse();
    	response.setMessage("DigitalCertificateResponse");
    	Mockito.when(partnerService.validateDigitalCertificate(Mockito.any())).thenReturn(response);
    	RequestWrapper<PartnerUpdateRequest> request = createvalidateDigitalCertificateRequest();
    	
    	mockMvc.perform(put("/partners/digitalcertificate").contentType(MediaType.APPLICATION_JSON_VALUE)
    			.content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());
    }
    
   @Test
   public void validateDigitalCertificatewithoutPublicKey_Test() throws JsonProcessingException, Exception {
	   DigitalCertificateResponse response = new DigitalCertificateResponse();
   	response.setMessage("DigitalCertificateResponse");
   	Mockito.when(partnerService.validateDigitalCertificate(Mockito.any())).thenReturn(response);
   	RequestWrapper<PartnerUpdateRequest> request = createvalidateDigitalCertificateRequest();
   	
   	mockMvc.perform(post("/partners/digitalcertificate").contentType(MediaType.APPLICATION_JSON_VALUE)
   			.content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());
   }
    public RequestWrapper<PartnerUpdateRequest> createvalidateDigitalCertificateRequest(){
    	RequestWrapper<PartnerUpdateRequest> request = new RequestWrapper<PartnerUpdateRequest>();
    	
    	
    	PartnerUpdateRequest partnerUpdateRequest = new PartnerUpdateRequest();
    	
    	partnerUpdateRequest.setAddress("Bangalore");
    	partnerUpdateRequest.setContactNumber("45678678");
    	partnerUpdateRequest.setEmailId("abc@gmail.com");
    	partnerUpdateRequest.setOrganizationName("Mosip");
    	
    	
    	request.setId("mosip.partnermanagement.partnerAPIKeyRequest.create");
    	request.setMetadata("{}");
    	request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
    	request.setVersion("1.0");
    	request.setRequest(partnerUpdateRequest);
    	return request;
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