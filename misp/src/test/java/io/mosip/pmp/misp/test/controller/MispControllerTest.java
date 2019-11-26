package io.mosip.pmp.misp.test.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pmp.misp.dto.MISPCreateRequestDto;
import io.mosip.pmp.misp.dto.MISPCreateResponseDto;
import io.mosip.pmp.misp.dto.MISPStatusUpdateRequestDto;
import io.mosip.pmp.misp.dto.MISPStatusUpdateResponse;
import io.mosip.pmp.misp.dto.MISPStatusUpdateResponseDto;
import io.mosip.pmp.misp.dto.MISPUpdateRequestDto;
import io.mosip.pmp.misp.dto.MISPUpdateResponseDto;
import io.mosip.pmp.misp.dto.MISPValidatelKeyRequestDto;
import io.mosip.pmp.misp.dto.MISPlKeyStatusUpdateRequestDto;
import io.mosip.pmp.misp.dto.MISPlKeyStatusUpdateResponseDto;
import io.mosip.pmp.misp.dto.RequestWrapper;
import io.mosip.pmp.misp.dto.ResponseWrapper;
import io.mosip.pmp.misp.entity.MISPEntity;
import io.mosip.pmp.misp.entity.MISPLicenseReadEntity;
import io.mosip.pmp.misp.service.MISPManagementService;
import io.mosip.pmp.misp.test.MispServiceTest;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = MispServiceTest.class)
@AutoConfigureMockMvc
@EnableWebMvc
public class MispControllerTest {

	@Autowired
    private MockMvc mockMvc;
	
	@MockBean
	private MISPManagementService mispManagementService;
	
	@MockBean
	private MISPLicenseReadEntity mispLicenseReadEntity;
	
	@Autowired
	private ObjectMapper objectMapper;	
	
	@Test
	public void mispCreationTest() throws JsonProcessingException, Exception{
		ResponseWrapper<MISPCreateResponseDto> response = new ResponseWrapper<MISPCreateResponseDto>();
		Mockito.when(mispManagementService.createMISP(Mockito.any())).thenReturn(response);
		RequestWrapper<MISPCreateRequestDto> request = formCreateRequest();
		
		mockMvc.perform(post("/pmp/misps").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	public void mispRequestStatusTest() throws JsonProcessingException, Exception{
		ResponseWrapper<MISPStatusUpdateResponse> response = new ResponseWrapper<MISPStatusUpdateResponse>();
		Mockito.when(mispManagementService.updateMispRequestStatus(Mockito.any())).thenReturn(response);
		RequestWrapper<MISPStatusUpdateRequestDto> request = formRequestStatusRequest();
		
		mockMvc.perform(post("/pmp/misps/12345/status").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	public void updateMispTest() throws JsonProcessingException, Exception{
		ResponseWrapper<MISPUpdateResponseDto> response = new ResponseWrapper<MISPUpdateResponseDto>();
		Mockito.when(mispManagementService.update(Mockito.any())).thenReturn(response);
		RequestWrapper<MISPUpdateRequestDto> request = formUpdateMispRequest();

		mockMvc.perform(post("/pmp/misps/12345").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());

	}
	
//	@Test
//	public void validateLkeyTest() throws JsonProcessingException, Exception{	
//		ResponseWrapper<MISPValidatelKeyResponseDto> response = new ResponseWrapper<MISPValidatelKeyResponseDto>();
//		MISPLicenseReadEntity serviceResponse = new MISPLicenseReadEntity();
//		Mockito.when(mispManagementService.getMispLicense(Mockito.any())).thenReturn(serviceResponse);
//		
//		RequestWrapper<MISPValidatelKeyRequestDto> request = formValidateLkeyRequest();
//		mockMvc.perform(post("/pmp/misps/12345/licenseKey").contentType(MediaType.APPLICATION_JSON_VALUE)
//                .content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());
//
//	}
	
	@Test
	public void updateMispStatusTest() throws JsonProcessingException, Exception{
		ResponseWrapper<MISPStatusUpdateResponseDto> response = new ResponseWrapper<MISPStatusUpdateResponseDto>();
		Mockito.when(mispManagementService.updateMISPStatus(Mockito.any())).thenReturn(response);
		RequestWrapper<MISPStatusUpdateRequestDto> request = formUpdateMispStatusRequest();
		
    	mockMvc.perform(put("/pmp/misps/12345").contentType(MediaType.APPLICATION_JSON_VALUE)
    			.content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());
    }
	
	@Test
	public void updateMispLkeyStatusTest() throws JsonProcessingException, Exception{
		ResponseWrapper<MISPlKeyStatusUpdateResponseDto> response = new ResponseWrapper<MISPlKeyStatusUpdateResponseDto>();
		Mockito.when(mispManagementService.updateMisplkeyStatus(Mockito.any())).thenReturn(response);
		RequestWrapper<MISPlKeyStatusUpdateRequestDto> request = formLicenseKeyUpdateRequest();

		mockMvc.perform(put("/pmp/misps/12345/licenseKey").contentType(MediaType.APPLICATION_JSON_VALUE)
    			.content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());

	}
	
	@Test
	public void getMispsTest() throws Exception{		
		List<MISPEntity> response = new ArrayList<MISPEntity>();
		Mockito.when(mispManagementService.getMisps()).thenReturn(response);
		
		mockMvc.perform(MockMvcRequestBuilders.get("/pmp/misps")).
		andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	public void getMispTest() throws Exception{
		MISPEntity response =  new MISPEntity();
		Mockito.when(mispManagementService.getMisp(Mockito.any())).thenReturn(response);
		
		mockMvc.perform(MockMvcRequestBuilders.get("/pmp/misps/12345")).
		andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	public void getMispByOrgTest() throws Exception{
		List<MISPEntity> response = new ArrayList<>();
		Mockito.when(mispManagementService.getMispsByOrg(Mockito.any())).thenReturn(response);
		
		mockMvc.perform(MockMvcRequestBuilders.get("/pmp/misps/misp/airtel")).
		andExpect(MockMvcResultMatchers.status().isOk());
		
	}
	
//	@Test
//	public void downloadLkeyTest() throws Exception{
//		ResponseWrapper<MISPLiceneseDto> response = new ResponseWrapper<MISPLiceneseDto>();
//		Mockito.when(mispManagementService.getLicense(Mockito.any())).thenReturn(response);
//		
//		mockMvc.perform(MockMvcRequestBuilders.get("/pmp/misps/12345/licenseKey")).
//		andExpect(MockMvcResultMatchers.status().isOk());
//	}
	
	private RequestWrapper<MISPlKeyStatusUpdateRequestDto> formLicenseKeyUpdateRequest() {
		RequestWrapper<MISPlKeyStatusUpdateRequestDto> request = new RequestWrapper<MISPlKeyStatusUpdateRequestDto>();
		request.setId("mosip.partnermanagement.misp.license.status.update");
		request.setMetadata("");
		request.setRequesttime(LocalDateTime.now());
		request.setVersion("0.9.1");
		MISPlKeyStatusUpdateRequestDto dto = new MISPlKeyStatusUpdateRequestDto();
		dto.setMispId("12345");
		dto.setMispLicenseKey("121hjgdwgdhjgdyhdwgdwhghdghd");
		dto.setMispLicenseKeyStatus("Active");
		dto.setMispStatus("Active");
		request.setRequest(dto);
		return request;
	}

	private RequestWrapper<MISPStatusUpdateRequestDto> formUpdateMispStatusRequest() {
		RequestWrapper<MISPStatusUpdateRequestDto> request = new RequestWrapper<MISPStatusUpdateRequestDto>();
		request.setId("mosip.partnermanagement.misp.status.update");
		request.setMetadata("");
		request.setRequesttime(LocalDateTime.now());
		request.setVersion("0.9.1");
		MISPStatusUpdateRequestDto dto = new MISPStatusUpdateRequestDto();
		dto.setMispId("12345");
		dto.setMispStatus("De-Active");
		request.setRequest(dto);
		return request;
	}

	@SuppressWarnings("unused")
	private RequestWrapper<MISPValidatelKeyRequestDto> formValidateLkeyRequest() {
		RequestWrapper<MISPValidatelKeyRequestDto> request = new RequestWrapper<MISPValidatelKeyRequestDto>();
		request.setId("mosip.partnermanagement.misp.license.validate");
		request.setMetadata("");
		request.setRequesttime(LocalDateTime.now());
		request.setVersion("0.9.1");
		
		MISPValidatelKeyRequestDto dto = new MISPValidatelKeyRequestDto();
		dto.setMispLicenseKey("zzsfgygghgghbbjhjnbnjkjnjgghvghqcscqcghdvdqghvd");
		request.setRequest(dto);
		return request;
	}

	private RequestWrapper<MISPUpdateRequestDto> formUpdateMispRequest() {
		RequestWrapper<MISPUpdateRequestDto> request = new RequestWrapper<MISPUpdateRequestDto>();
		request.setId("mosip.partnermanagement.misp.update");
		request.setMetadata("");
		request.setRequesttime(LocalDateTime.now());
		request.setVersion("0.9.1");
		MISPUpdateRequestDto dto = new MISPUpdateRequestDto();
		dto.setAddress("Bangalore");
		dto.setContactNumber("1234567890");
		dto.setEmailID("vodafone@gmail.com");
		dto.setName("Vodafone");		
		request.setRequest(dto);

		return request;
	}

	private RequestWrapper<MISPStatusUpdateRequestDto> formRequestStatusRequest() {
		RequestWrapper<MISPStatusUpdateRequestDto> request = new RequestWrapper<MISPStatusUpdateRequestDto>();
		request.setId("mosip.partnermanagement.misp.status.update");
		request.setMetadata("");
		request.setRequesttime(LocalDateTime.now());
		request.setVersion("0.9.1");
		MISPStatusUpdateRequestDto dto = new MISPStatusUpdateRequestDto();
		dto.setMispId("12345");
		dto.setMispStatus("approve");
		request.setRequest(dto);
		
		return request;
	}

	private RequestWrapper<MISPCreateRequestDto> formCreateRequest() {
		RequestWrapper<MISPCreateRequestDto> request = new RequestWrapper<MISPCreateRequestDto>();
		request.setId("mosip.partnermanagement.misp.create");
		request.setMetadata("");
		request.setRequesttime(LocalDateTime.now());
		request.setVersion("0.9.1");
		MISPCreateRequestDto dto = new MISPCreateRequestDto();
		dto.setAddress("Bangalore");
		dto.setContactNumber("1234567890");
		dto.setEmailId("airtel@gmail.com");
		dto.setName("Airtel");
		dto.setOrganizationName("Airtel");
		request.setRequest(dto);
		
		return request;
	}
}
