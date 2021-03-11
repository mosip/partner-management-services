package io.mosip.pmp.authdevice.service;

import org.springframework.stereotype.Service;

import io.mosip.pmp.authdevice.dto.IdDto;
import io.mosip.pmp.authdevice.dto.SBISearchDto;
import io.mosip.pmp.authdevice.dto.SecureBiometricInterfaceCreateDto;
import io.mosip.pmp.authdevice.dto.SecureBiometricInterfaceStatusUpdateDto;
import io.mosip.pmp.authdevice.dto.SecureBiometricInterfaceUpdateDto;
import io.mosip.pmp.common.dto.PageResponseDto;

@Service
public interface SecureBiometricInterfaceService {
	public IdDto createSecureBiometricInterface(SecureBiometricInterfaceCreateDto SecureBiometricInterfaceDto);
	
	public IdDto updateSecureBiometricInterface(SecureBiometricInterfaceUpdateDto SecureBiometricInterfaceDto);
	
	public String updateSecureBiometricInterfaceStatus(SecureBiometricInterfaceStatusUpdateDto secureBiometricInterfaceDto);
	
	public <E> PageResponseDto<SecureBiometricInterfaceCreateDto> searchSecureBiometricInterface(Class<E> entity, SBISearchDto dto);
}
