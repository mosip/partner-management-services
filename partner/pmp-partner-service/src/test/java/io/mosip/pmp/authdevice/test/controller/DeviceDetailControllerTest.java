package io.mosip.pmp.authdevice.test.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
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
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pmp.authdevice.controller.DeviceDetailController;
import io.mosip.pmp.authdevice.dto.DeviceDetailDto;
import io.mosip.pmp.authdevice.dto.DeviceDetailUpdateDto;
import io.mosip.pmp.authdevice.dto.IdDto;
import io.mosip.pmp.authdevice.dto.UpdateDeviceDetailStatusDto;
import io.mosip.pmp.authdevice.service.DeviceDetailService;
import io.mosip.pmp.authdevice.util.AuditUtil;
import io.mosip.pmp.partner.core.RequestWrapper;
import io.mosip.pmp.partner.core.ResponseWrapper;
import io.mosip.pmp.partner.test.PartnerserviceApplicationTest;
import io.mosip.pmp.regdevice.service.RegDeviceDetailService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PartnerserviceApplicationTest.class)
@AutoConfigureMockMvc
@EnableWebMvc
//@WithMockUser(roles = {"ZONAL_ADMIN"},username="zonal-admin",password="admin")
@Ignore
public class DeviceDetailControllerTest {
	
	@Autowired
    private MockMvc mockMvc;
   
    @Autowired
    private ObjectMapper objectMapper;
    
    @InjectMocks
    DeviceDetailController deviceDetailController;
    
    @MockBean
	AuditUtil auditUtil;
	
    @MockBean	
	DeviceDetailService deviceDetaillService;
	
    @MockBean	
	RegDeviceDetailService regDeviceDetaillService;
    
    RequestWrapper<DeviceDetailDto> createrequest=null;
    RequestWrapper<DeviceDetailUpdateDto> updaterequest=null;
    
    @Before
    public void setup() {
    	Mockito.doNothing().when(auditUtil).auditRequest(any(), any(), any());
    	Mockito.doNothing().when(auditUtil).auditRequest(any(), any(), any(),any());
    	
    	IdDto response = new IdDto();
    	ResponseWrapper<IdDto> responseWrapper = new ResponseWrapper<>();
    	responseWrapper.setResponse(response);
        Mockito.when(regDeviceDetaillService.updateDeviceDetails(Mockito.any())).thenReturn(response);
        Mockito.when(regDeviceDetaillService.createDeviceDetails(Mockito.any())).thenReturn(response);
        Mockito.when(deviceDetaillService.updateDeviceDetails(Mockito.any())).thenReturn(response);
        Mockito.when(deviceDetaillService.createDeviceDetails(Mockito.any())).thenReturn(response);
         createrequest = createRequest(false);
         updaterequest =updateRequest(false);
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
    	deviceDetailDto.setIsActive(true);
    	deviceDetailDto.setIsItForRegistrationDevice(isItForRegistrationDevice);
    	deviceDetailDto.setMake("make");
    	deviceDetailDto.setModel("model");
    	deviceDetailDto.setPartnerOrganizationName("pog");
        
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
    	dto.setIsItForRegistrationDevice(isItForRegistrationDevice);
    	return dto;
    }
    
    @Test    
    @WithMockUser(roles = {"PARTNER"})
    public void createDeviceDetailTest() throws Exception {
        mockMvc.perform(post("/devicedetail").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(createrequest))).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void updateDeviceDetailsTest() throws Exception {
    	mockMvc.perform(put("/devicedetail").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(updaterequest))).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNERMANAGER"})
    public void approveDeviceDetailsTest() throws JsonProcessingException, Exception {
    	RequestWrapper<UpdateDeviceDetailStatusDto> createrequest=approvalRequest(false);
    	mockMvc.perform(MockMvcRequestBuilders.patch("/devicedetail").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(createrequest))).andExpect(status().isOk());    	
    }
    
    @Test    
    @WithMockUser(roles = {"PARTNER"})
    public void createDeviceDetailTest_regdevice() throws Exception {
    	RequestWrapper<DeviceDetailDto> createrequest=createRequest(true);
        mockMvc.perform(post("/devicedetail").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(createrequest))).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void updateDeviceDetailsTest_regdevice() throws Exception {
    	RequestWrapper<DeviceDetailUpdateDto> updaterequest=updateRequest(true);
    	mockMvc.perform(put("/devicedetail").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(updaterequest))).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNERMANAGER"})
    public void approveDeviceDetailsTest_regDevice() throws JsonProcessingException, Exception {
    	RequestWrapper<UpdateDeviceDetailStatusDto> createrequest=approvalRequest(true);
    	mockMvc.perform(MockMvcRequestBuilders.patch("/devicedetail").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(createrequest))).andExpect(status().isOk());    	
    }
    
}
