package io.mosip.pms.device.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.pms.common.constant.Purpose;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ValidateResponseWrapper;
import io.mosip.pms.device.authdevice.entity.RegisteredDevice;
import io.mosip.pms.device.authdevice.service.RegisteredDeviceService;
import io.mosip.pms.device.regdevice.entity.RegRegisteredDevice;
import io.mosip.pms.device.regdevice.service.RegRegisteredDeviceService;
import io.mosip.pms.device.request.dto.DeRegisterDevicePostDto;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.RegisteredDevicePostDto;
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
	//@PreAuthorize("hasAnyRole('ZONAL_ADMIN','DEVICE_PROVIDER','FTM_PROVIDER')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostregistereddevices())")
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
	//@PreAuthorize("hasAnyRole('ZONAL_ADMIN','DEVICE_PROVIDER','FTM_PROVIDER')")
	@ApiOperation(value = "DeRegister Device")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostregistereddevicesderegister())")
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
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostregistereddevicessearch())")
	@PostMapping("/search")
	//@PreAuthorize("hasAnyRole('PARTNER','PMS_USER','AUTH_PARTNER','DEVICE_PROVIDER','FTM_PROVIDER','CREDENTIAL_PARTNER','CREDENTIAL_ISSUANCE','CREATE_SHARE','ID_AUTHENTICATION')")
	public ValidateResponseWrapper<PageResponseDto<RegisteredDevice>> searchRegisteredDevice(
			@RequestBody @Valid RequestWrapper<DeviceSearchDto> request) {
		ValidateResponseWrapper<PageResponseDto<RegisteredDevice>> responseWrapper = new ValidateResponseWrapper<>();
		if(request.getRequest().getPurpose().toString().equalsIgnoreCase(Purpose.REGISTRATION.toString())) {
			responseWrapper.setResponse(regRegisteredDeviceService.searchRegisteredDevice(RegRegisteredDevice.class, request.getRequest()));
			return responseWrapper;
		} 
		responseWrapper.setResponse(registeredDeviceService.searchRegisteredDevice(RegisteredDevice.class, request.getRequest()));
		return responseWrapper;
	}
}

