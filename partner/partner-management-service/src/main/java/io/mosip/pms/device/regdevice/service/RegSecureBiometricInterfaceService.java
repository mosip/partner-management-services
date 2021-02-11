package io.mosip.pms.device.regdevice.service;

import org.springframework.stereotype.Service;

import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceCreateDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceStatusUpdateDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceUpdateDto;
import io.mosip.pms.device.response.dto.IdDto;
import io.mosip.pms.device.response.dto.SbiSearchResponseDto;

@Service
public interface RegSecureBiometricInterfaceService {
	
	public IdDto createSecureBiometricInterface(SecureBiometricInterfaceCreateDto SecureBiometricInterfaceDto);

	public IdDto updateSecureBiometricInterface(SecureBiometricInterfaceUpdateDto SecureBiometricInterfaceDto);

	public String updateSecureBiometricInterfaceStatus(
			SecureBiometricInterfaceStatusUpdateDto secureBiometricInterfaceDto);

	public <E> PageResponseDto<SbiSearchResponseDto> searchSecureBiometricInterface(DeviceSearchDto dto);
}
