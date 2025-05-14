package io.mosip.pms.test.partner.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import io.mosip.pms.common.dto.NotificationsSeenRequestDto;
import io.mosip.pms.common.dto.NotificationsSeenResponseDto;
import io.mosip.pms.common.request.dto.RequestWrapperV2;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.partner.dto.UserDetailsDto;
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
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.partner.dto.MosipUserDto;
import io.mosip.pms.partner.dto.UserRegistrationRequestDto;
import io.mosip.pms.user.service.UserManagementService;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@EnableWebMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    UserManagementService userManagementService;    

	@Autowired
	private ObjectMapper objectMapper;

	@Value("${mosip.pms.api.id.users.notifications.seen.timestamp.put}")
	private String putNotificationsSeenTimestampId;
    
    @Test
    @WithMockUser(roles = {"PARTNER_ADMIN"})
    public void registerUserTest() throws JsonProcessingException, Exception {
    	MosipUserDto response = new MosipUserDto();
		Mockito.when(userManagementService.registerUser(Mockito.any())).thenReturn(response);
		RequestWrapper<UserRegistrationRequestDto> request = createRequest();
		mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isOk());
    }

	private RequestWrapper<UserRegistrationRequestDto> createRequest() {
		RequestWrapper<UserRegistrationRequestDto> request = new RequestWrapper<UserRegistrationRequestDto>();
		request.setId("mosip.partnerservice.MispLicense.create");
		request.setMetadata("{}");
		UserRegistrationRequestDto userRequest = new UserRegistrationRequestDto();
		userRequest.setFirstName("Test");
		userRequest.setUserName("ABC");
		userRequest.setContactNo("121313131");
		userRequest.setEmailID("test@email.com");
		request.setRequest(userRequest);
		request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
		request.setVersion("1.0");
		return request;
	}

	@Test
	@WithMockUser(roles = {"AUTH_PARTNER"})
	public void saveUserConsent() throws Exception {
		ResponseWrapperV2<UserDetailsDto> responseWrapper = new ResponseWrapperV2<>();
		UserDetailsDto userDetailsDto = new UserDetailsDto();
		responseWrapper.setResponse(userDetailsDto);
		Mockito.when(userManagementService.saveUserConsent()).thenReturn(responseWrapper);
		mockMvc.perform(post("/users/user-consent").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = {"AUTH_PARTNER"})
	public void isUserConsentGiven() throws Exception {
		ResponseWrapperV2<UserDetailsDto> responseWrapper = new ResponseWrapperV2<>();
		UserDetailsDto userDetailsDto = new UserDetailsDto();
		responseWrapper.setResponse(userDetailsDto);
		Mockito.when(userManagementService.isUserConsentGiven()).thenReturn(responseWrapper);
		mockMvc.perform(get("/users/user-consent").contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = {"AUTH_PARTNER"})
	public void getConfigValuesTest() throws Exception {
		mockMvc.perform(get("/system-config").contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = {"AUTH_PARTNER"})
	public void updateNotificationsSeenTimestampTest() throws Exception {
		RequestWrapperV2<NotificationsSeenRequestDto> requestWrapper = new RequestWrapperV2<>();
		requestWrapper.setId(putNotificationsSeenTimestampId);
		requestWrapper.setVersion("1.0");
		requestWrapper.setRequestTime(LocalDateTime.now());
		NotificationsSeenRequestDto requestDto = new NotificationsSeenRequestDto();
		requestDto.setNotificationsSeenDtimes(LocalDateTime.now());
		requestWrapper.setRequest(requestDto);
		ResponseWrapperV2<NotificationsSeenResponseDto> responseWrapper = new ResponseWrapperV2<>();
		NotificationsSeenResponseDto notificationsSeenResponseDto = new NotificationsSeenResponseDto();
		responseWrapper.setResponse(notificationsSeenResponseDto);

		Mockito.when(userManagementService.updateNotificationsSeenTimestamp(Mockito.anyString(), Mockito.any())).thenReturn(responseWrapper);
		mockMvc.perform(MockMvcRequestBuilders.put("/users/test123/notifications-seen-timestamp").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(requestWrapper))).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = {"AUTH_PARTNER"})
	public void updateNotificationsSeenTimestampTest1() throws Exception {
		RequestWrapperV2<NotificationsSeenRequestDto> requestWrapper = new RequestWrapperV2<>();
		NotificationsSeenRequestDto requestDto = new NotificationsSeenRequestDto();
		requestDto.setNotificationsSeenDtimes(LocalDateTime.now());
		requestWrapper.setRequest(requestDto);
		ResponseWrapperV2<NotificationsSeenResponseDto> responseWrapper = new ResponseWrapperV2<>();
		NotificationsSeenResponseDto notificationsSeenResponseDto = new NotificationsSeenResponseDto();
		responseWrapper.setResponse(notificationsSeenResponseDto);

		Mockito.when(userManagementService.updateNotificationsSeenTimestamp(Mockito.anyString(), Mockito.any())).thenReturn(responseWrapper);
		mockMvc.perform(MockMvcRequestBuilders.put("/users/test123/notifications-seen-timestamp").contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(objectMapper.writeValueAsString(requestWrapper))).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = {"AUTH_PARTNER"})
	public void getNotificationsSeenTimestampTest() throws Exception {
		ResponseWrapperV2<NotificationsSeenResponseDto> responseWrapper = new ResponseWrapperV2<>();
		NotificationsSeenResponseDto notificationsSeenResponseDto = new NotificationsSeenResponseDto();
		responseWrapper.setResponse(notificationsSeenResponseDto);

		Mockito.when(userManagementService.updateNotificationsSeenTimestamp(Mockito.anyString(), Mockito.any())).thenReturn(responseWrapper);
		mockMvc.perform(get("/users/test123/notifications-seen-timestamp").contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk());
	}
}
