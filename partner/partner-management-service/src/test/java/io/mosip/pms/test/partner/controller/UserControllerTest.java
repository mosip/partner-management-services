package io.mosip.pms.test.partner.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

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
		request.setRequest(userRequest);
		request.setRequesttime(ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
		request.setVersion("1.0");
		return request;
	}
}
