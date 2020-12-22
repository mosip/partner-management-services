package io.mosip.pmp.regdevice.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

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
import io.mosip.pmp.authdevice.dto.DigitalId;
import io.mosip.pmp.authdevice.dto.JWTSignatureRequestDto;
import io.mosip.pmp.authdevice.dto.JWTSignatureResponseDto;
import io.mosip.pmp.authdevice.dto.PageResponseDto;
import io.mosip.pmp.authdevice.dto.RegisterDeviceResponse;
import io.mosip.pmp.authdevice.dto.RegisteredDevicePostDto;
import io.mosip.pmp.authdevice.dto.SearchDto;
import io.mosip.pmp.authdevice.entity.RegisteredDevice;
import io.mosip.pmp.authdevice.exception.DeviceValidationException;
import io.mosip.pmp.authdevice.util.RegisteredDeviceConstant;
import io.mosip.pmp.partner.core.RequestWrapper;
import io.mosip.pmp.partner.core.ValidateResponseWrapper;
import io.mosip.pmp.partner.exception.ErrorResponse;
import io.mosip.pmp.partner.util.MapperUtils;
import io.mosip.pmp.partner.util.RestUtil;
import io.mosip.pmp.partner.util.SearchHelper;
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

	@Autowired
	RegRegisteredDeviceRepository registeredDeviceRepository;

	@Autowired
	RegRegisteredDeviceHistoryRepository registeredDeviceHistoryRepo;

	@Autowired
	RegFoundationalTrustProviderRepository ftpRepo;

	@Autowired
	Environment environment;

	@Autowired
	SearchHelper searchHelper;

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

	@Value("${mosip.kernel.device.search-url}")
	private String deviceUrl;

	@Value("${mosip.kernel.sign-url}")
	private String signUrl;

	@Value("${mosip.stage.environment}")
	private String activeProfile;

	@Value("${masterdata.registerdevice.timestamp.validate:+5}")
	private String registerDeviceTimeStamp;
	
	@Value("${mosip.sign.applicationid:KERNEL}")
	private String signAppId;
	
	@Value("${mosip.sign.refid:SIGN}")
	private String signRefId;

	/**
	 * 
	 */
	@Override
	public String signedRegisteredDevice(RegisteredDevicePostDto registeredDevicePostDto) {
		List<ServiceError> serviceErrors = new ArrayList<>();
		RegRegisteredDevice mapEntity = null;		
		RegRegisteredDeviceHistory entityHistory = new RegRegisteredDeviceHistory();
		String digitalIdJson;
		DeviceResponse response = new DeviceResponse();
		DeviceData deviceData = null;
		DigitalId digitalId = null;
		DeviceInfo deviceInfo = null;
		RegDeviceDetail deviceDetail = null;
		RegisterDeviceResponse registerDeviceResponse = null;
		String deviceDataPayLoad = getPayLoad(registeredDevicePostDto.getDeviceData());	

		try {
			deviceData = mapper.readValue(CryptoUtil.decodeBase64(deviceDataPayLoad), DeviceData.class);
			validate(deviceData);			
			String deviceInfoPayLoad = getPayLoad(deviceData.getDeviceInfo());
			deviceInfo = mapper.readValue(CryptoUtil.decodeBase64(deviceInfoPayLoad), DeviceInfo.class);
			validate(deviceData, deviceInfo);
			String digitalIdPayLoad = getPayLoad(deviceInfo.getDigitalId());
			digitalId = mapper.readValue(CryptoUtil.decodeBase64(digitalIdPayLoad), DigitalId.class);
			validate(digitalId);

			deviceDetail = deviceDetailRepository.findByDeviceDetail(digitalId.getMake(), digitalId.getModel(),
					digitalId.getDeviceProviderId(), digitalId.getDeviceSubType(), digitalId.getType());
			if (deviceDetail == null) {								
				serviceErrors.add(new ServiceError(RegisteredDeviceErrorCode.DEVICE_DETAIL_NOT_FOUND.getErrorCode(),
						RegisteredDeviceErrorCode.DEVICE_DETAIL_NOT_FOUND.getErrorMessage()));
				throw new DeviceValidationException(serviceErrors);
			} else {
				mapEntity = new RegRegisteredDevice();
				mapEntity.setDeviceDetailId(deviceDetail.getId());
			}

			if (deviceData.getFoundationalTrustProviderId() != null
					&& !deviceData.getFoundationalTrustProviderId().isEmpty()) {
				if (ftpRepo.findByIdAndIsActiveTrue(deviceData.getFoundationalTrustProviderId()) == null) {
					serviceErrors.add(new ServiceError(RegisteredDeviceErrorCode.FTP_NOT_FOUND.getErrorCode(),
							RegisteredDeviceErrorCode.FTP_NOT_FOUND.getErrorMessage()));
					throw new DeviceValidationException(serviceErrors);
				}
			} else {
				deviceData.setFoundationalTrustProviderId(null);
			}

			String deviceCode = null;
			RegRegisteredDevice regDevice = registeredDeviceRepository
					.findByDeviceDetailIdAndSerialNo(deviceDetail.getId(), digitalId.getSerialNo());
			if(regDevice != null && regDevice.isActive() && regDevice.getStatusCode().equals(REGISTERED)) {
				serviceErrors.add(new ServiceError(RegisteredDeviceErrorCode.SERIALNO_DEVICEDETAIL_ALREADY_EXIST.getErrorCode(),
						RegisteredDeviceErrorCode.SERIALNO_DEVICEDETAIL_ALREADY_EXIST.getErrorMessage()));
				throw new DeviceValidationException(serviceErrors);				
			}else if(regDevice != null && !regDevice.isActive() && regDevice.getStatusCode().equals(RETIRED)) {
				deviceCode = regDevice.getCode();
			}else {
				deviceCode = UUID.randomUUID().toString();
			}
			
			digitalIdJson = mapper.writeValueAsString(digitalId);
			mapEntity = mapRegisteredDeviceDto(registeredDevicePostDto, digitalIdJson, deviceData, deviceDetail,
					deviceInfo, digitalId);	
			mapEntity.setCode(deviceCode);
			entityHistory.setCode(mapEntity.getCode());
			entityHistory.setEffectDateTime(mapEntity.getCrDtimes());
			entityHistory = mapRegisteredDeviceHistory(entityHistory, mapEntity);			
			digitalId = mapper.readValue(digitalIdJson, DigitalId.class);
			registerDeviceResponse = mapRegisteredDeviceResponse(mapEntity, deviceInfo, deviceData);
			registerDeviceResponse
			.setDigitalId(CryptoUtil.encodeBase64(mapper.writeValueAsString(digitalId).getBytes("UTF-8")));
			registerDeviceResponse.setEnv(activeProfile);
			Objects.requireNonNull(registerDeviceResponse);
			response.setResponse(getSignedResponse(CryptoUtil.encodeBase64String(mapper.writeValueAsBytes(registerDeviceResponse))));
			registeredDeviceRepository.save(mapEntity);
			registeredDeviceHistoryRepo.save(entityHistory);
		} catch (IOException e) {
			serviceErrors.add(new ServiceError(
					RegisteredDeviceErrorCode.REGISTERED_DEVICE_INSERTION_EXCEPTION.getErrorCode(),
					RegisteredDeviceErrorCode.REGISTERED_DEVICE_INSERTION_EXCEPTION.getErrorMessage() + " "
							+ e.getMessage()));
			throw new DeviceValidationException(serviceErrors);
		}

		return response.getResponse();
	}

	/**
	 * 
	 * @param registerDeviceResponse
	 * @return
	 */
	private String getSignedResponse(String encodedResponse) {
		List<ServiceError> serviceErrors = new ArrayList<>();
		RequestWrapper<JWTSignatureRequestDto> request = new RequestWrapper<JWTSignatureRequestDto>();
		JWTSignatureRequestDto signatureRequestDto = new JWTSignatureRequestDto();
		JWTSignatureResponseDto signResponse = new JWTSignatureResponseDto();
		try {
			signatureRequestDto.setApplicationId(signAppId);
			signatureRequestDto.setDataToSign(encodedResponse);
			signatureRequestDto.setIncludeCertHash(false);
			signatureRequestDto.setIncludeCertificate(true);
			signatureRequestDto.setIncludePayload(true);
			signatureRequestDto.setReferenceId(signRefId);
			request.setRequest(signatureRequestDto);
			String response = restUtil.postApi(signUrl, null, "", "", MediaType.APPLICATION_JSON, request,
					String.class);
			ValidateResponseWrapper<?> responseObject;
			responseObject = mapper.readValue(response, ValidateResponseWrapper.class);
			if(responseObject.getResponse() == null && responseObject.getErrors() != null) {
				for(ErrorResponse error : responseObject.getErrors()) {
					serviceErrors.add(new ServiceError(error.getErrorCode(),error.getMessage()));
				}
				throw new DeviceValidationException(serviceErrors);
			}
			signResponse = mapper.readValue(mapper.writeValueAsString(responseObject.getResponse()),
					JWTSignatureResponseDto.class);
		} catch (IOException e) {			
			serviceErrors.add(new ServiceError(
					RegisteredDeviceErrorCode.RESPONSE_SIGN_FAILED.getErrorCode(),
					RegisteredDeviceErrorCode.RESPONSE_SIGN_FAILED.getErrorMessage() + " "
							+ e.getMessage()));
			throw new DeviceValidationException(serviceErrors);
		}
		return signResponse.getJwtSignedData();
	}

	/**
	 * 
	 * @param entity
	 * @param deviceData
	 * @return
	 */
	private RegisterDeviceResponse mapRegisteredDeviceResponse(RegRegisteredDevice entity, DeviceInfo deviceInfo,
			DeviceData deviceData) {
		RegisterDeviceResponse registerDeviceResponse = new RegisterDeviceResponse();
		registerDeviceResponse.setDeviceCode(entity.getCode());
		registerDeviceResponse.setStatus(entity.getStatusCode());
		registerDeviceResponse.setTimeStamp(deviceInfo.getTimestamp());
		return registerDeviceResponse;
	}

	/**
	 * 
	 * @param registeredDevicePostDto
	 * @param digitalIdJson
	 * @param deviceData
	 * @param deviceDetail
	 * @param digitalId
	 * @return
	 */
	private RegRegisteredDevice mapRegisteredDeviceDto(RegisteredDevicePostDto registeredDevicePostDto,
			String digitalIdJson, DeviceData deviceData, RegDeviceDetail deviceDetail, DeviceInfo deviceInfo,
			DigitalId digitalId) {
		RegRegisteredDevice entity = new RegRegisteredDevice();
		entity.setDeviceDetailId(deviceDetail.getId());
		entity.setStatusCode(REGISTERED);
		entity.setDeviceId(deviceData.getDeviceId());
		entity.setDeviceSubId(deviceInfo.getDeviceSubId().toString());
		entity.setHotlisted(false);
		entity.setDigitalId(digitalIdJson);
		entity.setSerialNo(digitalId.getSerialNo());
		Authentication authN = SecurityContextHolder.getContext().getAuthentication();
		if (!EmptyCheckUtils.isNullEmpty(authN)) {
			entity.setCrBy(authN.getName());
		}else {
			entity.setCrBy("admin");
		}
		entity.setCrDtimes(LocalDateTime.now(ZoneId.of("UTC")));
		entity.setActive(true);
		entity.setPurpose(deviceData.getPurpose());
		entity.setFirmware(deviceInfo.getFirmware());
		entity.setExpiryDate(deviceInfo.getDeviceExpiry());
		entity.setCertificationLevel(deviceInfo.getCertification());
		entity.setFoundationalTPId(deviceData.getFoundationalTrustProviderId());
		return entity;
	}

	/**
	 * 
	 * @param history
	 * @param entity
	 * @return
	 */
	private RegRegisteredDeviceHistory mapRegisteredDeviceHistory(RegRegisteredDeviceHistory history,
			RegRegisteredDevice entity) {

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

	/**
	 * 
	 * @param deviceData
	 */
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
			throw new DeviceValidationException(errors);
		}
	}

	/**
	 * 
	 * @param digitalId
	 */
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
			throw new DeviceValidationException(errors);
		}
	}

	/**
	 * 
	 * @param deviceData
	 * @param deviceInfo
	 */
	private void validate(DeviceData deviceData, DeviceInfo deviceInfo) {
		List<ServiceError> errors = new ArrayList<>();
		if (deviceInfo == null || deviceInfo.getCertification() == null) {
			errors.add(new ServiceError(RegisteredDeviceErrorCode.DEVICE_DATA_NOT_EXIST.getErrorCode(),
					RegisteredDeviceErrorCode.DEVICE_DATA_NOT_EXIST.getErrorMessage()));
		}
		if (deviceInfo.getCertification().equals(RegisteredDeviceConstant.L1)) {
			if (EmptyCheckUtils.isNullEmpty(deviceData.getFoundationalTrustProviderId())) {
				errors.add(new ServiceError(RegisteredDeviceErrorCode.FOUNDATIONAL_ID_MANDATORY.getErrorCode(),
						RegisteredDeviceErrorCode.FOUNDATIONAL_ID_MANDATORY.getErrorMessage()));
			}
		}

		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		if (deviceInfo != null) {
			if(deviceInfo.getTimestamp() == null) {
				errors.add(new ServiceError(RegisteredDeviceErrorCode.TIMESTAMP_CANNOTBE_NULL.getErrorCode(),
						RegisteredDeviceErrorCode.TIMESTAMP_CANNOTBE_NULL.getErrorMessage()));	

			}else {
				LocalDateTime timeStamp = deviceInfo.getTimestamp();
				String prefix = registerDeviceTimeStamp.substring(0, 1);
				String timeString = registerDeviceTimeStamp.replaceAll("\\" + prefix, "");
				boolean isBetween = timeStamp
						.isAfter(LocalDateTime.now(ZoneOffset.UTC).minus(Long.valueOf("2"), ChronoUnit.MINUTES))
						&& timeStamp.isBefore(
								LocalDateTime.now(ZoneOffset.UTC).plus(Long.valueOf(timeString), ChronoUnit.MINUTES));
				if (prefix.equals("+")) {
					if (!isBetween) {
						errors.add(new ServiceError(
								RegisteredDeviceErrorCode.TIMESTAMP_AFTER_CURRENTTIME.getErrorCode(),
								String.format(RegisteredDeviceErrorCode.TIMESTAMP_AFTER_CURRENTTIME.getErrorMessage(),
										timeString)));
					}
				} else if (prefix.equals("-")) {
					if (LocalDateTime.now(ZoneOffset.UTC)
							.isBefore(timeStamp.plus(Long.valueOf(timeString), ChronoUnit.MINUTES))) {
						errors.add(new ServiceError(
								RegisteredDeviceErrorCode.TIMESTAMP_BEFORE_CURRENTTIME.getErrorCode(),
								String.format(RegisteredDeviceErrorCode.TIMESTAMP_BEFORE_CURRENTTIME.getErrorMessage(),
										timeString)));
					}
				}
			}
		}
		Set<ConstraintViolation<DeviceInfo>> deviceInfoSet = validator.validate(deviceInfo);
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
			throw new DeviceValidationException(errors);
		}
	}

	/**
	 * 
	 * @param jws
	 * @return
	 */
	private String getPayLoad(String jws) {
		List<ServiceError> serviceErrors = new ArrayList<>();
		String[] split = jws.split("\\.");
		if (split.length > 2) {
			if (!verifySignature(jws)) {				
				serviceErrors.add(new ServiceError(
						RegisteredDeviceErrorCode.REGISTERED_DEVICE_SIGN_VALIDATION_FAILURE.getErrorCode(),
						RegisteredDeviceErrorCode.REGISTERED_DEVICE_SIGN_VALIDATION_FAILURE.getErrorMessage()));
				throw new DeviceValidationException(serviceErrors);
			}
			return split[1];
		}
		return jws;
	}

	protected boolean verifySignature(String jwsSignature) {
		try {
			return cryptoCore.verifySignature(jwsSignature);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 
	 */
	@Override
	public String deRegisterDevice(DeRegisterDevicePostDto deRegisterDevicePostDto) {
		List<ServiceError> serviceErrors = new ArrayList<>();
		RegRegisteredDevice deviceRegisterEntity = null;
		RegRegisteredDeviceHistory deviceRegisterHistory = new RegRegisteredDeviceHistory();
		String devicePayLoad = getPayLoad(deRegisterDevicePostDto.getDevice());
		try {
			DeRegisterDeviceReqDto device = mapper.readValue(CryptoUtil.decodeBase64(devicePayLoad),
					DeRegisterDeviceReqDto.class);
			validate(device);
			deviceRegisterEntity = registeredDeviceRepository.findByCodeAndIsActiveIsTrue(device.getDeviceCode());
			if (deviceRegisterEntity != null) {
				if (Arrays.asList(REVOKED, RETIRED).contains(deviceRegisterEntity.getStatusCode())) {					
					serviceErrors.add(new ServiceError(
							RegisteredDeviceErrorCode.DEVICE_DE_REGISTERED_ALREADY.getErrorCode(),
							RegisteredDeviceErrorCode.DEVICE_DE_REGISTERED_ALREADY.getErrorMessage()));
					throw new DeviceValidationException(serviceErrors);
				}
				deviceRegisterEntity.setStatusCode("Retired");
				Authentication authN = SecurityContextHolder.getContext().getAuthentication();
				if (!EmptyCheckUtils.isNullEmpty(authN)) {					
					deviceRegisterEntity.setUpdBy(authN.getName());
					
				}
				deviceRegisterEntity.setActive(false);
				deviceRegisterEntity.setUpdDtimes(LocalDateTime.now(ZoneId.of("UTC")));
				deviceRegisterHistory = mapUpdateHistory(deviceRegisterEntity, deviceRegisterHistory);
				deviceRegisterHistory.setEffectDateTime(deviceRegisterEntity.getUpdDtimes());
				deviceRegisterHistory.setCrDtimes(LocalDateTime.now(ZoneId.of("UTC")));
				registeredDeviceRepository.save(deviceRegisterEntity);
				registeredDeviceHistoryRepo.save(deviceRegisterHistory);
				DeviceDeRegisterResponse deviceDeRegisterResponse = new DeviceDeRegisterResponse();
				deviceDeRegisterResponse.setStatus("success");
				deviceDeRegisterResponse.setDeviceCode(deviceRegisterEntity.getCode());
				deviceDeRegisterResponse.setEnv(activeProfile);
				DigitalId digitalId = mapper.readValue(deviceRegisterEntity.getDigitalId(), DigitalId.class);
				deviceDeRegisterResponse.setTimeStamp(digitalId.getDateTime());				
				Objects.requireNonNull(deviceDeRegisterResponse);
				return getSignedResponse(CryptoUtil.encodeBase64String(mapper.writeValueAsBytes(deviceDeRegisterResponse)));
			} else {				
				serviceErrors.add(new ServiceError(RegisteredDeviceErrorCode.DEVICE_REGISTER_NOT_FOUND_EXCEPTION.getErrorCode(),
						RegisteredDeviceErrorCode.DEVICE_REGISTER_NOT_FOUND_EXCEPTION.getErrorMessage()));
				throw new DeviceValidationException(serviceErrors);
			}

		} catch (IOException e) {			
			serviceErrors.add(new ServiceError(
					RegisteredDeviceErrorCode.DEVICE_REGISTER_DELETED_EXCEPTION.getErrorCode(),
					RegisteredDeviceErrorCode.DEVICE_REGISTER_DELETED_EXCEPTION.getErrorMessage() + " " + e));
			throw new DeviceValidationException(serviceErrors);
		}		
	}

	/**
	 * 
	 * @param entity
	 * @param history
	 * @return
	 */
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
		history.setCrBy(entity.getUpdBy() == null? entity.getCrBy() : entity.getUpdBy());
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

	/**
	 * 
	 * @param device
	 */
	private void validate(DeRegisterDeviceReqDto device) {
		List<ServiceError> serviceErrors = new ArrayList<>();
		if (EmptyCheckUtils.isNullEmpty(device.getDeviceCode()) || EmptyCheckUtils.isNullEmpty(device.getEnv())) {			
			serviceErrors.add(new ServiceError(RegisteredDeviceErrorCode.DEVICE_REGISTER_NOT_FOUND_EXCEPTION.getErrorCode(),
					RegisteredDeviceErrorCode.DEVICE_REGISTER_NOT_FOUND_EXCEPTION.getErrorMessage()));
			throw new DeviceValidationException(serviceErrors);
		} else if (device.getDeviceCode().length() > 36) {
			serviceErrors.add(new ServiceError(RegisteredDeviceErrorCode.DEVICE_CODE_EXCEEDS_LENGTH.getErrorCode(),
					RegisteredDeviceErrorCode.DEVICE_CODE_EXCEEDS_LENGTH.getErrorMessage()));
			throw new DeviceValidationException(serviceErrors);
		} else if (!device.getEnv().equals(activeProfile)) {
			serviceErrors.add(new ServiceError(RegisteredDeviceErrorCode.INVALID_ENV.getErrorCode(),
					RegisteredDeviceErrorCode.INVALID_ENV.getErrorMessage()));
			throw new DeviceValidationException(serviceErrors);
		}
	}

	@PersistenceContext(unitName = "regDeviceEntityManagerFactory")
	private EntityManager entityManager;

	@Override
	public <E> PageResponseDto<RegisteredDevice> searchRegisteredDevice(Class<E> entity, SearchDto dto) {
		List<RegisteredDevice> partners=new ArrayList<>();
		PageResponseDto<RegisteredDevice> pageDto = new PageResponseDto<>();		
		Page<E> page =searchHelper.search(entityManager,entity, dto);
		if (page.getContent() != null && !page.getContent().isEmpty()) {
			partners=MapperUtils.mapAll(page.getContent(), RegisteredDevice.class);
		}
		pageDto.setData(partners);
		pageDto.setFromRecord(0);
		pageDto.setToRecord(page.getContent().size());
		pageDto.setTotalRecord(page.getContent().size());
		return pageDto;
	}

}
