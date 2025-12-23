package io.mosip.pms.test.partner.service.impl;

import io.mosip.kernel.openid.bridge.model.AuthUserDetails;
import io.mosip.pms.common.dto.NotificationsSeenRequestDto;
import io.mosip.pms.common.dto.NotificationsSeenResponseDto;
import io.mosip.pms.common.entity.UserDetails;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.repository.UserDetailsRepository;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
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
import org.springframework.test.util.ReflectionTestUtils;

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

	@Mock
	io.mosip.pms.partner.util.PartnerHelper partnerHelper;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(userManagementServiceImpl, "postSaveUserConsentGivenId", "post.consent.id");
		ReflectionTestUtils.setField(userManagementServiceImpl, "getUserConsentGivenId", "get.consent.id");
		ReflectionTestUtils.setField(userManagementServiceImpl, "putNotificationsSeenTimestampId", "put.notification.id");
		ReflectionTestUtils.setField(userManagementServiceImpl, "getNotificationsSeenTimestampId", "get.notification.id");
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
		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(false);
		when(partnerHelper.isPolicyManager(anyString())).thenReturn(false);

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
		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(false);
		when(partnerHelper.isPolicyManager(anyString())).thenReturn(false);

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
		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(false);
		when(partnerHelper.isPolicyManager(anyString())).thenReturn(false);

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
		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(false);
		when(partnerHelper.isPolicyManager(anyString())).thenReturn(false);

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
		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(false);
		when(partnerHelper.isPolicyManager(anyString())).thenReturn(false);

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

	@Test
	public void updateNotificationsSeenTimestampTest() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(false);

		NotificationsSeenRequestDto requestDto = new NotificationsSeenRequestDto();
		requestDto.setNotificationsSeenDtimes(LocalDateTime.now());

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
		when(userDetailsRepository.findByUserId(anyString())).thenReturn(Optional.of(userDetails));

		userDetails.setNotificationsSeenDtimes(LocalDateTime.now());
		userDetails.setUpdBy("12345");
		userDetails.setUpdDtimes(LocalDateTime.now());
		when(userDetailsRepository.save(any())).thenReturn(userDetails);
		userManagementServiceImpl.updateNotificationsSeenTimestamp("12345", requestDto);

		NotificationsSeenRequestDto requestDto1 = new NotificationsSeenRequestDto();
		userManagementServiceImpl.updateNotificationsSeenTimestamp("12345", requestDto1);

		partnerList = new ArrayList<>();
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		userManagementServiceImpl.updateNotificationsSeenTimestamp("12345", requestDto);
	}

	@Test
	public void getNotificationsSeenTimestampTest() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(false);

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
		userDetails.setNotificationsSeenDtimes(LocalDateTime.now());
		userDetails.setUpdBy("abc");
		userDetails.setUpdDtimes(LocalDateTime.now());
		when(userDetailsRepository.findByUserId(anyString())).thenReturn(Optional.of(userDetails));
		userManagementServiceImpl.getNotificationsSeenTimestamp("12345");

		partnerList = new ArrayList<>();
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		userManagementServiceImpl.getNotificationsSeenTimestamp("12345");
	}

	@Test
	public void getNotificationsSeenTimestampExceptionTest() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(false);

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("Auth_Partner");
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
		userManagementServiceImpl.getNotificationsSeenTimestamp("12345");
	}

	@Test
	public void testSaveUserConsentWithAdminRole() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(true);
		when(partnerHelper.isPolicyManager(anyString())).thenReturn(false);

		UserDetails userDetails = new UserDetails();
		userDetails.setUserId("123");
		when(userDetailsRepository.save(any())).thenReturn(userDetails);
		userManagementServiceImpl.saveUserConsent();
	}

	@Test
	public void testSaveUserConsentWithPolicyManagerRole() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(false);
		when(partnerHelper.isPolicyManager(anyString())).thenReturn(true);

		UserDetails userDetails = new UserDetails();
		userDetails.setUserId("123");
		when(userDetailsRepository.save(any())).thenReturn(userDetails);
		userManagementServiceImpl.saveUserConsent();
	}

	@Test
	public void testSaveUserConsentWithGeneralException() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(false);
		when(partnerHelper.isPolicyManager(anyString())).thenReturn(false);

		List<Partner> partnerList = new ArrayList<>();
		partnerList.add(new Partner());
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(userDetailsRepository.findByUserId(anyString())).thenThrow(new RuntimeException("DB error"));
		userManagementServiceImpl.saveUserConsent();
	}

	@Test
	public void testIsUserConsentGivenWithAdminRole() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(true);
		when(partnerHelper.isPolicyManager(anyString())).thenReturn(false);
		when(userDetailsRepository.findByUserId(anyString())).thenReturn(Optional.empty());
		userManagementServiceImpl.isUserConsentGiven();
	}

	@Test
	public void testIsUserConsentGivenWithPolicyManagerRole() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(false);
		when(partnerHelper.isPolicyManager(anyString())).thenReturn(true);

		UserDetails userDetails = new UserDetails();
		userDetails.setUserId("123");
		userDetails.setConsentGiven("YES");
		userDetails.setConsentGivenDtimes(LocalDateTime.now());
		when(userDetailsRepository.findByUserId(anyString())).thenReturn(Optional.of(userDetails));
		userManagementServiceImpl.isUserConsentGiven();
	}

	@Test
	public void testIsUserConsentGivenWithConsentNotYes() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(false);
		when(partnerHelper.isPolicyManager(anyString())).thenReturn(false);

		List<Partner> partnerList = new ArrayList<>();
		partnerList.add(new Partner());
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);

		UserDetails userDetails = new UserDetails();
		userDetails.setConsentGiven("NO");
		when(userDetailsRepository.findByUserId(anyString())).thenReturn(Optional.of(userDetails));
		userManagementServiceImpl.isUserConsentGiven();
	}

	@Test
	public void testIsUserConsentGivenWithEmptyUserDetails() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(false);
		when(partnerHelper.isPolicyManager(anyString())).thenReturn(false);

		List<Partner> partnerList = new ArrayList<>();
		partnerList.add(new Partner());
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(userDetailsRepository.findByUserId(anyString())).thenReturn(Optional.empty());
		userManagementServiceImpl.isUserConsentGiven();
	}

	@Test
	public void testIsUserConsentGivenWithGeneralException() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(false);
		when(partnerHelper.isPolicyManager(anyString())).thenReturn(false);

		List<Partner> partnerList = new ArrayList<>();
		partnerList.add(new Partner());
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(userDetailsRepository.findByUserId(anyString())).thenThrow(new RuntimeException("DB error"));
		userManagementServiceImpl.isUserConsentGiven();
	}

	@Test
	public void testUpdateNotificationsSeenTimestampWithAdminRole() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(true);

		UserDetails userDetails = new UserDetails();
		when(userDetailsRepository.findByUserId(anyString())).thenReturn(Optional.of(userDetails));
		when(userDetailsRepository.save(any())).thenReturn(userDetails);

		NotificationsSeenRequestDto requestDto = new NotificationsSeenRequestDto();
		requestDto.setNotificationsSeenDtimes(LocalDateTime.now());
		userManagementServiceImpl.updateNotificationsSeenTimestamp("123", requestDto);
	}

	@Test
	public void testUpdateNotificationsSeenTimestampWithUserDetailsNotFound() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(false);

		List<Partner> partnerList = new ArrayList<>();
		partnerList.add(new Partner());
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(userDetailsRepository.findByUserId(anyString())).thenReturn(Optional.empty());

		NotificationsSeenRequestDto requestDto = new NotificationsSeenRequestDto();
		requestDto.setNotificationsSeenDtimes(LocalDateTime.now());
		userManagementServiceImpl.updateNotificationsSeenTimestamp("123", requestDto);
	}

	@Test
	public void testUpdateNotificationsSeenTimestampWithGeneralException() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isPartnerAdmin(anyString())).thenThrow(new RuntimeException("Test exception"));

		NotificationsSeenRequestDto requestDto = new NotificationsSeenRequestDto();
		userManagementServiceImpl.updateNotificationsSeenTimestamp("123", requestDto);
	}

	@Test
	public void testUpdateNotificationsSeenTimestampWithNonAdminUser() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(false);

		List<Partner> partnerList = new ArrayList<>();
		partnerList.add(new Partner());
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);

		UserDetails userDetails = new UserDetails();
		userDetails.setUserId("123");
		userDetails.setNotificationsSeenDtimes(LocalDateTime.now());
		when(userDetailsRepository.findByUserId(anyString())).thenReturn(Optional.of(userDetails));
		when(userDetailsRepository.save(any())).thenReturn(userDetails);

		NotificationsSeenRequestDto requestDto = new NotificationsSeenRequestDto();
		requestDto.setNotificationsSeenDtimes(LocalDateTime.now());
		userManagementServiceImpl.updateNotificationsSeenTimestamp("123", requestDto);
	}

	@Test
	public void testGetNotificationsSeenTimestampWithAdminRole() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(true);

		UserDetails userDetails = new UserDetails();
		userDetails.setNotificationsSeenDtimes(LocalDateTime.now());
		when(userDetailsRepository.findByUserId(anyString())).thenReturn(Optional.of(userDetails));
		userManagementServiceImpl.getNotificationsSeenTimestamp("123");
	}

	@Test
	public void testGetNotificationsSeenTimestampWithGeneralException() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isPartnerAdmin(anyString())).thenThrow(new RuntimeException("Test exception"));
		userManagementServiceImpl.getNotificationsSeenTimestamp("123");
	}

	@Test
	public void testGetNotificationsSeenTimestampWithNonAdminUser() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(false);

		List<Partner> partnerList = new ArrayList<>();
		partnerList.add(new Partner());
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);

		UserDetails userDetails = new UserDetails();
		userDetails.setUserId("123");
		userDetails.setNotificationsSeenDtimes(LocalDateTime.now());
		when(userDetailsRepository.findByUserId(anyString())).thenReturn(Optional.of(userDetails));
		userManagementServiceImpl.getNotificationsSeenTimestamp("123");
	}

	@Test
	public void testSaveUserConsentWithEmptyUserDetails() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(false);
		when(partnerHelper.isPolicyManager(anyString())).thenReturn(false);

		List<Partner> partnerList = new ArrayList<>();
		partnerList.add(new Partner());
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(userDetailsRepository.findByUserId(anyString())).thenReturn(Optional.empty());

		UserDetails userDetails = new UserDetails();
		userDetails.setUserId("123");
		when(userDetailsRepository.save(any())).thenReturn(userDetails);
		userManagementServiceImpl.saveUserConsent();
	}

	private io.mosip.kernel.openid.bridge.model.MosipUserDto getMosipUserDto() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = new io.mosip.kernel.openid.bridge.model.MosipUserDto();
		mosipUserDto.setUserId("123");
		mosipUserDto.setMail("abc@gmail.com");
		return mosipUserDto;
	}
}
