package io.mosip.pms.test.partner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.pms.common.dto.DismissNotificationRequestDto;
import io.mosip.pms.common.dto.DismissNotificationResponseDto;
import io.mosip.pms.common.dto.NotificationsResponseDto;
import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.request.dto.RequestWrapperV2;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.partner.dto.NotificationsFilterDto;
import io.mosip.pms.partner.manager.dto.ApiKeyFilterDto;
import io.mosip.pms.partner.manager.dto.ApiKeyRequestSummaryDto;
import io.mosip.pms.partner.service.NotificationsService;
import io.mosip.pms.partner.service.impl.NotificationsServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@EnableWebMvc
public class NotificationsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    NotificationsService notificationsService;

    @Value("${mosip.pms.api.id.dismiss.notification.patch}")
    private String patchDismissNotificationId;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void getNotificationsTest() throws Exception {
        Integer pageNo = 0;
        Integer pageSize = 4;
        NotificationsFilterDto filterDto = new NotificationsFilterDto();
        ResponseWrapperV2<PageResponseV2Dto<NotificationsResponseDto>> responseWrapper = new ResponseWrapperV2<>();

        Mockito.when(notificationsService.getNotifications(pageNo, pageSize, filterDto))
                .thenReturn(responseWrapper);
        mockMvc.perform(MockMvcRequestBuilders.get("/notifications")
                        .param("pageNo", String.valueOf(pageNo))
                        .param("pageSize", String.valueOf(pageSize))
                        .param("notificationStatus", "active")
                        .param("notificationType", "root")
                        .param("certificateId", "ABC")
                        .param("issuedBy", "abc")
                        .param("issuedTo", "abc")
                        .param("expiryDate", "2025-04-29T11:21:19.226Z")
                        .param("partnerDomain", "auth")
                        .param("createdFromDate", "2025-04-29T11:21:19.226Z")
                        .param("createdToDate", "2025-04-29T11:21:19.226Z"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void dismissNotificationTest() throws Exception {
        RequestWrapperV2<DismissNotificationRequestDto> requestWrapper = new RequestWrapperV2<>();
        requestWrapper.setId(patchDismissNotificationId);
        requestWrapper.setVersion("1.0");
        requestWrapper.setRequestTime(LocalDateTime.now());
        DismissNotificationRequestDto requestDto = new DismissNotificationRequestDto();
        requestDto.setNotificationStatus("DISMISSED");
        requestWrapper.setRequest(requestDto);

        ResponseWrapperV2<DismissNotificationResponseDto> responseWrapper = new ResponseWrapperV2<>();
        DismissNotificationResponseDto responseDto = new DismissNotificationResponseDto();
        responseWrapper.setResponse(responseDto);

        Mockito.when(notificationsService.dismissNotification("12345", requestDto))
                .thenReturn(responseWrapper);

        mockMvc.perform(MockMvcRequestBuilders.patch("/notifications/12345").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(requestWrapper))).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void dismissNotificationTest1() throws Exception {
        RequestWrapperV2<DismissNotificationRequestDto> requestWrapper = new RequestWrapperV2<>();
        DismissNotificationRequestDto requestDto = new DismissNotificationRequestDto();
        requestDto.setNotificationStatus("DISMISSED");
        requestWrapper.setRequest(requestDto);

        ResponseWrapperV2<DismissNotificationResponseDto> responseWrapper = new ResponseWrapperV2<>();
        DismissNotificationResponseDto responseDto = new DismissNotificationResponseDto();
        responseWrapper.setResponse(responseDto);

        Mockito.when(notificationsService.dismissNotification("12345", requestDto))
                .thenReturn(responseWrapper);

        mockMvc.perform(MockMvcRequestBuilders.patch("/notifications/12345").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(requestWrapper))).andExpect(status().isOk());
    }
}
