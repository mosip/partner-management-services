package io.mosip.pmp.partner.test.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
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

import io.mosip.pmp.partner.core.RequestWrapper;
import io.mosip.pmp.partner.dto.APIkeyRequests;
import io.mosip.pmp.partner.dto.AddContactRequestDto;
import io.mosip.pmp.partner.dto.CACertificateRequestDto;
import io.mosip.pmp.partner.dto.CACertificateResponseDto;
import io.mosip.pmp.partner.dto.PartnerAPIKeyRequest;
import io.mosip.pmp.partner.dto.PartnerAPIKeyResponse;
import io.mosip.pmp.partner.dto.PartnerCertDownloadRequestDto;
import io.mosip.pmp.partner.dto.PartnerCertificateRequestDto;
import io.mosip.pmp.partner.dto.PartnerCertificateResponseDto;
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
@Ignore
public class PartnerServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PartnerService partnerService;
   
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void partnerSelfRegistrationTest() throws Exception {
        PartnerResponse response = new PartnerResponse();
        Mockito.when(partnerService.savePartner(Mockito.any())).thenReturn(response);
        RequestWrapper<PartnerRequest> request = createRequest();

        mockMvc.perform(post("/partners").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void addContactsTest() throws Exception {
        Mockito.when(partnerService.createAndUpdateContactDetails(Mockito.any(),Mockito.any())).thenReturn(new String());
        mockMvc.perform(post("/partners/12345/addcontact").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(addContactRequestWrapper()))).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void retrievePartnerDetailsTest() throws Exception {
        RetrievePartnerDetailsResponse response = new RetrievePartnerDetailsResponse();
        Mockito.when(partnerService.getPartnerDetails("12345")).thenReturn(response);
        mockMvc.perform(MockMvcRequestBuilders.get("/partners/12345")).andExpect(MockMvcResultMatchers.status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void updatePartnerDetailsTest() throws Exception{
    	String partnerId = "12345";
    	PartnerResponse response = new PartnerResponse();
    	response.setPartnerId(partnerId);
    	response.setStatus("true");
    	
    	Mockito.when(partnerService.updatePartnerDetail(Mockito.any(), Mockito.any())).thenReturn(response);
    	RequestWrapper<PartnerUpdateRequest> request = updateRequest();
    	
    	mockMvc.perform(put("/partners/partnerId").contentType(MediaType.APPLICATION_JSON_VALUE)
    			.content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = {"PARTNER"})
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
    @WithMockUser(roles = {"PARTNER"})
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
    @WithMockUser(roles = {"PARTNER"})
    public void viewApiKeyRequestStatusAndApiKey_Test() throws Exception{
    	APIkeyRequests response = new APIkeyRequests();
        Mockito.when(partnerService.viewApiKeyRequestStatusApiKey("12345","123456")).thenReturn(response);
        mockMvc.perform(MockMvcRequestBuilders.get("/partners/12345/partnerAPIKeyRequests/123456")).andExpect(MockMvcResultMatchers.status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void uploadCACertificateTest() throws Exception{
    	CACertificateResponseDto response = new CACertificateResponseDto();
        Mockito.when(partnerService.uploadCACertificate(cACertificateRequest())).thenReturn(response);
        mockMvc.perform(post("/partners/uploadCACertificate").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(createCACertificateRequest()))).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void uploadPartnerCertificateTest() throws Exception{
    	PartnerCertificateResponseDto response = new PartnerCertificateResponseDto();
        Mockito.when(partnerService.uploadPartnerCertificate(createPartnerCertificateRequest())).thenReturn(response);
        mockMvc.perform(post("/partners/uploadPartnerCertificate").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(partnerCertificateRequest()))).andExpect(status().isOk());
    }

//    @Test
//    @WithMockUser(roles = {"PARTNER"})
//    public void getPartnerCertificateTest() throws Exception{
//    	PartnerCertDownloadResponeDto response = new PartnerCertDownloadResponeDto();
//        Mockito.when(partnerService.getPartnerCertificate(certDownloadRequest())).thenReturn(response);
//        mockMvc.perform(MockMvcRequestBuilders.get("/partners/12345/partnerAPIKeyRequests/123456")).andExpect(MockMvcResultMatchers.status().isOk());
//    }    
    
    
    public RequestWrapper<PartnerUpdateRequest> createvalidateDigitalCertificateRequest(){
    	RequestWrapper<PartnerUpdateRequest> request = new RequestWrapper<PartnerUpdateRequest>();
    	
    	
    	PartnerUpdateRequest partnerUpdateRequest = new PartnerUpdateRequest();
    	
    	partnerUpdateRequest.setAddress("Bangalore");
    	partnerUpdateRequest.setContactNumber("45678678");
    	
    	
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
        partnerRequest.setPartnerType("test");
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
    	return partnerUpdateRequest;
    }
    
    private RequestWrapper<CACertificateRequestDto> createCACertificateRequest() {
        RequestWrapper<CACertificateRequestDto> request = new RequestWrapper<CACertificateRequestDto>();
        request.setRequest(cACertificateRequest());
        request.setId("mosip.partnermanagement.partners.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
    }
    
    private CACertificateRequestDto cACertificateRequest() {
    	CACertificateRequestDto dto = new CACertificateRequestDto();
    	dto.setCertificateData("qwertyui");
    	return dto;
    }
    
    private RequestWrapper<PartnerCertificateRequestDto> partnerCertificateRequest() {
        RequestWrapper<PartnerCertificateRequestDto> request = new RequestWrapper<PartnerCertificateRequestDto>();
        request.setRequest(createPartnerCertificateRequest());
        request.setId("mosip.partnermanagement.partners.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
    }
    
    private PartnerCertificateRequestDto createPartnerCertificateRequest() {
    	PartnerCertificateRequestDto dto = new PartnerCertificateRequestDto();
    	dto.setCertificateData("qweryre");
    	dto.setOrganizationName("MOSIP");
    	dto.setPartnerType("Test");
    	return dto;
    }
    
    @SuppressWarnings("unused")
	private RequestWrapper<PartnerCertDownloadRequestDto> partnerCertificateDownloadRequest() {
        RequestWrapper<PartnerCertDownloadRequestDto> request = new RequestWrapper<PartnerCertDownloadRequestDto>();
        request.setRequest(certDownloadRequest());
        request.setId("mosip.partnermanagement.partners.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
    }
    
    private PartnerCertDownloadRequestDto certDownloadRequest() {
    	PartnerCertDownloadRequestDto dto = new PartnerCertDownloadRequestDto();
    	//dto.setPartnerCertId("12345");
    	return dto;
    }
    
    private RequestWrapper<AddContactRequestDto> addContactRequestWrapper() {
        RequestWrapper<AddContactRequestDto> request = new RequestWrapper<AddContactRequestDto>();
        request.setRequest(addContactRequestDto());
        request.setId("mosip.partnermanagement.partners.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
    }
    
    private AddContactRequestDto addContactRequestDto() {
    	AddContactRequestDto dto = new AddContactRequestDto();
    	dto.setAddress("HSR");
    	dto.setContactNumber("1234678008");
    	dto.setEmailId("test@gmail.com");
    	dto.setIs_Active(true);
    	return dto;
    }
    
    
}