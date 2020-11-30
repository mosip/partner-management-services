package io.mosip.pmp.regdevice.service.impl;

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
import io.mosip.pmp.authdevice.constants.DeviceValidationErrorCode;
import io.mosip.pmp.authdevice.dto.DigitalIdDto;
import io.mosip.pmp.authdevice.dto.ResponseDto;
import io.mosip.pmp.authdevice.dto.ValidateDeviceDto;
import io.mosip.pmp.authdevice.exception.AuthDeviceServicesException;
import io.mosip.pmp.authdevice.exception.RequestsException;
import io.mosip.pmp.authdevice.exception.ValidationException;
import io.mosip.pmp.authdevice.util.AuditUtil;
import io.mosip.pmp.authdevice.util.AuthDeviceConstant;
import io.mosip.pmp.partner.entity.Partner;
import io.mosip.pmp.partner.repository.PartnerServiceRepository;
import io.mosip.pmp.regdevice.entity.RegDeviceDetail;
import io.mosip.pmp.regdevice.entity.RegRegisteredDevice;
import io.mosip.pmp.regdevice.entity.RegRegisteredDeviceHistory;
import io.mosip.pmp.regdevice.entity.RegSecureBiometricInterface;
import io.mosip.pmp.regdevice.entity.RegSecureBiometricInterfaceHistory;
import io.mosip.pmp.regdevice.repository.RegDeviceDetailRepository;
import io.mosip.pmp.regdevice.repository.RegRegisteredDeviceHistoryRepository;
import io.mosip.pmp.regdevice.repository.RegRegisteredDeviceRepository;
import io.mosip.pmp.regdevice.repository.RegSecureBiometricInterfaceHistoryRepository;
import io.mosip.pmp.regdevice.repository.RegSecureBiometricInterfaceRepository;
import io.mosip.pmp.regdevice.service.DeviceValidationService;

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
			responseDto.setStatus(AuthDeviceConstant.VALID);
			responseDto.setMessage("Device  details validated successfully");
		} else {
			responseDto = validateDeviceProviderHistory(validateDeviceDto);
		}
		return responseDto;
	}

	private ResponseDto validateDeviceProviderHistory(ValidateDeviceDto validateDeviceDto) {
		ResponseDto responseDto = new ResponseDto();
		responseDto.setStatus(AuthDeviceConstant.INVALID);
		responseDto.setMessage("Device details history is invalid");
		LocalDateTime effTimes = parseToLocalDateTime(validateDeviceDto.getTimeStamp());
		RegRegisteredDeviceHistory registeredDeviceHistory = isRegisteredDeviceHistory(validateDeviceDto.getDeviceCode(),
				effTimes, validateDeviceDto.getPurpose());
		isValidServiceVersionFromHistory(validateDeviceDto.getDeviceServiceVersion(), effTimes);
		validateDigitalIdWithRegisteredDeviceHistory(registeredDeviceHistory, validateDeviceDto.getDigitalId());
		responseDto.setStatus(AuthDeviceConstant.VALID);
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
					AuthDeviceConstant.MAKE));
			serviceErrors.add(serviceError);
		}
		if (!deviceDetail.getModel().equals(digitalIdDto.getModel())) {
			ServiceError serviceError = new ServiceError();
			serviceError
					.setErrorCode(DeviceValidationErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(String.format(
					DeviceValidationErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorMessage(),
					AuthDeviceConstant.MODEL));
			serviceErrors.add(serviceError);
		}
		if (!deviceDetail.getDeviceProviderId().equals(digitalIdDto.getDpId())) {
			ServiceError serviceError = new ServiceError();
			serviceError
					.setErrorCode(DeviceValidationErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(String.format(
					DeviceValidationErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorMessage(),
					AuthDeviceConstant.DP_ID));
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
					AuthDeviceConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC, "PMS-ADM-999", serviceErrors.toString()), "ADM-613");
			throw new ValidationException(serviceErrors);
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
			ServiceError serviceError = new ServiceError();
			auditUtil.auditRequest(
					AuthDeviceConstant.DEVICE_VALIDATION_HISTORY_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC,
							DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorCode(),
							DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorMessage()),
					"ADM-614");
			serviceError = new ServiceError(DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorCode(),
					String.format(DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorMessage(),
							AuthDeviceConstant.ERROR_OCCURED_MOSIP_DEVICE_SERVICE_HISTORY));
			serviceErrors.add(serviceError);
			throw new RequestsException(serviceErrors);
		}
		if (deviceServiceHistory.isEmpty()) {
			ServiceError serviceError = new ServiceError();
			auditUtil.auditRequest(
					AuthDeviceConstant.DEVICE_VALIDATION_HISTORY_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC,
							DeviceValidationErrorCode.SOFTWARE_VERSION_IS_NOT_A_MATCH.getErrorCode(),
							DeviceValidationErrorCode.SOFTWARE_VERSION_IS_NOT_A_MATCH.getErrorMessage()),
					"ADM-619");
			serviceError = new ServiceError(DeviceValidationErrorCode.SOFTWARE_VERSION_IS_NOT_A_MATCH.getErrorCode(),
					DeviceValidationErrorCode.SOFTWARE_VERSION_IS_NOT_A_MATCH.getErrorMessage());
			serviceErrors.add(serviceError);
			throw new RequestsException(serviceErrors);

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
			ServiceError serviceError = new ServiceError();
			auditUtil.auditRequest(
					AuthDeviceConstant.DEVICE_VALIDATION_HISTORY_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC,
							DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorCode(),
							DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorMessage()),
					"ADM-615");
			serviceError.setErrorCode(DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorCode());
			serviceError.setMessage(String.format(DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorMessage(),
					AuthDeviceConstant.ERROR_OCCURED_REGISTERED_DEVICE_HISTORY));
			serviceErrors.add(serviceError);
			throw new RequestsException(serviceErrors);
		}

		if (registeredDeviceHistory == null) {
			ServiceError serviceError = new ServiceError();
			auditUtil.auditRequest(
					AuthDeviceConstant.DEVICE_VALIDATION_HISTORY_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC,
							DeviceValidationErrorCode.DEVICE_DOES_NOT_EXIST.getErrorCode(),
							DeviceValidationErrorCode.DEVICE_DOES_NOT_EXIST.getErrorMessage()),
					"ADM-616");
			serviceError = new ServiceError(DeviceValidationErrorCode.DEVICE_DOES_NOT_EXIST.getErrorCode(),
					DeviceValidationErrorCode.DEVICE_DOES_NOT_EXIST.getErrorMessage());
			
			serviceErrors.add(serviceError);
			throw new RequestsException(serviceErrors);
		}
		if (!registeredDeviceHistory.getStatusCode().equalsIgnoreCase(REGISTERED)) {
			ServiceError serviceError = new ServiceError();
			auditUtil.auditRequest(
					AuthDeviceConstant.DEVICE_VALIDATION_HISTORY_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC,
							DeviceValidationErrorCode.DEVICE_REVOKED_OR_RETIRED.getErrorCode(),
							DeviceValidationErrorCode.DEVICE_REVOKED_OR_RETIRED.getErrorMessage()),
					"ADM-617");
			serviceError = new ServiceError(DeviceValidationErrorCode.DEVICE_REVOKED_OR_RETIRED.getErrorCode(),
					DeviceValidationErrorCode.DEVICE_REVOKED_OR_RETIRED.getErrorMessage());
			serviceErrors.add(serviceError);
			throw new RequestsException(serviceErrors);
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
					AuthDeviceConstant.MAKE));
			serviceErrors.add(serviceError);
		}
		if (!deviceDetail.getModel().equals(digitalIdDto.getModel())) {
			ServiceError serviceError = new ServiceError();
			serviceError
			.setErrorCode(DeviceValidationErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(String.format(
					DeviceValidationErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorMessage(),
					AuthDeviceConstant.MODEL));
			serviceErrors.add(serviceError);
		}
		if (!deviceDetail.getDeviceProviderId().equals(digitalIdDto.getDpId())) {
			ServiceError serviceError = new ServiceError();
			serviceError
			.setErrorCode(DeviceValidationErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(String.format(
					DeviceValidationErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorMessage(),
					AuthDeviceConstant.DP_ID));
			serviceErrors.add(serviceError);
		}
		if (!registeredDevice.getSerialNo().equals(digitalIdDto.getSerialNo())) {
			ServiceError serviceError = new ServiceError();
			serviceError
			.setErrorCode(DeviceValidationErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorCode());
			serviceError.setMessage(String.format(
					DeviceValidationErrorCode.PROVIDER_AND_DEVICE_CODE_NOT_MAPPED.getErrorMessage(),
					AuthDeviceConstant.SERIAL_NO));
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
					AuthDeviceConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC, "KER-ADM-999", serviceErrors.toString()), "ADM-613");
			throw new ValidationException(serviceErrors);
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
			ServiceError serviceError = new ServiceError();
			auditUtil.auditRequest(
					AuthDeviceConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC,
							DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorCode(),
							DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorMessage()),
					"ADM-608");
			serviceError = new ServiceError(DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorCode(),
					String.format(DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorMessage(),
							AuthDeviceConstant.ERROR_OCCURED_MOSIP_DEVICE_SERVICE));
			serviceErrors.add(serviceError);
			throw new AuthDeviceServicesException(serviceErrors);
		}
		if (deviceServices.isEmpty()) {
			ServiceError serviceError = new ServiceError();
			auditUtil.auditRequest(
					AuthDeviceConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC,
							DeviceValidationErrorCode.MDS_DOES_NOT_EXIST.getErrorCode(),
							DeviceValidationErrorCode.MDS_DOES_NOT_EXIST.getErrorMessage()),
					"ADM-609");
			serviceError = new ServiceError(DeviceValidationErrorCode.MDS_DOES_NOT_EXIST.getErrorCode(),
					DeviceValidationErrorCode.MDS_DOES_NOT_EXIST.getErrorMessage());
			serviceErrors.add(serviceError);
			throw new AuthDeviceServicesException(serviceErrors);
		}

		return true;
	}

	private boolean isDeviceProviderPresent(String dpId) {
		Partner deviceProvider = null;
		List<ServiceError> serviceErrors = new ArrayList<>();	
		try {
			deviceProvider = deviceProviderRepository.findByIdAndIsActiveIsTrue(dpId);
		} catch (DataAccessException | DataAccessLayerException e) {
			ServiceError serviceError = new ServiceError();
			auditUtil.auditRequest(
					AuthDeviceConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC,
							DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorCode(),
							DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorMessage()),
					"ADM-606");
			serviceError = new ServiceError(DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorCode(),
					String.format(DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorMessage(),
							AuthDeviceConstant.ERROR_OCCURED_DEVICE_PROVIDER));
			serviceErrors.add(serviceError);
			throw new AuthDeviceServicesException(serviceErrors);
		}
		if (deviceProvider == null) {
			ServiceError serviceError = new ServiceError();
			auditUtil.auditRequest(
					AuthDeviceConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC,
							DeviceValidationErrorCode.DEVICE_PROVIDER_INACTIVE.getErrorCode(),
							DeviceValidationErrorCode.DEVICE_PROVIDER_INACTIVE.getErrorMessage()),
					"ADM-607");
			serviceError = new ServiceError(DeviceValidationErrorCode.DEVICE_PROVIDER_INACTIVE.getErrorCode(),
					DeviceValidationErrorCode.DEVICE_PROVIDER_INACTIVE.getErrorMessage());
			serviceErrors.add(serviceError);
			throw new AuthDeviceServicesException(serviceErrors);
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
			ServiceError serviceError = new ServiceError();
			auditUtil.auditRequest(
					AuthDeviceConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.DEVICE_VALIDATION_API_CALLED,
							DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorCode(),
							DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorMessage()),
					"ADM-605");
			serviceError = new ServiceError(DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorCode(),
					String.format(DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorMessage(),
							AuthDeviceConstant.ERROR_OCCURED_REGISTERED_DEVICE));
			serviceErrors.add(serviceError);
			throw new AuthDeviceServicesException(serviceErrors);
		}
		if (registeredDevice == null) {
			ServiceError serviceError = new ServiceError();
			auditUtil.auditRequest(
					AuthDeviceConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.DEVICE_VALIDATION_API_CALLED,
							DeviceValidationErrorCode.DEVICE_DOES_NOT_EXIST.getErrorCode(),
							DeviceValidationErrorCode.DEVICE_DOES_NOT_EXIST.getErrorMessage()),
					"ADM-606");

			serviceError = new ServiceError(DeviceValidationErrorCode.DEVICE_DOES_NOT_EXIST.getErrorCode(),
					DeviceValidationErrorCode.DEVICE_DOES_NOT_EXIST.getErrorMessage());
			serviceErrors.add(serviceError);
			throw new AuthDeviceServicesException(serviceErrors);

		}
		if (!registeredDevice.getStatusCode().equalsIgnoreCase(REGISTERED)) {
			ServiceError serviceError = new ServiceError();
			auditUtil.auditRequest(
					AuthDeviceConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.DEVICE_VALIDATION_API_CALLED,
							DeviceValidationErrorCode.DEVICE_REVOKED_OR_RETIRED.getErrorCode(),
							DeviceValidationErrorCode.DEVICE_REVOKED_OR_RETIRED.getErrorMessage()),
					"ADM-607");
			serviceError = new ServiceError(DeviceValidationErrorCode.DEVICE_REVOKED_OR_RETIRED.getErrorCode(),
					DeviceValidationErrorCode.DEVICE_REVOKED_OR_RETIRED.getErrorMessage());
			serviceErrors.add(serviceError);
			throw new AuthDeviceServicesException(serviceErrors);

		}

		return registeredDevice;
	}
}
