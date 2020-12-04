package io.mosip.pmp.authdevice.service;

import org.springframework.stereotype.Service;

import io.mosip.pmp.authdevice.dto.DeRegisterDevicePostDto;
import io.mosip.pmp.authdevice.dto.PageResponseDto;
import io.mosip.pmp.authdevice.dto.RegisteredDevicePostDto;
import io.mosip.pmp.authdevice.dto.SearchDto;
import io.mosip.pmp.authdevice.entity.RegisteredDevice;

@Service
public interface RegisteredDeviceService {
	public String deRegisterDevice( DeRegisterDevicePostDto deRegisterDevicePostDto);

	
	public String signedRegisteredDevice(RegisteredDevicePostDto registeredDevicePostDto) throws Exception;


	public <E> PageResponseDto<RegisteredDevice> searchRegisteredDevice(Class<E> entity, SearchDto dto);

}
