package io.mosip.pms.device.authdevice.service;

import io.mosip.pms.common.dto.PageResponseV2Dto;
import io.mosip.pms.common.response.dto.ResponseWrapperV2;
import io.mosip.pms.partner.dto.DeviceDto;
import io.mosip.pms.device.response.dto.SbiDetailsResponseDto;
import io.mosip.pms.device.response.dto.SbiSummaryDto;
import io.mosip.pms.device.dto.SbiFilterDto;

import io.mosip.pms.device.dto.SbiDetailsDto;
import org.springframework.stereotype.Service;

import io.mosip.pms.common.dto.FilterValueDto;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.device.request.dto.DeviceDetailSBIMappingDto;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.DeactivateSbiRequestDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceCreateDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceStatusUpdateDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceUpdateDto;
import io.mosip.pms.device.request.dto.DeviceDetailDto;
import io.mosip.pms.device.response.dto.FilterResponseCodeDto;
import io.mosip.pms.device.response.dto.IdDto;
import io.mosip.pms.device.response.dto.MappedDeviceDetailsReponse;
import io.mosip.pms.device.response.dto.SbiSearchResponseDto;

import java.util.List;

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

	public ResponseWrapperV2<IdDto> addDeviceToSbi(DeviceDetailDto deviceDetailDto, String sbiId);

	public ResponseWrapperV2<List<DeviceDto>> getAllDevicesForSbi(String sbiId);

	public ResponseWrapperV2<SbiDetailsResponseDto> deactivateSbi(String id, DeactivateSbiRequestDto deactivateSbiRequestDto);

	public ResponseWrapperV2<PageResponseV2Dto<SbiSummaryDto>> getAllSbiDetails(String sortFieldName, String sortType, Integer pageNo, Integer pageSize, SbiFilterDto filterDto);
}
