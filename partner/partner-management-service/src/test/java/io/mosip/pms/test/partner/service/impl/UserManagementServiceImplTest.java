package io.mosip.pms.test.partner.service.impl;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.pms.common.dto.NotificationsSeenRequestDto;
import io.mosip.pms.common.dto.NotificationsSeenResponseDto;
import io.mosip.pms.common.entity.UserDetails;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.common.repository.UserDetailsRepository;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.partner.dto.UserDetailsDto;
import org.junit.After;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.pms.partner.constant.ErrorCode;
import io.mosip.pms.partner.dto.MosipUserDto;
import io.mosip.pms.partner.dto.UserRegistrationRequestDto;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.keycloak.service.KeycloakImpl;
import io.mosip.pms.user.service.impl.UserManagementServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserManagementServiceImplTest {

	private AuthUserDetails mockAuthUserDetails(String userId, String... authorities) {
		AuthUserDetails details = Mockito.mock(AuthUserDetails.class);
		when(details.getUserId()).thenReturn(userId);
		Collection auths = new ArrayList<GrantedAuthority>();
		if (authorities != null) {
			for (String a : authorities) {
				((Collection<GrantedAuthority>) auths).add(new SimpleGrantedAuthority(a));
			}
		}
		when(details.getAuthorities()).thenReturn(auths);
		return details;
	}
	
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
		// Mirrors PartnerHelper#validateLoggedInUserAuthorization: no-op when exempt (e.g. admin),
		// otherwise throws on mismatch against the currently mocked logged-in user.
		Mockito.doAnswer(invocation -> {
			String requestedUserId = invocation.getArgument(0);
			if (!partnerHelper.isOwnershipFilterExempt()) {
				AuthUserDetails details = (AuthUserDetails) SecurityContextHolder.getContext()
						.getAuthentication().getPrincipal();
				if (details == null || !details.getUserId().equals(requestedUserId)) {
					throw new PartnerServiceException(ErrorCode.LOGGEDIN_USER_NOT_AUTHORIZED.getErrorCode(),
							ErrorCode.LOGGEDIN_USER_NOT_AUTHORIZED.getErrorMessage());
				}
			}
			return null;
		}).when(partnerHelper).validateLoggedInUserAuthorization(anyString());
	}

	@After
	public void tearDown() {
		SecurityContextHolder.clearContext();
	}
	
	@Test
	public void registerUserTest() {
		UserRegistrationRequestDto registrationRequest = new UserRegistrationRequestDto();
		registrationRequest.setUserName("partner");
		MosipUserDto userDto = new MosipUserDto();
		userDto.setName("PARTNER");
		userDto.setMobile("partner@gmail.com");
		Mockito.doReturn(userDto).when(keycloakImpl).registerUser(registrationRequest);
		MosipUserDto result = userManagementServiceImpl.registerUser(registrationRequest);
		assertNotNull(result);
		verify(keycloakImpl).registerUser(registrationRequest);
	}

	@Test
	public void saveUserConsentTest() throws Exception {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(false);
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
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(false);
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
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(false);
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
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(false);
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
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(false);
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
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(false);

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
		userDetails.setUpdBy("123");
		userDetails.setUpdDtimes(LocalDateTime.now());
		when(userDetailsRepository.save(any())).thenReturn(userDetails);
		userManagementServiceImpl.updateNotificationsSeenTimestamp("123", requestDto);

		NotificationsSeenRequestDto requestDto1 = new NotificationsSeenRequestDto();
		userManagementServiceImpl.updateNotificationsSeenTimestamp("123", requestDto1);

		partnerList = new ArrayList<>();
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		userManagementServiceImpl.updateNotificationsSeenTimestamp("123", requestDto);
	}

	@Test
	public void updateNotificationsSeenTimestamp_whenUserIdDoesNotMatchLoggedInUser_setsUnauthorizedError() {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(false);

		NotificationsSeenRequestDto requestDto = new NotificationsSeenRequestDto();
		requestDto.setNotificationsSeenDtimes(LocalDateTime.now());

		ResponseWrapperV2<NotificationsSeenResponseDto> response =
				userManagementServiceImpl.updateNotificationsSeenTimestamp("someone-else", requestDto);

		assertNotNull(response);
		assertTrue(response.getErrors() != null && !response.getErrors().isEmpty());
		assertEquals(ErrorCode.LOGGEDIN_USER_NOT_AUTHORIZED.getErrorCode(),
				response.getErrors().get(0).getErrorCode());
		Mockito.verify(partnerRepository, Mockito.never()).findByUserId(anyString());
	}

	@Test
	public void getNotificationsSeenTimestampTest() {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(false);

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
		userManagementServiceImpl.getNotificationsSeenTimestamp("123");

		partnerList = new ArrayList<>();
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		userManagementServiceImpl.getNotificationsSeenTimestamp("123");
	}

	@Test
	public void getNotificationsSeenTimestamp_whenUserIdDoesNotMatchLoggedInUser_setsUnauthorizedError() {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(false);

		ResponseWrapperV2<NotificationsSeenResponseDto> response =
				userManagementServiceImpl.getNotificationsSeenTimestamp("someone-else");

		assertNotNull(response);
		assertTrue(response.getErrors() != null && !response.getErrors().isEmpty());
		assertEquals(ErrorCode.LOGGEDIN_USER_NOT_AUTHORIZED.getErrorCode(),
				response.getErrors().get(0).getErrorCode());
		Mockito.verify(partnerRepository, Mockito.never()).findByUserId(anyString());
	}

	@Test
	public void getNotificationsSeenTimestampExceptionTest() {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(false);

		List<Partner> partnerList = new ArrayList<>();
		Partner partner = new Partner();
		partner.setId("123");
		partner.setPartnerTypeCode("Auth_Partner");
		partnerList.add(partner);
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(partnerRepository.findById(anyString())).thenReturn(Optional.of(partner));
		userManagementServiceImpl.getNotificationsSeenTimestamp("123");
	}

	@Test
	public void testSaveUserConsentWithAdminRole() {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "PARTNER_ADMIN");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(true);
		when(partnerHelper.isPolicyManager(anyString())).thenReturn(false);

		UserDetails userDetails = new UserDetails();
		userDetails.setUserId("123");
		when(userDetailsRepository.save(any())).thenReturn(userDetails);
		userManagementServiceImpl.saveUserConsent();
	}

	@Test
	public void testSaveUserConsentWithPolicyManagerRole() {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "POLICY_MANAGER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(false);
		when(partnerHelper.isPolicyManager(anyString())).thenReturn(true);

		UserDetails userDetails = new UserDetails();
		userDetails.setUserId("123");
		when(userDetailsRepository.save(any())).thenReturn(userDetails);
		userManagementServiceImpl.saveUserConsent();
	}

	@Test
	public void testSaveUserConsentWithGeneralException() {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(false);
		when(partnerHelper.isPolicyManager(anyString())).thenReturn(false);

		List<Partner> partnerList = new ArrayList<>();
		partnerList.add(new Partner());
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(userDetailsRepository.findByUserId(anyString())).thenThrow(new RuntimeException("DB error"));
		userManagementServiceImpl.saveUserConsent();
	}

	@Test
	public void testIsUserConsentGivenWithAdminRole() {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "PARTNER_ADMIN");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(true);
		when(partnerHelper.isPolicyManager(anyString())).thenReturn(false);
		when(userDetailsRepository.findByUserId(anyString())).thenReturn(Optional.empty());
		userManagementServiceImpl.isUserConsentGiven();
	}

	@Test
	public void testIsUserConsentGivenWithPolicyManagerRole() {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "POLICY_MANAGER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(false);
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
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(false);
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
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(false);
		when(partnerHelper.isPolicyManager(anyString())).thenReturn(false);

		List<Partner> partnerList = new ArrayList<>();
		partnerList.add(new Partner());
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(userDetailsRepository.findByUserId(anyString())).thenReturn(Optional.empty());
		userManagementServiceImpl.isUserConsentGiven();
	}

	@Test
	public void testIsUserConsentGivenWithGeneralException() {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(false);
		when(partnerHelper.isPolicyManager(anyString())).thenReturn(false);

		List<Partner> partnerList = new ArrayList<>();
		partnerList.add(new Partner());
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(userDetailsRepository.findByUserId(anyString())).thenThrow(new RuntimeException("DB error"));
		userManagementServiceImpl.isUserConsentGiven();
	}

	@Test
	public void testUpdateNotificationsSeenTimestampWithAdminRole() {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "PARTNER_ADMIN");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(true);

		UserDetails userDetails = new UserDetails();
		when(userDetailsRepository.findByUserId(anyString())).thenReturn(Optional.of(userDetails));
		when(userDetailsRepository.save(any())).thenReturn(userDetails);

		NotificationsSeenRequestDto requestDto = new NotificationsSeenRequestDto();
		requestDto.setNotificationsSeenDtimes(LocalDateTime.now());
		userManagementServiceImpl.updateNotificationsSeenTimestamp("123", requestDto);
	}

	@Test
	public void testUpdateNotificationsSeenTimestampWithUserDetailsNotFound() {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(false);

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
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenThrow(new RuntimeException("Test exception"));

		NotificationsSeenRequestDto requestDto = new NotificationsSeenRequestDto();
		userManagementServiceImpl.updateNotificationsSeenTimestamp("123", requestDto);
	}

	@Test
	public void testUpdateNotificationsSeenTimestampWithNonAdminUser() {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(false);

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
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "PARTNER_ADMIN");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(true);

		UserDetails userDetails = new UserDetails();
		userDetails.setNotificationsSeenDtimes(LocalDateTime.now());
		when(userDetailsRepository.findByUserId(anyString())).thenReturn(Optional.of(userDetails));
		userManagementServiceImpl.getNotificationsSeenTimestamp("123");
	}

	@Test
	public void testGetNotificationsSeenTimestampWithGeneralException() {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "PARTNER_ADMIN");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenThrow(new RuntimeException("Test exception"));
		userManagementServiceImpl.getNotificationsSeenTimestamp("123");
	}

	@Test
	public void testSaveUserConsentWithEmptyUserDetails() {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(false);
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

	@Test
	public void testUpdateNotificationsSeenTimestampWithNullTimestamp() {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(false);

		List<Partner> partnerList = new ArrayList<>();
		partnerList.add(new Partner());
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);

		UserDetails userDetails = new UserDetails();
		when(userDetailsRepository.findByUserId(anyString())).thenReturn(Optional.of(userDetails));

		NotificationsSeenRequestDto requestDto = new NotificationsSeenRequestDto();
		requestDto.setNotificationsSeenDtimes(null);
		ResponseWrapperV2<NotificationsSeenResponseDto> response =
				userManagementServiceImpl.updateNotificationsSeenTimestamp("123", requestDto);
		assertNotNull(response);
		assertTrue(response.getErrors() != null && !response.getErrors().isEmpty());
		assertEquals(ErrorCode.UNABLE_TO_UPDATE_NOTIFICATIONS_SEEN_TIME.getErrorCode(),
				response.getErrors().get(0).getErrorCode());
	}

	@Test
	public void testUpdateNotificationsSeenTimestampPartnerServiceException() {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(false);
		when(partnerRepository.findByUserId(anyString())).thenReturn(new ArrayList<>());

		NotificationsSeenRequestDto requestDto = new NotificationsSeenRequestDto();
		requestDto.setNotificationsSeenDtimes(LocalDateTime.now());
		userManagementServiceImpl.updateNotificationsSeenTimestamp("123", requestDto);
	}

	@Test
	public void testUpdateNotificationsSeenTimestampGeneralException() {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(false);

		List<Partner> partnerList = new ArrayList<>();
		partnerList.add(new Partner());
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(userDetailsRepository.findByUserId(anyString())).thenThrow(new RuntimeException("DB error"));

		NotificationsSeenRequestDto requestDto = new NotificationsSeenRequestDto();
		requestDto.setNotificationsSeenDtimes(LocalDateTime.now());
		userManagementServiceImpl.updateNotificationsSeenTimestamp("123", requestDto);
	}

	@Test
	public void testGetNotificationsSeenTimestampWithNonAdminUser() {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(false);

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
	public void testGetNotificationsSeenTimestampPartnerServiceException() {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(false);
		when(partnerRepository.findByUserId(anyString())).thenReturn(new ArrayList<>());
		userManagementServiceImpl.getNotificationsSeenTimestamp("123");
	}

	@Test
	public void testGetNotificationsSeenTimestampUserDetailsNotFound() {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(false);

		List<Partner> partnerList = new ArrayList<>();
		partnerList.add(new Partner());
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);
		when(userDetailsRepository.findByUserId(anyString())).thenReturn(Optional.empty());
		userManagementServiceImpl.getNotificationsSeenTimestamp("123");
	}

	@Test
	public void testSaveUserConsentPartnerServiceException() {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(false);
		when(partnerHelper.isPolicyManager(anyString())).thenReturn(false);
		when(partnerRepository.findByUserId(anyString())).thenReturn(new ArrayList<>());
		userManagementServiceImpl.saveUserConsent();
	}

	@Test
	public void testSaveUserConsentWithNonAdminNonPolicyManager() {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(false);
		when(partnerHelper.isPolicyManager(anyString())).thenReturn(false);

		List<Partner> partnerList = new ArrayList<>();
		partnerList.add(new Partner());
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);

		UserDetails existingUserDetails = new UserDetails();
		existingUserDetails.setId("existing-id");
		existingUserDetails.setCrBy("creator");
		existingUserDetails.setCrDtimes(LocalDateTime.now());
		existingUserDetails.setUserId("existing-user-id");
		when(userDetailsRepository.findByUserId(anyString())).thenReturn(Optional.of(existingUserDetails));

		UserDetails savedUserDetails = new UserDetails();
		savedUserDetails.setUserId("123");
		savedUserDetails.setConsentGivenDtimes(LocalDateTime.now());
		when(userDetailsRepository.save(any())).thenReturn(savedUserDetails);
		userManagementServiceImpl.saveUserConsent();
	}

	@Test
	public void updateNotificationsSeenTimestamp_whenSaveThrows_setsGenericError() {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "PARTNER_ADMIN");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(true);

		UserDetails userDetails = new UserDetails();
		when(userDetailsRepository.findByUserId(anyString())).thenReturn(Optional.of(userDetails));
		when(userDetailsRepository.save(any())).thenThrow(new RuntimeException("save failed"));

		NotificationsSeenRequestDto requestDto = new NotificationsSeenRequestDto();
		requestDto.setNotificationsSeenDtimes(LocalDateTime.now());

		ResponseWrapperV2<NotificationsSeenResponseDto> response =
				userManagementServiceImpl.updateNotificationsSeenTimestamp("123", requestDto);

		assertNotNull(response);
		assertTrue(response.getErrors() != null && !response.getErrors().isEmpty());
		assertEquals(ErrorCode.UPDATE_NOTIFICATIONS_SEEN_TIME_ERROR.getErrorCode(),
				response.getErrors().get(0).getErrorCode());
	}

	@Test
	public void getNotificationsSeenTimestamp_whenRepositoryThrowsAfterAccess_setsGenericError() {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "PARTNER_ADMIN");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(true);
		when(userDetailsRepository.findByUserId(anyString())).thenThrow(new RuntimeException("db failure"));

		ResponseWrapperV2<NotificationsSeenResponseDto> response =
				userManagementServiceImpl.getNotificationsSeenTimestamp("any-user");

		assertNotNull(response);
		assertTrue(response.getErrors() != null && !response.getErrors().isEmpty());
		assertEquals(ErrorCode.GET_NOTIFICATIONS_SEEN_TIME_ERROR.getErrorCode(),
				response.getErrors().get(0).getErrorCode());
	}

	@Test
	public void testIsUserConsentGivenPartnerServiceException() {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(false);
		when(partnerHelper.isPolicyManager(anyString())).thenReturn(false);
		when(partnerRepository.findByUserId(anyString())).thenReturn(new ArrayList<>());
		userManagementServiceImpl.isUserConsentGiven();
	}

	@Test
	public void testIsUserConsentGivenWithNonAdminNonPolicyManager() {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "ROLE_PARTNER");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(false);
		when(partnerHelper.isPolicyManager(anyString())).thenReturn(false);

		List<Partner> partnerList = new ArrayList<>();
		partnerList.add(new Partner());
		when(partnerRepository.findByUserId(anyString())).thenReturn(partnerList);

		UserDetails userDetails = new UserDetails();
		userDetails.setConsentGiven("YES");
		userDetails.setConsentGivenDtimes(LocalDateTime.now());
		when(userDetailsRepository.findByUserId(anyString())).thenReturn(Optional.of(userDetails));
		userManagementServiceImpl.isUserConsentGiven();
	}

	@Test
	public void isUserConsentGiven_whenConsentGivenNull_setsGenericError() {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "PARTNER_ADMIN");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(true);

		UserDetails userDetails = new UserDetails();
		userDetails.setUserId("123");
		userDetails.setConsentGiven(null);
		when(userDetailsRepository.findByUserId(anyString())).thenReturn(Optional.of(userDetails));

		ResponseWrapperV2<UserDetailsDto> response = userManagementServiceImpl.isUserConsentGiven();

		assertNotNull(response);
		assertTrue(response.getErrors() != null && !response.getErrors().isEmpty());
		assertEquals(ErrorCode.PMS_CONSENT_ERR.getErrorCode(), response.getErrors().get(0).getErrorCode());
	}

	@Test
	public void updateNotificationsSeenTimestamp_whenRequestNull_setsGenericError() {
		AuthUserDetails authUserDetails = mockAuthUserDetails("123", "PARTNER_ADMIN");
		SecurityContextHolder.setContext(securityContext);
		when(authentication.getPrincipal()).thenReturn(authUserDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(partnerHelper.isOwnershipFilterExempt()).thenReturn(true);

		UserDetails userDetails = new UserDetails();
		when(userDetailsRepository.findByUserId(anyString())).thenReturn(Optional.of(userDetails));

		ResponseWrapperV2<NotificationsSeenResponseDto> response =
				userManagementServiceImpl.updateNotificationsSeenTimestamp("123", null);

		assertNotNull(response);
		assertTrue(response.getErrors() != null && !response.getErrors().isEmpty());
		assertEquals(ErrorCode.UPDATE_NOTIFICATIONS_SEEN_TIME_ERROR.getErrorCode(),
				response.getErrors().get(0).getErrorCode());
	}

	private io.mosip.kernel.openid.bridge.model.MosipUserDto getMosipUserDto() {
		io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = new io.mosip.kernel.openid.bridge.model.MosipUserDto();
		mosipUserDto.setUserId("123");
		mosipUserDto.setMail("abc@gmail.com");
		return mosipUserDto;
	}
}
