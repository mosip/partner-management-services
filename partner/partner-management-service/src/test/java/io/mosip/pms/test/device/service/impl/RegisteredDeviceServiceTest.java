package io.mosip.pms.test.device.service.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.crypto.jce.core.CryptoCore;

import io.mosip.pms.common.constant.Purpose;
import io.mosip.pms.common.dto.DeviceFilterValueDto;
import io.mosip.pms.common.dto.FilterDto;
import io.mosip.pms.common.dto.Pagination;
import io.mosip.pms.common.dto.SearchFilter;
import io.mosip.pms.common.dto.SearchSort;
import io.mosip.pms.common.helper.SearchHelper;
import io.mosip.pms.common.response.dto.ValidateResponseWrapper;
import io.mosip.pms.common.util.PageUtils;
import io.mosip.pms.common.util.RestUtil;
import io.mosip.pms.device.authdevice.entity.DeviceDetail;
import io.mosip.pms.device.authdevice.entity.FoundationalTrustProvider;
import io.mosip.pms.device.authdevice.entity.RegisteredDevice;
import io.mosip.pms.device.authdevice.entity.RegisteredDeviceHistory;
import io.mosip.pms.device.authdevice.repository.DeviceDetailRepository;
import io.mosip.pms.device.authdevice.repository.FoundationalTrustProviderRepository;
import io.mosip.pms.device.authdevice.repository.RegisteredDeviceHistoryRepository;
import io.mosip.pms.device.authdevice.repository.RegisteredDeviceRepository;
import io.mosip.pms.device.authdevice.service.RegisteredDeviceService;
import io.mosip.pms.device.authdevice.service.impl.RegisteredDeviceServiceImpl;
import io.mosip.pms.device.dto.DeRegisterDeviceReqDto;
import io.mosip.pms.device.dto.DeviceData;
import io.mosip.pms.device.dto.DeviceInfo;
import io.mosip.pms.device.dto.DigitalId;
import io.mosip.pms.device.dto.JWTSignatureResponseDto;
import io.mosip.pms.device.exception.DeviceValidationException;
import io.mosip.pms.device.request.dto.DeRegisterDevicePostDto;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.RegisteredDevicePostDto;
import io.mosip.pms.device.response.dto.DeviceDeRegisterResponse;
import io.mosip.pms.device.response.dto.RegisterDeviceResponse;
import io.mosip.pms.test.PartnerManagementServiceTest;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { PartnerManagementServiceTest.class })
@AutoConfigureMockMvc
@EnableWebMvc
public class RegisteredDeviceServiceTest {

	@Mock
	SearchHelper searchHelper;
	
	@Mock
	PageUtils pageUtils;
	
	
//	@Mock
//	FilterColumnValidator filterColumnValidator;
	
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
	ObjectMapper objectMapper;
	
	@Autowired
	ObjectMapper mapper;
	
	@Mock
	private RestUtil restUtil;

	DeviceDetail deviceDetail=new DeviceDetail();
	private RequestWrapper<DeviceSearchDto> deviceRequestDto;
	private RegisteredDevicePostDto registeredDevicePostDto = null;
	private DigitalId dig;
	private DigitalId dig1;
	private RegisteredDevice registeredDevice;
	private DeviceData device;
	private RegisteredDeviceHistory registeredDeviceHistory;
	private DeviceInfo deviceInfo;
	private ValidateResponseWrapper<JWTSignatureResponseDto> responseWrapper ;
	private JWTSignatureResponseDto signResponseDto;
	
	DeviceFilterValueDto DeviceFilterValueDto=new DeviceFilterValueDto();
	FilterDto filterDto = new FilterDto();
	SearchFilter searchDto = new SearchFilter();
	DeviceSearchDto deviceSearchDto = new DeviceSearchDto();
	Pagination pagination = new Pagination();
	SearchSort searchSort = new SearchSort();
	SearchFilter searchFilter = new SearchFilter();
	@Before
	public void setup() throws Exception {
		//ReflectionTestUtils.setField(registeredDeviceService, "filterColumnValidator", filterColumnValidator);
		ReflectionTestUtils.setField(registeredDeviceService, "searchHelper", searchHelper);
		ReflectionTestUtils.setField(registeredDeviceService, "pageUtils", pageUtils);
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
		deviceSearchDto.setPurpose(Purpose.REGISTRATION);
		//Filter
		filterDto.setColumnName("code");
    	filterDto.setText("");
    	filterDto.setType("all");
    	searchDto.setColumnName("code");
    	searchDto.setFromValue("");
    	searchDto.setToValue("");
    	searchDto.setType("all");
    	searchDto.setValue("b");
    	List<FilterDto> filterDtos = new ArrayList<FilterDto>();
    	filterDtos.add(filterDto);
    	List<SearchFilter> searchDtos = new ArrayList<SearchFilter>();
    	searchDtos.add(searchDto);
    	DeviceFilterValueDto.setFilters(filterDtos);
    	DeviceFilterValueDto.setOptionalFilters(searchDtos);
    	//DeviceFilterValueDto.setDeviceProviderId("all");
    	DeviceFilterValueDto.setPurpose(Purpose.REGISTRATION);
		
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
		
			FoundationalTrustProvider dList=new FoundationalTrustProvider();
			dList.setId("121");
			when(ftpRepo.findByIdAndIsActiveTrue(Mockito.anyString()))
					.thenReturn(dList);
			when(registeredDeviceRepository.save(Mockito.any())).thenReturn(registeredDevice);
			when(registeredDeviceHistoryRepo.save(Mockito.any())).thenReturn(registeredDeviceHistory);
			when(registeredDeviceRepository.findByCodeAndIsActiveIsTrue(Mockito.anyString())).thenReturn(registeredDevice);
			
			 signResponseDto = new JWTSignatureResponseDto();
			signResponseDto.setTimestamp(LocalDateTime.now());
			signResponseDto.setJwtSignedData("eyJ4NXQjUzI1NiI6InZqa2EzTlBHN2FTUzlETElsbnNxazRYMTZqNE8yTmwtcVR0RXhaQVJWS2siLCJhbGciOiJSUzI1NiJ9.eyJzdGF0dXMiOiJzdWNjZXNzIiwiZGV2aWNlQ29kZSI6ImIwYTIyMDc1LTZhOGYtNDU1NS05NDI4LTM5MGU5MzA4OWY0YyIsImVudiI6IkRldmVsb3BlciIsInRpbWVTdGFtcCI6IjIwMjAtMTAtMDVUMDQ6NDY6MTguMjQyWiJ9.Xuc2XJsh7SRj5PWGCSkHMV426sHL78Eniv6W5z31iIrHNF4XORp0EjjpY1l8FEe95YKAQvBEqK6cLqLnp45_wMyHsjQ5NLZsQXZYzOJpz8OvuM3xFf2xe1tvUrsNhOaOyeDcYtVOO3CawPldMZXT-30r8AxTngV2KEa1comh1qqbfZDLkv1PJP_wh38dpuvrWheVHV6SExoOpauuJ6Df_-9JivwT-WS1e77b0TAoQF7-mRog8azarABSantTKCQAW7rKQd_QYHz0BVvkv4n86qrJvtyqMT6isAFFvEbCAGeodZdLzIKI4eYVx4clTxxgatOD9M4Bz1V6CE35f7pWkQ");

			 responseWrapper = new ValidateResponseWrapper<>();
			responseWrapper.setResponse(signResponseDto);
			responseWrapper.setResponsetime(LocalDateTime.now());			
			when(restUtil.postApi(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn("response");
			Properties prop=new Properties();
			prop.setProperty("token", "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJScUZSaS1ZcmVzbDBfR2lINDdMZ2Q1ckQ0azhRbXBObXZvTFZCM3hYMWZFIn0.eyJqdGkiOiJhYmVmZmI0Mi03OWZkLTQ3OGMtOTA0Zi1jMWU0NmVlYmRiZTEiLCJleHAiOjE1OTkwNjY2MzAsIm5iZiI6MCwiaWF0IjoxNTk5MDMwNjMwLCJpc3MiOiJodHRwczovL2Rldi5tb3NpcC5uZXQva2V5Y2xvYWsvYXV0aC9yZWFsbXMvbW9zaXAiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiNDlkN2JiZTUtYTFjNC00NTYxLWE0YmYtMjY0NWIxNDBmZmRkIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoibW9zaXAtcmVncHJvYy1jbGllbnQiLCJhdXRoX3RpbWUiOjAsInNlc3Npb25fc3RhdGUiOiJlNzU1MTk4Yi0wY2IzLTQwYzgtODllYi0xMTM2MDA1NzQxYzMiLCJhY3IiOiIxIiwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIlJFR0lTVFJBVElPTl9QUk9DRVNTT1IiLCJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsibW9zaXAtcmVncHJvYy1jbGllbnQiOnsicm9sZXMiOlsidW1hX3Byb3RlY3Rpb24iXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImNsaWVudElkIjoibW9zaXAtcmVncHJvYy1jbGllbnQiLCJjbGllbnRIb3N0IjoiMTAuMjQ0LjkuOCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZS1hY2NvdW50LW1vc2lwLXJlZ3Byb2MtY2xpZW50IiwiY2xpZW50QWRkcmVzcyI6IjEwLjI0NC45LjgifQ.dpZv9EtF_QRm23q14wwRHeEoNGmvwAdY85eTAkKEmLlOvd1yQ7WTZsPGEPsiWn9ii8IqgqJWH_DuyEDPPjoKE2KBf_5csuvkzkSci3xuMG9KrivTsLufXhveDPzsiMDCoJHqvDxnE91Sb-RNQVCdG78AE7mwQuQBdLfz-Q8V_8uQWtW7lruEu4NUTjcpi7zukuxjTwQnJswzwSOv8zxR80jKSdoHdAr1g8t_oC7nrsJ2mfhZiG-kncA1V9F1FonrwUdeoWimpdHBOHqPlMQrBw9di2ZhD_Y0OqI5u1jbiTW1ecnrUtEKLllX2CEfiv333T3WWUftk5QKGDYjEN1LWw ");
		System.setProperties(prop);
		//AccessTokenResponse tok=new AccessTokenResponse();
		//tok.setAccess_token("eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJScUZSaS1ZcmVzbDBfR2lINDdMZ2Q1ckQ0azhRbXBObXZvTFZCM3hYMWZFIn0");		
		ReflectionTestUtils.setField(registeredDeviceService,"registerDeviceTimeStamp","+5");
		ReflectionTestUtils.setField(registeredDeviceService,"activeProfile","mz");
		ReflectionTestUtils.setField(registeredDeviceService,"signUrl","https://dev.mosip.net/v1/keymanager/sign");
		Mockito.when(environment.getProperty("PASSWORDBASEDTOKENAPI")).thenReturn("https://dev.mosip.net/v1/authmanager/authenticate/useridPwd");
		
	}
		
	@Test
	public void registeredDeviceSearchtest() throws Exception{
		objectMapper.writeValueAsString(deviceRequestDto);
		DeviceDetail device = new DeviceDetail();
		device.setId("1001");
		Mockito.doReturn(new PageImpl<>(Arrays.asList(device))).when(searchHelper).search(Mockito.any(),Mockito.any(),Mockito.any());
		registeredDeviceService.searchRegisteredDevice(RegisteredDevice.class, deviceSearchDto);
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
		//HeaderRequest header = new HeaderRequest();
		//header.setAlg("RS256");
		//header.setType("JWS");
		//String headerString = mapper.writeValueAsString(header);
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(device).thenReturn(dig);
		//Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(mapper.writeValueAsString(dig)).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(headerString).thenReturn(mapper.writeValueAsString(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
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
		//HeaderRequest header = new HeaderRequest();
		//header.setAlg("RS256");
		//header.setType("JWS");
		//String headerString = mapper.writeValueAsString(header);
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(device).thenReturn(dig);
		//Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(mapper.writeValueAsString(dig)).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(headerString).thenReturn(mapper.writeValueAsString(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		registeredDeviceService.signedRegisteredDevice(registeredDevicePostDto);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=DeviceValidationException.class)
	public void createRegisteredDeviceInvalidDigitalId() throws Exception {
		//Mockito.doReturn(null).when(deviceDetailRepository).findByDeviceDetail(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString());
		RegisterDeviceResponse registerDeviceResponse = new RegisterDeviceResponse();
		registerDeviceResponse.setDeviceCode("70959dd5-e45f-438a-9ff8-9b263908e572");
		registerDeviceResponse.setStatus("Registered");
		registerDeviceResponse.setTimeStamp(LocalDateTime.now(ZoneOffset.UTC));
		registerDeviceResponse.setEnv("mz");
		registerDeviceResponse.setDigitalId(mapper.writeValueAsString(dig1));
//		HeaderRequest header = new HeaderRequest();
//		header.setAlg("RS256");
//		header.setType("JWS");
//		String headerString = mapper.writeValueAsString(header);
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(device).thenReturn(dig1);
		//Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(mapper.writeValueAsString(dig1)).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(headerString).thenReturn(mapper.writeValueAsString(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig1).thenReturn(responseWrapper).thenReturn(signResponseDto);
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
//		HeaderRequest header = new HeaderRequest();
//		header.setAlg("RS256");
//		header.setType("JWS");
//		String headerString = mapper.writeValueAsString(header);
		Mockito.when(objectMapper.writeValueAsBytes(any())).thenReturn(mapper.writeValueAsBytes(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(device).thenReturn(dig);
		//Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(mapper.writeValueAsString(dig)).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(headerString).thenReturn(mapper.writeValueAsString(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
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
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
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
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
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
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		registeredDeviceService.signedRegisteredDevice(registeredDevicePostDto);
		
	}
	
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
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenThrow(JsonParseException.class);
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
		//Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(mapper.writeValueAsString(dig)).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(headerString).thenReturn(mapper.writeValueAsString(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenThrow(JsonParseException.class);
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
	//	Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(mapper.writeValueAsString(dig)).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(headerString).thenReturn(mapper.writeValueAsString(registerDeviceResponse));
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenThrow(JsonParseException.class);
		registeredDeviceService.signedRegisteredDevice(registeredDevicePostDto);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected = DeviceValidationException.class)
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
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
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
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
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
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
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
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(deRegisterDeviceReqDto);
		//Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(headerString).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(mapper.writeValueAsString(deviceDeRegisterResponse));
		registeredDeviceService.deRegisterDevice(deRegisterDevicePostDto);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected=DeviceValidationException.class)
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
		Mockito.when(objectMapper.readValue(Mockito.anyString(), any(Class.class))).thenReturn(dig).thenReturn(responseWrapper).thenReturn(signResponseDto);
		Mockito.when(objectMapper.readValue(any(byte[].class), any(Class.class))).thenReturn(deRegisterDeviceReqDto);
		//Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn(headerString).thenReturn(mapper.writeValueAsString(signResponseDto)).thenReturn(mapper.writeValueAsString(deviceDeRegisterResponse));
		registeredDeviceService.deRegisterDevice(deRegisterDevicePostDto);
		
	}
	
	private RegisteredDevicePostDto createDevRequest() throws JsonProcessingException {
		DigitalId digitalIdDto = new DigitalId();
		digitalIdDto.setDateTime(LocalDateTime.now(ZoneOffset.UTC));
		digitalIdDto.setDeviceSubType("Full face");
		digitalIdDto.setDeviceProvider("MOSIP.PROXY.SBI");
		digitalIdDto.setDeviceProvider("MOSIP.PROXY.SBI");
		digitalIdDto.setMake("MOSIP");
		digitalIdDto.setModel("FACE01");
		digitalIdDto.setSerialNo("TR001234567");
		digitalIdDto.setType("Face");
		DeviceInfo deviceInfo = new DeviceInfo();
		deviceInfo.setDigitalId(CryptoUtil.encodeBase64(mapper.writeValueAsBytes(digitalIdDto)));
		deviceInfo.setCertification("L0");
		deviceInfo.setDeviceExpiry(LocalDateTime.now(ZoneOffset.UTC).plusYears(1));
		List<String> deviceSubID = new ArrayList<String>();
		deviceSubID.add("1");
		deviceSubID.add("2");
		deviceSubID.add("3");
		deviceInfo.setDeviceSubId(deviceSubID);
		deviceInfo.setFirmware("0.9.5");
		deviceInfo.setTimestamp(LocalDateTime.now(ZoneOffset.UTC));
		DeviceData data = new DeviceData();
		data.setDeviceInfo(CryptoUtil.encodeBase64(mapper.writeValueAsBytes(deviceInfo)));
		data.setDeviceId("47d53c45-5edb-4607-9c64-35c80483693e");
		data.setFoundationalTrustProviderId("121");
		data.setPurpose("Auth");
		RegisteredDevicePostDto registeredDevicePostDto1 = new RegisteredDevicePostDto();
		registeredDevicePostDto1.setDeviceData(CryptoUtil.encodeBase64(mapper.writeValueAsBytes(data)));
		return registeredDevicePostDto1;
		
	}
	
	
	private DigitalId getDitalId() {
		DigitalId digitalIdDto = new DigitalId();
		digitalIdDto.setDateTime(LocalDateTime.now(ZoneOffset.UTC));
		digitalIdDto.setDeviceSubType("Full face");
		digitalIdDto.setDeviceProvider("MOSIP.PROXY.SBI");
		digitalIdDto.setDeviceProvider("MOSIP.PROXY.SBI");
		digitalIdDto.setMake("MOSIP");
		digitalIdDto.setModel("FACE01");
		digitalIdDto.setSerialNo("TR001234567");
		digitalIdDto.setType("Face");
		return digitalIdDto;
		
	}
	
	private DeviceInfo getDeviceInfo(DigitalId digitalId) throws JsonProcessingException {
		DeviceInfo deviceInfo = new DeviceInfo();
		deviceInfo.setDigitalId(CryptoUtil.encodeBase64(mapper.writeValueAsBytes(digitalId)));
		deviceInfo.setCertification("L0");
		deviceInfo.setDeviceExpiry(LocalDateTime.now(ZoneOffset.UTC).plusYears(1));
		List<String> deviceSubID = new ArrayList<String>();
		deviceSubID.add("1");
		deviceSubID.add("2");
		deviceSubID.add("3");
		deviceInfo.setDeviceSubId(deviceSubID);
		deviceInfo.setFirmware("0.9.5");
		deviceInfo.setTimestamp(LocalDateTime.now(ZoneOffset.UTC));
		DeviceData data = new DeviceData();
		data.setDeviceInfo(CryptoUtil.encodeBase64(mapper.writeValueAsBytes(deviceInfo)));
		return deviceInfo;
	}
	
	private DeviceData getDeviceData(DeviceInfo deviceInfo) throws JsonProcessingException {
		DeviceData data = new DeviceData();
		data.setDeviceInfo(CryptoUtil.encodeBase64(mapper.writeValueAsBytes(deviceInfo)));
		data.setDeviceId("47d53c45-5edb-4607-9c64-35c80483693e");
		data.setFoundationalTrustProviderId("121");
		data.setPurpose("Auth");
		return data;
	}
}
