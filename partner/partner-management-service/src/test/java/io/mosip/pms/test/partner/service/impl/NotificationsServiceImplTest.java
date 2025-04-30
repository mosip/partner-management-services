package io.mosip.pms.test.partner.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.openid.bridge.model.AuthUserDetails;
import io.mosip.kernel.openid.bridge.model.MosipUserDto;
import io.mosip.pms.common.constant.PartnerConstants;
import io.mosip.pms.common.dto.DismissNotificationRequestDto;
import io.mosip.pms.common.dto.DismissNotificationResponseDto;
import io.mosip.pms.common.dto.NotificationsResponseDto;
import io.mosip.pms.common.entity.ApiKeyRequestsSummaryEntity;
import io.mosip.pms.common.entity.NotificationEntity;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.entity.UserDetails;
import io.mosip.pms.common.repository.NotificationServiceRepository;
import io.mosip.pms.common.repository.NotificationsSummaryRepository;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.partner.dto.NotificationsFilterDto;
import io.mosip.pms.partner.service.impl.NotificationsServiceImpl;
import io.mosip.pms.partner.util.PartnerHelper;
import io.mosip.pms.test.config.TestSecurityConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
public class NotificationsServiceImplTest {

    @InjectMocks
    private NotificationsServiceImpl notificationsServiceImpl;

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
    public void getNotificationsTest() throws Exception {
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
        partner.setPartnerTypeCode("Auth_Partner");
        partner.setIsActive(true);
        partnerList.add(partner);
        when(partnerServiceRepository.findByUserId(anyString())).thenReturn(partnerList);

        int pageNo = 0;
        int pageSize = 4;

        NotificationsFilterDto filterDto = new NotificationsFilterDto();

        notificationsServiceImpl.getNotifications(pageNo, pageSize, filterDto);
    }

    @Test
    public void dismissNotificationTest() throws Exception {
        DismissNotificationRequestDto requestDto = new DismissNotificationRequestDto();
        requestDto.setNotificationStatus("DISMISSED");

        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setId("12345");
        notificationEntity.setNotificationType("PARTNER_CERT_EXPIRY");
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
        responseDto.setNotificationType("PARTNER_CERT_EXPIRY");
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
        responseDto.setNotificationType("PARTNER_CERT_EXPIRY");
        responseDto.setNotificationStatus("DISMISSED");
        when(mapper.convertValue(entity, DismissNotificationResponseDto.class)).thenReturn(responseDto);
        notificationsServiceImpl.dismissNotification("12345", requestDto);

        notificationEntity.setNotificationType("PARTNER_CERT_EXPIRY");
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

    private io.mosip.kernel.openid.bridge.model.MosipUserDto getMosipUserDto() {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = new io.mosip.kernel.openid.bridge.model.MosipUserDto();
        mosipUserDto.setUserId("123");
        mosipUserDto.setMail("abc@gmail.com");
        return mosipUserDto;
    }
}
