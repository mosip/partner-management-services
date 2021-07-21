package io.mosip.pms.device.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ValidateResponseWrapper;
import io.mosip.pms.device.constant.DeviceConstant;
import io.mosip.pms.device.regdevice.service.DeviceValidationService;
import io.mosip.pms.device.request.dto.ValidateDeviceDto;
import io.mosip.pms.device.response.dto.ResponseDto;
import io.mosip.pms.device.util.AuditUtil;
import io.swagger.annotations.Api;

@RestController
@RequestMapping(value = "/deviceprovidermanagement")
@Api(tags = { "Device Validation" })
public class DeviceValidationController {

	@Autowired
	private AuditUtil auditUtil;
	
	@Autowired
	private DeviceValidationService deviceValidationService;
	
	//@PreAuthorize("hasAnyRole('ZONAL_ADMIN','ID_AUTHENTICATION','REGISTRATION_PROCESSOR','RESIDENT')")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostdeviceprovidermanagementvalidate())")
	@PostMapping("/validate")
	@ResponseFilter
	public ValidateResponseWrapper<ResponseDto> validateDeviceProvider(
			@RequestBody @Valid RequestWrapper<ValidateDeviceDto> request) {
		ValidateResponseWrapper<ResponseDto> responseWrapper = new ValidateResponseWrapper<>();
		auditUtil.auditRequest(
				DeviceConstant.DEVICE_VALIDATION_API_CALLED + ValidateDeviceDto.class.getSimpleName(),
				DeviceConstant.AUDIT_SYSTEM,
				DeviceConstant.DEVICE_VALIDATION_API_CALLED + ValidateDeviceDto.class.getSimpleName(), "ADM-600");
		responseWrapper.setResponse(deviceValidationService.validateDeviceProviders(request.getRequest()));
		auditUtil.auditRequest(
				String.format(DeviceConstant.DEVICE_VALIDATION_SUCCESS, ValidateDeviceDto.class.getSimpleName()),
				DeviceConstant.AUDIT_SYSTEM,
				String.format(DeviceConstant.DEVICE_VALIDATION_HISTORY_SUCCESS_DESC,
						ValidateDeviceDto.class.getSimpleName()),
				"ADM-601");
		return responseWrapper;
	}
}
