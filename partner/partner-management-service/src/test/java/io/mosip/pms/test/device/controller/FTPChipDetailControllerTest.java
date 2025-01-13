package io.mosip.pms.test.device.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import io.mosip.pms.common.request.dto.RequestWrapperV2;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.device.dto.FtmChipDetailsDto;
import io.mosip.pms.device.dto.FtmChipFilterDto;
import io.mosip.pms.device.request.dto.*;
import io.mosip.pms.device.response.dto.*;
import io.mosip.pms.partner.response.dto.FtmCertificateDownloadResponseDto;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pms.common.constant.Purpose;
import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.common.dto.Pagination;
import io.mosip.pms.common.dto.SearchFilter;
import io.mosip.pms.common.dto.SearchSort;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.device.authdevice.entity.FTPChipDetail;
import io.mosip.pms.device.authdevice.service.FtpChipDetailService;
import io.mosip.pms.device.authdevice.service.impl.FTPChipDetailServiceImpl;
import io.mosip.pms.device.util.AuditUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@EnableWebMvc
public class FTPChipDetailControllerTest {
	@MockBean
	private FTPChipDetailServiceImpl ftpChipDetailServiceImpl;
	
	@Mock
	FtpChipDetailService ftpChipDetaillService;
	
	@Autowired
    private MockMvc mockMvc;
   
    @Autowired
    private ObjectMapper objectMapper;
    
	@Autowired
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate restTemplate;
    
    @MockBean
	AuditUtil auditUtil;
	
	 @Before
	    public void setup() throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {
		 IdDto response = new IdDto();
		 String stringResponse = new String();
		 PageResponseDto<FTPSearchResponseDto> searchResponse = new PageResponseDto<FTPSearchResponseDto>();
		 FtpCertificateResponseDto uploadResponse = new FtpCertificateResponseDto();
		 FtpCertDownloadResponeDto getResponse = new FtpCertDownloadResponeDto();		 
		 ResponseWrapper<IdDto> responseWrapper = new ResponseWrapper<>();
		 ResponseWrapper<String> responsewrapper = new ResponseWrapper<>();
		 ResponseWrapper<PageResponseDto<FTPSearchResponseDto>> searchResponseWrapper = new ResponseWrapper<PageResponseDto<FTPSearchResponseDto>>();
		 ResponseWrapper<FtpCertificateResponseDto> uploadResponseWrapper = new ResponseWrapper<FtpCertificateResponseDto>();
		 ResponseWrapper<FtpCertDownloadResponeDto> getResponseWrapper = new ResponseWrapper<FtpCertDownloadResponeDto>();
		 responseWrapper.setResponse(response);
		 responsewrapper.setResponse(stringResponse);
		 searchResponseWrapper.setResponse(searchResponse);
		 uploadResponseWrapper.setResponse(uploadResponse);
		 getResponseWrapper.setResponse(getResponse);
		 Mockito.when(ftpChipDetailServiceImpl.createFtpChipDetails(Mockito.any())).thenReturn(response);
		 Mockito.when(ftpChipDetailServiceImpl.updateFtpChipDetails(Mockito.any())).thenReturn(response);
		 Mockito.when(ftpChipDetailServiceImpl.updateFtpChipDetailStatus(Mockito.any())).thenReturn(stringResponse);
		 Mockito.when(ftpChipDetailServiceImpl.searchFTPChipDetails(Mockito.any(), Mockito.any())).thenReturn(searchResponse);
		 Mockito.when(ftpChipDetailServiceImpl.uploadCertificate(Mockito.any())).thenReturn(uploadResponse);
		 Mockito.when(ftpChipDetailServiceImpl.getCertificate(Mockito.any())).thenReturn(getResponse);		 
		 Mockito.doNothing().when(auditUtil).auditRequest(any(), any(), any());
	     Mockito.doNothing().when(auditUtil).auditRequest(any(), any(), any(),any());
	 }
	 
	 private RequestWrapper<FtpChipDetailDto> createRequest(boolean isItForRegistrationDevice) {
	        RequestWrapper<FtpChipDetailDto> request = new RequestWrapper<FtpChipDetailDto>();
	        request.setRequest(createFTPChipDetailRequest(isItForRegistrationDevice));
	        request.setId("mosip.partnermanagement.devicedetail.create");
	        request.setVersion("1.0");
	        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
	        request.setMetadata("{}");
	        return request;
	    }
	 
	 private FtpChipDetailDto createFTPChipDetailRequest(boolean isItForRegistrationDevice ) {
		 FtpChipDetailDto ftpChipDetailDto = new FtpChipDetailDto();
		 ftpChipDetailDto.setFtpProviderId("1234");
		 ftpChipDetailDto.setMake("make");
		 ftpChipDetailDto.setModel("model");
		 return ftpChipDetailDto;
	 }
	 
	 private RequestWrapper<FtpChipDetailUpdateDto>updateRequest(boolean isItForRegistrationDevice) {
	        RequestWrapper<FtpChipDetailUpdateDto> request = new RequestWrapper<FtpChipDetailUpdateDto>();
	        request.setRequest(updateFTPCHIPDetailRequest(isItForRegistrationDevice));
	        request.setId("mosip.partnermanagement.devicedetail.create");
	        request.setVersion("1.0");
	        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
	        request.setMetadata("{}");
	        return request;
	    }
	    
	    private FtpChipDetailUpdateDto updateFTPCHIPDetailRequest(boolean isItForRegistrationDevice) {
	    	FtpChipDetailUpdateDto ftpChipDetailUpdateDto = new FtpChipDetailUpdateDto();
	    	ftpChipDetailUpdateDto.setFtpChipDetailId("1234");
	    	ftpChipDetailUpdateDto.setMake("make");
	    	ftpChipDetailUpdateDto.setModel("model");
	    	return ftpChipDetailUpdateDto;
	    }
	 
	    
	    private RequestWrapper<FtpChipDetailStatusDto>approvalRequest(boolean isItForRegistrationDevice) {
	        RequestWrapper<FtpChipDetailStatusDto> request = new RequestWrapper<FtpChipDetailStatusDto>();
	        request.setRequest(approvalFTPChipDetailRequest(isItForRegistrationDevice));
	        request.setId("mosip.partnermanagement.devicedetail.create");
	        request.setVersion("1.0");
	        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
	        request.setMetadata("{}");
	        return request;
	    }
	    
	    private FtpChipDetailStatusDto approvalFTPChipDetailRequest(boolean isItForRegistrationDevice) {
	    	FtpChipDetailStatusDto ftpChipDetailStatusDto = new FtpChipDetailStatusDto();
	    	ftpChipDetailStatusDto.setApprovalStatus(true);
	    	ftpChipDetailStatusDto.setFtpChipDetailId("1234");
			return ftpChipDetailStatusDto;	
	    }
	    
	    private RequestWrapper<DeviceSearchDto> RegSearchRequest() {
	        RequestWrapper<DeviceSearchDto> request = new RequestWrapper<DeviceSearchDto>();
	        request.setRequest(searchRegFTPChipDetailRequest());
	        request.setId("mosip.partnermanagement.devicedetail.create");
	        request.setVersion("1.0");
	        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
	        request.setMetadata("{}");
	        return request;
	    }
	    
	    private DeviceSearchDto searchRegFTPChipDetailRequest() {
	    	DeviceSearchDto dto = new DeviceSearchDto();
	    	SearchFilter searchFilter = new SearchFilter();
	    	SearchSort searchSort = new SearchSort();
	    	Pagination pagination = new Pagination();
	    	searchSort.setSortField("model");
	    	searchSort.setSortType("asc");
	    	searchFilter.setColumnName("model");
	    	searchFilter.setFromValue("");
	    	searchFilter.setToValue("");
	    	searchFilter.setType("STARTSWITH");
	    	searchFilter.setValue("b");
	    	List<SearchSort> searchDtos1 = new ArrayList<SearchSort>();
	    	searchDtos1.add(searchSort);
	    	List<SearchFilter> searchfilterDtos = new ArrayList<SearchFilter>();
	    	searchfilterDtos.add(searchFilter);
	    	List<SearchFilter> searchDtos = new ArrayList<SearchFilter>();
	    	searchDtos.add(searchFilter);
	    	pagination.setPageFetch(10);
	    	pagination.setPageStart(0);
	    	dto.setSort(searchDtos1);
	    	dto.setFilters(searchfilterDtos);
	    	dto.setPagination(pagination);
	    	dto.setPurpose(Purpose.REGISTRATION.toString());
			return dto;	
	    }
	    
	    private RequestWrapper<DeviceSearchDto> searchRequest() {
	        RequestWrapper<DeviceSearchDto> request = new RequestWrapper<DeviceSearchDto>();
	        request.setRequest(searchFTPChipDetailRequest());
	        request.setId("mosip.partnermanagement.devicedetail.create");
	        request.setVersion("1.0");
	        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
	        request.setMetadata("{}");
	        return request;
	    }
	    
	    private DeviceSearchDto searchFTPChipDetailRequest() {
	    	DeviceSearchDto dto = new DeviceSearchDto();
	    	SearchFilter searchFilter = new SearchFilter();
	    	SearchSort searchSort = new SearchSort();
	    	Pagination pagination = new Pagination();
	    	searchSort.setSortField("model");
	    	searchSort.setSortType("asc");
	    	searchFilter.setColumnName("model");
	    	searchFilter.setFromValue("");
	    	searchFilter.setToValue("");
	    	searchFilter.setType("STARTSWITH");
	    	searchFilter.setValue("b");
	    	List<SearchSort> searchDtos1 = new ArrayList<SearchSort>();
	    	searchDtos1.add(searchSort);
	    	List<SearchFilter> searchfilterDtos = new ArrayList<SearchFilter>();
	    	searchfilterDtos.add(searchFilter);
	    	List<SearchFilter> searchDtos = new ArrayList<SearchFilter>();
	    	searchDtos.add(searchFilter);
	    	pagination.setPageFetch(10);
	    	pagination.setPageStart(0);
	    	dto.setSort(searchDtos1);
	    	dto.setFilters(searchfilterDtos);
	    	dto.setPagination(pagination);
	    	dto.setPurpose(Purpose.AUTH.toString());
			return dto;
	    }
	    
	    private RequestWrapper<FtpChipCertificateRequestDto> uploadRequest(boolean isItForRegistrationDevice) {
	        RequestWrapper<FtpChipCertificateRequestDto> request = new RequestWrapper<FtpChipCertificateRequestDto>();
	        request.setRequest(uploadCertRequest(isItForRegistrationDevice));
	        request.setId("mosip.partnermanagement.devicedetail.create");
	        request.setVersion("1.0");
	        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
	        request.setMetadata("{}");
	        return request;
	    }
	    
	    private FtpChipCertificateRequestDto uploadCertRequest(boolean isItForRegistrationDevice) {
	    	FtpChipCertificateRequestDto ftpChipCertificateRequestDto = new FtpChipCertificateRequestDto();
	    	ftpChipCertificateRequestDto.setCertificateData("abcd");
	    	ftpChipCertificateRequestDto.setFtpChipDeatilId("1234");
	    	ftpChipCertificateRequestDto.setFtpProviderId("1234");
	    	ftpChipCertificateRequestDto.setIsItForRegistrationDevice(isItForRegistrationDevice);
	    	ftpChipCertificateRequestDto.setOrganizationName("org");
	    	ftpChipCertificateRequestDto.setPartnerDomain("network");
			return ftpChipCertificateRequestDto;
	    }
	    
	    private RequestWrapper<FtpChipCertDownloadRequestDto> getRequest(boolean isItForRegistrationDevice) {
	        RequestWrapper<FtpChipCertDownloadRequestDto> request = new RequestWrapper<FtpChipCertDownloadRequestDto>();
	        request.setRequest(getCertRequest(isItForRegistrationDevice));
	        request.setId("mosip.partnermanagement.devicedetail.create");
	        request.setVersion("1.0");
	        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
	        request.setMetadata("{}");
	        return request;
	    }
	    
	    private FtpChipCertDownloadRequestDto getCertRequest(boolean isItForRegistrationDevice) {
	    	FtpChipCertDownloadRequestDto ftpChipCertDownloadRequestDto = new FtpChipCertDownloadRequestDto();
	    	ftpChipCertDownloadRequestDto.setFtpChipDetailId(null);
	    	return ftpChipCertDownloadRequestDto;
	    
	    }
	    
	   @Test    
	    @WithMockUser(roles = {"PARTNER_ADMIN"})
	    public void createFTPChipDetailTest() throws Exception {
		   RequestWrapper<FtpChipDetailDto> request = createRequest(true);
		   mockMvc.perform(post("/ftpchipdetail").contentType(MediaType.APPLICATION_JSON_VALUE)
	                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
	   }
	   
	   @Test    
	    @WithMockUser(roles = {"PARTNER_ADMIN"})
	    public void createFTPChipDetailTest_01() throws Exception {
		   RequestWrapper<FtpChipDetailDto> request = createRequest(false);
		   mockMvc.perform(post("/ftpchipdetail").contentType(MediaType.APPLICATION_JSON_VALUE)
	                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
	   }
	   
	   @Test
	    @WithMockUser(roles = {"PARTNER_ADMIN"})
	    public void updateFTPDetailsTest() throws Exception {
	    	RequestWrapper<FtpChipDetailUpdateDto> request = updateRequest(true);
	    	mockMvc.perform(put("/ftpchipdetail").contentType(MediaType.APPLICATION_JSON_VALUE)
	                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
	    }
	   
	   @Test
	    @WithMockUser(roles = {"PARTNER_ADMIN"})
	    public void updateFTPDetailsTest_01() throws Exception {
	    	RequestWrapper<FtpChipDetailUpdateDto> request = updateRequest(false);
	    	mockMvc.perform(put("/ftpchipdetail").contentType(MediaType.APPLICATION_JSON_VALUE)
	                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
	    }
	   
	   @Test
	    @WithMockUser(roles = {"PARTNER_ADMIN"})
	    public void approveFTPChipDetailsTest() throws JsonProcessingException, Exception {
	    	RequestWrapper<FtpChipDetailStatusDto> createrequest=approvalRequest(true);
	    	mockMvc.perform(MockMvcRequestBuilders.patch("/ftpchipdetail").contentType(MediaType.APPLICATION_JSON_VALUE)
	                .content(objectMapper.writeValueAsString(createrequest))).andExpect(status().isOk());  
	   }
	   
	   @Test
	    @WithMockUser(roles = {"PARTNER_ADMIN"})
	    public void approveFTPChipDetailsTest_01() throws JsonProcessingException, Exception {
	    	RequestWrapper<FtpChipDetailStatusDto> createrequest=approvalRequest(false);
	    	mockMvc.perform(MockMvcRequestBuilders.patch("/ftpchipdetail").contentType(MediaType.APPLICATION_JSON_VALUE)
	                .content(objectMapper.writeValueAsString(createrequest))).andExpect(status().isOk());  
	   }
	   
	   @Test
	    @WithMockUser(roles = {"PARTNER_ADMIN"})
	    public void FTPChipDetailsSearchTest() throws JsonProcessingException, Exception {
	    	RequestWrapper<DeviceSearchDto> request = searchRequest();
	    	mockMvc.perform(MockMvcRequestBuilders.post("/ftpchipdetail/search").contentType(MediaType.APPLICATION_JSON_VALUE)
	                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
	    }
	    
	    @Test
	    @WithMockUser(roles = {"PARTNER_ADMIN"})
	    public void regFTPChipDetailsSearchTest() throws JsonProcessingException, Exception {
	    	RequestWrapper<DeviceSearchDto> request = RegSearchRequest();
	    	mockMvc.perform(MockMvcRequestBuilders.post("/ftpchipdetail/search").contentType(MediaType.APPLICATION_JSON_VALUE)
	                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
	    }
	    
	    @Test
	    @WithMockUser(roles = {"PARTNER_ADMIN"})
	    public void uploadPartnerCertificateTest() throws JsonProcessingException, Exception {
	    	RequestWrapper<FtpChipCertificateRequestDto> request = uploadRequest(true);
	    	mockMvc.perform(MockMvcRequestBuilders.post("/ftpchipdetail/uploadcertificate").contentType(MediaType.APPLICATION_JSON_VALUE)
	                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
	    }
	    
	    @Test
	    @WithMockUser(roles = {"PARTNER_ADMIN"})
	    public void uploadPartnerCertificateTest_01() throws JsonProcessingException, Exception {
	    	RequestWrapper<FtpChipCertificateRequestDto> request = uploadRequest(false);
	    	mockMvc.perform(MockMvcRequestBuilders.post("/ftpchipdetail/uploadcertificate").contentType(MediaType.APPLICATION_JSON_VALUE)
	                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
	    }
	    
	    @Test
	    @WithMockUser(roles = {"PARTNER_ADMIN"})
	    public void getPartnerCertificateTest() throws JsonProcessingException, Exception {
	    	RequestWrapper<FtpChipCertDownloadRequestDto> request = getRequest(true);
	    	mockMvc.perform(MockMvcRequestBuilders.get("/ftpchipdetail/getPartnerCertificate/1234").contentType(MediaType.APPLICATION_JSON_VALUE)
	                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
	    }
	    
	    @Test
	    @WithMockUser(roles = {"PARTNER_ADMIN"})
	    public void getPartnerCertificateTest_01() throws JsonProcessingException, Exception {
	    	RequestWrapper<FtpChipCertDownloadRequestDto> request = getRequest(false);
	    	mockMvc.perform(MockMvcRequestBuilders.get("/ftpchipdetail/getPartnerCertificate/1234").contentType(MediaType.APPLICATION_JSON_VALUE)
	                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
	    }
	    
	    @Test
	    @WithMockUser(roles = {"PARTNER_ADMIN"})
	    public void getPartnerCertificateTest_02() throws JsonProcessingException, Exception {
	    	RequestWrapper<FtpChipCertDownloadRequestDto> request = getRequest(true);
	    	request.getRequest().setFtpChipDetailId("1234");
	    	FTPChipDetail ftpChipDetailResponse = new FTPChipDetail();
	    	ftpChipDetailResponse.setFtpChipDetailId("1234");
	    	Mockito.when(ftpChipDetaillService.getFtpChipDeatils(Mockito.anyString())).thenReturn(ftpChipDetailResponse);
	    	Mockito.when(ftpChipDetailServiceImpl.getFtpChipDeatils(Mockito.anyString())).thenReturn(ftpChipDetailResponse);
	    	mockMvc.perform(MockMvcRequestBuilders.get("/ftpchipdetail/getPartnerCertificate/1234").contentType(MediaType.APPLICATION_JSON_VALUE)
	                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
	    }

		@Test
		@WithMockUser(roles = {"FTM_PROVIDER"})
		public void deactivateFtmTest() throws Exception {
			ResponseWrapperV2<FtmDetailResponseDto> responseWrapper = new ResponseWrapperV2<>();
			RequestWrapperV2<DeactivateFtmRequestDto> requestWrapper = new RequestWrapperV2<>();
			DeactivateFtmRequestDto requestDto = new DeactivateFtmRequestDto();
			requestDto.setStatus("De-Activate");
			requestWrapper.setRequest(requestDto);
			FtmDetailResponseDto ftmDetailResponseDto = new FtmDetailResponseDto();
			responseWrapper.setResponse(ftmDetailResponseDto);
			Mockito.when(ftpChipDetaillService.deactivateFtm(Mockito.any(), Mockito.any())).thenReturn(responseWrapper);
			mockMvc.perform(MockMvcRequestBuilders.patch("/ftpchipdetail/1232").contentType(MediaType.APPLICATION_JSON_VALUE)
					.content(objectMapper.writeValueAsString(requestWrapper))).andExpect(status().isOk());
		}

	@Test
	@WithMockUser(roles = {"FTM_PROVIDER"})
	public void getFtmCertificateDataTest() throws Exception {
		ResponseWrapperV2<FtmCertificateDownloadResponseDto> responseWrapper = new ResponseWrapperV2<>();
		FtmCertificateDownloadResponseDto ftmCertificateDownloadResponseDto = new FtmCertificateDownloadResponseDto();
		responseWrapper.setResponse(ftmCertificateDownloadResponseDto);
		Mockito.when(ftpChipDetaillService.getFtmCertificateData(Mockito.any())).thenReturn(responseWrapper);
		mockMvc.perform(MockMvcRequestBuilders.get("/ftpchipdetail/1234/certificate-data")).andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void getPartnersFtmDetailsTest() throws Exception {
		String sortFieldName = "createdDateTime";
		String sortType = "desc";
		int pageNo = 0;
		int pageSize = 8;
		FtmChipFilterDto filterDto = new FtmChipFilterDto();
		ResponseWrapperV2<PageResponseV2Dto<FtmDetailSummaryDto>> responseWrapper = new ResponseWrapperV2<>();

		Mockito.when(ftpChipDetailServiceImpl.getPartnersFtmChipDetails(sortFieldName, sortType, pageNo, pageSize, filterDto))
				.thenReturn(responseWrapper);
		mockMvc.perform(MockMvcRequestBuilders.get("/ftpchipdetail/search/v2")
						.param("sortFieldName", sortFieldName)
						.param("sortType", sortType)
						.param("pageNo", String.valueOf(pageNo))
						.param("pageSize", String.valueOf(pageSize))
						.param("partnerId", "123")
						.param("orgName", "ABC")
						.param("ftmId", "ftm123")
						.param("make", "test")
						.param("model", "test")
						.param("status", "approved"))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	@WithMockUser(roles = {"FTM_PROVIDER"})
	public void ftmChipDetailTest() throws Exception {
		ResponseWrapperV2<List<FtmChipDetailsDto>> responseWrapper = new ResponseWrapperV2<>();
		List<FtmChipDetailsDto> ftmChipDetailsDtoList = new ArrayList<>();
		FtmChipDetailsDto ftmChipDetailsDto = new FtmChipDetailsDto();
		ftmChipDetailsDtoList.add(ftmChipDetailsDto);
		responseWrapper.setResponse(ftmChipDetailsDtoList);
		Mockito.when(ftpChipDetaillService.ftmChipDetail()).thenReturn(responseWrapper);
		mockMvc.perform(MockMvcRequestBuilders.get("/ftpchipdetail")).
				andExpect(MockMvcResultMatchers.status().isOk());
	}
}
