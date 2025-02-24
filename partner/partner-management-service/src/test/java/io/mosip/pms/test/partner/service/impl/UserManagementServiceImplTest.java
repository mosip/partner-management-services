package io.mosip.pms.test.partner.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import io.mosip.pms.partner.dto.MosipUserDto;
import io.mosip.pms.partner.dto.UserRegistrationRequestDto;
import io.mosip.pms.partner.keycloak.service.KeycloakImpl;
import io.mosip.pms.user.service.impl.UserManagementServiceImpl;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserManagementServiceImplTest {
	
	@Autowired
	UserManagementServiceImpl userManagementServiceImpl;
	
	@MockBean
	private KeycloakImpl keycloakImpl;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void registerUserTest() {
		UserRegistrationRequestDto registrationRequest = new UserRegistrationRequestDto();
		registrationRequest.setUserName("partner");
		MosipUserDto userDto = new MosipUserDto();
		userDto.setName("PARTNER");
		userDto.setMobile("partner@gmail.com");
		Mockito.doReturn(userDto).when(keycloakImpl).registerUser(registrationRequest);
		userManagementServiceImpl.registerUser(registrationRequest);
		
	}

}
