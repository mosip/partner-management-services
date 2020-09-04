package io.mosip.pmp.authdevice.test.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.Before;
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
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pmp.authdevice.controller.DeviceDetailController;
import io.mosip.pmp.authdevice.dto.DeviceDetailDto;
import io.mosip.pmp.authdevice.dto.DeviceDetailUpdateDto;
import io.mosip.pmp.authdevice.dto.IdDto;
import io.mosip.pmp.authdevice.service.DeviceDetailService;
import io.mosip.pmp.authdevice.util.AuditUtil;
import io.mosip.pmp.partner.core.RequestWrapper;
import io.mosip.pmp.partner.core.ResponseWrapper;
import io.mosip.pmp.partner.test.PartnerserviceApplicationTest;
import io.mosip.pmp.regdevice.service.RegDeviceDetaillService;
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PartnerserviceApplicationTest.class)
@AutoConfigureMockMvc
@EnableWebMvc
@WithMockUser(roles = {"ZONAL_ADMIN"},username="zonal-admin",password="admin")
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
	RegDeviceDetaillService regDeviceDetaillService;
    
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
         createrequest = createRequest();
         updaterequest =updateRequest();
    }
    
    private RequestWrapper<DeviceDetailDto> createRequest() {
        RequestWrapper<DeviceDetailDto> request = new RequestWrapper<DeviceDetailDto>();
        request.setRequest(createdetailRequest());
        request.setId("mosip.partnermanagement.devicedetail.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
    }
    
    private DeviceDetailDto createdetailRequest() {
    	DeviceDetailDto deviceDetailDto = new DeviceDetailDto();
    	
    	deviceDetailDto.setDeviceProviderId("1234");
    	deviceDetailDto.setDeviceSubTypeCode("123");
    	deviceDetailDto.setDeviceTypeCode("123");
    	deviceDetailDto.setId("121");
    	
    	deviceDetailDto.setIsItForRegistrationDevice(false);
    	deviceDetailDto.setMake("make");
    	deviceDetailDto.setModel("model");
    	deviceDetailDto.setPartnerOrganizationName("pog");
        
        return deviceDetailDto;
    }
    
    private RequestWrapper<DeviceDetailUpdateDto>updateRequest() {
        RequestWrapper<DeviceDetailUpdateDto> request = new RequestWrapper<DeviceDetailUpdateDto>();
        request.setRequest(updatedetailRequest());
        request.setId("mosip.partnermanagement.devicedetail.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
    }
    
    private DeviceDetailUpdateDto updatedetailRequest() {
    	DeviceDetailUpdateDto deviceDetailDto = new DeviceDetailUpdateDto();
    	
    	deviceDetailDto.setDeviceProviderId("1234");
    	deviceDetailDto.setDeviceSubTypeCode("123");
    	deviceDetailDto.setDeviceTypeCode("123");
    	deviceDetailDto.setId("121");
    	deviceDetailDto.setIsActive(true);
    	deviceDetailDto.setIsItForRegistrationDevice(false);
    	deviceDetailDto.setMake("make");
    	deviceDetailDto.setModel("model");
    	deviceDetailDto.setPartnerOrganizationName("pog");
        
        return deviceDetailDto;
    }
     
    @Test
    
    public void createDeviceDetailTest() throws Exception {
       

        mockMvc.perform(post("/devicedetail").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(createrequest))).andExpect(status().isOk());
    }

    @Test

    public void updateDeviceDetailsTest() throws Exception {
    	mockMvc.perform(put("/devicedetail").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(updaterequest))).andExpect(status().isOk());
    }
    
}
