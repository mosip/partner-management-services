package io.mosip.pms.test.partner.controller;

import io.mosip.pms.common.request.dto.RequestWrapperV2;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.partner.controller.MultiPartnerAdminServiceController;
import io.mosip.pms.partner.request.dto.SbiAndDeviceMappingRequestDto;
import io.mosip.pms.partner.service.MultiPartnerAdminService;
import io.mosip.pms.partner.util.RequestValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MultiPartnerAdminServiceController.class})
public class MultiPartnerAdminServiceContollerTest {

    public static final String VERSION = "1.0";

    @MockBean
    MultiPartnerAdminServiceController multiPartnerAdminServiceController;

    @MockBean
    MultiPartnerAdminService multiPartnerAdminService;

    @MockBean
    RequestValidator requestValidator;

    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void approveMappingDeviceToSbi() throws Exception {
        RequestWrapperV2<SbiAndDeviceMappingRequestDto> requestWrapper = new RequestWrapperV2<>();
        requestWrapper.setVersion(VERSION);
        requestWrapper.setRequestTime(LocalDateTime.now());
        SbiAndDeviceMappingRequestDto sbiAndDeviceMappingRequestDto = new SbiAndDeviceMappingRequestDto();
        requestWrapper.setRequest(sbiAndDeviceMappingRequestDto);
        ResponseWrapperV2<Boolean> responseWrapper = new ResponseWrapperV2<>();
        responseWrapper.setResponse(true);
        Mockito.when(multiPartnerAdminService.approveOrRejectMappingDeviceToSbi(requestWrapper.getRequest(), false)).thenReturn(responseWrapper);
        multiPartnerAdminServiceController.approveMappingDeviceToSbi(requestWrapper);
    }

    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void rejectMappingDeviceToSbi() throws Exception {
        RequestWrapperV2<SbiAndDeviceMappingRequestDto> requestWrapper = new RequestWrapperV2<>();
        requestWrapper.setVersion(VERSION);
        requestWrapper.setRequestTime(LocalDateTime.now());
        SbiAndDeviceMappingRequestDto sbiAndDeviceMappingRequestDto = new SbiAndDeviceMappingRequestDto();
        requestWrapper.setRequest(sbiAndDeviceMappingRequestDto);
        ResponseWrapperV2<Boolean> responseWrapper = new ResponseWrapperV2<>();
        responseWrapper.setResponse(true);
        Mockito.when(multiPartnerAdminService.approveOrRejectMappingDeviceToSbi(requestWrapper.getRequest(), true)).thenReturn(responseWrapper);
        multiPartnerAdminServiceController.rejectMappingDeviceToSbi(requestWrapper);
    }
}
