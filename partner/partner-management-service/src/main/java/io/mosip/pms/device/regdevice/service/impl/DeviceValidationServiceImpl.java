package io.mosip.pms.device.regdevice.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.pms.common.entity.Partner;
import io.mosip.pms.common.repository.PartnerServiceRepository;
import io.mosip.pms.device.constant.DeviceConstant;
import io.mosip.pms.device.constant.DeviceValidationErrorCode;
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
import io.mosip.pms.device.request.dto.DigitalIdDto;
import io.mosip.pms.device.request.dto.ValidateDeviceDto;
import io.mosip.pms.device.response.dto.ResponseDto;
import io.mosip.pms.device.util.AuditUtil;

@Component
@Transactional
public class DeviceValidationServiceImpl implements DeviceValidationService {
	private static final String UTC_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	
	private static final String REGISTERED = "Registered";

	@Autowired
	AuditUtil auditUtil;

	@Autowired
	private RegRegisteredDeviceRepository registeredDeviceRepository;
	
	@Autowired
	private RegRegisteredDeviceHistoryRepository registeredDeviceHistoryRepository;

	@Autowired
	private PartnerServiceRepository deviceProviderRepository;

	@Autowired
	private RegSecureBiometricInterfaceRepository deviceServiceRepository;	
	
	@Autowired
	private RegSecureBiometricInterfaceHistoryRepository deviceServiceHistoryRepository;

	@Autowired
	private RegDeviceDetailRepository deviceDetailRepository;

	@Override
	public ResponseDto validateDeviceProviders(ValidateDeviceDto validateDeviceDto) {
		ResponseDto responseDto = new ResponseDto();
		if (StringUtils.isBlank(validateDeviceDto.getTimeStamp())) {
			RegRegisteredDevice registeredDevice = findRegisteredDevice(validateDeviceDto.getDeviceCode(),
					validateDeviceDto.getPurpose());
			isDeviceProviderPresent(validateDeviceDto.getDigitalId().getDpId());
			isValidServiceSoftwareVersion(validateDeviceDto.getDeviceServiceVersion());
			validateDeviceCodeAndDigitalId(registeredDevice, validateDeviceDto.getDigitalId());
			responseDto.setStatus(DeviceConstant.VALID);
			responseDto.setMessage("Device  details validated successfully");
		} else {
			responseDto = validateDeviceProviderHistory(validateDeviceDto);
		}
		return responseDto;
	}

	private ResponseDto validateDeviceProviderHistory(ValidateDeviceDto validateDeviceDto) {
		ResponseDto responseDto = new ResponseDto();
		responseDto.setStatus(DeviceConstant.INVALID);
		responseDto.setMessage("Device details history is invalid");
		LocalDateTime effTimes = parseToLocalDateTime(validateDeviceDto.getTimeStamp());
		RegRegisteredDeviceHistory registeredDeviceHistory = isRegisteredDeviceHistory(validateDeviceDto.getDeviceCode(),
				effTimes, validateDeviceDto.getPurpose());
		isValidServiceVersionFromHistory(validateDeviceDto.getDeviceServiceVersion(), effTimes);
		validateDigitalIdWithRegisteredDeviceHistory(registeredDeviceHistory, validateDeviceDto.getDigitalId());
		responseDto.setStatus(DeviceConstant.VALID);
		responseDto.setMessage("Device details history validated successfully");
		return responseDto;

	}

	private void validateDigitalIdWithRegisteredDeviceHistory(RegRegisteredDeviceHistory registeredDeviceHistory,
			DigitalIdDto digitalIdDto) {
		List<ServiceError> serviceErrors = new ArrayList<>();
		RegDeviceDetail deviceDetail = deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(registeredDeviceHistory.getDeviceDetailId());
		if (!deviceDetail.getMake().equals(digitalIdDto.getMake())) {
			ServiceError serviceError = new ServiceError();
			serviceError
					.setErrorCode(DeviceValidationErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(String.format(
					DeviceValidationErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorMessage(),
					DeviceConstant.MAKE));
			serviceErrors.add(serviceError);
		}
		if (!deviceDetail.getModel().equals(digitalIdDto.getModel())) {
			ServiceError serviceError = new ServiceError();
			serviceError
					.setErrorCode(DeviceValidationErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(String.format(
					DeviceValidationErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorMessage(),
					DeviceConstant.MODEL));
			serviceErrors.add(serviceError);
		}
		if (!deviceDetail.getDeviceProviderId().equals(digitalIdDto.getDpId())) {
			ServiceError serviceError = new ServiceError();
			serviceError
					.setErrorCode(DeviceValidationErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(String.format(
					DeviceValidationErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorMessage(),
					DeviceConstant.DP_ID));
			serviceErrors.add(serviceError);
		}
		if (!deviceDetail.getDeviceTypeCode().equals(digitalIdDto.getType())) {
			ServiceError serviceError = new ServiceError();
			serviceError
					.setErrorCode(DeviceValidationErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(DeviceValidationErrorCode.PROVIDER_AND_TYPE_MAPPED.getErrorMessage());
			serviceErrors.add(serviceError);
		}
		if (!deviceDetail.getDeviceSubTypeCode().equals(digitalIdDto.getDeviceSubType())) {
			ServiceError serviceError = new ServiceError();
			serviceError
					.setErrorCode(DeviceValidationErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(DeviceValidationErrorCode.PROVIDER_AND_SUBTYPE_MAPPED.getErrorMessage());
			serviceErrors.add(serviceError);
		}
		if (!serviceErrors.isEmpty()) {
			auditUtil.auditRequest(
					DeviceConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC, "PMS-ADM-999", serviceErrors.toString()), "ADM-613");
			throw new DeviceValidationException(serviceErrors);
		} else {
			serviceErrors = null;
		}
	}
	
	private boolean isValidServiceVersionFromHistory(String deviceServiceVersion, LocalDateTime effTimes) {
		List<RegSecureBiometricInterfaceHistory> deviceServiceHistory = null;
		List<ServiceError> serviceErrors = new ArrayList<>();
		try {
			deviceServiceHistory = deviceServiceHistoryRepository
					.findByIdAndIsActiveIsTrueAndByEffectiveTimes(deviceServiceVersion, effTimes);
		} catch (DataAccessException | DataAccessLayerException e) {			
			auditUtil.auditRequest(
					DeviceConstant.DEVICE_VALIDATION_HISTORY_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorCode(),
							DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorMessage()),
					"ADM-614");			
			serviceErrors.add(new ServiceError(DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorCode(),
					String.format(DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorMessage(),
							DeviceConstant.ERROR_OCCURED_MOSIP_DEVICE_SERVICE_HISTORY)));
			throw new DeviceValidationException(serviceErrors);
		}
		if (deviceServiceHistory.isEmpty()) {			
			auditUtil.auditRequest(
					DeviceConstant.DEVICE_VALIDATION_HISTORY_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							DeviceValidationErrorCode.SOFTWARE_VERSION_IS_NOT_A_MATCH.getErrorCode(),
							DeviceValidationErrorCode.SOFTWARE_VERSION_IS_NOT_A_MATCH.getErrorMessage()),
					"ADM-619");
			serviceErrors.add(new ServiceError(DeviceValidationErrorCode.SOFTWARE_VERSION_IS_NOT_A_MATCH.getErrorCode(),
					DeviceValidationErrorCode.SOFTWARE_VERSION_IS_NOT_A_MATCH.getErrorMessage()));			
			throw new DeviceValidationException(serviceErrors);
		}

		return true;
	}

	private LocalDateTime parseToLocalDateTime(String timeStamp) {
		return LocalDateTime.parse(timeStamp, DateTimeFormatter.ofPattern(UTC_DATETIME_PATTERN));	
	}

	private RegRegisteredDeviceHistory isRegisteredDeviceHistory(String deviceCode, LocalDateTime effTimes,
			String purpose) {
		RegRegisteredDeviceHistory registeredDeviceHistory = null;
		List<ServiceError> serviceErrors = new ArrayList<>();
		try {
			if (StringUtils.isBlank(purpose)) {
			registeredDeviceHistory = registeredDeviceHistoryRepository
					.findRegisteredDeviceHistoryByIdAndEffTimes(deviceCode, effTimes);
			} else {
				registeredDeviceHistory = registeredDeviceHistoryRepository
						.findRegisteredDeviceHistoryByIdAndEffTimesAndPurpose(deviceCode, effTimes,
								purpose.toUpperCase());
			}
		} catch (DataAccessException | DataAccessLayerException e) {
			auditUtil.auditRequest(
					DeviceConstant.DEVICE_VALIDATION_HISTORY_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorCode(),
							DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorMessage()),
					"ADM-615");
			serviceErrors.add(new ServiceError(DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorCode(),
					String.format(DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorMessage(),
							DeviceConstant.ERROR_OCCURED_REGISTERED_DEVICE_HISTORY)));
			throw new DeviceValidationException(serviceErrors);
		}

		if (registeredDeviceHistory == null) {
			auditUtil.auditRequest(
					DeviceConstant.DEVICE_VALIDATION_HISTORY_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							DeviceValidationErrorCode.DEVICE_DOES_NOT_EXIST.getErrorCode(),
							DeviceValidationErrorCode.DEVICE_DOES_NOT_EXIST.getErrorMessage()),
					"ADM-616");
			serviceErrors.add(new ServiceError(DeviceValidationErrorCode.DEVICE_DOES_NOT_EXIST.getErrorCode(),
					DeviceValidationErrorCode.DEVICE_DOES_NOT_EXIST.getErrorMessage()));
			throw new DeviceValidationException(serviceErrors);
		}
		if (!registeredDeviceHistory.getStatusCode().equalsIgnoreCase(REGISTERED)) {
			auditUtil.auditRequest(
					DeviceConstant.DEVICE_VALIDATION_HISTORY_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							DeviceValidationErrorCode.DEVICE_REVOKED_OR_RETIRED.getErrorCode(),
							DeviceValidationErrorCode.DEVICE_REVOKED_OR_RETIRED.getErrorMessage()),
					"ADM-617");
			serviceErrors.add(new ServiceError(DeviceValidationErrorCode.DEVICE_REVOKED_OR_RETIRED.getErrorCode(),
					DeviceValidationErrorCode.DEVICE_REVOKED_OR_RETIRED.getErrorMessage()));
			throw new DeviceValidationException(serviceErrors);
		}
		return registeredDeviceHistory;
	}

	private void validateDeviceCodeAndDigitalId(RegRegisteredDevice registeredDevice, DigitalIdDto digitalIdDto) {
		List<ServiceError> serviceErrors = new ArrayList<>();
		RegDeviceDetail deviceDetail = deviceDetailRepository.findByIdAndIsDeletedFalseOrIsDeletedIsNull(registeredDevice.getDeviceDetailId());
		if (!deviceDetail.getMake().equals(digitalIdDto.getMake())) {
			ServiceError serviceError = new ServiceError();
			serviceError
			.setErrorCode(DeviceValidationErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(String.format(
					DeviceValidationErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorMessage(),
					DeviceConstant.MAKE));
			serviceErrors.add(serviceError);
		}
		if (!deviceDetail.getModel().equals(digitalIdDto.getModel())) {
			ServiceError serviceError = new ServiceError();
			serviceError
			.setErrorCode(DeviceValidationErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(String.format(
					DeviceValidationErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorMessage(),
					DeviceConstant.MODEL));
			serviceErrors.add(serviceError);
		}
		if (!deviceDetail.getDeviceProviderId().equals(digitalIdDto.getDpId())) {
			ServiceError serviceError = new ServiceError();
			serviceError
			.setErrorCode(DeviceValidationErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(String.format(
					DeviceValidationErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorMessage(),
					DeviceConstant.DP_ID));
			serviceErrors.add(serviceError);
		}
		if (!registeredDevice.getSerialNo().equals(digitalIdDto.getSerialNo())) {
			ServiceError serviceError = new ServiceError();
			serviceError
			.setErrorCode(DeviceValidationErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(String.format(
					DeviceValidationErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorMessage(),
					DeviceConstant.SERIAL_NO));
			serviceErrors.add(serviceError);
		}
		if (!deviceDetail.getDeviceTypeCode().equals(digitalIdDto.getType())) {
			ServiceError serviceError = new ServiceError();
			serviceError
			.setErrorCode(DeviceValidationErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(DeviceValidationErrorCode.PROVIDER_AND_TYPE_MAPPED.getErrorMessage());
			serviceErrors.add(serviceError);
		}
		if (!deviceDetail.getDeviceSubTypeCode().equals(digitalIdDto.getDeviceSubType())) {
			ServiceError serviceError = new ServiceError();
			serviceError
			.setErrorCode(DeviceValidationErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(DeviceValidationErrorCode.PROVIDER_AND_SUBTYPE_MAPPED.getErrorMessage());
			serviceErrors.add(serviceError);
		}
		if (!serviceErrors.isEmpty()) {
			auditUtil.auditRequest(
					DeviceConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC, "KER-ADM-999", serviceErrors.toString()), "ADM-613");
			throw new DeviceValidationException(serviceErrors);
		} else {
			serviceErrors = null;
		}
	}	

	private boolean isValidServiceSoftwareVersion(String deviceServiceVersion) {
		List<RegSecureBiometricInterface> deviceServices = null;
		List<ServiceError> serviceErrors = new ArrayList<>();
		try {
			deviceServices = deviceServiceRepository.findBySwVersionAndIsActiveIsTrue(deviceServiceVersion);
		} catch (DataAccessException | DataAccessLayerException e) {
			auditUtil.auditRequest(
					DeviceConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorCode(),
							DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorMessage()),
					"ADM-608");
			serviceErrors.add(new ServiceError(DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorCode(),
					String.format(DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorMessage(),
							DeviceConstant.ERROR_OCCURED_MOSIP_DEVICE_SERVICE)));
			throw new DeviceValidationException(serviceErrors);
		}
		if (deviceServices.isEmpty()) {
			auditUtil.auditRequest(
					DeviceConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							DeviceValidationErrorCode.MDS_DOES_NOT_EXIST.getErrorCode(),
							DeviceValidationErrorCode.MDS_DOES_NOT_EXIST.getErrorMessage()),
					"ADM-609");
			serviceErrors.add(new ServiceError(DeviceValidationErrorCode.MDS_DOES_NOT_EXIST.getErrorCode(),
					DeviceValidationErrorCode.MDS_DOES_NOT_EXIST.getErrorMessage()));
			throw new DeviceValidationException(serviceErrors);
		}

		return true;
	}

	private boolean isDeviceProviderPresent(String dpId) {
		Partner deviceProvider = null;
		List<ServiceError> serviceErrors = new ArrayList<>();
		try {
			deviceProvider = deviceProviderRepository.findByIdAndIsActiveIsTrue(dpId);
		} catch (DataAccessException | DataAccessLayerException e) {
			auditUtil.auditRequest(
					DeviceConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorCode(),
							DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorMessage()),
					"ADM-606");
			serviceErrors.add(new ServiceError(DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorCode(),
					String.format(DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorMessage(),
							DeviceConstant.ERROR_OCCURED_DEVICE_PROVIDER)));
			throw new DeviceValidationException(serviceErrors);
		}
		if (deviceProvider == null) {
			auditUtil.auditRequest(
					DeviceConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.FAILURE_DESC,
							DeviceValidationErrorCode.DEVICE_PROVIDER_INACTIVE.getErrorCode(),
							DeviceValidationErrorCode.DEVICE_PROVIDER_INACTIVE.getErrorMessage()),
					"ADM-607");
			serviceErrors.add(new ServiceError(DeviceValidationErrorCode.DEVICE_PROVIDER_INACTIVE.getErrorCode(),
					DeviceValidationErrorCode.DEVICE_PROVIDER_INACTIVE.getErrorMessage()));
			throw new DeviceValidationException(serviceErrors);
		}
		return true;
	}

	private RegRegisteredDevice findRegisteredDevice(String deviceCode, String purpose) {
		RegRegisteredDevice registeredDevice = null;
		List<ServiceError> serviceErrors = new ArrayList<>();
		try {
			if (StringUtils.isBlank(purpose)) {
				registeredDevice = registeredDeviceRepository.findByCodeAndIsActiveIsTrue(deviceCode);
			} else {
				registeredDevice = registeredDeviceRepository.findByCodeAndPurposeIgnoreCaseAndIsActiveIsTrue(deviceCode,
						purpose);
			}
		}catch (DataAccessException | DataAccessLayerException e) {
			auditUtil.auditRequest(
					DeviceConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.DEVICE_VALIDATION_API_CALLED,
							DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorCode(),
							DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorMessage()),
					"ADM-605");
			serviceErrors.add(new ServiceError(DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorCode(),
					String.format(DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorMessage(),
							DeviceConstant.ERROR_OCCURED_REGISTERED_DEVICE)));
			throw new DeviceValidationException(serviceErrors);
		}
		if (registeredDevice == null) {
			auditUtil.auditRequest(
					DeviceConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.DEVICE_VALIDATION_API_CALLED,
							DeviceValidationErrorCode.DEVICE_DOES_NOT_EXIST.getErrorCode(),
							DeviceValidationErrorCode.DEVICE_DOES_NOT_EXIST.getErrorMessage()),
					"ADM-606");

			serviceErrors.add(new ServiceError(DeviceValidationErrorCode.DEVICE_DOES_NOT_EXIST.getErrorCode(),
					DeviceValidationErrorCode.DEVICE_DOES_NOT_EXIST.getErrorMessage()));
			throw new DeviceValidationException(serviceErrors);
		}
		if (!registeredDevice.getStatusCode().equalsIgnoreCase(REGISTERED)) {
			auditUtil.auditRequest(
					DeviceConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					DeviceConstant.AUDIT_SYSTEM,
					String.format(DeviceConstant.DEVICE_VALIDATION_API_CALLED,
							DeviceValidationErrorCode.DEVICE_REVOKED_OR_RETIRED.getErrorCode(),
							DeviceValidationErrorCode.DEVICE_REVOKED_OR_RETIRED.getErrorMessage()),
					"ADM-607");
			serviceErrors.add(new ServiceError(DeviceValidationErrorCode.DEVICE_REVOKED_OR_RETIRED.getErrorCode(),
					DeviceValidationErrorCode.DEVICE_REVOKED_OR_RETIRED.getErrorMessage()));
			throw new DeviceValidationException(serviceErrors);
		}

		return registeredDevice;
	}
}