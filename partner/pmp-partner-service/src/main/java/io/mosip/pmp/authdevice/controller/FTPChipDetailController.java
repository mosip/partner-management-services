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
import io.mosip.pmp.authdevice.dto.FTPChipDetailDto;
import io.mosip.pmp.authdevice.dto.FTPChipDetailStatusDto;
import io.mosip.pmp.authdevice.dto.FTPChipDetailUpdateDto;
import io.mosip.pmp.authdevice.dto.IdDto;
import io.mosip.pmp.authdevice.service.FTPChipDetailService;
import io.mosip.pmp.authdevice.util.AuditUtil;
import io.mosip.pmp.authdevice.util.AuthDeviceConstant;
import io.mosip.pmp.partner.core.RequestWrapper;
import io.mosip.pmp.partner.core.ResponseWrapper;
import io.mosip.pmp.regdevice.service.RegFTPChipDetailService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(value = "/ftpchipdetail")
@Api(tags = { "FTPChipDetail" })
public class FTPChipDetailController {
	
	@Autowired
	AuditUtil auditUtil;
	
	@Autowired	
	FTPChipDetailService ftpChipDetaillService;
	
	@Autowired	
	RegFTPChipDetailService regFtpChipDetailService;
	
	/**
	 * Post API to insert a new row of ftpChipDetail data
	 * 
	 * @param chipDetailRequestDto input parameter chipDetailRequestDto
	 * 
	 * @return ResponseEntity DeviceDetail which is inserted successfully
	 *         {@link ResponseEntity}
	 */
	//@PreAuthorize("hasRole('PARTNER')")
	@ResponseFilter
	@PostMapping
	@ApiOperation(value = "Service to save ftpChipDetail", notes = "Saves ftpChipDetail and return ftpChipDetail id")
	@ApiResponses({ @ApiResponse(code = 201, message = "When ftpChipDetail successfully created"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While creating ftpChipDetail any error occured") })
	public ResponseWrapper<IdDto> createDeviceDetail(
			@Valid @RequestBody RequestWrapper<FTPChipDetailDto> chipDetailRequestDto) {
		auditUtil.auditRequest(
				AuthDeviceConstant.CREATE_API_IS_CALLED + FTPChipDetailDto.class.getCanonicalName(),
				AuthDeviceConstant.AUDIT_SYSTEM,
				AuthDeviceConstant.CREATE_API_IS_CALLED + FTPChipDetailDto.class.getCanonicalName(),
				"AUT-001");
		ResponseWrapper<IdDto> responseWrapper = new ResponseWrapper<>();
		if(chipDetailRequestDto.getRequest().getIsItForRegistrationDevice()) {
			responseWrapper
			.setResponse(regFtpChipDetailService.createFtpChipDetails(chipDetailRequestDto.getRequest()));
			
		}else {
		responseWrapper
				.setResponse(ftpChipDetaillService.createFtpChipDetails(chipDetailRequestDto.getRequest()));
		}
		auditUtil.auditRequest(
				String.format(AuthDeviceConstant.SUCCESSFUL_CREATE , FTPChipDetailDto.class.getCanonicalName()),
				AuthDeviceConstant.AUDIT_SYSTEM,
				String.format(AuthDeviceConstant.SUCCESSFUL_CREATE , FTPChipDetailDto.class.getCanonicalName()),
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
	//@PreAuthorize("hasRole('PARTNER')")
	@ResponseFilter
	@PutMapping
	@ApiOperation(value = "Service to update ftp chip detail", notes = "Updates ftp chip detail and returns success message")
	@ApiResponses({ @ApiResponse(code = 201, message = "When ftp chip detail successfully updated"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While updating ftp chip detail any error occured") })
	public ResponseWrapper<IdDto> updateDeviceDetail(
			@Valid @RequestBody RequestWrapper<FTPChipDetailUpdateDto> ftpChipDetailRequestDto) {
		auditUtil.auditRequest(
				AuthDeviceConstant.UPDATE_API_IS_CALLED + FTPChipDetailUpdateDto.class.getCanonicalName(),
				AuthDeviceConstant.AUDIT_SYSTEM,
				AuthDeviceConstant.UPDATE_API_IS_CALLED + FTPChipDetailUpdateDto.class.getCanonicalName(),
				"AUT-006");
		ResponseWrapper<IdDto> responseWrapper = new ResponseWrapper<>();
		if(ftpChipDetailRequestDto.getRequest().getIsItForRegistrationDevice()) {
			responseWrapper
			.setResponse(regFtpChipDetailService.updateFtpChipDetails(ftpChipDetailRequestDto.getRequest()));
			
		}else {
		responseWrapper
				.setResponse(ftpChipDetaillService.updateFtpChipDetails(ftpChipDetailRequestDto.getRequest()));
		}
		auditUtil.auditRequest(
				String.format(AuthDeviceConstant.SUCCESSFUL_UPDATE , FTPChipDetailUpdateDto.class.getCanonicalName()),
				AuthDeviceConstant.AUDIT_SYSTEM,
				String.format(AuthDeviceConstant.SUCCESSFUL_UPDATE , FTPChipDetailUpdateDto.class.getCanonicalName()),
				"AUT-007");
		return responseWrapper;
	}
	
	/**
	 * 
	 * @param deviceDetailRequestDto
	 * @return
	 */
	//@PreAuthorize("hasRole('PARTNERMANAGER')")
	@ResponseFilter
	@PatchMapping
	@ApiOperation(value = "Service to approve/reject ftp chip detail", notes = "Approve ftp chip detail and returns success message")
	@ApiResponses({ @ApiResponse(code = 201, message = "When ftp chip detail successfully approved/rejected"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While approving/rejecting ftp chip detail any error occured") })
	public ResponseWrapper<String> approveDeviceDetails(
			@Valid @RequestBody RequestWrapper<FTPChipDetailStatusDto> chipDetailRequestDto){
		
		auditUtil.auditRequest(
				AuthDeviceConstant.STATUS_UPDATE_API_IS_CALLED + FTPChipDetailStatusDto.class.getCanonicalName(),
				AuthDeviceConstant.AUDIT_SYSTEM,
				AuthDeviceConstant.STATUS_UPDATE_API_IS_CALLED + FTPChipDetailStatusDto.class.getCanonicalName(),
				"AUT-006");
		ResponseWrapper<String> responseWrapper = new ResponseWrapper<>();
		if(chipDetailRequestDto.getRequest().getIsItForRegistrationDevice()) {
			responseWrapper
			.setResponse(regFtpChipDetailService.updateFtpChipDetailStatus(chipDetailRequestDto.getRequest()));
			
		}else {
			responseWrapper
			.setResponse(ftpChipDetaillService.updateFtpChipDetailStatus(chipDetailRequestDto.getRequest()));
		}
		auditUtil.auditRequest(
				String.format(AuthDeviceConstant.SUCCESSFUL_UPDATE , FTPChipDetailStatusDto.class.getCanonicalName()),
				AuthDeviceConstant.AUDIT_SYSTEM,
				String.format(AuthDeviceConstant.SUCCESSFUL_UPDATE , FTPChipDetailStatusDto.class.getCanonicalName()),
				"AUT-007");

		return responseWrapper;
	}
}
