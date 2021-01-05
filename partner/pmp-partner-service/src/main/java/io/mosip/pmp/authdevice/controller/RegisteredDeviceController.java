package io.mosip.pmp.authdevice.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.pmp.authdevice.constants.Purpose;
import io.mosip.pmp.authdevice.dto.DeRegisterDevicePostDto;
import io.mosip.pmp.authdevice.dto.PageResponseDto;
import io.mosip.pmp.authdevice.dto.RegisteredDevicePostDto;
import io.mosip.pmp.authdevice.dto.SearchDto;
import io.mosip.pmp.authdevice.entity.RegisteredDevice;
import io.mosip.pmp.authdevice.service.RegisteredDeviceService;
import io.mosip.pmp.partner.core.RequestWrapper;
import io.mosip.pmp.partner.core.ValidateResponseWrapper;
import io.mosip.pmp.regdevice.entity.RegRegisteredDevice;
import io.mosip.pmp.regdevice.service.RegRegisteredDeviceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value = "/registereddevices")
@Api(tags = { "Registered Device" })
@Validated
public class RegisteredDeviceController {

	@Autowired
	RegisteredDeviceService registeredDeviceService;
	
	@Autowired
	RegRegisteredDeviceService regRegisteredDeviceService;

	/**
	 * Api to Register Device.
	 * 
	 * Digitally signed device
	 * 
	 * @param registeredDevicePostDto
	 * @return ResponseWrapper<String>
	 * @throws Exception
	 */
	@ResponseFilter
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','DEVICE_PROVIDER','FTM_PROVIDER')")
	@PostMapping
	public ValidateResponseWrapper<String> signedRegisteredDevice(
			@Valid @RequestBody RequestWrapper<RegisteredDevicePostDto> registeredDevicePostDto) throws Exception {
		ValidateResponseWrapper<String> response = new ValidateResponseWrapper<>();
		response.setId("io.mosip.deviceregister");
		response.setResponse(registeredDeviceService.signedRegisteredDevice(registeredDevicePostDto.getRequest()));
		return response;
	}

	/**
	 * Api to de-register Device.
	 * 
	 * @param request
	 *            the request DTO.
	 * @return the {@link DeviceRegisterResponseDto}.
	 */
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','DEVICE_PROVIDER','FTM_PROVIDER')")
	@ApiOperation(value = "DeRegister Device")
	@PostMapping("/deregister")
	@ResponseFilter
	public ValidateResponseWrapper<String> deRegisterDevice(@Valid @RequestBody RequestWrapper<DeRegisterDevicePostDto>
							deRegisterDevicePostDto) {
		ValidateResponseWrapper<String> response = new ValidateResponseWrapper<>();
		if(deRegisterDevicePostDto.getRequest().getIsItForRegistrationDevice()) {
			response.setResponse(regRegisteredDeviceService.deRegisterDevice(deRegisterDevicePostDto.getRequest()));			
		}else {
			response.setResponse(registeredDeviceService.deRegisterDevice(deRegisterDevicePostDto.getRequest()));
		}
		return response;
	}
	
	@ResponseFilter
	@PostMapping("/search")
	@PreAuthorize("hasAnyRole('PARTNER','PMS_USER','AUTH_PARTNER','DEVICE_PROVIDER','FTM_PROVIDER','CREDENTIAL_PARTNER','CREDENTIAL_ISSUANCE','CREATE_SHARE','ID_AUTHENTICATION')")
	public ValidateResponseWrapper<PageResponseDto<RegisteredDevice>> searchRegisteredDevice(
			@RequestBody @Valid RequestWrapper<SearchDto> request) {
		ValidateResponseWrapper<PageResponseDto<RegisteredDevice>> responseWrapper = new ValidateResponseWrapper<>();
		if(request.getRequest().getPurpose().equals(Purpose.REGISTRATION)) {
			responseWrapper.setResponse(regRegisteredDeviceService.searchRegisteredDevice(RegRegisteredDevice.class, request.getRequest()));
			return responseWrapper;
		} 
		responseWrapper.setResponse(registeredDeviceService.searchRegisteredDevice(RegisteredDevice.class, request.getRequest()));
		return responseWrapper;
	}
}

