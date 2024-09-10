package io.mosip.pms.test.partner.service.impl;

import io.mosip.kernel.openid.bridge.model.AuthUserDetails;
import io.mosip.pms.common.entity.DeviceDetailSBI;
import io.mosip.pms.common.repository.DeviceDetailSbiRepository;
import io.mosip.pms.device.authdevice.entity.DeviceDetail;
import io.mosip.pms.device.authdevice.entity.SecureBiometricInterface;
import io.mosip.pms.device.authdevice.repository.DeviceDetailRepository;
import io.mosip.pms.device.authdevice.repository.SecureBiometricInterfaceRepository;
import io.mosip.pms.partner.exception.PartnerServiceException;
import io.mosip.pms.partner.request.dto.SbiAndDeviceMappingRequestDto;
import io.mosip.pms.partner.service.impl.MultiPartnerAdminServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MultiPartnerAdminServiceImplTest {

    @MockBean
    SecureBiometricInterfaceRepository secureBiometricInterfaceRepository;

    @MockBean
    DeviceDetailSbiRepository deviceDetailSbiRepository;

    @MockBean
    DeviceDetailRepository deviceDetailRepository;

    @Mock
    Authentication authentication;

    @Mock
    SecurityContext securityContext;

    @Autowired
    MultiPartnerAdminServiceImpl multiPartnerAdminServiceImpl;


    private io.mosip.kernel.openid.bridge.model.MosipUserDto getMosipUserDto() {
        io.mosip.kernel.openid.bridge.model.MosipUserDto mosipUserDto = new io.mosip.kernel.openid.bridge.model.MosipUserDto();
        mosipUserDto.setUserId("123");
        mosipUserDto.setMail("abc@gmail.com");
        return mosipUserDto;
    }

    @Test
    public void approveOrRejectDeviceWithSbiMappingTest() throws Exception {
        SbiAndDeviceMappingRequestDto requestDto = new SbiAndDeviceMappingRequestDto();
        requestDto.setPartnerId("123");
        requestDto.setSbiId("112");
        requestDto.setDeviceDetailId("dgdg");

        DeviceDetailSBI deviceDetailSBI = new DeviceDetailSBI();
        when(deviceDetailSbiRepository.findByDeviceProviderIdAndSbiIdAndDeviceDetailId(anyString(), anyString(), anyString())).thenReturn(deviceDetailSBI);

        DeviceDetail deviceDetail = new DeviceDetail();
        deviceDetail.setDeviceProviderId("123");
        deviceDetail.setApprovalStatus("pending_approval");
        when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(anyString())).thenReturn(deviceDetail);

        SecureBiometricInterface secureBiometricInterface = new SecureBiometricInterface();
        secureBiometricInterface.setSwCreateDateTime(LocalDateTime.now());
        secureBiometricInterface.setSwExpiryDateTime(LocalDateTime.now());
        secureBiometricInterface.setApprovalStatus("approved");
        secureBiometricInterface.setActive(true);
        secureBiometricInterface.setCrDtimes(LocalDateTime.now());
        secureBiometricInterface.setSwVersion("1.0");
        secureBiometricInterface.setProviderId("123");
        when(secureBiometricInterfaceRepository.findById(anyString())).thenReturn(Optional.of(secureBiometricInterface));

        when(deviceDetailRepository.findById(anyString())).thenReturn(Optional.of(deviceDetail));
        deviceDetailSBI.setProviderId("123");
        when(deviceDetailSbiRepository.save(any())).thenReturn(deviceDetailSBI);

        multiPartnerAdminServiceImpl.approveOrRejectDeviceWithSbiMapping(requestDto, false);
    }

    @Test
    public void approveOrRejectDeviceWithSbiMappingTest2() throws Exception {
        SbiAndDeviceMappingRequestDto requestDto = new SbiAndDeviceMappingRequestDto();
        requestDto.setPartnerId("123");
        requestDto.setSbiId("112");
        requestDto.setDeviceDetailId("dgdg");

        DeviceDetailSBI deviceDetailSBI = new DeviceDetailSBI();
        when(deviceDetailSbiRepository.findByDeviceProviderIdAndSbiIdAndDeviceDetailId(anyString(), anyString(), anyString())).thenReturn(deviceDetailSBI);

        DeviceDetail deviceDetail = new DeviceDetail();
        deviceDetail.setDeviceProviderId("123");
        deviceDetail.setApprovalStatus("pending_approval");
        when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(anyString())).thenReturn(deviceDetail);

        SecureBiometricInterface secureBiometricInterface = new SecureBiometricInterface();
        secureBiometricInterface.setSwCreateDateTime(LocalDateTime.now());
        secureBiometricInterface.setSwExpiryDateTime(LocalDateTime.now());
        secureBiometricInterface.setApprovalStatus("approved");
        secureBiometricInterface.setActive(true);
        secureBiometricInterface.setCrDtimes(LocalDateTime.now());
        secureBiometricInterface.setSwVersion("1.0");
        secureBiometricInterface.setProviderId("123");
        when(secureBiometricInterfaceRepository.findById(anyString())).thenReturn(Optional.of(secureBiometricInterface));

        when(deviceDetailRepository.findById(anyString())).thenReturn(Optional.of(deviceDetail));
        deviceDetailSBI.setProviderId("123");
        when(deviceDetailSbiRepository.save(any())).thenReturn(deviceDetailSBI);

        multiPartnerAdminServiceImpl.approveOrRejectDeviceWithSbiMapping(requestDto, true);
    }

    @Test
    public void approveDeviceWithSbiMappingException() {

        SbiAndDeviceMappingRequestDto requestDto = new SbiAndDeviceMappingRequestDto();
        requestDto.setPartnerId("123");
        requestDto.setSbiId("112");

        multiPartnerAdminServiceImpl.approveOrRejectDeviceWithSbiMapping(requestDto, false);

        requestDto.setDeviceDetailId("dgdg");

        SecureBiometricInterface secureBiometricInterface = new SecureBiometricInterface();
        secureBiometricInterface.setSwCreateDateTime(LocalDateTime.now());
        secureBiometricInterface.setSwExpiryDateTime(LocalDateTime.now());
        secureBiometricInterface.setApprovalStatus("approved");
        secureBiometricInterface.setActive(true);
        secureBiometricInterface.setCrDtimes(LocalDateTime.now());
        secureBiometricInterface.setSwVersion("1.0");
        secureBiometricInterface.setProviderId("123");

        multiPartnerAdminServiceImpl.approveOrRejectDeviceWithSbiMapping(requestDto, false);
        when(secureBiometricInterfaceRepository.findById(anyString())).thenReturn(Optional.of(secureBiometricInterface));
        DeviceDetail deviceDetail = new DeviceDetail();
        deviceDetail.setDeviceProviderId("123");

        multiPartnerAdminServiceImpl.approveOrRejectDeviceWithSbiMapping(requestDto, false);
        when(deviceDetailRepository.findById(anyString())).thenReturn(Optional.of(deviceDetail));

        when(deviceDetailSbiRepository.findByDeviceProviderIdAndSbiIdAndDeviceDetailId(anyString(), anyString(), anyString())).thenReturn(null);
        multiPartnerAdminServiceImpl.approveOrRejectDeviceWithSbiMapping(requestDto, false);

        DeviceDetailSBI deviceDetailSBI = new DeviceDetailSBI();
        deviceDetailSBI.setProviderId("123");
        when(deviceDetailSbiRepository.findByDeviceProviderIdAndSbiIdAndDeviceDetailId(anyString(), anyString(), anyString())).thenReturn(deviceDetailSBI);

        multiPartnerAdminServiceImpl.approveOrRejectDeviceWithSbiMapping(requestDto, false);
    }
}
