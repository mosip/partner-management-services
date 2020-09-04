package io.mosip.pmp.authdevice.test.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
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

import io.mosip.pmp.authdevice.controller.SecureBiometricInterfaceController;
import io.mosip.pmp.authdevice.dto.IdDto;
import io.mosip.pmp.authdevice.dto.SecureBiometricInterfaceCreateDto;
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
        createRequest = createRequest();
        updateRequest=updateRequest();
    }
    
    private RequestWrapper<SecureBiometricInterfaceUpdateDto> updateRequest() {
    	RequestWrapper<SecureBiometricInterfaceUpdateDto> request = new RequestWrapper<SecureBiometricInterfaceUpdateDto>();
        request.setRequest(createSBIUpdate());
        request.setId("mosip.partnermanagement.sbi.update");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
	}

	private SecureBiometricInterfaceUpdateDto createSBIUpdate() {
		SecureBiometricInterfaceUpdateDto sbidto = new SecureBiometricInterfaceUpdateDto();
    	
    	sbidto.setDeviceDetailId("1234");
    	sbidto.setSwBinaryHash("swb");
    	sbidto.setSwCreateDateTime(LocalDateTime.now());
    	sbidto.setSwExpiryDateTime(LocalDateTime.now());
    	sbidto.setIsActive(true);
    	sbidto.setIsItForRegistrationDevice(false);
    	sbidto.setSwVersion("v1");
    	sbidto.setId("1234");
        return sbidto;
	}

	private RequestWrapper<SecureBiometricInterfaceCreateDto> createRequest() {
        RequestWrapper<SecureBiometricInterfaceCreateDto> request = new RequestWrapper<SecureBiometricInterfaceCreateDto>();
        request.setRequest(createSBIRequest());
        request.setId("mosip.partnermanagement.sbi.create");
        request.setVersion("1.0");
        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        request.setMetadata("{}");
        return request;
    }
    
    private SecureBiometricInterfaceCreateDto createSBIRequest() {
    	SecureBiometricInterfaceCreateDto sbidto = new SecureBiometricInterfaceCreateDto();
    	
    	sbidto.setDeviceDetailId("1234");
    	sbidto.setSwBinaryHash("swb");
    	sbidto.setSwCreateDateTime(LocalDateTime.now());
    	sbidto.setSwExpiryDateTime(LocalDateTime.now());
    	
    	sbidto.setIsItForRegistrationDevice(false);
    	sbidto.setSwVersion("v1");
        
        return sbidto;
    }
     
    @Test
    @WithMockUser(roles = {"ZONAL_ADMIN"})
    public void createsbiTest() throws Exception {
       

        mockMvc.perform(post("/securebiometricinterface").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(createRequest))).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ZONAL_ADMIN"})
    public void updatesbiTest() throws Exception {
    	mockMvc.perform(put("/securebiometricinterface").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(updateRequest))).andExpect(status().isOk());
    }
}
