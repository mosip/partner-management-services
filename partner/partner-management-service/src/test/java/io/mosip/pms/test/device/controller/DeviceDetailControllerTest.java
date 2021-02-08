package io.mosip.pms.test.device.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.Before;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.device.authdevice.service.impl.DeviceDetailServiceImpl;
import io.mosip.pms.device.regdevice.service.impl.RegDeviceDetailServiceImpl;
import io.mosip.pms.device.request.dto.DeviceDetailDto;
import io.mosip.pms.device.request.dto.DeviceDetailUpdateDto;
import io.mosip.pms.device.response.dto.DeviceDetailSearchResponseDto;
import io.mosip.pms.device.response.dto.FilterResponseCodeDto;
import io.mosip.pms.device.response.dto.IdDto;
import io.mosip.pms.device.response.dto.RegistrationSubTypeDto;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.test.PartnerManagementServiceTest;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = PartnerManagementServiceTest.class)
public class DeviceDetailControllerTest {
	
	@Autowired
    private MockMvc mockMvc;
   
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
	AuditUtil auditUtil;
    
    @MockBean	
    private DeviceDetailServiceImpl deviceDetaillService;

    @MockBean	
    private RegDeviceDetailServiceImpl regDeviceDetaillService;
    
    @Before
    public void setup() {
    	Mockito.doNothing().when(auditUtil).auditRequest(Mockito.any(),Mockito.any(),Mockito.any());
    	Mockito.doNothing().when(auditUtil).auditRequest(Mockito.any(),Mockito.any(),Mockito.any(),Mockito.any());
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
    	Mockito.when(regDeviceDetaillService.regDeviceFilterValues(Mockito.any())).thenReturn(filterResponse);
    	Mockito.when(regDeviceDetaillService.regDeviceTypeFilterValues(Mockito.any())).thenReturn(filterResponse);
    	Mockito.when(regDeviceDetaillService.regDeviceSubTypeFilterValues(Mockito.any())).thenReturn(filterResponse);
    	Mockito.when(regDeviceDetaillService.searchDeviceType(Mockito.any(), Mockito.any())).thenReturn(searchTypeResponse);
    	Mockito.when(regDeviceDetaillService.searchDeviceDetails(Mockito.any(), Mockito.any())).thenReturn(searchResponse);
        Mockito.when(regDeviceDetaillService.updateDeviceDetails(Mockito.any())).thenReturn(response);
        Mockito.when(regDeviceDetaillService.createDeviceDetails(Mockito.any())).thenReturn(response);
        Mockito.when(regDeviceDetaillService.updateDeviceDetailStatus(Mockito.any())).thenReturn(stringResponse);
        Mockito.when(deviceDetaillService.updateDeviceDetails(Mockito.any())).thenReturn(response);
        Mockito.when(deviceDetaillService.createDeviceDetails(Mockito.any())).thenReturn(response);
        Mockito.when(deviceDetaillService.updateDeviceDetailStatus(Mockito.any())).thenReturn(stringResponse);
        Mockito.when(deviceDetaillService.searchDeviceDetails(Mockito.any(), Mockito.any())).thenReturn(searchResponse);
        Mockito.when(deviceDetaillService.searchDeviceType(Mockito.any(), Mockito.any())).thenReturn(searchTypeResponse);
        Mockito.when(deviceDetaillService.deviceFilterValues(Mockito.any())).thenReturn(filterResponse);
        Mockito.when(deviceDetaillService.deviceTypeFilterValues(Mockito.any())).thenReturn(filterResponse);
        Mockito.when(deviceDetaillService.deviceSubTypeFilterValues(Mockito.any())).thenReturn(filterResponse);
    }
    
	@Test    
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void createAuthDeviceDetailTest() throws Exception {
        mockMvc.perform(post("/devicedetail").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(createRequest(false)))).andExpect(status().isOk());
    }
	
	@Test    
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void createRegDeviceDetailTest() throws Exception {
        mockMvc.perform(post("/devicedetail").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(createRequest(true)))).andExpect(status().isOk());
    }
	
	@SuppressWarnings("unused")
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

    	deviceDetailDto.setDeviceProviderId("1234");
    	deviceDetailDto.setDeviceSubTypeCode("123");
    	deviceDetailDto.setDeviceTypeCode("123");
    	deviceDetailDto.setId("121");
    	deviceDetailDto.setIsItForRegistrationDevice(isItForRegistrationDevice);
    	deviceDetailDto.setMake("make");
    	deviceDetailDto.setModel("model");
    	deviceDetailDto.setPartnerOrganizationName("pog");

        return deviceDetailDto;
    }
	private DeviceDetailDto createdetailRequest(boolean isItForRegistrationDevice ) {
    	DeviceDetailDto deviceDetailDto = new DeviceDetailDto();
    	
    	deviceDetailDto.setDeviceProviderId("1234");
    	deviceDetailDto.setDeviceSubTypeCode("123");
    	deviceDetailDto.setDeviceTypeCode("123");
    	deviceDetailDto.setId("121");
    	
    	deviceDetailDto.setIsItForRegistrationDevice(isItForRegistrationDevice);
    	deviceDetailDto.setMake("make");
    	deviceDetailDto.setModel("model");
    	deviceDetailDto.setPartnerOrganizationName("pog");
        
        return deviceDetailDto;
    }
	
    private RequestWrapper<DeviceDetailDto> createRequest(boolean isItForRegistrationDevice) {
        RequestWrapper<DeviceDetailDto> request = new RequestWrapper<DeviceDetailDto>();
        request.setRequest(createdetailRequest(isItForRegistrationDevice));
        request.setId("mosip.partnermanagement.devicedetail.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
    }
}
