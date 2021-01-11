package io.mosip.pmp.regdevice.service;

import io.mosip.pmp.authdevice.dto.DeRegisterDevicePostDto;
import io.mosip.pmp.authdevice.dto.DeviceSearchDto;
import io.mosip.pmp.authdevice.dto.RegisteredDevicePostDto;
import io.mosip.pmp.authdevice.entity.RegisteredDevice;
import io.mosip.pmp.common.dto.PageResponseDto;

public interface RegRegisteredDeviceService {
	public String deRegisterDevice( DeRegisterDevicePostDto deRegisterDevicePostDto);

	
	public String signedRegisteredDevice(RegisteredDevicePostDto registeredDevicePostDto) throws Exception;

	public <E> PageResponseDto<RegisteredDevice> searchRegisteredDevice(Class<E> entity, DeviceSearchDto dto);

}
