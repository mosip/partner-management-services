package io.mosip.pmp.authdevice.controller;

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
import io.mosip.pmp.authdevice.constants.Purpose;
import io.mosip.pmp.authdevice.dto.DeviceDetailDto;
import io.mosip.pmp.authdevice.dto.DeviceDetailUpdateDto;
import io.mosip.pmp.authdevice.dto.IdDto;
import io.mosip.pmp.authdevice.dto.PageResponseDto;
import io.mosip.pmp.authdevice.dto.RegistrationSubTypeDto;
import io.mosip.pmp.authdevice.dto.SearchDto;
import io.mosip.pmp.authdevice.dto.UpdateDeviceDetailStatusDto;
import io.mosip.pmp.authdevice.entity.DeviceDetail;
import io.mosip.pmp.authdevice.entity.RegistrationDeviceSubType;
import io.mosip.pmp.authdevice.service.DeviceDetailService;
import io.mosip.pmp.authdevice.util.AuditUtil;
import io.mosip.pmp.authdevice.util.AuthDeviceConstant;
import io.mosip.pmp.partner.core.RequestWrapper;
import io.mosip.pmp.partner.core.ResponseWrapper;
import io.mosip.pmp.regdevice.entity.RegDeviceDetail;
import io.mosip.pmp.regdevice.entity.RegRegistrationDeviceSubType;
import io.mosip.pmp.regdevice.service.RegDeviceDetailService;
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
	@PreAuthorize("hasRole('DEVICE_PROVIDER')")
	@ResponseFilter
	@PostMapping
	@ApiOperation(value = "Service to save DeviceDetail", notes = "Saves DeviceDetail and return DeviceDetail id")
	@ApiResponses({ @ApiResponse(code = 201, message = "When DeviceDetail successfully created"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While creating DeviceDetail any error occured") })
	public ResponseWrapper<IdDto> createDeviceDetail(
			@Valid @RequestBody RequestWrapper<DeviceDetailDto> deviceDetailRequestDto) {
		auditUtil.auditRequest(
				AuthDeviceConstant.CREATE_API_IS_CALLED + DeviceDetailDto.class.getCanonicalName(),
				AuthDeviceConstant.AUDIT_SYSTEM,
				AuthDeviceConstant.CREATE_API_IS_CALLED + DeviceDetailDto.class.getCanonicalName(),
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
				String.format(AuthDeviceConstant.SUCCESSFUL_CREATE , DeviceDetailDto.class.getCanonicalName()),
				AuthDeviceConstant.AUDIT_SYSTEM,
				String.format(AuthDeviceConstant.SUCCESSFUL_CREATE , DeviceDetailDto.class.getCanonicalName()),
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
	@PreAuthorize("hasRole('DEVICE_PROVIDER')")
	@ResponseFilter
	@PutMapping
	@ApiOperation(value = "Service to update DeviceDetail", notes = "Updates DeviceDetail and returns success message")
	@ApiResponses({ @ApiResponse(code = 201, message = "When DeviceDetail successfully updated"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While updating DeviceDetail any error occured") })
	public ResponseWrapper<IdDto> updateDeviceDetail(
			@Valid @RequestBody RequestWrapper<DeviceDetailUpdateDto> deviceDetailRequestDto) {
		auditUtil.auditRequest(
				AuthDeviceConstant.UPDATE_API_IS_CALLED + DeviceDetailDto.class.getCanonicalName(),
				AuthDeviceConstant.AUDIT_SYSTEM,
				AuthDeviceConstant.UPDATE_API_IS_CALLED + DeviceDetailDto.class.getCanonicalName(),
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
				String.format(AuthDeviceConstant.SUCCESSFUL_UPDATE , DeviceDetailDto.class.getCanonicalName()),
				AuthDeviceConstant.AUDIT_SYSTEM,
				String.format(AuthDeviceConstant.SUCCESSFUL_UPDATE , DeviceDetailDto.class.getCanonicalName()),
				"AUT-007");
		return responseWrapper;
	}
	
	/**
	 * 
	 * @param deviceDetailRequestDto
	 * @return
	 */
	@PreAuthorize("hasRole('DEVICE_PROVIDER')")
	@ResponseFilter
	@PatchMapping
	@ApiOperation(value = "Service to approve/reject DeviceDetail", notes = "Approve DeviceDetail and returns success message")
	@ApiResponses({ @ApiResponse(code = 201, message = "When DeviceDetail successfully approved/rejected"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While approving/rejecting DeviceDetail any error occured") })
	public ResponseWrapper<String> approveDeviceDetails(
			@Valid @RequestBody RequestWrapper<UpdateDeviceDetailStatusDto> deviceDetailRequestDto){
		auditUtil.auditRequest(
				AuthDeviceConstant.STATUS_UPDATE_API_IS_CALLED + UpdateDeviceDetailStatusDto.class.getCanonicalName(),
				AuthDeviceConstant.AUDIT_SYSTEM,
				AuthDeviceConstant.STATUS_UPDATE_API_IS_CALLED + UpdateDeviceDetailStatusDto.class.getCanonicalName(),
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
				String.format(AuthDeviceConstant.SUCCESSFUL_UPDATE , UpdateDeviceDetailStatusDto.class.getCanonicalName()),
				AuthDeviceConstant.AUDIT_SYSTEM,
				String.format(AuthDeviceConstant.SUCCESSFUL_UPDATE , UpdateDeviceDetailStatusDto.class.getCanonicalName()),
				"AUT-007");

		return responseWrapper;
	}
	
	@ResponseFilter
	@PostMapping("/search")
	@PreAuthorize("hasAnyRole('PARTNER','PMS_USER','AUTH_PARTNER','DEVICE_PROVIDER','FTM_PROVIDER','CREDENTIAL_PARTNER','CREDENTIAL_ISSUANCE','CREATE_SHARE','ID_AUTHENTICATION')")
	public ResponseWrapper<PageResponseDto<DeviceDetailDto>> searchDeviceDetails(
			@RequestBody @Valid RequestWrapper<SearchDto> request) {
		ResponseWrapper<PageResponseDto<DeviceDetailDto>> responseWrapper = new ResponseWrapper<>();
		if(request.getRequest().getPurpose().equals(Purpose.REGISTRATION)) {
			responseWrapper.setResponse(regDeviceDetaillService.searchDeviceDetails(RegDeviceDetail.class, request.getRequest()));
			return responseWrapper;
		} 
		responseWrapper.setResponse(deviceDetaillService.searchDeviceDetails(DeviceDetail.class, request.getRequest()));
		return responseWrapper;
	}
	
	@ResponseFilter
	@PostMapping("/deviceType/search")
	@PreAuthorize("hasAnyRole('PARTNER','PMS_USER','AUTH_PARTNER','DEVICE_PROVIDER','FTM_PROVIDER','CREDENTIAL_PARTNER','CREDENTIAL_ISSUANCE','CREATE_SHARE','ID_AUTHENTICATION')")
	public ResponseWrapper<PageResponseDto<RegistrationSubTypeDto>> searchDeviceType(
			@RequestBody @Valid RequestWrapper<SearchDto> request) {
		ResponseWrapper<PageResponseDto<RegistrationSubTypeDto>> responseWrapper = new ResponseWrapper<>();
		if(request.getRequest().getPurpose().equals(Purpose.REGISTRATION)) {
			responseWrapper.setResponse(regDeviceDetaillService.searchDeviceType(RegRegistrationDeviceSubType.class, request.getRequest()));
			return responseWrapper;
		} 
		responseWrapper.setResponse(deviceDetaillService.searchDeviceType(RegistrationDeviceSubType.class, request.getRequest()));
		return responseWrapper;
	}
}
