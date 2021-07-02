package io.mosip.pms.device.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.pms.common.constant.Purpose;
import io.mosip.pms.common.dto.DeviceFilterValueDto;
import io.mosip.pms.common.dto.PageResponseDto;
import io.mosip.pms.common.request.dto.RequestWrapper;
import io.mosip.pms.common.response.dto.ResponseWrapper;
import io.mosip.pms.device.authdevice.entity.DeviceDetail;
import io.mosip.pms.device.authdevice.entity.RegistrationDeviceSubType;
import io.mosip.pms.device.authdevice.service.DeviceDetailService;
import io.mosip.pms.device.constant.DeviceConstant;
import io.mosip.pms.device.regdevice.entity.RegDeviceDetail;
import io.mosip.pms.device.regdevice.entity.RegRegistrationDeviceSubType;
import io.mosip.pms.device.regdevice.service.RegDeviceDetailService;
import io.mosip.pms.device.request.dto.DeviceDetailDto;
import io.mosip.pms.device.request.dto.DeviceDetailUpdateDto;
import io.mosip.pms.device.request.dto.DeviceSearchDto;
import io.mosip.pms.device.request.dto.UpdateDeviceDetailStatusDto;
import io.mosip.pms.device.response.dto.DeviceDetailSearchResponseDto;
import io.mosip.pms.device.response.dto.FilterResponseCodeDto;
import io.mosip.pms.device.response.dto.IdDto;
import io.mosip.pms.device.response.dto.RegistrationSubTypeDto;
import io.mosip.pms.device.util.AuditUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(value = "/devicedetail")
@Api(tags = { "DeviceDetail" })
public class DeviceDetailController {
	
	@Autowired
	AuditUtil auditUtil;
	
	@Autowired	
	DeviceDetailService deviceDetaillService;
	
	@Autowired	
	RegDeviceDetailService regDeviceDetaillService;
	
	/**
	 * Post API to insert a new row of DeviceDetail data
	 * 
	 * @param deviceDetailRequestDto input parameter deviceRequestDto
	 * 
	 * @return ResponseEntity DeviceDetail which is inserted successfully
	 *         {@link ResponseEntity}
	 */
	//@PreAuthorize("hasAnyRole('PARTNER_ADMIN','DEVICE_PROVIDER','FTM_PROVIDER')")
	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostdevicedetail())")
	@PostMapping
	@ApiOperation(value = "Service to save DeviceDetail", notes = "Saves DeviceDetail and return DeviceDetail id")
	@ApiResponses({ @ApiResponse(code = 201, message = "When DeviceDetail successfully created"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While creating DeviceDetail any error occured") })
	public ResponseWrapper<IdDto> createDeviceDetail(
			@Valid @RequestBody RequestWrapper<DeviceDetailDto> deviceDetailRequestDto) {
		auditUtil.auditRequest(
				DeviceConstant.CREATE_API_IS_CALLED + DeviceDetailDto.class.getCanonicalName(),
				DeviceConstant.AUDIT_SYSTEM,
				DeviceConstant.CREATE_API_IS_CALLED + DeviceDetailDto.class.getCanonicalName(),
				"AUT-001");
		ResponseWrapper<IdDto> responseWrapper = new ResponseWrapper<>();
		if(deviceDetailRequestDto.getRequest().getIsItForRegistrationDevice()) {
			responseWrapper
			.setResponse(regDeviceDetaillService.createDeviceDetails(deviceDetailRequestDto.getRequest()));
			
		}else {
		responseWrapper
				.setResponse(deviceDetaillService.createDeviceDetails(deviceDetailRequestDto.getRequest()));
		}
		auditUtil.auditRequest(
				String.format(DeviceConstant.SUCCESSFUL_CREATE , DeviceDetailDto.class.getCanonicalName()),
				DeviceConstant.AUDIT_SYSTEM,
				String.format(DeviceConstant.SUCCESSFUL_CREATE , DeviceDetailDto.class.getCanonicalName()),
				"AUT-005");
		return responseWrapper;

	}

	/**
	 * Put API to update a row of DeviceDetail data
	 * 
	 * @param deviceDetailRequestDto input parameter deviceRequestDto
	 * 
	 * @return ResponseEntity DeviceDetail which is updated successfully
	 *         {@link ResponseEntity}
	 */
	//@PreAuthorize("hasAnyRole('PARTNER_ADMIN','DEVICE_PROVIDER','FTM_PROVIDER')")
	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPutdevicedetail())")
	@PutMapping
	@ApiOperation(value = "Service to update DeviceDetail", notes = "Updates DeviceDetail and returns success message")
	@ApiResponses({ @ApiResponse(code = 201, message = "When DeviceDetail successfully updated"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While updating DeviceDetail any error occured") })
	public ResponseWrapper<IdDto> updateDeviceDetail(
			@Valid @RequestBody RequestWrapper<DeviceDetailUpdateDto> deviceDetailRequestDto) {
		auditUtil.auditRequest(
				DeviceConstant.UPDATE_API_IS_CALLED + DeviceDetailDto.class.getCanonicalName(),
				DeviceConstant.AUDIT_SYSTEM,
				DeviceConstant.UPDATE_API_IS_CALLED + DeviceDetailDto.class.getCanonicalName(),
				"AUT-006");
		ResponseWrapper<IdDto> responseWrapper = new ResponseWrapper<>();
		if(deviceDetailRequestDto.getRequest().getIsItForRegistrationDevice()) {
			responseWrapper
			.setResponse(regDeviceDetaillService.updateDeviceDetails(deviceDetailRequestDto.getRequest()));
			
		}else {
		responseWrapper
				.setResponse(deviceDetaillService.updateDeviceDetails(deviceDetailRequestDto.getRequest()));
		}
		auditUtil.auditRequest(
				String.format(DeviceConstant.SUCCESSFUL_UPDATE , DeviceDetailDto.class.getCanonicalName()),
				DeviceConstant.AUDIT_SYSTEM,
				String.format(DeviceConstant.SUCCESSFUL_UPDATE , DeviceDetailDto.class.getCanonicalName()),
				"AUT-007");
		return responseWrapper;
	}
	
	/**
	 * 
	 * @param deviceDetailRequestDto
	 * @return
	 */
	//@PreAuthorize("hasAnyRole('PARTNER_ADMIN')")
	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPatchdevicedetail())")
	@PatchMapping
	@ApiOperation(value = "Service to approve/reject DeviceDetail", notes = "Approve DeviceDetail and returns success message")
	@ApiResponses({ @ApiResponse(code = 201, message = "When DeviceDetail successfully approved/rejected"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While approving/rejecting DeviceDetail any error occured") })
	public ResponseWrapper<String> approveDeviceDetails(
			@Valid @RequestBody RequestWrapper<UpdateDeviceDetailStatusDto> deviceDetailRequestDto){
		auditUtil.auditRequest(
				DeviceConstant.STATUS_UPDATE_API_IS_CALLED + UpdateDeviceDetailStatusDto.class.getCanonicalName(),
				DeviceConstant.AUDIT_SYSTEM,
				DeviceConstant.STATUS_UPDATE_API_IS_CALLED + UpdateDeviceDetailStatusDto.class.getCanonicalName(),
				"AUT-006");
		ResponseWrapper<String> responseWrapper = new ResponseWrapper<>();
		if(deviceDetailRequestDto.getRequest().getIsItForRegistrationDevice()) {
			responseWrapper
			.setResponse(regDeviceDetaillService.updateDeviceDetailStatus(deviceDetailRequestDto.getRequest()));
			
		}else {
			responseWrapper
			.setResponse(deviceDetaillService.updateDeviceDetailStatus(deviceDetailRequestDto.getRequest()));
		}
		auditUtil.auditRequest(
				String.format(DeviceConstant.SUCCESSFUL_UPDATE , UpdateDeviceDetailStatusDto.class.getCanonicalName()),
				DeviceConstant.AUDIT_SYSTEM,
				String.format(DeviceConstant.SUCCESSFUL_UPDATE , UpdateDeviceDetailStatusDto.class.getCanonicalName()),
				"AUT-007");

		return responseWrapper;
	}
	
	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostdevicedetailsearch())")
	@PostMapping("/search")
	//@PreAuthorize("hasAnyRole('DEVICE_PROVIDER','FTM_PROVIDER','PARTNER_ADMIN')")
	public ResponseWrapper<PageResponseDto<DeviceDetailSearchResponseDto>> searchDeviceDetails(
			@RequestBody @Valid RequestWrapper<DeviceSearchDto> request) {
		ResponseWrapper<PageResponseDto<DeviceDetailSearchResponseDto>> responseWrapper = new ResponseWrapper<>();
		if(request.getRequest().getPurpose().toString().equalsIgnoreCase(Purpose.REGISTRATION.toString())) {
			responseWrapper.setResponse(regDeviceDetaillService.searchDeviceDetails(RegDeviceDetail.class, request.getRequest()));
			return responseWrapper;
		} 
		responseWrapper.setResponse(deviceDetaillService.searchDeviceDetails(DeviceDetail.class, request.getRequest()));
		return responseWrapper;
	}
	
	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostdevicedetaildevicetypesearch())")
	@PostMapping("/deviceType/search")
	//@PreAuthorize("hasAnyRole('PARTNER_ADMIN','DEVICE_PROVIDER','FTM_PROVIDER')")
	public ResponseWrapper<PageResponseDto<RegistrationSubTypeDto>> searchDeviceType(
			@RequestBody @Valid RequestWrapper<DeviceSearchDto> request) {
		ResponseWrapper<PageResponseDto<RegistrationSubTypeDto>> responseWrapper = new ResponseWrapper<>();
		if(request.getRequest().getPurpose().toString().equalsIgnoreCase(Purpose.REGISTRATION.toString())){
			responseWrapper.setResponse(regDeviceDetaillService.searchDeviceType(RegRegistrationDeviceSubType.class, request.getRequest()));
			return responseWrapper;
		} 
		responseWrapper.setResponse(deviceDetaillService.searchDeviceType(RegistrationDeviceSubType.class, request.getRequest()));
		return responseWrapper;
	}
	
	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostdevicedetailfiltervalues())")
	@PostMapping("/filtervalues")
	//@PreAuthorize("hasAnyRole('PARTNER_ADMIN','DEVICE_PROVIDER','FTM_PROVIDER')")
	public ResponseWrapper<FilterResponseCodeDto> filterValues(
			@RequestBody @Valid RequestWrapper<DeviceFilterValueDto> request) {
		ResponseWrapper<FilterResponseCodeDto> responseWrapper = new ResponseWrapper<>();
		if(request.getRequest().getPurpose().toString().equalsIgnoreCase(Purpose.REGISTRATION.toString())){
			responseWrapper.setResponse(regDeviceDetaillService.regDeviceFilterValues(request.getRequest()));
			return responseWrapper;
		} 
		responseWrapper.setResponse(deviceDetaillService.deviceFilterValues(request.getRequest()));
		return responseWrapper;

	}
	
	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostdevicedetaildevicetypefiltervalues())")
	@PostMapping("/deviceType/filtervalues")
	//@PreAuthorize("hasAnyRole('PARTNER_ADMIN','DEVICE_PROVIDER','FTM_PROVIDER')")
	public ResponseWrapper<FilterResponseCodeDto> filterDeviceType(
			@RequestBody @Valid RequestWrapper<DeviceFilterValueDto> request) {
		ResponseWrapper<FilterResponseCodeDto> responseWrapper = new ResponseWrapper<>();
		if(request.getRequest().getPurpose().toString().equalsIgnoreCase(Purpose.REGISTRATION.toString())){
			responseWrapper.setResponse(regDeviceDetaillService.regDeviceTypeFilterValues(request.getRequest()));
			return responseWrapper;
		} 
		responseWrapper.setResponse(deviceDetaillService.deviceTypeFilterValues(request.getRequest()));
		return responseWrapper;
	}
	
	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostdevicedetaildevicesubtypefiltervalues())")
	@PostMapping("/deviceSubType/filtervalues")
	//@PreAuthorize("hasAnyRole('PARTNER_ADMIN','DEVICE_PROVIDER','FTM_PROVIDER')")
	public ResponseWrapper<FilterResponseCodeDto> filterDeviceSubType(
			@RequestBody @Valid RequestWrapper<DeviceFilterValueDto> request) {
		ResponseWrapper<FilterResponseCodeDto> responseWrapper = new ResponseWrapper<>();
		if(request.getRequest().getPurpose().toString().equalsIgnoreCase(Purpose.REGISTRATION.toString())){
			responseWrapper.setResponse(regDeviceDetaillService.regDeviceSubTypeFilterValues(request.getRequest()));
			return responseWrapper;
		} 
		responseWrapper.setResponse(deviceDetaillService.deviceSubTypeFilterValues(request.getRequest()));
		return responseWrapper;
	}
}
