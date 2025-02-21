package io.mosip.pms.device.authdevice.service;

import org.springframework.stereotype.Service;

import io.mosip.pms.common.dto.FilterValueDto;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.device.request.dto.DeviceDetailSBIMappingDto;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceCreateDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceStatusUpdateDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceUpdateDto;
import io.mosip.pms.device.response.dto.FilterResponseCodeDto;
import io.mosip.pms.device.response.dto.IdDto;
import io.mosip.pms.device.response.dto.MappedDeviceDetailsReponse;
import io.mosip.pms.device.response.dto.SbiSearchResponseDto;

@Service
public interface SecureBiometricInterfaceService {
	
	public IdDto createSecureBiometricInterface(SecureBiometricInterfaceCreateDto SecureBiometricInterfaceDto);
	
	public IdDto updateSecureBiometricInterface(SecureBiometricInterfaceUpdateDto SecureBiometricInterfaceDto);
	
	public String updateSecureBiometricInterfaceStatus(SecureBiometricInterfaceStatusUpdateDto secureBiometricInterfaceDto);	

	public <E> PageResponseDto<SbiSearchResponseDto> searchSecureBiometricInterface(Class<E> entity, DeviceSearchDto dto);
	
	public String mapDeviceDetailAndSbi(DeviceDetailSBIMappingDto input);
	
	public String deleteDeviceDetailAndSbiMapping(DeviceDetailSBIMappingDto input);
	
	public <E> PageResponseDto<MappedDeviceDetailsReponse> searchMappedDeviceDetails(Class<E> entity, DeviceSearchDto dto);
	
	public FilterResponseCodeDto filterValues(FilterValueDto filterValueDto);
}
