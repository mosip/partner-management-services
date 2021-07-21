package io.mosip.pms.device.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.pms.common.constant.Purpose;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.device.authdevice.entity.SecureBiometricInterface;
import io.mosip.pms.device.authdevice.service.SecureBiometricInterfaceService;
import io.mosip.pms.device.constant.DeviceConstant;
import io.mosip.pms.device.regdevice.service.RegSecureBiometricInterfaceService;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceCreateDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceStatusUpdateDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceUpdateDto;
import io.mosip.pms.device.request.dto.UpdateDeviceDetailStatusDto;
import io.mosip.pms.device.response.dto.IdDto;
import io.mosip.pms.device.response.dto.SbiSearchResponseDto;
import io.mosip.pms.device.util.AuditUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(value = "/securebiometricinterface")
@Api(tags = { "SecureBiometricInterface" })
public class SecureBiometricInterfaceController {
	
	@Autowired
	SecureBiometricInterfaceService secureBiometricInterface;
	
	@Autowired
	RegSecureBiometricInterfaceService regSecureBiometricInterface;
	
	@Autowired
	AuditUtil auditUtil;
	//@PreAuthorize("hasAnyRole('DEVICE_PROVIDER','PARTNER_ADMIN','FTM_PROVIDER')")
	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostsecurebiometricinterface())")
	@PostMapping
	@ApiOperation(value = "Service to save SecureBiometricInterfaceCreateDto", notes = "Saves SecureBiometricInterfaceCreateDto and return DeviceDetail id")
	@ApiResponses({ @ApiResponse(code = 201, message = "When SecureBiometricInterfaceCreateDto successfully created"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While creating SecureBiometricInterfaceCreateDto any error occured") })
	public ResponseWrapper<IdDto> SecureBiometricInterface(
			@Valid @RequestBody RequestWrapper<SecureBiometricInterfaceCreateDto> secureBiometricInterfaceCreateDto) {
		auditUtil.auditRequest(
				DeviceConstant.CREATE_API_IS_CALLED + SecureBiometricInterfaceCreateDto.class.getCanonicalName(),
				DeviceConstant.AUDIT_SYSTEM,
				DeviceConstant.CREATE_API_IS_CALLED + SecureBiometricInterfaceCreateDto.class.getCanonicalName(),
				"AUT-011");
		ResponseWrapper<IdDto> responseWrapper = new ResponseWrapper<>();
		if(secureBiometricInterfaceCreateDto.getRequest().getIsItForRegistrationDevice()) {
			responseWrapper
			.setResponse(regSecureBiometricInterface.createSecureBiometricInterface(secureBiometricInterfaceCreateDto.getRequest()));
			
		}else {
			responseWrapper
			.setResponse(secureBiometricInterface.createSecureBiometricInterface(secureBiometricInterfaceCreateDto.getRequest()));			
		}
		auditUtil.auditRequest(
				String.format(DeviceConstant.SUCCESSFUL_CREATE , SecureBiometricInterfaceCreateDto.class.getCanonicalName()),
				DeviceConstant.AUDIT_SYSTEM,
				String.format(DeviceConstant.SUCCESSFUL_CREATE , SecureBiometricInterfaceCreateDto.class.getCanonicalName()),
				"AUT-012");
		return responseWrapper;

	}
	
	//@PreAuthorize("hasAnyRole('DEVICE_PROVIDER','PARTNER_ADMIN','FTM_PROVIDER')")
	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPutsecurebiometricinterface())")
	@PutMapping
	@ApiOperation(value = "Service to update SecureBiometricInterface", notes = "Updates SecureBiometricInterface and returns success message")
	@ApiResponses({ @ApiResponse(code = 201, message = "When SecureBiometricInterface successfully updated"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While updating SecureBiometricInterface any error occured") })
	public ResponseWrapper<IdDto> updateSecureBiometricInterface(
			@Valid @RequestBody RequestWrapper<SecureBiometricInterfaceUpdateDto> secureBiometricInterfaceUpdateDto) {
		auditUtil.auditRequest(
				DeviceConstant.UPDATE_API_IS_CALLED + SecureBiometricInterfaceUpdateDto.class.getCanonicalName(),
				DeviceConstant.AUDIT_SYSTEM,
				DeviceConstant.UPDATE_API_IS_CALLED + SecureBiometricInterfaceUpdateDto.class.getCanonicalName(),
				"AUT-013");
		ResponseWrapper<IdDto> responseWrapper = new ResponseWrapper<>();
		if(secureBiometricInterfaceUpdateDto.getRequest().getIsItForRegistrationDevice()) {
			responseWrapper
			.setResponse(regSecureBiometricInterface.updateSecureBiometricInterface(secureBiometricInterfaceUpdateDto.getRequest()));
			
		}else {
			responseWrapper
			.setResponse(secureBiometricInterface.updateSecureBiometricInterface(secureBiometricInterfaceUpdateDto.getRequest()));
			
		}
		auditUtil.auditRequest(
				String.format(DeviceConstant.SUCCESSFUL_UPDATE , SecureBiometricInterfaceUpdateDto.class.getCanonicalName()),
				DeviceConstant.AUDIT_SYSTEM,
				String.format(DeviceConstant.SUCCESSFUL_UPDATE , SecureBiometricInterfaceUpdateDto.class.getCanonicalName()),
				"AUT-012");
		return responseWrapper;
	}
	
	//@PreAuthorize("hasAnyRole('PARTNER_ADMIN')")
	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPatchsecurebiometricinterface())")
	@PatchMapping
	@ApiOperation(value = "Service to approve/reject SecureBiometricInterface", notes = "Approve SecureBiometricInterface and returns success message")
	@ApiResponses({ @ApiResponse(code = 201, message = "When SecureBiometricInterface successfully approved/rejected"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While approving/rejecting DeviceDetail any error occured") })
	public ResponseWrapper<String> approveSecureBiometricInterface(
			@Valid @RequestBody RequestWrapper<SecureBiometricInterfaceStatusUpdateDto> secureBiometricInterfaceStatusUpdateDto){
		auditUtil.auditRequest(
				DeviceConstant.STATUS_UPDATE_API_IS_CALLED + UpdateDeviceDetailStatusDto.class.getCanonicalName(),
				DeviceConstant.AUDIT_SYSTEM,
				DeviceConstant.STATUS_UPDATE_API_IS_CALLED + UpdateDeviceDetailStatusDto.class.getCanonicalName(),
				"AUT-006");
		ResponseWrapper<String> responseWrapper = new ResponseWrapper<>();
		if(secureBiometricInterfaceStatusUpdateDto.getRequest().getIsItForRegistrationDevice()) {
			responseWrapper
			.setResponse(regSecureBiometricInterface.updateSecureBiometricInterfaceStatus(secureBiometricInterfaceStatusUpdateDto.getRequest()));
			
		}else {
			responseWrapper
			.setResponse(secureBiometricInterface.updateSecureBiometricInterfaceStatus(secureBiometricInterfaceStatusUpdateDto.getRequest()));
		}
		auditUtil.auditRequest(
				String.format(DeviceConstant.SUCCESSFUL_UPDATE , UpdateDeviceDetailStatusDto.class.getCanonicalName()),
				DeviceConstant.AUDIT_SYSTEM,
				String.format(DeviceConstant.SUCCESSFUL_UPDATE , UpdateDeviceDetailStatusDto.class.getCanonicalName()),
				"AUT-007");

		return responseWrapper;
	}
	
	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostsecurebiometricinterfacesearch())")
	@PostMapping("/search")
	//@PreAuthorize("hasAnyRole('DEVICE_PROVIDER','PARTNER_ADMIN','FTM_PROVIDER')")
	public ResponseWrapper<PageResponseDto<SbiSearchResponseDto>> searchSecureBiometric(
			@RequestBody @Valid RequestWrapper<DeviceSearchDto> request) {
		ResponseWrapper<PageResponseDto<SbiSearchResponseDto>> responseWrapper = new ResponseWrapper<>();
		if(request.getRequest().getPurpose().toString().equalsIgnoreCase(Purpose.REGISTRATION.toString())) {
			responseWrapper.setResponse(regSecureBiometricInterface.searchSecureBiometricInterface(request.getRequest()));
			return responseWrapper;
		} 
		responseWrapper.setResponse(secureBiometricInterface.searchSecureBiometricInterface(SecureBiometricInterface.class, request.getRequest()));
		return responseWrapper;
	}
}
