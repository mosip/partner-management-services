package io.mosip.pmp.authdevice.service;

import org.springframework.stereotype.Service;

import io.mosip.pmp.authdevice.dto.DeRegisterDevicePostDto;
import io.mosip.pmp.authdevice.dto.RegisteredDevicePostDto;

@Service
public interface RegisteredDeviceService {
	public String deRegisterDevice( DeRegisterDevicePostDto deRegisterDevicePostDto);

	
	public String signedRegisteredDevice(RegisteredDevicePostDto registeredDevicePostDto) throws Exception;
}
