package io.mosip.pms.test.regdevice.service.impl;


import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.device.exception.DeviceValidationException;
import io.mosip.pms.device.regdevice.entity.RegDeviceDetail;
import io.mosip.pms.device.regdevice.entity.RegRegisteredDevice;
import io.mosip.pms.device.regdevice.entity.RegRegisteredDeviceHistory;
import io.mosip.pms.device.regdevice.entity.RegSecureBiometricInterface;
import io.mosip.pms.device.regdevice.entity.RegSecureBiometricInterfaceHistory;
import io.mosip.pms.device.regdevice.repository.RegDeviceDetailRepository;
import io.mosip.pms.device.regdevice.repository.RegRegisteredDeviceHistoryRepository;
import io.mosip.pms.device.regdevice.repository.RegRegisteredDeviceRepository;
import io.mosip.pms.device.regdevice.repository.RegSecureBiometricInterfaceHistoryRepository;
import io.mosip.pms.device.regdevice.repository.RegSecureBiometricInterfaceRepository;
import io.mosip.pms.device.regdevice.service.DeviceValidationService;
import io.mosip.pms.device.regdevice.service.impl.DeviceValidationServiceImpl;
import io.mosip.pms.device.request.dto.DigitalIdDto;
import io.mosip.pms.device.request.dto.ValidateDeviceDto;
import io.mosip.pms.device.util.AuditUtil;
import io.mosip.pms.test.PartnerManagementServiceTest;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = { PartnerManagementServiceTest.class })
@Transactional("regDevicePlatformTransactionManager")
public class DeviceValidationServiceImplTest {

	@InjectMocks
	private DeviceValidationService deviceValidationService = new DeviceValidationServiceImpl();

	@Mock
	private RegRegisteredDeviceRepository registeredDeviceRepository;
	
	@Mock
	private PartnerServiceRepository deviceProviderRepository;
	
	@Mock
	private RegSecureBiometricInterfaceRepository deviceServiceRepository;
	
	@Mock
	private RegDeviceDetailRepository deviceDetailRepository;

	@Mock
	private RegRegisteredDeviceHistoryRepository registeredDeviceHistoryRepository;
	
	@Mock
	private RegSecureBiometricInterfaceHistoryRepository deviceServiceHistoryRepository;
	
	@Mock
	private AuditUtil audit;
	
	@Test	
	public void validateDeviceProviderstest() {
		Mockito.doNothing().when(audit).auditRequest(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		ValidateDeviceDto validateDeviceDto = new ValidateDeviceDto();
		DigitalIdDto digitalId = new DigitalIdDto();
		RegRegisteredDevice regRegisteredDevice = new RegRegisteredDevice();
		RegDeviceDetail regDeviceDetail = new RegDeviceDetail();
		Partner deviceProvider = new Partner();
		List<RegSecureBiometricInterface> deviceServices = new ArrayList<RegSecureBiometricInterface>();
		RegSecureBiometricInterface deviceobject = new RegSecureBiometricInterface();
		digitalId.setDateTime("2021-01-29T13:13:04.923Z");
		digitalId.setDeviceSubType("subtype");
		digitalId.setDp("abcd");
		digitalId.setDpId("1234");
		digitalId.setMake("make");
		digitalId.setModel("model");
		digitalId.setSerialNo("123456");
		digitalId.setType("type");
		validateDeviceDto.setDeviceCode("1234");
		validateDeviceDto.setDeviceServiceVersion("1.0.0");
		validateDeviceDto.setDigitalId(digitalId);
		validateDeviceDto.setPurpose("Registration");
		regRegisteredDevice.setDeviceId("1234");
		regRegisteredDevice.setDeviceDetailId("1234");
		regRegisteredDevice.setStatusCode("REGISTERED");
		regRegisteredDevice.setSerialNo("123456");
		regDeviceDetail.setDeviceProviderId("1234");
		regDeviceDetail.setDeviceSubTypeCode("subtype");
		regDeviceDetail.setDeviceTypeCode("type");
		regDeviceDetail.setMake("make");
		regDeviceDetail.setModel("model");
		deviceServices.add(deviceobject);
		deviceProvider.setId("1234");
		Mockito.when(registeredDeviceRepository.findByCodeAndPurposeIgnoreCaseAndIsActiveIsTrue(Mockito.anyString(), Mockito.anyString())).thenReturn(regRegisteredDevice);
		Mockito.when(deviceProviderRepository.findByIdAndIsActiveIsTrue(Mockito.anyString())).thenReturn(deviceProvider);
		Mockito.when(deviceServiceRepository.findBySwVersionAndIsActiveIsTrue(Mockito.anyString())).thenReturn(deviceServices);
		Mockito.when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString())).thenReturn(regDeviceDetail);
		deviceValidationService.validateDeviceProviders(validateDeviceDto);
	}
	
	@Test
	public void validateDeviceProviderstest001() {
		Mockito.doNothing().when(audit).auditRequest(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		ValidateDeviceDto validateDeviceDto = new ValidateDeviceDto();
		DigitalIdDto digitalId = new DigitalIdDto();
		RegDeviceDetail regDeviceDetail = new RegDeviceDetail();
		RegRegisteredDeviceHistory regRegisteredDeviceHistory = new RegRegisteredDeviceHistory();
		List<RegSecureBiometricInterfaceHistory> deviceServiceHistory = new ArrayList<>();
		RegSecureBiometricInterfaceHistory object = new RegSecureBiometricInterfaceHistory();
		deviceServiceHistory.add(object);
		regRegisteredDeviceHistory.setStatusCode("REGISTERED");
		regRegisteredDeviceHistory.setDeviceDetailId("12345");
		digitalId.setDateTime("2021-01-29T13:13:04.923Z");
		digitalId.setDeviceSubType("subtype");
		digitalId.setDp("abcd");
		digitalId.setDpId("1234");
		digitalId.setMake("make");
		digitalId.setModel("model");
		digitalId.setSerialNo("123456");
		digitalId.setType("type");
		validateDeviceDto.setDeviceCode("1234");
		validateDeviceDto.setDeviceServiceVersion("1.0.0");
		validateDeviceDto.setDigitalId(digitalId);
		validateDeviceDto.setPurpose("Registration");
		validateDeviceDto.setTimeStamp("2021-01-29T13:13:04.923Z");
		regDeviceDetail.setDeviceProviderId("1234");
		regDeviceDetail.setDeviceSubTypeCode("subtype");
		regDeviceDetail.setDeviceTypeCode("type");
		regDeviceDetail.setMake("make");
		regDeviceDetail.setModel("model");
		Mockito.when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString())).thenReturn(regDeviceDetail);
		Mockito.when(registeredDeviceHistoryRepository.findRegisteredDeviceHistoryByIdAndEffTimesAndPurpose(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(regRegisteredDeviceHistory);
		Mockito.when(deviceServiceHistoryRepository.findByIdAndIsActiveIsTrueAndByEffectiveTimes(Mockito.anyString(), Mockito.any())).thenReturn(deviceServiceHistory);
		deviceValidationService.validateDeviceProviders(validateDeviceDto);
	}
	
	@Test(expected = DeviceValidationException.class)
	public void validateDeviceProviderstest001a() {
		Mockito.doNothing().when(audit).auditRequest(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		ValidateDeviceDto validateDeviceDto = new ValidateDeviceDto();
		DigitalIdDto digitalId = new DigitalIdDto();
		RegDeviceDetail regDeviceDetail = new RegDeviceDetail();
		RegRegisteredDeviceHistory regRegisteredDeviceHistory = new RegRegisteredDeviceHistory();
		List<RegSecureBiometricInterfaceHistory> deviceServiceHistory = new ArrayList<>();
		RegSecureBiometricInterfaceHistory object = new RegSecureBiometricInterfaceHistory();
		deviceServiceHistory.add(object);
		regRegisteredDeviceHistory.setStatusCode("REGISTERED");
		regRegisteredDeviceHistory.setDeviceDetailId("12345");
		digitalId.setDateTime("2021-01-29T13:13:04.923Z");
		digitalId.setDeviceSubType("subtype");
		digitalId.setDp("abcd");
		digitalId.setDpId("1234");
		digitalId.setMake("make");
		digitalId.setModel("model");
		digitalId.setSerialNo("123456");
		digitalId.setType("type");
		validateDeviceDto.setDeviceCode("1234");
		validateDeviceDto.setDeviceServiceVersion("1.0.0");
		validateDeviceDto.setDigitalId(digitalId);
		validateDeviceDto.setPurpose("Registration");
		validateDeviceDto.setTimeStamp("2021-01-29T13:13:04.923Z");
		regDeviceDetail.setDeviceProviderId("1234");
		regDeviceDetail.setDeviceSubTypeCode("subtype");
		regDeviceDetail.setDeviceTypeCode("type");
		regDeviceDetail.setMake("make");
		regDeviceDetail.setModel("model");
		Mockito.when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString())).thenReturn(regDeviceDetail);
		//Mockito.when(registeredDeviceHistoryRepository.findRegisteredDeviceHistoryByIdAndEffTimesAndPurpose(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(regRegisteredDeviceHistory);
		Mockito.when(deviceServiceHistoryRepository.findByIdAndIsActiveIsTrueAndByEffectiveTimes(Mockito.anyString(), Mockito.any())).thenReturn(deviceServiceHistory);
		deviceValidationService.validateDeviceProviders(validateDeviceDto);
	}
	
	@Test(expected = DeviceValidationException.class)
	public void validateDeviceProviderstest001b() {
		Mockito.doNothing().when(audit).auditRequest(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		ValidateDeviceDto validateDeviceDto = new ValidateDeviceDto();
		DigitalIdDto digitalId = new DigitalIdDto();
		RegDeviceDetail regDeviceDetail = new RegDeviceDetail();
		RegRegisteredDeviceHistory regRegisteredDeviceHistory = new RegRegisteredDeviceHistory();
		List<RegSecureBiometricInterfaceHistory> deviceServiceHistory = new ArrayList<>();
		RegSecureBiometricInterfaceHistory object = new RegSecureBiometricInterfaceHistory();
		deviceServiceHistory.add(object);
		regRegisteredDeviceHistory.setStatusCode("AUTH");
		regRegisteredDeviceHistory.setDeviceDetailId("12345");
		digitalId.setDateTime("2021-01-29T13:13:04.923Z");
		digitalId.setDeviceSubType("subtype");
		digitalId.setDp("abcd");
		digitalId.setDpId("1234");
		digitalId.setMake("make");
		digitalId.setModel("model");
		digitalId.setSerialNo("123456");
		digitalId.setType("type");
		validateDeviceDto.setDeviceCode("1234");
		validateDeviceDto.setDeviceServiceVersion("1.0.0");
		validateDeviceDto.setDigitalId(digitalId);
		validateDeviceDto.setPurpose("Registration");
		validateDeviceDto.setTimeStamp("2021-01-29T13:13:04.923Z");
		regDeviceDetail.setDeviceProviderId("1234");
		regDeviceDetail.setDeviceSubTypeCode("subtype");
		regDeviceDetail.setDeviceTypeCode("type");
		regDeviceDetail.setMake("make");
		regDeviceDetail.setModel("model");
		Mockito.when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString())).thenReturn(regDeviceDetail);
		Mockito.when(registeredDeviceHistoryRepository.findRegisteredDeviceHistoryByIdAndEffTimesAndPurpose(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(regRegisteredDeviceHistory);
		Mockito.when(deviceServiceHistoryRepository.findByIdAndIsActiveIsTrueAndByEffectiveTimes(Mockito.anyString(), Mockito.any())).thenReturn(deviceServiceHistory);
		deviceValidationService.validateDeviceProviders(validateDeviceDto);
	}
	
	@Test(expected = DeviceValidationException.class)
	public void validateDeviceProviderstest001c() {
		Mockito.doNothing().when(audit).auditRequest(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		ValidateDeviceDto validateDeviceDto = new ValidateDeviceDto();
		DigitalIdDto digitalId = new DigitalIdDto();
		RegDeviceDetail regDeviceDetail = new RegDeviceDetail();
		RegRegisteredDeviceHistory regRegisteredDeviceHistory = new RegRegisteredDeviceHistory();
		List<RegSecureBiometricInterfaceHistory> deviceServiceHistory = new ArrayList<>();
		RegSecureBiometricInterfaceHistory object = new RegSecureBiometricInterfaceHistory();
		deviceServiceHistory.add(object);
		regRegisteredDeviceHistory.setStatusCode("REGISTERED");
		regRegisteredDeviceHistory.setDeviceDetailId("12345");
		digitalId.setDateTime("2021-01-29T13:13:04.923Z");
		digitalId.setDeviceSubType("subtype");
		digitalId.setDp("abcd");
		digitalId.setDpId("1234");
		digitalId.setMake("make");
		digitalId.setModel("model");
		digitalId.setSerialNo("123456");
		digitalId.setType("type");
		validateDeviceDto.setDeviceCode("1234");
		validateDeviceDto.setDeviceServiceVersion("1.0.0");
		validateDeviceDto.setDigitalId(digitalId);
		validateDeviceDto.setPurpose("Registration");
		validateDeviceDto.setTimeStamp("2021-01-29T13:13:04.923Z");
		regDeviceDetail.setDeviceProviderId("1234");
		regDeviceDetail.setDeviceSubTypeCode("subtype");
		regDeviceDetail.setDeviceTypeCode("type");
		regDeviceDetail.setMake("make");
		regDeviceDetail.setModel("model");
		Mockito.when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString())).thenReturn(regDeviceDetail);
		Mockito.when(registeredDeviceHistoryRepository.findRegisteredDeviceHistoryByIdAndEffTimesAndPurpose(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(regRegisteredDeviceHistory);
		//Mockito.when(deviceServiceHistoryRepository.findByIdAndIsActiveIsTrueAndByEffectiveTimes(Mockito.anyString(), Mockito.any())).thenReturn(deviceServiceHistory);
		deviceValidationService.validateDeviceProviders(validateDeviceDto);
	}
	
	@Test(expected = DeviceValidationException.class)
	public void validateDeviceProviderstest001d() {
		Mockito.doNothing().when(audit).auditRequest(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		ValidateDeviceDto validateDeviceDto = new ValidateDeviceDto();
		DigitalIdDto digitalId = new DigitalIdDto();
		RegDeviceDetail regDeviceDetail = new RegDeviceDetail();
		RegRegisteredDeviceHistory regRegisteredDeviceHistory = new RegRegisteredDeviceHistory();
		List<RegSecureBiometricInterfaceHistory> deviceServiceHistory = new ArrayList<>();
		RegSecureBiometricInterfaceHistory object = new RegSecureBiometricInterfaceHistory();
		deviceServiceHistory.add(object);
		regRegisteredDeviceHistory.setStatusCode("REGISTERED");
		regRegisteredDeviceHistory.setDeviceDetailId("12345");
		digitalId.setDateTime("2021-01-29T13:13:04.923Z");
		digitalId.setDeviceSubType("subtype");
		digitalId.setDp("abcd");
		digitalId.setDpId("1234");
		digitalId.setMake("makee");
		digitalId.setModel("model");
		digitalId.setSerialNo("123456");
		digitalId.setType("type");
		validateDeviceDto.setDeviceCode("1234");
		validateDeviceDto.setDeviceServiceVersion("1.0.0");
		validateDeviceDto.setDigitalId(digitalId);
		validateDeviceDto.setPurpose("Registration");
		validateDeviceDto.setTimeStamp("2021-01-29T13:13:04.923Z");
		regDeviceDetail.setDeviceProviderId("1234");
		regDeviceDetail.setDeviceSubTypeCode("subtype");
		regDeviceDetail.setDeviceTypeCode("type");
		regDeviceDetail.setMake("make");
		regDeviceDetail.setModel("model");
		Mockito.when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString())).thenReturn(regDeviceDetail);
		Mockito.when(registeredDeviceHistoryRepository.findRegisteredDeviceHistoryByIdAndEffTimesAndPurpose(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(regRegisteredDeviceHistory);
		Mockito.when(deviceServiceHistoryRepository.findByIdAndIsActiveIsTrueAndByEffectiveTimes(Mockito.anyString(), Mockito.any())).thenReturn(deviceServiceHistory);
		deviceValidationService.validateDeviceProviders(validateDeviceDto);
	}
	
	@Test(expected = DeviceValidationException.class)
	public void validateDeviceProviderstest001e() {
		Mockito.doNothing().when(audit).auditRequest(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		ValidateDeviceDto validateDeviceDto = new ValidateDeviceDto();
		DigitalIdDto digitalId = new DigitalIdDto();
		RegDeviceDetail regDeviceDetail = new RegDeviceDetail();
		RegRegisteredDeviceHistory regRegisteredDeviceHistory = new RegRegisteredDeviceHistory();
		List<RegSecureBiometricInterfaceHistory> deviceServiceHistory = new ArrayList<>();
		RegSecureBiometricInterfaceHistory object = new RegSecureBiometricInterfaceHistory();
		deviceServiceHistory.add(object);
		regRegisteredDeviceHistory.setStatusCode("REGISTERED");
		regRegisteredDeviceHistory.setDeviceDetailId("12345");
		digitalId.setDateTime("2021-01-29T13:13:04.923Z");
		digitalId.setDeviceSubType("subtype");
		digitalId.setDp("abcd");
		digitalId.setDpId("1234");
		digitalId.setMake("make");
		digitalId.setModel("modell");
		digitalId.setSerialNo("123456");
		digitalId.setType("type");
		validateDeviceDto.setDeviceCode("1234");
		validateDeviceDto.setDeviceServiceVersion("1.0.0");
		validateDeviceDto.setDigitalId(digitalId);
		validateDeviceDto.setPurpose("Registration");
		validateDeviceDto.setTimeStamp("2021-01-29T13:13:04.923Z");
		regDeviceDetail.setDeviceProviderId("1234");
		regDeviceDetail.setDeviceSubTypeCode("subtype");
		regDeviceDetail.setDeviceTypeCode("type");
		regDeviceDetail.setMake("make");
		regDeviceDetail.setModel("model");
		Mockito.when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString())).thenReturn(regDeviceDetail);
		Mockito.when(registeredDeviceHistoryRepository.findRegisteredDeviceHistoryByIdAndEffTimesAndPurpose(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(regRegisteredDeviceHistory);
		Mockito.when(deviceServiceHistoryRepository.findByIdAndIsActiveIsTrueAndByEffectiveTimes(Mockito.anyString(), Mockito.any())).thenReturn(deviceServiceHistory);
		deviceValidationService.validateDeviceProviders(validateDeviceDto);
	}
	
	@Test(expected = DeviceValidationException.class)
	public void validateDeviceProviderstest001f() {
		Mockito.doNothing().when(audit).auditRequest(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		ValidateDeviceDto validateDeviceDto = new ValidateDeviceDto();
		DigitalIdDto digitalId = new DigitalIdDto();
		RegDeviceDetail regDeviceDetail = new RegDeviceDetail();
		RegRegisteredDeviceHistory regRegisteredDeviceHistory = new RegRegisteredDeviceHistory();
		List<RegSecureBiometricInterfaceHistory> deviceServiceHistory = new ArrayList<>();
		RegSecureBiometricInterfaceHistory object = new RegSecureBiometricInterfaceHistory();
		deviceServiceHistory.add(object);
		regRegisteredDeviceHistory.setStatusCode("REGISTERED");
		regRegisteredDeviceHistory.setDeviceDetailId("12345");
		digitalId.setDateTime("2021-01-29T13:13:04.923Z");
		digitalId.setDeviceSubType("subtype");
		digitalId.setDp("abcd");
		digitalId.setDpId("12345");
		digitalId.setMake("make");
		digitalId.setModel("model");
		digitalId.setSerialNo("123456");
		digitalId.setType("type");
		validateDeviceDto.setDeviceCode("1234");
		validateDeviceDto.setDeviceServiceVersion("1.0.0");
		validateDeviceDto.setDigitalId(digitalId);
		validateDeviceDto.setPurpose("Registration");
		validateDeviceDto.setTimeStamp("2021-01-29T13:13:04.923Z");
		regDeviceDetail.setDeviceProviderId("1234");
		regDeviceDetail.setDeviceSubTypeCode("subtype");
		regDeviceDetail.setDeviceTypeCode("type");
		regDeviceDetail.setMake("make");
		regDeviceDetail.setModel("model");
		Mockito.when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString())).thenReturn(regDeviceDetail);
		Mockito.when(registeredDeviceHistoryRepository.findRegisteredDeviceHistoryByIdAndEffTimesAndPurpose(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(regRegisteredDeviceHistory);
		Mockito.when(deviceServiceHistoryRepository.findByIdAndIsActiveIsTrueAndByEffectiveTimes(Mockito.anyString(), Mockito.any())).thenReturn(deviceServiceHistory);
		deviceValidationService.validateDeviceProviders(validateDeviceDto);
	}
	
	@Test(expected = DeviceValidationException.class)
	public void validateDeviceProviderstest001g() {
		Mockito.doNothing().when(audit).auditRequest(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		ValidateDeviceDto validateDeviceDto = new ValidateDeviceDto();
		DigitalIdDto digitalId = new DigitalIdDto();
		RegDeviceDetail regDeviceDetail = new RegDeviceDetail();
		RegRegisteredDeviceHistory regRegisteredDeviceHistory = new RegRegisteredDeviceHistory();
		List<RegSecureBiometricInterfaceHistory> deviceServiceHistory = new ArrayList<>();
		RegSecureBiometricInterfaceHistory object = new RegSecureBiometricInterfaceHistory();
		deviceServiceHistory.add(object);
		regRegisteredDeviceHistory.setStatusCode("REGISTERED");
		regRegisteredDeviceHistory.setDeviceDetailId("12345");
		digitalId.setDateTime("2021-01-29T13:13:04.923Z");
		digitalId.setDeviceSubType("subtype");
		digitalId.setDp("abcd");
		digitalId.setDpId("1234");
		digitalId.setMake("make");
		digitalId.setModel("model");
		digitalId.setSerialNo("123456");
		digitalId.setType("typee");
		validateDeviceDto.setDeviceCode("1234");
		validateDeviceDto.setDeviceServiceVersion("1.0.0");
		validateDeviceDto.setDigitalId(digitalId);
		validateDeviceDto.setPurpose("Registration");
		validateDeviceDto.setTimeStamp("2021-01-29T13:13:04.923Z");
		regDeviceDetail.setDeviceProviderId("1234");
		regDeviceDetail.setDeviceSubTypeCode("subtype");
		regDeviceDetail.setDeviceTypeCode("type");
		regDeviceDetail.setMake("make");
		regDeviceDetail.setModel("model");
		Mockito.when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString())).thenReturn(regDeviceDetail);
		Mockito.when(registeredDeviceHistoryRepository.findRegisteredDeviceHistoryByIdAndEffTimesAndPurpose(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(regRegisteredDeviceHistory);
		Mockito.when(deviceServiceHistoryRepository.findByIdAndIsActiveIsTrueAndByEffectiveTimes(Mockito.anyString(), Mockito.any())).thenReturn(deviceServiceHistory);
		deviceValidationService.validateDeviceProviders(validateDeviceDto);
	}
	
	@Test(expected = DeviceValidationException.class)
	public void validateDeviceProviderstest001h() {
		Mockito.doNothing().when(audit).auditRequest(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		ValidateDeviceDto validateDeviceDto = new ValidateDeviceDto();
		DigitalIdDto digitalId = new DigitalIdDto();
		RegDeviceDetail regDeviceDetail = new RegDeviceDetail();
		RegRegisteredDeviceHistory regRegisteredDeviceHistory = new RegRegisteredDeviceHistory();
		List<RegSecureBiometricInterfaceHistory> deviceServiceHistory = new ArrayList<>();
		RegSecureBiometricInterfaceHistory object = new RegSecureBiometricInterfaceHistory();
		deviceServiceHistory.add(object);
		regRegisteredDeviceHistory.setStatusCode("REGISTERED");
		regRegisteredDeviceHistory.setDeviceDetailId("12345");
		digitalId.setDateTime("2021-01-29T13:13:04.923Z");
		digitalId.setDeviceSubType("subtypee");
		digitalId.setDp("abcd");
		digitalId.setDpId("1234");
		digitalId.setMake("make");
		digitalId.setModel("model");
		digitalId.setSerialNo("123456");
		digitalId.setType("type");
		validateDeviceDto.setDeviceCode("1234");
		validateDeviceDto.setDeviceServiceVersion("1.0.0");
		validateDeviceDto.setDigitalId(digitalId);
		validateDeviceDto.setPurpose("Registration");
		validateDeviceDto.setTimeStamp("2021-01-29T13:13:04.923Z");
		regDeviceDetail.setDeviceProviderId("1234");
		regDeviceDetail.setDeviceSubTypeCode("subtype");
		regDeviceDetail.setDeviceTypeCode("type");
		regDeviceDetail.setMake("make");
		regDeviceDetail.setModel("model");
		Mockito.when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString())).thenReturn(regDeviceDetail);
		Mockito.when(registeredDeviceHistoryRepository.findRegisteredDeviceHistoryByIdAndEffTimesAndPurpose(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(regRegisteredDeviceHistory);
		Mockito.when(deviceServiceHistoryRepository.findByIdAndIsActiveIsTrueAndByEffectiveTimes(Mockito.anyString(), Mockito.any())).thenReturn(deviceServiceHistory);
		deviceValidationService.validateDeviceProviders(validateDeviceDto);
	}
	
	@Test (expected = DeviceValidationException.class)
	public void validateDeviceProviderstest01() {
		ValidateDeviceDto validateDeviceDto = new ValidateDeviceDto();
		DigitalIdDto digitalId = new DigitalIdDto();
		RegRegisteredDevice regRegisteredDevice = new RegRegisteredDevice();
		RegDeviceDetail regDeviceDetail = new RegDeviceDetail();
		Partner deviceProvider = new Partner();
		List<RegSecureBiometricInterface> deviceServices = new ArrayList<RegSecureBiometricInterface>();
		RegSecureBiometricInterface deviceobject = new RegSecureBiometricInterface();
		digitalId.setDateTime("2021-01-29T13:13:04.923Z");
		digitalId.setDeviceSubType("subtype");
		digitalId.setDp("abcd");
		digitalId.setDpId("1234");
		digitalId.setMake("makeee");
		digitalId.setModel("model");
		digitalId.setSerialNo("123456");
		digitalId.setType("type");
		validateDeviceDto.setDeviceCode("1234");
		validateDeviceDto.setDeviceServiceVersion("1.0.0");
		validateDeviceDto.setDigitalId(digitalId);
		validateDeviceDto.setPurpose("Registration");
		regRegisteredDevice.setDeviceId("1234");
		regRegisteredDevice.setDeviceDetailId("1234");
		regRegisteredDevice.setStatusCode("REGISTERED");
		regRegisteredDevice.setSerialNo("123456");
		regDeviceDetail.setDeviceProviderId("1234");
		regDeviceDetail.setDeviceSubTypeCode("subtype");
		regDeviceDetail.setDeviceTypeCode("type");
		regDeviceDetail.setMake("make");
		regDeviceDetail.setModel("model");
		deviceServices.add(deviceobject);
		deviceProvider.setId("1234");
		Mockito.when(registeredDeviceRepository.findByCodeAndPurposeIgnoreCaseAndIsActiveIsTrue(Mockito.anyString(), Mockito.anyString())).thenReturn(regRegisteredDevice);
		Mockito.when(deviceProviderRepository.findByIdAndIsActiveIsTrue(Mockito.anyString())).thenReturn(deviceProvider);
		Mockito.when(deviceServiceRepository.findBySwVersionAndIsActiveIsTrue(Mockito.anyString())).thenReturn(deviceServices);
		Mockito.when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString())).thenReturn(regDeviceDetail);
		deviceValidationService.validateDeviceProviders(validateDeviceDto);
	}
	
	@Test (expected = DeviceValidationException.class)
	public void validateDeviceProviderstest02() {
		ValidateDeviceDto validateDeviceDto = new ValidateDeviceDto();
		DigitalIdDto digitalId = new DigitalIdDto();
		RegRegisteredDevice regRegisteredDevice = new RegRegisteredDevice();
		RegDeviceDetail regDeviceDetail = new RegDeviceDetail();
		Partner deviceProvider = new Partner();
		List<RegSecureBiometricInterface> deviceServices = new ArrayList<RegSecureBiometricInterface>();
		RegSecureBiometricInterface deviceobject = new RegSecureBiometricInterface();
		digitalId.setDateTime("2021-01-29T13:13:04.923Z");
		digitalId.setDeviceSubType("subtype");
		digitalId.setDp("abcd");
		digitalId.setDpId("1234");
		digitalId.setMake("make");
		digitalId.setModel("modelll");
		digitalId.setSerialNo("123456");
		digitalId.setType("type");
		validateDeviceDto.setDeviceCode("1234");
		validateDeviceDto.setDeviceServiceVersion("1.0.0");
		validateDeviceDto.setDigitalId(digitalId);
		validateDeviceDto.setPurpose("Registration");
		regRegisteredDevice.setDeviceId("1234");
		regRegisteredDevice.setDeviceDetailId("1234");
		regRegisteredDevice.setStatusCode("REGISTERED");
		regRegisteredDevice.setSerialNo("123456");
		regDeviceDetail.setDeviceProviderId("1234");
		regDeviceDetail.setDeviceSubTypeCode("subtype");
		regDeviceDetail.setDeviceTypeCode("type");
		regDeviceDetail.setMake("make");
		regDeviceDetail.setModel("model");
		deviceServices.add(deviceobject);
		deviceProvider.setId("1234");
		Mockito.when(registeredDeviceRepository.findByCodeAndPurposeIgnoreCaseAndIsActiveIsTrue(Mockito.anyString(), Mockito.anyString())).thenReturn(regRegisteredDevice);
		Mockito.when(deviceProviderRepository.findByIdAndIsActiveIsTrue(Mockito.anyString())).thenReturn(deviceProvider);
		Mockito.when(deviceServiceRepository.findBySwVersionAndIsActiveIsTrue(Mockito.anyString())).thenReturn(deviceServices);
		Mockito.when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString())).thenReturn(regDeviceDetail);
		deviceValidationService.validateDeviceProviders(validateDeviceDto);
	}
	
	@Test (expected = DeviceValidationException.class)
	public void validateDeviceProviderstest03() {
		ValidateDeviceDto validateDeviceDto = new ValidateDeviceDto();
		DigitalIdDto digitalId = new DigitalIdDto();
		RegRegisteredDevice regRegisteredDevice = new RegRegisteredDevice();
		RegDeviceDetail regDeviceDetail = new RegDeviceDetail();
		Partner deviceProvider = new Partner();
		List<RegSecureBiometricInterface> deviceServices = new ArrayList<RegSecureBiometricInterface>();
		RegSecureBiometricInterface deviceobject = new RegSecureBiometricInterface();
		digitalId.setDateTime("2021-01-29T13:13:04.923Z");
		digitalId.setDeviceSubType("subtype");
		digitalId.setDp("abcd");
		digitalId.setDpId("12");
		digitalId.setMake("make");
		digitalId.setModel("model");
		digitalId.setSerialNo("123456");
		digitalId.setType("type");
		validateDeviceDto.setDeviceCode("1234");
		validateDeviceDto.setDeviceServiceVersion("1.0.0");
		validateDeviceDto.setDigitalId(digitalId);
		validateDeviceDto.setPurpose("Registration");
		regRegisteredDevice.setDeviceId("1234");
		regRegisteredDevice.setDeviceDetailId("1234");
		regRegisteredDevice.setStatusCode("REGISTERED");
		regRegisteredDevice.setSerialNo("123456");
		regDeviceDetail.setDeviceProviderId("1234");
		regDeviceDetail.setDeviceSubTypeCode("subtype");
		regDeviceDetail.setDeviceTypeCode("type");
		regDeviceDetail.setMake("make");
		regDeviceDetail.setModel("model");
		deviceServices.add(deviceobject);
		deviceProvider.setId("1234");
		Mockito.when(registeredDeviceRepository.findByCodeAndPurposeIgnoreCaseAndIsActiveIsTrue(Mockito.anyString(), Mockito.anyString())).thenReturn(regRegisteredDevice);
		Mockito.when(deviceProviderRepository.findByIdAndIsActiveIsTrue(Mockito.anyString())).thenReturn(deviceProvider);
		Mockito.when(deviceServiceRepository.findBySwVersionAndIsActiveIsTrue(Mockito.anyString())).thenReturn(deviceServices);
		Mockito.when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString())).thenReturn(regDeviceDetail);
		deviceValidationService.validateDeviceProviders(validateDeviceDto);
	}
	
	@Test (expected = DeviceValidationException.class)
	public void validateDeviceProviderstest04() {
		ValidateDeviceDto validateDeviceDto = new ValidateDeviceDto();
		DigitalIdDto digitalId = new DigitalIdDto();
		RegRegisteredDevice regRegisteredDevice = new RegRegisteredDevice();
		RegDeviceDetail regDeviceDetail = new RegDeviceDetail();
		Partner deviceProvider = new Partner();
		List<RegSecureBiometricInterface> deviceServices = new ArrayList<RegSecureBiometricInterface>();
		RegSecureBiometricInterface deviceobject = new RegSecureBiometricInterface();
		digitalId.setDateTime("2021-01-29T13:13:04.923Z");
		digitalId.setDeviceSubType("subtype");
		digitalId.setDp("abcd");
		digitalId.setDpId("1234");
		digitalId.setMake("make");
		digitalId.setModel("model");
		digitalId.setSerialNo("1234");
		digitalId.setType("type");
		validateDeviceDto.setDeviceCode("1234");
		validateDeviceDto.setDeviceServiceVersion("1.0.0");
		validateDeviceDto.setDigitalId(digitalId);
		validateDeviceDto.setPurpose("Registration");
		regRegisteredDevice.setDeviceId("1234");
		regRegisteredDevice.setDeviceDetailId("1234");
		regRegisteredDevice.setStatusCode("REGISTERED");
		regRegisteredDevice.setSerialNo("123456");
		regDeviceDetail.setDeviceProviderId("1234");
		regDeviceDetail.setDeviceSubTypeCode("subtype");
		regDeviceDetail.setDeviceTypeCode("type");
		regDeviceDetail.setMake("make");
		regDeviceDetail.setModel("model");
		deviceServices.add(deviceobject);
		deviceProvider.setId("1234");
		Mockito.when(registeredDeviceRepository.findByCodeAndPurposeIgnoreCaseAndIsActiveIsTrue(Mockito.anyString(), Mockito.anyString())).thenReturn(regRegisteredDevice);
		Mockito.when(deviceProviderRepository.findByIdAndIsActiveIsTrue(Mockito.anyString())).thenReturn(deviceProvider);
		Mockito.when(deviceServiceRepository.findBySwVersionAndIsActiveIsTrue(Mockito.anyString())).thenReturn(deviceServices);
		Mockito.when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString())).thenReturn(regDeviceDetail);
		deviceValidationService.validateDeviceProviders(validateDeviceDto);
	}
	
	@Test (expected = DeviceValidationException.class)
	public void validateDeviceProviderstest05() {
		ValidateDeviceDto validateDeviceDto = new ValidateDeviceDto();
		DigitalIdDto digitalId = new DigitalIdDto();
		RegRegisteredDevice regRegisteredDevice = new RegRegisteredDevice();
		RegDeviceDetail regDeviceDetail = new RegDeviceDetail();
		Partner deviceProvider = new Partner();
		List<RegSecureBiometricInterface> deviceServices = new ArrayList<RegSecureBiometricInterface>();
		RegSecureBiometricInterface deviceobject = new RegSecureBiometricInterface();
		digitalId.setDateTime("2021-01-29T13:13:04.923Z");
		digitalId.setDeviceSubType("subtype");
		digitalId.setDp("abcd");
		digitalId.setDpId("1234");
		digitalId.setMake("make");
		digitalId.setModel("model");
		digitalId.setSerialNo("123456");
		digitalId.setType("typeee");
		validateDeviceDto.setDeviceCode("1234");
		validateDeviceDto.setDeviceServiceVersion("1.0.0");
		validateDeviceDto.setDigitalId(digitalId);
		validateDeviceDto.setPurpose("Registration");
		regRegisteredDevice.setDeviceId("1234");
		regRegisteredDevice.setDeviceDetailId("1234");
		regRegisteredDevice.setStatusCode("REGISTERED");
		regRegisteredDevice.setSerialNo("123456");
		regDeviceDetail.setDeviceProviderId("1234");
		regDeviceDetail.setDeviceSubTypeCode("subtype");
		regDeviceDetail.setDeviceTypeCode("type");
		regDeviceDetail.setMake("make");
		regDeviceDetail.setModel("model");
		deviceServices.add(deviceobject);
		deviceProvider.setId("1234");
		Mockito.when(registeredDeviceRepository.findByCodeAndPurposeIgnoreCaseAndIsActiveIsTrue(Mockito.anyString(), Mockito.anyString())).thenReturn(regRegisteredDevice);
		Mockito.when(deviceProviderRepository.findByIdAndIsActiveIsTrue(Mockito.anyString())).thenReturn(deviceProvider);
		Mockito.when(deviceServiceRepository.findBySwVersionAndIsActiveIsTrue(Mockito.anyString())).thenReturn(deviceServices);
		Mockito.when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString())).thenReturn(regDeviceDetail);
		deviceValidationService.validateDeviceProviders(validateDeviceDto);
	}
	
	@Test (expected = DeviceValidationException.class)
	public void validateDeviceProviderstest06() {
		ValidateDeviceDto validateDeviceDto = new ValidateDeviceDto();
		DigitalIdDto digitalId = new DigitalIdDto();
		RegRegisteredDevice regRegisteredDevice = new RegRegisteredDevice();
		RegDeviceDetail regDeviceDetail = new RegDeviceDetail();
		Partner deviceProvider = new Partner();
		List<RegSecureBiometricInterface> deviceServices = new ArrayList<RegSecureBiometricInterface>();
		RegSecureBiometricInterface deviceobject = new RegSecureBiometricInterface();
		digitalId.setDateTime("2021-01-29T13:13:04.923Z");
		digitalId.setDeviceSubType("subtypeee");
		digitalId.setDp("abcd");
		digitalId.setDpId("1234");
		digitalId.setMake("make");
		digitalId.setModel("model");
		digitalId.setSerialNo("123456");
		digitalId.setType("type");
		validateDeviceDto.setDeviceCode("1234");
		validateDeviceDto.setDeviceServiceVersion("1.0.0");
		validateDeviceDto.setDigitalId(digitalId);
		validateDeviceDto.setPurpose("Registration");
		regRegisteredDevice.setDeviceId("1234");
		regRegisteredDevice.setDeviceDetailId("1234");
		regRegisteredDevice.setStatusCode("REGISTERED");
		regRegisteredDevice.setSerialNo("123456");
		regDeviceDetail.setDeviceProviderId("1234");
		regDeviceDetail.setDeviceSubTypeCode("subtype");
		regDeviceDetail.setDeviceTypeCode("type");
		regDeviceDetail.setMake("make");
		regDeviceDetail.setModel("model");
		deviceServices.add(deviceobject);
		deviceProvider.setId("1234");
		Mockito.when(registeredDeviceRepository.findByCodeAndPurposeIgnoreCaseAndIsActiveIsTrue(Mockito.anyString(), Mockito.anyString())).thenReturn(regRegisteredDevice);
		Mockito.when(deviceProviderRepository.findByIdAndIsActiveIsTrue(Mockito.anyString())).thenReturn(deviceProvider);
		Mockito.when(deviceServiceRepository.findBySwVersionAndIsActiveIsTrue(Mockito.anyString())).thenReturn(deviceServices);
		Mockito.when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString())).thenReturn(regDeviceDetail);
		deviceValidationService.validateDeviceProviders(validateDeviceDto);
	}
	
	@Test (expected = DeviceValidationException.class)
	public void validateDeviceProviderstest07() {
		ValidateDeviceDto validateDeviceDto = new ValidateDeviceDto();
		DigitalIdDto digitalId = new DigitalIdDto();
		RegRegisteredDevice regRegisteredDevice = new RegRegisteredDevice();
		RegDeviceDetail regDeviceDetail = new RegDeviceDetail();
		Partner deviceProvider = new Partner();
		List<RegSecureBiometricInterface> deviceServices = new ArrayList<RegSecureBiometricInterface>();
		RegSecureBiometricInterface deviceobject = new RegSecureBiometricInterface();
		digitalId.setDateTime("2021-01-29T13:13:04.923Z");
		digitalId.setDeviceSubType("subtype");
		digitalId.setDp("abcd");
		digitalId.setDpId("1234");
		digitalId.setMake("make");
		digitalId.setModel("model");
		digitalId.setSerialNo("123456");
		digitalId.setType("type");
		validateDeviceDto.setDeviceCode("1234");
		validateDeviceDto.setDeviceServiceVersion("1.0.0");
		validateDeviceDto.setDigitalId(digitalId);
		validateDeviceDto.setPurpose("Registration");
		regRegisteredDevice.setDeviceId("1234");
		regRegisteredDevice.setDeviceDetailId("1234");
		regRegisteredDevice.setStatusCode("REGISTERED");
		regRegisteredDevice.setSerialNo("123456");
		regDeviceDetail.setDeviceProviderId("1234");
		regDeviceDetail.setDeviceSubTypeCode("subtype");
		regDeviceDetail.setDeviceTypeCode("type");
		regDeviceDetail.setMake("make");
		regDeviceDetail.setModel("model");
		deviceServices.add(deviceobject);
		deviceProvider.setId("1234");
		Mockito.when(registeredDeviceRepository.findByCodeAndPurposeIgnoreCaseAndIsActiveIsTrue(Mockito.anyString(), Mockito.anyString())).thenReturn(regRegisteredDevice);
		Mockito.when(deviceProviderRepository.findByIdAndIsActiveIsTrue(Mockito.anyString())).thenReturn(deviceProvider);
		Mockito.when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString())).thenReturn(regDeviceDetail);
		deviceValidationService.validateDeviceProviders(validateDeviceDto);
	}
	
	@Test (expected = DeviceValidationException.class)
	public void validateDeviceProviderstest08() {
		ValidateDeviceDto validateDeviceDto = new ValidateDeviceDto();
		DigitalIdDto digitalId = new DigitalIdDto();
		RegRegisteredDevice regRegisteredDevice = new RegRegisteredDevice();
		RegDeviceDetail regDeviceDetail = new RegDeviceDetail();
		Partner deviceProvider = new Partner();
		List<RegSecureBiometricInterface> deviceServices = new ArrayList<RegSecureBiometricInterface>();
		RegSecureBiometricInterface deviceobject = new RegSecureBiometricInterface();
		digitalId.setDateTime("2021-01-29T13:13:04.923Z");
		digitalId.setDeviceSubType("subtype");
		digitalId.setDp("abcd");
		digitalId.setDpId("1234");
		digitalId.setMake("make");
		digitalId.setModel("model");
		digitalId.setSerialNo("123456");
		digitalId.setType("type");
		validateDeviceDto.setDeviceCode("1234");
		validateDeviceDto.setDeviceServiceVersion("1.0.0");
		validateDeviceDto.setDigitalId(digitalId);
		validateDeviceDto.setPurpose("Registration");
		regRegisteredDevice.setDeviceId("1234");
		regRegisteredDevice.setDeviceDetailId("1234");
		regRegisteredDevice.setStatusCode("REGISTERED");
		regRegisteredDevice.setSerialNo("123456");
		regDeviceDetail.setDeviceProviderId("1234");
		regDeviceDetail.setDeviceSubTypeCode("subtype");
		regDeviceDetail.setDeviceTypeCode("type");
		regDeviceDetail.setMake("make");
		regDeviceDetail.setModel("model");
		deviceServices.add(deviceobject);
		deviceProvider.setId("1234");
		Mockito.when(registeredDeviceRepository.findByCodeAndPurposeIgnoreCaseAndIsActiveIsTrue(Mockito.anyString(), Mockito.anyString())).thenReturn(regRegisteredDevice);
		Mockito.when(deviceServiceRepository.findBySwVersionAndIsActiveIsTrue(Mockito.anyString())).thenReturn(deviceServices);
		Mockito.when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString())).thenReturn(regDeviceDetail);
		deviceValidationService.validateDeviceProviders(validateDeviceDto);
	}
	
	@Test (expected = DeviceValidationException.class)
	public void validateDeviceProviderstest09() {
		ValidateDeviceDto validateDeviceDto = new ValidateDeviceDto();
		DigitalIdDto digitalId = new DigitalIdDto();
		RegRegisteredDevice regRegisteredDevice = new RegRegisteredDevice();
		RegDeviceDetail regDeviceDetail = new RegDeviceDetail();
		Partner deviceProvider = new Partner();
		List<RegSecureBiometricInterface> deviceServices = new ArrayList<RegSecureBiometricInterface>();
		RegSecureBiometricInterface deviceobject = new RegSecureBiometricInterface();
		digitalId.setDateTime("2021-01-29T13:13:04.923Z");
		digitalId.setDeviceSubType("subtype");
		digitalId.setDp("abcd");
		digitalId.setDpId("1234");
		digitalId.setMake("make");
		digitalId.setModel("model");
		digitalId.setSerialNo("123456");
		digitalId.setType("type");
		validateDeviceDto.setDeviceCode("1234");
		validateDeviceDto.setDeviceServiceVersion("1.0.0");
		validateDeviceDto.setDigitalId(digitalId);
		validateDeviceDto.setPurpose("Registration");
		regRegisteredDevice.setDeviceId("1234");
		regRegisteredDevice.setDeviceDetailId("1234");
		regRegisteredDevice.setStatusCode("REGISTERED");
		regRegisteredDevice.setSerialNo("123456");
		regDeviceDetail.setDeviceProviderId("1234");
		regDeviceDetail.setDeviceSubTypeCode("subtype");
		regDeviceDetail.setDeviceTypeCode("type");
		regDeviceDetail.setMake("make");
		regDeviceDetail.setModel("model");
		deviceServices.add(deviceobject);
		deviceProvider.setId("1234");
		Mockito.when(deviceProviderRepository.findByIdAndIsActiveIsTrue(Mockito.anyString())).thenReturn(deviceProvider);
		Mockito.when(deviceServiceRepository.findBySwVersionAndIsActiveIsTrue(Mockito.anyString())).thenReturn(deviceServices);
		Mockito.when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString())).thenReturn(regDeviceDetail);
		deviceValidationService.validateDeviceProviders(validateDeviceDto);
	}
	
	@Test (expected = DeviceValidationException.class)
	public void validateDeviceProviderstest10() {
		ValidateDeviceDto validateDeviceDto = new ValidateDeviceDto();
		DigitalIdDto digitalId = new DigitalIdDto();
		RegRegisteredDevice regRegisteredDevice = new RegRegisteredDevice();
		RegDeviceDetail regDeviceDetail = new RegDeviceDetail();
		Partner deviceProvider = new Partner();
		List<RegSecureBiometricInterface> deviceServices = new ArrayList<RegSecureBiometricInterface>();
		RegSecureBiometricInterface deviceobject = new RegSecureBiometricInterface();
		digitalId.setDateTime("2021-01-29T13:13:04.923Z");
		digitalId.setDeviceSubType("subtype");
		digitalId.setDp("abcd");
		digitalId.setDpId("1234");
		digitalId.setMake("make");
		digitalId.setModel("model");
		digitalId.setSerialNo("123456");
		digitalId.setType("type");
		validateDeviceDto.setDeviceCode("1234");
		validateDeviceDto.setDeviceServiceVersion("1.0.0");
		validateDeviceDto.setDigitalId(digitalId);
		validateDeviceDto.setPurpose("Registration");
		regRegisteredDevice.setDeviceId("1234");
		regRegisteredDevice.setDeviceDetailId("1234");
		regRegisteredDevice.setStatusCode("AUTH");
		regRegisteredDevice.setSerialNo("123456");
		regDeviceDetail.setDeviceProviderId("1234");
		regDeviceDetail.setDeviceSubTypeCode("subtype");
		regDeviceDetail.setDeviceTypeCode("type");
		regDeviceDetail.setMake("make");
		regDeviceDetail.setModel("model");
		deviceServices.add(deviceobject);
		deviceProvider.setId("1234");
		Mockito.when(registeredDeviceRepository.findByCodeAndPurposeIgnoreCaseAndIsActiveIsTrue(Mockito.anyString(), Mockito.anyString())).thenReturn(regRegisteredDevice);
		Mockito.when(deviceProviderRepository.findByIdAndIsActiveIsTrue(Mockito.anyString())).thenReturn(deviceProvider);
		Mockito.when(deviceServiceRepository.findBySwVersionAndIsActiveIsTrue(Mockito.anyString())).thenReturn(deviceServices);
		Mockito.when(deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(Mockito.anyString())).thenReturn(regDeviceDetail);
		deviceValidationService.validateDeviceProviders(validateDeviceDto);
	}

	
	
}
