package io.mosip.pms.test.device.controller;

import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.device.regdevice.service.impl.DeviceValidationServiceImpl;
import io.mosip.pms.device.request.dto.DigitalIdDto;
import io.mosip.pms.device.request.dto.ValidateDeviceDto;
import io.mosip.pms.device.response.dto.ResponseDto;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.test.PartnerManagementServiceTest;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = PartnerManagementServiceTest.class)
@AutoConfigureMockMvc
@EnableWebMvc
public class DeviceValidationControllerTest {

	@Autowired
    private MockMvc mockMvc;
   
    @Autowired
    private ObjectMapper objectMapper;
	
	@MockBean
	DeviceValidationServiceImpl deviceValidationServiceImpl;
	
	 @MockBean
		AuditUtil auditUtil;
	
	 @Before
	    public void setup() {
		 Mockito.doNothing().when(auditUtil).auditRequest(any(), any(), any());
	    	Mockito.doNothing().when(auditUtil).auditRequest(any(), any(), any(),any());
		 ResponseDto response = new ResponseDto();
		 ResponseWrapper<ResponseDto> validateResponse = new ResponseWrapper<ResponseDto>();
		 validateResponse.setResponse(response);
		 Mockito.when(deviceValidationServiceImpl.validateDeviceProviders(Mockito.any())).thenReturn(response);
	 }
	 
	 private RequestWrapper<ValidateDeviceDto> createRequest() {
	        RequestWrapper<ValidateDeviceDto> request = new RequestWrapper<ValidateDeviceDto>();
	        request.setRequest(validateRequest());
	        request.setId("mosip.partnermanagement.devicedetail.create");
	        request.setVersion("1.0");
	        request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
	        request.setMetadata("{}");
	        return request;
	    }
	 
	 private ValidateDeviceDto validateRequest() {
		 ValidateDeviceDto validateDeviceDto = new ValidateDeviceDto();
		 DigitalIdDto DigitalId = new DigitalIdDto();
		 DigitalId.setDateTime("28-01-2021, 18:23:00");
		 DigitalId.setDeviceSubType("subType");
		 DigitalId.setDp("abcd");
		 DigitalId.setDpId("1234");
		 DigitalId.setMake("make");
		 DigitalId.setModel("model");
		 DigitalId.setSerialNo("123456");
		 DigitalId.setType("type");
		 validateDeviceDto.setDeviceCode("1234");
		 validateDeviceDto.setDeviceServiceVersion("1.0.0");
		 validateDeviceDto.setDigitalId(DigitalId);
		 validateDeviceDto.setPurpose("REGISTRATION");
		 validateDeviceDto.setTimeStamp("28-01-2021, 18:23:00");
		 return validateDeviceDto;
	 }
	 
	 @Test    
	    @WithMockUser(roles = {"ZONAL_ADMIN"})
	    public void validateTest() throws Exception {
		   RequestWrapper<ValidateDeviceDto> request = createRequest();
		   mockMvc.perform(post("/deviceprovidermanagement/validate").contentType(MediaType.APPLICATION_JSON_VALUE)
	                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
	   }
	   
	 
	
}
