package io.mosip.pmp.regdevice.service;

import io.mosip.pmp.authdevice.dto.DeRegisterDevicePostDto;
import io.mosip.pmp.authdevice.dto.RegisteredDevicePostDto;

public interface RegRegisteredDeviceService {
	public String deRegisterDevice( DeRegisterDevicePostDto deRegisterDevicePostDto);

	
	public String signedRegisteredDevice(RegisteredDevicePostDto registeredDevicePostDto) throws Exception;
}
