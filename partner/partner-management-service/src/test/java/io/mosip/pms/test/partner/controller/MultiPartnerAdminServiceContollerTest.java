package io.mosip.pms.test.partner.controller;

import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MultiPartnerAdminServiceController.class})
public class MultiPartnerAdminServiceContollerTest {

    public static final String VERSION = "1.0";

    private static final String MULTI_PARTNER_ADMIN_SERVICE_POST = "multi.partner.admin.service.post";

    @MockBean
    MultiPartnerAdminServiceController multiPartnerAdminServiceController;

    @MockBean
    MultiPartnerAdminService multiPartnerAdminService;

    @MockBean
    RequestValidator requestValidator;

    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void approveDeviceWithSbiMapping() throws Exception {
        RequestWrapper<SbiAndDeviceMappingRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setId(MULTI_PARTNER_ADMIN_SERVICE_POST);
        requestWrapper.setVersion(VERSION);
        requestWrapper.setRequesttime(LocalDateTime.now());
        SbiAndDeviceMappingRequestDto sbiAndDeviceMappingRequestDto = new SbiAndDeviceMappingRequestDto();
        requestWrapper.setRequest(sbiAndDeviceMappingRequestDto);
        Mockito.when(multiPartnerAdminService.approveDeviceWithSbiMapping(requestWrapper.getRequest())).thenReturn(true);
        doNothing().when(requestValidator).validateId(anyString(), anyString());
        doNothing().when(requestValidator).validate(any());
        multiPartnerAdminServiceController.approveDeviceWithSbiMapping(requestWrapper);
    }
}
