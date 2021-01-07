package io.mosip.pmp.regdevice.service;

import org.springframework.stereotype.Service;

import io.mosip.pmp.authdevice.dto.IdDto;
import io.mosip.pmp.authdevice.dto.PageResponseDto;
import io.mosip.pmp.authdevice.dto.DeviceSearchDto;
import io.mosip.pmp.authdevice.dto.SecureBiometricInterfaceCreateDto;
import io.mosip.pmp.authdevice.dto.SecureBiometricInterfaceStatusUpdateDto;
import io.mosip.pmp.authdevice.dto.SecureBiometricInterfaceUpdateDto;

@Service
public interface RegSecureBiometricInterfaceService {
	public IdDto createSecureBiometricInterface(SecureBiometricInterfaceCreateDto SecureBiometricInterfaceDto);

	public IdDto updateSecureBiometricInterface(SecureBiometricInterfaceUpdateDto SecureBiometricInterfaceDto);

	public String updateSecureBiometricInterfaceStatus(
			SecureBiometricInterfaceStatusUpdateDto secureBiometricInterfaceDto);

	public <E> PageResponseDto<SecureBiometricInterfaceCreateDto> searchSecureBiometricInterface(Class<E> entity, DeviceSearchDto dto);
}
