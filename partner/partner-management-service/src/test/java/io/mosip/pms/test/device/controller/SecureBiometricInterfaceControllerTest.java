package io.mosip.pms.test.device.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import io.mosip.pms.common.request.dto.RequestWrapperV2;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.device.dto.SbiFilterDto;
import io.mosip.pms.device.response.dto.SbiSummaryDto;
import io.mosip.pms.partner.dto.DeviceDto;
import io.mosip.pms.device.response.dto.SbiDetailsResponseDto;
import io.mosip.pms.device.dto.SbiDetailsDto;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pms.common.constant.Purpose;
import io.mosip.pms.common.dto.FilterDto;
import io.mosip.pms.common.dto.FilterValueDto;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.common.dto.Pagination;
import io.mosip.pms.common.dto.SearchFilter;
import io.mosip.pms.common.dto.SearchSort;
import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.device.authdevice.service.SecureBiometricInterfaceService;
import io.mosip.pms.device.request.dto.DeviceDetailSBIMappingDto;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceCreateDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceStatusUpdateDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceUpdateDto;
import io.mosip.pms.device.request.dto.DeactivateSbiRequestDto;
import io.mosip.pms.device.request.dto.DeviceDetailDto;
import io.mosip.pms.device.response.dto.IdDto;
import io.mosip.pms.device.response.dto.SbiSearchResponseDto;
import io.mosip.pms.device.util.AuditUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@EnableWebMvc
public class SecureBiometricInterfaceControllerTest {
	
	@Autowired
    private MockMvc mockMvc;
   
    @Autowired
    private ObjectMapper objectMapper;
    
	@Autowired
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate restTemplate;
    
    @MockBean
	AuditUtil auditUtil;
	
    @MockBean	
   private  SecureBiometricInterfaceService secureBiometricInterfaceService;
    
    RequestWrapper<SecureBiometricInterfaceCreateDto> createRequest=null;
    RequestWrapper<SecureBiometricInterfaceUpdateDto> updateRequest=null;
    
    @Before
    public void setup() {
    	Mockito.doNothing().when(auditUtil).auditRequest(any(), any(), any());
    	Mockito.doNothing().when(auditUtil).auditRequest(any(), any(), any(),any());
    	PageResponseDto<SbiSearchResponseDto> searchresponse = new PageResponseDto<SbiSearchResponseDto>();
    	IdDto response = new IdDto();
    	ResponseWrapper<IdDto> responseWrapper = new ResponseWrapper<>();
    	ResponseWrapper<PageResponseDto<SbiSearchResponseDto>> searchResponseWrapper = new ResponseWrapper<>();
    	searchResponseWrapper.setResponse(searchresponse);
    	responseWrapper.setResponse(response);
        Mockito.when(secureBiometricInterfaceService.searchSecureBiometricInterface(Mockito.any(), Mockito.any())).thenReturn(searchresponse);
        Mockito.when(secureBiometricInterfaceService.updateSecureBiometricInterface(Mockito.any())).thenReturn(response);
        Mockito.when(secureBiometricInterfaceService.createSecureBiometricInterface(Mockito.any())).thenReturn(response);
        createRequest = createRequest(false);
        updateRequest=updateRequest(false);
    }
    
    private RequestWrapper<DeviceSearchDto> searchRequest() {
    	RequestWrapper<DeviceSearchDto> request = new RequestWrapper<DeviceSearchDto>();
        request.setRequest(searchSBIRequest());
        request.setId("mosip.partnermanagement.sbi.update");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
	}
    
    private DeviceSearchDto searchSBIRequest () {
    	DeviceSearchDto dto = new DeviceSearchDto();
    	Pagination pagination = new Pagination();
    	SearchSort searchSort = new SearchSort();
    	SearchFilter searchFilter = new SearchFilter();
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
    	pagination.setPageFetch(10);
    	pagination.setPageStart(0);
    	dto.setFilters(searchfilterDtos);
    	dto.setPagination(pagination);
    	dto.setPurpose(Purpose.AUTH.toString());
    	dto.setSort(searchDtos1);
    	return dto;
    }
    
    private RequestWrapper<DeviceSearchDto> searchRegRequest() {
    	RequestWrapper<DeviceSearchDto> request = new RequestWrapper<DeviceSearchDto>();
        request.setRequest(searchRegSBIRequest());
        request.setId("mosip.partnermanagement.sbi.update");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
	}
    
    private RequestWrapper<FilterValueDto> createFilterRequest(){
    	RequestWrapper<FilterValueDto> request = new RequestWrapper<FilterValueDto>();
    	request.setId("mosip.partnermanagement.sbi.filtervalues");
    	request.setMetadata("{}");
    	request.setRequest(filterValuesRequest());
    	request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
    	request.setVersion("1.0");
    	return request;
    }
    
    private FilterValueDto filterValuesRequest() {
    	FilterValueDto sbiFilterValueDto = new FilterValueDto();
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
    	sbiFilterValueDto.setFilters(filterDtos);    	
    	return sbiFilterValueDto;
    }
    
    private DeviceSearchDto searchRegSBIRequest () {
    	DeviceSearchDto dto = new DeviceSearchDto();
    	Pagination pagination = new Pagination();
    	SearchSort searchSort = new SearchSort();
    	SearchFilter searchFilter = new SearchFilter();
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
    	pagination.setPageFetch(10);
    	pagination.setPageStart(0);
    	dto.setFilters(searchfilterDtos);
    	dto.setPagination(pagination);
    	dto.setPurpose(Purpose.REGISTRATION.toString());
    	dto.setSort(searchDtos1);
    	return dto;
    }
    
    private RequestWrapper<SecureBiometricInterfaceUpdateDto> updateRequest(boolean isItForRegistrationDevice) {
    	RequestWrapper<SecureBiometricInterfaceUpdateDto> request = new RequestWrapper<SecureBiometricInterfaceUpdateDto>();
        request.setRequest(createSBIUpdate(isItForRegistrationDevice));
        request.setId("mosip.partnermanagement.sbi.update");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
	}

	private SecureBiometricInterfaceUpdateDto createSBIUpdate(boolean isItForRegistrationDevice) {
		SecureBiometricInterfaceUpdateDto sbidto = new SecureBiometricInterfaceUpdateDto();
    	List<String> deviceDetails = new ArrayList<>();
    	deviceDetails.add("1234");
    	sbidto.setSwBinaryHash("swb");
    	sbidto.setSwCreateDateTime(LocalDateTime.now());
    	sbidto.setSwExpiryDateTime(LocalDateTime.now());
    	sbidto.setIsActive(true);
    	sbidto.setSwVersion("v1");
    	sbidto.setId("1234");
        return sbidto;
	}

	private RequestWrapper<SecureBiometricInterfaceCreateDto> createRequest(boolean isItForRegistrationDevice) {
        RequestWrapper<SecureBiometricInterfaceCreateDto> request = new RequestWrapper<SecureBiometricInterfaceCreateDto>();
        request.setRequest(createSBIRequest(isItForRegistrationDevice));
        request.setId("mosip.partnermanagement.sbi.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
    }
    
    private SecureBiometricInterfaceCreateDto createSBIRequest(boolean isItForRegistrationDevice) {
    	SecureBiometricInterfaceCreateDto sbidto = new SecureBiometricInterfaceCreateDto();
    	sbidto.setProviderId("1234");
    	sbidto.setSwBinaryHash("swb");
    	sbidto.setSwCreateDateTime(LocalDateTime.now());
    	sbidto.setSwExpiryDateTime(LocalDateTime.now().plusYears(10));
    	sbidto.setSwVersion("v1");
        
        return sbidto;
    }
     
    private RequestWrapper<SecureBiometricInterfaceStatusUpdateDto>approvalRequest(boolean isItForRegistrationDevice) {
        RequestWrapper<SecureBiometricInterfaceStatusUpdateDto> request = new RequestWrapper<SecureBiometricInterfaceStatusUpdateDto>();
        request.setRequest(approvalDetailRequest(isItForRegistrationDevice));
        request.setId("mosip.partnermanagement.devicedetail.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
    }
    
    private SecureBiometricInterfaceStatusUpdateDto approvalDetailRequest(boolean isItForRegistrationDevice) {
    	SecureBiometricInterfaceStatusUpdateDto dto = new SecureBiometricInterfaceStatusUpdateDto();
    	dto.setApprovalStatus("Activate");
    	dto.setId("123456");
    	return dto;
    }
    
    @Test
    @WithMockUser(roles = {"DEVICE_PROVIDER"})
    public void createsbiTest() throws Exception {      

        mockMvc.perform(post("/securebiometricinterface").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(createRequest))).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"DEVICE_PROVIDER"})
    public void updatesbiTest() throws Exception {
    	mockMvc.perform(put("/securebiometricinterface").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(updateRequest))).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void approveDeviceDetailsTest() throws JsonProcessingException, Exception {
    	RequestWrapper<SecureBiometricInterfaceStatusUpdateDto> createrequest=approvalRequest(false);
    	mockMvc.perform(MockMvcRequestBuilders.patch("/securebiometricinterface").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(createrequest))).andExpect(status().isOk());    	
    }
    
    @Test
    @WithMockUser(roles = {"DEVICE_PROVIDER"})
    public void searchSecureBiometricTest() throws JsonProcessingException, Exception {
    	RequestWrapper<DeviceSearchDto> createrequest=searchRequest();
    	mockMvc.perform(MockMvcRequestBuilders.post("/securebiometricinterface/search").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(createrequest))).andExpect(status().isOk());    	
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void approveDeviceDetailsTest_regDevice() throws JsonProcessingException, Exception {
    	RequestWrapper<SecureBiometricInterfaceStatusUpdateDto> createrequest=approvalRequest(true);
    	mockMvc.perform(MockMvcRequestBuilders.patch("/securebiometricinterface").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(createrequest))).andExpect(status().isOk());    	
    }
    
    @Test
    @WithMockUser(roles = {"DEVICE_PROVIDER"})
    public void createsbiTest_regDevice() throws Exception {
    	RequestWrapper<SecureBiometricInterfaceCreateDto> createRequest=createRequest(true);
        mockMvc.perform(post("/securebiometricinterface").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(createRequest))).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"DEVICE_PROVIDER"})
    public void updatesbiTest_regDevice() throws Exception {
    	RequestWrapper<SecureBiometricInterfaceUpdateDto> updateRequest=updateRequest(true);
    	mockMvc.perform(put("/securebiometricinterface").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(updateRequest))).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"DEVICE_PROVIDER"})
    public void searchRegSecureBiometricTest() throws JsonProcessingException, Exception {
    	RequestWrapper<DeviceSearchDto> createrequest=searchRegRequest();
    	mockMvc.perform(MockMvcRequestBuilders.post("/securebiometricinterface/search").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(createrequest))).andExpect(status().isOk());    	
    }
    
    @Test
    @WithMockUser(roles = {"DEVICE_PROVIDER"})
    public void filterSecureBiometricTest() throws JsonProcessingException, Exception {
    	RequestWrapper<FilterValueDto> createrequest=createFilterRequest();
    	mockMvc.perform(MockMvcRequestBuilders.post("/securebiometricinterface/filtervalues").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(createrequest))).andExpect(status().isOk());    	
    }
    
    @Test
    @WithMockUser(roles = {"DEVICE_PROVIDER"})
    public void mapDeviceDetailsTest() throws JsonProcessingException, Exception {
    	RequestWrapper<DeviceDetailSBIMappingDto> createrequest=createMappingRequest();
    	mockMvc.perform(MockMvcRequestBuilders.put("/securebiometricinterface/devicedetails/map").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(createrequest))).andExpect(status().isOk());    	
    }
    
    @Test
    @WithMockUser(roles = {"DEVICE_PROVIDER"})
    public void removeMapDeviceDetailsTest() throws JsonProcessingException, Exception {
    	RequestWrapper<DeviceDetailSBIMappingDto> removeMappingrequest=createMappingRequest();
    	mockMvc.perform(MockMvcRequestBuilders.put("/securebiometricinterface/devicedetails/map/remove").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(removeMappingrequest))).andExpect(status().isOk());    	
    }
    
    @Test
    @WithMockUser(roles = {"DEVICE_PROVIDER"})
    public void searchMappedDeviceDetailsSecureBiometricTest() throws JsonProcessingException, Exception {
    	RequestWrapper<DeviceSearchDto> createrequest=searchRequest();
    	mockMvc.perform(MockMvcRequestBuilders.post("/securebiometricinterface/devicedetails/map/search").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(createrequest))).andExpect(status().isOk());    	
    }

	@Test
	@WithMockUser(roles = {"DEVICE_PROVIDER"})
	public void getAllDevicesForSbiTest() throws Exception {
		ResponseWrapperV2<List<DeviceDto>> responseWrapper = new ResponseWrapperV2<>();
		List<DeviceDto> deviceDtoList = new ArrayList<>();
		DeviceDto deviceDto = new DeviceDto();
		deviceDtoList.add(deviceDto);
		responseWrapper.setResponse(deviceDtoList);
		Mockito.when(secureBiometricInterfaceService.getAllDevicesForSbi(Mockito.any())).thenReturn(responseWrapper);
		mockMvc.perform(MockMvcRequestBuilders.get("/securebiometricinterface/123/devices").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(responseWrapper))).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = {"DEVICE_PROVIDER"})
	public void deactivateSbiTest() throws Exception {
		RequestWrapperV2<DeactivateSbiRequestDto> requestWrapper = new RequestWrapperV2<>();
		DeactivateSbiRequestDto requestDto = new DeactivateSbiRequestDto();
		requestDto.setStatus("De-Activate");
		requestWrapper.setRequest(requestDto);
		ResponseWrapperV2<SbiDetailsResponseDto> responseWrapper = new ResponseWrapperV2<>();
		SbiDetailsResponseDto sbiDetailsResponseDto = new SbiDetailsResponseDto();
		responseWrapper.setResponse(sbiDetailsResponseDto);

		Mockito.when(secureBiometricInterfaceService.deactivateSbi(Mockito.anyString(), Mockito.any())).thenReturn(responseWrapper);

		mockMvc.perform(MockMvcRequestBuilders.patch("/securebiometricinterface/1234").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(requestWrapper))).andExpect(status().isOk());
	}

	private RequestWrapper<DeviceDetailSBIMappingDto> createMappingRequest() {
		RequestWrapper<DeviceDetailSBIMappingDto> request = new RequestWrapper<DeviceDetailSBIMappingDto>();
		DeviceDetailSBIMappingDto mappingDto = new DeviceDetailSBIMappingDto();
		mappingDto.setDeviceDetailId("devicedetailid");
		mappingDto.setSbiId("sbiid");
		request.setRequest(mappingDto);
		return request;
	}

	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void getAllSbiDetailsTest() throws Exception {
		String sortFieldName = "createdDateTime";
		String sortType = "desc";
		Integer pageNo = 0;
		Integer pageSize = 8;
		SbiFilterDto filterDto = new SbiFilterDto();
		ResponseWrapperV2<PageResponseV2Dto<SbiSummaryDto>> responseWrapper = new ResponseWrapperV2<>();
		Mockito.when(secureBiometricInterfaceService.getAllSbiDetails(sortFieldName, sortType, pageNo, pageSize, filterDto)).thenReturn(responseWrapper);
		mockMvc.perform(MockMvcRequestBuilders.get("/securebiometricinterface")
						.param("sortFieldName", sortFieldName)
						.param("sortType", sortType)
						.param("pageNo", String.valueOf(pageNo))
						.param("pageSize", String.valueOf(pageSize))
						.param("partnerId", "123")
						.param("orgName", "ABC")
						.param("sbiId", "sbi123")
						.param("sbiVersion", "test")
						.param("status", "approved")
						.param("sbiExpiryStatus", "expired"))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	@WithMockUser(roles = {"DEVICE_PROVIDER"})
	public void addDeviceToSbiTest() throws Exception {
		RequestWrapperV2<DeviceDetailDto> requestWrapper = new RequestWrapperV2<>();
		requestWrapper.setId("mosip.pms.add.device.to.sbi.id.post");
		requestWrapper.setVersion("1.0");
		DeviceDetailDto requestDto = new DeviceDetailDto();
		requestDto.setId(null);
		requestDto.setDeviceTypeCode("Finger");
		requestDto.setDeviceSubTypeCode("Slap");
		requestDto.setMake("make");
		requestDto.setModel("model");
		requestDto.setDeviceProviderId("mosip123");
		requestWrapper.setRequest(requestDto);
		ResponseWrapperV2<IdDto> responseWrapper = new ResponseWrapperV2<>();
		responseWrapper.setId("mosip.pms.add.device.to.sbi.id.post");
		requestWrapper.setVersion("1.0");
		IdDto dto = new IdDto();
		dto.setId("12345");
		responseWrapper.setResponse(dto);
		Mockito.when(secureBiometricInterfaceService.addDeviceToSbi(requestDto, "sbi123")).thenReturn(responseWrapper);
		mockMvc.perform(MockMvcRequestBuilders.post("/securebiometricinterface/sbi123/devices").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(requestWrapper))).andExpect(status().isOk());
	}
}
