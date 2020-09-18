package io.mosip.pmp.regdevice.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.kernel.crypto.jce.core.CryptoCore;
import io.mosip.pmp.authdevice.constants.RegisteredDeviceErrorCode;
import io.mosip.pmp.authdevice.dto.DeRegisterDevicePostDto;
import io.mosip.pmp.authdevice.dto.DeRegisterDeviceReqDto;
import io.mosip.pmp.authdevice.dto.DeviceData;
import io.mosip.pmp.authdevice.dto.DeviceDeRegisterResponse;
import io.mosip.pmp.authdevice.dto.DeviceInfo;
import io.mosip.pmp.authdevice.dto.DeviceResponse;
import io.mosip.pmp.authdevice.dto.DeviceSearchDto;
import io.mosip.pmp.authdevice.dto.DigitalId;
import io.mosip.pmp.authdevice.dto.PageResponseDto;
import io.mosip.pmp.authdevice.dto.Pagination;
import io.mosip.pmp.authdevice.dto.RegisterDeviceResponse;
import io.mosip.pmp.authdevice.dto.RegisteredDevicePostDto;
import io.mosip.pmp.authdevice.dto.SearchDto;
import io.mosip.pmp.authdevice.dto.SearchFilter;
import io.mosip.pmp.authdevice.dto.SearchSort;
import io.mosip.pmp.authdevice.dto.SignRequestDto;
import io.mosip.pmp.authdevice.dto.SignResponseDto;
import io.mosip.pmp.authdevice.exception.AuthDeviceServiceException;
import io.mosip.pmp.authdevice.exception.RequestException;
import io.mosip.pmp.authdevice.exception.ValidationException;
import io.mosip.pmp.authdevice.util.HeaderRequest;
import io.mosip.pmp.authdevice.util.RegisteredDeviceConstant;
import io.mosip.pmp.keycloak.impl.AccessTokenResponse;
import io.mosip.pmp.partner.core.RequestWrapper;
import io.mosip.pmp.partner.core.ResponseWrapper;
import io.mosip.pmp.partner.util.RestUtil;
import io.mosip.pmp.regdevice.entity.RegDeviceDetail;
import io.mosip.pmp.regdevice.entity.RegRegisteredDevice;
import io.mosip.pmp.regdevice.entity.RegRegisteredDeviceHistory;
import io.mosip.pmp.regdevice.repository.RegDeviceDetailRepository;
import io.mosip.pmp.regdevice.repository.RegFoundationalTrustProviderRepository;
import io.mosip.pmp.regdevice.repository.RegRegisteredDeviceHistoryRepository;
import io.mosip.pmp.regdevice.repository.RegRegisteredDeviceRepository;
import io.mosip.pmp.regdevice.service.RegRegisteredDeviceService;

@Component
@Transactional
public class RegRegisteredDeviceServiceImpl implements RegRegisteredDeviceService {
	private static final String AUTHORIZATION ="Authorization=";

	@Autowired
	RegRegisteredDeviceRepository registeredDeviceRepository;

	@Autowired
	RegRegisteredDeviceHistoryRepository registeredDeviceHistoryRepo;
	
	@Autowired
	RegFoundationalTrustProviderRepository ftpRepo;

	@Autowired
	Environment environment;

	@Autowired
	RegDeviceDetailRepository deviceDetailRepository;
	
	@Autowired    
	private CryptoCore cryptoCore;
	
	@Autowired
	ObjectMapper mapper;
	
	@Autowired
	RestUtil restUtil;
	
	/** The registered. */
	private static String REGISTERED = "Registered";

	/** The revoked. */
	private static String REVOKED = "Revoked";

	/** The retired. */
	private static String RETIRED = "Retired";

	@Autowired
	private RestTemplate restTemplate;

	@Value("${mosip.kernel.device.search-url}")
	private String deviceUrl;
	
	@Value("${mosip.kernel.sign-url}")
	private String signUrl;
	
	@Value("${mosip.stage.environment}")
	private String activeProfile;
	
	@Value("${mosip.iam.open-id-url}")
	private String keycloakOpenIdUrl;

	@Value("${mosip.iam.master.realm-id}")
	private String realmId;

	@Value("${mosip.keycloak.admin.client.id}")
	private String adminClientID;

	@Value("${mosip.keycloak.admin.user.id}")
	private String adminUserName;

	@Value("${mosip.keycloak.admin.secret.key}")
	private String adminSecret;

	@Value("${masterdata.registerdevice.timestamp.validate:+5}")
	private String registerDeviceTimeStamp;
	
	@Override
	public String signedRegisteredDevice(RegisteredDevicePostDto registeredDevicePostDto)  {
		
		RegRegisteredDevice mapEntity = null;
		RegRegisteredDevice crtRegisteredDevice = null;
		RegRegisteredDeviceHistory entityHistory = new RegRegisteredDeviceHistory();
		String digitalIdJson;
		DeviceResponse response = new DeviceResponse();
		DeviceData deviceData = null;
		DigitalId digitalId = null;
		RegDeviceDetail deviceDetail = null;
		RegisterDeviceResponse registerDeviceResponse = null;
		String deviceDataPayLoad = getPayLoad(registeredDevicePostDto.getDeviceData());
		String headerString, signedResponse, registerDevice = null;

		try {
			deviceData = mapper.readValue(CryptoUtil.decodeBase64(deviceDataPayLoad), DeviceData.class);
			validate(deviceData);
			String digitalIdPayLoad = getPayLoad(deviceData.getDeviceInfo().getDigitalId());
			digitalId = mapper.readValue(CryptoUtil.decodeBase64(digitalIdPayLoad),
					DigitalId.class);
			validate(digitalId);
			deviceDetail = deviceDetailRepository.findByDeviceDetail(digitalId.getMake(), digitalId.getModel(),
					digitalId.getDeviceProviderId(), digitalId.getDeviceSubType(), digitalId.getType());
			if (deviceDetail == null) {
				throw new RequestException(RegisteredDeviceErrorCode.DEVICE_DETAIL_NOT_FOUND.getErrorCode(),
						RegisteredDeviceErrorCode.DEVICE_DETAIL_NOT_FOUND.getErrorMessage());
			}else {
				mapEntity=new RegRegisteredDevice();
				mapEntity.setDeviceDetailId(deviceDetail.getId());
			}

			if(ftpRepo.findByIdAndIsActiveTrue(deviceData.getFoundationalTrustProviderId())==null) {
				throw new RequestException(RegisteredDeviceErrorCode.FTP_NOT_FOUND.getErrorCode(),
						RegisteredDeviceErrorCode.FTP_NOT_FOUND.getErrorMessage());
			}

			RegRegisteredDevice regDevice = registeredDeviceRepository
					.findByDeviceDetailIdAndSerialNoAndIsActiveIsTrue(deviceDetail.getId(), digitalId.getSerialNo());

			if (regDevice != null) {
				throw new RequestException(RegisteredDeviceErrorCode.SERIALNO_DEVICEDETAIL_ALREADY_EXIST.getErrorCode(),
						RegisteredDeviceErrorCode.SERIALNO_DEVICEDETAIL_ALREADY_EXIST.getErrorMessage());
			}
			
				digitalIdJson = mapper.writeValueAsString(digitalId);
				mapEntity = mapRegisteredDeviceDto(registeredDevicePostDto, digitalIdJson, deviceData,deviceDetail,
						digitalId);
				if (deviceData.getPurpose().equalsIgnoreCase(RegisteredDeviceConstant.AUTH)) {
					// should be uniquely randomly generated
					mapEntity.setCode( UUID.randomUUID().toString());
					entityHistory.setCode(mapEntity.getCode());
					entityHistory.setEffectDateTime(mapEntity.getCrDtimes());
				}else if (deviceData.getPurpose().equalsIgnoreCase(RegisteredDeviceConstant.REGISTRATION)){
				mapEntity.setCode(generateCodeValue( registeredDevicePostDto,  deviceData, digitalId));
				entityHistory.setCode(mapEntity.getCode());
				entityHistory.setEffectDateTime(mapEntity.getCrDtimes());
				}
				entityHistory=mapRegisteredDeviceHistory(entityHistory,mapEntity);
				crtRegisteredDevice = registeredDeviceRepository.save(mapEntity);
				
				registeredDeviceHistoryRepo.save(entityHistory);

				digitalId = mapper.readValue(digitalIdJson, DigitalId.class);

				registerDeviceResponse = mapRegisteredDeviceResponse(crtRegisteredDevice, deviceData);
				registerDeviceResponse
						.setDigitalId(CryptoUtil.encodeBase64(mapper.writeValueAsString(digitalId).getBytes("UTF-8")));
				registerDeviceResponse.setEnv(activeProfile);
				HeaderRequest header = new HeaderRequest();
				header.setAlg("RS256");
				header.setType("JWS");
				headerString = mapper.writeValueAsString(header);
				Objects.requireNonNull(registerDeviceResponse);
				signedResponse = getSignedResponse(registerDeviceResponse);
				registerDevice = mapper.writeValueAsString(registerDeviceResponse);
				response.setResponse(convertToJWS(headerString, registerDevice, signedResponse));
		} catch (IOException e) {
			throw new AuthDeviceServiceException(
					RegisteredDeviceErrorCode.REGISTERED_DEVICE_INSERTION_EXCEPTION.getErrorCode(),
					RegisteredDeviceErrorCode.REGISTERED_DEVICE_INSERTION_EXCEPTION.getErrorMessage() + " "
							+ e.getMessage());
		}
		

		
		return response.getResponse();
	}
	
	private String generateCodeValue(RegisteredDevicePostDto registeredDevicePostDto, DeviceData deviceData,
			DigitalId digitalId) {
		String code = "";
		if (deviceData.getPurpose().equalsIgnoreCase(RegisteredDeviceConstant.REGISTRATION)) {
			try {
			RequestWrapper<SearchDto> request = new RequestWrapper<>();
			SearchDto searchDto=new SearchDto();
			searchDto.setLanguageCode("eng");
			SearchFilter searchFilter=new SearchFilter();
			searchFilter.setColumnName("serialNum");
			searchFilter.setType("equals");
			searchFilter.setValue(digitalId.getSerialNo());
			searchDto.setFilters(Arrays.asList(searchFilter));
			Pagination pagination=new Pagination(0, 100);
			searchDto.setPagination(pagination);
			SearchSort searchSort=new SearchSort();
			searchSort.setSortField("name");
			searchSort.setSortType("ASC");
			searchDto.setSort(Arrays.asList(searchSort));
			request.setRequest(searchDto);
			
			ResponseEntity<String> response=restTemplate.exchange(deviceUrl, HttpMethod.POST, setRequestHeader(request, MediaType.APPLICATION_JSON), String.class);
			ResponseWrapper<?> responseObject = mapper.readValue(response.getBody(), ResponseWrapper.class);
			PageResponseDto<?> page=mapper.readValue(mapper.writeValueAsString(responseObject.getResponse()),
					PageResponseDto.class);
			for(Object data: page.getData()) {
				DeviceSearchDto deviceSearchDto=mapper.readValue(mapper.writeValueAsString(data),DeviceSearchDto.class);
				if(deviceSearchDto.getSerialNum().equals(digitalId.getSerialNo())
						&&(deviceSearchDto.getIsDeleted()==null || deviceSearchDto.getIsDeleted()==false)) {
					code=deviceSearchDto.getId();
				}
			}
			if(code==null) {
				throw new RequestException(RegisteredDeviceErrorCode.SERIALNUM_NOT_EXIST.getErrorCode(), String.format(
						RegisteredDeviceErrorCode.SERIALNUM_NOT_EXIST.getErrorMessage(), digitalId.getSerialNo()));
			}
		} catch ( Exception e) {
			throw new AuthDeviceServiceException(
					RegisteredDeviceErrorCode.REGISTERED_DEVICE_INSERTION_EXCEPTION.getErrorCode(),
					RegisteredDeviceErrorCode.REGISTERED_DEVICE_INSERTION_EXCEPTION.getErrorMessage() + " "
							+ e.getMessage());
		}
		} else if (deviceData.getPurpose().equalsIgnoreCase(RegisteredDeviceConstant.AUTH)) {
			// should be uniquely randomly generated
			code = UUID.randomUUID().toString();
		}
		return code;
	}

	private String convertToJWS(String headerString, String registerDevice, String signedResponse) {
		return CryptoUtil.encodeBase64String(headerString.getBytes()) + "."
				+ CryptoUtil.encodeBase64String(registerDevice.getBytes()) + "."
				+ CryptoUtil.encodeBase64String(signedResponse.getBytes());
	}



	private String getSignedResponse(RegisterDeviceResponse registerDeviceResponse)  {
		RequestWrapper<SignRequestDto> request = new RequestWrapper<>();
		SignRequestDto signatureRequestDto = new SignRequestDto();
		SignResponseDto signResponse = new SignResponseDto();
		
		
		try {
			signatureRequestDto
					.setData(CryptoUtil.encodeBase64String(mapper.writeValueAsBytes(registerDeviceResponse)));
			request.setRequest(signatureRequestDto);
			ResponseEntity<String> response = restTemplate.exchange(signUrl, HttpMethod.POST, setRequestHeader(request, MediaType.APPLICATION_JSON), String.class);
			ResponseWrapper<?> responseObject;
					responseObject = mapper.readValue(response.getBody(), ResponseWrapper.class);
					signResponse = mapper.readValue(mapper.writeValueAsString(responseObject.getResponse()),
							SignResponseDto.class);
				} catch (IOException e) {
					throw new AuthDeviceServiceException(
							RegisteredDeviceErrorCode.REGISTERED_DEVICE_INSERTION_EXCEPTION.getErrorCode(),
							RegisteredDeviceErrorCode.REGISTERED_DEVICE_INSERTION_EXCEPTION.getErrorMessage() + " "
									+ e.getMessage());
				}
				
			

		return signResponse.getSignature();
	}



	private RegisterDeviceResponse mapRegisteredDeviceResponse(RegRegisteredDevice entity, DeviceData deviceData) {
		RegisterDeviceResponse registerDeviceResponse = new RegisterDeviceResponse();
		registerDeviceResponse.setDeviceCode(entity.getCode());
		registerDeviceResponse.setStatus(entity.getStatusCode());
		registerDeviceResponse.setTimeStamp(deviceData.getDeviceInfo().getTimeStamp());
		return registerDeviceResponse;
	}



	private RegRegisteredDevice mapRegisteredDeviceDto(RegisteredDevicePostDto registeredDevicePostDto,
			String digitalIdJson, DeviceData deviceData, RegDeviceDetail deviceDetail, DigitalId digitalId) {
		RegRegisteredDevice entity = new RegRegisteredDevice();
		entity.setDeviceDetailId(deviceDetail.getId());
		entity.setStatusCode(REGISTERED);
		entity.setDeviceId(deviceData.getDeviceId());
		entity.setDeviceSubId(deviceData.getDeviceInfo().getDeviceSubId());
		entity.setHotlisted(deviceData.getHotlisted());
		entity.setDigitalId(digitalIdJson);
		entity.setSerialNo(digitalId.getSerialNo());
		Authentication authN = SecurityContextHolder.getContext().getAuthentication();
		if (!EmptyCheckUtils.isNullEmpty(authN)) {
			entity.setCrBy(authN.getName());
		}
		entity.setCrDtimes(LocalDateTime.now(ZoneId.of("UTC")));
		entity.setActive(true);
		entity.setPurpose(deviceData.getPurpose());
		entity.setFirmware(deviceData.getDeviceInfo().getFirmware());
		entity.setExpiryDate(deviceData.getDeviceInfo().getDeviceExpiry());
		entity.setCertificationLevel(deviceData.getDeviceInfo().getCertification());
		entity.setFoundationalTPId(deviceData.getFoundationalTrustProviderId());
		/*
		 * entity.setFoundationalTrustSignature(dto.getFoundationalTrustSignature());
		 * entity.setFoundationalTrustCertificate(dto.getFoundationalTrustCertificate())
		 * ; entity.setDeviceProviderSignature(dto.getDeviceProviderSignature());
		 */

		return entity;
	}
	
	private RegRegisteredDeviceHistory mapRegisteredDeviceHistory(RegRegisteredDeviceHistory history,RegRegisteredDevice entity) {
		
		history.setDeviceDetailId(entity.getDeviceDetailId());
		history.setStatusCode(entity.getStatusCode());
		history.setDeviceId(entity.getDeviceId());
		history.setDeviceSubId(entity.getDeviceSubId());
		history.setHotlisted(entity.isHotlisted());
		history.setDigitalId(entity.getDigitalId());
		history.setSerialNo(entity.getSerialNo());
		
		history.setCrBy(entity.getCrBy());
		history.setEffectDateTime(entity.getCrDtimes());
		history.setCrDtimes(entity.getCrDtimes());
		history.setActive(entity.isActive());
		history.setPurpose(entity.getPurpose());
		history.setFirmware(entity.getFirmware());
		history.setExpiryDate(entity.getExpiryDate());
		history.setCertificationLevel(entity.getCertificationLevel());
		history.setFoundationalTPId(entity.getFoundationalTPId());
		/*
		 * entity.setFoundationalTrustSignature(dto.getFoundationalTrustSignature());
		 * entity.setFoundationalTrustCertificate(dto.getFoundationalTrustCertificate())
		 * ; entity.setDeviceProviderSignature(dto.getDeviceProviderSignature());
		 */

		return history;
	}

	private void validate(DeviceData deviceData) {
		List<ServiceError> errors = new ArrayList<>();
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<DeviceData>> constraintSet = validator.validate(deviceData);
		for (ConstraintViolation<DeviceData> c : constraintSet) {
			if (c.getPropertyPath().toString().equalsIgnoreCase("purpose")) {
				errors.add(new ServiceError(RegisteredDeviceErrorCode.DEVICE_DATA_NOT_EXIST.getErrorCode(),
						c.getMessage()));
			} else {
				errors.add(new ServiceError(RegisteredDeviceErrorCode.DEVICE_DATA_NOT_EXIST.getErrorCode(),
						c.getPropertyPath() + " " + c.getMessage()));
			}
		}
		if (!errors.isEmpty()) {

			throw new ValidationException(errors);
		}

		if (deviceData.getDeviceInfo() != null) {
			LocalDateTime timeStamp = deviceData.getDeviceInfo().getTimeStamp();
			String prefix = registerDeviceTimeStamp.substring(0, 1);
			String timeString = registerDeviceTimeStamp.replaceAll("\\" + prefix, "");
			boolean isBetween = timeStamp.isAfter(LocalDateTime.now(ZoneOffset.UTC).minus(Long.valueOf("2"), ChronoUnit.MINUTES)) && timeStamp.isBefore(LocalDateTime.now(ZoneOffset.UTC).plus(Long.valueOf(timeString), ChronoUnit.MINUTES));
			if (prefix.equals("+")) {
				if (!isBetween) {
					throw new AuthDeviceServiceException(
							RegisteredDeviceErrorCode.TIMESTAMP_AFTER_CURRENTTIME.getErrorCode(),
							String.format(RegisteredDeviceErrorCode.TIMESTAMP_AFTER_CURRENTTIME.getErrorMessage(),
									timeString));
				}
			} else if (prefix.equals("-")) {
				if (LocalDateTime.now(ZoneOffset.UTC)
						.isBefore(timeStamp.plus(Long.valueOf(timeString), ChronoUnit.MINUTES))) {
					throw new AuthDeviceServiceException(
							RegisteredDeviceErrorCode.TIMESTAMP_BEFORE_CURRENTTIME.getErrorCode(),
							String.format(RegisteredDeviceErrorCode.TIMESTAMP_BEFORE_CURRENTTIME.getErrorMessage(),
									timeString));
				}
			}
			}
		Set<ConstraintViolation<DeviceInfo>> deviceInfoSet = validator.validate(deviceData.getDeviceInfo());
		for (ConstraintViolation<DeviceInfo> d : deviceInfoSet) {
			if (d.getPropertyPath().toString().equalsIgnoreCase("certification")) {
				errors.add(new ServiceError(RegisteredDeviceErrorCode.DEVICE_DATA_NOT_EXIST.getErrorCode(),
						d.getMessage()));
			} else {
				errors.add(new ServiceError(RegisteredDeviceErrorCode.DEVICE_DATA_NOT_EXIST.getErrorCode(),
						d.getPropertyPath() + " " + d.getMessage()));
			}
		}
		if (!errors.isEmpty()) {

			throw new ValidationException(errors);
		}

		}
	private void validate(DigitalId digitalId) {
		List<ServiceError> errors = new ArrayList<>();
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<DigitalId>> constraintSet = validator.validate(digitalId);
		for (ConstraintViolation<DigitalId> c : constraintSet) {
			errors.add(new ServiceError(RegisteredDeviceErrorCode.DEVICE_DATA_NOT_EXIST.getErrorCode(),
					c.getPropertyPath() + " " + c.getMessage()));
		}
		if (!errors.isEmpty()) {

			throw new ValidationException(errors);
		}

	}

	private String getPayLoad(String jws) {
		String[] split = jws.split("\\.");
		if (split.length > 2) {
			if(cryptoCore.verifySignature(new String(CryptoUtil.decodeBase64(split[2])))) {
				return split[1];
			
			}else  {
				throw new AuthDeviceServiceException(
						RegisteredDeviceErrorCode.REGISTERED_DEVICE_SIGN_VALIDATION_FAILURE.getErrorCode(),
						RegisteredDeviceErrorCode.REGISTERED_DEVICE_SIGN_VALIDATION_FAILURE.getErrorMessage() );
			}
		}
		return jws;
	}

	@Override
	public String deRegisterDevice(DeRegisterDevicePostDto deRegisterDevicePostDto) {
		RegRegisteredDevice deviceRegisterEntity = null;
		RegRegisteredDeviceHistory deviceRegisterHistory = new RegRegisteredDeviceHistory();
		String headerString, signedResponse, deRegisterDevice = null;
		String devicePayLoad = getPayLoad(deRegisterDevicePostDto.getDevice());
		try {
			DeRegisterDeviceReqDto device = mapper.readValue(CryptoUtil.decodeBase64(devicePayLoad), DeRegisterDeviceReqDto.class);
			validate(device);
			deviceRegisterEntity = registeredDeviceRepository.findByCodeAndIsActiveIsTrue(device.getDeviceCode());
			if (deviceRegisterEntity != null) {
				if (Arrays.asList(REVOKED, RETIRED).contains(deviceRegisterEntity.getStatusCode())) {
					throw new AuthDeviceServiceException(
							RegisteredDeviceErrorCode.DEVICE_DE_REGISTERED_ALREADY.getErrorCode(),
							RegisteredDeviceErrorCode.DEVICE_DE_REGISTERED_ALREADY.getErrorMessage());
				}
				deviceRegisterEntity.setStatusCode("Retired");
				Authentication authN = SecurityContextHolder.getContext().getAuthentication();
				if (!EmptyCheckUtils.isNullEmpty(authN)) {
					deviceRegisterEntity.setCrBy(authN.getName());
				}
				deviceRegisterEntity.setActive(false);
				deviceRegisterEntity.setUpdDtimes(LocalDateTime.now(ZoneId.of("UTC")));
				deviceRegisterHistory=mapUpdateHistory(deviceRegisterEntity, deviceRegisterHistory);
				deviceRegisterHistory.setEffectDateTime(deviceRegisterEntity.getUpdDtimes());
				registeredDeviceRepository.save(deviceRegisterEntity);
				
				registeredDeviceHistoryRepo.save(deviceRegisterHistory);
				DeviceDeRegisterResponse deviceDeRegisterResponse = new DeviceDeRegisterResponse();
				deviceDeRegisterResponse.setStatus("success");
				deviceDeRegisterResponse.setDeviceCode(deviceRegisterEntity.getCode());
				deviceDeRegisterResponse.setEnv(activeProfile);
				DigitalId digitalId=mapper.readValue(deviceRegisterEntity.getDigitalId(),DigitalId.class);
				deviceDeRegisterResponse.setTimeStamp(digitalId.getDateTime());
				HeaderRequest header = new HeaderRequest();
				header.setAlg("RS256");
				header.setType("JWS");
				headerString = mapper.writeValueAsString(header);
				Objects.requireNonNull(deviceDeRegisterResponse);
				signedResponse = getSignedResponse(deviceDeRegisterResponse);
				deRegisterDevice = mapper.writeValueAsString(deviceDeRegisterResponse);
			} else {

				throw new RequestException(
						RegisteredDeviceErrorCode.DEVICE_REGISTER_NOT_FOUND_EXCEPTION.getErrorCode(),
						RegisteredDeviceErrorCode.DEVICE_REGISTER_NOT_FOUND_EXCEPTION.getErrorMessage());
			}

		} catch ( IOException e) {
			throw new AuthDeviceServiceException(
					RegisteredDeviceErrorCode.DEVICE_REGISTER_DELETED_EXCEPTION.getErrorCode(),
					RegisteredDeviceErrorCode.DEVICE_REGISTER_DELETED_EXCEPTION.getErrorMessage() + " "
							+ e);
		}
		
		return convertToJWS(headerString, deRegisterDevice, signedResponse);
	}



	private RegRegisteredDeviceHistory mapUpdateHistory(RegRegisteredDevice entity,
			RegRegisteredDeviceHistory history) {
		history.setDeviceDetailId(entity.getDeviceDetailId());
		history.setStatusCode(entity.getStatusCode());
		history.setDeviceId(entity.getDeviceId());
		history.setDeviceSubId(entity.getDeviceSubId());
		history.setHotlisted(entity.isHotlisted());
		history.setDigitalId(entity.getDigitalId());
		history.setSerialNo(entity.getSerialNo());
		history.setCode(entity.getCode());
		history.setCrBy(entity.getUpdBy());
		history.setEffectDateTime(entity.getUpdDtimes());
		history.setCrDtimes(entity.getUpdDtimes());
		history.setActive(entity.isActive());
		history.setPurpose(entity.getPurpose());
		history.setFirmware(entity.getFirmware());
		history.setExpiryDate(entity.getExpiryDate());
		history.setCertificationLevel(entity.getCertificationLevel());
		history.setFoundationalTPId(entity.getFoundationalTPId());
		/*
		 * entity.setFoundationalTrustSignature(dto.getFoundationalTrustSignature());
		 * entity.setFoundationalTrustCertificate(dto.getFoundationalTrustCertificate())
		 * ; entity.setDeviceProviderSignature(dto.getDeviceProviderSignature());
		 */

		return history;
	}
	



	private String getSignedResponse(DeviceDeRegisterResponse registerDeviceResponse) throws IOException {
		RequestWrapper<SignRequestDto> request = new RequestWrapper<>();
		SignRequestDto signatureRequestDto = new SignRequestDto();
		SignResponseDto signResponse = new SignResponseDto();
		
		
			signatureRequestDto
					.setData(CryptoUtil.encodeBase64String(mapper.writeValueAsBytes(registerDeviceResponse)));
			request.setRequest(signatureRequestDto);
			ResponseEntity<String> response = restTemplate.exchange(signUrl, HttpMethod.POST, setRequestHeader(request, MediaType.APPLICATION_JSON), String.class);
			ResponseWrapper<?> responseObject;
			
				responseObject = mapper.readValue(response.getBody(), ResponseWrapper.class);
				signResponse = mapper.readValue(mapper.writeValueAsString(responseObject.getResponse()),
						SignResponseDto.class);
			

		return signResponse.getSignature();
	}

	private void validate(DeRegisterDeviceReqDto device) {
		if(EmptyCheckUtils.isNullEmpty(device.getDeviceCode()) || EmptyCheckUtils.isNullEmpty(device.getEnv())) {
			throw new RequestException(RegisteredDeviceErrorCode.DEVICE_REGISTER_NOT_FOUND_EXCEPTION.getErrorCode(),
					RegisteredDeviceErrorCode.DEVICE_REGISTER_NOT_FOUND_EXCEPTION.getErrorMessage());
		}else if(device.getDeviceCode().length()>36) {
			throw new RequestException(RegisteredDeviceErrorCode.DEVICE_CODE_EXCEEDS_LENGTH.getErrorCode(),
					RegisteredDeviceErrorCode.DEVICE_CODE_EXCEEDS_LENGTH.getErrorMessage());
		}else if(!device.getEnv().equals(activeProfile)) {
			throw new RequestException(RegisteredDeviceErrorCode.INVALID_ENV.getErrorCode(),
					RegisteredDeviceErrorCode.INVALID_ENV.getErrorMessage());
		}
		
	}


	
	@SuppressWarnings({ "unchecked" })
	private HttpEntity<Object> setRequestHeader(Object requestType, MediaType mediaType) throws IOException {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
		headers.add("Cookie", AUTHORIZATION+getAdminToken());
		if (mediaType != null) {
			headers.add("Content-Type", mediaType.toString());
		}
		if (requestType != null) {
			try {
				HttpEntity<Object> httpEntity = (HttpEntity<Object>) requestType;
				HttpHeaders httpHeader = httpEntity.getHeaders();
				Iterator<String> iterator = httpHeader.keySet().iterator();
				while (iterator.hasNext()) {
					String key = iterator.next();
					if (!(headers.containsKey("Content-Type") && key == "Content-Type"))
						headers.add(key, httpHeader.get(key).get(0));
				}
				return new HttpEntity<Object>(httpEntity.getBody(), headers);
			} catch (ClassCastException e) {
				return new HttpEntity<Object>(requestType, headers);
			}
		} else
			return new HttpEntity<Object>(headers);
	}

	private String getAdminToken() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> tokenRequestBody = null;
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put("realmId", realmId);
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(keycloakOpenIdUrl + "/token");
		
			tokenRequestBody = getAdminValueMap();
		

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(tokenRequestBody, headers);
		ResponseEntity<AccessTokenResponse> response=null;
		try {
		 response = restTemplate.postForEntity(
				uriComponentsBuilder.buildAndExpand(pathParams).toUriString(), request, AccessTokenResponse.class);
		}catch(HttpServerErrorException | HttpClientErrorException ex) {
			throw new AuthDeviceServiceException(
					RegisteredDeviceErrorCode.API_RESOURCE_EXCEPTION.getErrorCode(),
					RegisteredDeviceErrorCode.API_RESOURCE_EXCEPTION.getErrorMessage()+ex.getMessage());
		}
		
		return response.getBody().getAccess_token();
	}

	private MultiValueMap<String, String> getAdminValueMap() {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("grant_type", "password");
		map.add("username", adminUserName);
		map.add("password", adminSecret);
		map.add("client_id", adminClientID);
		return map;
	}
	

}
