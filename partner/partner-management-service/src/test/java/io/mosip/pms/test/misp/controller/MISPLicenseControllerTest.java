package io.mosip.pms.test.misp.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.partner.misp.controller.MISPLicenseController;
import io.mosip.pms.partner.misp.dto.MISPLicenseRequestDto;
import io.mosip.pms.partner.misp.dto.MISPLicenseResponseDto;
import io.mosip.pms.partner.misp.dto.MISPLicenseUpdateRequestDto;
import io.mosip.pms.partner.misp.exception.MISPServiceException;
import io.mosip.pms.partner.misp.service.impl.InfraProviderServiceImpl;
import io.mosip.pms.test.PartnerManagementServiceTest;



@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = PartnerManagementServiceTest.class)
@AutoConfigureMockMvc
@EnableWebMvc
@Import(MISPLicenseController.class)

public class MISPLicenseControllerTest {

	@Autowired
    private MockMvc mockMvc;
	
	@MockBean
	private InfraProviderServiceImpl infraProvidertService;
		
	
	@MockBean
	private AuditUtil auditUtil;
	
	@Autowired
	private ObjectMapper objectMapper;	
	
	@Test
	@WithMockUser(roles = {"PARTNERMANAGER"})
	public void generateLicenseTest () throws MISPServiceException , Exception{
		MISPLicenseResponseDto response = new MISPLicenseResponseDto();
		Mockito.when(infraProvidertService.approveInfraProvider(Mockito.any())).thenReturn(response);
		RequestWrapper<MISPLicenseRequestDto> request = createRequest();
		mockMvc.perform(post("/misp").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());	
	}
	
	@Test
	@WithMockUser(roles = {"PARTNERMANAGER"})
	public void updateLicenseDetailsTest () throws MISPServiceException , Exception{
		MISPLicenseResponseDto response = new MISPLicenseResponseDto();
		Mockito.when(infraProvidertService.updateInfraProvider(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(response);
		RequestWrapper<MISPLicenseUpdateRequestDto> request = createUpdateRequest();
		mockMvc.perform(put("/misp").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(MockMvcResultMatchers.status().isOk());
	}

	
	@Test
	@WithMockUser(roles = {"MISP_PARTNER"})
	public void getLicenseDetailsTest () throws MISPServiceException , Exception{
		mockMvc.perform(MockMvcRequestBuilders.get("/misp")).
		andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	@WithMockUser(roles = {"PARTNER_ADMIN"})
	public void regenarteLicenseKeyTest () throws Exception{
		Mockito.when(infraProvidertService.regenerateKey("12345")).thenReturn(new MISPLicenseResponseDto() );
		mockMvc.perform(MockMvcRequestBuilders.get("/misp/12345/licenseKey")).andExpect(status().isOk());
	}
	
	private RequestWrapper<MISPLicenseRequestDto> createRequest() {
		RequestWrapper<MISPLicenseRequestDto> request = new RequestWrapper<MISPLicenseRequestDto>();
		request.setId("mosip.partnerservice.MispLicense.create");
		request.setMetadata("{}");
		request.setRequest(mispLicenseRequest());
		request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
		request.setVersion("1.0");
		return request;
	}
	
	private MISPLicenseRequestDto mispLicenseRequest() {
		MISPLicenseRequestDto requestDto = new MISPLicenseRequestDto();
		requestDto.setProviderId("12345");
		return requestDto;
	}
	
	private RequestWrapper<MISPLicenseUpdateRequestDto> createUpdateRequest() {
		RequestWrapper<MISPLicenseUpdateRequestDto> request = new RequestWrapper<MISPLicenseUpdateRequestDto>();
		request.setId("mosip.partnerservice.MispLicense.update");
		request.setMetadata("{}");
		request.setRequest(mispLicenseUpdateRequest());
		request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
		request.setVersion("1.0");
		return request;
	}
	
	private MISPLicenseUpdateRequestDto mispLicenseUpdateRequest() {
		MISPLicenseUpdateRequestDto requestDto = new MISPLicenseUpdateRequestDto();
		requestDto.setLicenseKey("12345");
		requestDto.setLicenseKeyStatus("active");
		requestDto.setProviderId("1234");
		return requestDto;
	}

	
}
