package io.mosip.pmp.authdevice.service.impl;

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

import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
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
import io.mosip.pmp.authdevice.dto.RegisterDeviceResponse;
import io.mosip.pmp.authdevice.dto.RegisteredDevicePostDto;
import io.mosip.pmp.authdevice.dto.SignRequestDto;
import io.mosip.pmp.authdevice.dto.SignResponseDto;
import io.mosip.pmp.authdevice.entity.DeviceDetail;
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
import io.mosip.pmp.authdevice.util.HeaderRequest;
import io.mosip.pmp.authdevice.util.RegisteredDeviceConstant;
import io.mosip.pmp.partner.core.RequestWrapper;
import io.mosip.pmp.partner.core.ResponseWrapper;
import io.mosip.pmp.partner.util.RestUtil;
import io.mosip.pmp.regdevice.service.RegRegisteredDeviceService;

@Component
@Transactional
public class RegisteredDeviceServiceImpl implements RegisteredDeviceService {

	@Autowired
	RegisteredDeviceRepository registeredDeviceRepository;

	@Autowired
	RegisteredDeviceHistoryRepository registeredDeviceHistoryRepo;

	@Autowired
	FoundationalTrustProviderRepository ftpRepo;

	@Autowired
	private CryptoCore cryptoCore;

	@Autowired
	Environment environment;

	@Autowired
	DeviceDetailRepository deviceDetailRepository;

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

	@Autowired
	RegRegisteredDeviceService regRegisteredDeviceService;

	/**
	 * 
	 */
	@Override
	public String signedRegisteredDevice(RegisteredDevicePostDto registeredDevicePostDto) throws Exception {
		RegisteredDevice mapEntity = null;
		RegisteredDevice crtRegisteredDevice = null;
		RegisteredDeviceHistory entityHistory = new RegisteredDeviceHistory();
		String digitalIdJson;
		DeviceResponse response = new DeviceResponse();
		DeviceData deviceData = null;
		DigitalId digitalId = null;
		DeviceDetail deviceDetail = null;
		RegisterDeviceResponse registerDeviceResponse = null;
		String headerString, signedResponse, registerDevice = null;
		try {
			String deviceDataPayLoad = getPayLoad(registeredDevicePostDto.getDeviceData());
			deviceData = mapper.readValue(CryptoUtil.decodeBase64(deviceDataPayLoad), DeviceData.class);
			validate(deviceData);
			if (deviceData.getPurpose().equalsIgnoreCase(RegisteredDeviceConstant.REGISTRATION)) {
				return regRegisteredDeviceService.signedRegisteredDevice(registeredDevicePostDto);
			}
			String digitalIdPayLoad = getPayLoad(deviceData.getDeviceInfo().getDigitalId());
			digitalId = mapper.readValue(CryptoUtil.decodeBase64(digitalIdPayLoad), DigitalId.class);
			validate(digitalId);
			deviceDetail = deviceDetailRepository.findByDeviceDetail(digitalId.getMake(), digitalId.getModel(),
					digitalId.getDeviceProviderId(), digitalId.getDeviceSubType(), digitalId.getType());
			if (deviceDetail == null) {
				throw new RequestException(RegisteredDeviceErrorCode.DEVICE_DETAIL_NOT_FOUND.getErrorCode(),
						RegisteredDeviceErrorCode.DEVICE_DETAIL_NOT_FOUND.getErrorMessage());
			} else {
				mapEntity = new RegisteredDevice();
				mapEntity.setDeviceDetailId(deviceDetail.getId());
			}

			if (deviceData.getFoundationalTrustProviderId() != null
					&& !deviceData.getFoundationalTrustProviderId().isEmpty()) {
				if (ftpRepo.findByIdAndIsActiveTrue(deviceData.getFoundationalTrustProviderId()) == null) {
					throw new RequestException(RegisteredDeviceErrorCode.FTP_NOT_FOUND.getErrorCode(),
							RegisteredDeviceErrorCode.FTP_NOT_FOUND.getErrorMessage());
				}
			} else {
				deviceData.setFoundationalTrustProviderId(null);
			}

			RegisteredDevice regDevice = registeredDeviceRepository
					.findByDeviceDetailIdAndSerialNoAndIsActiveIsTrue(deviceDetail.getId(), digitalId.getSerialNo());

			if (regDevice != null) {
				throw new RequestException(RegisteredDeviceErrorCode.SERIALNO_DEVICEDETAIL_ALREADY_EXIST.getErrorCode(),
						RegisteredDeviceErrorCode.SERIALNO_DEVICEDETAIL_ALREADY_EXIST.getErrorMessage());
			}

			digitalIdJson = mapper.writeValueAsString(digitalId);
			mapEntity = mapRegisteredDeviceDto(registeredDevicePostDto, digitalIdJson, deviceData, deviceDetail,
					digitalId);
			mapEntity.setCode(UUID.randomUUID().toString());
			entityHistory.setCode(mapEntity.getCode());
			entityHistory.setEffectDateTime(mapEntity.getCrDtimes());
			entityHistory = mapRegisteredDeviceHistory(entityHistory, mapEntity);
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

	/**
	 * 
	 * @param headerString
	 * @param registerDevice
	 * @param signedResponse
	 * @return
	 */
	private String convertToJWS(String headerString, String registerDevice, String signedResponse) {
		return CryptoUtil.encodeBase64String(headerString.getBytes()) + "."
				+ CryptoUtil.encodeBase64String(registerDevice.getBytes()) + "."
				+ CryptoUtil.encodeBase64String(signedResponse.getBytes());
	}

	/**
	 * 
	 * @param registerDeviceResponse
	 * @return
	 */
	private String getSignedResponse(RegisterDeviceResponse registerDeviceResponse) {
		RequestWrapper<SignRequestDto> request = new RequestWrapper<>();
		SignRequestDto signatureRequestDto = new SignRequestDto();
		SignResponseDto signResponse = new SignResponseDto();
		try {
			signatureRequestDto
					.setData(CryptoUtil.encodeBase64String(mapper.writeValueAsBytes(registerDeviceResponse)));
			request.setRequest(signatureRequestDto);
			String response = restUtil.postApi(signUrl, null, "", "", MediaType.APPLICATION_JSON, request,
					String.class);
			ResponseWrapper<?> responseObject;
			responseObject = mapper.readValue(response, ResponseWrapper.class);
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

	/**
	 * 
	 * @param entity
	 * @param deviceData
	 * @return
	 */
	private RegisterDeviceResponse mapRegisteredDeviceResponse(RegisteredDevice entity, DeviceData deviceData) {
		RegisterDeviceResponse registerDeviceResponse = new RegisterDeviceResponse();
		registerDeviceResponse.setDeviceCode(entity.getCode());
		registerDeviceResponse.setStatus(entity.getStatusCode());
		registerDeviceResponse.setTimeStamp(deviceData.getDeviceInfo().getTimeStamp());
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
	private RegisteredDevice mapRegisteredDeviceDto(RegisteredDevicePostDto registeredDevicePostDto,
			String digitalIdJson, DeviceData deviceData, DeviceDetail deviceDetail, DigitalId digitalId) {
		RegisteredDevice entity = new RegisteredDevice();
		entity.setDeviceDetailId(deviceDetail.getId());
		entity.setStatusCode(REGISTERED);
		entity.setDeviceId(deviceData.getDeviceId());
		entity.setDeviceSubId(deviceData.getDeviceInfo().getDeviceSubId());
		entity.setHotlisted(false);
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

	/**
	 * 
	 * @param history
	 * @param entity
	 * @return
	 */
	private RegisteredDeviceHistory mapRegisteredDeviceHistory(RegisteredDeviceHistory history,
			RegisteredDevice entity) {

		history.setDeviceDetailId(entity.getDeviceDetailId());
		history.setStatusCode(entity.getStatusCode());
		history.setDeviceId(entity.getDeviceId());
		history.setDeviceSubId(entity.getDeviceSubId());
		history.setHotlisted(entity.isHotlisted());
		history.setDigitalId(entity.getDigitalId());
		history.setSerialNo(entity.getSerialNo());

		history.setCrBy(entity.getCrBy());

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

			throw new ValidationException(errors);
		}

		if (deviceData.getDeviceInfo() != null) {
			LocalDateTime timeStamp = deviceData.getDeviceInfo().getTimeStamp();
			String prefix = registerDeviceTimeStamp.substring(0, 1);
			String timeString = registerDeviceTimeStamp.replaceAll("\\" + prefix, "");
			boolean isBetween = timeStamp
					.isAfter(LocalDateTime.now(ZoneOffset.UTC).minus(Long.valueOf("2"), ChronoUnit.MINUTES))
					&& timeStamp.isBefore(
							LocalDateTime.now(ZoneOffset.UTC).plus(Long.valueOf(timeString), ChronoUnit.MINUTES));
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

			throw new ValidationException(errors);
		}

	}

	/**
	 * 
	 * @param jws
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	private String getPayLoad(String jws)
			throws JsonParseException, JsonMappingException, JsonProcessingException, IOException {
		String[] split = jws.split("\\.");
		if (split.length > 2) {

			if (cryptoCore.verifySignature(new String(CryptoUtil.decodeBase64(split[2])))) {
				return split[1];

			} else {
				throw new AuthDeviceServiceException(
						RegisteredDeviceErrorCode.REGISTERED_DEVICE_SIGN_VALIDATION_FAILURE.getErrorCode(),
						RegisteredDeviceErrorCode.REGISTERED_DEVICE_SIGN_VALIDATION_FAILURE.getErrorMessage());
			}

		}
		return jws;
	}

	/**
	 * 
	 */
	@Override
	public String deRegisterDevice(DeRegisterDevicePostDto deRegisterDevicePostDto) {
		RegisteredDevice deviceRegisterEntity = null;
		RegisteredDeviceHistory deviceRegisterHistory = new RegisteredDeviceHistory();
		String headerString, signedResponse, deRegisterDevice = null;
		try {
			String devicePayLoad = getPayLoad(deRegisterDevicePostDto.getDevice());
			DeRegisterDeviceReqDto device = mapper.readValue(CryptoUtil.decodeBase64(devicePayLoad),
					DeRegisterDeviceReqDto.class);
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
				deviceRegisterEntity.setUpdDtimes(LocalDateTime.now(ZoneId.of("UTC")));
				deviceRegisterHistory.setCode(deviceRegisterEntity.getCode());
				deviceRegisterHistory.setEffectDateTime(deviceRegisterEntity.getUpdDtimes());
				registeredDeviceRepository.save(deviceRegisterEntity);
				deviceRegisterHistory = mapUpdateHistory(deviceRegisterEntity, deviceRegisterHistory);
				deviceRegisterHistory.setEffectDateTime(deviceRegisterEntity.getUpdDtimes());
				registeredDeviceHistoryRepo.save(deviceRegisterHistory);
				DeviceDeRegisterResponse deviceDeRegisterResponse = new DeviceDeRegisterResponse();
				deviceDeRegisterResponse.setStatus("success");
				deviceDeRegisterResponse.setDeviceCode(deviceRegisterEntity.getCode());
				deviceDeRegisterResponse.setEnv(activeProfile);
				DigitalId digitalId = mapper.readValue(deviceRegisterEntity.getDigitalId(), DigitalId.class);
				deviceDeRegisterResponse.setTimeStamp(digitalId.getDateTime());
				HeaderRequest header = new HeaderRequest();
				header.setAlg("RS256");
				header.setType("JWS");
				headerString = mapper.writeValueAsString(header);
				Objects.requireNonNull(deviceDeRegisterResponse);
				signedResponse = getSignedResponse(deviceDeRegisterResponse);
				deRegisterDevice = mapper.writeValueAsString(deviceDeRegisterResponse);
			} else {

				throw new RequestException(RegisteredDeviceErrorCode.DEVICE_REGISTER_NOT_FOUND_EXCEPTION.getErrorCode(),
						RegisteredDeviceErrorCode.DEVICE_REGISTER_NOT_FOUND_EXCEPTION.getErrorMessage());
			}

		} catch (IOException e) {
			throw new AuthDeviceServiceException(
					RegisteredDeviceErrorCode.DEVICE_REGISTER_DELETED_EXCEPTION.getErrorCode(),
					RegisteredDeviceErrorCode.DEVICE_REGISTER_DELETED_EXCEPTION.getErrorMessage() + " " + e);
		}

		return convertToJWS(headerString, deRegisterDevice, signedResponse);
	}

	/**
	 * 
	 * @param entity
	 * @param history
	 * @return
	 */
	private RegisteredDeviceHistory mapUpdateHistory(RegisteredDevice entity, RegisteredDeviceHistory history) {
		history.setDeviceDetailId(entity.getDeviceDetailId());
		history.setStatusCode(entity.getStatusCode());
		history.setDeviceId(entity.getDeviceId());
		history.setDeviceSubId(entity.getDeviceSubId());
		history.setHotlisted(entity.isHotlisted());
		history.setDigitalId(entity.getDigitalId());
		history.setSerialNo(entity.getSerialNo());

		history.setCrBy(entity.getUpdBy());

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
	 * @param registerDeviceResponse
	 * @return
	 * @throws IOException
	 */
	private String getSignedResponse(DeviceDeRegisterResponse registerDeviceResponse) throws IOException {
		RequestWrapper<SignRequestDto> request = new RequestWrapper<>();
		SignRequestDto signatureRequestDto = new SignRequestDto();
		SignResponseDto signResponse = new SignResponseDto();
		signatureRequestDto.setData(CryptoUtil.encodeBase64String(mapper.writeValueAsBytes(registerDeviceResponse)));
		request.setRequest(signatureRequestDto);
		String response = restUtil.postApi(signUrl, null, "", "", MediaType.APPLICATION_JSON, request, String.class);
		ResponseWrapper<?> responseObject = mapper.readValue(response, ResponseWrapper.class);
		signResponse = mapper.readValue(mapper.writeValueAsString(responseObject.getResponse()), SignResponseDto.class);
		return signResponse.getSignature();
	}

	/**
	 * 
	 * @param device
	 */
	private void validate(DeRegisterDeviceReqDto device) {
		if (EmptyCheckUtils.isNullEmpty(device.getDeviceCode()) || EmptyCheckUtils.isNullEmpty(device.getEnv())) {
			throw new RequestException(RegisteredDeviceErrorCode.DEVICE_REGISTER_NOT_FOUND_EXCEPTION.getErrorCode(),
					RegisteredDeviceErrorCode.DEVICE_REGISTER_NOT_FOUND_EXCEPTION.getErrorMessage());
		} else if (device.getDeviceCode().length() > 36) {
			throw new RequestException(RegisteredDeviceErrorCode.DEVICE_CODE_EXCEEDS_LENGTH.getErrorCode(),
					RegisteredDeviceErrorCode.DEVICE_CODE_EXCEEDS_LENGTH.getErrorMessage());
		} else if (!device.getEnv().equals(activeProfile)) {
			throw new RequestException(RegisteredDeviceErrorCode.INVALID_ENV.getErrorCode(),
					RegisteredDeviceErrorCode.INVALID_ENV.getErrorMessage());
		}

	}
}
