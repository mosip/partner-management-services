package io.mosip.pms.test.partner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.pms.common.request.dto.RequestWrapperV2;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.partner.controller.MultiPartnerAdminServiceController;
import io.mosip.pms.partner.request.dto.SbiAndDeviceMappingRequestDto;
import io.mosip.pms.partner.service.MultiPartnerAdminService;
import io.mosip.pms.partner.util.RequestValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@EnableWebMvc
public class MultiPartnerAdminServiceContollerTest {

    public static final String VERSION = "1.0";

    @InjectMocks
    MultiPartnerAdminServiceController multiPartnerAdminServiceController;

    @Mock
    MultiPartnerAdminService multiPartnerAdminService;

    @Mock
    RequestValidator requestValidator;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void approveMappingDeviceToSbiTest() throws Exception {
        RequestWrapperV2<SbiAndDeviceMappingRequestDto> requestWrapper = new RequestWrapperV2<>();
        requestWrapper.setId("mosip.pms.approve.mapping.device.to.sbi.post");
        requestWrapper.setVersion(VERSION);
        requestWrapper.setRequestTime(LocalDateTime.now());
        SbiAndDeviceMappingRequestDto sbiAndDeviceMappingRequestDto = new SbiAndDeviceMappingRequestDto();
        requestWrapper.setRequest(sbiAndDeviceMappingRequestDto);
        ResponseWrapperV2<Boolean> responseWrapper = new ResponseWrapperV2<>();
        responseWrapper.setResponse(true);
        Mockito.when(multiPartnerAdminService.approveOrRejectMappingDeviceToSbi(requestWrapper.getRequest(), false)).thenReturn(responseWrapper);
        mockMvc.perform(post("/admin/approve-mapping-device-to-sbi").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(requestWrapper))).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void approveMappingDeviceToSbiTest1() throws Exception {
        RequestWrapperV2<SbiAndDeviceMappingRequestDto> requestWrapper = new RequestWrapperV2<>();
        requestWrapper.setVersion(VERSION);
        requestWrapper.setRequestTime(LocalDateTime.now());
        SbiAndDeviceMappingRequestDto sbiAndDeviceMappingRequestDto = new SbiAndDeviceMappingRequestDto();
        requestWrapper.setRequest(sbiAndDeviceMappingRequestDto);
        ResponseWrapperV2<Boolean> responseWrapper = new ResponseWrapperV2<>();
        responseWrapper.setResponse(true);
        Mockito.when(multiPartnerAdminService.approveOrRejectMappingDeviceToSbi(requestWrapper.getRequest(), false)).thenReturn(responseWrapper);
        mockMvc.perform(post("/admin/approve-mapping-device-to-sbi").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(requestWrapper))).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void rejectMappingDeviceToSbiTest() throws Exception {
        RequestWrapperV2<SbiAndDeviceMappingRequestDto> requestWrapper = new RequestWrapperV2<>();
        requestWrapper.setId("mosip.pms.reject.mapping.device.to.sbi.post");
        requestWrapper.setVersion(VERSION);
        requestWrapper.setRequestTime(LocalDateTime.now());
        SbiAndDeviceMappingRequestDto sbiAndDeviceMappingRequestDto = new SbiAndDeviceMappingRequestDto();
        requestWrapper.setRequest(sbiAndDeviceMappingRequestDto);
        ResponseWrapperV2<Boolean> responseWrapper = new ResponseWrapperV2<>();
        responseWrapper.setResponse(true);
        Mockito.when(multiPartnerAdminService.approveOrRejectMappingDeviceToSbi(requestWrapper.getRequest(), true)).thenReturn(responseWrapper);
        mockMvc.perform(post("/admin/reject-mapping-device-to-sbi").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(requestWrapper))).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void rejectMappingDeviceToSbiTest1() throws Exception {
        RequestWrapperV2<SbiAndDeviceMappingRequestDto> requestWrapper = new RequestWrapperV2<>();
        requestWrapper.setVersion(VERSION);
        requestWrapper.setRequestTime(LocalDateTime.now());
        SbiAndDeviceMappingRequestDto sbiAndDeviceMappingRequestDto = new SbiAndDeviceMappingRequestDto();
        requestWrapper.setRequest(sbiAndDeviceMappingRequestDto);
        ResponseWrapperV2<Boolean> responseWrapper = new ResponseWrapperV2<>();
        responseWrapper.setResponse(true);
        Mockito.when(multiPartnerAdminService.approveOrRejectMappingDeviceToSbi(requestWrapper.getRequest(), true)).thenReturn(responseWrapper);
        mockMvc.perform(post("/admin/reject-mapping-device-to-sbi").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(requestWrapper))).andExpect(status().isOk());
    }
}
