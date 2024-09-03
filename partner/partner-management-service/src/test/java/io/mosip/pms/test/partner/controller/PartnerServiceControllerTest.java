package io.mosip.pms.test.partner.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import io.mosip.pms.partner.response.dto.*;
import io.mosip.pms.partner.controller.PartnerServiceController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import io.mosip.pms.common.dto.FilterDto;
import io.mosip.pms.common.dto.FilterValueDto;
import io.mosip.pms.common.dto.Pagination;
import io.mosip.pms.common.dto.SearchDto;
import io.mosip.pms.common.dto.SearchFilter;
import io.mosip.pms.common.dto.SearchSort;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.partner.constant.PartnerServiceAuditEnum;
import io.mosip.pms.partner.dto.PartnerPolicyMappingResponseDto;
import io.mosip.pms.partner.manager.service.PartnerManagerService;
import io.mosip.pms.partner.request.dto.APIKeyGenerateRequestDto;
import io.mosip.pms.partner.request.dto.AddContactRequestDto;
import io.mosip.pms.partner.request.dto.CACertificateRequestDto;
import io.mosip.pms.partner.request.dto.EmailVerificationRequestDto;
import io.mosip.pms.partner.request.dto.ExtractorDto;
import io.mosip.pms.partner.request.dto.ExtractorProviderDto;
import io.mosip.pms.partner.request.dto.ExtractorsDto;
import io.mosip.pms.partner.request.dto.PartnerCertDownloadRequestDto;
import io.mosip.pms.partner.request.dto.PartnerCertificateRequestDto;
import io.mosip.pms.partner.request.dto.PartnerPolicyMappingRequest;
import io.mosip.pms.partner.request.dto.PartnerRequest;
import io.mosip.pms.partner.request.dto.PartnerSearchDto;
import io.mosip.pms.partner.request.dto.PartnerUpdateRequest;
import io.mosip.pms.partner.response.dto.APIKeyGenerateResponseDto;
import io.mosip.pms.partner.response.dto.APIkeyRequests;
import io.mosip.pms.partner.response.dto.CACertificateResponseDto;
import io.mosip.pms.partner.response.dto.EmailVerificationResponseDto;
import io.mosip.pms.partner.response.dto.PartnerCertificateResponseDto;
import io.mosip.pms.partner.response.dto.PartnerCredentialTypePolicyDto;
import io.mosip.pms.partner.response.dto.PartnerResponse;
import io.mosip.pms.partner.response.dto.RetrievePartnerDetailsResponse;
import io.mosip.pms.partner.service.PartnerService;
 

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@EnableWebMvc
public class PartnerServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PartnerService partnerService;

    @MockBean
    private PartnerServiceController partnerServiceController;
    
    @MockBean
    PartnerManagerService partnerManagerService;
   
    @Autowired
    private ObjectMapper objectMapper;
    
	@Autowired
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate restTemplate;
    
    @MockBean
	AuditUtil auditUtil;
	
	@Before
	public void setup() {
		Mockito.doNothing().when(auditUtil).setAuditRequestDto(Mockito.any(PartnerServiceAuditEnum.class));	
	}
    
    
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
        mockMvc.perform(post("/partners/12345/contact/add").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(addContactRequestWrapper()))).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNERMANAGER"})
    public void uploadCACertificateTest() throws Exception{
    	CACertificateResponseDto response = new CACertificateResponseDto();
        Mockito.when(partnerService.uploadCACertificate(cACertificateRequest())).thenReturn(response);
        mockMvc.perform(post("/partners/certificate/ca/upload").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(createCACertificateRequest()))).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void uploadPartnerCertificateTest() throws Exception{
    	PartnerCertificateResponseDto response = new PartnerCertificateResponseDto();
        Mockito.when(partnerService.uploadPartnerCertificate(Mockito.any())).thenReturn(response);
        mockMvc.perform(post("/partners/certificate/upload").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(partnerCertificateRequest()))).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void addBiometricExtractorsTest() throws JsonProcessingException, Exception {
    	Mockito.when(partnerService.addBiometricExtractors("123456", "12345", getExtractorsInput())).thenReturn(new String());
    	mockMvc.perform(post("/partners/123456/bioextractors/12345").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(createAddBiometricExtractorRequest()))).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void getCredentialTypePolicyTest() throws Exception{
    	PartnerCredentialTypePolicyDto response = new PartnerCredentialTypePolicyDto();
        Mockito.when(partnerService.getPartnerCredentialTypePolicy(Mockito.anyString(),Mockito.anyString())).thenReturn(response);
        mockMvc.perform(MockMvcRequestBuilders.get("/partners/12345/credentialtype/12345/policies")).andExpect(MockMvcResultMatchers.status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void mapPolicyToCredentialTypeTest() throws Exception{
        Mockito.when(partnerService.mapPartnerPolicyCredentialType(Mockito.anyString(),Mockito.anyString(),Mockito.anyString())).thenReturn(new String());
        mockMvc.perform(MockMvcRequestBuilders.post("/partners/12345/credentialtype/12345/policies/12345")).andExpect(MockMvcResultMatchers.status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void getBiometricExtractorsTest() throws JsonProcessingException, Exception {
    	Mockito.when(partnerService.getBiometricExtractors("123456", "12345")).thenReturn(new ExtractorsDto());
    	mockMvc.perform(MockMvcRequestBuilders.get("/partners/123456/bioextractors/12345")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void retrievePartnerCertificateTest() throws Exception {
        PartnerCertDownloadResponeDto partnerCertDownloadResponeDto = new PartnerCertDownloadResponeDto();
        Mockito.when(partnerService.getPartnerCertificate(Mockito.any())).thenReturn(partnerCertDownloadResponeDto);
        mockMvc.perform(MockMvcRequestBuilders.get("/partners/12345/certificate")).andExpect(MockMvcResultMatchers.status().isOk());
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
    	
    	mockMvc.perform(put("/partners/12345").contentType(MediaType.APPLICATION_JSON_VALUE)
    			.content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void retrieveAllApiKeyRequestsSubmittedByPartnerTillDateTest()throws Exception{
    	String partnerId = "345678";
    	APIkeyRequests aPIkeyRequests = new APIkeyRequests();
    	
    	LocalDateTime now = LocalDateTime.now();
    	aPIkeyRequests.setApiKeyReqID("345678");
    	aPIkeyRequests.setApiKeyRequestStatus("Active");
    	aPIkeyRequests.setPartnerApiKey("34567895678");
    	aPIkeyRequests.setValidityTill(Timestamp.valueOf(now));
    	
    	
    	List<APIkeyRequests> list_aPIkeyRequests = new ArrayList<>();
    	list_aPIkeyRequests.add(aPIkeyRequests);
		
       Mockito.when(partnerService.retrieveAllApiKeyRequestsSubmittedByPartner(partnerId)).thenReturn(list_aPIkeyRequests);
       mockMvc.perform(MockMvcRequestBuilders.get("/partners/12345/apikey/request")).andExpect(MockMvcResultMatchers.status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void searchPartnerTest() throws Exception{
    	RequestWrapper<PartnerSearchDto> request = createSearchPartnerRequest();
        mockMvc.perform(MockMvcRequestBuilders.post("/partners/search").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void searchPartnerTypeTest() throws Exception{
    	RequestWrapper<SearchDto> request = createSearchPartnerTypeRequest();
        mockMvc.perform(MockMvcRequestBuilders.post("/partners/partnertype/search").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void searchApikeyRequestTest() throws Exception{
    	RequestWrapper<SearchDto> request = createSearchPartnerTypeRequest();
        mockMvc.perform(MockMvcRequestBuilders.post("/partners/apikey/request/search").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void searchApikeyTest() throws Exception{
    	RequestWrapper<SearchDto> request = createSearchPartnerTypeRequest();
        mockMvc.perform(MockMvcRequestBuilders.post("/partners/apikey/search").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void filterValuesTest() throws Exception{
    	RequestWrapper<FilterValueDto> request = createFilterRequest();
        mockMvc.perform(MockMvcRequestBuilders.post("/partners/filtervalues").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void apikeyRequetsFilterValuesTest() throws Exception{
    	RequestWrapper<FilterValueDto> request = createFilterRequest();
        mockMvc.perform(MockMvcRequestBuilders.post("/partners/apikey/request/filtervalues").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void updatePolicyGroup() throws Exception{    
    	Mockito.when(partnerService.updatePolicyGroup(Mockito.any(),Mockito.any())).thenReturn("Success");
    	mockMvc.perform(put("/partners/1234/policygroup/5678")).andExpect(MockMvcResultMatchers.status().isOk());
    }
    
    @Test    
    @WithMockUser
    public void isEmailExists() throws Exception{ 
    	EmailVerificationResponseDto response = new EmailVerificationResponseDto();
    	RequestWrapper<EmailVerificationRequestDto> request = new RequestWrapper<>();
    	EmailVerificationRequestDto requestDto = new EmailVerificationRequestDto();
    	request.setRequest(requestDto);
    	Mockito.when(partnerService.isPartnerExistsWithEmail(request.getRequest().getEmailId())).thenReturn(response);
    	mockMvc.perform(put("/partners/email/verify").contentType(MediaType.APPLICATION_JSON_VALUE)
    			.content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test    
    @WithMockUser(roles = {"PARTNER"})
    public void mapPolicyToPartner() throws Exception{ 
    	PartnerPolicyMappingResponseDto response = new PartnerPolicyMappingResponseDto();
    	RequestWrapper<PartnerPolicyMappingRequest> request = new RequestWrapper<>();
    	PartnerPolicyMappingRequest requestDto = new PartnerPolicyMappingRequest();
    	request.setRequest(requestDto);
    	Mockito.when(partnerService.requestForPolicyMapping(request.getRequest(),"1234")).thenReturn(response);
    	partnerServiceController.mapPolicyToPartner("1234", request);
    }
    
    @Test    

    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void generateAPIKey() throws Exception{ 
    	APIKeyGenerateResponseDto response = new APIKeyGenerateResponseDto();
    	RequestWrapper<APIKeyGenerateRequestDto> request = new RequestWrapper<>();
    	APIKeyGenerateRequestDto requestDto = new APIKeyGenerateRequestDto();
    	request.setRequest(requestDto);
    	Mockito.when(partnerManagerService.generateAPIKey("1234",requestDto)).thenReturn(response);
    	partnerServiceController.generateAPIKey("1234", request);
    }

    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void getPartnerCertificateTest() throws Exception{
        PartnerCertDownloadResponeDto certDownloadResponeDto = new PartnerCertDownloadResponeDto();
        RequestWrapper<PartnerCertDownloadRequestDto> requestWrapper = new RequestWrapper<>();
        PartnerCertDownloadRequestDto requestDto = new PartnerCertDownloadRequestDto();
        requestWrapper.setRequest(requestDto);
        Mockito.when(partnerService.getPartnerCertificate(requestDto)).thenReturn(certDownloadResponeDto);
        mockMvc.perform(MockMvcRequestBuilders.get("/partners/1234/certificate")).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void getOriginalPartnerCertificateTest() throws Exception{
        OriginalCertDownloadResponseDto originalCertDownloadResponseDto = new OriginalCertDownloadResponseDto();
        RequestWrapper<PartnerCertDownloadRequestDto> requestWrapper = new RequestWrapper<>();
        PartnerCertDownloadRequestDto requestDto = new PartnerCertDownloadRequestDto();
        requestWrapper.setRequest(requestDto);
        Mockito.when(partnerService.getOriginalPartnerCertificate(requestDto)).thenReturn(originalCertDownloadResponseDto);
        mockMvc.perform(MockMvcRequestBuilders.get("/partners/1234/originalPartnerCertificate")).andExpect(MockMvcResultMatchers.status().isOk());
    }
    
    private RequestWrapper<FilterValueDto> createFilterRequest(){
    	RequestWrapper<FilterValueDto> request = new RequestWrapper<FilterValueDto>();
    	request.setId("mosip.partnermanagement.partnerAPIKeyRequest.create");
    	request.setMetadata("{}");
    	request.setRequest(filterValuesRequest());
    	request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
    	request.setVersion("1.0");
    	return request;
    }
    
    private FilterValueDto filterValuesRequest() {
    	FilterValueDto deviceFilterValueDto = new FilterValueDto();
    	FilterDto filterDto = new FilterDto();
    	SearchFilter searchFilter = new SearchFilter();
    	searchFilter.setColumnName("name");
		searchFilter.setFromValue("");
		searchFilter.setToValue("");
		searchFilter.setType("all");
		searchFilter.setValue("m");
		List<SearchFilter> searchDtos = new ArrayList<SearchFilter>();
    	searchDtos.add(searchFilter);
    	filterDto.setColumnName("name");
    	filterDto.setText("");
    	filterDto.setType("all");
    	List<FilterDto> filterDtos = new ArrayList<FilterDto>();
    	filterDtos.add(filterDto);
    	deviceFilterValueDto.setFilters(filterDtos);    	
    	return deviceFilterValueDto;
    }
    
    private RequestWrapper<SearchDto> createSearchPartnerTypeRequest(){
    	RequestWrapper<SearchDto> request = new RequestWrapper<SearchDto>();
    	request.setId("mosip.partnermanagement.partnerAPIKeyRequest.create");
    	request.setMetadata("{}");
    	request.setRequest(SearchPartnerTypeRequest());
    	request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
    	request.setVersion("1.0");
    	return request;
    }
     
    private SearchDto SearchPartnerTypeRequest() {
    	SearchDto searchDto = new SearchDto();
    	SearchFilter searchFilter = new SearchFilter();
    	Pagination pagination = new Pagination();
    	SearchSort searchSort = new SearchSort();
    	pagination.setPageFetch(10);
    	pagination.setPageStart(0);
    	searchSort.setSortField("name");
    	searchSort.setSortType("asc");
    	List<SearchSort> sortDtos = new ArrayList<SearchSort>();
    	sortDtos.add(searchSort);
    	searchFilter.setColumnName("name");
		searchFilter.setFromValue("");
		searchFilter.setToValue("");
		searchFilter.setType("all");
		searchFilter.setValue("m");
		List<SearchFilter> searchDtos = new ArrayList<SearchFilter>();
    	searchDto.setSort(sortDtos);
    	searchDto.setFilters(searchDtos);
    	return searchDto;
    }
    
    
    private RequestWrapper<PartnerSearchDto> createSearchPartnerRequest(){
    	RequestWrapper<PartnerSearchDto> request = new RequestWrapper<PartnerSearchDto>();
    	request.setId("mosip.partnermanagement.partnerAPIKeyRequest.create");
    	request.setMetadata("{}");
    	request.setRequest(searchPartnerRequest());
    	request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
    	request.setVersion("1.0");
    	return request;
    }
    
    
    
    private PartnerSearchDto searchPartnerRequest() {
    	PartnerSearchDto partnerSearchDto = new PartnerSearchDto();
    	Pagination pagination = new Pagination();
    	SearchSort searchSort = new SearchSort();
    	SearchFilter searchFilter = new SearchFilter();
    	pagination.setPageFetch(10);
    	pagination.setPageStart(0);
    	searchSort.setSortField("name");
    	searchSort.setSortType("asc");
    	List<SearchSort> sortDtos = new ArrayList<SearchSort>();
    	sortDtos.add(searchSort);
    	searchFilter.setColumnName("name");
		searchFilter.setFromValue("");
		searchFilter.setToValue("");
		searchFilter.setType("all");
		searchFilter.setValue("m");
		List<SearchFilter> searchDtos = new ArrayList<SearchFilter>();
    	partnerSearchDto.setPartnerType("all");
    	partnerSearchDto.setFilters(searchDtos);
    	partnerSearchDto.setSort(sortDtos);
    	return partnerSearchDto;
    	
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
        partnerRequest.setPartnerId("1001");
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
    
    private RequestWrapper<ExtractorsDto> createAddBiometricExtractorRequest() {
        RequestWrapper<ExtractorsDto> request = new RequestWrapper<ExtractorsDto>();
        request.setRequest(getExtractorsInput());
        request.setId("mosip.partnermanagement.partners.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
    }
    
    private CACertificateRequestDto cACertificateRequest() {
    	CACertificateRequestDto dto = new CACertificateRequestDto();
    	dto.setCertificateData("qwertyui");
    	dto.setPartnerDomain("Auth");
    	return dto;
    }
    
    private ExtractorsDto getExtractorsInput() {
    	ExtractorsDto request = new ExtractorsDto();
    	List<ExtractorDto> extractors = new ArrayList<>();
    	ExtractorDto dto = new ExtractorDto();
    	dto.setAttributeName("face");
    	dto.setBiometric("face");
    	ExtractorProviderDto provider = new ExtractorProviderDto();
    	provider.setProvider("t5");
    	provider.setVersion("1.1");
    	dto.setExtractor(provider);
    	extractors.add(dto);
    	request.setExtractors(extractors);
    	return request;
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
    
    private RequestWrapper<AddContactRequestDto> addContactRequestWrapper() {
        RequestWrapper<AddContactRequestDto> request = new RequestWrapper<AddContactRequestDto>();
        request.setRequest(addContactRequestDto());
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
    	dto.setPartnerDomain("Auth");
    	dto.setPartnerId("1001");
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

    
    private AddContactRequestDto addContactRequestDto() {
    	AddContactRequestDto dto = new AddContactRequestDto();
    	dto.setAddress("HSR");
    	dto.setContactNumber("1234678008");
    	dto.setEmailId("test@gmail.com");
    	dto.setIs_Active(true);
    	return dto;
    }
    
    
}