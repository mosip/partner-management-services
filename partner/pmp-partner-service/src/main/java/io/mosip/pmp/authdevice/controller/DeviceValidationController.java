package io.mosip.pmp.authdevice.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.pmp.authdevice.dto.ResponseDto;
import io.mosip.pmp.authdevice.dto.ValidateDeviceDto;
import io.mosip.pmp.authdevice.util.AuditUtil;
import io.mosip.pmp.authdevice.util.AuthDeviceConstant;
import io.mosip.pmp.partner.core.RequestWrapper;
import io.mosip.pmp.partner.core.ResponseWrapper;
import io.mosip.pmp.regdevice.service.DeviceValidationService;
import io.swagger.annotations.Api;

@RestController
@RequestMapping(value = "/deviceprovidermanagement")
@Api(tags = { "Device Validation" })
public class DeviceValidationController {

	@Autowired
	private AuditUtil auditUtil;
	
	@Autowired
	private DeviceValidationService deviceValidationService;
	
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','ID_AUTHENTICATION','REGISTRATION_PROCESSOR','RESIDENT')")
	@PostMapping("/validate")
	@ResponseFilter
	public ResponseWrapper<ResponseDto> validateDeviceProvider(
			@RequestBody @Valid RequestWrapper<ValidateDeviceDto> request) {
		ResponseWrapper<ResponseDto> responseWrapper = new ResponseWrapper<>();
		auditUtil.auditRequest(
				AuthDeviceConstant.DEVICE_VALIDATION_API_CALLED + ValidateDeviceDto.class.getSimpleName(),
				AuthDeviceConstant.AUDIT_SYSTEM,
				AuthDeviceConstant.DEVICE_VALIDATION_API_CALLED + ValidateDeviceDto.class.getSimpleName(), "ADM-600");
		responseWrapper.setResponse(deviceValidationService.validateDeviceProviders(request.getRequest()));
		auditUtil.auditRequest(
				String.format(AuthDeviceConstant.DEVICE_VALIDATION_SUCCESS, ValidateDeviceDto.class.getSimpleName()),
				AuthDeviceConstant.AUDIT_SYSTEM,
				String.format(AuthDeviceConstant.DEVICE_VALIDATION_HISTORY_SUCCESS_DESC,
						ValidateDeviceDto.class.getSimpleName()),
				"ADM-601");
		return responseWrapper;
	}
}
