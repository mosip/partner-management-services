package io.mosip.pms.device.regdevice.service;

import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.device.authdevice.entity.RegisteredDevice;
import io.mosip.pms.device.request.dto.DeRegisterDevicePostDto;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.RegisteredDevicePostDto;

public interface RegRegisteredDeviceService {
	
	public String deRegisterDevice(DeRegisterDevicePostDto deRegisterDevicePostDto);
	
	public String signedRegisteredDevice(RegisteredDevicePostDto registeredDevicePostDto) throws Exception;

	public <E> PageResponseDto<RegisteredDevice> searchRegisteredDevice(Class<E> entity, DeviceSearchDto dto);
}
