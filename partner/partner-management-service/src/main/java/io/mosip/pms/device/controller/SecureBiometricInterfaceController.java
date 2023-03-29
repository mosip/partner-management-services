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
import io.mosip.pms.common.dto.FilterValueDto;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.common.entity.DeviceDetailSBI;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.device.authdevice.entity.SecureBiometricInterface;
import io.mosip.pms.device.authdevice.service.SecureBiometricInterfaceService;
import io.mosip.pms.device.constant.DeviceConstant;
import io.mosip.pms.device.request.dto.DeviceDetailSBIMappingDto;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceCreateDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceStatusUpdateDto;
import io.mosip.pms.device.request.dto.SecureBiometricInterfaceUpdateDto;
import io.mosip.pms.device.request.dto.UpdateDeviceDetailStatusDto;
import io.mosip.pms.device.response.dto.FilterResponseCodeDto;
import io.mosip.pms.device.response.dto.IdDto;
import io.mosip.pms.device.response.dto.MappedDeviceDetailsReponse;
import io.mosip.pms.device.response.dto.SbiSearchResponseDto;
import io.mosip.pms.device.util.AuditUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping(value = "/securebiometricinterface")
@Api(tags = { "SecureBiometricInterface" })
public class SecureBiometricInterfaceController {

	@Autowired
	SecureBiometricInterfaceService secureBiometricInterface;

	@Autowired
	AuditUtil auditUtil;

	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostsecurebiometricinterface())")
	@ResponseFilter
	@PostMapping
	@Operation(summary = "Service to save SecureBiometricInterfaceCreateDto", description = "Saves SecureBiometricInterfaceCreateDto and return DeviceDetail id")
	@ApiResponses({ @ApiResponse(code = 201, message = "When SecureBiometricInterfaceCreateDto successfully created"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While creating SecureBiometricInterfaceCreateDto any error occured") })
	public ResponseWrapper<IdDto> SecureBiometricInterface(
			@Valid @RequestBody RequestWrapper<SecureBiometricInterfaceCreateDto> secureBiometricInterfaceCreateDto) {
		auditUtil.auditRequest(
				DeviceConstant.CREATE_API_IS_CALLED + SecureBiometricInterfaceCreateDto.class.getCanonicalName(),
				DeviceConstant.AUDIT_SYSTEM,
				DeviceConstant.CREATE_API_IS_CALLED + SecureBiometricInterfaceCreateDto.class.getCanonicalName(),
				"AUT-011", secureBiometricInterfaceCreateDto.getRequest().getProviderId(), "partnerId");
		ResponseWrapper<IdDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(secureBiometricInterface
				.createSecureBiometricInterface(secureBiometricInterfaceCreateDto.getRequest()));
		auditUtil.auditRequest(
				String.format(DeviceConstant.SUCCESSFUL_CREATE,
						SecureBiometricInterfaceCreateDto.class.getCanonicalName()),
				DeviceConstant.AUDIT_SYSTEM, String.format(DeviceConstant.SUCCESSFUL_CREATE,
						SecureBiometricInterfaceCreateDto.class.getCanonicalName()),
				"AUT-012", secureBiometricInterfaceCreateDto.getRequest().getProviderId(), "partnerId");
		return responseWrapper;

	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getPutsecurebiometricinterface())")
	@ResponseFilter
	@PutMapping
	@Operation(summary = "Service to update SecureBiometricInterface", description = "Updates SecureBiometricInterface and returns success message")
	@ApiResponses({ @ApiResponse(code = 201, message = "When SecureBiometricInterface successfully updated"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While updating SecureBiometricInterface any error occured") })
	public ResponseWrapper<IdDto> updateSecureBiometricInterface(
			@Valid @RequestBody RequestWrapper<SecureBiometricInterfaceUpdateDto> secureBiometricInterfaceUpdateDto) {
		auditUtil.auditRequest(
				DeviceConstant.UPDATE_API_IS_CALLED + SecureBiometricInterfaceUpdateDto.class.getCanonicalName(),
				DeviceConstant.AUDIT_SYSTEM,
				DeviceConstant.UPDATE_API_IS_CALLED + SecureBiometricInterfaceUpdateDto.class.getCanonicalName(),
				"AUT-013", secureBiometricInterfaceUpdateDto.getRequest().getId(), "sbiId");
		ResponseWrapper<IdDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(secureBiometricInterface
				.updateSecureBiometricInterface(secureBiometricInterfaceUpdateDto.getRequest()));
		auditUtil.auditRequest(
				String.format(DeviceConstant.SUCCESSFUL_UPDATE,
						SecureBiometricInterfaceUpdateDto.class.getCanonicalName()),
				DeviceConstant.AUDIT_SYSTEM, String.format(DeviceConstant.SUCCESSFUL_UPDATE,
						SecureBiometricInterfaceUpdateDto.class.getCanonicalName()),
				"AUT-012", secureBiometricInterfaceUpdateDto.getRequest().getId(), "sbiId");
		return responseWrapper;
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getPatchsecurebiometricinterface())")
	@ResponseFilter
	@PatchMapping
	@Operation(summary = "Service to approve/reject SecureBiometricInterface", description = "Approve SecureBiometricInterface and returns success message")
	@ApiResponses({ @ApiResponse(code = 201, message = "When SecureBiometricInterface successfully approved/rejected"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While approving/rejecting DeviceDetail any error occured") })
	public ResponseWrapper<String> approveSecureBiometricInterface(
			@Valid @RequestBody RequestWrapper<SecureBiometricInterfaceStatusUpdateDto> secureBiometricInterfaceStatusUpdateDto) {
		auditUtil.auditRequest(
				DeviceConstant.STATUS_UPDATE_API_IS_CALLED + UpdateDeviceDetailStatusDto.class.getCanonicalName(),
				DeviceConstant.AUDIT_SYSTEM,
				DeviceConstant.STATUS_UPDATE_API_IS_CALLED + UpdateDeviceDetailStatusDto.class.getCanonicalName(),
				"AUT-006", secureBiometricInterfaceStatusUpdateDto.getRequest().getId(), "sbiId");
		ResponseWrapper<String> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(secureBiometricInterface
				.updateSecureBiometricInterfaceStatus(secureBiometricInterfaceStatusUpdateDto.getRequest()));
		auditUtil.auditRequest(
				String.format(DeviceConstant.SUCCESSFUL_UPDATE, UpdateDeviceDetailStatusDto.class.getCanonicalName()),
				DeviceConstant.AUDIT_SYSTEM,
				String.format(DeviceConstant.SUCCESSFUL_UPDATE, UpdateDeviceDetailStatusDto.class.getCanonicalName()),
				"AUT-007", secureBiometricInterfaceStatusUpdateDto.getRequest().getId(), "sbiId");

		return responseWrapper;
	}

	@ResponseFilter
	@PostMapping("/search")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostsecurebiometricinterfacesearch())")
	@Operation(summary = "Service to search SecureBiometricInterface details", description = "Service to search SecureBiometricInterface details")
	public ResponseWrapper<PageResponseDto<SbiSearchResponseDto>> searchSecureBiometric(
			@RequestBody @Valid RequestWrapper<DeviceSearchDto> request) {
		ResponseWrapper<PageResponseDto<SbiSearchResponseDto>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(secureBiometricInterface
				.searchSecureBiometricInterface(SecureBiometricInterface.class, request.getRequest()));
		return responseWrapper;
	}

	@ResponseFilter
	@PutMapping("/devicedetails/map")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPutsecurebiometricinterfacedevicedetailsmap())")
	@Operation(summary = "Service to map device details with sbi", description = "Service to map device details with sbi")
	public ResponseWrapper<String> mapDeviceDetails(
			@RequestBody @Valid RequestWrapper<DeviceDetailSBIMappingDto> request) {
		ResponseWrapper<String> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(secureBiometricInterface.mapDeviceDetailAndSbi(request.getRequest()));
		return responseWrapper;
	}

	@ResponseFilter
	@PutMapping("/devicedetails/map/remove")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPutsecurebiometricinterfacedevicedetailsmapremove())")
	@Operation(summary = "Service to remove mapped device details with sbi", description = "Service to remove mapped device details with sbi")
	public ResponseWrapper<String> removeMappedDeviceDetails(
			@RequestBody @Valid RequestWrapper<DeviceDetailSBIMappingDto> request) {
		ResponseWrapper<String> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(secureBiometricInterface.deleteDeviceDetailAndSbiMapping(request.getRequest()));
		return responseWrapper;
	}

	@ResponseFilter
	@PostMapping("/devicedetails/map/search")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostsecurebiometricinterfacedevicedetailsmapsearch())")
	@Operation(summary = "Service to search mapped device details and SecureBiometricInterface details", description = "Service to search mapped device details and SecureBiometricInterface details")
	public ResponseWrapper<PageResponseDto<MappedDeviceDetailsReponse>> searchMappedDeviceDetails(
			@RequestBody @Valid RequestWrapper<DeviceSearchDto> request) {
		ResponseWrapper<PageResponseDto<MappedDeviceDetailsReponse>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(
				secureBiometricInterface.searchMappedDeviceDetails(DeviceDetailSBI.class, request.getRequest()));
		return responseWrapper;
	}
	
	@ResponseFilter
	@PostMapping("/filtervalues")
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostsecurebiometricinterfacefiltervalues())")
	@Operation(summary = "Service to filter SBI's", description = "Service to filter SBI's")
	public ResponseWrapper<FilterResponseCodeDto> filterValues(
			@RequestBody @Valid RequestWrapper<FilterValueDto> request) {
		ResponseWrapper<FilterResponseCodeDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(secureBiometricInterface.filterValues(request.getRequest()));
		return responseWrapper;
	}
}
