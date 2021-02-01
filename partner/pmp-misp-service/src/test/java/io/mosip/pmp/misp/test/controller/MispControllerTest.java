package io.mosip.pmp.misp.test.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pmp.misp.dto.MISPCreateRequestDto;
import io.mosip.pmp.misp.dto.MISPCreateResponseDto;
import io.mosip.pmp.misp.dto.MISPDetailsDto;
import io.mosip.pmp.misp.dto.MISPLiceneseDto;
import io.mosip.pmp.misp.dto.MISPStatusUpdateRequestDto;
import io.mosip.pmp.misp.dto.MISPStatusUpdateResponse;
import io.mosip.pmp.misp.dto.MISPStatusUpdateResponseDto;
import io.mosip.pmp.misp.dto.MISPUpdateRequestDto;
import io.mosip.pmp.misp.dto.MISPUpdateResponseDto;
import io.mosip.pmp.misp.dto.MISPValidatelKeyRequestDto;
import io.mosip.pmp.misp.dto.MISPValidatelKeyResponseDto;
import io.mosip.pmp.misp.dto.MISPlKeyStatusUpdateRequestDto;
import io.mosip.pmp.misp.dto.MISPlKeyStatusUpdateResponseDto;
import io.mosip.pmp.misp.dto.RequestWrapper;
import io.mosip.pmp.misp.dto.ResponseWrapper;
import io.mosip.pmp.misp.service.MISPManagementService;
import io.mosip.pmp.misp.test.MispServiceTest;
import io.mosip.pmp.misp.utils.AuditUtil;
import io.mosip.pms.common.entity.MISPlKeyUniqueKeyEntity;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = MispServiceTest.class)
@AutoConfigureMockMvc
public class MispControllerTest {
	
	@Autowired
    private MockMvc mockMvc;
	
	@MockBean
	private MISPManagementService mispManagementService;	
	
	@Autowired
	private ObjectMapper objectMapper;	
	
	@MockBean
	private AuditUtil audit;
	
	@Before
	public void setUp() {
		Mockito.doNothing().when(audit).setAuditRequestDto(Mockito.any());
	}
	
	@Test
	@WithMockUser(roles = {"MISP"})
	public void mispCreationTest() throws JsonProcessingException, Exception{
		ResponseWrapper<MISPCreateResponseDto> response = new ResponseWrapper<MISPCreateResponseDto>();
		Mockito.when(mispManagementService.createMISP(ArgumentMatchers.any())).thenReturn(response);
		RequestWrapper<MISPCreateRequestDto> request = formCreateRequest();
		
		mockMvc.perform(post("/misps").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());	
		
	}
	
	
	@Test
	@WithMockUser(roles = {"MISP"})
	public void mispRequestStatusTest() throws JsonProcessingException, Exception{
		ResponseWrapper<MISPStatusUpdateResponse> response = new ResponseWrapper<MISPStatusUpdateResponse>();
		Mockito.when(mispManagementService.processRequest(ArgumentMatchers.any())).thenReturn(response);
		RequestWrapper<MISPStatusUpdateRequestDto> request = formRequestStatusRequest();
		
		mockMvc.perform(MockMvcRequestBuilders.patch("/misps/12345/status").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());		
	}
	
	@Test
	@WithMockUser(roles = {"MISP"})
	public void updateMispTest() throws JsonProcessingException, Exception{
		ResponseWrapper<MISPUpdateResponseDto> response = new ResponseWrapper<MISPUpdateResponseDto>();
		Mockito.when(mispManagementService.update(ArgumentMatchers.any())).thenReturn(response);
		RequestWrapper<MISPUpdateRequestDto> request = formUpdateMispRequest();

		mockMvc.perform(put("/misps/12345").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());

	}
	
	@Test
	@WithMockUser(roles = {"MISP"})
	public void validateLkeyTest() throws JsonProcessingException, Exception{
		MISPValidatelKeyResponseDto serviceResponse = new MISPValidatelKeyResponseDto();
		Mockito.when(mispManagementService.validateLicenseKey(new MISPlKeyUniqueKeyEntity())).thenReturn(serviceResponse);
		
		RequestWrapper<MISPValidatelKeyRequestDto> request = formValidateLkeyRequest();
		mockMvc.perform(MockMvcRequestBuilders.patch("/misps/12345/licenseKey").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());

	}
	
	@Test
	@WithMockUser(roles = {"MISP"})
	public void updateMispStatusTest() throws JsonProcessingException, Exception{
		ResponseWrapper<MISPStatusUpdateResponseDto> response = new ResponseWrapper<MISPStatusUpdateResponseDto>();
		Mockito.when(mispManagementService.updateMISPStatus(ArgumentMatchers.any())).thenReturn(response);
		RequestWrapper<MISPStatusUpdateRequestDto> request = formUpdateMispStatusRequest();
		
    	mockMvc.perform(MockMvcRequestBuilders.patch("/misps/12345").contentType(MediaType.APPLICATION_JSON_VALUE)
    			.content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());
    }
	
	@Test
	@WithMockUser(roles = {"MISP"})
	public void updateMispLkeyStatusTest() throws JsonProcessingException, Exception{
		ResponseWrapper<MISPlKeyStatusUpdateResponseDto> response = new ResponseWrapper<MISPlKeyStatusUpdateResponseDto>();
		Mockito.when(mispManagementService.updateMisplkeyStatus(ArgumentMatchers.any(),ArgumentMatchers.any())).thenReturn(response);
		RequestWrapper<MISPlKeyStatusUpdateRequestDto> request = formLicenseKeyUpdateRequest();

		mockMvc.perform(put("/misps/12345/licenseKey").contentType(MediaType.APPLICATION_JSON)
    			.content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());

	}
	
	@Test
	@WithMockUser(roles = {"MISP"})
	public void getMispsTest() throws Exception{		
		List<MISPDetailsDto> response = new ArrayList<MISPDetailsDto>();
		Mockito.when(mispManagementService.getMisps()).thenReturn(response);
		
		mockMvc.perform(MockMvcRequestBuilders.get("/misps")).
		andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	@WithMockUser(roles = {"MISP"})
	public void getMispTest() throws Exception{
		MISPDetailsDto response =  new MISPDetailsDto();
		Mockito.when(mispManagementService.getMisp(ArgumentMatchers.any())).thenReturn(response);
		
		mockMvc.perform(MockMvcRequestBuilders.get("/misps/mispId/12345")).
		andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	@WithMockUser(roles = {"MISP"})
	public void getMispByOrgTest() throws Exception{
		List<MISPDetailsDto> response = new ArrayList<>();
		Mockito.when(mispManagementService.getMispsByOrg(ArgumentMatchers.any())).thenReturn(response);
		
		mockMvc.perform(MockMvcRequestBuilders.get("/misps/name/airtel")).
		andExpect(MockMvcResultMatchers.status().isOk());
		
	}
	
	@Test
	@WithMockUser(roles = {"MISP"})
	public void downloadLkeyTest() throws Exception{
		ResponseWrapper<MISPLiceneseDto> response = new ResponseWrapper<MISPLiceneseDto>();
		Mockito.when(mispManagementService.retriveLicense(ArgumentMatchers.any())).thenReturn(response);
		
		mockMvc.perform(MockMvcRequestBuilders.get("/misps/12345/licenseKey")).
		andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	private RequestWrapper<MISPlKeyStatusUpdateRequestDto> formLicenseKeyUpdateRequest() {
		RequestWrapper<MISPlKeyStatusUpdateRequestDto> request = new RequestWrapper<MISPlKeyStatusUpdateRequestDto>();
		request.setId("mosip.partnermanagement.misp.license.status.update");
		request.setMetadata("");
		request.setRequesttime(LocalDateTime.now());
		request.setVersion("0.9.1");
		MISPlKeyStatusUpdateRequestDto dto = new MISPlKeyStatusUpdateRequestDto();
		dto.setMispLicenseKey("121hjgdwgdhjgdyhdwgdwhghdghd");
		dto.setMispLicenseKeyStatus("Active");
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
		dto.setEmailId("vodafone@gmail.com");
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
		dto.setMispStatus("approved");
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
		dto.setOrganizationName("Airtel");
		request.setRequest(dto);
		
		return request;
	}
//	
	
}
