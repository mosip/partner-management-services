package io.mosip.pms.test.device.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.pms.common.constant.Purpose;
import io.mosip.pms.common.dto.*;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.request.dto.RequestWrapperV2;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.device.authdevice.service.impl.DeviceDetailServiceImpl;
import io.mosip.pms.device.dto.DeviceDetailFilterDto;
import io.mosip.pms.device.dto.DeviceDetailSummaryDto;
import io.mosip.pms.device.request.dto.DeviceDetailDto;
import io.mosip.pms.device.request.dto.DeviceDetailUpdateDto;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.DeactivateDeviceRequestDto;
import io.mosip.pms.device.request.dto.UpdateDeviceDetailStatusDto;
import io.mosip.pms.device.response.dto.*;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.partner.request.dto.SbiAndDeviceMappingRequestDto;
import io.mosip.pms.partner.util.PartnerHelper;
import io.mosip.pms.test.PartnerManagementServiceTest;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = PartnerManagementServiceTest.class)
@AutoConfigureMockMvc
@EnableWebMvc
public class DeviceDetailControllerTest {

    public static final String VERSION = "1.0";

	@Autowired
    private MockMvc mockMvc;
   
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
	AuditUtil auditUtil;
    
	@Autowired
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate restTemplate;
    
    @MockBean	
    private DeviceDetailServiceImpl deviceDetaillService;

    @MockBean
    PartnerHelper partnerHelper;
    
    @Before
    public void setup() {
    	doNothing().when(auditUtil).auditRequest(any(), any(), any());
    	doNothing().when(auditUtil).auditRequest(any(), any(), any(),any());
    	
    	IdDto response = new IdDto();
    	String stringResponse = new String();
    	PageResponseDto<DeviceDetailSearchResponseDto> searchResponse = new PageResponseDto<DeviceDetailSearchResponseDto>();
    	PageResponseDto<RegistrationSubTypeDto> searchTypeResponse = new PageResponseDto<RegistrationSubTypeDto>();
    	FilterResponseCodeDto filterResponse = new FilterResponseCodeDto();
    	ResponseWrapper<IdDto> responseWrapper = new ResponseWrapper<>();
    	ResponseWrapper<String> responsewrapper = new ResponseWrapper<>();
    	ResponseWrapper<PageResponseDto<DeviceDetailSearchResponseDto>> searchResponseWrapper = new ResponseWrapper<>();
    	ResponseWrapper<PageResponseDto<RegistrationSubTypeDto>> searchTypeResponseWrapper = new ResponseWrapper<>();
    	ResponseWrapper<FilterResponseCodeDto> filterResponseWrapper = new  ResponseWrapper<>();
    	filterResponseWrapper.setResponse(filterResponse);
    	searchTypeResponseWrapper.setResponse(searchTypeResponse);
    	searchResponseWrapper.setResponse(searchResponse);
    	responseWrapper.setResponse(response);
    	responsewrapper.setResponse(stringResponse);
        when(deviceDetaillService.updateDeviceDetails(Mockito.any())).thenReturn(response);
        when(deviceDetaillService.createDeviceDetails(Mockito.any())).thenReturn(response);
        when(deviceDetaillService.updateDeviceDetailStatus(Mockito.any())).thenReturn(stringResponse);
        when(deviceDetaillService.searchDeviceDetails(Mockito.any(), Mockito.any())).thenReturn(searchResponse);
        when(deviceDetaillService.searchDeviceType(Mockito.any(), Mockito.any())).thenReturn(searchTypeResponse);
        when(deviceDetaillService.deviceFilterValues(Mockito.any())).thenReturn(filterResponse);
        when(deviceDetaillService.deviceTypeFilterValues(Mockito.any())).thenReturn(filterResponse);
        when(deviceDetaillService.deviceSubTypeFilterValues(Mockito.any())).thenReturn(filterResponse);
    }
    
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    private RequestWrapper<DeviceDetailDto> createRequest(boolean isItForRegistrationDevice) {
        RequestWrapper<DeviceDetailDto> request = new RequestWrapper<DeviceDetailDto>();
        request.setRequest(createdetailRequest(isItForRegistrationDevice));
        request.setId("mosip.partnermanagement.devicedetail.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
    }
    
    private DeviceDetailDto createdetailRequest(boolean isItForRegistrationDevice ) {
    	DeviceDetailDto deviceDetailDto = new DeviceDetailDto();
    	
    	deviceDetailDto.setDeviceProviderId("1234");
    	deviceDetailDto.setDeviceSubTypeCode("123");
    	deviceDetailDto.setDeviceTypeCode("123");
    	deviceDetailDto.setId("121");
    	deviceDetailDto.setMake("make");
    	deviceDetailDto.setModel("model");
        
        return deviceDetailDto;
    }
    
    private RequestWrapper<DeviceDetailUpdateDto>updateRequest(boolean isItForRegistrationDevice) {
        RequestWrapper<DeviceDetailUpdateDto> request = new RequestWrapper<DeviceDetailUpdateDto>();
        request.setRequest(updatedetailRequest(isItForRegistrationDevice));
        request.setId("mosip.partnermanagement.devicedetail.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
    }
    
    private DeviceDetailUpdateDto updatedetailRequest(boolean isItForRegistrationDevice) {
    	DeviceDetailUpdateDto deviceDetailDto = new DeviceDetailUpdateDto();
    	deviceDetailDto.setDeviceSubTypeCode("123");
    	deviceDetailDto.setDeviceTypeCode("123");
    	deviceDetailDto.setId("121");
    	deviceDetailDto.setMake("make");
    	deviceDetailDto.setModel("model");
        
        return deviceDetailDto;
    }
     
    private RequestWrapper<UpdateDeviceDetailStatusDto>approvalRequest(boolean isItForRegistrationDevice) {
        RequestWrapper<UpdateDeviceDetailStatusDto> request = new RequestWrapper<UpdateDeviceDetailStatusDto>();
        request.setRequest(approvalDetailRequest(isItForRegistrationDevice));
        request.setId("mosip.partnermanagement.devicedetail.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
    }
    
    private UpdateDeviceDetailStatusDto approvalDetailRequest(boolean isItForRegistrationDevice) {
    	UpdateDeviceDetailStatusDto dto = new UpdateDeviceDetailStatusDto();
    	dto.setApprovalStatus("Activate");
    	dto.setId("123456");
    	return dto;
    }
    
    private RequestWrapper<DeviceSearchDto> RegSearchRequest() {
        RequestWrapper<DeviceSearchDto> request = new RequestWrapper<DeviceSearchDto>();
        request.setRequest(searchRegDetailRequest());
        request.setId("mosip.partnermanagement.devicedetail.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
    }
    
    private DeviceSearchDto searchRegDetailRequest() {
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
        request.setRequest(searchDetailRequest());
        request.setId("mosip.partnermanagement.devicedetail.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
    }
    
    private DeviceSearchDto searchDetailRequest() {
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
    
    private RequestWrapper<DeviceFilterValueDto> filterRegRequest() {
        RequestWrapper<DeviceFilterValueDto> request = new RequestWrapper<DeviceFilterValueDto>();
        request.setRequest(filterRegDetailRequest());
        request.setId("mosip.partnermanagement.devicedetail.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
    }
    
    private DeviceFilterValueDto filterRegDetailRequest() {
    	DeviceFilterValueDto dto = new DeviceFilterValueDto();
    	SearchFilter searchFilter = new SearchFilter();
    	FilterDto filterDto = new FilterDto();
    	filterDto.setColumnName("model");
    	filterDto.setText("");
    	filterDto.setType("all");
    	searchFilter.setColumnName("model");
    	searchFilter.setFromValue("");
    	searchFilter.setToValue("");
    	searchFilter.setType("STARTSWITH");
    	searchFilter.setValue("b");
       	List<SearchFilter> searchfilterDtos = new ArrayList<SearchFilter>();
    	searchfilterDtos.add(searchFilter);
    	List<FilterDto> filterDtos = new ArrayList<FilterDto>();
    	filterDtos.add(filterDto);
    	dto.setFilters(filterDtos);    	
    	dto.setPurpose(Purpose.REGISTRATION);
		return dto;
    
    }
    
    private RequestWrapper<DeviceFilterValueDto> filterRequest() {
        RequestWrapper<DeviceFilterValueDto> request = new RequestWrapper<DeviceFilterValueDto>();
        request.setRequest(filterDetailRequest());
        request.setId("mosip.partnermanagement.devicedetail.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
    }
    
    private DeviceFilterValueDto filterDetailRequest() {
    	DeviceFilterValueDto dto = new DeviceFilterValueDto();
    	SearchFilter searchFilter = new SearchFilter();
    	FilterDto filterDto = new FilterDto();
    	filterDto.setColumnName("model");
    	filterDto.setText("");
    	filterDto.setType("all");
    	searchFilter.setColumnName("model");
    	searchFilter.setFromValue("");
    	searchFilter.setToValue("");
    	searchFilter.setType("STARTSWITH");
    	searchFilter.setValue("b");
       	List<SearchFilter> searchfilterDtos = new ArrayList<SearchFilter>();
    	searchfilterDtos.add(searchFilter);
    	List<FilterDto> filterDtos = new ArrayList<FilterDto>();
    	filterDtos.add(filterDto);
    	dto.setFilters(filterDtos);    	
    	dto.setPurpose(Purpose.AUTH);
		return dto;
    
    }
    
    @Test    
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void createDeviceDetailTest() throws Exception {
    	RequestWrapper<DeviceDetailDto> request = createRequest (true);
        mockMvc.perform(post("/devicedetail").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }
    
    @Test    
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void createDeviceDetailTest_01() throws Exception {
    	RequestWrapper<DeviceDetailDto> request = createRequest (false);
        mockMvc.perform(post("/devicedetail").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void updateDeviceDetailsTest() throws Exception {
    	RequestWrapper<DeviceDetailUpdateDto> request = updateRequest(true);
    	mockMvc.perform(put("/devicedetail").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void updateDeviceDetailsTest_01() throws Exception {
    	RequestWrapper<DeviceDetailUpdateDto> request = updateRequest(false);
    	mockMvc.perform(put("/devicedetail").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void approveDeviceDetailsTest() throws JsonProcessingException, Exception {
    	RequestWrapper<UpdateDeviceDetailStatusDto> createrequest=approvalRequest(true);
    	mockMvc.perform(MockMvcRequestBuilders.patch("/devicedetail").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(createrequest))).andExpect(status().isOk());    	
    }
    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void approveDeviceDetailsTest_01() throws JsonProcessingException, Exception {
    	RequestWrapper<UpdateDeviceDetailStatusDto> createrequest=approvalRequest(false);
    	mockMvc.perform(MockMvcRequestBuilders.patch("/devicedetail").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(createrequest))).andExpect(status().isOk());    	
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void deviceDetailsSearchTest() throws JsonProcessingException, Exception {
    	RequestWrapper<DeviceSearchDto> request = searchRequest();
    	mockMvc.perform(MockMvcRequestBuilders.post("/devicedetail/search").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void regDeviceDetailsSearchTest() throws JsonProcessingException, Exception {
    	RequestWrapper<DeviceSearchDto> request = RegSearchRequest();
    	mockMvc.perform(MockMvcRequestBuilders.post("/devicedetail/search").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void deviceTypeSearchTest() throws JsonProcessingException, Exception {
    	RequestWrapper<DeviceSearchDto> request = searchRequest();
    	mockMvc.perform(MockMvcRequestBuilders.post("/devicedetail/deviceType/search").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
       }   

    @Test    
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void createDeviceDetailTest_regdevice() throws Exception {
    	RequestWrapper<DeviceDetailDto> createrequest=createRequest(true);
        mockMvc.perform(post("/devicedetail").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(createrequest))).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void regDeviceTypeSearchTest() throws JsonProcessingException, Exception {
    	RequestWrapper<DeviceSearchDto> request = RegSearchRequest();
    	mockMvc.perform(MockMvcRequestBuilders.post("/devicedetail/deviceType/search").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void filterRegDeviceTest() throws JsonProcessingException, Exception {
    	RequestWrapper<DeviceFilterValueDto> request = filterRegRequest();
    	mockMvc.perform(MockMvcRequestBuilders.post("/devicedetail/filtervalues").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void filterDeviceTest() throws JsonProcessingException, Exception {
    	RequestWrapper<DeviceFilterValueDto> request = filterRequest();
    	mockMvc.perform(MockMvcRequestBuilders.post("/devicedetail/filtervalues").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void filterRegDeviceTypeTest() throws JsonProcessingException, Exception {
    	RequestWrapper<DeviceFilterValueDto> request = filterRegRequest();
    	mockMvc.perform(MockMvcRequestBuilders.post("/devicedetail/deviceType/filtervalues").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void filterDeviceTypeTest() throws JsonProcessingException, Exception {
    	RequestWrapper<DeviceFilterValueDto> request = filterRequest();
    	mockMvc.perform(MockMvcRequestBuilders.post("/devicedetail/deviceType/filtervalues").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void filterRegDeviceSubTypeTest() throws JsonProcessingException, Exception {
    	RequestWrapper<DeviceFilterValueDto> request = filterRegRequest();
    	mockMvc.perform(MockMvcRequestBuilders.post("/devicedetail/deviceSubType/filtervalues").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void filterDeviceSubTypeTest() throws JsonProcessingException, Exception {
    	RequestWrapper<DeviceFilterValueDto> request = filterRequest();
    	mockMvc.perform(MockMvcRequestBuilders.post("/devicedetail/deviceSubType/filtervalues").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"DEVICE_PROVIDER"})
    public void deactivateDeviceTest() throws Exception {
        RequestWrapperV2<DeactivateDeviceRequestDto> requestWrapper = new RequestWrapperV2<>();
        DeactivateDeviceRequestDto requestDto = new DeactivateDeviceRequestDto();
        requestDto.setStatus("De-Activate");
        requestWrapper.setRequest(requestDto);
        ResponseWrapperV2<DeviceDetailResponseDto> responseWrapper = new ResponseWrapperV2<>();
        DeviceDetailResponseDto deviceDetailResponseDto = new DeviceDetailResponseDto();
        responseWrapper.setResponse(deviceDetailResponseDto);

        Mockito.when(deviceDetaillService.deactivateDevice(Mockito.anyString(), Mockito.any())).thenReturn(responseWrapper);

        mockMvc.perform(MockMvcRequestBuilders.patch("/devicedetail/12345").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(requestWrapper))).andExpect(status().isOk());
    }


    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void getAllDeviceDetailsTest() throws Exception {
        String sortFieldName = "deviceId";
        String sortType = "asc";
        Integer pageNo = 0;
        Integer pageSize = 8;
        String partnerId = "test-partner";
        String orgName = "test-org";
        String deviceType = "Finger";
        String deviceSubType = "single";
        String status = "approved";
        String make = "test-make";
        String model = "test-model";
        String sbiId = "test-sbi-id";
        String sbiVersion = "1.0";
        String deviceId = "test-device-id";

        ResponseWrapperV2<PageResponseV2Dto<DeviceDetailSummaryDto>> mockResponse = new ResponseWrapperV2<>();
        when(deviceDetaillService.getAllDeviceDetails(eq(sortFieldName), eq(sortType), eq(pageNo), eq(pageSize), any(DeviceDetailFilterDto.class)))
                .thenReturn(mockResponse);

        doNothing().when(partnerHelper).validateRequestParameters(any(), eq(sortFieldName), eq(sortType), eq(pageNo), eq(pageSize));

        mockMvc.perform(MockMvcRequestBuilders.get("/devicedetail")
                        .param("sortFieldName", sortFieldName)
                        .param("sortType", sortType)
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .param("partnerId", partnerId)
                        .param("orgName", orgName)
                        .param("deviceType", deviceType)
                        .param("deviceSubType", deviceSubType)
                        .param("status", status)
                        .param("make", make)
                        .param("model", model)
                        .param("sbiId", sbiId)
                        .param("sbiVersion", sbiVersion)
                        .param("deviceId", deviceId))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void approveOrRejectMappingDeviceToSbiTest() throws Exception {
        RequestWrapperV2<SbiAndDeviceMappingRequestDto> requestWrapper = new RequestWrapperV2<>();
        requestWrapper.setId("mosip.pms.mapping.device.to.sbi.post");
        requestWrapper.setVersion(VERSION);
        requestWrapper.setRequestTime(LocalDateTime.now());
        SbiAndDeviceMappingRequestDto sbiAndDeviceMappingRequestDto = new SbiAndDeviceMappingRequestDto();
        requestWrapper.setRequest(sbiAndDeviceMappingRequestDto);
        ResponseWrapperV2<Boolean> responseWrapper = new ResponseWrapperV2<>();
        responseWrapper.setResponse(true);
        Mockito.when(deviceDetaillService.approveOrRejectMappingDeviceToSbi(any(), any())).thenReturn(responseWrapper);
        mockMvc.perform(post("/devicedetail/1234/approval").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(requestWrapper))).andExpect(status().isOk());
    }
    
}
