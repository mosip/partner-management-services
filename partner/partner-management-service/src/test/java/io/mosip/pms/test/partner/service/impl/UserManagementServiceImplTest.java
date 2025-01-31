package io.mosip.pms.test.partner.service.impl;

import io.mosip.kernel.openid.bridge.model.AuthUserDetails;
import io.mosip.pms.common.dto.UserDetails;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.repository.UserDetailsRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import io.mosip.pms.partner.dto.MosipUserDto;
import io.mosip.pms.partner.dto.UserRegistrationRequestDto;
import io.mosip.pms.partner.keycloak.service.KeycloakImpl;
import io.mosip.pms.user.service.impl.UserManagementServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserManagementServiceImplTest {
	
	@InjectMocks
	UserManagementServiceImpl userManagementServiceImpl;
	
	@Mock
	private KeycloakImpl keycloakImpl;

	@Mock
	Authentication authentication;
	
	@Mock
	SecurityContext securityContext;

	@Mock
	UserDetailsRepository userDetailsRepository;

	@Mock
	PartnerServiceRepository partnerRepository;
	
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

	@Test
	public void saveUserConsentTest() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("Auth_Partner");
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));

		UserDetails userDetails = new UserDetails();
		userDetails.setUserId("123");
		userDetails.setId("abc");
		userDetails.setCrDtimes(LocalDateTime.now());
		userDetails.setCrBy("abc");
		Optional<UserDetails> optionalEntity = Optional.of(new UserDetails());
		when(userDetailsRepository.findByUserId(anyString())).thenReturn(optionalEntity);
		when(userDetailsRepository.save(any())).thenReturn(userDetails);
		userManagementServiceImpl.saveUserConsent();
	}

	@Test
	public void saveUserConsentTest1() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("Auth_Partner");
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));

		UserDetails userDetails = new UserDetails();
		userDetails.setUserId("123");
		userDetails.setId("abc");
		userDetails.setCrDtimes(LocalDateTime.now());
		userDetails.setCrBy("abc");
		when(userDetailsRepository.save(any())).thenReturn(userDetails);
		userManagementServiceImpl.saveUserConsent();
	}

	@Test
	public void saveUserConsentExceptionTest() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
		userManagementServiceImpl.saveUserConsent();
	}
	@Test
	public void saveUserConsentExceptionTest1() throws Exception {
		userManagementServiceImpl.saveUserConsent();
	}

	@Test
	public void isUserConsentGivenTest() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("Auth_Partner");
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
		UserDetails userDetails = new UserDetails();
		userDetails.setUserId("123");
		userDetails.setId("abc");
		userDetails.setCrDtimes(LocalDateTime.now());
		userDetails.setCrBy("abc");
		userDetails.setConsentGiven("YES");
		userDetails.setConsentGivenDtimes(LocalDateTime.now());
		Optional<UserDetails> optionalEntity = Optional.of(userDetails);
		when(userDetailsRepository.findByUserId(anyString())).thenReturn(optionalEntity);
		userManagementServiceImpl.isUserConsentGiven();
	}

	@Test
	public void isUserConsentGivenExceptionTest() throws Exception {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
		userManagementServiceImpl.isUserConsentGiven();
	}

	@Test
	public void isUserConsentGivenExceptionTest1() throws Exception {
		userManagementServiceImpl.isUserConsentGiven();
	}

	private io.mosip.kernel.openid.bridge.model.MosipUserDto getMosipUserDto() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = new io.mosip.kernel.openid.bridge.model.MosipUserDto();
		mosipUserDto.setUserId("123");
		mosipUserDto.setMail("abc@gmail.com");
		return mosipUserDto;
	}

}
