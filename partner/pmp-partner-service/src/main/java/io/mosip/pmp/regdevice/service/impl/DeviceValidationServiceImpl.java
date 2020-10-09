package io.mosip.pmp.regdevice.service.impl;

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
import io.mosip.pmp.authdevice.exception.AuthDeviceServiceException;
import io.mosip.pmp.authdevice.exception.RequestException;
import io.mosip.pmp.authdevice.exception.ValidationException;
import io.mosip.pmp.authdevice.util.AuditUtil;
import io.mosip.pmp.authdevice.util.AuthDeviceConstant;
import io.mosip.pmp.partner.entity.Partner;
import io.mosip.pmp.partner.repository.PartnerServiceRepository;
import io.mosip.pmp.regdevice.entity.RegDeviceDetail;
import io.mosip.pmp.regdevice.entity.RegRegisteredDevice;
import io.mosip.pmp.regdevice.entity.RegSecureBiometricInterface;
import io.mosip.pmp.regdevice.repository.RegDeviceDetailRepository;
import io.mosip.pmp.regdevice.repository.RegRegisteredDeviceRepository;
import io.mosip.pmp.regdevice.repository.RegSecureBiometricInterfaceRepository;
import io.mosip.pmp.regdevice.service.DeviceValidationService;

@Component
@Transactional
public class DeviceValidationServiceImpl implements DeviceValidationService {
	
	private static final String REGISTERED = "Registered";
	
	@Autowired
	AuditUtil auditUtil;
	
	@Autowired
	private RegRegisteredDeviceRepository registeredDeviceRepository;
	
	@Autowired
	private PartnerServiceRepository deviceProviderRepository;

	@Autowired
	private RegSecureBiometricInterfaceRepository deviceServiceRepository;	
	
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
			responseDto.setStatus("Valid");
			responseDto.setMessage("Device  details validated successfully");
		} else {
			responseDto = validateDeviceProviderHistory(validateDeviceDto);
		}
		return responseDto;
	}

	private ResponseDto validateDeviceProviderHistory(ValidateDeviceDto validateDeviceDto) {
		
		return null;
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
		try {
			deviceServices = deviceServiceRepository.findBySwVersionAndIsActiveIsTrue(deviceServiceVersion);
		} catch (DataAccessException | DataAccessLayerException e) {
			auditUtil.auditRequest(
					AuthDeviceConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC,
							DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorCode(),
							DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorMessage()),
					"ADM-608");
			throw new AuthDeviceServiceException(DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorCode(),
					String.format(DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorMessage(),
							AuthDeviceConstant.ERROR_OCCURED_MOSIP_DEVICE_SERVICE));
		}
		if (deviceServices.isEmpty()) {
			auditUtil.auditRequest(
					AuthDeviceConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC,
							DeviceValidationErrorCode.MDS_DOES_NOT_EXIST.getErrorCode(),
							DeviceValidationErrorCode.MDS_DOES_NOT_EXIST.getErrorMessage()),
					"ADM-609");
			throw new AuthDeviceServiceException(DeviceValidationErrorCode.MDS_DOES_NOT_EXIST.getErrorCode(),
					DeviceValidationErrorCode.MDS_DOES_NOT_EXIST.getErrorMessage());
		}

		return true;
	}

	private boolean isDeviceProviderPresent(String dpId) {
		Partner deviceProvider = null;
		
		try {
			deviceProvider = deviceProviderRepository.findByIdAndIsActiveIsTrue(dpId);
		} catch (DataAccessException | DataAccessLayerException e) {
			auditUtil.auditRequest(
					AuthDeviceConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC,
							DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorCode(),
							DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorMessage()),
					"ADM-606");
			throw new AuthDeviceServiceException(DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorCode(),
					String.format(DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorMessage(),
							AuthDeviceConstant.ERROR_OCCURED_DEVICE_PROVIDER));
		}
		if (deviceProvider == null) {
			auditUtil.auditRequest(
					AuthDeviceConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.FAILURE_DESC,
							DeviceValidationErrorCode.DEVICE_PROVIDER_INACTIVE.getErrorCode(),
							DeviceValidationErrorCode.DEVICE_PROVIDER_INACTIVE.getErrorMessage()),
					"ADM-607");
			throw new AuthDeviceServiceException(DeviceValidationErrorCode.DEVICE_PROVIDER_INACTIVE.getErrorCode(),
					DeviceValidationErrorCode.DEVICE_PROVIDER_INACTIVE.getErrorMessage());
		}
		return true;
	}

	private RegRegisteredDevice findRegisteredDevice(String deviceCode, String purpose) {
		RegRegisteredDevice registeredDevice = null;
		try {
			if (StringUtils.isBlank(purpose)) {
				registeredDevice = registeredDeviceRepository.findByCodeAndIsActiveIsTrue(deviceCode);
			} else {
				registeredDevice = registeredDeviceRepository.findByCodeAndPurposeIgnoreCaseAndIsActiveIsTrue(deviceCode,
						purpose);
			}
		}catch (DataAccessException | DataAccessLayerException e) {
			auditUtil.auditRequest(
					AuthDeviceConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.DEVICE_VALIDATION_API_CALLED,
							DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorCode(),
							DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorMessage()),
					"ADM-605");
			throw new AuthDeviceServiceException(DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorCode(),
					String.format(DeviceValidationErrorCode.DATABASE_EXCEPTION.getErrorMessage(),
							AuthDeviceConstant.ERROR_OCCURED_REGISTERED_DEVICE));
		}
		if (registeredDevice == null) {
			auditUtil.auditRequest(
					AuthDeviceConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.DEVICE_VALIDATION_API_CALLED,
							DeviceValidationErrorCode.DEVICE_DOES_NOT_EXIST.getErrorCode(),
							DeviceValidationErrorCode.DEVICE_DOES_NOT_EXIST.getErrorMessage()),
					"ADM-606");

			throw new AuthDeviceServiceException(DeviceValidationErrorCode.DEVICE_DOES_NOT_EXIST.getErrorCode(),
					DeviceValidationErrorCode.DEVICE_DOES_NOT_EXIST.getErrorMessage());
		}
		if (!registeredDevice.getStatusCode().equalsIgnoreCase(REGISTERED)) {
			auditUtil.auditRequest(
					AuthDeviceConstant.DEVICE_VALIDATION_FAILURE + ValidateDeviceDto.class.getSimpleName(),
					AuthDeviceConstant.AUDIT_SYSTEM,
					String.format(AuthDeviceConstant.DEVICE_VALIDATION_API_CALLED,
							DeviceValidationErrorCode.DEVICE_REVOKED_OR_RETIRED.getErrorCode(),
							DeviceValidationErrorCode.DEVICE_REVOKED_OR_RETIRED.getErrorMessage()),
					"ADM-607");
			throw new RequestException(DeviceValidationErrorCode.DEVICE_REVOKED_OR_RETIRED.getErrorCode(),
					DeviceValidationErrorCode.DEVICE_REVOKED_OR_RETIRED.getErrorMessage());
		}

		return registeredDevice;
	}
}
