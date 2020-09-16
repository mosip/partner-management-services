package io.mosip.pmp.authdevice.test.service;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.crypto.jce.core.CryptoCore;
import io.mosip.pmp.authdevice.dto.DeRegisterDevicePostDto;
import io.mosip.pmp.authdevice.dto.DeRegisterDeviceReqDto;
import io.mosip.pmp.authdevice.dto.DeviceData;
import io.mosip.pmp.authdevice.dto.DeviceDeRegisterResponse;
import io.mosip.pmp.authdevice.dto.DeviceInfo;
import io.mosip.pmp.authdevice.dto.DigitalId;
import io.mosip.pmp.authdevice.dto.RegisterDeviceResponse;
import io.mosip.pmp.authdevice.dto.RegisteredDevicePostDto;
import io.mosip.pmp.authdevice.dto.SignResponseDto;
import io.mosip.pmp.authdevice.entity.DeviceDetail;
import io.mosip.pmp.authdevice.entity.FoundationalTrustProvider;
import io.mosip.pmp.authdevice.entity.RegisteredDevice;
import io.mosip.pmp.authdevice.entity.RegisteredDeviceHistory;
import io.mosip.pmp.authdevice.exception.AuthDeviceServiceException;
import io.mosip.pmp.authdevice.exception.RequestException;
import io.mosip.pmp.authdevice.exception.ValidationException;
import io.mosip.pmp.authdevice.repository.DeviceDetailRepository;
import io.mosip.pmp.authdevice.repository.FoundationalTrustProviderRepository;
import io.mosip.pmp.authdevice.repository.RegisteredDeviceHistoryRepository;
import io.mosip.pmp.authdevice.repository.RegisteredDeviceRepository;
import io.mosip.pmp.authdevice.service.RegisteredDeviceService;
import io.mosip.pmp.authdevice.service.impl.RegisteredDeviceServiceImpl;
import io.mosip.pmp.authdevice.util.HeaderRequest;
import io.mosip.pmp.keycloak.impl.AccessTokenResponse;
import io.mosip.pmp.partner.PartnerserviceApplication;
import io.mosip.pmp.partner.core.ResponseWrapper;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { PartnerserviceApplication.class })
@AutoConfigureMockMvc
@EnableWebMvc
public class RegisteredDeviceServiceTest {

	@InjectMocks	
    RegisteredDeviceService registeredDeviceService=new  RegisteredDeviceServiceImpl();
	
	@Mock
	RegisteredDeviceRepository registeredDeviceRepository;

	@Mock
	RegisteredDeviceHistoryRepository registeredDeviceHistoryRepo;
	
	@Mock
	FoundationalTrustProviderRepository ftpRepo;

	@Mock
	Environment environment;

	@Mock
	DeviceDetailRepository deviceDetailRepository;
	
	@Mock    
	private CryptoCore cryptoCore;
	@Mock
	private RestTemplate restTemplate;
	@Mock
	ObjectMapper objectMapper;
	@Autowired
	ObjectMapper mapper;

	DeviceDetail deviceDetail=new DeviceDetail();
	
	private RegisteredDevicePostDto registeredDevicePostDto = null;
	private DigitalId dig;
	private RegisteredDevice registeredDevice;
	private DeviceData device;
	private RegisteredDeviceHistory registeredDeviceHistory;
	private DeviceInfo deviceInfo;
	private ResponseWrapper<SignResponseDto> responseWrapper ;
	private SignResponseDto signResponseDto;
	@SuppressWarnings("unchecked")
	@Before
	public void setup() throws Exception {
		registeredDevice = new RegisteredDevice();
		registeredDevice.setCode("10001");
		registeredDevice.setStatusCode("Registered");
		
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
		device.setHotlisted(true);
		 deviceInfo = new DeviceInfo();
		deviceInfo.setCertification("L0");
		deviceInfo.setDeviceSubId("1");
		deviceInfo.setDeviceExpiry(LocalDateTime.now(ZoneOffset.UTC));
		deviceInfo.setFirmware("firmware");
		deviceInfo.setDigitalId(CryptoUtil.encodeBase64String(mapper.writeValueAsBytes(dig)));
		deviceInfo.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		device.setDeviceInfo(deviceInfo);
		registeredDevicePostDto.setDeviceData(CryptoUtil.encodeBase64String(mapper.writeValueAsBytes(device)));
		
		deviceDetail.setId("1234");
		Mockito.doReturn(deviceDetail).when(deviceDetailRepository).findByDeviceDetail(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString());
		Mockito.doReturn(registeredDevice).when(registeredDeviceRepository).findByCodeAndIsActiveIsTrue(Mockito.anyString());
			FoundationalTrustProvider dList=new FoundationalTrustProvider();
			dList.setId("121");
			when(ftpRepo.findByIdAndIsActiveTrue(Mockito.anyString()))
					.thenReturn(dList);
			when(registeredDeviceRepository.save(Mockito.any())).thenReturn(registeredDevice);
			when(registeredDeviceHistoryRepo.save(Mockito.any())).thenReturn(registeredDeviceHistory);
			when(registeredDeviceRepository.findByCodeAndIsActiveIsTrue(Mockito.anyString())).thenReturn(registeredDevice);
			 signResponseDto = new SignResponseDto();
			signResponseDto.setTimestamp(LocalDateTime.now());
			signResponseDto.setSignature(
					".TGlqZ0lPaUU0MTVHTHEwekxlSkZMb2I4MktTeHdnazc0YkgzZUdwTE9tdm4xVFNYUS8rZHFuemZoM2x2cjZhOVRHb1ZzYjFIeEJqRFdpOStWNlV5THBJVm82VlVwVnppaCtVRno4c0xDSjJsUWJWajhKdm5ybDdPWlpTQWZwVHZnYkxsZ3pNV3FDR0JrVzdITnFTRHVVZFRPblE3azc5RHlQam5sSjlHQkdFaWpMRERUSVNDKzUyT2JpdjdZemUxWVBjbkl4MGNtYVI4bWF2bmYvN09qdmk5VFZQQlppYkx3eVlFZDgvQnJ4OVpReWlXUmJ5bVNIUGo2L1dqVFBsSnJQZGdXTEVONVhrdWFLQldWN1BrR1R2d3Fydit4RjRtc3FvdElGTGs0cnZ3R0JYTTJ3K2pCeUhNT3c1SmpTMXUxNFh1ejhTK3N0eTMrNGNXcVZ0bVZRPT0=");

			 responseWrapper = new ResponseWrapper<>();
			responseWrapper.setResponse(signResponseDto);
			responseWrapper.setResponsetime(LocalDateTime.now());
			String response = mapper.writeValueAsString(responseWrapper);
			when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class),
					Mockito.any(Class.class))).thenReturn(new ResponseEntity<String>(response, HttpStatus.OK));
			Properties prop=new Properties();
			prop.setProperty("token", "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJScUZSaS1ZcmVzbDBfR2lINDdMZ2Q1ckQ0azhRbXBObXZvTFZCM3hYMWZFIn0.eyJqdGkiOiJhYmVmZmI0Mi03OWZkLTQ3OGMtOTA0Zi1jMWU0NmVlYmRiZTEiLCJleHAiOjE1OTkwNjY2MzAsIm5iZiI6MCwiaWF0IjoxNTk5MDMwNjMwLCJpc3MiOiJodHRwczovL2Rldi5tb3NpcC5uZXQva2V5Y2xvYWsvYXV0aC9yZWFsbXMvbW9zaXAiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiNDlkN2JiZTUtYTFjNC00NTYxLWE0YmYtMjY0NWIxNDBmZmRkIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibW9zaXAtcmVncHJvYy1jbGllbnQiLCJhdXRoX3RpbWUiOjAsInNlc3Npb25fc3RhdGUiOiJlNzU1MTk4Yi0wY2IzLTQwYzgtODllYi0xMTM2MDA1NzQxYzMiLCJhY3IiOiIxIiwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIlJFR0lTVFJBVElPTl9QUk9DRVNTT1IiLCJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsibW9zaXAtcmVncHJvYy1jbGllbnQiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImNsaWVudElkIjoibW9zaXAtcmVncHJvYy1jbGllbnQiLCJjbGllbnRIb3N0IjoiMTAuMjQ0LjkuOCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LW1vc2lwLXJlZ3Byb2MtY2xpZW50IiwiY2xpZW50QWRkcmVzcyI6IjEwLjI0NC45LjgifQ.dpZv9EtF_QRm23q14wwRHeEoNGmvwAdY85eTAkKEmLlOvd1yQ7WTZsPGEPsiWn9ii8IqgqJWH_DuyEDPPjoKE2KBf_5csuvkzkSci3xuMG9KrivTsLufXhveDPzsiMDCoJHqvDxnE91Sb-RNQVCdG78AE7mwQuQBdLfz-Q8V_8uQWtW7lruEu4NUTjcpi7zukuxjTwQnJswzwSOv8zxR80jKSdoHdAr1g8t_oC7nrsJ2mfhZiG-kncA1V9F1FonrwUdeoWimpdHBOHqPlMQrBw9di2ZhD_Y0OqI5u1jbiTW1ecnrUtEKLllX2CEfiv333T3WWUftk5QKGDYjEN1LWw ");
		System.setProperties(prop);
		AccessTokenResponse tok=new AccessTokenResponse();
		tok.setAccess_token("eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJScUZSaS1ZcmVzbDBfR2lINDdMZ2Q1ckQ0azhRbXBObXZvTFZCM3hYMWZFIn0");
		ResponseEntity<AccessTokenResponse> responsetoken=new ResponseEntity<>(tok,HttpStatus.OK);
		when(restTemplate.postForEntity(
				Mockito.anyString(), Mockito.any(HttpEntity.class), Mockito.any(Class.class))).thenReturn(responsetoken);
		ReflectionTestUtils.setField(registeredDeviceService,"registerDeviceTimeStamp","+5");
		ReflectionTestUtils.setField(registeredDeviceService,"activeProfile","mz");
		ReflectionTestUtils.setField(registeredDeviceService,"signUrl","https://dev.mosip.net/v1/keymanager/sign");
		Mockito.when(environment.getProperty("PASSWORDBASEDTOKENAPI")).thenReturn("https://dev.mosip.net/v1/authmanager/authenticate/useridPwd");
		
	}
		
	@SuppressWarnings("unchecked")
	@Test(expected=AuthDeviceServiceException.class)
	public void createRegisteredDevicenegativetimevariant() throws Exception {
		ReflectionTestUtils.setField(registeredDeviceService,"registerDeviceTimeStamp","-5");
		RegisterDeviceResponse registerDeviceResponse = new RegisterDeviceResponse();
		registerDeviceResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		registerDeviceResponse.setStatus("Registered");
		registerDeviceResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		registerDeviceResponse.setEnv("mz");
		registerDeviceResponse.setDigitalId(mapper.writeValueAsString(dig));
		HeaderRequest header = new HeaderRequest();
		header.setAlg("RS256");
		header.setType("JWS");
		String headerString = mapper.writeValueAsString(header);
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(device).thenReturn(dig);
		Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(mapper.writeValueAsString(dig)).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(headerString).thenReturn(mapper.writeValueAsString(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		registeredDeviceService.signedRegisteredDevice(registeredDevicePostDto);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=AuthDeviceServiceException.class)
	public void createRegisteredDeviceExcessTimeOffset() throws Exception {
		deviceInfo.setTimeStamp(LocalDateTime.MAX);
		device.setDeviceInfo(deviceInfo);
		registeredDevicePostDto.setDeviceData(CryptoUtil.encodeBase64String(mapper.writeValueAsBytes(device)));
		
		RegisterDeviceResponse registerDeviceResponse = new RegisterDeviceResponse();
		registerDeviceResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		registerDeviceResponse.setStatus("Registered");
		registerDeviceResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		registerDeviceResponse.setEnv("mz");
		registerDeviceResponse.setDigitalId(mapper.writeValueAsString(dig));
		HeaderRequest header = new HeaderRequest();
		header.setAlg("RS256");
		header.setType("JWS");
		String headerString = mapper.writeValueAsString(header);
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(device).thenReturn(dig);
		Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(mapper.writeValueAsString(dig)).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(headerString).thenReturn(mapper.writeValueAsString(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		registeredDeviceService.signedRegisteredDevice(registeredDevicePostDto);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=RequestException.class)
	public void createRegisteredDeviceinvaliddevice() throws Exception {
		Mockito.doReturn(null).when(deviceDetailRepository).findByDeviceDetail(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString());
		
		RegisterDeviceResponse registerDeviceResponse = new RegisterDeviceResponse();
		registerDeviceResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		registerDeviceResponse.setStatus("Registered");
		registerDeviceResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		registerDeviceResponse.setEnv("mz");
		registerDeviceResponse.setDigitalId(mapper.writeValueAsString(dig));
		HeaderRequest header = new HeaderRequest();
		header.setAlg("RS256");
		header.setType("JWS");
		String headerString = mapper.writeValueAsString(header);
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(device).thenReturn(dig);
		Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(mapper.writeValueAsString(dig)).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(headerString).thenReturn(mapper.writeValueAsString(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		registeredDeviceService.signedRegisteredDevice(registeredDevicePostDto);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=RequestException.class)
	public void createRegisteredDeviceftpnotfound() throws Exception {
		when(ftpRepo.findByIdAndIsActiveTrue(Mockito.anyString()))
		.thenReturn(null);
		RegisterDeviceResponse registerDeviceResponse = new RegisterDeviceResponse();
		registerDeviceResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		registerDeviceResponse.setStatus("Registered");
		registerDeviceResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		registerDeviceResponse.setEnv("mz");
		registerDeviceResponse.setDigitalId(mapper.writeValueAsString(dig));
		HeaderRequest header = new HeaderRequest();
		header.setAlg("RS256");
		header.setType("JWS");
		String headerString = mapper.writeValueAsString(header);
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(device).thenReturn(dig);
		Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(mapper.writeValueAsString(dig)).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(headerString).thenReturn(mapper.writeValueAsString(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		registeredDeviceService.signedRegisteredDevice(registeredDevicePostDto);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=RequestException.class)
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
		HeaderRequest header = new HeaderRequest();
		header.setAlg("RS256");
		header.setType("JWS");
		String headerString = mapper.writeValueAsString(header);
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(device).thenReturn(dig);
		Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(mapper.writeValueAsString(dig)).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(headerString).thenReturn(mapper.writeValueAsString(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		registeredDeviceService.signedRegisteredDevice(registeredDevicePostDto);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=AuthDeviceServiceException.class)
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
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		registeredDeviceService.signedRegisteredDevice(registeredDevicePostDto);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=AuthDeviceServiceException.class)
	public void createRegisteredDevicesignerror() throws Exception {
		RegisterDeviceResponse registerDeviceResponse = new RegisterDeviceResponse();
		registerDeviceResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		registerDeviceResponse.setStatus("Registered");
		registerDeviceResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		registerDeviceResponse.setEnv("mz");
		registerDeviceResponse.setDigitalId(mapper.writeValueAsString(dig));
		HeaderRequest header = new HeaderRequest();
		header.setAlg("RS256");
		header.setType("JWS");
		String headerString = mapper.writeValueAsString(header);
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(device).thenReturn(dig);
		Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(mapper.writeValueAsString(dig)).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(headerString).thenReturn(mapper.writeValueAsString(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenThrow(JsonParseException.class);
		registeredDeviceService.signedRegisteredDevice(registeredDevicePostDto);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=ValidationException.class)
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
		HeaderRequest header = new HeaderRequest();
		header.setAlg("RS256");
		header.setType("JWS");
		String headerString = mapper.writeValueAsString(header);
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(device).thenReturn(dig);
		Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(mapper.writeValueAsString(dig)).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(headerString).thenReturn(mapper.writeValueAsString(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenThrow(JsonParseException.class);
		registeredDeviceService.signedRegisteredDevice(registeredDevicePostDto);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=ValidationException.class)
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
		HeaderRequest header = new HeaderRequest();
		header.setAlg("RS256");
		header.setType("JWS");
		String headerString = mapper.writeValueAsString(header);
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(device).thenReturn(dig);
		Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(mapper.writeValueAsString(dig)).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(headerString).thenReturn(mapper.writeValueAsString(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenThrow(JsonParseException.class);
		registeredDeviceService.signedRegisteredDevice(registeredDevicePostDto);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void createRegisteredDevice() throws Exception {
		RegisterDeviceResponse registerDeviceResponse = new RegisterDeviceResponse();
		registerDeviceResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		registerDeviceResponse.setStatus("Registered");
		registerDeviceResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		registerDeviceResponse.setEnv("mz");
		registerDeviceResponse.setDigitalId(mapper.writeValueAsString(dig));
		HeaderRequest header = new HeaderRequest();
		header.setAlg("RS256");
		header.setType("JWS");
		String headerString = mapper.writeValueAsString(header);
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(device).thenReturn(dig);
		Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(mapper.writeValueAsString(dig)).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(headerString).thenReturn(mapper.writeValueAsString(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		String signedRegisteredDevice=registeredDeviceService.signedRegisteredDevice(registeredDevicePostDto);
		String[] split = signedRegisteredDevice.split("\\.");
		String srd= split[1];
		
		RegisterDeviceResponse dev=mapper.readValue(CryptoUtil.decodeBase64(srd), RegisterDeviceResponse.class);
		assertTrue(dev.getDeviceCode().equals(registerDeviceResponse.getDeviceCode()));
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=RequestException.class)
	public void deRegisteredDeviceNotFound() throws Exception {
		
		when(registeredDeviceRepository.findByCodeAndIsActiveIsTrue(Mockito.anyString())).thenReturn(null);
		
		DeRegisterDeviceReqDto deRegisterDeviceReqDto=new DeRegisterDeviceReqDto();
		deRegisterDeviceReqDto.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		deRegisterDeviceReqDto.setEnv("mz");
		DeRegisterDevicePostDto deRegisterDevicePostDto=new DeRegisterDevicePostDto();
		deRegisterDevicePostDto.setIsItForRegistrationDevice(false);
		deRegisterDevicePostDto.setDevice(CryptoUtil.encodeBase64String(mapper.writeValueAsBytes(deRegisterDeviceReqDto)));
		HeaderRequest header = new HeaderRequest();
		header.setAlg("RS256");
		header.setType("JWS");
		String headerString = mapper.writeValueAsString(header);
		DeviceDeRegisterResponse deviceDeRegisterResponse = new DeviceDeRegisterResponse();
		deviceDeRegisterResponse.setStatus("success");
		deviceDeRegisterResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		deviceDeRegisterResponse.setEnv("mz");
		deviceDeRegisterResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(deviceDeRegisterResponse));
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(deRegisterDeviceReqDto);
		Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(headerString).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(mapper.writeValueAsString(deviceDeRegisterResponse));
		registeredDeviceService.deRegisterDevice(deRegisterDevicePostDto);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=RequestException.class)
	public void deRegisteredDeviceinvalidinput() throws Exception {
		DeRegisterDeviceReqDto deRegisterDeviceReqDto=new DeRegisterDeviceReqDto();
		deRegisterDeviceReqDto.setDeviceCode("");
		deRegisterDeviceReqDto.setEnv("mz");
		DeRegisterDevicePostDto deRegisterDevicePostDto=new DeRegisterDevicePostDto();
		deRegisterDevicePostDto.setIsItForRegistrationDevice(false);
		deRegisterDevicePostDto.setDevice(CryptoUtil.encodeBase64String(mapper.writeValueAsBytes(deRegisterDeviceReqDto)));
		HeaderRequest header = new HeaderRequest();
		header.setAlg("RS256");
		header.setType("JWS");
		String headerString = mapper.writeValueAsString(header);
		DeviceDeRegisterResponse deviceDeRegisterResponse = new DeviceDeRegisterResponse();
		deviceDeRegisterResponse.setStatus("success");
		deviceDeRegisterResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		deviceDeRegisterResponse.setEnv("mz");
		deviceDeRegisterResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(deviceDeRegisterResponse));
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(deRegisterDeviceReqDto);
		Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(headerString).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(mapper.writeValueAsString(deviceDeRegisterResponse));
		registeredDeviceService.deRegisterDevice(deRegisterDevicePostDto);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=RequestException.class)
	public void deRegisteredDeviceinvalidinputenv() throws Exception {
		DeRegisterDeviceReqDto deRegisterDeviceReqDto=new DeRegisterDeviceReqDto();
		deRegisterDeviceReqDto.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		deRegisterDeviceReqDto.setEnv("mz3");
		DeRegisterDevicePostDto deRegisterDevicePostDto=new DeRegisterDevicePostDto();
		deRegisterDevicePostDto.setIsItForRegistrationDevice(false);
		deRegisterDevicePostDto.setDevice(CryptoUtil.encodeBase64String(mapper.writeValueAsBytes(deRegisterDeviceReqDto)));
		HeaderRequest header = new HeaderRequest();
		header.setAlg("RS256");
		header.setType("JWS");
		String headerString = mapper.writeValueAsString(header);
		DeviceDeRegisterResponse deviceDeRegisterResponse = new DeviceDeRegisterResponse();
		deviceDeRegisterResponse.setStatus("success");
		deviceDeRegisterResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		deviceDeRegisterResponse.setEnv("mz");
		deviceDeRegisterResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(deviceDeRegisterResponse));
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(deRegisterDeviceReqDto);
		Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(headerString).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(mapper.writeValueAsString(deviceDeRegisterResponse));
		registeredDeviceService.deRegisterDevice(deRegisterDevicePostDto);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=RequestException.class)
	public void deRegisteredDeviceinvalidinputCode() throws Exception {
		DeRegisterDeviceReqDto deRegisterDeviceReqDto=new DeRegisterDeviceReqDto();
		deRegisterDeviceReqDto.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572"
				+ "ghgchghggggggggggggggggggggggggggggggggggggggggggggggggggggggg"
				+ "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		deRegisterDeviceReqDto.setEnv("mz");
		DeRegisterDevicePostDto deRegisterDevicePostDto=new DeRegisterDevicePostDto();
		deRegisterDevicePostDto.setIsItForRegistrationDevice(false);
		deRegisterDevicePostDto.setDevice(CryptoUtil.encodeBase64String(mapper.writeValueAsBytes(deRegisterDeviceReqDto)));
		HeaderRequest header = new HeaderRequest();
		header.setAlg("RS256");
		header.setType("JWS");
		String headerString = mapper.writeValueAsString(header);
		DeviceDeRegisterResponse deviceDeRegisterResponse = new DeviceDeRegisterResponse();
		deviceDeRegisterResponse.setStatus("success");
		deviceDeRegisterResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		deviceDeRegisterResponse.setEnv("mz");
		deviceDeRegisterResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(deviceDeRegisterResponse));
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(deRegisterDeviceReqDto);
		Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(headerString).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(mapper.writeValueAsString(deviceDeRegisterResponse));
		registeredDeviceService.deRegisterDevice(deRegisterDevicePostDto);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=AuthDeviceServiceException.class)
	public void deRegisteredDeviceRetired() throws Exception {
		registeredDevice = new RegisteredDevice();
		registeredDevice.setCode("10001");
		registeredDevice.setStatusCode("Retired");
		when(registeredDeviceRepository.findByCodeAndIsActiveIsTrue(Mockito.anyString())).thenReturn(registeredDevice);
		
		DeRegisterDeviceReqDto deRegisterDeviceReqDto=new DeRegisterDeviceReqDto();
		deRegisterDeviceReqDto.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		deRegisterDeviceReqDto.setEnv("mz");
		DeRegisterDevicePostDto deRegisterDevicePostDto=new DeRegisterDevicePostDto();
		deRegisterDevicePostDto.setIsItForRegistrationDevice(false);
		deRegisterDevicePostDto.setDevice(CryptoUtil.encodeBase64String(mapper.writeValueAsBytes(deRegisterDeviceReqDto)));
		HeaderRequest header = new HeaderRequest();
		header.setAlg("RS256");
		header.setType("JWS");
		String headerString = mapper.writeValueAsString(header);
		DeviceDeRegisterResponse deviceDeRegisterResponse = new DeviceDeRegisterResponse();
		deviceDeRegisterResponse.setStatus("success");
		deviceDeRegisterResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		deviceDeRegisterResponse.setEnv("mz");
		deviceDeRegisterResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(deviceDeRegisterResponse));
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(deRegisterDeviceReqDto);
		Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(headerString).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(mapper.writeValueAsString(deviceDeRegisterResponse));
		registeredDeviceService.deRegisterDevice(deRegisterDevicePostDto);
		
	}
	
	
	
	@SuppressWarnings("unchecked")
	@Test(expected=AuthDeviceServiceException.class)
	public void deRegisteredDeviceIOException() throws Exception {
		DeRegisterDeviceReqDto deRegisterDeviceReqDto=new DeRegisterDeviceReqDto();
		deRegisterDeviceReqDto.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		deRegisterDeviceReqDto.setEnv("mz");
		DeRegisterDevicePostDto deRegisterDevicePostDto=new DeRegisterDevicePostDto();
		deRegisterDevicePostDto.setIsItForRegistrationDevice(false);
		deRegisterDevicePostDto.setDevice(CryptoUtil.encodeBase64String(mapper.writeValueAsBytes(deRegisterDeviceReqDto)));
		HeaderRequest header = new HeaderRequest();
		header.setAlg("RS256");
		header.setType("JWS");
		String headerString = mapper.writeValueAsString(header);
		DeviceDeRegisterResponse deviceDeRegisterResponse = new DeviceDeRegisterResponse();
		deviceDeRegisterResponse.setStatus("success");
		deviceDeRegisterResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		deviceDeRegisterResponse.setEnv("mz");
		deviceDeRegisterResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(deviceDeRegisterResponse));
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(deRegisterDeviceReqDto);
		Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(headerString).thenReturn(mapper.writeValueAsString(signResponseDto)).thenThrow(JsonProcessingException.class);
		registeredDeviceService.deRegisterDevice(deRegisterDevicePostDto);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void deRegisteredDevice() throws Exception {
		DeRegisterDeviceReqDto deRegisterDeviceReqDto=new DeRegisterDeviceReqDto();
		deRegisterDeviceReqDto.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		deRegisterDeviceReqDto.setEnv("mz");
		DeRegisterDevicePostDto deRegisterDevicePostDto=new DeRegisterDevicePostDto();
		deRegisterDevicePostDto.setIsItForRegistrationDevice(false);
		deRegisterDevicePostDto.setDevice(CryptoUtil.encodeBase64String(mapper.writeValueAsBytes(deRegisterDeviceReqDto)));
		HeaderRequest header = new HeaderRequest();
		header.setAlg("RS256");
		header.setType("JWS");
		String headerString = mapper.writeValueAsString(header);
		DeviceDeRegisterResponse deviceDeRegisterResponse = new DeviceDeRegisterResponse();
		deviceDeRegisterResponse.setStatus("success");
		deviceDeRegisterResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		deviceDeRegisterResponse.setEnv("mz");
		deviceDeRegisterResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(deviceDeRegisterResponse));
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(deRegisterDeviceReqDto);
		Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(headerString).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(mapper.writeValueAsString(deviceDeRegisterResponse));
		String signedRegisteredDevice=registeredDeviceService.deRegisterDevice(deRegisterDevicePostDto);
		String[] split = signedRegisteredDevice.split("\\.");
		String srd= split[1];
		
		DeviceDeRegisterResponse dev=mapper.readValue(CryptoUtil.decodeBase64(srd), DeviceDeRegisterResponse.class);
		assertTrue(dev.getDeviceCode().equals(deviceDeRegisterResponse.getDeviceCode()));
	}
}
