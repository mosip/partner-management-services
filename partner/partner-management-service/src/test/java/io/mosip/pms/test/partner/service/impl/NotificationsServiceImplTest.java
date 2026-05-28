package io.mosip.pms.test.partner.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.openid.bridge.model.AuthUserDetails;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.dto.DismissNotificationRequestDto;
import io.mosip.pms.common.dto.DismissNotificationResponseDto;
import io.mosip.pms.common.dto.NotificationDetailsDto;
import io.mosip.pms.common.dto.NotificationsResponseDto;
import io.mosip.pms.common.entity.NotificationEntity;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.repository.NotificationServiceRepository;
import io.mosip.pms.common.repository.NotificationsSummaryRepository;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.partner.dto.NotificationsFilterDto;
import io.mosip.pms.partner.service.impl.NotificationsServiceImpl;
import io.mosip.pms.partner.util.PartnerHelper;
import io.mosip.pms.user.service.UserManagementService;
import io.mosip.pms.test.config.TestSecurityConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
public class NotificationsServiceImplTest {

    @Spy
    @InjectMocks
    private NotificationsServiceImpl notificationsServiceImpl;

    @MockBean
    private UserManagementService userManagementService;

    @Mock
    NotificationServiceRepository notificationServiceRepository;

    @Mock
    NotificationsSummaryRepository notificationsSummaryRepository;

    @Mock
    PartnerServiceRepository partnerServiceRepository;

    @Mock
    Authentication authentication;

    @Mock
    SecurityContext securityContext;

    @Mock
    private ObjectMapper mapper;

    @Mock
    PartnerHelper partnerHelper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getNotificationsTest1() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        Collection<GrantedAuthority> newAuthorities = List.of(
                new SimpleGrantedAuthority("PARTNER_ADMIN")
        );
        Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
        addAuthoritiesMethod.setAccessible(true);
        addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        partner.setId("123");
        partner.setPartnerTypeCode("Auth_Partner");
        partner.setIsActive(true);
        partnerList.add(partner);
        when(partnerServiceRepository.findByUserId(anyString())).thenReturn(partnerList);

        Integer pageNo = 0;
        Integer pageSize = 4;
        List<String> partnerIdList = new ArrayList<>();
        partnerIdList.add("123");
        when(partnerHelper.isPartnerAdmin(anyString())).thenReturn(true);

        NotificationsFilterDto filterDto = new NotificationsFilterDto();
        filterDto.setNotificationType("ROOT_CERT_EXPIRY");
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        NotificationEntity entity = new NotificationEntity();
        entity.setId("12345");
        entity.setNotificationDetailsJson("{\"abc\":\"test\"}");
        Page<NotificationEntity> page =new PageImpl<>(List.of(entity), pageable, 1);
        doReturn(page).when(notificationsServiceImpl).fetchNotifications(filterDto, pageable, partnerIdList, true);
        when(notificationsSummaryRepository.getSummaryOfAllRootIntermediatePartnerCertNotifications(anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString(), any(), any())).thenReturn(page);
        notificationsServiceImpl.getNotifications(pageNo, pageSize, filterDto);

        filterDto.setNotificationType("INTERMEDIATE_CERT_EXPIRY");
        doReturn(page).when(notificationsServiceImpl).fetchNotifications(filterDto, pageable, partnerIdList, true);
        notificationsServiceImpl.getNotifications(pageNo, pageSize, filterDto);

        filterDto.setNotificationType("WEEKLY_SUMMARY");
        doReturn(page).when(notificationsServiceImpl).fetchNotifications(filterDto, pageable, partnerIdList, true);
        when(notificationsSummaryRepository.getSummaryOfWeeklyNotifications(anyString(), anyString(),
                anyString(), anyString(), any(), any())).thenReturn(page);
        notificationsServiceImpl.getNotifications(pageNo, pageSize, filterDto);

        filterDto.setExpiryDate("2025-04-07");
        notificationsServiceImpl.getNotifications(pageNo, pageSize, filterDto);

        filterDto.setCreatedFromDate("2025-04-07");
        filterDto.setCreatedToDate("2025-04-07T13:08:37");
        notificationsServiceImpl.getNotifications(pageNo, pageSize, filterDto);

        filterDto.setNotificationType("PARTNER_CERT_EXPIRY");
        filterDto.setCreatedToDate(null);
        notificationsServiceImpl.getNotifications(pageNo, pageSize, filterDto);

        filterDto.setNotificationType(null);
        notificationsServiceImpl.getNotifications(pageNo, pageSize, filterDto);
    }

    @Test
    public void getNotificationsTest2() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        Collection<GrantedAuthority> newAuthorities = List.of(
                new SimpleGrantedAuthority("AUTH_PARTNER")
        );
        Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
        addAuthoritiesMethod.setAccessible(true);
        addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        partner.setId("123");
        partner.setPartnerTypeCode("Auth_Partner");
        partner.setIsActive(true);
        partnerList.add(partner);
        when(partnerServiceRepository.findByUserId(anyString())).thenReturn(partnerList);

        Integer pageNo = 0;
        Integer pageSize = 4;
        List<String> partnerIdList = new ArrayList<>();
        partnerIdList.add("123");

        NotificationsFilterDto filterDto = new NotificationsFilterDto();
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        NotificationEntity entity = new NotificationEntity();
        entity.setId("12345");
        entity.setNotificationDetailsJson("{\"abc\":\"test\"}");
        Page<NotificationEntity> page =new PageImpl<>(List.of(entity), pageable, 1);
        doReturn(page).when(notificationsServiceImpl).fetchNotifications(filterDto, pageable, partnerIdList, false);
        when(mapper.readValue(anyString(), eq(NotificationDetailsDto.class)))
                .thenThrow(new JsonProcessingException("test") {});
        when(notificationsSummaryRepository.getSummaryOfAllNotifications(anyString(), any(), any(), any())).thenReturn(page);
        notificationsServiceImpl.getNotifications(pageNo, pageSize, filterDto);

        doReturn(page).when(notificationsServiceImpl).fetchNotifications(filterDto, pageable, partnerIdList, true);
        when(notificationsSummaryRepository.getSummaryOfAllNotifications(anyString(), any(), any(), any())).thenReturn(page);
        notificationsServiceImpl.getNotifications(pageNo, pageSize, filterDto);

        filterDto.setNotificationType("PARTNER_CERT_EXPIRY");
        doReturn(page).when(notificationsServiceImpl).fetchNotifications(filterDto, pageable, partnerIdList, false);
        when(notificationsSummaryRepository.getSummaryOfAllRootIntermediatePartnerCertNotifications(anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString(), any(), any())).thenReturn(page);
        notificationsServiceImpl.getNotifications(pageNo, pageSize, filterDto);

        filterDto.setNotificationType("abc");
        doReturn(Page.empty()).when(notificationsServiceImpl).fetchNotifications(filterDto, pageable, partnerIdList, false);
        notificationsServiceImpl.getNotifications(pageNo, pageSize, filterDto);

        filterDto.setNotificationType("PARTNER_CERT_EXPIRY");
        filterDto.setApiKeyName("123");
        doReturn(Page.empty()).when(notificationsServiceImpl).fetchNotifications(filterDto, pageable, partnerIdList, false);
        notificationsServiceImpl.getNotifications(pageNo, pageSize, filterDto);

        filterDto.setNotificationType("ROOT_CERT_EXPIRY");
        doReturn(Page.empty()).when(notificationsServiceImpl).fetchNotifications(filterDto, pageable, partnerIdList, false);
        notificationsServiceImpl.getNotifications(pageNo, pageSize, filterDto);
    }

    @Test
    public void getNotificationsTest3() throws Exception {
        NotificationsFilterDto filterDto = new NotificationsFilterDto();
        notificationsServiceImpl.getNotifications(null, 4, filterDto);

        notificationsServiceImpl.getNotifications(-3, 4, filterDto);

        notificationsServiceImpl.getNotifications(0, -1, filterDto);

        notificationsServiceImpl.getNotifications(0, 4, null);
    }

    @Test
    public void getNotificationsTest4() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        Collection<GrantedAuthority> newAuthorities = List.of(
                new SimpleGrantedAuthority("AUTH_PARTNER")
        );
        Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
        addAuthoritiesMethod.setAccessible(true);
        addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        when(partnerServiceRepository.findByUserId(anyString())).thenReturn(partnerList);

        Integer pageNo = 0;
        Integer pageSize = 4;

        NotificationsFilterDto filterDto = new NotificationsFilterDto();
        notificationsServiceImpl.getNotifications(pageNo, pageSize, filterDto);
    }

    @Test
    public void getNotificationsTest5() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        Collection<GrantedAuthority> newAuthorities = List.of(
                new SimpleGrantedAuthority("DEVICE_PROVIDER")
        );
        Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
        addAuthoritiesMethod.setAccessible(true);
        addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        partner.setId("123");
        partner.setPartnerTypeCode("Device_Provider");
        partner.setIsActive(true);
        partnerList.add(partner);
        when(partnerServiceRepository.findByUserId(anyString())).thenReturn(partnerList);

        Integer pageNo = 0;
        Integer pageSize = 4;
        List<String> partnerIdList = new ArrayList<>();
        partnerIdList.add("123");

        NotificationsFilterDto filterDto = new NotificationsFilterDto();
        filterDto.setNotificationType("SBI_EXPIRY");
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        NotificationEntity entity = new NotificationEntity();
        entity.setId("12345");
        entity.setNotificationDetailsJson("{\"abc\":\"test\"}");
        Page<NotificationEntity> page =new PageImpl<>(List.of(entity), pageable, 1);
        doReturn(page).when(notificationsServiceImpl).fetchNotifications(filterDto, pageable, partnerIdList, false);
        when(notificationsSummaryRepository.getSummaryOfAllSbiNotifications(anyString(), anyString(), anyString(), anyString(), any(), any(), any())).thenReturn(page);
        notificationsServiceImpl.getNotifications(pageNo, pageSize, filterDto);

        filterDto.setNotificationType("PARTNER_CERT_EXPIRY");
        filterDto.setSbiVersion("123");
        doReturn(page).when(notificationsServiceImpl).fetchNotifications(filterDto, pageable, partnerIdList, false);
        notificationsServiceImpl.getNotifications(pageNo, pageSize, filterDto);

        filterDto.setNotificationType("API_KEY_EXPIRY");
        filterDto.setCertificateId("123");
        doReturn(page).when(notificationsServiceImpl).fetchNotifications(filterDto, pageable, partnerIdList, false);
        notificationsServiceImpl.getNotifications(pageNo, pageSize, filterDto);

        filterDto = new NotificationsFilterDto();
        doReturn(page).when(notificationsServiceImpl).fetchNotifications(filterDto, pageable, partnerIdList, false);
        notificationsServiceImpl.getNotifications(pageNo, pageSize, filterDto);
    }

    @Test
    public void getNotificationsTest6() throws Exception {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        Collection<GrantedAuthority> newAuthorities = List.of(
                new SimpleGrantedAuthority("FTM_PROVIDER")
        );
        Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
        addAuthoritiesMethod.setAccessible(true);
        addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        partner.setId("123");
        partner.setPartnerTypeCode("FTM_Provider");
        partner.setIsActive(true);
        partnerList.add(partner);
        when(partnerServiceRepository.findByUserId(anyString())).thenReturn(partnerList);

        Integer pageNo = 0;
        Integer pageSize = 4;
        List<String> partnerIdList = new ArrayList<>();
        partnerIdList.add("123");

        NotificationsFilterDto filterDto = new NotificationsFilterDto();
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        NotificationEntity entity = new NotificationEntity();
        entity.setId("12345");
        entity.setNotificationDetailsJson("{\"abc\":\"test\"}");
        Page<NotificationEntity> page =new PageImpl<>(List.of(entity), pageable, 1);
        doReturn(page).when(notificationsServiceImpl).fetchNotifications(filterDto, pageable, partnerIdList, false);
        when(notificationsSummaryRepository.getSummaryOfAllFtmChipCertNotifications(anyString(), anyString(), anyString(), anyString(), anyString(), any(), any(), any())).thenReturn(page);
        notificationsServiceImpl.getNotifications(pageNo, pageSize, filterDto);

        filterDto.setNotificationType("FTM_CHIP_CERT_EXPIRY");
        doReturn(page).when(notificationsServiceImpl).fetchNotifications(filterDto, pageable, partnerIdList, false);
        notificationsServiceImpl.getNotifications(pageNo, pageSize, filterDto);

        filterDto.setNotificationType("PARTNER_CERT_EXPIRY");
        filterDto.setFtmId("123");
        doReturn(page).when(notificationsServiceImpl).fetchNotifications(filterDto, pageable, partnerIdList, false);
        notificationsServiceImpl.getNotifications(pageNo, pageSize, filterDto);
    }

    @Test
    public void dismissNotificationTest() throws Exception {
        DismissNotificationRequestDto requestDto = new DismissNotificationRequestDto();
        requestDto.setNotificationStatus("DISMISSED");

        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setId("12345");
        notificationEntity.setNotificationType("PARTNER_CERT_EXPIRY_NOTIFICATION_TYPE");
        notificationEntity.setPartnerId("123");
        notificationEntity.setEmailId("abc@gmail.com");
        notificationEntity.setEmailLangCode("eng");
        notificationEntity.setEmailSent(true);
        notificationEntity.setNotificationDetailsJson("details");
        Optional<NotificationEntity> optionalEntity = Optional.of(notificationEntity);
        when(notificationServiceRepository.findById(anyString())).thenReturn(optionalEntity);

        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        Collection<GrantedAuthority> newAuthorities = List.of(
                new SimpleGrantedAuthority("Auth_Partner")
        );
        Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
        addAuthoritiesMethod.setAccessible(true);
        addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        partner.setId("123");
        partner.setPolicyGroupId("policyGroup123");
        partner.setPartnerTypeCode("Auth_Partner");
        partner.setName("abc");
        partner.setIsActive(true);
        partnerList.add(partner);
        when(partnerServiceRepository.findByUserId(anyString())).thenReturn(partnerList);

        NotificationEntity entity = new NotificationEntity();
        entity.setId("12345");
        entity.setNotificationStatus(PartnerConstants.STATUS_DISMISSED);
        entity.setUpdatedDatetime(LocalDateTime.now());
        entity.setUpdatedBy("123");
        when(notificationServiceRepository.save(any())).thenReturn(entity);

        DismissNotificationResponseDto responseDto = new DismissNotificationResponseDto();
        responseDto.setId("12345");
        responseDto.setNotificationType("PARTNER_CERT_EXPIRY_NOTIFICATION_TYPE");
        responseDto.setNotificationStatus("DISMISSED");
        when(mapper.convertValue(entity, DismissNotificationResponseDto.class)).thenReturn(responseDto);
        notificationsServiceImpl.dismissNotification("12345", requestDto);

        partnerList = new ArrayList<>();
        when(partnerServiceRepository.findByUserId(anyString())).thenReturn(partnerList);
        notificationsServiceImpl.dismissNotification("12345", requestDto);

        notificationsServiceImpl.dismissNotification(null, requestDto);

        requestDto = new DismissNotificationRequestDto();
        requestDto.setNotificationStatus("active");
        notificationsServiceImpl.dismissNotification("12345", requestDto);

        requestDto = new DismissNotificationRequestDto();
        notificationsServiceImpl.dismissNotification("12345", requestDto);

    }

    @Test
    public void dismissNotificationExceptionTest1() throws Exception {
        DismissNotificationRequestDto requestDto = new DismissNotificationRequestDto();
        requestDto.setNotificationStatus("DISMISSED");
        notificationsServiceImpl.dismissNotification("12345", requestDto);

        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setId("12345");
        notificationEntity.setNotificationType("ROOT_CERT_EXPIRY");
        notificationEntity.setPartnerId("123");
        notificationEntity.setEmailId("abc@gmail.com");
        notificationEntity.setEmailLangCode("eng");
        notificationEntity.setEmailSent(true);
        notificationEntity.setNotificationDetailsJson("details");
        Optional<NotificationEntity> optionalEntity = Optional.of(notificationEntity);
        when(notificationServiceRepository.findById(anyString())).thenReturn(optionalEntity);

        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        Collection<GrantedAuthority> newAuthorities = List.of(
                new SimpleGrantedAuthority("Auth_Partner")
        );
        Method addAuthoritiesMethod = AuthUserDetails.class.getDeclaredMethod("addAuthorities", Collection.class, String.class);
        addAuthoritiesMethod.setAccessible(true);
        addAuthoritiesMethod.invoke(authUserDetails, newAuthorities, null);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        notificationsServiceImpl.dismissNotification("12345", requestDto);

        List<Partner> partnerList = new ArrayList<>();
        Partner partner = new Partner();
        partner.setId("123");
        partner.setPolicyGroupId("policyGroup123");
        partner.setPartnerTypeCode("Auth_Partner");
        partner.setName("abc");
        partner.setIsActive(true);
        partnerList.add(partner);
        when(partnerServiceRepository.findByUserId(anyString())).thenReturn(partnerList);

        NotificationEntity entity = new NotificationEntity();
        entity.setId("12345");
        entity.setNotificationStatus(PartnerConstants.STATUS_DISMISSED);
        entity.setUpdatedDatetime(LocalDateTime.now());
        entity.setUpdatedBy("123");
        when(notificationServiceRepository.save(any())).thenReturn(entity);

        DismissNotificationResponseDto responseDto = new DismissNotificationResponseDto();
        responseDto.setId("12345");
        responseDto.setNotificationType("PARTNER_CERT_EXPIRY_NOTIFICATION_TYPE");
        responseDto.setNotificationStatus("DISMISSED");
        when(mapper.convertValue(entity, DismissNotificationResponseDto.class)).thenReturn(responseDto);
        notificationsServiceImpl.dismissNotification("12345", requestDto);

        notificationEntity.setNotificationType("PARTNER_CERT_EXPIRY_NOTIFICATION_TYPE");
        notificationEntity.setNotificationStatus("DISMISSED");
        when(notificationServiceRepository.findById(anyString())).thenReturn(optionalEntity);
        notificationsServiceImpl.dismissNotification("12345", requestDto);

        partnerList = new ArrayList<>();
        Partner partner1 = new Partner();
        partner1.setId("123");
        partnerList.add(partner1);
        when(partnerServiceRepository.findByUserId(anyString())).thenReturn(partnerList);
        notificationsServiceImpl.dismissNotification("12345", requestDto);


        partnerList = new ArrayList<>();
        partner.setId("xxx");
        partnerList.add(partner);
        when(partnerServiceRepository.findByUserId(anyString())).thenReturn(partnerList);
        notificationsServiceImpl.dismissNotification("12345", requestDto);
    }

    @Test
    public void dismissNotificationExceptionTest2() {
        notificationsServiceImpl.dismissNotification("12345", null);
    }

    @Test
    public void mapToResponseDto_returnsNotificationTypeFromEntity() {
        NotificationEntity entity = new NotificationEntity();
        entity.setId("notification-1");
        entity.setPartnerId("partner-1");
        entity.setNotificationType("ROOT_CERT_EXPIRY");
        entity.setNotificationStatus(PartnerConstants.STATUS_ACTIVE);
        entity.setCreatedDatetime(LocalDateTime.now());

        NotificationsResponseDto responseDto = notificationsServiceImpl.mapToResponseDto(entity);

        assertEquals("notification-1", responseDto.getNotificationId());
        assertEquals("ROOT_CERT_EXPIRY", responseDto.getNotificationType());
        assertEquals(PartnerConstants.STATUS_ACTIVE, responseDto.getNotificationStatus());
        assertNull(responseDto.getNotificationDetails());
    }

    private io.mosip.kernel.openid.bridge.model.MosipUserDto getMosipUserDto() {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = new io.mosip.kernel.openid.bridge.model.MosipUserDto();
        mosipUserDto.setUserId("123");
        mosipUserDto.setMail("abc@gmail.com");
        return mosipUserDto;
    }
}
