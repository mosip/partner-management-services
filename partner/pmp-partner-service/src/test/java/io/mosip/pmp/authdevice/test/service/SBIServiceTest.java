package io.mosip.pmp.authdevice.test.service;

import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import io.mosip.pmp.authdevice.dto.SecureBiometricInterfaceCreateDto;
import io.mosip.pmp.authdevice.dto.SecureBiometricInterfaceUpdateDto;
import io.mosip.pmp.authdevice.entity.DeviceDetail;
import io.mosip.pmp.authdevice.entity.SecureBiometricInterface;
import io.mosip.pmp.authdevice.entity.SecureBiometricInterfaceHistory;
import io.mosip.pmp.authdevice.exception.RequestException;
import io.mosip.pmp.authdevice.repository.DeviceDetailRepository;
import io.mosip.pmp.authdevice.repository.SecureBiometricInterfaceHistoryRepository;
import io.mosip.pmp.authdevice.repository.SecureBiometricInterfaceRepository;
import io.mosip.pmp.authdevice.service.SecureBiometricInterfaceService;
import io.mosip.pmp.authdevice.service.impl.SecureBiometricInterfaceServiceImpl;
import io.mosip.pmp.authdevice.util.AuditUtil;
import io.mosip.pmp.partner.PartnerserviceApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { PartnerserviceApplication.class })
@AutoConfigureMockMvc
@EnableWebMvc
public class SBIServiceTest {
	@InjectMocks
	SecureBiometricInterfaceService secureBiometricInterfaceService=new SecureBiometricInterfaceServiceImpl();
	@Mock
	DeviceDetailRepository deviceDetailRepository;
	@Mock
	AuditUtil auditUtil;
	@Mock
	SecureBiometricInterfaceRepository sbiRepository;
	@Mock
	SecureBiometricInterfaceHistoryRepository sbiHistoryRepository;
	
	DeviceDetail deviceDetail=new DeviceDetail();
	SecureBiometricInterfaceCreateDto sbicreatedto = new SecureBiometricInterfaceCreateDto();
	SecureBiometricInterfaceUpdateDto sbidto = new SecureBiometricInterfaceUpdateDto();
	SecureBiometricInterface secureBiometricInterface=new SecureBiometricInterface();
	SecureBiometricInterfaceHistory secureBiometricInterfaceHistory=new SecureBiometricInterfaceHistory();
	@Before
	public void setup() {
		secureBiometricInterfaceHistory.setApprovalStatus("pending");
		secureBiometricInterfaceHistory.setDeviceDetailId("1234");
		secureBiometricInterfaceHistory.setSwBinaryHAsh("swb".getBytes());
		secureBiometricInterfaceHistory.setEffectDateTime(LocalDateTime.now());
		secureBiometricInterfaceHistory.setSwCreateDateTime(LocalDateTime.now());
		secureBiometricInterfaceHistory.setSwExpiryDateTime(LocalDateTime.now());
		secureBiometricInterfaceHistory.setActive(true);
		secureBiometricInterfaceHistory.setCrBy("110005");
		secureBiometricInterfaceHistory.setCrDtimes(LocalDateTime.now());
		secureBiometricInterfaceHistory.setUpdDtimes(LocalDateTime.now());
		secureBiometricInterfaceHistory.setUpdBy("110005");
		secureBiometricInterfaceHistory.setSwVersion("v1");
		secureBiometricInterfaceHistory.setId("1234");
		secureBiometricInterface.setApprovalStatus("pending");
		secureBiometricInterface.setDeviceDetailId("1234");
		secureBiometricInterface.setSwBinaryHash("swb".getBytes());
		secureBiometricInterface.setSwCreateDateTime(LocalDateTime.now());
		secureBiometricInterface.setSwExpiryDateTime(LocalDateTime.now());
		secureBiometricInterface.setActive(true);
		secureBiometricInterface.setCrBy("110005");
		secureBiometricInterface.setCrDtimes(LocalDateTime.now());
		secureBiometricInterface.setUpdDtimes(LocalDateTime.now());
		secureBiometricInterface.setUpdBy("110005");
		secureBiometricInterface.setSwVersion("v1");
		secureBiometricInterface.setId("1234");
		
		sbidto.setDeviceDetailId("1234");
		sbidto.setSwBinaryHash("swb");
		sbidto.setSwCreateDateTime(LocalDateTime.now());
		sbidto.setSwExpiryDateTime(LocalDateTime.now());
		sbidto.setIsActive(true);
		sbidto.setIsItForRegistrationDevice(false);
		sbidto.setSwVersion("v1");
		sbidto.setId("1234");
		
		sbicreatedto.setDeviceDetailId("1234");
		sbicreatedto.setSwBinaryHash("swb");
		sbicreatedto.setSwCreateDateTime(LocalDateTime.now());
		sbicreatedto.setSwExpiryDateTime(LocalDateTime.now());
		
		sbicreatedto.setIsItForRegistrationDevice(false);
		sbicreatedto.setSwVersion("v1");
    	deviceDetail.setApprovalStatus("pending");
    	deviceDetail.setDeviceProviderId("1234");
    	deviceDetail.setDeviceSubTypeCode("123");
    	deviceDetail.setDeviceTypeCode("123");
    	deviceDetail.setId("121");
    	deviceDetail.setIsActive(true);
    	deviceDetail.setCrBy("110005");
    	deviceDetail.setUpdBy("110005");
    	deviceDetail.setCrDtimes(LocalDateTime.now());
    	deviceDetail.setUpdDtimes(LocalDateTime.now());
    	deviceDetail.setMake("make");
    	deviceDetail.setModel("model");
    	deviceDetail.setPartnerOrganizationName("pog");
		Mockito.doNothing().when(auditUtil).auditRequest(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.doNothing().when(auditUtil).auditRequest(Mockito.any(), Mockito.any(), Mockito.any());
		Mockito.doReturn(secureBiometricInterface).when(sbiRepository).findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString());
		Mockito.doReturn(secureBiometricInterface).when(sbiRepository).save(Mockito.any());
		Mockito.doReturn(secureBiometricInterfaceHistory).when(sbiHistoryRepository).save(Mockito.any());
		
		Mockito.doReturn(deviceDetail).when(deviceDetailRepository).findByIdAndIsDeletedFalseOrIsDeletedIsNullAndIsActiveTrue(Mockito.anyString());
		
		}
	
	@Test
    public void createSBITest() throws Exception {
		assertTrue(secureBiometricInterfaceService.createSecureBiometricInterface(sbicreatedto).getId().equals("1234"));
    }
	
	@Test(expected=RequestException.class)
    public void createSBINoDviceTest() throws Exception {
		Mockito.doReturn(null).when(deviceDetailRepository).findByIdAndIsDeletedFalseOrIsDeletedIsNullAndIsActiveTrue(Mockito.anyString());
		
		secureBiometricInterfaceService.createSecureBiometricInterface(sbicreatedto);
    }
	
	@Test(expected=RequestException.class)
    public void updateSBINoDeviceTest() throws Exception {
		Mockito.doReturn(null).when(deviceDetailRepository).findByIdAndIsDeletedFalseOrIsDeletedIsNullAndIsActiveTrue(Mockito.anyString());
		
		secureBiometricInterfaceService.updateSecureBiometricInterface(sbidto);
    }
	

	@Test
    public void updateDeviceDetailTest() throws Exception {
		assertTrue(secureBiometricInterfaceService.updateSecureBiometricInterface(sbidto).getId().equals("1234"));
    }
	@Test(expected=RequestException.class)
    public void updateDeviceDetailNotFoundTest() throws Exception {
		Mockito.doReturn(null).when(sbiRepository).findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString());
		secureBiometricInterfaceService.updateSecureBiometricInterface(sbidto);
    }
}
