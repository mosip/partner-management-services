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

import io.mosip.pmp.authdevice.dto.DeviceDetailDto;
import io.mosip.pmp.authdevice.dto.DeviceDetailUpdateDto;
import io.mosip.pmp.authdevice.dto.UpdateDeviceDetailStatusDto;
import io.mosip.pmp.authdevice.entity.DeviceDetail;
import io.mosip.pmp.authdevice.entity.RegistrationDeviceSubType;
import io.mosip.pmp.authdevice.exception.RequestException;
import io.mosip.pmp.authdevice.repository.DeviceDetailRepository;
import io.mosip.pmp.authdevice.repository.RegistrationDeviceSubTypeRepository;
import io.mosip.pmp.authdevice.service.DeviceDetailService;
import io.mosip.pmp.authdevice.service.impl.DeviceDetailServiceImpl;
import io.mosip.pmp.authdevice.util.AuditUtil;
import io.mosip.pmp.partner.PartnerserviceApplication;
import io.mosip.pmp.partner.entity.Partner;
import io.mosip.pmp.partner.repository.PartnerServiceRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { PartnerserviceApplication.class })
@AutoConfigureMockMvc
@EnableWebMvc
public class DeviceDetailServiceTest {
	
	@InjectMocks
	DeviceDetailService deviceDetaillService = new DeviceDetailServiceImpl();
	
	@Mock
	AuditUtil auditUtil;
	
	@Mock
	DeviceDetailRepository deviceDetailRepository;

	@Mock
	RegistrationDeviceSubTypeRepository registrationDeviceSubTypeRepository;

	@Mock
	PartnerServiceRepository partnerRepository;
	
	DeviceDetail deviceDetail=new DeviceDetail();
	Partner partner=new Partner();
	RegistrationDeviceSubType registrationDeviceSubType=new RegistrationDeviceSubType();
	DeviceDetailDto deviceDetailDto=new DeviceDetailDto();
	DeviceDetailUpdateDto deviceDetailUpdateDto=new DeviceDetailUpdateDto();
	@Before
	public void setup() {
		partner.setId("1234");
		registrationDeviceSubType.setCode("123");
		registrationDeviceSubType.setDeviceTypeCode("123");
		deviceDetailUpdateDto.setDeviceProviderId("1234");
		deviceDetailUpdateDto.setDeviceSubTypeCode("123");
		deviceDetailUpdateDto.setDeviceTypeCode("123");
		deviceDetailUpdateDto.setId("121");
		deviceDetailUpdateDto.setIsActive(true);
		deviceDetailUpdateDto.setIsItForRegistrationDevice(false);
		deviceDetailUpdateDto.setMake("make");
		deviceDetailUpdateDto.setModel("model");
		deviceDetailUpdateDto.setPartnerOrganizationName("pog");
    	deviceDetailDto.setDeviceProviderId("1234");
    	deviceDetailDto.setDeviceSubTypeCode("123");
    	deviceDetailDto.setDeviceTypeCode("123");
    	deviceDetailDto.setId("121");
    	
    	deviceDetailDto.setIsItForRegistrationDevice(false);
    	deviceDetailDto.setMake("make");
    	deviceDetailDto.setModel("model");
    	deviceDetailDto.setPartnerOrganizationName("pog");
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
		Mockito.doReturn(deviceDetail).when(deviceDetailRepository).save(Mockito.any(DeviceDetail.class));
		Mockito.doReturn(deviceDetail).when(deviceDetailRepository).findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString());
		Mockito.doReturn(deviceDetail).when(deviceDetailRepository).findByDeviceDetail(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString());
		Mockito.doReturn(registrationDeviceSubType).when(registrationDeviceSubTypeRepository).findByCodeAndTypeCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString(),Mockito.anyString());
		Mockito.doReturn(partner).when(partnerRepository).findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString());
	}
	
	@Test
    public void createDeviceDetailTest() throws Exception {
		Mockito.doReturn(null).when(deviceDetailRepository).findByDeviceDetail(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString());
		assertTrue(deviceDetaillService.createDeviceDetails(deviceDetailDto).getId().equals("121"));
    }
	
	@Test(expected=RequestException.class)
    public void createDeviceDetailNoPartnerTest() throws Exception {
		Mockito.doReturn(null).when(partnerRepository).findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString());

       deviceDetaillService.createDeviceDetails(deviceDetailDto);
    }
	
	@Test(expected=RequestException.class)
    public void updateDeviceDetailNoPartnerTest() throws Exception {
		Mockito.doReturn(null).when(partnerRepository).findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString());

	       deviceDetaillService.updateDeviceDetails(deviceDetailUpdateDto);
    }
	
	@Test(expected=RequestException.class)
    public void createDeviceDetailNoSubtypeTest() throws Exception {
		Mockito.doReturn(null).when(registrationDeviceSubTypeRepository).findByCodeAndTypeCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString(),Mockito.anyString());
		deviceDetaillService.createDeviceDetails(deviceDetailDto);
    }
	
	@Test(expected=RequestException.class)
    public void updateDeviceDetailNoSubtypeTest() throws Exception {
		Mockito.doReturn(null).when(registrationDeviceSubTypeRepository).findByCodeAndTypeCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(Mockito.anyString(),Mockito.anyString());
		deviceDetaillService.updateDeviceDetails(deviceDetailUpdateDto);
    }
	
	@Test(expected=RequestException.class)
    public void createDeviceDetailAlreadyExistsTest() throws Exception {
       deviceDetaillService.createDeviceDetails(deviceDetailDto);
    }
	

	@Test
    public void updateDeviceDetailTest() throws Exception {
       assertTrue(deviceDetaillService.updateDeviceDetails(deviceDetailUpdateDto).getId().equals("121"));
    }
	
	@Test(expected=RequestException.class)
    public void updateDeviceDetailNotFoundTest() throws Exception {
		Mockito.doReturn(null).when(deviceDetailRepository).findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString());
		deviceDetaillService.updateDeviceDetails(deviceDetailUpdateDto);
    }
	
	@Test
	public void updateDeviceDetailStatusTest_Approve() {
		deviceDetaillService.updateDeviceDetailStatus(statusUpdateRequest("Activate"));
	}
	
	@Test
	public void updateDeviceDetailStatusTest_Reject() {
		deviceDetaillService.updateDeviceDetailStatus(statusUpdateRequest("De-activate"));
	}
	
	@Test(expected = RequestException.class)
	public void updateDeviceDetailStatusTest_Status_Exception() {
		deviceDetaillService.updateDeviceDetailStatus(statusUpdateRequest("De-Activate"));
	}
	
	@Test(expected = RequestException.class)
	public void updateDeviceDetailStatusTest_DeviceDetail_Exception() {
		UpdateDeviceDetailStatusDto request = statusUpdateRequest("De-Activate");
		request.setId("34567");
		Mockito.doReturn(null).when(deviceDetailRepository).findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString());
		deviceDetaillService.updateDeviceDetailStatus(request);
	}
	
	private UpdateDeviceDetailStatusDto statusUpdateRequest(String status) {
		UpdateDeviceDetailStatusDto request = new UpdateDeviceDetailStatusDto();
		request.setApprovalStatus(status);
		request.setId("121");
		return request;
	}
}
