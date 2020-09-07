package io.mosip.pmp.authdevice.test.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
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

import io.mosip.pmp.authdevice.controller.SecureBiometricInterfaceController;
import io.mosip.pmp.authdevice.dto.IdDto;
import io.mosip.pmp.authdevice.dto.SecureBiometricInterfaceCreateDto;
import io.mosip.pmp.authdevice.dto.SecureBiometricInterfaceStatusUpdateDto;
import io.mosip.pmp.authdevice.dto.SecureBiometricInterfaceUpdateDto;
import io.mosip.pmp.authdevice.service.SecureBiometricInterfaceService;
import io.mosip.pmp.authdevice.util.AuditUtil;
import io.mosip.pmp.partner.core.RequestWrapper;
import io.mosip.pmp.partner.core.ResponseWrapper;
import io.mosip.pmp.partner.test.PartnerserviceApplicationTest;
import io.mosip.pmp.regdevice.service.RegSecureBiometricInterfaceService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PartnerserviceApplicationTest.class)
@AutoConfigureMockMvc
@EnableWebMvc
//@WithMockUser(roles = {"ZONAL_ADMIN"},username="zonal-admin",password="admin")
@Ignore
public class SecureBiometricInterfaceControllerTest {
	
	@Autowired
    private MockMvc mockMvc;
   
    @Autowired
    private ObjectMapper objectMapper;
    
    @InjectMocks
    SecureBiometricInterfaceController secureBiometricInterfaceController;
    
    @MockBean
	AuditUtil auditUtil;
	
    @MockBean	
    SecureBiometricInterfaceService secureBiometricInterfaceService;
	
    @MockBean	
    RegSecureBiometricInterfaceService regSecureBiometricInterface;
    
    RequestWrapper<SecureBiometricInterfaceCreateDto> createRequest=null;
    RequestWrapper<SecureBiometricInterfaceUpdateDto> updateRequest=null;
    
    @Before
    public void setup() {
    	Mockito.doNothing().when(auditUtil).auditRequest(any(), any(), any());
    	Mockito.doNothing().when(auditUtil).auditRequest(any(), any(), any(),any());
    	
    	IdDto response = new IdDto();
    	ResponseWrapper<IdDto> responseWrapper = new ResponseWrapper<>();
    	responseWrapper.setResponse(response);
        Mockito.when(regSecureBiometricInterface.updateSecureBiometricInterface(Mockito.any())).thenReturn(response);
        Mockito.when(regSecureBiometricInterface.createSecureBiometricInterface(Mockito.any())).thenReturn(response);
        Mockito.when(secureBiometricInterfaceService.updateSecureBiometricInterface(Mockito.any())).thenReturn(response);
        Mockito.when(secureBiometricInterfaceService.createSecureBiometricInterface(Mockito.any())).thenReturn(response);
        createRequest = createRequest(false);
        updateRequest=updateRequest(false);
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
    	
    	sbidto.setDeviceDetailId("1234");
    	sbidto.setSwBinaryHash("swb");
    	sbidto.setSwCreateDateTime(LocalDateTime.now());
    	sbidto.setSwExpiryDateTime(LocalDateTime.now());
    	sbidto.setIsActive(true);
    	sbidto.setIsItForRegistrationDevice(isItForRegistrationDevice);
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
    	
    	sbidto.setDeviceDetailId("1234");
    	sbidto.setSwBinaryHash("swb");
    	sbidto.setSwCreateDateTime(LocalDateTime.now());
    	sbidto.setSwExpiryDateTime(LocalDateTime.now());
    	
    	sbidto.setIsItForRegistrationDevice(isItForRegistrationDevice);
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
    	dto.setIsItForRegistrationDevice(isItForRegistrationDevice);
    	return dto;
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void createsbiTest() throws Exception {      

        mockMvc.perform(post("/securebiometricinterface").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(createRequest))).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void updatesbiTest() throws Exception {
    	mockMvc.perform(put("/securebiometricinterface").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(updateRequest))).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = {"PARTNERMANAGER"})
    public void approveDeviceDetailsTest() throws JsonProcessingException, Exception {
    	RequestWrapper<SecureBiometricInterfaceStatusUpdateDto> createrequest=approvalRequest(false);
    	mockMvc.perform(MockMvcRequestBuilders.patch("/securebiometricinterface").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(createrequest))).andExpect(status().isOk());    	
    }
    
    @Test
    @WithMockUser(roles = {"PARTNERMANAGER"})
    public void approveDeviceDetailsTest_regDevice() throws JsonProcessingException, Exception {
    	RequestWrapper<SecureBiometricInterfaceStatusUpdateDto> createrequest=approvalRequest(true);
    	mockMvc.perform(MockMvcRequestBuilders.patch("/securebiometricinterface").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(createrequest))).andExpect(status().isOk());    	
    }
    
    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void createsbiTest_regDevice() throws Exception {
    	RequestWrapper<SecureBiometricInterfaceCreateDto> createRequest=createRequest(true);
        mockMvc.perform(post("/securebiometricinterface").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(createRequest))).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"PARTNER"})
    public void updatesbiTest_regDevice() throws Exception {
    	RequestWrapper<SecureBiometricInterfaceUpdateDto> updateRequest=updateRequest(true);
    	mockMvc.perform(put("/securebiometricinterface").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(updateRequest))).andExpect(status().isOk());
    }
}
