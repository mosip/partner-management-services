package io.mosip.pms.test.regdevice.service.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.util.CryptoUtil;

import io.mosip.pms.common.constant.Purpose;
import io.mosip.pms.common.dto.Pagination;
import io.mosip.pms.common.dto.SearchFilter;
import io.mosip.pms.common.dto.SearchSort;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.common.util.PageUtils;
import io.mosip.pms.device.authdevice.entity.DeviceDetail;
import io.mosip.pms.device.dto.DeRegisterDeviceReqDto;
import io.mosip.pms.device.dto.DeviceData;
import io.mosip.pms.device.dto.DeviceInfo;
import io.mosip.pms.device.dto.DigitalId;
import io.mosip.pms.device.exception.DeviceValidationException;
import io.mosip.pms.device.regdevice.entity.RegDeviceDetail;
import io.mosip.pms.device.regdevice.entity.RegFoundationalTrustProvider;
import io.mosip.pms.device.regdevice.entity.RegRegisteredDevice;
import io.mosip.pms.device.regdevice.entity.RegRegisteredDeviceHistory;
import io.mosip.pms.device.regdevice.repository.RegDeviceDetailRepository;
import io.mosip.pms.device.regdevice.repository.RegFoundationalTrustProviderRepository;
import io.mosip.pms.device.regdevice.repository.RegRegisteredDeviceHistoryRepository;
import io.mosip.pms.device.regdevice.repository.RegRegisteredDeviceRepository;
import io.mosip.pms.device.regdevice.service.RegRegisteredDeviceService;
import io.mosip.pms.device.regdevice.service.impl.RegRegisteredDeviceServiceImpl;
import io.mosip.pms.device.request.dto.DeRegisterDevicePostDto;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.RegisteredDevicePostDto;
import io.mosip.pms.device.response.dto.DeviceDeRegisterResponse;
import io.mosip.pms.device.response.dto.RegisterDeviceResponse;
import io.mosip.pms.test.PartnerManagementServiceTest;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { PartnerManagementServiceTest.class })
@Transactional("regDevicePlatformTransactionManager")
public class RegRegisteredDeviceServiceTest {

	
	@Mock
	SearchHelper searchHelper;
	
	@Mock
	PageUtils pageUtils;
	
	@InjectMocks	
    RegRegisteredDeviceService registeredDeviceService=new  RegRegisteredDeviceServiceImpl();
	
	@Mock
	RegRegisteredDeviceRepository registeredDeviceRepository;

	@Mock
	RegRegisteredDeviceHistoryRepository registeredDeviceHistoryRepo;
	
	@Mock
	RegFoundationalTrustProviderRepository ftpRepo;

	@Mock
	Environment environment;

	@Mock
	RegDeviceDetailRepository deviceDetailRepository;
	
	@Mock
	private RestTemplate restTemplate;
	@Mock
	ObjectMapper objectMapper;
	@Autowired
	ObjectMapper mapper;

	RegDeviceDetail deviceDetail=new RegDeviceDetail();
	private RequestWrapper<DeviceSearchDto> deviceRequestDto;
	private RegisteredDevicePostDto registeredDevicePostDto = null;
	private DigitalId dig;
	private RegRegisteredDevice registeredDevice;
	private DeviceData device;
	private RegRegisteredDeviceHistory registeredDeviceHistory;
	private DeviceInfo deviceInfo;
	DeviceSearchDto deviceSearchDto = new DeviceSearchDto();
	Pagination pagination = new Pagination();
	SearchSort searchSort = new SearchSort();
	SearchFilter searchFilter = new SearchFilter();
	
	@Before
	public void setup() throws Exception {
		registeredDevice = new RegRegisteredDevice();
		registeredDevice.setCode("10001");
		registeredDevice.setStatusCode("Registered");
		
		//Search
				pagination.setPageFetch(10);
				pagination.setPageStart(0);
				searchSort.setSortField("model");
				searchSort.setSortType("asc");
				searchFilter.setColumnName("model");
				searchFilter.setFromValue("");
				searchFilter.setToValue("");
				searchFilter.setType("STARTSWITH");
				searchFilter.setValue("b");
				deviceSearchDto.setPurpose(Purpose.REGISTRATION.toString());
		
		registeredDevicePostDto = new RegisteredDevicePostDto();
		 dig = new DigitalId();
		dig.setDateTime(LocalDateTime.now(ZoneOffset.UTC));
		dig.setDeviceProvider("SYNCBYTE");
		dig.setDeviceProviderId("SYNCBYTE.MC01A");
		dig.setMake("MC01A");
		dig.setModel("SMIDCL");
		dig.setSerialNo("1801160991");
		dig.setDeviceSubType("Single");
		dig.setType("Fingerprint");
		registeredDevice.setDigitalId(mapper.writeValueAsString(dig));
		 device = new DeviceData();
		device.setDeviceId("70959dd5-e45f-438a-9ff8-9b263908e572");
		device.setFoundationalTrustProviderId("121");
		device.setPurpose("AUTH");
		 deviceInfo = new DeviceInfo();
		deviceInfo.setCertification("L0");
		List<String> deviceSubIds = new ArrayList<>();
		deviceSubIds.add("1");
		deviceSubIds.add("2");
		deviceInfo.setDeviceSubId(deviceSubIds);
		deviceInfo.setDeviceExpiry(LocalDateTime.now(ZoneOffset.UTC));
		deviceInfo.setFirmware("firmware");
		deviceInfo.setDigitalId(CryptoUtil.encodeBase64String(mapper.writeValueAsBytes(dig)));
		deviceInfo.setTimestamp(LocalDateTime.now(ZoneOffset.UTC));
		//device.setDeviceInfo(deviceInfo);
		registeredDevicePostDto.setDeviceData(CryptoUtil.encodeBase64String(mapper.writeValueAsBytes(device)));
		
		deviceDetail.setId("1234");
		Mockito.doReturn(deviceDetail).when(deviceDetailRepository).findByDeviceDetail(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString());
		Mockito.doReturn(registeredDevice).when(registeredDeviceRepository).findByCodeAndIsActiveIsTrue(Mockito.anyString());
			RegFoundationalTrustProvider dList=new RegFoundationalTrustProvider();
			dList.setId("121");
			when(ftpRepo.findByIdAndIsActiveTrue(Mockito.anyString()))
					.thenReturn(dList);
			when(registeredDeviceRepository.save(Mockito.any())).thenReturn(registeredDevice);
			when(registeredDeviceHistoryRepo.save(Mockito.any())).thenReturn(registeredDeviceHistory);
			when(registeredDeviceRepository.findByCodeAndIsActiveIsTrue(Mockito.anyString())).thenReturn(registeredDevice);
		ReflectionTestUtils.setField(registeredDeviceService,"registerDeviceTimeStamp","+5");
		ReflectionTestUtils.setField(registeredDeviceService,"activeProfile","mz");
		ReflectionTestUtils.setField(registeredDeviceService,"signUrl","https://dev.mosip.net/v1/keymanager/sign");
		Mockito.when(environment.getProperty("PASSWORDBASEDTOKENAPI")).thenReturn("https://dev.mosip.net/v1/authmanager/authenticate/useridPwd");
		
	}
		
	@Test
	public void regRegisteredDeviceSearchtest() throws Exception{
		objectMapper.writeValueAsString(deviceRequestDto);
		DeviceDetail device = new DeviceDetail();
		device.setId("1001");
		Mockito.doReturn(new PageImpl<>(Arrays.asList(device))).when(searchHelper).search(Mockito.any(),Mockito.any(),Mockito.any());
		registeredDeviceService.searchRegisteredDevice(RegRegisteredDevice.class, deviceSearchDto);
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=DeviceValidationException.class)
	public void createRegisteredDevicenegativetimevariant() throws Exception {
		ReflectionTestUtils.setField(registeredDeviceService,"registerDeviceTimeStamp","-5");
		RegisterDeviceResponse registerDeviceResponse = new RegisterDeviceResponse();
		registerDeviceResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		registerDeviceResponse.setStatus("Registered");
		registerDeviceResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		registerDeviceResponse.setEnv("mz");
		registerDeviceResponse.setDigitalId(mapper.writeValueAsString(dig));
//		HeaderRequest header = new HeaderRequest();
//		header.setAlg("RS256");
//		header.setType("JWS");
//		String headerString = mapper.writeValueAsString(header);
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(device).thenReturn(dig);
		//Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(mapper.writeValueAsString(dig)).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(headerString).thenReturn(mapper.writeValueAsString(registerDeviceResponse));
		//Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		registeredDeviceService.signedRegisteredDevice(registeredDevicePostDto);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=DeviceValidationException.class)
	public void createRegisteredDeviceExcessTimeOffset() throws Exception {
		deviceInfo.setTimestamp(LocalDateTime.MAX);
		//device.setDeviceInfo(deviceInfo);
		registeredDevicePostDto.setDeviceData(CryptoUtil.encodeBase64String(mapper.writeValueAsBytes(device)));
		
		RegisterDeviceResponse registerDeviceResponse = new RegisterDeviceResponse();
		registerDeviceResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		registerDeviceResponse.setStatus("Registered");
		registerDeviceResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		registerDeviceResponse.setEnv("mz");
		registerDeviceResponse.setDigitalId(mapper.writeValueAsString(dig));
//		HeaderRequest header = new HeaderRequest();
//		header.setAlg("RS256");
//		header.setType("JWS");
//		String headerString = mapper.writeValueAsString(header);
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(device).thenReturn(dig);
		//Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(mapper.writeValueAsString(dig)).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(headerString).thenReturn(mapper.writeValueAsString(registerDeviceResponse));
		//Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		registeredDeviceService.signedRegisteredDevice(registeredDevicePostDto);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=DeviceValidationException.class)
	public void createRegisteredDeviceinvaliddevice() throws Exception {
		Mockito.doReturn(null).when(deviceDetailRepository).findByDeviceDetail(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString());
		
		RegisterDeviceResponse registerDeviceResponse = new RegisterDeviceResponse();
		registerDeviceResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		registerDeviceResponse.setStatus("Registered");
		registerDeviceResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		registerDeviceResponse.setEnv("mz");
		registerDeviceResponse.setDigitalId(mapper.writeValueAsString(dig));
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(device).thenReturn(dig);
		registeredDeviceService.signedRegisteredDevice(registeredDevicePostDto);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=DeviceValidationException.class)
	public void createRegisteredDeviceftpnotfound() throws Exception {
		when(ftpRepo.findByIdAndIsActiveTrue(Mockito.anyString()))
		.thenReturn(null);
		RegisterDeviceResponse registerDeviceResponse = new RegisterDeviceResponse();
		registerDeviceResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		registerDeviceResponse.setStatus("Registered");
		registerDeviceResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		registerDeviceResponse.setEnv("mz");
		registerDeviceResponse.setDigitalId(mapper.writeValueAsString(dig));
//		HeaderRequest header = new HeaderRequest();
//		header.setAlg("RS256");
//		header.setType("JWS");
//		String headerString = mapper.writeValueAsString(header);
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(device).thenReturn(dig);
		//Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(mapper.writeValueAsString(dig)).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(headerString).thenReturn(mapper.writeValueAsString(registerDeviceResponse));
		//Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		registeredDeviceService.signedRegisteredDevice(registeredDevicePostDto);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=DeviceValidationException.class)
	public void createRegisteredDeviceserialnoalreadyexist() throws Exception {
		when(registeredDeviceRepository
				.findByDeviceDetailIdAndSerialNoAndIsActiveIsTrue(Mockito.anyString(),Mockito.anyString()))
		.thenReturn(registeredDevice);
		RegisterDeviceResponse registerDeviceResponse = new RegisterDeviceResponse();
		registerDeviceResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		registerDeviceResponse.setStatus("Registered");
		registerDeviceResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		registerDeviceResponse.setEnv("mz");
		registerDeviceResponse.setDigitalId(mapper.writeValueAsString(dig));
//		HeaderRequest header = new HeaderRequest();
//		header.setAlg("RS256");
//		header.setType("JWS");
//		String headerString = mapper.writeValueAsString(header);
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(device).thenReturn(dig);
		//Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(mapper.writeValueAsString(dig)).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(headerString).thenReturn(mapper.writeValueAsString(registerDeviceResponse));
		//Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		registeredDeviceService.signedRegisteredDevice(registeredDevicePostDto);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=DeviceValidationException.class)
	public void createRegisteredDeviceIOException() throws Exception {
		RegisterDeviceResponse registerDeviceResponse = new RegisterDeviceResponse();
		registerDeviceResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		registerDeviceResponse.setStatus("Registered");
		registerDeviceResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		registerDeviceResponse.setEnv("mz");
		registerDeviceResponse.setDigitalId(mapper.writeValueAsString(dig));
		
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(device).thenReturn(dig);
		Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenThrow(JsonProcessingException.class);
	//	Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		registeredDeviceService.signedRegisteredDevice(registeredDevicePostDto);
		
	}
	
	@Ignore
	@SuppressWarnings("unchecked")
	@Test(expected=DeviceValidationException.class)
	public void createRegisteredDevicesignerror() throws Exception {
		RegisterDeviceResponse registerDeviceResponse = new RegisterDeviceResponse();
		registerDeviceResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		registerDeviceResponse.setStatus("Registered");
		registerDeviceResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		registerDeviceResponse.setEnv("mz");
		registerDeviceResponse.setDigitalId(mapper.writeValueAsString(dig));
//		HeaderRequest header = new HeaderRequest();
//		header.setAlg("RS256");
//		header.setType("JWS");
//		String headerString = mapper.writeValueAsString(header);
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(device).thenReturn(dig);
		//Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(mapper.writeValueAsString(dig)).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(headerString).thenReturn(mapper.writeValueAsString(registerDeviceResponse));
		//Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenThrow(JsonParseException.class);
		registeredDeviceService.signedRegisteredDevice(registeredDevicePostDto);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=DeviceValidationException.class)
	public void createRegisteredDevicevalidationerror() throws Exception {
		device.setDeviceInfo(null);
		device.setPurpose(null);
		registeredDevicePostDto.setDeviceData(CryptoUtil.encodeBase64String(mapper.writeValueAsBytes(device)));
		RegisterDeviceResponse registerDeviceResponse = new RegisterDeviceResponse();
		registerDeviceResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		registerDeviceResponse.setStatus("Registered");
		registerDeviceResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		registerDeviceResponse.setEnv("mz");
		registerDeviceResponse.setDigitalId(mapper.writeValueAsString(dig));
//		HeaderRequest header = new HeaderRequest();
//		header.setAlg("RS256");
//		header.setType("JWS");
//		String headerString = mapper.writeValueAsString(header);
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(device).thenReturn(dig);
	//	Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(mapper.writeValueAsString(dig)).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(headerString).thenReturn(mapper.writeValueAsString(registerDeviceResponse));
	//	Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenThrow(JsonParseException.class);
		registeredDeviceService.signedRegisteredDevice(registeredDevicePostDto);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=DeviceValidationException.class)
	public void createRegisteredDeviceinfovalidationerror() throws Exception {
		dig.setMake("ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"
				+ "ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"
				+ "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd");
		RegisterDeviceResponse registerDeviceResponse = new RegisterDeviceResponse();
		registerDeviceResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		registerDeviceResponse.setStatus("Registered");
		registerDeviceResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		registerDeviceResponse.setEnv("mz");
		registerDeviceResponse.setDigitalId(mapper.writeValueAsString(dig));
//		HeaderRequest header = new HeaderRequest();
//		header.setAlg("RS256");
//		header.setType("JWS");
//		String headerString = mapper.writeValueAsString(header);
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(device).thenReturn(dig);
		//Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(mapper.writeValueAsString(dig)).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(headerString).thenReturn(mapper.writeValueAsString(registerDeviceResponse));
		//Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenThrow(JsonParseException.class);
		registeredDeviceService.signedRegisteredDevice(registeredDevicePostDto);
		
	}
	
	@Ignore
	@SuppressWarnings("unchecked")
	@Test
	public void createRegisteredDevice() throws Exception {
		RegisterDeviceResponse registerDeviceResponse = new RegisterDeviceResponse();
		registerDeviceResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		registerDeviceResponse.setStatus("Registered");
		registerDeviceResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		registerDeviceResponse.setEnv("mz");
		registerDeviceResponse.setDigitalId(mapper.writeValueAsString(dig));
//		HeaderRequest header = new HeaderRequest();
//		header.setAlg("RS256");
//		header.setType("JWS");
//		String headerString = mapper.writeValueAsString(header);
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(device).thenReturn(dig);
		//Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(mapper.writeValueAsString(dig)).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(headerString).thenReturn(mapper.writeValueAsString(registerDeviceResponse));
		//Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		String signedRegisteredDevice=registeredDeviceService.signedRegisteredDevice(registeredDevicePostDto);
		String[] split = signedRegisteredDevice.split("\\.");
		String srd= split[1];
		
		RegisterDeviceResponse dev=mapper.readValue(CryptoUtil.decodeBase64(srd), RegisterDeviceResponse.class);
		assertTrue(dev.getDeviceCode().equals(registerDeviceResponse.getDeviceCode()));
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=DeviceValidationException.class)
	public void deRegisteredDeviceNotFound() throws Exception {
		
		when(registeredDeviceRepository.findByCodeAndIsActiveIsTrue(Mockito.anyString())).thenReturn(null);
		
		DeRegisterDeviceReqDto deRegisterDeviceReqDto=new DeRegisterDeviceReqDto();
		deRegisterDeviceReqDto.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		deRegisterDeviceReqDto.setEnv("mz");
		DeRegisterDevicePostDto deRegisterDevicePostDto=new DeRegisterDevicePostDto();
		deRegisterDevicePostDto.setIsItForRegistrationDevice(false);
		deRegisterDevicePostDto.setDevice(CryptoUtil.encodeBase64String(mapper.writeValueAsBytes(deRegisterDeviceReqDto)));
//		HeaderRequest header = new HeaderRequest();
//		header.setAlg("RS256");
//		header.setType("JWS");
//		String headerString = mapper.writeValueAsString(header);
		DeviceDeRegisterResponse deviceDeRegisterResponse = new DeviceDeRegisterResponse();
		deviceDeRegisterResponse.setStatus("success");
		deviceDeRegisterResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		deviceDeRegisterResponse.setEnv("mz");
		deviceDeRegisterResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(deviceDeRegisterResponse));
		//Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(deRegisterDeviceReqDto);
		//Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(headerString).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(mapper.writeValueAsString(deviceDeRegisterResponse));
		registeredDeviceService.deRegisterDevice(deRegisterDevicePostDto);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=DeviceValidationException.class)
	public void deRegisteredDeviceinvalidinput() throws Exception {
		DeRegisterDeviceReqDto deRegisterDeviceReqDto=new DeRegisterDeviceReqDto();
		deRegisterDeviceReqDto.setDeviceCode("");
		deRegisterDeviceReqDto.setEnv("mz");
		DeRegisterDevicePostDto deRegisterDevicePostDto=new DeRegisterDevicePostDto();
		deRegisterDevicePostDto.setIsItForRegistrationDevice(false);
		deRegisterDevicePostDto.setDevice(CryptoUtil.encodeBase64String(mapper.writeValueAsBytes(deRegisterDeviceReqDto)));
//		HeaderRequest header = new HeaderRequest();
//		header.setAlg("RS256");
//		header.setType("JWS");
//		String headerString = mapper.writeValueAsString(header);
		DeviceDeRegisterResponse deviceDeRegisterResponse = new DeviceDeRegisterResponse();
		deviceDeRegisterResponse.setStatus("success");
		deviceDeRegisterResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		deviceDeRegisterResponse.setEnv("mz");
		deviceDeRegisterResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(deviceDeRegisterResponse));
		//Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(deRegisterDeviceReqDto);
		//Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(headerString).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(mapper.writeValueAsString(deviceDeRegisterResponse));
		registeredDeviceService.deRegisterDevice(deRegisterDevicePostDto);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=DeviceValidationException.class)
	public void deRegisteredDeviceinvalidinputenv() throws Exception {
		DeRegisterDeviceReqDto deRegisterDeviceReqDto=new DeRegisterDeviceReqDto();
		deRegisterDeviceReqDto.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		deRegisterDeviceReqDto.setEnv("mz3");
		DeRegisterDevicePostDto deRegisterDevicePostDto=new DeRegisterDevicePostDto();
		deRegisterDevicePostDto.setIsItForRegistrationDevice(false);
		deRegisterDevicePostDto.setDevice(CryptoUtil.encodeBase64String(mapper.writeValueAsBytes(deRegisterDeviceReqDto)));
//		HeaderRequest header = new HeaderRequest();
//		header.setAlg("RS256");
//		header.setType("JWS");
//		String headerString = mapper.writeValueAsString(header);
		DeviceDeRegisterResponse deviceDeRegisterResponse = new DeviceDeRegisterResponse();
		deviceDeRegisterResponse.setStatus("success");
		deviceDeRegisterResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		deviceDeRegisterResponse.setEnv("mz");
		deviceDeRegisterResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(deviceDeRegisterResponse));
		//Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(deRegisterDeviceReqDto);
		//Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(headerString).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(mapper.writeValueAsString(deviceDeRegisterResponse));
		registeredDeviceService.deRegisterDevice(deRegisterDevicePostDto);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=DeviceValidationException.class)
	public void deRegisteredDeviceinvalidinputCode() throws Exception {
		DeRegisterDeviceReqDto deRegisterDeviceReqDto=new DeRegisterDeviceReqDto();
		deRegisterDeviceReqDto.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572"
				+ "ghgchghggggggggggggggggggggggggggggggggggggggggggggggggggggggg"
				+ "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		deRegisterDeviceReqDto.setEnv("mz");
		DeRegisterDevicePostDto deRegisterDevicePostDto=new DeRegisterDevicePostDto();
		deRegisterDevicePostDto.setIsItForRegistrationDevice(false);
		deRegisterDevicePostDto.setDevice(CryptoUtil.encodeBase64String(mapper.writeValueAsBytes(deRegisterDeviceReqDto)));
//		HeaderRequest header = new HeaderRequest();
//		header.setAlg("RS256");
//		header.setType("JWS");
//		String headerString = mapper.writeValueAsString(header);
		DeviceDeRegisterResponse deviceDeRegisterResponse = new DeviceDeRegisterResponse();
		deviceDeRegisterResponse.setStatus("success");
		deviceDeRegisterResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		deviceDeRegisterResponse.setEnv("mz");
		deviceDeRegisterResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(deviceDeRegisterResponse));
	//	Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(deRegisterDeviceReqDto);
	//	Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(headerString).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(mapper.writeValueAsString(deviceDeRegisterResponse));
		registeredDeviceService.deRegisterDevice(deRegisterDevicePostDto);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=DeviceValidationException.class)
	public void deRegisteredDeviceRetired() throws Exception {
		registeredDevice = new RegRegisteredDevice();
		registeredDevice.setCode("10001");
		registeredDevice.setStatusCode("Retired");
		when(registeredDeviceRepository.findByCodeAndIsActiveIsTrue(Mockito.anyString())).thenReturn(registeredDevice);
		
		DeRegisterDeviceReqDto deRegisterDeviceReqDto=new DeRegisterDeviceReqDto();
		deRegisterDeviceReqDto.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		deRegisterDeviceReqDto.setEnv("mz");
		DeRegisterDevicePostDto deRegisterDevicePostDto=new DeRegisterDevicePostDto();
		deRegisterDevicePostDto.setIsItForRegistrationDevice(false);
		deRegisterDevicePostDto.setDevice(CryptoUtil.encodeBase64String(mapper.writeValueAsBytes(deRegisterDeviceReqDto)));
//		HeaderRequest header = new HeaderRequest();
//		header.setAlg("RS256");
//		header.setType("JWS");
//		String headerString = mapper.writeValueAsString(header);
		DeviceDeRegisterResponse deviceDeRegisterResponse = new DeviceDeRegisterResponse();
		deviceDeRegisterResponse.setStatus("success");
		deviceDeRegisterResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		deviceDeRegisterResponse.setEnv("mz");
		deviceDeRegisterResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(deviceDeRegisterResponse));
		//Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(deRegisterDeviceReqDto);
		//Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(headerString).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(mapper.writeValueAsString(deviceDeRegisterResponse));
		registeredDeviceService.deRegisterDevice(deRegisterDevicePostDto);
		
	}	
	
	@Ignore
	@SuppressWarnings("unchecked")
	@Test(expected=DeviceValidationException.class)
	public void deRegisteredDeviceIOException() throws Exception {
		DeRegisterDeviceReqDto deRegisterDeviceReqDto=new DeRegisterDeviceReqDto();
		deRegisterDeviceReqDto.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		deRegisterDeviceReqDto.setEnv("mz");
		DeRegisterDevicePostDto deRegisterDevicePostDto=new DeRegisterDevicePostDto();
		deRegisterDevicePostDto.setIsItForRegistrationDevice(false);
		deRegisterDevicePostDto.setDevice(CryptoUtil.encodeBase64String(mapper.writeValueAsBytes(deRegisterDeviceReqDto)));
//		HeaderRequest header = new HeaderRequest();
//		header.setAlg("RS256");
//		header.setType("JWS");
//		String headerString = mapper.writeValueAsString(header);
		DeviceDeRegisterResponse deviceDeRegisterResponse = new DeviceDeRegisterResponse();
		deviceDeRegisterResponse.setStatus("success");
		deviceDeRegisterResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		deviceDeRegisterResponse.setEnv("mz");
		deviceDeRegisterResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(deviceDeRegisterResponse));
		//Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(deRegisterDeviceReqDto);
		//Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(headerString).thenReturn(mapper.writeValueAsString(signResponseDto)).thenThrow(JsonProcessingException.class);
		registeredDeviceService.deRegisterDevice(deRegisterDevicePostDto);
	}
	
	@Ignore
	@SuppressWarnings("unchecked")
	@Test
	public void deRegisteredDevice() throws Exception {
		DeRegisterDeviceReqDto deRegisterDeviceReqDto=new DeRegisterDeviceReqDto();
		deRegisterDeviceReqDto.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		deRegisterDeviceReqDto.setEnv("mz");
		DeRegisterDevicePostDto deRegisterDevicePostDto=new DeRegisterDevicePostDto();
		deRegisterDevicePostDto.setIsItForRegistrationDevice(false);
		deRegisterDevicePostDto.setDevice(CryptoUtil.encodeBase64String(mapper.writeValueAsBytes(deRegisterDeviceReqDto)));
//		HeaderRequest header = new HeaderRequest();
//		header.setAlg("RS256");
//		header.setType("JWS");
//		String headerString = mapper.writeValueAsString(header);
		DeviceDeRegisterResponse deviceDeRegisterResponse = new DeviceDeRegisterResponse();
		deviceDeRegisterResponse.setStatus("success");
		deviceDeRegisterResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		deviceDeRegisterResponse.setEnv("mz");
		deviceDeRegisterResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(deviceDeRegisterResponse));
	//	Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(deRegisterDeviceReqDto);
	//	Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(headerString).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(mapper.writeValueAsString(deviceDeRegisterResponse));
		String signedRegisteredDevice=registeredDeviceService.deRegisterDevice(deRegisterDevicePostDto);
		String[] split = signedRegisteredDevice.split("\\.");
		String srd= split[1];
		
		DeviceDeRegisterResponse dev=mapper.readValue(CryptoUtil.decodeBase64(srd), DeviceDeRegisterResponse.class);
		assertTrue(dev.getDeviceCode().equals(deviceDeRegisterResponse.getDeviceCode()));
	}
}
