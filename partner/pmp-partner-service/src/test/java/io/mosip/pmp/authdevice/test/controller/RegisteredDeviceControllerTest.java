package io.mosip.pmp.authdevice.test.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import io.mosip.pmp.authdevice.controller.RegisteredDeviceController;
import io.mosip.pmp.authdevice.dto.DeRegisterDevicePostDto;
import io.mosip.pmp.authdevice.dto.RegisteredDevicePostDto;
import io.mosip.pmp.authdevice.service.RegisteredDeviceService;
import io.mosip.pmp.partner.core.RequestWrapper;
import io.mosip.pmp.partner.core.ResponseWrapper;
import io.mosip.pmp.partner.test.PartnerserviceApplicationTest;
import io.mosip.pmp.regdevice.service.RegRegisteredDeviceService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PartnerserviceApplicationTest.class)
@AutoConfigureMockMvc
@EnableWebMvc
public class RegisteredDeviceControllerTest {
	@Autowired
    private MockMvc mockMvc;
   
    @Autowired
    private ObjectMapper objectMapper;
    
    @InjectMocks
    RegisteredDeviceController registeredDeviceController;
	
    @MockBean	
    RegisteredDeviceService registeredDeviceService;
	
    @MockBean	
    RegRegisteredDeviceService regRegisteredDeviceService;
    
    @Before
    public void setup() throws Exception {
    	String response="";
    	ResponseWrapper<String> responseWrapper = new ResponseWrapper<>();
    	responseWrapper.setResponse(response);
        Mockito.when(registeredDeviceService.signedRegisteredDevice(Mockito.any())).thenReturn(response);
        Mockito.when(registeredDeviceService.deRegisterDevice(Mockito.any())).thenReturn(response);
        Mockito.when(regRegisteredDeviceService.signedRegisteredDevice(Mockito.any())).thenReturn(response);
        Mockito.when(regRegisteredDeviceService.deRegisterDevice(Mockito.any())).thenReturn(response);
        
    }
    
    @Test
    @WithMockUser(roles = {"ZONAL_ADMIN"})
    public void signedregisterTest() throws Exception {
    	RequestWrapper<RegisteredDevicePostDto> request=new RequestWrapper<RegisteredDevicePostDto>();
    	RegisteredDevicePostDto registeredDevicePostDto=new RegisteredDevicePostDto();
    	registeredDevicePostDto.setDeviceData("dasfvdfvsf");
    	registeredDevicePostDto.setIsItForRegistrationDevice(false);
    	request.setRequest(registeredDevicePostDto);
        mockMvc.perform(post("/registereddevices").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ZONAL_ADMIN"})
    public void derigsterTest() throws Exception {
    	RequestWrapper<DeRegisterDevicePostDto> request=new RequestWrapper<DeRegisterDevicePostDto>();
    	DeRegisterDevicePostDto registeredDevicePostDto=new DeRegisterDevicePostDto();
    	registeredDevicePostDto.setDevice("adcsdcsdcs");
    	registeredDevicePostDto.setIsItForRegistrationDevice(false);
    	request.setRequest(registeredDevicePostDto);
    	mockMvc.perform(post("/registereddevices/deregister").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }
}
