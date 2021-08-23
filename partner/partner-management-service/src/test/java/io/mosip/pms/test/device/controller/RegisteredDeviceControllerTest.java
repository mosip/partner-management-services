package io.mosip.pms.test.device.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
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
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pms.common.constant.Purpose;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.common.dto.Pagination;
import io.mosip.pms.common.dto.SearchFilter;
import io.mosip.pms.common.dto.SearchSort;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.device.authdevice.entity.RegisteredDevice;
import io.mosip.pms.device.authdevice.service.impl.RegisteredDeviceServiceImpl;
import io.mosip.pms.device.regdevice.service.impl.RegRegisteredDeviceServiceImpl;
import io.mosip.pms.device.request.dto.DeRegisterDevicePostDto;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.RegisteredDevicePostDto;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.test.PartnerManagementServiceTest;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PartnerManagementServiceTest.class)
@AutoConfigureMockMvc
@EnableWebMvc
@Ignore
public class RegisteredDeviceControllerTest {
	@Autowired
    private MockMvc mockMvc;
   
    @Autowired
    private ObjectMapper objectMapper;
	
    @MockBean	
    private RegisteredDeviceServiceImpl registeredDeviceService;
	
    @MockBean	
    private RegRegisteredDeviceServiceImpl regRegisteredDeviceService;
    
    @MockBean
   	AuditUtil auditUtil;
    
    @Before
    public void setup() throws Exception {
    	String response="";
    	PageResponseDto<RegisteredDevice> searchResponse = new PageResponseDto<RegisteredDevice>();
    	ResponseWrapper<String> responseWrapper = new ResponseWrapper<>();
    	ResponseWrapper<PageResponseDto<RegisteredDevice>> searchResponseWrapper = new ResponseWrapper<>(); 
    	responseWrapper.setResponse(response);
    	searchResponseWrapper.setResponse(searchResponse);
    	Mockito.when(registeredDeviceService.searchRegisteredDevice(Mockito.any(), Mockito.any())).thenReturn(searchResponse);
        Mockito.when(registeredDeviceService.signedRegisteredDevice(Mockito.any())).thenReturn(response);
        Mockito.when(registeredDeviceService.deRegisterDevice(Mockito.any())).thenReturn(response);
        Mockito.when(regRegisteredDeviceService.searchRegisteredDevice(Mockito.any(), Mockito.any())).thenReturn(searchResponse);
        Mockito.when(regRegisteredDeviceService.signedRegisteredDevice(Mockito.any())).thenReturn(response);
        Mockito.when(regRegisteredDeviceService.deRegisterDevice(Mockito.any())).thenReturn(response);        
        Mockito.doNothing().when(auditUtil).auditRequest(any(), any(), any());
	    Mockito.doNothing().when(auditUtil).auditRequest(any(), any(), any(),any());
    }
    
    @Test
    @WithMockUser(roles = {"DEVICE_PROVIDER"})
    public void signedregisterTest() throws Exception {
    	RequestWrapper<RegisteredDevicePostDto> request=new RequestWrapper<RegisteredDevicePostDto>();
    	RegisteredDevicePostDto registeredDevicePostDto=new RegisteredDevicePostDto();
    	registeredDevicePostDto.setDeviceData("dasfvdfvsf");
    	request.setRequest(registeredDevicePostDto);
        mockMvc.perform(post("/registereddevices").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"DEVICE_PROVIDER"})
    public void derigsterTest() throws Exception {
    	RequestWrapper<DeRegisterDevicePostDto> request=new RequestWrapper<DeRegisterDevicePostDto>();
    	DeRegisterDevicePostDto registeredDevicePostDto=new DeRegisterDevicePostDto();
    	registeredDevicePostDto.setDevice("adcsdcsdcs");
    	registeredDevicePostDto.setIsItForRegistrationDevice(false);
    	request.setRequest(registeredDevicePostDto);
    	mockMvc.perform(post("/registereddevices/deregister").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"DEVICE_PROVIDER"})
    public void searchRegisteredDeviceTest() throws Exception {
    	RequestWrapper<DeviceSearchDto> request=new RequestWrapper<DeviceSearchDto>();
    	DeviceSearchDto registeredDeviceSearchDto=new DeviceSearchDto();
    	Pagination pagination = new Pagination();
    	SearchSort searchSort = new SearchSort();
    	SearchFilter searchFilter = new SearchFilter();
    	pagination.setPageFetch(10);
		pagination.setPageStart(0);
		searchSort.setSortField("model");
		searchSort.setSortType("asc");
		searchFilter.setColumnName("model");
		searchFilter.setFromValue("");
		searchFilter.setToValue("");
		searchFilter.setType("STARTSWITH");
		searchFilter.setValue("b");
		List<SearchFilter> searchfilterDtos = new ArrayList<SearchFilter>();
    	searchfilterDtos.add(searchFilter);
    	List<SearchSort> searchDtos1 = new ArrayList<SearchSort>();
    	searchDtos1.add(searchSort);
    	registeredDeviceSearchDto.setSort(searchDtos1);
    	registeredDeviceSearchDto.setFilters(searchfilterDtos);
		registeredDeviceSearchDto.setPagination(pagination);
    	registeredDeviceSearchDto.setPurpose(Purpose.AUTH.toString());
    	request.setRequest(registeredDeviceSearchDto);
    	mockMvc.perform(post("/registereddevices/search").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"DEVICE_PROVIDER"})
    public void signedregisterTest_regDevice() throws Exception {
    	RequestWrapper<RegisteredDevicePostDto> request=new RequestWrapper<RegisteredDevicePostDto>();
    	RegisteredDevicePostDto registeredDevicePostDto=new RegisteredDevicePostDto();
    	registeredDevicePostDto.setDeviceData("dasfvdfvsf");
    	request.setRequest(registeredDevicePostDto);
        mockMvc.perform(post("/registereddevices").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"DEVICE_PROVIDER"})
    public void derigsterTest_regDevice() throws Exception {
    	RequestWrapper<DeRegisterDevicePostDto> request=new RequestWrapper<DeRegisterDevicePostDto>();
    	DeRegisterDevicePostDto registeredDevicePostDto=new DeRegisterDevicePostDto();
    	registeredDevicePostDto.setDevice("adcsdcsdcs");
    	registeredDevicePostDto.setIsItForRegistrationDevice(true);
    	request.setRequest(registeredDevicePostDto);
    	mockMvc.perform(post("/registereddevices/deregister").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"DEVICE_PROVIDER"})
    public void searchRegRegisteredDeviceTest() throws Exception {
    	RequestWrapper<DeviceSearchDto> request=new RequestWrapper<DeviceSearchDto>();
    	DeviceSearchDto registeredDeviceSearchDto=new DeviceSearchDto();
    	Pagination pagination = new Pagination();
    	SearchSort searchSort = new SearchSort();
    	SearchFilter searchFilter = new SearchFilter();
    	pagination.setPageFetch(10);
		pagination.setPageStart(0);
		searchSort.setSortField("model");
		searchSort.setSortType("asc");
		searchFilter.setColumnName("model");
		searchFilter.setFromValue("");
		searchFilter.setToValue("");
		searchFilter.setType("STARTSWITH");
		searchFilter.setValue("b");
		List<SearchFilter> searchfilterDtos = new ArrayList<SearchFilter>();
    	searchfilterDtos.add(searchFilter);
    	List<SearchSort> searchDtos1 = new ArrayList<SearchSort>();
    	searchDtos1.add(searchSort);
    	registeredDeviceSearchDto.setSort(searchDtos1);
    	registeredDeviceSearchDto.setFilters(searchfilterDtos);
		registeredDeviceSearchDto.setPagination(pagination);
    	registeredDeviceSearchDto.setPurpose(Purpose.REGISTRATION.toString());
    	request.setRequest(registeredDeviceSearchDto);
    	mockMvc.perform(post("/registereddevices/search").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }
}
